package main;

import hilo.HiloAutomatico;
import hilo.HiloGenerador;

import java.io.IOException;

public class Main {
    public static long startTime = System.nanoTime();

    public static void main(String[] args) throws IOException {

        LogFileManager log = new LogFileManager();

        Monitor monitor = new Monitor(log);

        Transicion[] secuenciaBuffer1 = {Transicion.TAREA_A_BUFFER_1};
        Transicion[] secuenciaEncencido1 = {Transicion.ENCENDER_CPU_1, Transicion.T3, Transicion.T4, Transicion.APAGAR_CPU_1};
        Transicion[] secuenciaProcesar1 = {Transicion.PROCESANDO_EN_NUCLEO_1, Transicion.T7, Transicion.T9};
        Transicion[] secuenciaTareaExtra1 = {Transicion.NUEVA_TAREA_1, Transicion.T6};

        Transicion[] secuenciaBuffer2 = {Transicion.TAREA_A_BUFFER_2};
        Transicion[] secuenciaEncendido2 = {Transicion.ENCENDER_CPU_2, Transicion.T13, Transicion.T14, Transicion.APAGAR_CPU_2};
        Transicion[] secuenciaProcesar2 = {Transicion.PROCESANDO_EN_NUCLEO_2, Transicion.T17,Transicion.T19};
        Transicion[] secuenciaTareaExtra2 = {Transicion.NUEVA_TAREA_2,Transicion.T16};

        Thread generadorDeTareas = new Thread(new HiloGenerador(Transicion.GENERAR_TAREA, monitor));

        Thread aBuffer1 = new Thread(new HiloAutomatico(secuenciaBuffer1, monitor));
        Thread aBuffer2 = new Thread(new HiloAutomatico(secuenciaBuffer2, monitor));
        Thread nucleoUno = new Thread(new HiloAutomatico(secuenciaProcesar1, monitor));
        Thread nucleoDos = new Thread(new HiloAutomatico(secuenciaProcesar2, monitor));
        Thread CPU1 = new Thread(new HiloAutomatico(secuenciaEncencido1, monitor));
        Thread CPU2 = new Thread(new HiloAutomatico(secuenciaEncendido2, monitor));
        Thread TareaExtra1 = new Thread(new HiloAutomatico(secuenciaTareaExtra1, monitor)); //cambiar nombre
        Thread TareaExtra2 = new Thread(new HiloAutomatico(secuenciaTareaExtra2, monitor));

        generadorDeTareas.setName("Generador de tareas ");

        generadorDeTareas.start();
        aBuffer1.start();
        aBuffer2.start();
        nucleoUno.start();
        nucleoDos.start();
        CPU1.start();
        CPU2.start();
        TareaExtra1.start();
        TareaExtra2.start();
    }
}