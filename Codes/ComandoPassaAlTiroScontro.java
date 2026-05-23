import java.awt.Color;

/**
 * Commuta il turno di tiro dello scontro da sinistra a destra o viceversa,
 * aggiornando la fase, il tempo rimasto e i colori del semaforo.
 */
public class ComandoPassaAlTiroScontro implements Comando {

    private final ArcherySoftwareMain app;
    private final boolean iniziaSinistra;

    public ComandoPassaAlTiroScontro(ArcherySoftwareMain app, boolean iniziaSinistra) {
        this.app = app;
        this.iniziaSinistra = iniziaSinistra;
    }

    @Override
    public void esegui() {
        if (iniziaSinistra) {
            app.motoreTimer.sincronizzaEResettaAccumulatoreSx(); // FIXED: Clear fractional time
            app.gestoreLog.logTurnoScontro(app.nomeSx);
            app.faseAttuale = Fase.SCONTRO_TIRO_SX;
            app.timeRemainingSx = app.tempoSalvatoSx;
            new ComandoImpostaColoriScontro(app, Color.GREEN, Color.RED).esegui();
        } else {
            app.motoreTimer.sincronizzaEResettaAccumulatoreDx(); // FIXED: Clear fractional time
            app.gestoreLog.logTurnoScontro(app.nomeDx);
            app.faseAttuale = Fase.SCONTRO_TIRO_DX;
            app.timeRemainingDx = app.tempoSalvatoDx;
            new ComandoImpostaColoriScontro(app, Color.RED, Color.GREEN).esegui();
        }
        new ComandoAggiornaDisplay(app).esegui();
    }
}
