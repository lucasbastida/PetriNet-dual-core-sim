package main;

import java.util.concurrent.ThreadLocalRandom;

public class Politica {

    //a mayor val mayor prioridad
    private int[] politica;
    private Buffer buffer1;
    private Buffer buffer2;

    Politica(Buffer buffer1, Buffer buffer2, int numeroTransiciones) {
        this.buffer1 = buffer1;
        this.buffer2 = buffer2;
        politica = new int[numeroTransiciones];

        for (int i = 0; i < politica.length; i++) {
            politica[i] = ThreadLocalRandom.current().nextInt(0, 200);
        }
    }

    public int cual(int[] vectorSens, int[] vectorCola) {

        int vectorAND[] = new int[vectorSens.length];
        int max = 0;
        int transicion = 0;

        for (Transicion t :
                Transicion.values()) {
            if (vectorSens[t.getValor()] == 1 && vectorCola[t.getValor()] == 1 && !t.esTemporizada()) {
                vectorAND[t.getValor()] = 1;
            } else {
                vectorAND[t.getValor()] = 0;
            }
        }

        //despertar segun tamaño buffer
        if (vectorAND[Transicion.TAREA_A_BUFFER_1.getValor()] == 1) {
            if (buffer1.getEstado() > buffer2.getEstado()) {
                return Transicion.TAREA_A_BUFFER_2.getValor();
            } else {
                return Transicion.TAREA_A_BUFFER_1.getValor();
            }
        }

        //despertar cualquier otra sens
        for (int i = 0; i < vectorSens.length; i++) {
            if (vectorAND[i] == 1 && politica[i] > max) {
                max = politica[i];
                transicion = i;
            }
        }
        return transicion;
    }

}
