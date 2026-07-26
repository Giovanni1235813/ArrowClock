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
            annullaRecupero();
        } else if (app.faseAttuale == Fase.ATTESA) {
            attivaRecuperoImmediat();
        } else {
            togglePrenotazione();
        }
    }

    private void annullaRecupero() {
        app.recuperoPrenotato = false;
        new ComandoFermaTutto(app).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();
        new ComandoAggiornaDisplay(app).esegui();
        app.gestoreLog.logRecupero("ANNULLATO");
    }

    private void attivaRecuperoImmediat() {
        app.faseAttuale = Fase.RECUPERO_ATTESA;
        int frecce = (int) app.spinFrecceRecupero.getValue();
        int secPerFreccia = (int) app.spinSecFrecciaRecupero.getValue();
        app.timeRemainingSx = frecce * secPerFreccia;
        
        if (app.bottomLeftCardLayout != null && app.bottomLeftContainer != null) {
            app.bottomLeftCardLayout.show(app.bottomLeftContainer, "RECUPERO");
        }
        
        new ComandoBloccaInterfaccia(app, true).esegui();
        new ComandoImpostaColoriSingoli(app, Color.RED).esegui();
        new ComandoAggiornaDisplay(app).esegui();
        app.gestoreLog.logRecupero("ATTIVATO");
    }

    private void togglePrenotazione() {
        app.recuperoPrenotato = !app.recuperoPrenotato;
        new ComandoAggiornaBottoni(app).esegui();
        app.gestoreLog.logRecupero(app.recuperoPrenotato ? "PRENOTATO" : "ANNULLATO");
    }
}
