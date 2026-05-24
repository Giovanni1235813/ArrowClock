/**
 * Aggiorna l'etichetta dei turni su tutti i display e il pulsante (T).
 * Delega il calcolo del testo ai generatori puri GeneratoreTestoTurno
 * e GeneratoreTestoBottone.
 */
public class ComandoAggiornaTestoTurno implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoAggiornaTestoTurno(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.faseAttuale == Fase.EMERGENZA
                || app.faseAttuale == Fase.RECUPERO_ATTESA
                || app.faseAttuale == Fase.RECUPERO_TIRO
                || app.faseAttuale == Fase.IDENTIFICAZIONE_MONITOR) return;

        if (app.isScontroMode) {
            aggiornaTestoScontro();
        } else {
            aggiornaTestoLineare();
        }
    }

    private void aggiornaTestoScontro() {
        app.btnAlternaMetata.setText(GestoreLingua.t(
                app.iniziaConPrimaParte ? "btn.alterna.sx" : "btn.alterna.dx"));
        app.minTurniSx.setText(app.nomeSx);
        app.minTurniDx.setText(app.nomeDx);
        for (DisplayArciere da : app.archerDisplays) {
            da.turniLabelSx.setText(app.nomeSx);
            da.turniLabelDx.setText(app.nomeDx);

            // MODIFICATO: Calcolo dinamico sul nome dello scontro (es. "A - B")
            da.aggiornaTestoEFontTurniSpecial(app.nomeSx + " - " + app.nomeDx);
        }
    }

    private void aggiornaTestoLineare() {
        String mod = String.valueOf(app.comboTurni.getSelectedItem());
        app.btnAlternaMetata.setText(new GeneratoreTestoBottone(mod, app.indicePartenza).genera());
        String testo = new GeneratoreTestoTurno(mod, app.indicePartenza, app.faseAttuale, app.turnoCorrente).genera();

        if (app.minTurniSingolo != null) {
            app.minTurniSingolo.setText(testo);
        }

        for (DisplayArciere da : app.archerDisplays) {
            da.turniLabelSingolo.setText(testo);

            // MODIFICATO: Calcolo dinamico sulla stringa dei turni lineari (es. "AB - cd")
            da.aggiornaTestoEFontTurniSpecial(testo);
        }
    }
}
