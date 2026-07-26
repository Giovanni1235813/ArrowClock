/**
 * Gestisce la fine della fase di tiro nella modalità lineare.
 * Se ci sono ancora turni da completare avanza al turno successivo,
 * altrimenti chiude la volée (o innesca il recupero prenotato).
 */
public class ComandoGestisciFineFaseTiro implements Comando {

    private final ArcherySoftwareMain app;
    private final boolean timeout; // NEW FLAG

    public ComandoGestisciFineFaseTiro(ArcherySoftwareMain app, boolean timeout) {
        this.app = app;
        this.timeout = timeout;
    }

    @Override
    public void esegui() {
        if (app.turnoCorrente < app.totaleTurni) {
            avanzaTurno();
        } else {
            chiudiVolee();
        }
    }

    private void avanzaTurno() {
        app.turnoCorrente++;
        app.gestoreLog.logCambioTurnoLineare();
        new ComandoAggiornaTestoTurno(app).esegui();
        MotoreAudio.istanza().eseguiFischi(2, app.isSuonoAttivo); // rimosso il if(timeout)
        new ComandoIniziaPrepSingola(app).esegui();
    }

    private void chiudiVolee() {
        if (app.recuperoPrenotato) {
            MotoreAudio.istanza().eseguiFischi(5, app.isSuonoAttivo);
            app.recuperoPrenotato = false;
            app.prenotazione4Fischi = false; // Reset 4 fischi as well
            new ComandoAvanzaVolee(app).esegui();
            new ComandoInnescaRecupero(app).esegui();
        } else {
            int fischi = app.prenotazione4Fischi ? 4 : 3;
            app.prenotazione4Fischi = false;
            MotoreAudio.istanza().eseguiFischi(fischi, app.isSuonoAttivo);
            new ComandoConcludiCiclo(app).esegui();
        }
    }
}