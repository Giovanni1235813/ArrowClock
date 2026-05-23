/**
 * Alterna la lingua dell'interfaccia tra IT e EN e aggiorna
 * immediatamente tutti i testi visibili tramite ComandoAggiornaTesti.
 *
 * È il punto di ingresso unico per il cambio lingua:
 * nessun altro componente deve chiamare GestoreLingua.toggle() direttamente.
 */
public class ComandoCambiaLingua implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoCambiaLingua(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        GestoreLingua.toggle();
        new ComandoAggiornaTesti(app).esegui();
    }
}
