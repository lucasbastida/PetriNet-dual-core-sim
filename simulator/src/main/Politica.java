package main;

import java.util.concurrent.ThreadLocalRandom;

public class Politica {

    //a mayor val mayor prioridad
    private int[] politica;

    Politica(){
        politica = new int[Monitor.numeroTransiciones];

        for (int i = 0; i < politica.length; i++) {
            politica[i] = ThreadLocalRandom.current().nextInt(0,200);
        }
    }

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

        for (int i = 0; i < vectorSens.length; i++) {
            if (vectorAND[i] == 1 && politica[i] > max) {
                max = politica[i];
                transicion = i;
            }
        }
        return transicion;
    }

}
