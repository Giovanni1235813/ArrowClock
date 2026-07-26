import java.awt.CardLayout;

/**
 * Applica un preset di modalità di gara (Manuale, INDOOR, OUTDOOR, SCONTRO, SHOOT-OFF).
 * Resetta la gara, aggiorna i parametri degli spinner e mostra il pannello
 * di opzioni corretto.
 */
public class ComandoApplicaPreset implements Comando {

    private final ArcherySoftwareMain app;
    private final String preset;

    public ComandoApplicaPreset(ArcherySoftwareMain app, String preset) {
        this.app = app;
        this.preset = preset;
    }

    @Override
    public void esegui() {
        new ComandoReset(app).esegui();
        configuraPreset();
        impostaLimiteVolee();
        new ComandoApplicaLayoutMonitor(app).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();
        new ComandoBloccaInterfaccia(app, app.faseAttuale != Fase.ATTESA).esegui();
        // La finestra è null nei test automatici (nessuna GUI): guardia di sicurezza.
        if (app.operatorFrame != null) {
            app.operatorFrame.revalidate();
            app.operatorFrame.repaint();
        }
    }

    private void configuraPreset() {
        switch (preset) {
            case "Manuale" -> {
                mostraPanel("VUOTO");
                app.isScontroMode = false;
                app.comboTurni.setSelectedItem("AB - CD");
            }
            case "INDOOR" -> {
                mostraPanel("LINEARE");
                app.isScontroMode = false;
                app.comboTurni.setSelectedItem("AB - CD");
                app.spinT1.setValue(10);
                app.spinSecFrecciaLineare.setValue(40);
                app.spinT2.setValue(120);
                app.spinT3.setValue(30);
            }
            case "OUTDOOR" -> {
                mostraPanel("LINEARE");
                app.isScontroMode = false;
                app.comboTurni.setSelectedItem("AB - CD");
                app.spinT1.setValue(10);
                app.spinSecFrecciaLineare.setValue(40);
                app.spinT2.setValue(240);
                app.spinT3.setValue(30);
            }
            case "SCONTRO ALTERNATO", "SHOOT-OFF ALTERNATO" -> {
                mostraPanel("SCONTRO");
                app.isScontroMode = true;
                app.nomeSx = "A";
                app.nomeDx = "B";
                app.spinT1.setValue(10);
                app.comboScontroType.setSelectedItem("INDIVIDUALE");
                app.spinFrecce.setValue(1);
                app.spinFrecce.setEnabled(preset.equals("SCONTRO ALTERNATO")); // bloccato a 1 per SHOOT-OFF
                app.spinSecFreccia.setValue(20);
                app.comboTurni.setSelectedItem("A - B");
            }
            case "SCONTRO SIMULTANEO", "SHOOT-OFF SIMULTANEO" -> {
                mostraPanel("SCONTRO");
                app.isScontroMode = false;
                app.comboTurni.setSelectedItem("- Nessuno -"); // Turno unico senza nome
                app.spinT1.setValue(10);
                app.spinT3.setValue(30); // Il tempo giallo di 30 secondi è importante come richiesto
                app.comboScontroType.setSelectedItem("INDIVIDUALE");
                if (preset.equals("SHOOT-OFF SIMULTANEO")) {
                    app.spinFrecce.setValue(1);
                    app.spinFrecce.setEnabled(false);
                } else {
                    app.spinFrecce.setEnabled(true);
                    app.spinFrecce.setValue(3);
                }
                app.spinSecFreccia.setValue(20);
                // spinT2 verrà aggiornato automaticamente dal listener in CostruttoreOperatore
            }
        }
    }

    private void mostraPanel(String nomePanel) {
        ((CardLayout) app.scontroOptionsPanel.getParent().getLayout())
                .show(app.scontroOptionsPanel.getParent(), nomePanel);
    }

    private void impostaLimiteVolee() {
        int maxVolee = 999;
        if ("INDOOR".equals(preset))  maxVolee = 10;
        else if ("OUTDOOR".equals(preset)) maxVolee = 6;

        int currentV = (int) app.spinVolee.getValue();
        if (currentV > maxVolee) app.spinVolee.setValue(maxVolee);

        ((javax.swing.SpinnerNumberModel) app.spinVolee.getModel()).setMaximum(maxVolee);
    }
}
