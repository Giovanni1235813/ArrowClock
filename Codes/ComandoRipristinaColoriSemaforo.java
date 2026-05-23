import java.awt.Color;

/**
 * Ripristina i colori del semaforo in base alla fase attuale.
 * Usato principalmente quando si cambia tema o si torna da una fase
 * intermedia alla visualizzazione corrente.
 */
public class ComandoRipristinaColoriSemaforo implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoRipristinaColoriSemaforo(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        switch (app.faseAttuale) {
            case ATTESA, PREPARAZIONE_ROSSO, IDENTIFICAZIONE_MONITOR -> {
                new ComandoImpostaColoriSingoli(app, Color.RED).esegui();
                new ComandoImpostaColoriScontro(app, Color.RED, Color.RED).esegui();
            }
            case TIRO_VERDE_GIALLO -> {
                int t3 = (int) app.spinT3.getValue();
                Color colore = (app.timeRemainingSx <= t3 && app.timeRemainingSx > 0) ? Color.YELLOW : Color.GREEN;
                new ComandoImpostaColoriSingoli(app, colore).esegui();
            }
            case SCONTRO_TIRO_SX ->
                new ComandoImpostaColoriScontro(app, Color.GREEN, Color.RED).esegui();
            case SCONTRO_TIRO_DX ->
                new ComandoImpostaColoriScontro(app, Color.RED, Color.GREEN).esegui();
            case RECUPERO_ATTESA -> {
                new ComandoImpostaColoriSingoli(app, Color.RED).esegui();
                new ComandoImpostaColoriScontro(app, Color.RED, Color.RED).esegui();
            }
            case RECUPERO_TIRO -> {
                Color colore = (app.timeRemainingSx <= 30 && app.timeRemainingSx > 0) ? Color.YELLOW : Color.GREEN;
                new ComandoImpostaColoriSingoli(app, colore).esegui();
            }
            case EMERGENZA -> {
                Color colore = app.emergenzaGiallaAttiva ? Color.RED : Color.YELLOW;
                new ComandoImpostaColoriSingoli(app, colore).esegui();
                new ComandoImpostaColoriScontro(app, colore, colore).esegui();
            }
        }
    }
}
