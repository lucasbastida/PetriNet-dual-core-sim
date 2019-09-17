package petri;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


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
    public long[] sleepAmount;

    private int nucleo1;
    private int nucleo2;
    private Buffer buffer1;
    private Buffer buffer2;
    private LogFileManager log;
    private boolean pInv = true;


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
        sleepAmount = new long[numeroTransiciones];

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
            //actualizar marcado
            setMarcadoActual(transicion);
            checkPInvariant();

            //si se disparo una temporizada, reset a 0
            if (transicion.esTemporizada()) {
                timeStamp[transicion.getValor()] = 0;
            }

            //cuando dispara actualiza nuevo sensibilizado extendido
            calcularSensibilizadoExtendido();

            //accion realizada al disparar una transicion
            modificarBuffer(transicion);
            checkProcesados(transicion);

            //log y print en console
            log.escribirDatos(datosArchivo(transicion.getValor()));
            System.out.println(datosArchivo(transicion.getValor()));
            if (!pInv){
                throw new RuntimeException("NO SE CUMPLIO UN P INVARIANTE");
            }

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

    private void calcularTranSens() {

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
        long diferencia = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timeStamp[transicion]);
        if (diferencia < intervalos[transicion][0]) {
            sleepAmount[transicion] = intervalos[transicion][0] - diferencia;
            return false;
        }
        if(diferencia > intervalos[transicion][1]){
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
                    timeStamp[i] = System.nanoTime();
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

    private String datosArchivo(int transicion) {
        String print =
                "disparo=" + transicion +
                "\nmarcado=" + Arrays.toString(marcadoActual) +
                "\nsensibilizado extendido=" + Arrays.toString(sensibilizadoExtendido) +
                "\nbuffer1=" + marcadoActual[2] +
                "\nbuffer2=" + marcadoActual[9] +
                "\ntotalProcesadas1 =" + nucleo1 +
                "\ntotalProcesadas2 =" + nucleo2 +
                "\nInvariantes M(P0)+M(P1)=" + marcadoActual[0] + "+" + marcadoActual[1] + "= 1" +
                "\nInvariantes M(P10)+M(P11)= " + marcadoActual[10] + "+" + marcadoActual[11] + "=1" +
                "\nInvariantes M(P12)+M(P13)+M(P15)=" + marcadoActual[12] + "+" + marcadoActual[13] + "+" + marcadoActual[15] + "=1" +
                "\nInvariantes M(P5)+M(P7)+M(P8)=" + marcadoActual[5] + "+" + marcadoActual[7] + "+" + marcadoActual[8] + "=1" +
                "\nInvariantes M(P3)+M(P4)=" + marcadoActual[3] + "+" + marcadoActual[4] + "=1 ";

        if (!pInv){
            print += "\nNO SE CUMPLIO UN P-INVARIANTE";
        }

        return print;
    }

    private String datosArchivo() {
        return "marcado=" + Arrays.toString(marcadoActual) +
                "\nsensibilizado extendido=" + Arrays.toString(sensibilizadoExtendido) +
                "\nbuffer1=" + marcadoActual[2] +
                "\nbuffer2=" + marcadoActual[9] +
                "\ntotalProcesadas1 =" + nucleo1 +
                "\ntotalProcesadas2 =" + nucleo2;
    }

    private void checkPInvariant(){
        if (marcadoActual[0] + marcadoActual[1] != 1
                || marcadoActual[10] + marcadoActual[11] != 1
                || marcadoActual[12] + marcadoActual[13] + marcadoActual[15] != 1
                || marcadoActual[5] + marcadoActual[7] + marcadoActual[8] != 1
                || marcadoActual[3] + marcadoActual[4] != 1){
            pInv = false;
        }
    }


    /*
     *   METODOS PARA CARGAR LOS ARCHIVOS
     *   refactor so it becomes only 1 method.
     * */
    private void cargarMarcadoInicial(String file_name, int numeroPlazas) {

        marcadoActual = new int[numeroPlazas];

        try {

            InputStream in = getClass().getResourceAsStream("/"+file_name);
            DataInputStream entrada = new DataInputStream(in);
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

            InputStream in = getClass().getResourceAsStream("/"+file_name);
            DataInputStream entrada = new DataInputStream(in);
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