package main;

import java.io.*;
import java.util.Arrays;


public class RDP {

    private final int numeroPlazas;
    private final int numeroTransiciones;

    //n x m (plazas x transiciones)
    private int[][] incidenciaPre;
    private int[][] incidenciaPos;
    private int[][] inhibicion;
    //n filas (plazas)
    private int[] marcadoActual;

    //m columnas (transiciones)
    private int[] sensibilizado;
    private int[] desensibilizadasInhibidor;
    private int[] desensibilizadasTiempo;
    private int[] sensibilizadoExtendido; //usado en calculo final

    private long[] timeStamp;
    private boolean[] temporizadas;
    private int[][] intervalos;     //m x 2 filas(transiciones x 2)

    private int nucleo1;
    private int nucleo2;
    private Buffer buffer1;
    private Buffer buffer2;
    private LogFileManager log;


    public RDP(LogFileManager log, Buffer buffer1, Buffer buffer2, int numeroPlazas, int numeroTransiciones) throws IOException {
        this.numeroPlazas = numeroPlazas;
        this.numeroTransiciones = numeroTransiciones;

        incidenciaPre = cargarMatriz("Matriz incidencia pre.txt", numeroPlazas, numeroTransiciones);
        incidenciaPos = cargarMatriz("Matriz incidencia pos.txt", numeroPlazas, numeroTransiciones);
        inhibicion = cargarMatriz("Matriz de inhibicion.txt", numeroPlazas, numeroTransiciones);
        intervalos = cargarMatriz("Intervalos temporales.txt", numeroTransiciones, 2);
        cargarMarcadoInicial("Marcado inicial.txt", numeroPlazas);


        sensibilizado = new int[numeroTransiciones];//numero de transiciones
        desensibilizadasInhibidor = new int[numeroTransiciones];//numero de transiciones
        desensibilizadasTiempo = new int[numeroTransiciones];
        sensibilizadoExtendido = new int[numeroTransiciones];

        timeStamp = new long[numeroTransiciones];
        temporizadas = new boolean[numeroTransiciones];

        setTemporizadas();
        Arrays.fill(timeStamp, 0);

        calcularSensibilizadoExtendido();

        this.buffer1 = buffer1;
        this.buffer2 = buffer2;
        this.log = log;
        this.log.escribirDatos(datosArchivo());

    }

    public boolean disparar(Transicion transicion) throws IOException {

        if (estaSensibilizada(transicion)) {
            setMarcadoActual(transicion);

            //si se disparo una temporizada, reset a 0
            if (transicion.esTemporizada()) {
                timeStamp[transicion.getValor()] = 0;
            }

            calcularSensibilizadoExtendido();//cuando dispara actualiza nuevo sensibilizado extendido

            //accion realizada al disparar una transicion
            modificarBuffer(transicion);
            checkProcesados(transicion);

            System.out.println("marcado: " + Arrays.toString(marcadoActual));
            System.out.println(toString());
            log.escribirDatos(datosArchivo(transicion.getValor()));
            return true;
        }
        return false;
    }

    private void calcularSensibilizadoExtendido() {
        calcularTranSens();
        calcularDesensibilizadasInhibidor();
        calcularDesensibilizadasTiempo();

        for (int i = 0; i < numeroTransiciones; i++) {
            if (sensibilizado[i] == 1 && desensibilizadasInhibidor[i] == 1 && desensibilizadasTiempo[i] == 1)
                sensibilizadoExtendido[i] = 1;
            else sensibilizadoExtendido[i] = 0;
        }

    }

    public void calcularTranSens() {

        int[] vectorE = new int[numeroTransiciones];
        Arrays.fill(vectorE, 1);

        for (int i = 0; i < numeroTransiciones; i++) {
            int[] vectorSi = new int[numeroPlazas];
            for (int j = 0; j < numeroPlazas; j++) {
                vectorSi[j] = marcadoActual[j] - incidenciaPre[j][i];
            }

            for (int j = 0; j < vectorSi.length; j++) {
                if (vectorSi[j] < 0) {
                    vectorE[i] = 0;
                }
            }
        }
        sensibilizado = vectorE;
    }

    public void calcularDesensibilizadasInhibidor() {
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
        setTimeStamp();
        Arrays.fill(desensibilizadasTiempo, 1);
        for (int i = 0; i < numeroTransiciones; i++) {
            if (temporizadas[i]) { //Se fija si se cumplio el tiempo solo si la transicion es temporizada
                if (!enVentanaTiempo(i)) {
                    desensibilizadasTiempo[i] = 0;
                }
            }
        }
    }

    /*
     * @return true si transcurrio el tiempo de dura el disparo de la transicion?
     */
    private boolean enVentanaTiempo(int transicion) {
        if (System.currentTimeMillis() - timeStamp[transicion] < intervalos[transicion][0]) {
            return false;
        }
        return true;
    }

    /*
     *Setea el comienzo del tiempo de sensibilizado de una transicion
     */
    private void setTimeStamp() {
        for (int i = 0; i < numeroTransiciones; i++) {
            if (sensibilizado[i] == 1 && desensibilizadasInhibidor[i] == 1 && temporizadas[i]) {
                if (timeStamp[i] == 0)
                    timeStamp[i] = System.currentTimeMillis();
            }
        }
    }

    /*
     * sets boolean array indicating which transitions are timed.
     * */
    private void setTemporizadas() {
        for (Transicion transicion : Transicion.values()) {
            temporizadas[transicion.getValor()] = transicion.esTemporizada();
        }
    }

    private void setMarcadoActual(Transicion transicion) {
        System.out.println("disparo: " + transicion);

        int[] nuevoMarcado = new int[numeroPlazas];
        for (int i = 0; i < nuevoMarcado.length; i++) {
            nuevoMarcado[i] = marcadoActual[i] +
                    incidenciaPos[i][transicion.getValor()] - incidenciaPre[i][transicion.getValor()];
        }
        marcadoActual = nuevoMarcado;
    }

    /*
     * @return true si la transicion esta sensibilizada, no esta inhibida por un arco inhibidor
     * y esta dentro del tiempo de disparo
     */
    private boolean estaSensibilizada(Transicion transicion) {
        calcularSensibilizadoExtendido(); //para actualizar extendido (actualizar timestamp)
        return sensibilizadoExtendido[transicion.getValor()] == 1;
    }

    public int[] sensibilizadas() {
        return sensibilizadoExtendido;
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

    public String datosArchivo(int transicion) {
        return "disparo=" + transicion +
                "\nmarcado=" + Arrays.toString(marcadoActual) +
                "\nsensibilizado extendido=" + Arrays.toString(sensibilizadoExtendido) +
                "\nbuffer1=" + marcadoActual[2] +
                "\nbuffer2=" + marcadoActual[9] +
                "\ntotalProcesadas1 =" + nucleo1 +
                "\ntotalProcesadas2 =" + nucleo2;
    }

    public String datosArchivo() {
        return "marcado=" + Arrays.toString(marcadoActual) +
                "\nsensibilizado extendido=" + Arrays.toString(sensibilizadoExtendido) +
                "\nbuffer1=" + marcadoActual[2] +
                "\nbuffer2=" + marcadoActual[9] +
                "\ntotalProcesadas1 =" + nucleo1 +
                "\ntotalProcesadas2 =" + nucleo2;
    }

    @Override
    public String toString() {
        String print = "RDP{" +
                "\n, marcadoActual=" + Arrays.toString(marcadoActual) +
                "\n, sensibilizado=" + Arrays.toString(sensibilizado) +
                "\n, desensibilizada por arco=" + Arrays.toString(desensibilizadasInhibidor) +
                "\n, desensibilizada por tiempo=" + Arrays.toString(desensibilizadasTiempo) +
                "\n, sensibilizado extendido=" + Arrays.toString(sensibilizadoExtendido) +
                "\n, tareas en buffer 1 =" + marcadoActual[2] +
                "\n, tareas en buffer 2 =" + marcadoActual[9] +
                "\n, tareas finalizadas en nucleo 1 =" + nucleo1 +
                "\n, tareas finalizadas en nucleo 2 =" + nucleo2 +
                "\n, Invariantes M(P0)+M(P1)=" + marcadoActual[0] + "+" + marcadoActual[1] + "= 1" +
                "\n, Invariantes M(P10)+M(P11) = " + marcadoActual[10] + "+" + marcadoActual[11] + "=1" +
                "\n, Invariantes M(P12)+M(P13)+M(P15)=" + marcadoActual[12] + "+" + marcadoActual[13] + "+" + marcadoActual[15] + "=1" +
                "\n, Invariantes M(P5)+M(P7)+M(P8)=" + marcadoActual[5] + "+" + marcadoActual[7] + "+" + marcadoActual[8] + "=1" +
                "\n, Invariantes M(P3)+M(P4)=" + marcadoActual[3] + "+" + marcadoActual[4] + "=1 " +
                "\n}";

        assert (marcadoActual[0] + marcadoActual[1] == 1) : "no se cumplio un p-invariante";
        assert (marcadoActual[10] + marcadoActual[11] == 1) : "no se cumplio un p-invariante";
        assert (marcadoActual[12] + marcadoActual[13] + marcadoActual[15] == 1) : "no se cumplio un p-invariante";
        assert (marcadoActual[5] + marcadoActual[7] + marcadoActual[8] == 1) : "no se cumplio un p-invariante";
        assert (marcadoActual[3] + marcadoActual[4] == 1) : "no se cumplio un p-invariante";

        return print;
    }

    /*
     *   METODOS PARA CARGAR LOS ARCHIVOS
     *   refactor so it becomes only 1 method.
     * */
    private void cargarMarcadoInicial(String file_name, int numeroPlazas) {

        marcadoActual = new int[numeroPlazas];

        try {

            FileInputStream fstream = new FileInputStream(file_name);
            DataInputStream entrada = new DataInputStream(fstream);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));

            String strLinea;

            while ((strLinea = buffer.readLine()) != null) {
                String[] linea = strLinea.split(",");
                for (int j = 0; j < numeroPlazas; j++) {
                    marcadoActual[j] = Integer.parseInt(linea[j]);
                }
            }

            entrada.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[][] cargarMatriz(String file_name, int numeroFilas, int numeroColumnas) {

        int[][] matriz = new int[numeroFilas][numeroColumnas];

        try {

            FileInputStream fstream = new FileInputStream(file_name);
            DataInputStream entrada = new DataInputStream(fstream);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));

            String strLinea;

            for (int fila = 0; (strLinea = buffer.readLine()) != null; fila++) {
                String[] linea = strLinea.split(",");
                for (int columna = 0; columna < numeroColumnas; columna++) {
                    matriz[fila][columna] = Integer.parseInt(linea[columna]);
                }
            }

            entrada.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matriz;
    }
}