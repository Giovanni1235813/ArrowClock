import java.awt.Color;

/**
 * Gestisce la prenotazione dei 4 fischi a fine volée.
 * Funziona in modo analogo alla prenotazione del recupero.
 */
public class Comando4Fischi implements Comando {

    private final ArcherySoftwareMain app;

    public Comando4Fischi(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.faseAttuale == Fase.EMERGENZA
                || app.faseAttuale == Fase.RECUPERO_TIRO
                || app.faseAttuale == Fase.IDENTIFICAZIONE_MONITOR
                || app.faseAttuale == Fase.INNO_NAZIONALE) {
            return;
        }

        app.prenotazione4Fischi = !app.prenotazione4Fischi;
        new ComandoAggiornaBottoni(app).esegui();
        app.gestoreLog.logPrenotazione4Fischi(app.prenotazione4Fischi);
    }
}
