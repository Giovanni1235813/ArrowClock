/**
 * Timer finto usato dai test automatici.
 * Neutralizza l'avvio del vero {@code javax.swing.Timer}: nei test i secondi
 * vengono fatti avanzare a mano ({@link BancoDiProva#tick(int)}) chiamando
 * direttamente {@code ComandoTickTimer}, così la macchina a stati è
 * completamente deterministica e non dipende dal tempo reale.
 */
public class MotoreTimerFinto extends MotoreTimer {

    public MotoreTimerFinto(ArcherySoftwareMain app) {
        super(app);
    }

    @Override public void avvia() { /* no-op: i tick sono guidati dal banco di prova */ }
    @Override public void sincronizzaEResettaAccumulatoreSx() { }
    @Override public void sincronizzaEResettaAccumulatoreDx() { }
    @Override public void sincronizzaEResettaAccumulatoreSingolo() { }
}