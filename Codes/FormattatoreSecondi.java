import java.awt.Color;

/** Mostra il tempo come numero intero di secondi (es. "120"). */
public class FormattatoreSecondi extends FormattatoreTempo {

    @Override
    public String formatta(int secondi) {
        return String.format("%d", secondi);
    }

    @Override
    public Color coloreForeground(Color coloreBackground) {
        return Color.BLACK;
    }
}
