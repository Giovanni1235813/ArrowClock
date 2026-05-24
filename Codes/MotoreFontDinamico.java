// MotoreFontDinamico.java
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;

public class MotoreFontDinamico {

    // MotoreFontDinamico.java

    public static int[] calcolaDimensioni(int boundWidth, int boundHeight, GraphicsConfiguration gc) {
        if (boundWidth <= 0 || boundHeight <= 0) return new int[]{10, 10, 10};

        int dpi;
        if (gc != null) {
            dpi = (int) (96 * gc.getDefaultTransform().getScaleY());
        } else {
            dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        }

        double widthInches = (double) boundWidth / dpi;
        double heightInches = (double) boundHeight / dpi;
        double containerDiagonal = Math.sqrt((widthInches * widthInches) + (heightInches * heightInches));

        // 1. NUMERI DEL TIMER: Intoccati e massimizzati fino a 25cm per l'omologazione
        int targetSizePts = (int) Math.round((25.0 / 2.54) * dpi);
        double scaleFactor = (containerDiagonal < 27.0) ? (containerDiagonal / 27.0) : 1.0;
        int sizeNumeri = (int) Math.round(targetSizePts * scaleFactor);

        int maxHeightNumeri = (int) (boundHeight * 0.75);
        if (sizeNumeri > maxHeightNumeri) {
            sizeNumeri = maxHeightNumeri;
        }
        if (sizeNumeri < 10) sizeNumeri = 10;

        // 2. DIMENSIONE DELLO "STOP" DI EMERGENZA (Intoccato)
        int maxWidthStop = (int) (boundWidth / 5.2);
        int maxHeightStop = (int) (boundHeight * 0.55);
        int sizeStop = Math.min(sizeNumeri, Math.min(maxHeightStop, maxWidthStop));
        if (sizeStop < 10) sizeStop = 10;

        // 3. NUOVO CALCOLO PROTETTIVO PER TUTTE LE ALTRE SCRITTE (Turni, Nomi, Scritte Statiche)
        // Applichiamo un tetto massimo geometrico stringente per evitare tassativamente il troncamento orizzontale
        int maxWidthTesti = (int) (boundWidth / 8.5);   // Freno orizzontale per far entrare stringhe lunghe (es. "AB - CD")
        int maxHeightTesti = (int) (boundHeight * 0.12); // Freno verticale (max 12% dell'altezza totale dello schermo)

        // Scegliamo il valore più piccolo tra la proporzione standard e i freni di sicurezza della scatola
        int sizeTesti = Math.min((int) Math.round(sizeNumeri / 5.0), Math.min(maxHeightTesti, maxWidthTesti));
        if (sizeTesti < 10) sizeTesti = 10;

        return new int[]{sizeNumeri, sizeStop, sizeTesti};
    }

    public static int calcolaFontAdattivoPerTesto(String testo, int boundWidth, int boundHeight, int maxFontSize) {
        if (boundWidth <= 0 || boundHeight <= 0 || testo == null || testo.trim().isEmpty()) {
            return maxFontSize;
        }

        int size = maxFontSize;
        while (size > 10) {
            double stringWidth = 0;
            for (char c : testo.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    stringWidth += size * 0.75; // Lettere maiuscole spesse (A, B, C, D, M, S, T)
                } else if (Character.isLowerCase(c)) {
                    stringWidth += size * 0.55; // Lettere minuscole
                } else if (Character.isDigit(c)) {
                    stringWidth += size * 0.62; // Cifre numeriche
                } else if (c == ' ') {
                    stringWidth += size * 0.32; // Spazi vuoti
                } else {
                    stringWidth += size * 0.45; // Trattini o punteggiatura ("-")
                }
            }

            int estimatedHeight = (int) (size * 1.2);
            // Verifica se la stringa rientra perfettamente nella scatola del monitor attuale
            if (stringWidth <= (boundWidth - 50) && estimatedHeight <= (boundHeight - 20)) {
                return size;
            }
            size--;
        }
        return 10; // Pavimento di sopravvivenza
    }
}