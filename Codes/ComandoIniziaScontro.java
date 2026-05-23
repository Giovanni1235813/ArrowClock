import java.awt.Color;

/**
 * Inizializza tutti i parametri di uno scontro/shoot-off
 * (numero di scambi, tempi iniziali) e porta la fase a PREPARAZIONE_ROSSO.
 */
public class ComandoIniziaScontro implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoIniziaScontro(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        app.faseAttuale = Fase.PREPARAZIONE_ROSSO;
        new ComandoBloccaInterfaccia(app, true).esegui();
        app.timeRemainingSx = (int) app.spinT1.getValue();

        calcolaParametriScontro();

        app.scambiEffettuati = 0;
        new ComandoImpostaColoriScontro(app, Color.RED, Color.RED).esegui();
        new ComandoAggiornaDisplay(app).esegui();
    }

    private void calcolaParametriScontro() {
        String tipoScontro = String.valueOf(app.comboScontroType.getSelectedItem());
        int frecce = (int) app.spinFrecce.getValue();
        int secPerFreccia = (int) app.spinSecFreccia.getValue();
        boolean isShootOff = "SHOOT-OFF".equals(String.valueOf(app.comboPreset.getSelectedItem()));

        switch (tipoScontro) {
            case "INDIVIDUALE" -> {
                app.scambiTotaliScontro = frecce * 2;
                app.tempoSalvatoSx = secPerFreccia;
                app.tempoSalvatoDx = secPerFreccia;
            }
            case "SQUADRE" -> {
                app.scambiTotaliScontro = isShootOff ? 6 : (frecce * 2);
                app.tempoSalvatoSx = frecce * 3 * secPerFreccia;
                app.tempoSalvatoDx = app.tempoSalvatoSx;
            }
            case "MIX-TEAM" -> {
                app.scambiTotaliScontro = isShootOff ? 4 : (frecce * 2);
                app.tempoSalvatoSx = frecce * 2 * secPerFreccia;
                app.tempoSalvatoDx = app.tempoSalvatoSx;
            }
        }
    }
}
