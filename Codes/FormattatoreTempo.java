import java.awt.Color;

/**
 * Classe astratta Strategy per la formattazione del tempo.
 * Ogni sottoclasse incapsula sia il formato testuale sia il colore
 * del testo (es. invisibile = stesso colore dello sfondo).
 *
 * Il metodo factory crea() restituisce la strategia giusta in base
 * allo stato corrente scelto dall'operatore.
 */
public abstract class FormattatoreTempo {

    public abstract String formatta(int secondi);

    public abstract Color coloreForeground(Color coloreBackground);

    public static FormattatoreTempo crea(int stato) {
        return switch (stato) {
            case 1  -> new FormattatoreMMSS();
            case 2  -> new FormattatoreInvisibile();
            default -> new FormattatoreSecondi();
        };
    }

    /** Shortcut statico per non dover costruire l'oggetto nei punti di chiamata singola. */
    public static String formatta(int secondi, int stato) {
        return crea(stato).formatta(secondi);
    }
}
