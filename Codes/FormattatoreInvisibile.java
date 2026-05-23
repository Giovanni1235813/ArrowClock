import java.awt.Color;

/**
 * Il testo viene formattato come MM:SS ma reso invisibile:
 * il colore del testo coincide con il colore di sfondo del pannello.
 */
public class FormattatoreInvisibile extends FormattatoreTempo {

    @Override
    public String formatta(int secondi) {
        return String.format("%02d:%02d", secondi / 60, secondi % 60);
    }

    @Override
    public Color coloreForeground(Color coloreBackground) {
        return coloreBackground;
    }
}
