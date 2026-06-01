// ComandoInno.java
public class ComandoInno implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoInno(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.faseAttuale != Fase.INNO_NAZIONALE) {
            // Entrata in modalità INNO
            app.faseAttuale = Fase.INNO_NAZIONALE;
            new ComandoApplicaLayoutMonitor(app).esegui();
            new ComandoBloccaInterfaccia(app, true).esegui();
        } else {
            // Uscita dalla modalità INNO
            app.faseAttuale = Fase.ATTESA;
            RiproduttoreInno.fermaInno(); // Spegne brutalmente l'audio se stava andando
            app.btnInno.setSelected(false);
            new ComandoApplicaLayoutMonitor(app).esegui();
            new ComandoBloccaInterfaccia(app, false).esegui();
        }
    }
}