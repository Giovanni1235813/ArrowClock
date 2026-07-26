import java.util.List;

/** Test del recupero materiale e dell'emergenza (congelamento tempo). */
public class TestRecuperoEmergenza {

    /** Recupero PRENOTATO durante la volée: 5 fischi a fine volée, poi ciclo di recupero. */
    public static void recuperoPrenotato() {
        BancoDiProva b = new BancoDiProva();
        b.applicaPreset("INDOOR");
        b.app.comboTurni.setSelectedItem("- Nessuno -"); // turno unico per arrivare presto a fine volée

        b.premiStart();                       // preparazione (2 fischi)
        b.premiRecupero();                    // prenota il recupero
        Verifica.vero("recupero prenotato", b.app.recuperoPrenotato);

        b.azzeraFischi();
        b.tick(10);                           // → tiro (1 fischio)
        b.tick(120);                          // fine volée con prenotazione → 5 fischi + innesco recupero
        Verifica.uguale("fischi prep+chiusura recupero", List.of(1, 5), b.fischi());
        Verifica.uguale("in attesa di recupero", Fase.RECUPERO_ATTESA, b.app.faseAttuale);
        Verifica.uguale("40s di recupero", 40, b.app.timeRemainingSx);
        Verifica.vero("prenotazione consumata", !b.app.recuperoPrenotato);

        // START → parte il tiro di recupero (1 fischio)
        b.azzeraFischi();
        b.premiStart();
        Verifica.uguale("tiro di recupero", Fase.RECUPERO_TIRO, b.app.faseAttuale);
        Verifica.uguale("fischio inizio recupero", List.of(1), b.fischi());

        // Scade il recupero → 3 fischi e ritorno in attesa
        b.azzeraFischi();
        b.tick(40);
        Verifica.uguale("fine recupero → attesa", Fase.ATTESA, b.app.faseAttuale);
        Verifica.uguale("3 fischi fine recupero", List.of(3), b.fischi());
        b.chiudi();
    }

    /** Recupero IMMEDIATO da attesa e annullamento. */
    public static void recuperoImmediatoEAnnullamento() {
        BancoDiProva b = new BancoDiProva();
        b.applicaPreset("INDOOR");
        b.app.spinFrecceRecupero.setValue(2);
        b.app.spinSecFrecciaRecupero.setValue(40);
        b.premiRecupero();                    // da ATTESA → recupero immediato 80s (2*40)
        Verifica.uguale("attesa di recupero", Fase.RECUPERO_ATTESA, b.app.faseAttuale);
        Verifica.uguale("80s iniziali", 80, b.app.timeRemainingSx);
        b.premiRecupero();                    // annulla
        Verifica.uguale("fase attesa", Fase.ATTESA, b.app.faseAttuale);
        b.chiudi();
    }

    /** Emergenza durante il tiro: congela lo stato e lo ripristina alla risoluzione. */
    public static void emergenzaCongelaERipristina() {
        BancoDiProva b = new BancoDiProva();
        b.applicaPreset("INDOOR");
        b.premiStart();
        b.tick(10);          // in tiro (T2=120)
        b.tick(50);          // scende a 70
        int tempoPrima = b.app.timeRemainingSx;

        b.azzeraFischi();
        b.premiEmergenza();  // attiva emergenza: 5 fischi
        Verifica.uguale("fase emergenza", Fase.EMERGENZA, b.app.faseAttuale);
        Verifica.uguale("5 fischi emergenza", List.of(5), b.fischi());
        Verifica.uguale("tempo congelato", tempoPrima, b.app.timeRemainingSx);
        Verifica.uguale("fase salvata = tiro", Fase.TIRO_VERDE_GIALLO, b.app.faseSalvata);

        b.azzeraFischi();
        b.premiEmergenza();  // risolve: 1 fischio, ripristina la fase
        Verifica.uguale("ripresa in tiro", Fase.TIRO_VERDE_GIALLO, b.app.faseAttuale);
        Verifica.uguale("1 fischio ripresa", List.of(1), b.fischi());
        Verifica.uguale("tempo invariato dopo ripresa", tempoPrima, b.app.timeRemainingSx);
        b.chiudi();
    }
}