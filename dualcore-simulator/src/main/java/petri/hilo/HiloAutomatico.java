package hilo;

import petri.Monitor;
import petri.Transicion;

import java.io.IOException;

public class HiloAutomatico implements Runnable {

    private Transicion[] secuenciaDeDisparos;
    private Monitor monitor;

    public HiloAutomatico( Transicion[] secuenciaDeDisparos, Monitor monitor) {
        this.secuenciaDeDisparos = secuenciaDeDisparos;
        this.monitor = monitor;
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
}
