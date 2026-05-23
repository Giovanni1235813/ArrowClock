import java.awt.Color;

/**
 * Imposta i colori dei due pannelli semaforo in modalità scontro
 * (sinistra e destra possono avere colori diversi).
 */
public class ComandoImpostaColoriScontro implements Comando {

    private final ArcherySoftwareMain app;
    private final Color bgSx;
    private final Color bgDx;

    public ComandoImpostaColoriScontro(ArcherySoftwareMain app, Color bgSx, Color bgDx) {
        this.app = app;
        this.bgSx = bgSx;
        this.bgDx = bgDx;
    }

    @Override
    public void esegui() {
        app.minLightSx.setBackground(bgSx);
        app.minLightDx.setBackground(bgDx);

        // FIX: Forza rigorosamente il testo a NERO in emergenza per lo Scontro
        Color fgSx = (app.faseAttuale == Fase.EMERGENZA) ? Color.BLACK :
                FormattatoreTempo.crea(app.statoFormatoTempo).coloreForeground(bgSx);
        Color fgDx = (app.faseAttuale == Fase.EMERGENZA) ? Color.BLACK :
                FormattatoreTempo.crea(app.statoFormatoTempo).coloreForeground(bgDx);

        app.minTimerSx.setForeground(fgSx);
        app.minTurniSx.setForeground(fgSx);
        app.minTimerDx.setForeground(fgDx);
        app.minTurniDx.setForeground(fgDx);

        for (DisplayArciere da : app.archerDisplays) {
            da.lightPanelSx.setBackground(bgSx);
            da.timerLabelSx.setForeground(fgSx);
            da.turniLabelSx.setForeground(fgSx);

            da.lightPanelDx.setBackground(bgDx);
            da.timerLabelDx.setForeground(fgDx);
            da.turniLabelDx.setForeground(fgDx);
        }
    }
}
