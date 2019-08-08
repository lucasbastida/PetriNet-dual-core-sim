package main;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Monitor {

    private static final int numeroTransiciones = 15;
    private static final int numeroPlazas = 16;

    private Buffer buffer1 = new Buffer();
    private Buffer buffer2 = new Buffer();

    private Semaphore mutex = new Semaphore(1, true);
    private Colas colas = new Colas(numeroTransiciones);
    private Politica politica = new Politica(buffer1, buffer2, numeroTransiciones);
    private RDP rdp;


    public Monitor(LogFileManager log) throws IOException {
        this.rdp = new RDP(log, buffer1, buffer2, numeroPlazas, numeroTransiciones);
    }

    public boolean dispararTransicion(Transicion transicion) throws InterruptedException, IOException {

        mutex.acquire();

        while (!rdp.disparar(transicion)) {

            mutex.release();
            if (transicion.esTemporizada()) {
                System.out.println(Thread.currentThread().getName() + "\t no logro disparar " + transicion + " -> durmiendo: "+ rdp.sleepAmount[transicion.getValor()]);
                Thread.sleep(rdp.sleepAmount[transicion.getValor()]);
            } else {
                System.out.println(Thread.currentThread().getName() + "\t no logro disparar " + transicion + " -> encolando");
                colas.await(transicion);
            }
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
}