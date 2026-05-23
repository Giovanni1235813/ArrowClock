/**
 * Aggiorna il contatore di volée e la rotazione dei turni al termine
 * di ogni ciclo. Gestisce il cambio parte automatico, le volée di prova
 * e la rotazione dell'indice di partenza turni.
 */
public class ComandoAvanzaVolee implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoAvanzaVolee(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        int voleeAttuale = (int) app.spinVolee.getValue();
        int proveTotali  = (int) app.spinVoleeProva.getValue();
        int parteAttuale = (int) app.spinParte.getValue();
        String preset    = String.valueOf(app.comboPreset.getSelectedItem());
        int limiteVolee  = calcolaLimiteVolee(preset);

        app.gestoreLog.logFineVolee();

        if (voleeAttuale == 0) {
            gestisciProva(proveTotali);
        } else if (voleeAttuale >= limiteVolee) {
            gestisciCambioParte(parteAttuale);
        } else {
            app.spinVolee.setValue(voleeAttuale + 1);
        }

        ruotaTurni();
    }

    private int calcolaLimiteVolee(String preset) {
        if (preset.equals("INDOOR"))  return 10;
        if (preset.equals("OUTDOOR")) return 6;
        return 9999;
    }

    private void gestisciProva(int proveTotali) {
        if (app.attualeVoleeProva < proveTotali) {
            app.attualeVoleeProva++;
        } else {
            app.spinVolee.setValue(1);
            app.spinParte.setValue(1);
            app.gestoreLog.logNotificaParte("FINE VOLÉE DI PROVA");
        }
    }

    private void gestisciCambioParte(int parteAttuale) {
        app.gestoreLog.logNotificaParte("FINE PARTE " + parteAttuale);
        app.spinParte.setValue(parteAttuale + 1);
        app.spinVolee.setValue(1);
    }

    private void ruotaTurni() {
        if (app.isScontroMode) {
            app.iniziaConPrimaParte = !app.iniziaConPrimaParte;
        } else if (app.totaleTurni > 1) {
            app.indicePartenza = (app.indicePartenza + 1) % app.totaleTurni;
        }
    }
}
