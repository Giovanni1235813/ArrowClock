/**
 * Chiude la fase di recupero materiale: 3 fischi, reset pulito
 * e ritorno allo stato di attesa per la volée successiva.
 */
public class ComandoChiudiRecupero implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoChiudiRecupero(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        MotoreAudio.istanza().eseguiFischi(3, app.isSuonoAttivo);
        app.recuperoPrenotato = false;
        new ComandoFermaTutto(app).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();
        new ComandoAggiornaDisplay(app).esegui();
        app.gestoreLog.logRecupero("CONCLUSO");
    }
}
