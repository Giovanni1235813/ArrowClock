// PannelloBandiera.java
import java.awt.*;
import java.io.File;
import javax.swing.JPanel;
import javax.imageio.ImageIO;

public class PannelloBandiera extends JPanel {
    private Image immagineBandiera;

    public PannelloBandiera() {
        setBackground(Color.BLACK);
        try {
            File imgFile = new File(RiproduttoreInno.FILE_BANDIERA);
            if (imgFile.exists()) {
                immagineBandiera = ImageIO.read(imgFile);
            }
        } catch (Exception e) {
            System.out.println("Nessuna bandiera trovata o errore di lettura.");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        if (immagineBandiera != null) {
            // Alta qualità per il ridimensionamento (Antialiasing)
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = immagineBandiera.getWidth(null);
            int imgH = immagineBandiera.getHeight(null);

            // Calcolo proporzionale (mantiene l'aspect ratio)
            double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
            int scaledW = (int) (imgW * scale);
            int scaledH = (int) (imgH * scale);

            // Centratura perfetta
            int x = (panelW - scaledW) / 2;
            int y = (panelH - scaledH) / 2;

            g2d.drawImage(immagineBandiera, x, y, scaledW, scaledH, null);
        } else {
            // Se non c'è l'immagine, pesca il testo tradotto
            String txt = GestoreLingua.t("display.bandiera");

            // Sfruttiamo il Motore Font Dinamico per calcolare la dimensione massima che entra nel pannello
            // Passiamo l'altezza del pannello come "dimensione massima teorica"
            int fontAdattivo = MotoreFontDinamico.calcolaFontAdattivoPerTesto(txt, getWidth(), getHeight(), getHeight());

            g2d.setFont(new Font("Arial", Font.BOLD, fontAdattivo));
            g2d.setColor(Color.WHITE);

            // Centratura perfetta
            FontMetrics fm = g2d.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(txt)) / 2;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(txt, tx, ty);
        }
        g2d.dispose();
    }
}