import java.util.List;

/** Test della modalità scontro (chess-clock): alternanza lati, scambi, fischi. */
public class TestScontro {

    /** Scontro INDIVIDUALE (1 freccia → 2 scambi): SX poi DX poi chiusura. */
    public static void individualeDueScambi() {
        BancoDiProva b = new BancoDiProva();
        b.applicaPreset("SCONTRO ALTERNATO"); // INDIVIDUALE, T1=10, 20 s/freccia, 1 freccia

        b.premiStart();
        Verifica.uguale("preparazione scontro", Fase.PREPARAZIONE_ROSSO, b.app.faseAttuale);
        Verifica.uguale("2 scambi totali (1 freccia)", 2, b.app.scambiTotaliScontro);
        Verifica.uguale("2 fischi di partenza", List.of(2), b.fischi());

        // Fine preparazione → tiro di SINISTRA (20s), 1 fischio
        b.azzeraFischi();
        b.tick(10);
        Verifica.uguale("tiro sinistra", Fase.SCONTRO_TIRO_SX, b.app.faseAttuale);
        Verifica.uguale("tempo SX = 20", 20, b.app.timeRemainingSx);
        Verifica.uguale("fischio inizio tiro", List.of(1), b.fischi());

        // Scade SX → passa a DESTRA (20s), 1 fischio
        b.azzeraFischi();
        b.tick(20);
        Verifica.uguale("tiro destra", Fase.SCONTRO_TIRO_DX, b.app.faseAttuale);
        Verifica.uguale("tempo DX = 20", 20, b.app.timeRemainingDx);
        Verifica.uguale("uno scambio effettuato", 1, b.app.scambiEffettuati);
        Verifica.uguale("fischio cambio lato", List.of(1), b.fischi());

        // Scade DX → ultimo scambio → chiusura (3 fischi)
        b.azzeraFischi();
        b.tick(20);
        Verifica.uguale("fine scontro → attesa", Fase.ATTESA, b.app.faseAttuale);
        Verifica.uguale("due scambi effettuati", 2, b.app.scambiEffettuati);
        Verifica.uguale("3 fischi di chiusura", List.of(3), b.fischi());
        b.chiudi();
    }

    /** In modalità scontro il tasto RECUPERO è disabilitato (schermo diviso). */
    public static void recuperoIgnoratoInScontro() {
        BancoDiProva b = new BancoDiProva();
        b.applicaPreset("SCONTRO ALTERNATO");
        b.premiStart();
        b.tick(10); // tiro sinistra
        Fase prima = b.app.faseAttuale;
        b.premiRecupero();
        Verifica.uguale("recupero ignorato in scontro", prima, b.app.faseAttuale);
        Verifica.vero("nessuna prenotazione recupero", !b.app.recuperoPrenotato);
        b.chiudi();
    }
}