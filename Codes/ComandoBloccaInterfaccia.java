/**
 * Abilita o disabilita i controlli della UI in base allo stato di gara.
 * Gestisce anche i casi speciali (modalità ID monitor, gara in corso, shoot-off).
 */
public class ComandoBloccaInterfaccia implements Comando {

    private final ArcherySoftwareMain app;
    private final boolean bloccata;

    public ComandoBloccaInterfaccia(ArcherySoftwareMain app, boolean bloccata) {
        this.app = app;
        this.bloccata = bloccata;
    }

    @Override
    public void esegui() {
        boolean attiva      = !bloccata;
        boolean bloccoModo  = bloccata || app.faseAttuale == Fase.RECUPERO_ATTESA || app.faseAttuale == Fase.RECUPERO_TIRO;
        boolean modoAttivo  = !bloccoModo;
        boolean isIdMode    = (app.faseAttuale == Fase.IDENTIFICAZIONE_MONITOR);
        boolean garaSuON    = app.isGaraInCorso;

        app.spinVoleeProva.setEnabled(modoAttivo && !isIdMode && !garaSuON);
        app.spinParte.setEnabled(modoAttivo && !isIdMode && !garaSuON);
        app.btnGaraInCorso.setEnabled(modoAttivo && !isIdMode);

        app.comboPreset.setEnabled(modoAttivo && !isIdMode && !garaSuON);
        app.comboScontroType.setEnabled(modoAttivo && !isIdMode && !garaSuON);

        boolean isShootOff = "SHOOT-OFF".equals(String.valueOf(app.comboPreset.getSelectedItem()));
        app.spinFrecce.setEnabled(modoAttivo && !isShootOff && !isIdMode);
        app.spinSecFreccia.setEnabled(modoAttivo && !isIdMode);

        boolean manualeAttivo = modoAttivo && "Manuale".equals(String.valueOf(app.comboPreset.getSelectedItem()));
        app.spinT1.setEnabled(manualeAttivo && !isIdMode);
        app.spinT2.setEnabled(manualeAttivo && !isIdMode);
        app.spinT3.setEnabled(manualeAttivo && !isIdMode);

        boolean isLineare = "INDOOR".equals(String.valueOf(app.comboPreset.getSelectedItem()))
                || "OUTDOOR".equals(String.valueOf(app.comboPreset.getSelectedItem()))
                || manualeAttivo;
        app.spinSecFrecciaLineare.setEnabled(modoAttivo && isLineare && !isIdMode);

        app.spinVolee.setEnabled(attiva && !isIdMode);

        if (app.isScontroMode) {
            app.btnAlternaMetata.setEnabled(modoAttivo && !isIdMode);
            app.comboTurni.setEnabled(false);
            if (app.btnImpostaNomi != null) {
                app.btnImpostaNomi.setVisible(true);
                app.btnImpostaNomi.setEnabled(modoAttivo && !isIdMode);
            }
        } else {
            app.comboTurni.setEnabled(modoAttivo && !isIdMode);
            String mod = String.valueOf(app.comboTurni.getSelectedItem());
            boolean isTurnoUnico = mod.equals("- Nessuno -") || mod.equals("ABC");
            app.btnAlternaMetata.setEnabled(modoAttivo && !isTurnoUnico && !isIdMode);
            if (app.btnImpostaNomi != null) {
                app.btnImpostaNomi.setVisible(false);
            }
        }

        app.btnTema.setEnabled(modoAttivo && !isIdMode);
        app.btnToggleTurniSpecial.setEnabled(modoAttivo && !isIdMode);
        app.comboSelettoreDisplay.setEnabled(modoAttivo && !app.btnToggleTurniSpecial.isSelected() && !isIdMode);
        app.btnIdentificaMonitor.setEnabled(modoAttivo || isIdMode);

        // Blocca il tasto lingua se la gara è in corso
        if (app.btnLingua != null) {
            app.btnLingua.setEnabled(!garaSuON && !isIdMode);
        }

        new ComandoAggiornaBottoni(app).esegui();
    }
}
