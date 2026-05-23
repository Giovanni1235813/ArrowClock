import java.awt.Color;

/**
 * Passa dalla preparazione rossa alla fase di tiro (verde/giallo)
 * nella modalità singola/lineare, caricando T2 e impostando il colore.
 */
public class ComandoPassaAlTiroSingolo implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoPassaAlTiroSingolo(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        app.motoreTimer.sincronizzaEResettaAccumulatoreSingolo(); // FIXED: Clear fractional time
        app.faseAttuale = Fase.TIRO_VERDE_GIALLO;
        app.timeRemainingSx = (int) app.spinT2.getValue();
        int t3 = (int) app.spinT3.getValue();

        Color colore = (app.timeRemainingSx <= t3 && app.timeRemainingSx > 0) ? Color.YELLOW : Color.GREEN;
        new ComandoImpostaColoriSingoli(app, colore).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();
        new ComandoAggiornaDisplay(app).esegui();
    }
}
