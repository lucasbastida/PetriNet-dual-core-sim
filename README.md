# dual-core processor using petri net
A dual-core processor simulator implemented using petri net and its generalized equation 

### Consideracion (implementacion faltante)
El comportamiento segun la semantica de disparo [a,b] es la siguiente: El hilo se duerme por un tiempo para alcanzar el valor de a (en caso de despertarse y ser menor que a) para luego desperatrse y caer dentro del intervalo. Puede suceder que se despierte y debido al comportamiento del sistema (prioridad de los demas hilos o por el mismo comportamiento de la JVM), intenta disparar y se encuentra que ha superado b. En esta situacion el hilo deberia encolarse, es decir, pasar a un estado bloqueado hasta que sea requerido (sensibilizado). 
Sin embargo, esto no sucede porque, por la naturaleza del problema, esto nunca va a suceder ya que la semantica temporal en este caso es de [a.inf].

Por lo tanto para que funcione el comportamiento descripto anteriormente hace falta implementarlo.

Si se utiliza este codigo con un intervalo temporal de, e.g., [1,2] y se despierta pasando la unidad 2, se volvera a dormir segun la cantidad calculada anteriormente.
