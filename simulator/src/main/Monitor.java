package main;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Monitor {

    private Semaphore mutex = new Semaphore(1, true);
    private RDP rdp;
    private Politica politica = new Politica();
    private Colas colas = new Colas(15);
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

            if(noEncolar(transicion)){
               // System.out.println(Thread.currentThread().getName() + " no logró disparar " + transicion);
                mutex.release();
                return false;
            }

            int[] vectorSens = rdp.sensibilizadas();
            int[] vectorCola = colas.quienesEstan();
            int m = funcionAND(vectorSens, vectorCola);

            mutex.release();

            if (m != 0) {//si
                mutex.acquire();
                int despertarCola = politica.cual(vectorSens, vectorCola);//cual despierto?
                System.out.println(Thread.currentThread().getName() + "\t se sensibilizo " + despertarCola + " -> despertando");
                colas.signal(despertarCola); //despierta a un thread que estaba esperando poder disparar esa trans
                mutex.release();
            }

            System.out.println(Thread.currentThread().getName() + "\t no logro disparar " + transicion + " -> encolando");
            colas.await(transicion);
            mutex.acquire();
        }

        System.out.println(Thread.currentThread().getName() + "\t disparo " + transicion);

        int[] vectorSens = rdp.sensibilizadas();
        int[] vectorCola = colas.quienesEstan();
        int m = funcionAND(vectorSens, vectorCola); //hay disparos encolados y sensibilizados?

        if (m != 0) {//si
            int despertarCola = politica.cual(vectorSens, vectorCola);//cual despierto?
            System.out.println(Thread.currentThread().getName() + "\t se sensibilizo " + despertarCola + " -> despertando");
            colas.signal(despertarCola); //despierta a un thread que estaba esperando poder disparar esa trans
        }
        mutex.release();//libero monitor asi alguno lo puede adquirir y devuelvo true;

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

    //SE ACEPTAN CAMBIOS EN ESTE METODO :)
    private boolean noEncolar(Transicion transicion){
        if(transicion == Transicion.GENERAR_TAREA || transicion == Transicion.T4 || transicion == Transicion.T10
                || transicion == Transicion.PROCESANDO_EN_NUCLEO_1 || transicion == Transicion.PROCESANDO_EN_NUCLEO_2)
            return  true;
        return false;
    }
}
