import javax.swing.Timer;
import java.awt.Color;

/**
 * Motore del timer di conto alla rovescia e del lampeggio di emergenza.
 * Unica responsabilità: gestire il tick temporale e delegare
 * la logica di gioco al comando apposito.
 */
public class MotoreTimer {

    private final ArcherySoftwareMain app;

    public MotoreTimer(ArcherySoftwareMain app) {
        this.app = app;
    }

    public void configura() {
        app.countdownTimer = new Timer(100, e -> eseguiTick());

        app.flashTimer = new Timer(1000, e -> {
            app.blinkState = !app.blinkState;
            new ComandoAggiornaBottoni(app).esegui();

            if (app.emergenzaGiallaAttiva) {
                new ComandoImpostaColoriSingoli(app, Color.YELLOW).esegui();
                new ComandoImpostaColoriScontro(app, Color.YELLOW, Color.YELLOW).esegui();
            } else {
                new ComandoImpostaColoriSingoli(app, Color.RED).esegui();
                new ComandoImpostaColoriScontro(app, Color.RED, Color.RED).esegui();
            }
            app.emergenzaGiallaAttiva = !app.emergenzaGiallaAttiva;
        });
    }

    private long lastTickTimeNano = 0;
    private double accumulatoreSx = 0.0;
    private double accumulatoreDx = 0.0;

    public void avvia() {
        this.lastTickTimeNano = System.nanoTime();
        app.accumulatoreSecondi = 0.0; // Reset generic accumulator
        this.accumulatoreSx = 0.0;     // Reset isolated Left accumulator
        this.accumulatoreDx = 0.0;     // Reset isolated Right accumulator
        app.countdownTimer.start();
    }

    public void sincronizzaEResettaAccumulatoreSx() {
        this.lastTickTimeNano = System.nanoTime();
        this.accumulatoreSx = 0.0;
    }

    public void sincronizzaEResettaAccumulatoreDx() {
        this.lastTickTimeNano = System.nanoTime();
        this.accumulatoreDx = 0.0;
    }

    public void sincronizzaEResettaAccumulatoreSingolo() {
        this.lastTickTimeNano = System.nanoTime();
        app.accumulatoreSecondi = 0.0;
    }

    private void eseguiTick() {
        long now = System.nanoTime();
        // Calculate high-precision delta in seconds
        double deltaSec = (now - this.lastTickTimeNano) / 1_000_000_000.0;
        this.lastTickTimeNano = now;

        int secDaScalare = 0;

        // Route and isolate the delta-time accumulation to prevent fractional bleeding between teams
        if (app.faseAttuale == Fase.SCONTRO_TIRO_SX) {
            this.accumulatoreSx += deltaSec;
            secDaScalare = (int) this.accumulatoreSx;
            if (secDaScalare > 0) this.accumulatoreSx -= secDaScalare;
        } else if (app.faseAttuale == Fase.SCONTRO_TIRO_DX) {
            this.accumulatoreDx += deltaSec;
            secDaScalare = (int) this.accumulatoreDx;
            if (secDaScalare > 0) this.accumulatoreDx -= secDaScalare;
        } else {
            // Generic timer behavior (Preparazione, Singolo, Recupero)
            app.accumulatoreSecondi += deltaSec;
            secDaScalare = (int) app.accumulatoreSecondi;
            if (secDaScalare > 0) app.accumulatoreSecondi -= secDaScalare;
        }

        for (int i = 0; i < secDaScalare; i++) {
            new ComandoTickTimer(app).esegui();
        }
    }
}
