/**
 * Gestisce la fine di un singolo turno di tiro nello scontro.
 * Se tutti gli scambi sono completati chiude la volée (o innesca il recupero),
 * altrimenti passa al lato opposto.
 */
public class ComandoScambiaTurnoScontro implements Comando {

    private final ArcherySoftwareMain app;
    private final boolean timeout;

    public ComandoScambiaTurnoScontro(ArcherySoftwareMain app, boolean timeout) {
        this.app = app;
        this.timeout = timeout;
    }

    @Override
    public void esegui() {
        app.scambiEffettuati++;
        salvaTempoResiduo();

        if (app.scambiEffettuati >= app.scambiTotaliScontro) {
            chiudiVolee();
        } else {
            cambiaLato();
        }
    }

    private void salvaTempoResiduo() {
        if (!"INDIVIDUALE".equals(String.valueOf(app.comboScontroType.getSelectedItem()))) {
            if (app.faseAttuale == Fase.SCONTRO_TIRO_SX) app.tempoSalvatoSx = app.timeRemainingSx;
            if (app.faseAttuale == Fase.SCONTRO_TIRO_DX) app.tempoSalvatoDx = app.timeRemainingDx;
        }
    }

    private void chiudiVolee() {
        if (app.recuperoPrenotato) {
            MotoreAudio.istanza().eseguiFischi(5, app.isSuonoAttivo); // rimosso if (timeout)
            app.recuperoPrenotato = false;
            new ComandoAvanzaVolee(app).esegui();
            new ComandoInnescaRecupero(app).esegui();
        } else {
            MotoreAudio.istanza().eseguiFischi(3, app.isSuonoAttivo); // rimosso if (timeout)
            new ComandoConcludiCiclo(app).esegui();
        }
    }

    private void cambiaLato() {
        if (timeout) MotoreAudio.istanza().eseguiFischi(1, app.isSuonoAttivo);
        boolean passaASinistra = (app.faseAttuale == Fase.SCONTRO_TIRO_DX);
        new ComandoPassaAlTiroScontro(app, passaASinistra).esegui();
    }
}