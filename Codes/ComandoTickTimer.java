import java.awt.Color;

/**
 * Esegue la logica di un singolo secondo di conteggio alla rovescia.
 * Delega alla fase corretta (recupero, singolo o scontro) e
 * aggiorna il display al termine.
 */
public class ComandoTickTimer implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoTickTimer(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.faseAttuale == Fase.RECUPERO_TIRO) {
            tickRecupero();
        } else if (!app.isScontroMode) {
            tickSingolo();
        } else {
            tickScontro();
        }
        new ComandoAggiornaDisplay(app).esegui();
    }

    private void tickRecupero() {
        app.timeRemainingSx--;
        if (app.timeRemainingSx <= 30 && app.timeRemainingSx > 0) {
            new ComandoImpostaColoriSingoli(app, Color.YELLOW).esegui();
        } else if (app.timeRemainingSx <= 0) {
            new ComandoChiudiRecupero(app).esegui();
        }
    }

    private void tickSingolo() {
        app.timeRemainingSx--;
        if (app.faseAttuale == Fase.PREPARAZIONE_ROSSO && app.timeRemainingSx <= 0) {
            MotoreAudio.istanza().eseguiFischi(1, app.isSuonoAttivo);
            new ComandoPassaAlTiroSingolo(app).esegui();
        } else if (app.faseAttuale == Fase.TIRO_VERDE_GIALLO) {
            int t3 = (int) app.spinT3.getValue();
            if (app.timeRemainingSx <= t3 && app.timeRemainingSx > 0) {
                new ComandoImpostaColoriSingoli(app, Color.YELLOW).esegui();
            } else if (app.timeRemainingSx <= 0) {
                new ComandoGestisciFineFaseTiro(app, true).esegui(); // Pass true for timer timeout
            }
        }
    }

    private void tickScontro() {
        if (app.faseAttuale == Fase.PREPARAZIONE_ROSSO && --app.timeRemainingSx <= 0) {
            MotoreAudio.istanza().eseguiFischi(1, app.isSuonoAttivo);
            new ComandoPassaAlTiroScontro(app, app.iniziaConPrimaParte).esegui();
        } else if (app.faseAttuale == Fase.SCONTRO_TIRO_SX && --app.timeRemainingSx <= 0) {
            new ComandoScambiaTurnoScontro(app, true).esegui();
        } else if (app.faseAttuale == Fase.SCONTRO_TIRO_DX && --app.timeRemainingDx <= 0) {
            new ComandoScambiaTurnoScontro(app, true).esegui();
        }
    }
}
