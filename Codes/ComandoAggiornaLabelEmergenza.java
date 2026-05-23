import javax.swing.JLabel;

/**
 * Aggiorna le etichette di tempo mostrate durante l'emergenza.
 * Ogni pannello colorato mostra il tempo congelato al momento
 * dell'attivazione dell'emergenza.
 */
public class ComandoAggiornaLabelEmergenza implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoAggiornaLabelEmergenza(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        String timeSxStr = calcolaTimeSx();
        String timeDxStr = calcolaTimeDx(timeSxStr);

        if (app.minEmergenzaTimeSingolo != null) {
            app.minEmergenzaTimeSingolo.setText(timeSxStr);
            app.minEmergenzaTimeSx.setText(timeSxStr);
            app.minEmergenzaTimeDx.setText(timeDxStr);
        }

        for (JLabel[] labels : app.emergenzaLabelsList) {
            labels[0].setText(timeSxStr);
            labels[1].setText(timeSxStr);
            labels[2].setText(timeDxStr);
        }
    }

    private String calcolaTimeSx() {
        if (!app.isScontroMode || app.faseSalvata == Fase.RECUPERO_TIRO) {
            return FormattatoreTempo.formatta(app.timeRemainingSx, app.statoFormatoTempo);
        }
        if (app.faseSalvata == Fase.PREPARAZIONE_ROSSO) {
            return FormattatoreTempo.formatta(app.timeRemainingSx, app.statoFormatoTempo);
        }
        if (app.faseSalvata == Fase.SCONTRO_TIRO_SX) {
            return FormattatoreTempo.formatta(app.timeRemainingSx, app.statoFormatoTempo);
        }
        if (app.faseSalvata == Fase.SCONTRO_TIRO_DX) {
            boolean isIndiv = "INDIVIDUALE".equals(String.valueOf(app.comboScontroType.getSelectedItem()));
            return isIndiv
                    ? FormattatoreTempo.formatta(0, app.statoFormatoTempo)
                    : FormattatoreTempo.formatta(app.tempoSalvatoSx, app.statoFormatoTempo);
        }
        return FormattatoreTempo.formatta(0, app.statoFormatoTempo);
    }

    private String calcolaTimeDx(String timeSxStr) {
        if (!app.isScontroMode || app.faseSalvata == Fase.RECUPERO_TIRO) {
            return timeSxStr;
        }
        if (app.faseSalvata == Fase.PREPARAZIONE_ROSSO) {
            return timeSxStr;
        }
        if (app.faseSalvata == Fase.SCONTRO_TIRO_SX) {
            boolean isIndiv = "INDIVIDUALE".equals(String.valueOf(app.comboScontroType.getSelectedItem()));
            return isIndiv
                    ? FormattatoreTempo.formatta(0, app.statoFormatoTempo)
                    : FormattatoreTempo.formatta(app.tempoSalvatoDx, app.statoFormatoTempo);
        }
        if (app.faseSalvata == Fase.SCONTRO_TIRO_DX) {
            return FormattatoreTempo.formatta(app.timeRemainingDx, app.statoFormatoTempo);
        }
        return FormattatoreTempo.formatta(0, app.statoFormatoTempo);
    }
}
