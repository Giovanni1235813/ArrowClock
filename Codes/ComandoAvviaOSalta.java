import java.awt.Color;

/**
 * Gestisce la pressione del pulsante START/SALTA (Spazio).
 * Il comportamento dipende dalla fase attuale:
 * - ATTESA → avvia la preparazione
 * - RECUPERO_ATTESA → avvia il cronometro di recupero
 * - RECUPERO_TIRO → chiude il recupero
 * - altra fase → salta la fase in corso
 */
public class ComandoAvviaOSalta implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoAvviaOSalta(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.faseAttuale == Fase.EMERGENZA || app.faseAttuale == Fase.IDENTIFICAZIONE_MONITOR) return;

        if (app.faseAttuale == Fase.RECUPERO_ATTESA) {
            avviaRecuperoTiro();
            return;
        }

        if (app.faseAttuale == Fase.RECUPERO_TIRO) {
            new ComandoChiudiRecupero(app).esegui();
            return;
        }

        if (app.faseAttuale == Fase.ATTESA) {
            avviaNuovaVolee();
        } else {
            saltaFaseCorrente();
        }
    }

    private void avviaRecuperoTiro() {
        MotoreAudio.istanza().azzeraCodaFischi();
        MotoreAudio.istanza().eseguiFischi(1, app.isSuonoAttivo);
        app.faseAttuale = Fase.RECUPERO_TIRO;
        new ComandoBloccaInterfaccia(app, true).esegui();
        new ComandoImpostaColoriSingoli(app, Color.GREEN).esegui();
        app.gestoreLog.logRecupero("INIZIATO");
        app.motoreTimer.avvia();
        new ComandoAggiornaDisplay(app).esegui();
    }

    private void avviaNuovaVolee() {
        MotoreAudio.istanza().azzeraCodaFischi();
        app.turnoCorrente = 1;
        MotoreAudio.istanza().eseguiFischi(2, app.isSuonoAttivo);

        if (!app.isScontroMode) {
            calcolaTotaleTurni();
            app.gestoreLog.logInizioVolee();
            app.gestoreLog.logCambioTurnoLineare();
            new ComandoIniziaPrepSingola(app).esegui();
        } else {
            app.gestoreLog.logInizioVolee();
            new ComandoIniziaScontro(app).esegui();
        }
        app.motoreTimer.avvia();
    }

    private void calcolaTotaleTurni() {
        String mod = String.valueOf(app.comboTurni.getSelectedItem());
        if (mod.equals("- Nessuno -") || mod.equals("ABC")) {
            app.totaleTurni = 1;
        } else {
            app.totaleTurni = mod.split(" - ").length;
        }
    }

    private void saltaFaseCorrente() {
        MotoreAudio.istanza().azzeraCodaFischi();

        if (app.faseAttuale == Fase.PREPARAZIONE_ROSSO) {
            //MotoreAudio.istanza().eseguiFischi(1, app.isSuonoAttivo); //capiamo se usarlo o meno
            app.timeRemainingSx = 0;
            // The hardcoded MotoreAudio whistle call here was removed
            if (app.isScontroMode) {
                new ComandoPassaAlTiroScontro(app, app.iniziaConPrimaParte).esegui();
            } else {
                new ComandoPassaAlTiroSingolo(app).esegui();
            }
        } else if (app.faseAttuale == Fase.TIRO_VERDE_GIALLO) {
            app.timeRemainingSx = 0;
            new ComandoGestisciFineFaseTiro(app, false).esegui(); // Pass false for manual skip
        } else if (app.faseAttuale == Fase.SCONTRO_TIRO_SX) {
            new ComandoScambiaTurnoScontro(app, false).esegui(); // Pass false for manual skip
        } else if (app.faseAttuale == Fase.SCONTRO_TIRO_DX) {
            new ComandoScambiaTurnoScontro(app, false).esegui(); // Pass false for manual skip
        }
    }
}
