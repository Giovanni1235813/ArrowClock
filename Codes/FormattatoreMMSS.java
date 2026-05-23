import java.awt.Color;

/** Mostra il tempo in formato minuti:secondi (es. "02:00"). */
public class FormattatoreMMSS extends FormattatoreTempo {

    @Override
    public String formatta(int secondi) {
        return String.format("%02d:%02d", secondi / 60, secondi % 60);
    }

    @Override
    public Color coloreForeground(Color coloreBackground) {
        return Color.BLACK;
    }
}
