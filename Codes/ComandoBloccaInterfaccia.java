/**
 * Abilita o disabilita i controlli della UI in base allo stato di gara.
 * Gestisce anche i casi speciali (modalità ID monitor, gara in corso, shoot-off, inno).
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
        boolean isInnoMode  = (app.faseAttuale == Fase.INNO_NAZIONALE);
        boolean garaSuON    = app.isGaraInCorso;

        app.spinVoleeProva.setEnabled(modoAttivo && !isIdMode && !garaSuON && !isInnoMode);
        app.spinParte.setEnabled(modoAttivo && !isIdMode && !garaSuON && !isInnoMode);
        app.btnGaraInCorso.setEnabled(modoAttivo && !isIdMode && !isInnoMode);

        app.comboPreset.setEnabled(modoAttivo && !isIdMode && !garaSuON && !isInnoMode);
        app.comboScontroType.setEnabled(modoAttivo && !isIdMode && !garaSuON && !isInnoMode);

        boolean isShootOff = "SHOOT-OFF".equals(String.valueOf(app.comboPreset.getSelectedItem()));
        app.spinFrecce.setEnabled(modoAttivo && !isShootOff && !isIdMode && !isInnoMode);
        app.spinSecFreccia.setEnabled(modoAttivo && !isIdMode && !isInnoMode);

        boolean manualeAttivo = modoAttivo && "Manuale".equals(String.valueOf(app.comboPreset.getSelectedItem()));
        app.spinT1.setEnabled(manualeAttivo && !isIdMode && !isInnoMode);
        app.spinT2.setEnabled(manualeAttivo && !isIdMode && !isInnoMode);
        app.spinT3.setEnabled(manualeAttivo && !isIdMode && !isInnoMode);

        boolean isLineare = "INDOOR".equals(String.valueOf(app.comboPreset.getSelectedItem()))
                || "OUTDOOR".equals(String.valueOf(app.comboPreset.getSelectedItem()))
                || manualeAttivo;
        app.spinSecFrecciaLineare.setEnabled(modoAttivo && isLineare && !isIdMode && !isInnoMode);

        app.spinVolee.setEnabled(attiva && !isIdMode && !isInnoMode);

        if (app.isScontroMode) {
            app.btnAlternaMetata.setEnabled(modoAttivo && !isIdMode && !isInnoMode);
            app.comboTurni.setEnabled(false);
            if (app.btnImpostaNomi != null) {
                app.btnImpostaNomi.setVisible(true);
                app.btnImpostaNomi.setEnabled(modoAttivo && !isIdMode && !isInnoMode);
            }
        } else {
            app.comboTurni.setEnabled(modoAttivo && !isIdMode && !isInnoMode);
            String mod = String.valueOf(app.comboTurni.getSelectedItem());
            boolean isTurnoUnico = mod.equals("- Nessuno -") || mod.equals("ABC");
            app.btnAlternaMetata.setEnabled(modoAttivo && !isTurnoUnico && !isIdMode && !isInnoMode);
            if (app.btnImpostaNomi != null) {
                app.btnImpostaNomi.setVisible(false);
            }
        }

        app.btnTema.setEnabled(modoAttivo && !isIdMode && !isInnoMode);
        app.btnToggleTurniSpecial.setEnabled(modoAttivo && !isIdMode && !isInnoMode);
        app.comboSelettoreDisplay.setEnabled(modoAttivo && !app.btnToggleTurniSpecial.isSelected() && !isIdMode && !isInnoMode);
        app.btnIdentificaMonitor.setEnabled((modoAttivo || isIdMode) && !isInnoMode && !garaSuON);

        if (app.btnLingua != null) {
            app.btnLingua.setEnabled((!garaSuON && !isIdMode) && !isInnoMode);
        }

        if (app.btnInno != null) {
            app.btnInno.setEnabled(modoAttivo || isInnoMode);
        }

        new ComandoAggiornaBottoni(app).esegui();
    }
}