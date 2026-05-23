/**
 * Ferma la gara e riporta tutto allo stato iniziale ATTESA.
 * Annulla anche recuperi in corso o prenotati, loggando l'evento.
 */
public class ComandoReset implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoReset(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.faseAttuale == Fase.RECUPERO_ATTESA
                || app.faseAttuale == Fase.RECUPERO_TIRO
                || app.recuperoPrenotato) {
            app.gestoreLog.logRecupero("ANNULLATO (Reset Gara)");
        }

        MotoreAudio.istanza().azzeraCodaFischi();
        new ComandoFermaTutto(app).esegui();
        app.recuperoPrenotato = false;
        app.btnIdentificaMonitor.setSelected(false);
        new ComandoAggiornaBottoni(app).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();
        new ComandoAggiornaDisplay(app).esegui();
    }
}
