import java.util.List;
import java.util.Objects;

/** Micro-libreria di asserzioni per i test (nessuna dipendenza esterna). */
public class Verifica {

    public static void uguale(String cosa, Object atteso, Object ottenuto) {
        if (!Objects.equals(atteso, ottenuto)) {
            throw new AssertionError(cosa + " → atteso <" + atteso + "> ma ottenuto <" + ottenuto + ">");
        }
    }

    public static void vero(String cosa, boolean condizione) {
        if (!condizione) throw new AssertionError(cosa + " → condizione falsa");
    }

    public static void contiene(String cosa, List<String> righe, String frammento) {
        for (String r : righe) if (r.contains(frammento)) return;
        throw new AssertionError(cosa + " → nessuna riga contiene <" + frammento + ">. Righe: " + righe);
    }
}