package main;

import java.util.concurrent.ThreadLocalRandom;

public class Politica {


    private int[] politica = new int[16];
    //a mayor val mayor prioridad

    public int cual(int[] vectorSens, int[] vectorCola) {

        int vectorAND[] = new int[vectorSens.length];
        int max = 0;
        int transicion = 0;

        for (int i = 0; i < vectorSens.length; i++) {
            if (vectorSens[i] == 1 && vectorCola[i] == 1) {
                vectorAND[i] = 1;
            } else {
                vectorAND[i] = 0;
            }
        }

        //para tratar de ser no deterministico en la eleccion de la trans a despertar
        for (int i = 0; i < politica.length; i++) {
            politica[i] = ThreadLocalRandom.current().nextInt(0,200);
        }

        for (int i = 0; i < vectorSens.length; i++) {
            if (vectorAND[i] == 1 && politica[i] > max) {
                max = politica[i];
                transicion = i;
            }
        }
        return transicion;
    }

    public Transicion elegirBuffer(Buffer buffer1, Buffer buffer2){
        if(buffer1.getEstado() > buffer2.getEstado()){
            return Transicion.TAREA_A_BUFFER_2;
        }else return Transicion.TAREA_A_BUFFER_1;
    }

}
