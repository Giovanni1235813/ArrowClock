/**
 * Calcola la stringa da mostrare sul display dei turni.
 * Il gruppo attivo è in MAIUSCOLO, gli altri in minuscolo.
 * Fuori gara (ATTESA) tutti i gruppi sono maiuscoli.
 */
public class GeneratoreTestoTurno {

    private final String modalita;
    private final int indicePartenza;
    private final Fase fase;
    private final int turnoCorrente;

    public GeneratoreTestoTurno(String modalita, int indicePartenza, Fase fase, int turnoCorrente) {
        this.modalita = modalita;
        this.indicePartenza = indicePartenza;
        this.fase = fase;
        this.turnoCorrente = turnoCorrente;
    }

    public String genera() {
        if (modalita.equals("- Nessuno -")) return " ";
        if (modalita.equals("ABC")) return "ABC";

        String[] gruppi = modalita.split(" - ");
        int nGruppi = gruppi.length;
        int offset = indicePartenza % nGruppi;

        String[] ruotato = new String[nGruppi];
        for (int i = 0; i < nGruppi; i++) {
            ruotato[i] = gruppi[(i + offset) % nGruppi];
        }

        StringBuilder sb = new StringBuilder();
        if (fase == Fase.ATTESA) {
            for (int i = 0; i < nGruppi; i++) {
                if (i > 0) sb.append(" - ");
                sb.append(ruotato[i].toUpperCase());
            }
        } else {
            int indiceTurnoAttuale = turnoCorrente - 1;
            for (int i = 0; i < nGruppi; i++) {
                if (i > 0) sb.append(" - ");
                sb.append(i == indiceTurnoAttuale
                        ? ruotato[i].toUpperCase()
                        : ruotato[i].toLowerCase());
            }
        }
        return sb.toString();
    }
}
