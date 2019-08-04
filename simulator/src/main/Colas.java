package main;/*
 * CREAS CONDITIONS CON TIEMPO PARA ARRIVAL Y SERVICE RATE
 *
 * */

import java.util.concurrent.Semaphore;

public class Colas {

    private Semaphore[] arregloSemaphores;
    private int numeroTransiciones;

    public Colas(int numeroTransiciones) {
        this.numeroTransiciones = numeroTransiciones;
        arregloSemaphores = new Semaphore[numeroTransiciones];

        for (int i = 0; i < numeroTransiciones; i++) {
            arregloSemaphores[i] = new Semaphore(0);
        }
    }

    public void signal(int i) {
        arregloSemaphores[i].release();
    }

    public void await(Transicion transicion) throws InterruptedException {
        arregloSemaphores[transicion.getValor()].acquire();
    }

    public int[] quienesEstan() {

        int[] vectorCola = new int[numeroTransiciones];

        for (int i = 0; i < arregloSemaphores.length; i++) {
            if (arregloSemaphores[i].hasQueuedThreads()) {
                vectorCola[i] = 1;
            }
        }
        return vectorCola;
    }

}