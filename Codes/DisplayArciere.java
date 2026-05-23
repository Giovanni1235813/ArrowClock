import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Rappresenta un singolo monitor di gara (schermo esterno o finestra ausiliaria).
 * Costruisce il proprio layout con i pannelli per le modalità: singolo, scontro,
 * turni speciali e identificazione monitor.
 */
public class DisplayArciere {

    final JFrame frame;
    final JPanel displayContainer;
    final CardLayout displayCardLayout;

    final JPanel lightPanelSingolo;
    final JLabel timerLabelSingolo;
    final JLabel turniLabelSingolo;

    final JPanel lightPanelSx, lightPanelDx;
    final JLabel timerLabelSx, timerLabelDx;
    final JLabel turniLabelSx, turniLabelDx;

    final JPanel turniSpecialPanel;
    final JLabel turniSpecialLabel;

    final JPanel idPanel;
    final JLabel idLabel;

    // Nuovi campi interni alla classe per congelare i font calcolati dal motore
    private Font fontNumeri;
    private Font fontStop;
    private Font fontTesti;

    public DisplayArciere() {
        frame = new JFrame("ArrowClock Display");
        displayCardLayout = new CardLayout();
        displayContainer = new JPanel(displayCardLayout);

        lightPanelSingolo = new JPanel(new BorderLayout());
        lightPanelSingolo.setBackground(Color.RED);
        timerLabelSingolo = new JLabel("00:00", SwingConstants.CENTER);
        timerLabelSingolo.setForeground(Color.BLACK);
        turniLabelSingolo = new JLabel("ABCD", SwingConstants.CENTER);
        turniLabelSingolo.setForeground(Color.BLACK);
        turniLabelSingolo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        lightPanelSingolo.add(turniLabelSingolo, BorderLayout.SOUTH);
        lightPanelSingolo.add(timerLabelSingolo, BorderLayout.CENTER);

        JPanel scontroContainer = new JPanel(new GridLayout(1, 2));

        lightPanelSx = new JPanel(new BorderLayout());
        lightPanelSx.setBackground(Color.RED);
        lightPanelSx.setBorder(BorderFactory.createEmptyBorder()); // Sostituisce il MatteBorder
        timerLabelSx = new JLabel("00:00", SwingConstants.CENTER);
        timerLabelSx.setForeground(Color.BLACK);
        turniLabelSx = new JLabel("A", SwingConstants.CENTER);
        turniLabelSx.setForeground(Color.BLACK);
        turniLabelSx.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        lightPanelSx.add(turniLabelSx, BorderLayout.SOUTH);
        lightPanelSx.add(timerLabelSx, BorderLayout.CENTER);

        lightPanelDx = new JPanel(new BorderLayout());
        lightPanelDx.setBackground(Color.RED);
        timerLabelDx = new JLabel("00:00", SwingConstants.CENTER);
        timerLabelDx.setForeground(Color.BLACK);
        turniLabelDx = new JLabel("B", SwingConstants.CENTER);
        turniLabelDx.setForeground(Color.BLACK);
        turniLabelDx.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        lightPanelDx.add(turniLabelDx, BorderLayout.SOUTH);
        lightPanelDx.add(timerLabelDx, BorderLayout.CENTER);

        scontroContainer.add(lightPanelSx);
        scontroContainer.add(lightPanelDx);

        turniSpecialPanel = new JPanel(new BorderLayout());
        turniSpecialPanel.setBackground(Color.DARK_GRAY);
        turniSpecialLabel = new JLabel("ABCD", SwingConstants.CENTER);
        turniSpecialLabel.setForeground(Color.WHITE);
        turniSpecialPanel.add(turniSpecialLabel, BorderLayout.CENTER);

        idPanel = new JPanel(new BorderLayout());
        idPanel.setBackground(Color.BLUE);
        idLabel = new JLabel("", SwingConstants.CENTER);
        idLabel.setForeground(Color.WHITE);
        idPanel.add(idLabel, BorderLayout.CENTER);

        displayContainer.add(lightPanelSingolo, "SINGOLO");
        displayContainer.add(scontroContainer, "SCONTRO");
        displayContainer.add(turniSpecialPanel, "TURNI");
        displayContainer.add(idPanel, "IDENTIFICAZIONE");

        frame.add(displayContainer);

        // NEW: Responsive Integration Listener
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                aggiornaFontDinamico();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                aggiornaFontDinamico();
            }
        });
    }

    // REFACTORED: Delegate all complex math to the shared engine
    public void aggiornaFontDinamico() {
        int boundHeight = frame.getHeight();
        int boundWidth = frame.getWidth();

        int[] dimensioni = MotoreFontDinamico.calcolaDimensioni(
                boundWidth, boundHeight, frame.getGraphicsConfiguration());

        impostaFontGrandi(dimensioni[0], dimensioni[1], dimensioni[2]);
    }

    public void impostaFontGrandi(int sizeNumeri, int sizeStop, int sizeTesti) {
        this.fontNumeri = new Font("Arial", Font.BOLD, sizeNumeri);
        this.fontStop = new Font("Arial", Font.BOLD, sizeStop);
        this.fontTesti = new Font("Arial", Font.BOLD, sizeTesti);

        // Di base applichiamo la configurazione numerica normale
        applicaFontNumeri();

        // Mantiene il ridimensionamento delle label NORTH di emergenza
        if (lightPanelSingolo.getLayout() instanceof BorderLayout layout) {
            Component emSingolo = layout.getLayoutComponent(BorderLayout.NORTH);
            if (emSingolo != null) emSingolo.setFont(fontTesti);
        }
        if (lightPanelSx.getLayout() instanceof BorderLayout layout) {
            Component emSx = layout.getLayoutComponent(BorderLayout.NORTH);
            if (emSx != null) emSx.setFont(fontTesti);
        }
        if (lightPanelDx.getLayout() instanceof BorderLayout layout) {
            Component emDx = layout.getLayoutComponent(BorderLayout.NORTH);
            if (emDx != null) emDx.setFont(fontTesti);
        }
    }

    public void applicaFontNumeri() {
        if (fontNumeri == null) return;
        timerLabelSingolo.setFont(fontNumeri);
        turniLabelSingolo.setFont(fontTesti);
        timerLabelSx.setFont(new Font("Arial", Font.BOLD, (int)(fontNumeri.getSize() * 0.8)));
        turniLabelSx.setFont(fontTesti);
        timerLabelDx.setFont(new Font("Arial", Font.BOLD, (int)(fontNumeri.getSize() * 0.8)));
        turniLabelDx.setFont(fontTesti);
        turniSpecialLabel.setFont(fontNumeri);
        idLabel.setFont(fontNumeri);
    }

    public void applicaFontStop() {
        if (fontStop == null) return;
        timerLabelSingolo.setFont(fontStop);
        turniLabelSingolo.setFont(fontTesti);
        timerLabelSx.setFont(new Font("Arial", Font.BOLD, (int)(fontStop.getSize() * 0.8)));
        turniLabelSx.setFont(fontTesti);
        timerLabelDx.setFont(new Font("Arial", Font.BOLD, (int)(fontStop.getSize() * 0.8)));
        turniLabelDx.setFont(fontTesti);
        turniSpecialLabel.setFont(fontStop);
    }

    public void impostaTestoId(int numeroMonitor) {
        idLabel.setText("MONITOR " + numeroMonitor);
    }
}
