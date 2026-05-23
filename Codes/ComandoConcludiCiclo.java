/**
 * Termina un ciclo di volée completato: ferma tutto, avanza il contatore
 * di volée e aggiorna display e testo turno.
 */
public class ComandoConcludiCiclo implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoConcludiCiclo(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        new ComandoFermaTutto(app).esegui();
        new ComandoAvanzaVolee(app).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();
        new ComandoAggiornaDisplay(app).esegui();
    }
}
