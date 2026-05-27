import java.awt.Color;

/**
 * Gestisce la pressione del tasto RECUPERO (R).
 * Il comportamento dipende dalla fase corrente:
 * - ATTESA → attiva il recupero immediato (40s)
 * - RECUPERO_ATTESA → aggiunge 40s al tempo rimasto
 * - altra fase → prenota/annulla il recupero per fine volée
 */
public class ComandoRecupero implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoRecupero(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        // FIX: Blocco totale e immediato del recupero durante gli scontri (schermo diviso)
        if (app.isScontroMode) {
            return;
        }
        if (app.faseAttuale == Fase.EMERGENZA
                || app.faseAttuale == Fase.RECUPERO_TIRO
                || app.faseAttuale == Fase.IDENTIFICAZIONE_MONITOR) return;

        if (app.faseAttuale == Fase.RECUPERO_ATTESA) {
            aggiungi40s();
        } else if (app.faseAttuale == Fase.ATTESA) {
            attivaRecuperoImmediat();
        } else {
            togglePrenotazione();
        }
    }

    private void aggiungi40s() {
        app.timeRemainingSx += 40;
        new ComandoAggiornaDisplay(app).esegui();
        app.gestoreLog.logNotificaParte(
                "AGGIUNTI 40s AL RECUPERO (Tempo Totale: " + app.timeRemainingSx + "s)");
    }

    private void attivaRecuperoImmediat() {
        app.faseAttuale = Fase.RECUPERO_ATTESA;
        app.timeRemainingSx = 40;
        new ComandoBloccaInterfaccia(app, true).esegui();
        new ComandoImpostaColoriSingoli(app, Color.RED).esegui();
        new ComandoAggiornaDisplay(app).esegui();
        app.gestoreLog.logRecupero("ATTIVATO (40s)");
    }

    private void togglePrenotazione() {
        app.recuperoPrenotato = !app.recuperoPrenotato;
        new ComandoAggiornaBottoni(app).esegui();
        app.gestoreLog.logRecupero(app.recuperoPrenotato ? "PRENOTATO" : "ANNULLATO");
    }
}
