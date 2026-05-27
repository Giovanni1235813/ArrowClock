// ComandoAggiornaDisplay.java
import java.awt.Color;
import java.awt.Font;

public class ComandoAggiornaDisplay implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoAggiornaDisplay(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        new ComandoAggiornaBottoni(app).esegui();

        if (app.faseAttuale == Fase.IDENTIFICAZIONE_MONITOR) {
            app.btnStartSkip.setEnabled(false);
            return;
        }

        // 1. REGIA TYPOGRAPHY: Commuta istantaneamente i font in base allo stato
        applicaFontInBaseAllaFase();

        // 2. Aggiorna colori e testi
        aggiornaColoriTimerVisibili();
        aggiornaTestiTimer();

        // FIX LINUX: Forza lo svuotamento immediato del buffer grafico verso il monitor
        java.awt.Toolkit.getDefaultToolkit().sync();
    }

    private void applicaFontInBaseAllaFase() {
        boolean isEmergenza = (app.faseAttuale == Fase.EMERGENZA);

        // Commuta i display esterni
        for (DisplayArciere da : app.archerDisplays) {
            if (isEmergenza) {
                da.applicaFontStop();
            } else {
                da.applicaFontNumeri();
            }
        }

        // Commuta l'anteprima operatore
        if (app.fontMinNumeri != null && app.fontMinStop != null) {
            Font fTimer = isEmergenza ? app.fontMinStop : app.fontMinNumeri;
            Font fTimerSide = new Font("Arial", Font.BOLD, (int)(fTimer.getSize() * 0.8));

            app.minTimerSingolo.setFont(fTimer);
            app.minTurniSingolo.setFont(app.fontMinTesti);
            app.minTimerSx.setFont(fTimerSide);
            app.minTurniSx.setFont(app.fontMinTesti);
            app.minTimerDx.setFont(fTimerSide);
            app.minTurniDx.setFont(app.fontMinTesti);
        }
    }

    private void aggiornaColoriTimerVisibili() {
        if (app.faseAttuale == Fase.EMERGENZA) {
            Color alertColor = Color.BLACK;
            app.minTimerSingolo.setForeground(alertColor);
            app.minTurniSingolo.setForeground(alertColor);
            app.minTimerSx.setForeground(alertColor);
            app.minTurniSx.setForeground(alertColor);
            app.minTimerDx.setForeground(alertColor);
            app.minTurniDx.setForeground(alertColor);

            for (DisplayArciere da : app.archerDisplays) {
                da.timerLabelSingolo.setForeground(alertColor);
                da.turniLabelSingolo.setForeground(alertColor);
                da.timerLabelSx.setForeground(alertColor);
                da.turniLabelSx.setForeground(alertColor);
                da.timerLabelDx.setForeground(alertColor);
                da.turniLabelDx.setForeground(alertColor);

                // FIX MIRATO: Forza il BIANCO solo sulla scritta dei turni speciali (es. "STOP")
                da.turniSpecialLabel.setForeground(Color.WHITE);
            }
            return;
        }

        // --- FASI NORMALI ---
        Color colorSingolo = FormattatoreTempo.crea(app.statoFormatoTempo).coloreForeground(app.minLightSingolo.getBackground());
        app.minTimerSingolo.setForeground(colorSingolo);
        app.minTurniSingolo.setForeground(colorSingolo);
        for (DisplayArciere da : app.archerDisplays) {
            da.timerLabelSingolo.setForeground(colorSingolo);
            da.turniLabelSingolo.setForeground(colorSingolo);

            // FIX MIRATO: Ripristina e garantisce il BIANCO quando si torna alla visualizzazione turni normale
            da.turniSpecialLabel.setForeground(Color.WHITE);
        }

        Color colorSx = FormattatoreTempo.crea(app.statoFormatoTempo).coloreForeground(app.minLightSx.getBackground());
        app.minTimerSx.setForeground(colorSx);
        app.minTurniSx.setForeground(colorSx);
        for (DisplayArciere da : app.archerDisplays) {
            da.timerLabelSx.setForeground(colorSx);
            da.turniLabelSx.setForeground(colorSx);
        }

        Color colorDx = FormattatoreTempo.crea(app.statoFormatoTempo).coloreForeground(app.minLightDx.getBackground());
        app.minTimerDx.setForeground(colorDx);
        app.minTurniDx.setForeground(colorDx);
        for (DisplayArciere da : app.archerDisplays) {
            da.timerLabelDx.setForeground(colorDx);
            da.turniLabelDx.setForeground(colorDx);
        }
    }

    private void aggiornaTestiTimer() {
        if (app.faseAttuale == Fase.EMERGENZA) {
            String testoStop = GestoreLingua.t("display.stop");
            aggiornaTestoSicuro(app.minTimerSingolo, testoStop);
            aggiornaTestoSicuro(app.minTimerSx, testoStop);
            aggiornaTestoSicuro(app.minTimerDx, testoStop);
            for (DisplayArciere da : app.archerDisplays) {
                aggiornaTestoSicuro(da.timerLabelSingolo, testoStop);
                aggiornaTestoSicuro(da.timerLabelSx, testoStop);
                aggiornaTestoSicuro(da.timerLabelDx, testoStop);

                // Proteggiamo anche il ricalcolo del font adattivo
                if (!testoStop.equals(da.turniSpecialLabel.getText())) {
                    da.aggiornaTestoEFontTurniSpecial(testoStop);
                }
            }
            return;
        }

        // --- GESTIONE SCHERMO INTERO (SINGOLO, TIRO LINEARE, RECUPERO, ATTESA) ---
        if (!app.isScontroMode || app.faseAttuale == Fase.RECUPERO_ATTESA || app.faseAttuale == Fase.RECUPERO_TIRO || app.faseAttuale == Fase.ATTESA) {
            String tempoFormat = FormattatoreTempo.formatta(app.timeRemainingSx, app.statoFormatoTempo);

            if (app.faseAttuale == Fase.ATTESA) {
                tempoFormat = FormattatoreTempo.formatta(0, app.statoFormatoTempo);
            }

            aggiornaTestoSicuro(app.minTimerSingolo, tempoFormat);
            for (DisplayArciere da : app.archerDisplays) {
                aggiornaTestoSicuro(da.timerLabelSingolo, tempoFormat);
            }

            // GESTIONE RECUPERO
            if (app.faseAttuale == Fase.RECUPERO_ATTESA || app.faseAttuale == Fase.RECUPERO_TIRO) {
                String testoRecupero = GestoreLingua.t("display.recupero");
                aggiornaTestoSicuro(app.minTurniSingolo, testoRecupero);
                for (DisplayArciere da : app.archerDisplays) {
                    aggiornaTestoSicuro(da.turniLabelSingolo, testoRecupero);

                    if (!testoRecupero.equals(da.turniSpecialLabel.getText())) {
                        da.aggiornaTestoEFontTurniSpecial(testoRecupero);
                    }
                }
            }
        }

        // --- GESTIONE SCONTRO / CHESS-CLOCK ---
        if (app.isScontroMode && app.faseAttuale != Fase.RECUPERO_ATTESA && app.faseAttuale != Fase.RECUPERO_TIRO) {
            boolean isIndiv = "INDIVIDUALE".equals(String.valueOf(app.comboScontroType.getSelectedItem()));
            String sxFormat = "0";
            String dxFormat = "0";

            if (app.faseAttuale == Fase.ATTESA) {
                sxFormat = FormattatoreTempo.formatta(0, app.statoFormatoTempo);
                dxFormat = FormattatoreTempo.formatta(0, app.statoFormatoTempo);
            } else if (app.faseAttuale == Fase.PREPARAZIONE_ROSSO) {
                sxFormat = FormattatoreTempo.formatta(app.timeRemainingSx, app.statoFormatoTempo);
                dxFormat = FormattatoreTempo.formatta(app.timeRemainingSx, app.statoFormatoTempo);
            } else if (app.faseAttuale == Fase.SCONTRO_TIRO_SX) {
                sxFormat = FormattatoreTempo.formatta(app.timeRemainingSx, app.statoFormatoTempo);
                dxFormat = isIndiv ? FormattatoreTempo.formatta(0, app.statoFormatoTempo) : FormattatoreTempo.formatta(app.tempoSalvatoDx, app.statoFormatoTempo);
            } else if (app.faseAttuale == Fase.SCONTRO_TIRO_DX) {
                dxFormat = FormattatoreTempo.formatta(app.timeRemainingDx, app.statoFormatoTempo);
                sxFormat = isIndiv ? FormattatoreTempo.formatta(0, app.statoFormatoTempo) : FormattatoreTempo.formatta(app.tempoSalvatoSx, app.statoFormatoTempo);
            }

            aggiornaTestoSicuro(app.minTimerSx, sxFormat);
            aggiornaTestoSicuro(app.minTimerDx, dxFormat);
            for (DisplayArciere da : app.archerDisplays) {
                aggiornaTestoSicuro(da.timerLabelSx, sxFormat);
                aggiornaTestoSicuro(da.timerLabelDx, dxFormat);
            }
        }
    }
    private void aggiornaTestoSicuro(javax.swing.JLabel label, String nuovoTesto) {
        if (label != null && nuovoTesto != null && !nuovoTesto.equals(label.getText())) {
            label.setText(nuovoTesto);
        }
    }
}