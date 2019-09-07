package main;

public enum Transicion {

    GENERAR_TAREA(0, true),
    TAREA_A_BUFFER_1(1,false),
    TAREA_A_BUFFER_2(2,false),
    PROCESANDO_EN_NUCLEO_1(3,false),
    T4(4,true),
    ENCENDER_CPU_1(5,false),
    T6(6,false),
    T7(7,false),
    APAGAR_CPU_1(8,false),
    PROCESANDO_EN_NUCLEO_2(9,false),
    T10(10,true),
    ENCENDER_CPU_2(11,false),
    T12(12,false),
    T13(13,false),
    APAGAR_CPU_2(14,false);

    private int valor;
    private boolean timed;

    Transicion(int valor, boolean timed) {
        this.valor = valor;
        this.timed = timed;
    }

    public int getValor() { return this.valor; }
    public boolean esTemporizada(){return this.timed;}
}