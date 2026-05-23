import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Finestra modale per impostare i nomi dei due arcieri/squadre nello scontro.
 * Unica responsabilità: raccogliere i due nomi e salvarli nell'app.
 */
public class DialogNomiScontro {

    private final ArcherySoftwareMain app;

    public DialogNomiScontro(ArcherySoftwareMain app) {
        this.app = app;
    }

    public void apri() {
        JDialog dialog = new JDialog(app.operatorFrame, "Imposta Nomi", true);
        dialog.setUndecorated(true);
        dialog.setSize(650, 220);
        dialog.setLocationRelativeTo(app.operatorFrame);

        Color bg      = app.isDarkMode ? new Color(40, 40, 40)  : new Color(240, 240, 240);
        Color fg      = app.isDarkMode ? Color.WHITE             : Color.BLACK;
        Color fieldBg = app.isDarkMode ? new Color(60, 60, 60)  : Color.WHITE;

        JPanel panel = costruisciPanel(bg, fg);
        JTextField txtSx = costruisciCampoTesto(app.nomeSx, fieldBg, fg);
        JTextField txtDx = costruisciCampoTesto(app.nomeDx, fieldBg, fg);

        collegaNavigazioneFrecce(txtSx, txtDx);
        collegaSelezioneTotale(txtSx, txtDx);

        panel.add(costruisciTitolo(fg), BorderLayout.NORTH);
        panel.add(costruisciCentro(bg, fg, txtSx, txtDx), BorderLayout.CENTER);

        Runnable actionConfirm = () -> {
            app.nomeSx = txtSx.getText().trim().isEmpty() ? "A" : txtSx.getText().trim();
            app.nomeDx = txtDx.getText().trim().isEmpty() ? "B" : txtDx.getText().trim();
            new ComandoAggiornaTestoTurno(app).esegui();
            dialog.dispose();
        };

        panel.add(costruisciBottom(bg, actionConfirm, dialog), BorderLayout.SOUTH);
        collegaKeybindings(panel, actionConfirm, dialog);

        dialog.add(panel);
        SwingUtilities.invokeLater(txtSx::requestFocusInWindow);
        dialog.setVisible(true);
    }

    private JPanel costruisciPanel(Color bg, Color fg) {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(app.isDarkMode ? Color.GRAY : Color.DARK_GRAY, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        return panel;
    }

    private JLabel costruisciTitolo(Color fg) {
        JLabel lblTitle = new JLabel(GestoreLingua.t("dialog.nomi.header"), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(fg);
        return lblTitle;
    }

    private JTextField costruisciCampoTesto(String testo, Color bg, Color fg) {
        JTextField field = new JTextField(testo);
        field.setBackground(bg);
        field.setForeground(fg);
        field.setCaretColor(fg);
        field.setFont(new Font("Arial", Font.BOLD, 20));
        field.setHorizontalAlignment(SwingConstants.CENTER);
        return field;
    }

    private JPanel costruisciCentro(Color bg, Color fg, JTextField txtSx, JTextField txtDx) {
        JPanel center = new JPanel(new GridLayout(2, 2, 10, 15));
        center.setBackground(bg);
        JLabel lblSx = new JLabel(GestoreLingua.t("dialog.nomi.sx"));
        lblSx.setForeground(fg); lblSx.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel lblDx = new JLabel(GestoreLingua.t("dialog.nomi.dx"));
        lblDx.setForeground(fg); lblDx.setFont(new Font("Arial", Font.BOLD, 14));
        center.add(lblSx); center.add(txtSx);
        center.add(lblDx); center.add(txtDx);
        return center;
    }

    private JPanel costruisciBottom(Color bg, Runnable confirm, JDialog dialog) {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        bottom.setBackground(bg);
        JButton btnConfirm = new JButton(GestoreLingua.t("dialog.nomi.confirm"));
        btnConfirm.setFocusable(false);
        btnConfirm.setBackground(new Color(100, 200, 100));
        btnConfirm.setForeground(Color.BLACK);
        btnConfirm.addActionListener(e -> confirm.run());
        JButton btnCancel = new JButton(GestoreLingua.t("dialog.nomi.cancel"));
        btnCancel.setFocusable(false);
        btnCancel.setBackground(new Color(200, 100, 100));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> dialog.dispose());
        bottom.add(btnConfirm);
        bottom.add(btnCancel);
        return bottom;
    }

    private void collegaNavigazioneFrecce(JTextField txtSx, JTextField txtDx) {
        KeyAdapter nav = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
                    if (e.getSource() == txtSx) txtDx.requestFocusInWindow();
                    else txtSx.requestFocusInWindow();
                }
            }
        };
        txtSx.addKeyListener(nav);
        txtDx.addKeyListener(nav);
    }

    private void collegaSelezioneTotale(JTextField txtSx, JTextField txtDx) {
        FocusAdapter selectAll = new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(((JTextField) e.getSource())::selectAll);
            }
        };
        txtSx.addFocusListener(selectAll);
        txtDx.addFocusListener(selectAll);
    }

    private void collegaKeybindings(JPanel panel, Runnable confirm, JDialog dialog) {
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();
        im.put(KeyStroke.getKeyStroke("ENTER"),  "confirm");
        am.put("confirm", new AbstractAction() { public void actionPerformed(ActionEvent e) { confirm.run(); } });
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        am.put("cancel",  new AbstractAction() { public void actionPerformed(ActionEvent e) { dialog.dispose(); } });
    }
}
