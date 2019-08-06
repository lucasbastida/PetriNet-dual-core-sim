package main;

public enum Transicion {

    GENERAR_TAREA(0, true),

    TAREA_A_BUFFER_1(1,false),
    ENCENDER_CPU_1(2,false),
    T3(3,false),
    T4(4,false),
    APAGAR_CPU_1(10,false),
    PROCESANDO_EN_NUCLEO_1(8,false),
    T7(7,false),
    T9(9,true),
    NUEVA_TAREA_1(5,false),
    T6(6,false),

    TAREA_A_BUFFER_2(11,false),
    ENCENDER_CPU_2(12,false),
    T13(13,false),
    T14(14,false),
    APAGAR_CPU_2(20,false),
    PROCESANDO_EN_NUCLEO_2(18,false),
    T17(17,false),
    T19(19,true),
    NUEVA_TAREA_2(15,false),
    T16(16,false);

    private int valor;
    private boolean timed;

    Transicion(int valor, boolean timed) {
        this.valor = valor;
        this.timed = timed;
    }

    public int getValor() { return this.valor; }
    public boolean esTemporizada(){return this.timed;}
}


