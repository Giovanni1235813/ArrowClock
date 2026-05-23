// MotoreFontDinamico.java
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;

public class MotoreFontDinamico {

    public static int[] calcolaDimensioni(int boundWidth, int boundHeight, GraphicsConfiguration gc) {
        if (boundWidth <= 0 || boundHeight <= 0) return new int[]{10, 10, 2};

        int dpi;
        if (gc != null) {
            dpi = (int) (96 * gc.getDefaultTransform().getScaleY());
        } else {
            dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        }

        double widthInches = (double) boundWidth / dpi;
        double heightInches = (double) boundHeight / dpi;
        double containerDiagonal = Math.sqrt((widthInches * widthInches) + (heightInches * heightInches));

        // 1. CALCOLO NUMERI (Grande, stabile e indipendente da STOP, punta a ~25cm)
        int targetSizePts = (int) Math.round((25.0 / 2.54) * dpi);
        double scaleFactor = (containerDiagonal < 27.0) ? (containerDiagonal / 27.0) : 1.0;
        int sizeNumeri = (int) Math.round(targetSizePts * scaleFactor);

        // Cap verticale massimo di sicurezza per i numeri (75% dell'altezza dello schermo)
        int maxHeightNumeri = (int) (boundHeight * 0.75);
        if (sizeNumeri > maxHeightNumeri) {
            sizeNumeri = maxHeightNumeri;
        }
        if (sizeNumeri < 10) sizeNumeri = 10;

        // 2. CALCOLO SEPARATO PER LO "STOP" (Freno sussidiario orizzontale per evitare "ST...")
        int maxWidthStop = (int) (boundWidth / 5.2);
        int maxHeightStop = (int) (boundHeight * 0.55);
        int sizeStop = Math.min(sizeNumeri, Math.min(maxHeightStop, maxWidthStop));
        if (sizeStop < 10) sizeStop = 10;

        // 3. CALCOLO ETICHETTE TESTUALI (1/5 dei numeri)
        int sizeTesti = (int) Math.round(sizeNumeri / 5.0);
        if (sizeTesti < 2) sizeTesti = 2;

        return new int[]{sizeNumeri, sizeStop, sizeTesti};
    }
}