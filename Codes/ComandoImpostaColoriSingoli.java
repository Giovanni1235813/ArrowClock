import java.awt.Color;

/**
 * Imposta il colore di sfondo del pannello semaforo in modalità singola
 * e aggiorna il colore del testo timer in base al formato scelto.
 */
public class ComandoImpostaColoriSingoli implements Comando {

    private final ArcherySoftwareMain app;
    private final Color bg;

    public ComandoImpostaColoriSingoli(ArcherySoftwareMain app, Color bg) {
        this.app = app;
        this.bg = bg;
    }

    @Override
    public void esegui() {
        app.minLightSingolo.setBackground(bg);

        // FIX: Forza rigorosamente il testo a NERO se siamo in emergenza,
        // altrimenti il formato "Invisibile" renderà la parola STOP rossa su rosso!
        Color fg = (app.faseAttuale == Fase.EMERGENZA) ? Color.BLACK :
                FormattatoreTempo.crea(app.statoFormatoTempo).coloreForeground(bg);

        app.minTimerSingolo.setForeground(fg);
        app.minTurniSingolo.setForeground(fg);

        for (DisplayArciere da : app.archerDisplays) {
            da.lightPanelSingolo.setBackground(bg);
            da.timerLabelSingolo.setForeground(fg);
            da.turniLabelSingolo.setForeground(fg);
        }
    }
}
