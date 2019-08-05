package main;

import java.io.*;
import java.util.Arrays;


class RDP {

    private final int numeroPlazas = 16;
    private final int numeroTransiciones = 15;

    private int[][] incidencia;
    private int[][] incidenciaPre;
    private int[][] inhibicion;

    private int[] marcadoActual;

    private int[] sensibilizado;
    private int[] desensibilizadasInhibidor;
    private int[] desensibilizadasTiempo;
    private int[] sensibilizadoExtendido;

    private int[][] intervalos;
    private final long[] timeStamp;
    private final boolean[] temporizadas;

    private int nucleo1;
    private int nucleo2;

    private final Buffer buffer1;
    private final Buffer buffer2;
    private final LogFileManager log;


    public RDP(LogFileManager log, Buffer buffer1, Buffer buffer2) throws IOException {

        temporizadas = new boolean[numeroTransiciones];
        sensibilizado = new int[numeroTransiciones];//numero de transiciones
        desensibilizadasInhibidor = new int[numeroTransiciones];//numero de transiciones
        desensibilizadasTiempo = new int[numeroTransiciones];
        sensibilizadoExtendido = new int[numeroTransiciones];
        timeStamp = new long[numeroTransiciones];

        setTemporizadas();
        Arrays.fill(timeStamp, 0);
        calcularDesensibilizadasInhibidor();
        calcularTranSens();
        setTimeStamp();
        //calcularDesensibilizadasTiempo();
        // calcularSensibilizadoExtendido();

        this.buffer1 = buffer1;
        this.buffer2 = buffer2;
        this.log = log;
        this.log.escribirDatos(datosArchivo());

    }

    public boolean disparar(Transicion transicion) throws IOException {

        if (estaSensibilizada(transicion)) {
            setMarcadoActual(transicion);
            modificarBuffer(transicion);
            System.out.println("marcado: " + Arrays.toString(marcadoActual));

            calcularTranSens();
            calcularDesensibilizadasInhibidor();
            setTimeStamp();
            //calcularSensibilizadoExtendido();
            checkProcesados(transicion);
            System.out.println(toString());

            log.escribirDatos(datosArchivo(transicion.getValor()));
            return true;
        }
        return false;
    }

    private void calcularTranSens() {

        /*int[] vectorE = new int[numeroTransiciones];
        Arrays.fill(vectorE, 1);

        for (int i = 0; i < numeroTransiciones; i++) {
            int[] vectorSi = new int[numeroPlazas];
            for (int j = 0; j < numeroPlazas; j++) {
                vectorSi[j] = marcadoActual[j] + incidencia[j][i];
            }

            for (int j = 0; j < vectorSi.length; j++) {
                if (vectorSi[j] < 0) {
                    vectorE[i] = 0;
                }
            }
        }
        sensibilizado = vectorE;
         */
        Arrays.fill(sensibilizado, 0);

        for (int i = 0; i < numeroTransiciones; i++) {
            if (transicionSens(i)) {
                sensibilizado[i] = 1;
            }
        }
    }

    private void calcularDesensibilizadasInhibidor() {
        int[] vectorQ = new int[numeroPlazas];
        Arrays.fill(vectorQ, 0);
        Arrays.fill(desensibilizadasInhibidor, 0);

        for (int i = 0; i < numeroPlazas; i++) {
            if (marcadoActual[i] > 0)
                vectorQ[i] = 1;
        }

        for (int j = 0; j < numeroTransiciones; j++) {
            for (int k = 0; k < numeroPlazas; k++) {
                desensibilizadasInhibidor[j] += vectorQ[k] * inhibicion[k][j];
            }
        }

        for (int k = 0; k < numeroTransiciones; k++) {
            if (desensibilizadasInhibidor[k] == 0)
                desensibilizadasInhibidor[k] = 1;
            else desensibilizadasInhibidor[k] = 0;
        }
    }

    private void calcularDesensibilizadasTiempo() {
        Arrays.fill(desensibilizadasTiempo, 1);

        for (int i = 0; i < numeroTransiciones; i++) {
            if (temporizadas[i]) { //Se fija si se cumplio el tiempo solo si la transicion es temporizada
                if (!testVentanaTiempo(i)) {
                    desensibilizadasTiempo[i] = 0;
                }
            }
        }
    }

    private void calcularSensibilizadoExtendido() {
        calcularDesensibilizadasTiempo();

        for (int i = 0; i < numeroTransiciones; i++) {
            if (sensibilizado[i] == 1 && desensibilizadasInhibidor[i] == 1 && desensibilizadasTiempo[i] == 1)
                sensibilizadoExtendido[i] = 1;
            else sensibilizadoExtendido[i] = 0;
        }
    }


    public int[] sensibilizadas() {
        return sensibilizadoExtendido;
    }

    /*
     * @return true si la transicion esta sensibilizada, no esta inhibida por un arco inhibidor
     * y esta dentro del tiempo de disparo
     */
    private boolean estaSensibilizada(Transicion transicion) {
        calcularSensibilizadoExtendido();
        return sensibilizadoExtendido[transicion.getValor()] == 1;
    }

    private void setMarcadoActual(Transicion transicion) {
        int[] nuevoMarcado = new int[numeroPlazas];
        for (int i = 0; i < nuevoMarcado.length; i++) {
            nuevoMarcado[i] = marcadoActual[i] + incidencia[i][transicion.getValor()];
        }
        System.out.println("disparo: " + transicion);
        marcadoActual = nuevoMarcado;
    }

    private boolean transicionSens(int transicion) { //devuelve true si la transicion esta sensibilizada
        for (int i = 0; i < numeroTransiciones; i++) {
            if (incidenciaPre[i][transicion] == 1 && marcadoActual[i] < 1) { //se fija una transicion y se busca en la matriz de incidencia de salida a lo largo de toda una columna
                return false;                               //y se van variando las plazas(filas). Si se encuentra un uno es porque esa plaza tiene arco  hacia la transicion
            }                                        //entonces se busca esa plaza y se le pregunta su marca: si es menor que 1, la transicion no esta sensibilizada
        }
        return true;
    }

    /*
     * @return true si transcurrio el tiempo de dura el disparo de la transicion?
     */
    private boolean testVentanaTiempo(int transicion) {
        return System.currentTimeMillis() - timeStamp[transicion] >= intervalos[transicion][0];
    }

    /*
     *Setea el comienzo del tiempo de sensibilizado de una transicion
     */
    private void setTimeStamp() {
        for (int i = 0; i < numeroTransiciones; i++) {
            if (sensibilizado[i] == 1 && desensibilizadasInhibidor[i] == 1) {
                if (timeStamp[i] == 0)
                    timeStamp[i] = System.currentTimeMillis();
            } else timeStamp[i] = 0;
        }
        calcularSensibilizadoExtendido();
    }

    private void checkProcesados(Transicion transicion) {

        switch (transicion) {
            case T4:
                nucleo1++;
                break;
            case T10:
                nucleo2++;
                break;
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void modificarBuffer(Transicion transicion) {

        switch (transicion) {
            case TAREA_A_BUFFER_1:
                buffer1.add(new Object());
                break;
            case T4:
                buffer1.remove();
                break;
            case TAREA_A_BUFFER_2:
                buffer2.add(new Object());
                break;
            case T10:
                buffer2.remove();
                break;
        }
    }

    private void setTemporizadas() {
        for (Transicion transicion : Transicion.values()) {
            temporizadas[transicion.getValor()] = transicion.esTemporizada();
        }
    }

    private String datosArchivo(int transicion) {
        return "disparo=" + transicion +
                "\nmarcado=" + Arrays.toString(marcadoActual) +
                "\nsensibilizado extendido=" + Arrays.toString(sensibilizadoExtendido);
    }

    private String datosArchivo() {
        return "marcado=" + Arrays.toString(marcadoActual) +
                "\nsensibilizado extendido=" + Arrays.toString(sensibilizadoExtendido);
    }

    @Override
    public String toString() {
        return "RDP{" +
                "\n, marcadoActual=" + Arrays.toString(marcadoActual) +
                "\n, sensibilizado=" + Arrays.toString(sensibilizado) +
                "\n, desensibilizada por arco=" + Arrays.toString(desensibilizadasInhibidor) +
                "\n, sensibilizado extendido=" + Arrays.toString(sensibilizadoExtendido) +
                "\n, tareas en buffer 1 =" + marcadoActual[2] +
                "\n, tareas en buffer 2 =" + marcadoActual[9] +
                "\n, tareas en nucleo 1 =" + nucleo1 +
                "\n, tareas en nucleo 2 =" + nucleo2 +
                "\n, Invariantes M(P0)+M(P1)=" + marcadoActual[0] + "+" + marcadoActual[1] + "= 1" +
                "\n, Invariantes M(P10)+M(P11) = " + marcadoActual[10] + "+" + marcadoActual[11] + "=1" +
                "\n, Invariantes M(P12)+M(P13)+M(P15)=" + marcadoActual[12] + "+" + marcadoActual[13] + "+" + marcadoActual[15] + "=1" +
                "\n, Invariantes M(P5)+M(P7)+M(P8)=" + marcadoActual[5] + "+" + marcadoActual[7] + "+" + marcadoActual[8] + "=1" +
                "\n, Invariantes M(P3)+M(P4)=" + marcadoActual[3] + "+" + marcadoActual[4] + "=1 " +
                '}';
    }

    private int[][] cargarMatriz(String dir, int fila, int columna) {

        int[][] matriz = new int[fila][columna];

        try {

            FileInputStream fstream = new FileInputStream(dir);
            DataInputStream entrada = new DataInputStream(fstream);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));

            String strLinea;

            for (int i = 0; (strLinea = buffer.readLine()) != null ; i++) {
                String[] linea = strLinea.split(",");
                for (int j = 0; j < columna; i++) {
                    intervalos[i][j] = Integer.parseInt(linea[j]);
                }
            }

            entrada.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matriz;
    }


}