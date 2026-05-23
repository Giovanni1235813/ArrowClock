import javax.swing.*;
import java.awt.*;

/**
 * Renderer per JComboBox che mostra il testo tradotto nella lingua corrente
 * senza alterare i valori interni del modello.
 *
 * Riceve app per poter applicare i colori corretti del tema scuro/chiaro,
 * sovrascrivendo i colori di default del delegate (che usa colori di sistema).
 */
public class RendererComboLocalizzato implements ListCellRenderer<Object> {

    private final DefaultListCellRenderer delegate = new DefaultListCellRenderer();
    private final ArcherySoftwareMain app;

    public RendererComboLocalizzato(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        String testo = traduci(value);
        JLabel label = (JLabel) delegate.getListCellRendererComponent(
                list, testo, index, isSelected, cellHasFocus);

        if (isSelected) {
            label.setBackground(app.isDarkMode ? new Color(70, 110, 170) : new Color(184, 207, 229));
            label.setForeground(Color.WHITE);
        } else {
            // Risolve il bug dell'illeggibilità forzando il contrasto in entrambe le modalità
            label.setBackground(app.isDarkMode ? new Color(45, 45, 45) : Color.WHITE);
            label.setForeground(app.isDarkMode ? Color.WHITE : Color.BLACK);
        }
        label.setOpaque(true);
        return label;
    }

    private String traduci(Object value) {
        if (value == null) return "";
        String stringValue = value.toString();
        String chiave = "combo." + stringValue;

        // Fallback: se non è nel dizionario, restituisce la stringa grezza ("Monitor 1")
        return GestoreLingua.ha(chiave) ? GestoreLingua.t(chiave) : stringValue;
    }
}
