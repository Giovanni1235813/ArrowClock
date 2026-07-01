/**
 * Esecutore dei test automatici di ArrowClock.
 *
 * <p>Runner autonomo, senza dipendenze esterne (coerente con la portabilità del
 * progetto): compila con lo stesso {@code javac} dei sorgenti e si lancia con
 * {@code java EsecutoreTest}. Esce con codice 0 se tutti i test passano,
 * altrimenti con codice 1 (utile per script di build / CI).</p>
 */
public class EsecutoreTest {

    private static int passati = 0;
    private static int falliti = 0;

    public static void main(String[] args) {
        // Nessuna finestra: garantiamo l'esecuzione anche senza schermo.
        System.setProperty("java.awt.headless", "true");

        System.out.println("=== ArrowClock — Test automatici ===\n");

        System.out.println("Modalità lineare (qualifica):");
        esegui("Ciclo INDOOR completo a due turni", TestCicloLineare::indoorCicloCompletoDueTurni);
        esegui("Salto manuale silenzioso",          TestCicloLineare::saltoManualeSilenzioso);
        esegui("Avviso giallo senza fischi",        TestCicloLineare::avvisoGialloSenzaFischi);

        System.out.println("\nModalità scontro (chess-clock):");
        esegui("Individuale a due scambi",          TestScontro::individualeDueScambi);
        esegui("Recupero ignorato in scontro",      TestScontro::recuperoIgnoratoInScontro);

        System.out.println("\nRecupero materiale ed emergenza:");
        esegui("Recupero prenotato (5 fischi)",     TestRecuperoEmergenza::recuperoPrenotato);
        esegui("Recupero immediato +40s",           TestRecuperoEmergenza::recuperoImmediatoConAggiunta);
        esegui("Emergenza congela e ripristina",    TestRecuperoEmergenza::emergenzaCongelaERipristina);

        System.out.println("\nRiepilogo log di gara:");
        esegui("Riepilogo conta solo gara reale",   TestLog::riepilogoContaSoloGaraEffettiva);
        esegui("Riepilogo senza gara",              TestLog::riepilogoSenzaGara);

        System.out.println("\n=== Risultato: " + passati + " passati, " + falliti + " falliti ===");
        System.exit(falliti == 0 ? 0 : 1);
    }

    private static void esegui(String nome, Runnable test) {
        try {
            test.run();
            System.out.println("  [OK]  " + nome);
            passati++;
        } catch (Throwable t) {
            System.out.println("  [!!]  " + nome + "\n        " + t.getMessage());
            falliti++;
        }
    }
}
