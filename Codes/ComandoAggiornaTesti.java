import javax.swing.border.TitledBorder;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * Aggiorna istantaneamente tutti i testi visibili dell'interfaccia operatore
 * in base alla lingua corrente impostata in GestoreLingua.
 *
 * Struttura della responsabilità:
 *  1. Toolbar e pulsanti di stato (testo dipende dallo stato corrente dell'app)
 *  2. Etichette statiche dei pannelli (testo fisso, varia solo con la lingua)
 *  3. Titoli dei pannelli (TitledBorder)
 *  4. Pulsanti di controllo dinamici → delegati a ComandoAggiornaBottoni
 *  5. Testi di turno → delegati a ComandoAggiornaTestoTurno
 */
public class ComandoAggiornaTesti implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoAggiornaTesti(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        aggiornaToolbar();
        aggiornaEtichetteStatiche();
        aggiornaBordiTitolati();
        aggiornaComboBox();
        new ComandoAggiornaBottoni(app).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();
        app.operatorFrame.revalidate();
        app.operatorFrame.repaint();
    }

    // ── 1. Toolbar ────────────────────────────────────────────────────────────

    private void aggiornaToolbar() {
        app.btnLingua.setText(GestoreLingua.t("btn.lingua"));

        app.btnGaraInCorso.setText(GestoreLingua.t(
                app.isGaraInCorso ? "btn.gara.on" : "btn.gara.off"));

        app.btnSuono.setText(GestoreLingua.t(
                app.isSuonoAttivo ? "btn.suono.on" : "btn.suono.off"));

        app.btnTema.setText(GestoreLingua.t("btn.tema"));

        app.btnToggleTurniSpecial.setText(GestoreLingua.t(
                app.btnToggleTurniSpecial.isSelected() ? "btn.display.on" : "btn.display.off"));

        app.btnIdentificaMonitor.setText(GestoreLingua.t("btn.idmonitor"));

        if (app.lblSuMonitor != null) {
            app.lblSuMonitor.setText(GestoreLingua.t("lbl.sux"));
        }
        if (app.btnImpostaNomi != null) {
            app.btnImpostaNomi.setText(GestoreLingua.t("btn.nomi"));
        }
        if (app.btnAdd40s != null) {
            app.btnAdd40s.setText(GestoreLingua.t("btn.reimposta"));
        }
    }

    // ── 2. Etichette statiche ─────────────────────────────────────────────────

    private void aggiornaEtichetteStatiche() {
        aggiorna(app.lblT1desc,                 "lbl.t1");
        aggiorna(app.lblT2desc,                 "lbl.t2");
        aggiorna(app.lblT3desc,                 "lbl.t3");
        aggiorna(app.lblVoleeProvaDesc,          "lbl.voleeprova");
        aggiorna(app.lblVoleeCorrenteDesc,       "lbl.voleecorrente");
        aggiorna(app.lblParteDesc,              "lbl.parte");
        aggiorna(app.lblModalitaDesc,           "lbl.modalita");
        aggiorna(app.lblTipoDesc,               "lbl.tipo");
        aggiorna(app.lblFrecceDesc,             "lbl.frecce");
        aggiorna(app.lblSecFrecciaDesc,         "lbl.secfreccia");
        aggiorna(app.lblSecFrecciaLineareDesc,  "lbl.secfreccia");
    }

    // ── 3. TitledBorder ───────────────────────────────────────────────────────

    private void aggiornaBordiTitolati() {
        aggiorna(app.borderTempi,        "panel.tempi");
        aggiorna(app.borderDisplay,      "panel.display");
        aggiorna(app.borderPreset,       "panel.preset");
        aggiorna(app.borderControllo,    "panel.controllo");
        aggiorna(app.borderScontroPanel, "panel.scontro");
        aggiorna(app.borderLinearePanel, "panel.lineare");
    }

    // ── 4. ComboBox (il renderer legge la lingua al momento del disegno) ─────

    private void aggiornaComboBox() {
        repingi(app.comboPreset);
        repingi(app.comboScontroType);
        repingi(app.comboTurni);
        repingi(app.comboSelettoreDisplay);
    }

    private void repingi(JComboBox<?> combo) {
        if (combo != null) combo.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void aggiorna(JLabel label, String chiave) {
        if (label != null) label.setText(GestoreLingua.t(chiave));
    }

    private void aggiorna(TitledBorder border, String chiave) {
        if (border != null) border.setTitle(GestoreLingua.t(chiave));
    }
}
