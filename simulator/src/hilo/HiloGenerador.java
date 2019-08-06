package hilo;

import main.Monitor;
import main.Transicion;

import java.io.IOException;

public class HiloGenerador implements Runnable {

    private Transicion transicion;
    private Monitor monitor;

    public HiloGenerador(Transicion transicion, Monitor monitor) {
        this.transicion = transicion;
        this.monitor = monitor;
    }

    public void run() {
        int tareas = 0;
        while (tareas < 1000) {
            try {
                if (monitor.dispararTransicion(transicion)) {
                    tareas++;
                    System.out.println("#################################################################################################################" + tareas);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
