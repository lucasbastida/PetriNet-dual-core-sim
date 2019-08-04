package hilo;
//Esta clase es absolutamente innecesaria
import main.Monitor;
import main.Transicion;

import java.io.IOException;

public class HiloTemporizado implements Runnable {

    private Transicion[] secuenciaDeDisparos;
    private Monitor monitor;
    private long tiempo;

    public HiloTemporizado(Transicion[] secuenciaDeDisparos, Monitor monitor, long tiempo) {
        this.secuenciaDeDisparos = secuenciaDeDisparos;
        this.monitor = monitor;
        this.tiempo = tiempo;
    }

    public void run() {
        while (true) {
            for(Transicion transicion: secuenciaDeDisparos) {
                try {
                    monitor.dispararTransicion(transicion);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

   /* public void run() {
        while (true) {
            for (Transicion transicion : secuenciaDeDisparos) {
                try {
                    if (monitor.dispararTransicion(transicion)) {
                        if (transicion == Transicion.T4 || transicion == Transicion.T10) {
                            Thread.sleep(tiempo);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }*/
}