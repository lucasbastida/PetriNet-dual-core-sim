package main;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Monitor {

    public static final int numeroTransiciones = 21;
    public static final int numeroPlazas = 22;

    private Semaphore mutex = new Semaphore(1, true);
    private RDP rdp;
    private Politica politica = new Politica();
    private Colas colas = new Colas(numeroTransiciones);
    private Buffer buffer1 = new Buffer();
    private Buffer buffer2 = new Buffer();

    public Monitor(LogFileManager log) throws IOException {
        this.rdp = new RDP(log, buffer1, buffer2);
    }

    public boolean dispararTransicion(Transicion transicion) throws InterruptedException, IOException {

        mutex.acquire();

        if (transicion == Transicion.TAREA_A_BUFFER_1 || transicion == Transicion.TAREA_A_BUFFER_2) {
            transicion = politica.elegirBuffer(buffer1, buffer2);
        }

        while (!rdp.disparar(transicion)) {

            mutex.release();
            if (noEncolar(transicion)) {
                return false;
            }

            System.out.println(Thread.currentThread().getName() + "\t no logro disparar " + transicion + " -> encolando");
            colas.await(transicion);
            mutex.acquire();
        }

        System.out.println(Thread.currentThread().getName() + "\t disparo " + transicion);

        int[] vectorSens = rdp.sensibilizadas();
        int[] vectorCola = colas.quienesEstan();
        int m = funcionAND(vectorSens, vectorCola);

        if (m != 0) {
            int despertarCola = politica.cual(vectorSens, vectorCola);
            System.out.println(Thread.currentThread().getName() + "\t se sensibilizo " + despertarCola + " -> despertando");
            colas.signal(despertarCola);
        }
        mutex.release();

        return true;
    }

    private int funcionAND(int vectorSensibilizado[], int vectorCola[]) {
        for (int i = 0; i < vectorSensibilizado.length; i++) {
            if (vectorSensibilizado[i] == 1 && vectorCola[i] == 1) {
                return 1;
            }
        }
        return 0;
    }

    private boolean noEncolar(Transicion transicion) {
        if (transicion == Transicion.GENERAR_TAREA
                || transicion == Transicion.PROCESANDO_EN_NUCLEO_1 || transicion == Transicion.PROCESANDO_EN_NUCLEO_2
                || transicion == Transicion.T7 || transicion == Transicion.T17
                || transicion == Transicion.T9 || transicion == Transicion.T19)
            return true;
        return false;
    }
}