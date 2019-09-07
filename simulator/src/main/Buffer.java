package main;/*
 *   main.Buffer sincronizado para log manager
 * */


import java.util.LinkedList;
import java.util.Queue;

class Buffer {

    private Queue<Object> buffer;


    public Buffer() {
        this.buffer = new LinkedList<>();

    }

    /*
     * Agrega un objeto en la COLA del buffer
     * */
    public void add(Object e) {

        buffer.add(e);

    }

    /*
     * Retorna el HEAD del buffer
     * */
    public Object remove() {

        return buffer.remove();

    }

    public int getEstado(){
        return buffer.size();
    }

    @Override
    public String toString() {

        return "main.Buffer{" +
                "size= " + buffer.size()+
                '}';

    }
}