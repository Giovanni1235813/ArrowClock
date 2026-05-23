/**
 * Calcola l'etichetta del pulsante (T) che indica
 * quale gruppo inizierà il prossimo turno.
 */
public class GeneratoreTestoBottone {

    private final String modalita;
    private final int indicePartenza;

    public GeneratoreTestoBottone(String modalita, int indicePartenza) {
        this.modalita = modalita;
        this.indicePartenza = indicePartenza;
    }

    public String genera() {
        if (modalita.equals("- Nessuno -")) return GestoreLingua.t("btn.turno.disabilitato");
        if (modalita.equals("ABC"))         return GestoreLingua.t("btn.turno.unico");

        String[] gruppi = modalita.split(" - ");
        int offset = indicePartenza % gruppi.length;
        return GestoreLingua.tf("btn.turno.inizia", gruppi[offset % gruppi.length]);
    }
}
