import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

/**
 * Delegato UI Proprietario per le JComboBox.
 * Sostituisce totalmente il motore di rendering per ignorare i temi di sistema (Windows/Mac)
 * e forzare i colori del tema personalizzato dell'applicazione.
 */
public class ComboTemaUI extends BasicComboBoxUI {

    private final ArcherySoftwareMain app;

    public ComboTemaUI(ArcherySoftwareMain app) {
        this.app = app;
    }

    // Sovrascrive il disegno dello sfondo per ignorare i colori di default di sistema
    // e forzare l'utilizzo del colore di background definito dinamicamente.
    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        g.setColor(comboBox.getBackground());
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    protected JButton createArrowButton() {
        JButton btn = new JButton() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean enabled = getParent().isEnabled();

                // 1. Dipingiamo lo sfondo del quadratino della freccia
                Color bg = app.isDarkMode ? new Color(60, 60, 60) : new Color(230, 230, 230);
                if (!enabled) {
                    bg = app.isDarkMode ? new Color(40, 40, 40) : new Color(180, 180, 180);
                }
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 2. Disegniamo il triangolo (Freccia)
                Color arrowColor = app.isDarkMode ? Color.WHITE : Color.DARK_GRAY;
                if (!enabled) {
                    arrowColor = Color.GRAY;
                }
                g2.setColor(arrowColor);

                int w = 10;
                int h = 6;
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;

                int[] xs = {x, x + w, x + (w / 2)};
                int[] ys = {y, y, y + h};
                g2.fillPolygon(xs, ys, 3);

                g2.dispose();
            }
        };

        // Azzeriamo bordi e comportamenti nativi che potrebbero "tagliare" il disegno
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusable(false);
        btn.setContentAreaFilled(false);
        return btn;
    }
}