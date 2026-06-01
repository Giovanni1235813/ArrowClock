/**
 * Sceglie quale "carta" del CardLayout mostrare su ciascun monitor
 * (SINGOLO, SCONTRO, TURNI o IDENTIFICAZIONE o BANDIERA) in base allo stato attuale.
 */
public class ComandoApplicaLayoutMonitor implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoApplicaLayoutMonitor(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.faseAttuale == Fase.IDENTIFICAZIONE_MONITOR) return;

        // NUOVO BLOCCO: Se siamo nella fase dell'Inno, mostra la bandiera ovunque e fermati.
        if (app.faseAttuale == Fase.INNO_NAZIONALE) {
            app.miniaturaCardLayout.show(app.miniaturaContainer, "BANDIERA");
            for (DisplayArciere da : app.archerDisplays) {
                da.displayCardLayout.show(da.displayContainer, "BANDIERA");
            }
            return;
        }

        boolean isTurniMode = app.btnToggleTurniSpecial.isSelected();
        int monitorScelto  = app.comboSelettoreDisplay.getSelectedIndex();

        for (int i = 0; i < app.archerDisplays.size(); i++) {
            DisplayArciere da = app.archerDisplays.get(i);
            if (isTurniMode && i == monitorScelto) {
                da.displayCardLayout.show(da.displayContainer, "TURNI");
            } else if (app.isScontroMode && !isRecupero()) {
                da.displayCardLayout.show(da.displayContainer, "SCONTRO");
            } else {
                da.displayCardLayout.show(da.displayContainer, "SINGOLO");
            }
        }

        if (app.isScontroMode && !isRecupero()) {
            app.miniaturaCardLayout.show(app.miniaturaContainer, "SCONTRO");
        } else {
            app.miniaturaCardLayout.show(app.miniaturaContainer, "SINGOLO");
        }

        new ComandoAggiornaDisplay(app).esegui();
    }

    private boolean isRecupero() {
        return app.faseAttuale == Fase.RECUPERO_ATTESA || app.faseAttuale == Fase.RECUPERO_TIRO;
    }
}