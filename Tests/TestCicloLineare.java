import java.util.List;

/** Test della modalità lineare (qualifica): preparazione, tiro, turni, fischi. */
public class TestCicloLineare {

    /** Ciclo INDOOR completo a due turni (AB - CD): verifica fasi, tempi e conteggio fischi. */
    public static void indoorCicloCompletoDueTurni() {
        BancoDiProva b = new BancoDiProva();
        b.applicaPreset("INDOOR"); // T1=10, T2=120, T3=30, turni "AB - CD" (2 turni)

        // START → preparazione rossa del 1° turno: 2 fischi
        b.premiStart();
        Verifica.uguale("fase dopo start", Fase.PREPARAZIONE_ROSSO, b.app.faseAttuale);
        Verifica.uguale("T1 caricato", 10, b.app.timeRemainingSx);
        Verifica.uguale("fischi di partenza", List.of(2), b.fischi());

        // Fine preparazione → 1 fischio, si passa al tiro (T2)
        b.azzeraFischi();
        b.tick(10);
        Verifica.uguale("fase in tiro", Fase.TIRO_VERDE_GIALLO, b.app.faseAttuale);
        Verifica.uguale("T2 caricato", 120, b.app.timeRemainingSx);
        Verifica.uguale("fischio fine preparazione", List.of(1), b.fischi());

        // Fine 1° turno → 2 fischi e preparazione del 2° turno
        b.azzeraFischi();
        b.tick(120);
        Verifica.uguale("cambio turno", Fase.PREPARAZIONE_ROSSO, b.app.faseAttuale);
        Verifica.uguale("turno corrente = 2", 2, b.app.turnoCorrente);
        Verifica.uguale("2 fischi cambio turno", List.of(2), b.fischi());

        // 2° turno: preparazione → tiro
        b.azzeraFischi();
        b.tick(10);
        Verifica.uguale("2° turno in tiro", Fase.TIRO_VERDE_GIALLO, b.app.faseAttuale);
        Verifica.uguale("fischio fine prep 2° turno", List.of(1), b.fischi());

        // Fine 2° (ultimo) turno → 3 fischi e ciclo concluso
        b.azzeraFischi();
        b.tick(120);
        Verifica.uguale("fine volée → attesa", Fase.ATTESA, b.app.faseAttuale);
        Verifica.uguale("3 fischi di chiusura", List.of(3), b.fischi());
        // Rotazione dell'indice di partenza turni (2 turni → passa a 1)
        Verifica.uguale("rotazione indice partenza", 1, b.app.indicePartenza);
        b.chiudi();
    }

    /** La pressione manuale di SALTA non deve emettere fischi (a differenza dello scadere del timer). */
    public static void saltoManualeSilenzioso() {
        BancoDiProva b = new BancoDiProva();
        b.applicaPreset("INDOOR");
        b.premiStart();          // → preparazione (2 fischi)
        b.azzeraFischi();
        b.premiStart();          // SALTA preparazione: silenzioso
        Verifica.uguale("salto porta al tiro", Fase.TIRO_VERDE_GIALLO, b.app.faseAttuale);
        Verifica.uguale("nessun fischio sul salto", List.of(), b.fischi());
        b.chiudi();
    }

    /** L'avviso giallo scatta a T3 senza emettere fischi. */
    public static void avvisoGialloSenzaFischi() {
        BancoDiProva b = new BancoDiProva();
        b.applicaPreset("INDOOR"); // T2=120, T3=30
        b.premiStart();
        b.tick(10);                // in tiro, 120s
        b.azzeraFischi();
        b.tick(90);                // scende a 30 (=T3): giallo, nessun fischio
        Verifica.uguale("tempo a T3", 30, b.app.timeRemainingSx);
        Verifica.uguale("ancora in tiro", Fase.TIRO_VERDE_GIALLO, b.app.faseAttuale);
        Verifica.uguale("giallo senza fischi", List.of(), b.fischi());
        b.chiudi();
    }
}