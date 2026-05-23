import javax.swing.*;
import java.awt.*;

/**
 * Factory per creare JComboBox tematizzate.
 * Risolve definitivamente il conflitto con i LookAndFeel nativi
 * bloccando l'aggiornamento automatico della UI e delegando
 * il rendering alla classe ComboTemaUI.
 */
public class FabbricaCombo {

    public static JComboBox<String> crea(String[] items, ArcherySoftwareMain app) {
        JComboBox<String> combo = new JComboBox<String>(items) {

            @Override
            public void updateUI() {
                setUI(new ComboTemaUI(app));
            }

            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(b);
                Color bg = b
                        ? (app.isDarkMode ? new Color(50, 50, 50) : Color.WHITE)
                        : (app.isDarkMode ? new Color(40, 40, 40) : new Color(200, 200, 200));
                Color fg = b
                        ? (app.isDarkMode ? Color.WHITE : Color.BLACK)
                        : Color.GRAY;

                setBackground(bg);
                setForeground(fg);
            }
        };

        combo.updateUI();
        combo.setFocusable(false);
        combo.setRenderer(new RendererComboLocalizzato(app));
        return combo;
    }
}