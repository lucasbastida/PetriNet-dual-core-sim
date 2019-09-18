package verification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TransitionInvariant {
    public static void init() {

        LinkedList<Integer> disparos = new LinkedList<>();
        readDisparos(disparos, "log.txt");
        int totalRemoved = 0;

        int tinv[][] = {{0, 10, 11, 12, 14, 2, 9},
                {0, 10, 13, 2, 9},
                {0, 1, 3, 4, 6},
                {0, 1, 3, 4, 5, 7, 8}};

        System.out.println("Todos los disparos realizados:");
        for (int i :
                disparos) {
            System.out.println(i);
        }

        for (int[] ints : tinv) {

            boolean conjuntoEnDisparos = true;
            //mientras un conjunto de disparos correspondiente a un tinv este en la lista, borrarlo.
            while (conjuntoEnDisparos) {

                //verificar si un tinv esta en la lista, caso contrario no borrar y continuar con otro t inv.
                boolean deleteSet = true;
                for (int trans = 0; trans < ints.length; trans++) {
                    if (!disparos.contains(ints[trans])) {
                        deleteSet = false;
                        conjuntoEnDisparos = false;
                        break;
                    }
                }

                //borrar 1 conjunto de disparos correspondiente a un tinv de la lista
                if (deleteSet) {
                    totalRemoved++;
                    for (int trans = 0; trans < ints.length; trans++) {
                        disparos.removeFirstOccurrence(ints[trans]);
                    }
                }
            }
        }

        System.out.println("Disparos restantes luego de remover los tinv:");
        for (int i :
                disparos) {
            System.out.print(i + " ");
        }
        System.out.println("Total de invariantes borrados: "+ totalRemoved);

    }

    private static void readDisparos(List<Integer> disparos, String dir) {
        Pattern p = Pattern.compile("disparo=(\\d+)");
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(dir));

            String line = reader.readLine();

            while (line != null) {
                Matcher m = p.matcher(line);

                if (m.matches()) {
                    disparos.add(Integer.valueOf(m.group(1)));
                }
                line = reader.readLine(); //read next line
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
