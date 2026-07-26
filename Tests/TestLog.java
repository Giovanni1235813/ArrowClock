import java.util.List;

/** Test del riepilogo di gara nel log: conteggi limitati ai periodi con "Gara in Corso" attiva. */
public class TestLog {

    /** Il riepilogo conta solo gli eventi avvenuti mentre "Gara in Corso" era attiva. */
    public static void riepilogoContaSoloGaraEffettiva() {
        GestoreLingua.setLingua(GestoreLingua.Lingua.EN);
        BancoDiProva b = new BancoDiProva();

        // Eventi PRIMA di attivare la gara: NON devono essere contati.
        b.app.spinVolee.setValue(1);
        b.app.gestoreLog.logFineVolee();
        b.app.gestoreLog.logEmergenza(true);

        // Attiva la gara e registra eventi reali.
        b.app.isGaraInCorso = true;
        b.app.gestoreLog.registraGaraOn();
        b.app.spinVolee.setValue(1);
        b.app.gestoreLog.logFineVolee();          // 1 volée di gara
        b.app.spinVolee.setValue(0);
        b.app.gestoreLog.logFineVolee();          // 1 volée di prova
        b.app.gestoreLog.logEmergenza(true);      // 1 emergenza
        b.app.gestoreLog.logRecuperoIniziato(40); // 1 recupero
        b.app.spinParte.setValue(3);
        b.app.gestoreLog.logInizioVolee();        // parte 3 raggiunta
        b.app.gestoreLog.registraGaraOff();

        b.app.gestoreLog.finalizzaSessione();
        List<String> righe = b.app.gestoreLog.costruisciRigheRiepilogo();

        Verifica.contiene("volée di gara = 1", righe, "Match ends completed: 1");
        Verifica.contiene("volée di prova = 1", righe, "Trial ends completed: 1");
        Verifica.contiene("emergenze = 1 (non 2)", righe, "Emergencies triggered: 1");
        Verifica.contiene("recuperi = 1", righe, "Equipment recoveries performed: 1");
        Verifica.contiene("parti = 3", righe, "Parts reached: 3");
        b.chiudi();
    }

    /** Senza gara attivata, il riepilogo lo dichiara esplicitamente. */
    public static void riepilogoSenzaGara() {
        GestoreLingua.setLingua(GestoreLingua.Lingua.EN);
        BancoDiProva b = new BancoDiProva();
        b.app.gestoreLog.finalizzaSessione();
        List<String> righe = b.app.gestoreLog.costruisciRigheRiepilogo();
        Verifica.contiene("nessuna gara registrata", righe, "No match recorded");
        b.chiudi();
    }
}
