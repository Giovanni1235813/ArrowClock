/**
 * Interfaccia Command (Robert C. Martin, Clean Code).
 * Ogni operazione del programma è una classe che implementa questa interfaccia
 * con un unico metodo pubblico: esegui().
 */
public interface Comando {
    void esegui();
}
