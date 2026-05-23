import javax.swing.JLabel;

/**
 * Azzera il testo di tutte le etichette di emergenza
 * quando l'emergenza viene risolta o la gara viene resettata.
 */
public class ComandoPulisciLabelEmergenza implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoPulisciLabelEmergenza(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.minEmergenzaTimeSingolo != null) {
            app.minEmergenzaTimeSingolo.setText("");
            app.minEmergenzaTimeSx.setText("");
            app.minEmergenzaTimeDx.setText("");
        }
        for (JLabel[] labels : app.emergenzaLabelsList) {
            labels[0].setText("");
            labels[1].setText("");
            labels[2].setText("");
        }
    }
}
