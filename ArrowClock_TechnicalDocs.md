# ArrowClock — Technical Documentation / Documentazione Tecnica

> **Author / Autore:** Giovanni Zucchi — Arcieri Aquila Bianca, Modena  
> **Language / Linguaggio:** Java 17+ (Swing GUI)

---

## Table of Contents / Indice

1. [Architecture Overview / Panoramica Architetturale](#1-architecture-overview--panoramica-architetturale)
2. [Class Inventory / Inventario delle Classi](#2-class-inventory--inventario-delle-classi)
3. [Design Patterns](#3-design-patterns)
4. [State Machine / Macchina a Stati](#4-state-machine--macchina-a-stati)
5. [Timer Engine / Motore Timer](#5-timer-engine--motore-timer)
6. [Audio Engine / Motore Audio](#6-audio-engine--motore-audio)
7. [Display System / Sistema Display](#7-display-system--sistema-display)
8. [Localisation / Localizzazione](#8-localisation--localizzazione)
9. [Logging / Sistema di Log](#9-logging--sistema-di-log)
10. [Keyboard Shortcuts / Scorciatoie](#10-keyboard-shortcuts--scorciatoie)
11. [Known Minor Issues / Anomalie Note](#11-known-minor-issues--anomalie-note)

---

---

# ENGLISH

---

## 1. Architecture Overview

ArrowClock is a single-process Java Swing application that follows the **Command Pattern** (Robert C. Martin, *Clean Code*) as its central architectural principle. Every user action, timer tick, and state transition is encapsulated in a dedicated `Comando` class. The main class `ArcherySoftwareMain` acts as a **shared state container** only — it holds all fields but contains no business logic itself.

### Layered responsibilities

```
┌─────────────────────────────────────────────────────────────┐
│  CostruttoreOperatore       — Swing UI assembly             │
│  GestoreScorciatoie         — Keyboard binding              │
├─────────────────────────────────────────────────────────────┤
│  Comando* classes           — All business logic            │
│  (one class = one operation)                                │
├─────────────────────────────────────────────────────────────┤
│  MotoreTimer                — Countdown tick engine         │
│  MotoreAudio                — Whistle / sound engine        │
│  MotoreFontDinamico         — Responsive font calculator    │
├─────────────────────────────────────────────────────────────┤
│  GestoreLingua              — Localisation registry         │
│  GestoreLog                 — File logging                  │
├─────────────────────────────────────────────────────────────┤
│  ArcherySoftwareMain        — Shared state (fields only)    │
└─────────────────────────────────────────────────────────────┘
```

### Entry point

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(ArcherySoftwareMain::new);
}
```

The constructor of `ArcherySoftwareMain` orchestrates initialisation in strict order: UI colour defaults → icon loading → audio prewarm → log/timer collaborators → window creation → operator UI construction → theme → shortcuts → preset → initial reset → session log.

---

## 2. Class Inventory

### 2.1 Core / State

| Class | Role |
|---|---|
| `ArcherySoftwareMain` | Central state container. Holds all Swing components and application state as package-accessible fields. Owns `main()`. |
| `Fase` | `enum` of all application states: `ATTESA`, `PREPARAZIONE_ROSSO`, `TIRO_VERDE_GIALLO`, `SCONTRO_TIRO_SX`, `SCONTRO_TIRO_DX`, `EMERGENZA`, `RECUPERO_ATTESA`, `RECUPERO_TIRO`, `IDENTIFICAZIONE_MONITOR`. |
| `Comando` | Marker interface with a single `void esegui()` method — the Command Pattern contract. |

### 2.2 Command Classes

Each class implements `Comando` and encapsulates exactly one operation.

| Class | Triggered by | Effect |
|---|---|---|
| `ComandoAvviaOSalta` | SPACE key / START button | Starts a new end or skips the current phase. |
| `ComandoReset` | G key / RESET button | Stops all timers, returns to `ATTESA`, clears recovery state. |
| `ComandoFermaTutto` | Multiple commands | Atomic stop: halts timers, resets phase and times, restores colours. Used internally. |
| `ComandoEmergenza` | ENTER key | Activates or resolves emergency mode (freeze → blink → STOP). |
| `ComandoRecupero` | R key | Manages recovery phase: immediate 40s, booking, or +40s increment. |
| `ComandoChiudiRecupero` | Auto / SPACE during recovery | Ends recovery with 3 whistles, resets to `ATTESA`. |
| `ComandoInnescaRecupero` | End of end (if recovery booked) | Transitions to `RECUPERO_ATTESA` with 40s initial time. |
| `ComandoTickTimer` | `MotoreTimer` (every second) | One-second countdown logic, delegates to phase-specific handlers. |
| `ComandoIniziaPrepSingola` | Start of end (linear mode) | Sets `PREPARAZIONE_ROSSO`, loads T1, applies RED colour. |
| `ComandoPassaAlTiroSingolo` | T1 timeout / SPACE skip | Sets `TIRO_VERDE_GIALLO`, loads T2, applies GREEN or YELLOW. |
| `ComandoGestisciFineFaseTiro` | T2 timeout / SPACE skip | Advances to next turn or closes the end (with or without recovery). |
| `ComandoConcludiCiclo` | End of final turn | Calls `FermaTutto` + `AvanzaVolee`, refreshes display and turn text. |
| `ComandoAvanzaVolee` | End of each end | Increments end counter, handles part transitions, rotates turn index. |
| `ComandoIniziaScontro` | START in MATCH mode | Initialises match parameters (exchanges, times), sets `PREPARAZIONE_ROSSO`. |
| `ComandoPassaAlTiroScontro` | T1 timeout / SPACE skip (match) | Sets `SCONTRO_TIRO_SX` or `SCONTRO_TIRO_DX`, loads individual/team time. |
| `ComandoScambiaTurnoScontro` | End of shooter's turn | Saves residual time (teams), switches sides or closes the end. |
| `ComandoIdentificaMonitor` | I key | Shows monitor number on each external display for cabling identification. |
| `ComandoApplicaPreset` | Preset combo change | Configures T1/T2/T3/mode/end-limit for the selected preset. Calls `ComandoReset`. |
| `ComandoBloccaInterfaccia` | Phase changes | Enables/disables spinner and combo controls based on current state and mode. |
| `ComandoAggiornaDisplay` | Nearly every state change | Master display sync: updates fonts, colours, and timer texts on all monitors. |
| `ComandoAggiornaBottoni` | Phase changes | Updates colour, text, and enabled state of all action buttons. |
| `ComandoAggiornaTestoTurno` | Phase / turn changes | Recalculates and sets the turn label on all displays and the turn button. |
| `ComandoAggiornaTesti` | Language change | Refreshes all static labels, titled borders, and combo renderers. |
| `ComandoAggiornaTema` | Theme toggle | Recursively applies dark/light colours to the entire operator panel. |
| `ComandoApplicaLayoutMonitor` | Mode/phase changes | Selects which CardLayout card (SINGOLO/SCONTRO/TURNI/IDENTIFICAZIONE) to show. |
| `ComandoImpostaColoriSingoli` | Colour-triggering phases | Sets the background colour of the single-mode semaphore panel + text colour. |
| `ComandoImpostaColoriScontro` | Colour-triggering phases (match) | Sets independent background colours for left and right semaphore panels. |
| `ComandoRipristinaColoriSemaforo` | Theme change | Re-applies the correct semaphore colour for the current phase after a theme switch. |
| `ComandoAggiornaLabelEmergenza` | Emergency activation | Freezes the current time on the emergency overlay labels. |
| `ComandoPulisciLabelEmergenza` | Emergency end / Reset | Clears the emergency overlay labels. |
| `ComandoCambiaLingua` | L key | Calls `GestoreLingua.toggle()` then `ComandoAggiornaTesti`. |

### 2.3 Engine / Service Classes

| Class | Pattern | Role |
|---|---|---|
| `MotoreTimer` | — | Manages the Swing `Timer` at 100ms intervals. Accumulates fractional seconds with **separate accumulators** for left/right sides to prevent "bleeding" in match mode. |
| `MotoreAudio` | Singleton | Whistle sound engine. Uses a `SourceDataLine` and a single-thread `ExecutorService` to generate square-wave tones. `azzeraCodaFischi()` immediately stops any in-progress sound by incrementing a generation counter. |
| `MotoreFontDinamico` | Static utility | Computes three font sizes (numbers, STOP word, labels) based on the container's physical diagonal in inches, using the display's DPI. The STOP font has an independent horizontal cap to prevent truncation. |
| `GestoreLingua` | Registry | Static map of all localised strings. Supports IT and EN. Keys are hierarchical (`btn.start`, `panel.tempi`, etc.). `t(key)` retrieves, `tf(key, args)` formats with `printf`-style arguments. |
| `GestoreLog` | — | Writes timestamped events to `~/ArrowClock_Logs/ArrowClock_Log.txt`. Only writes when `isGaraInCorso == true`. Internally translates hardcoded Italian state strings via `traduciStatoRecupero()`. |
| `GestoreScorciatoie` | — | Maps keyboard shortcuts to commands using Swing `InputMap`/`ActionMap` on the operator panel. |

### 2.4 UI Builder / Dialog Classes

| Class | Role |
|---|---|
| `CostruttoreOperatore` | Assembles the entire operator window: title bar, toolbar, 4-quadrant grid, bottom bar. Wires all action listeners. |
| `DisplayArciere` | Builds one external archer display window with CardLayout for SINGOLO/SCONTRO/TURNI/IDENTIFICAZIONE modes. Manages its own font state and responsive resize listener. |
| `DialogTempo` | Modal dialog for manual time correction during emergency. Supports ±40s, arrow-count × seconds calculation, keyboard bindings. |
| `DialogNomiScontro` | Modal dialog for entering competitor/team names shown on the boards. |
| `FabbricaCombo` | Factory creating themed `JComboBox` instances with custom `ComboTemaUI` and `RendererComboLocalizzato`. |
| `ComboTemaUI` | `BasicComboBoxUI` subclass that fully overrides the native combo rendering for dark/light theme consistency. |
| `RendererComboLocalizzato` | `ListCellRenderer` that translates combo item keys through `GestoreLingua` before display. |

### 2.5 Strategy / Formatter Classes

| Class | Pattern | Role |
|---|---|---|
| `FormattatoreTempo` | Abstract Strategy | Factory + abstract base for time formatting. `crea(stato)` returns the right subclass. |
| `FormattatoreSecondi` | Concrete Strategy | Formats time as integer seconds: `"120"`. |
| `FormattatoreMMSS` | Concrete Strategy | Formats time as `"MM:SS"`: `"02:00"`. |
| `FormattatoreInvisibile` | Concrete Strategy | Formats as MM:SS but returns the **background colour** as foreground (invisible). |
| `GeneratoreTestoTurno` | — | Pure function: computes the turn display string (active group in UPPERCASE, others lowercase). |
| `GeneratoreTestoBottone` | — | Pure function: computes the start-turn button label. |

---

## 3. Design Patterns

### 3.1 Command Pattern
`Comando` is the interface; every operation is a class. Commands receive `ArcherySoftwareMain` via constructor injection. This makes each operation independently testable and keeps the main class free of logic.

### 3.2 Strategy Pattern
`FormattatoreTempo` and its three subclasses (`FormattatoreSecondi`, `FormattatoreMMSS`, `FormattatoreInvisibile`) allow the timer display format to be swapped at runtime without conditionals throughout the codebase.

### 3.3 Singleton Pattern
`MotoreAudio` is a lazy-initialised singleton (`MotoreAudio.istanza()`), ensuring that only one audio line is ever open and that `azzeraCodaFischi()` always acts on the correct instance.

### 3.4 Factory Method
`FormattatoreTempo.crea(stato)` and `FabbricaCombo.crea(items, app)` centralise object construction, keeping instantiation logic in one place.

### 3.5 State Machine
The application's behaviour is driven entirely by the `Fase` enum value stored in `app.faseAttuale`. Each `Comando` reads this value and routes to the correct logic branch. See Section 4 for the full state diagram.

---

## 4. State Machine

```
                        ┌─────────────────┐
                  ┌────▶│     ATTESA      │◀──────────────────┐
                  │     └────────┬────────┘                   │
          RESET / │              │ SPACE (avvia volée)        │
          FermaTutto             ▼                            │
                  │     ┌─────────────────┐                   │
                  │     │ PREPARAZIONE    │                   │
                  │     │    ROSSO (T1)   │                   │
                  │     └────────┬────────┘                   │
                  │              │ T1 timeout / SPACE         │ ComandoFermaTutto
                  │              ▼                            │
                  │  ┌──────────────────────┐                │
                  │  │  TIRO_VERDE_GIALLO   │                │
                  │  │  (T2, GREEN → YELLOW)│                │
                  │  └──────────┬───────────┘                │
                  │             │ T2 timeout / SPACE          │
                  │             ▼                             │
                  │  ┌─────────────────────┐                 │
                  │  │  [all turns done?]  │ ──No──▶ back to PREPARAZIONE_ROSSO (next turn)
                  │  └──────────┬──────────┘                 │
                  │             │ Yes                         │
                  │      ┌──────┴──────┐                     │
                  │  Recovery?  Yes ──▶ RECUPERO_ATTESA ──▶ RECUPERO_TIRO ──┘
                  │      │ No                                 
                  └──────┘ (3 whistles → ATTESA)             

  MATCH MODE (replaces TIRO_VERDE_GIALLO):
  PREPARAZIONE_ROSSO → SCONTRO_TIRO_SX ⇄ SCONTRO_TIRO_DX → (end) → ATTESA

  EMERGENCY (any active phase):
  any phase ──ENTER──▶ EMERGENZA ──ENTER──▶ (restores saved phase)

  IDENTIFICATION:
  ATTESA ──I──▶ IDENTIFICAZIONE_MONITOR ──I──▶ ATTESA
```

---

## 5. Timer Engine

`MotoreTimer` runs a Swing `Timer` at **100ms intervals**. A high-precision `System.nanoTime()` delta is used to accumulate fractional seconds, firing `ComandoTickTimer` once per accumulated whole second. This avoids timer drift over long sessions.

**Three separate accumulators** prevent fractional time from "bleeding" between phases:
- `accumulatoreSx` — used exclusively during `SCONTRO_TIRO_SX`
- `accumulatoreDx` — used exclusively during `SCONTRO_TIRO_DX`
- `app.accumulatoreSecondi` — used for all other phases (preparation, linear shooting, recovery)

Each accumulator is reset (via `sincronizzaEResetta*()` methods) every time the corresponding phase begins, ensuring phase transitions start with a clean sub-second state.

---

## 6. Audio Engine

`MotoreAudio` generates whistles as **square-wave tones** at 750 Hz for 600ms, with a 15ms linear fade-in and fade-out to prevent clicking. Audio is produced on a dedicated `ExecutorService` thread.

**Immediate cancellation** is implemented via a volatile `generazioneAudio` integer counter. Every submitted audio task checks the counter against its own snapshot; if they differ (because `azzeraCodaFischi()` was called), the task exits. The `SourceDataLine` is also flushed and closed synchronously under a lock.

A silent keepalive thread (`avviaMotoreAudioSilenzioso`) prewarms the audio subsystem at startup to eliminate the latency on the first whistle.

---

## 7. Display System

### 7.1 Multiple Monitors
At startup, `createArcherWindow()` enumerates `GraphicsDevice` instances:
- **One monitor:** a single non-fullscreen `DisplayArciere` window at 1000×600.
- **Two or more monitors:** fullscreen undecorated windows on each secondary screen.

### 7.2 CardLayout
Each `DisplayArciere` and the operator miniature panel use `CardLayout` with four named cards:
- `"SINGOLO"` — full-screen single timer panel.
- `"SCONTRO"` — split-screen left/right panels.
- `"TURNI"` — full-screen team/group turn display (dark background).
- `"IDENTIFICAZIONE"` — blue panel showing the monitor's sequential number.

`ComandoApplicaLayoutMonitor` selects the correct card on every state change.

### 7.3 Responsive Fonts
`MotoreFontDinamico.calcolaDimensioni()` computes three sizes (numbers, STOP word, labels) from the container's physical diagonal in inches:
- **Numbers** target ~25cm equivalent at full diagonal, scaled down for smaller containers.
- **STOP** has an additional width cap (`boundWidth / 5.2`) to prevent the word from being truncated horizontally.
- **Labels** are always 1/5 of the number size.

Both `DisplayArciere` and the operator miniature have `ComponentListener` resize handlers that call this engine.

---

## 8. Localisation

`GestoreLingua` is a static registry backed by a `HashMap<String, EnumMap<Lingua, String>>`. All 100+ UI strings are registered in a single static initialiser block. The current language (`IT` or `EN`) is toggled by `ComandoCambiaLingua` and persists for the session only (no persistence to disk).

Combo-box items use **key-based internal values** (the Italian string): the renderer calls `GestoreLingua.t("combo." + value)` at paint time, so the model never needs to change when the language switches.

---

## 9. Logging

`GestoreLog.scriviLog()` appends to `~/ArrowClock_Logs/ArrowClock_Log.txt` using `FileWriter(path, true)` (append mode). Writing only occurs when `app.isGaraInCorso == true`.

Each entry is prefixed with `[HH:MM:SS][Part N]`. The log is written in the **currently active language** at the time of each event. Hardcoded Italian state strings passed from Command classes are intercepted and translated by `traduciStatoRecupero()` and `traduciNotifica()` inside `GestoreLog`.

---

## 10. Keyboard Shortcuts

Shortcuts are registered on the operator panel's `ContentPane` `InputMap` with `WHEN_IN_FOCUSED_WINDOW` scope. Each shortcut checks whether the corresponding button is enabled before firing, respecting the `ComandoBloccaInterfaccia` state.

| Key | Action Class |
|---|---|
| SPACE | `ComandoAvviaOSalta` |
| ENTER | `ComandoEmergenza` |
| G | `ComandoReset` |
| R | `ComandoRecupero` |
| F | `MotoreAudio.eseguiFischi(1, ...)` |
| T | `btnAlternaMetata.doClick()` |
| M | `btnFormatoTempo.doClick()` |
| C | `btnTema.doClick()` |
| S | `btnSuono.doClick()` |
| D | `btnToggleTurniSpecial.doClick()` |
| I | `btnIdentificaMonitor.doClick()` |
| L | `ComandoCambiaLingua` |
| N | `DialogNomiScontro` (match mode only) |
| SHIFT | `DialogTempo` (emergency only) |
| ↑ / ↓ | Increment / decrement `spinVolee` |

---

## 11. Known Minor Issues

| # | Issue | Location | Severity | Suggested Fix |
|---|---|---|---|---|
| 1 | `app.lastTickTime` is declared but never used | `ArcherySoftwareMain` | Low (dead code) | Remove field; `MotoreTimer` uses its own `lastTickTimeNano`. |
| 2 | `app.accumulatoreSecondi` is owned by two classes | `ArcherySoftwareMain`, `MotoreTimer` | Low | Move the field into `MotoreTimer`; expose read access if needed. |
| 3 | `fasePreRecupero`, `tempoSxPreRecupero`, `tempoDxPreRecupero`, `colorePreRecupero` are written but never read | `ArcherySoftwareMain`, `ComandoInnescaRecupero` | Low (dead code) | Remove all four fields and the write statements in `ComandoInnescaRecupero`. |
| 4 | `ComandoAggiornaTema.applicaRicorsivo()` is `public` | `ComandoAggiornaTema` | Low (encapsulation leak) | Change to `package-private`; extract a dedicated `applicaTema(Container)` helper used by `DialogTempo`. |
| 5 | `faseSalvata` is null until first emergency | `ArcherySoftwareMain` | Low (robustness) | Initialise to `Fase.ATTESA` in the constructor to prevent potential NPE if `risolviEmergenza` were ever reached via an unexpected code path. |

---
---

# ITALIANO

---

## 1. Panoramica Architetturale

ArrowClock è un'applicazione Java Swing a processo singolo che adotta il **Command Pattern** (Robert C. Martin, *Clean Code*) come principio architetturale centrale. Ogni azione utente, tick del timer e transizione di stato è incapsulata in una classe `Comando` dedicata. La classe principale `ArcherySoftwareMain` funge **esclusivamente da contenitore di stato condiviso** — contiene tutti i campi ma non contiene logica di business.

### Responsabilità a livelli

```
┌─────────────────────────────────────────────────────────────┐
│  CostruttoreOperatore       — Assemblaggio UI Swing         │
│  GestoreScorciatoie         — Binding tastiera              │
├─────────────────────────────────────────────────────────────┤
│  Classi Comando*            — Tutta la logica di business   │
│  (una classe = un'operazione)                               │
├─────────────────────────────────────────────────────────────┤
│  MotoreTimer                — Motore tick conto alla rovesc.│
│  MotoreAudio                — Motore fischi / audio         │
│  MotoreFontDinamico         — Calcolo font responsivo       │
├─────────────────────────────────────────────────────────────┤
│  GestoreLingua              — Registro localizzazione       │
│  GestoreLog                 — Log su file                   │
├─────────────────────────────────────────────────────────────┤
│  ArcherySoftwareMain        — Stato condiviso (solo campi)  │
└─────────────────────────────────────────────────────────────┘
```

### Punto di ingresso

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(ArcherySoftwareMain::new);
}
```

Il costruttore di `ArcherySoftwareMain` orchestra l'inizializzazione in ordine stretto: colori UI disabilitati → icona app → preriscaldamento audio → collaboratori log/timer → creazione finestre → costruzione UI operatore → tema → scorciatoie → preset → reset iniziale → log sessione.

---

## 2. Inventario delle Classi

### 2.1 Core / Stato

| Classe | Ruolo |
|---|---|
| `ArcherySoftwareMain` | Contenitore di stato centrale. Mantiene tutti i componenti Swing e lo stato dell'applicazione come campi package-accessible. Possiede `main()`. |
| `Fase` | `enum` di tutti gli stati dell'applicazione: `ATTESA`, `PREPARAZIONE_ROSSO`, `TIRO_VERDE_GIALLO`, `SCONTRO_TIRO_SX`, `SCONTRO_TIRO_DX`, `EMERGENZA`, `RECUPERO_ATTESA`, `RECUPERO_TIRO`, `IDENTIFICAZIONE_MONITOR`. |
| `Comando` | Interfaccia marcatore con un unico metodo `void esegui()` — il contratto del Command Pattern. |

### 2.2 Classi Comando

Ogni classe implementa `Comando` e incapsula esattamente un'operazione.

| Classe | Attivata da | Effetto |
|---|---|---|
| `ComandoAvviaOSalta` | Tasto SPAZIO / pulsante START | Avvia una nuova volée o salta la fase corrente. |
| `ComandoReset` | Tasto G / pulsante RESET | Ferma tutto, torna ad `ATTESA`, annulla lo stato di recupero. |
| `ComandoFermaTutto` | Più comandi | Stop atomico: ferma i timer, azzera fase e tempi, ripristina i colori. Uso interno. |
| `ComandoEmergenza` | Tasto INVIO | Attiva o risolve la modalità emergenza (congela → lampeggia → STOP). |
| `ComandoRecupero` | Tasto R | Gestisce il recupero: 40s immediati, prenotazione, o incremento +40s. |
| `ComandoChiudiRecupero` | Auto / SPAZIO durante recupero | Termina il recupero con 3 fischi, torna ad `ATTESA`. |
| `ComandoInnescaRecupero` | Fine volée (se recupero prenotato) | Transisce a `RECUPERO_ATTESA` con 40s iniziali. |
| `ComandoTickTimer` | `MotoreTimer` (ogni secondo) | Logica conto alla rovescia di un secondo, delega ai gestori specifici per fase. |
| `ComandoIniziaPrepSingola` | Inizio volée (modalità lineare) | Imposta `PREPARAZIONE_ROSSO`, carica T1, applica colore ROSSO. |
| `ComandoPassaAlTiroSingolo` | Timeout T1 / skip SPAZIO | Imposta `TIRO_VERDE_GIALLO`, carica T2, applica VERDE o GIALLO. |
| `ComandoGestisciFineFaseTiro` | Timeout T2 / skip SPAZIO | Avanza al turno successivo o chiude la volée. |
| `ComandoConcludiCiclo` | Fine ultimo turno | Chiama `FermaTutto` + `AvanzaVolee`, aggiorna display e testo turno. |
| `ComandoAvanzaVolee` | Fine di ogni volée | Incrementa contatore volée, gestisce transizioni di parte, ruota l'indice turni. |
| `ComandoIniziaScontro` | START in modalità SCONTRO | Inizializza i parametri dello scontro (scambi, tempi), imposta `PREPARAZIONE_ROSSO`. |
| `ComandoPassaAlTiroScontro` | Timeout T1 / skip SPAZIO (scontro) | Imposta `SCONTRO_TIRO_SX` o `SCONTRO_TIRO_DX`, carica il tempo individuale/squadra. |
| `ComandoScambiaTurnoScontro` | Fine del turno del tiratore | Salva il tempo residuo (squadre), cambia lato o chiude la volée. |
| `ComandoIdentificaMonitor` | Tasto I | Mostra il numero di ciascun monitor esterno per l'identificazione del cablaggio. |
| `ComandoApplicaPreset` | Cambio combo Preset | Configura T1/T2/T3/modalità/limite-volée per il preset scelto. Chiama `ComandoReset`. |
| `ComandoBloccaInterfaccia` | Cambi di fase | Abilita/disabilita spinner e combo in base allo stato corrente e alla modalità. |
| `ComandoAggiornaDisplay` | Quasi ogni cambio di stato | Sincronizzazione display master: aggiorna font, colori e testi timer su tutti i monitor. |
| `ComandoAggiornaBottoni` | Cambi di fase | Aggiorna colore, testo e stato abilitato di tutti i pulsanti di azione. |
| `ComandoAggiornaTestoTurno` | Cambi di fase/turno | Ricalcola e imposta l'etichetta del turno su tutti i display e il pulsante di turno. |
| `ComandoAggiornaTesti` | Cambio lingua | Aggiorna tutte le etichette statiche, i bordi titolati e i renderer dei combo. |
| `ComandoAggiornaTema` | Toggle tema | Applica ricorsivamente i colori scuro/chiaro all'intero pannello operatore. |
| `ComandoApplicaLayoutMonitor` | Cambi modalità/fase | Seleziona quale card del CardLayout (SINGOLO/SCONTRO/TURNI/IDENTIFICAZIONE) mostrare. |
| `ComandoImpostaColoriSingoli` | Fasi che cambiano colore | Imposta il colore di sfondo del pannello semaforo in modalità singola + colore testo. |
| `ComandoImpostaColoriScontro` | Fasi che cambiano colore (scontro) | Imposta colori indipendenti per i pannelli semaforo sinistro e destro. |
| `ComandoRipristinaColoriSemaforo` | Cambio tema | Riapplica il colore semaforo corretto per la fase corrente dopo un cambio tema. |
| `ComandoAggiornaLabelEmergenza` | Attivazione emergenza | Congela il tempo corrente sulle etichette overlay di emergenza. |
| `ComandoPulisciLabelEmergenza` | Fine emergenza / Reset | Azzera le etichette overlay di emergenza. |
| `ComandoCambiaLingua` | Tasto L | Chiama `GestoreLingua.toggle()` poi `ComandoAggiornaTesti`. |

### 2.3 Classi Engine / Servizio

| Classe | Pattern | Ruolo |
|---|---|---|
| `MotoreTimer` | — | Gestisce il `Timer` Swing a intervalli da 100ms. Accumula secondi frazionari con **accumulatori separati** per i lati sinistro/destro per prevenire "bleeding" in modalità scontro. |
| `MotoreAudio` | Singleton | Motore audio per fischi. Usa `SourceDataLine` e `ExecutorService` a thread singolo per generare toni a onda quadra. `azzeraCodaFischi()` ferma immediatamente qualsiasi suono in corso tramite un contatore di generazione volatile. |
| `MotoreFontDinamico` | Utility statica | Calcola tre dimensioni di font (numeri, parola STOP, etichette) sulla diagonale fisica del contenitore in pollici, usando il DPI del display. Il font STOP ha un limite orizzontale indipendente per prevenire troncature. |
| `GestoreLingua` | Registro | Mappa statica di tutte le stringhe localizzate. Supporta IT e EN. `t(chiave)` recupera, `tf(chiave, args)` formatta in stile `printf`. |
| `GestoreLog` | — | Scrive eventi con timestamp su `~/ArrowClock_Logs/ArrowClock_Log.txt`. Scrive solo quando `isGaraInCorso == true`. Traduce internamente le stringhe di stato tramite `traduciStatoRecupero()`. |
| `GestoreScorciatoie` | — | Mappa le scorciatoie da tastiera ai comandi usando `InputMap`/`ActionMap` Swing sul pannello operatore. |

### 2.4 Classi UI Builder / Dialog

| Classe | Ruolo |
|---|---|
| `CostruttoreOperatore` | Assembla l'intera finestra operatore: barra del titolo, toolbar, griglia a 4 quadranti, barra inferiore. Collega tutti gli action listener. |
| `DisplayArciere` | Costruisce una finestra display arcieri esterna con CardLayout per le modalità SINGOLO/SCONTRO/TURNI/IDENTIFICAZIONE. Gestisce il proprio stato font e il listener di resize responsivo. |
| `DialogTempo` | Dialog modale per la correzione manuale del tempo durante l'emergenza. Supporta ±40s, calcolo frecce × secondi, binding tastiera. |
| `DialogNomiScontro` | Dialog modale per inserire i nomi degli arcieri/squadre mostrati sui tabelloni. |
| `FabbricaCombo` | Factory per creare istanze `JComboBox` tematizzate con `ComboTemaUI` e `RendererComboLocalizzato` personalizzati. |
| `ComboTemaUI` | Sottoclasse di `BasicComboBoxUI` che sovrascrive completamente il rendering nativo del combo per la coerenza tema scuro/chiaro. |
| `RendererComboLocalizzato` | `ListCellRenderer` che traduce le chiavi degli elementi del combo tramite `GestoreLingua` prima della visualizzazione. |

### 2.5 Classi Strategy / Formatter

| Classe | Pattern | Ruolo |
|---|---|---|
| `FormattatoreTempo` | Strategy astratta | Factory + base astratta per la formattazione del tempo. `crea(stato)` restituisce la sottoclasse giusta. |
| `FormattatoreSecondi` | Strategy concreta | Formatta il tempo come secondi interi: `"120"`. |
| `FormattatoreMMSS` | Strategy concreta | Formatta come `"MM:SS"`: `"02:00"`. |
| `FormattatoreInvisibile` | Strategy concreta | Formatta come MM:SS ma restituisce il **colore di sfondo** come colore del testo (invisibile). |
| `GeneratoreTestoTurno` | — | Funzione pura: calcola la stringa del display turni (gruppo attivo in MAIUSCOLO, altri in minuscolo). |
| `GeneratoreTestoBottone` | — | Funzione pura: calcola l'etichetta del pulsante turno di partenza. |

---

## 3. Design Pattern

### 3.1 Command Pattern
`Comando` è l'interfaccia; ogni operazione è una classe. I comandi ricevono `ArcherySoftwareMain` tramite iniezione nel costruttore. Questo rende ogni operazione testabile indipendentemente e mantiene la classe principale libera da logica.

### 3.2 Strategy Pattern
`FormattatoreTempo` e le sue tre sottoclassi consentono di cambiare il formato di visualizzazione del timer a runtime senza condizionali sparsi nel codice.

### 3.3 Singleton Pattern
`MotoreAudio` è un singleton a inizializzazione pigra (`MotoreAudio.istanza()`), garantendo che una sola linea audio sia mai aperta e che `azzeraCodaFischi()` agisca sempre sull'istanza corretta.

### 3.4 Factory Method
`FormattatoreTempo.crea(stato)` e `FabbricaCombo.crea(items, app)` centralizzano la costruzione degli oggetti.

### 3.5 State Machine
Il comportamento dell'applicazione è guidato interamente dal valore dell'enum `Fase` in `app.faseAttuale`. Ogni `Comando` legge questo valore e instrada alla logica corretta. Vedere Sezione 4 per il diagramma di stato completo.

---

## 4. Macchina a Stati

```
                        ┌─────────────────┐
                  ┌────▶│     ATTESA      │◀──────────────────┐
                  │     └────────┬────────┘                   │
          RESET / │              │ SPAZIO (avvia volée)       │
          FermaTutto             ▼                            │
                  │     ┌─────────────────┐                   │
                  │     │ PREPARAZIONE    │                   │
                  │     │    ROSSO (T1)   │                   │
                  │     └────────┬────────┘                   │
                  │              │ Timeout T1 / SPAZIO        │ ComandoFermaTutto
                  │              ▼                            │
                  │  ┌──────────────────────┐                │
                  │  │  TIRO_VERDE_GIALLO   │                │
                  │  │  (T2, VERDE → GIALLO)│                │
                  │  └──────────┬───────────┘                │
                  │             │ Timeout T2 / SPAZIO         │
                  │             ▼                             │
                  │  ┌─────────────────────┐                 │
                  │  │  [tutti turni done?]│ ──No──▶ torna a PREPARAZIONE_ROSSO (turno successivo)
                  │  └──────────┬──────────┘                 │
                  │             │ Sì                          │
                  │      ┌──────┴──────┐                     │
                  │  Recupero?  Sì ──▶ RECUPERO_ATTESA ──▶ RECUPERO_TIRO ──┘
                  │      │ No                                 
                  └──────┘ (3 fischi → ATTESA)

  MODALITÀ SCONTRO (sostituisce TIRO_VERDE_GIALLO):
  PREPARAZIONE_ROSSO → SCONTRO_TIRO_SX ⇄ SCONTRO_TIRO_DX → (fine) → ATTESA

  EMERGENZA (qualsiasi fase attiva):
  qualsiasi fase ──INVIO──▶ EMERGENZA ──INVIO──▶ (ripristina fase salvata)

  IDENTIFICAZIONE:
  ATTESA ──I──▶ IDENTIFICAZIONE_MONITOR ──I──▶ ATTESA
```

---

## 5. Motore Timer

`MotoreTimer` esegue un `Timer` Swing a **100ms di intervallo**. Un delta ad alta precisione (`System.nanoTime()`) viene usato per accumulare secondi frazionari, eseguendo `ComandoTickTimer` una volta per ogni secondo intero accumulato. Questo evita la deriva del timer nelle sessioni lunghe.

**Tre accumulatori separati** impediscono che il tempo frazionario "contamini" le transizioni di fase:
- `accumulatoreSx` — usato esclusivamente durante `SCONTRO_TIRO_SX`
- `accumulatoreDx` — usato esclusivamente durante `SCONTRO_TIRO_DX`
- `app.accumulatoreSecondi` — usato per tutte le altre fasi

Ogni accumulatore viene azzerato (tramite i metodi `sincronizzaEResetta*()`) ogni volta che inizia la fase corrispondente.

---

## 6. Motore Audio

`MotoreAudio` genera fischi come **toni a onda quadra** a 750 Hz per 600ms, con fade-in e fade-out lineari da 15ms per evitare clic. L'audio viene prodotto su un thread `ExecutorService` dedicato.

La **cancellazione immediata** è implementata tramite un contatore intero volatile `generazioneAudio`. Ogni task audio sottomesso confronta il contatore con il proprio snapshot; se differiscono (perché è stato chiamato `azzeraCodaFischi()`), il task esce. Il `SourceDataLine` viene anche flushato e chiuso in modo sincrono sotto un lock.

Un thread keepalive silenzioso (`avviaMotoreAudioSilenzioso`) preriscalda il sottosistema audio all'avvio per eliminare la latenza al primo fischio.

---

## 7. Sistema Display

### 7.1 Monitor Multipli
All'avvio, `createArcherWindow()` enumera le istanze `GraphicsDevice`:
- **Un monitor:** una singola finestra `DisplayArciere` non a schermo intero da 1000×600.
- **Due o più monitor:** finestre senza decorazioni a schermo intero su ciascuno schermo secondario.

### 7.2 CardLayout
Ogni `DisplayArciere` e il pannello miniatura dell'operatore usano `CardLayout` con quattro card nominate:
- `"SINGOLO"` — pannello timer singolo a schermo intero.
- `"SCONTRO"` — pannelli sinistro/destro affiancati.
- `"TURNI"` — display turni a schermo intero (sfondo scuro).
- `"IDENTIFICAZIONE"` — pannello blu che mostra il numero progressivo del monitor.

`ComandoApplicaLayoutMonitor` seleziona la card corretta ad ogni cambio di stato.

### 7.3 Font Responsivi
`MotoreFontDinamico.calcolaDimensioni()` calcola tre dimensioni dalla diagonale fisica del contenitore in pollici:
- **Numeri** mirano a ~25cm equivalenti alla diagonale completa, scalati per contenitori più piccoli.
- **STOP** ha un limite di larghezza aggiuntivo (`boundWidth / 5.2`) per evitare troncature orizzontali.
- **Etichette** sono sempre 1/5 della dimensione dei numeri.

Sia `DisplayArciere` che la miniatura dell'operatore hanno listener `ComponentListener` di resize che chiamano questo motore.

---

## 8. Localizzazione

`GestoreLingua` è un registro statico supportato da `HashMap<String, EnumMap<Lingua, String>>`. Tutte le 100+ stringhe UI sono registrate in un singolo blocco di inizializzazione statico. La lingua corrente (`IT` o `EN`) viene alternata da `ComandoCambiaLingua` e persiste solo per la sessione (senza salvataggio su disco).

Gli elementi dei combo-box usano **valori interni basati su chiavi** (la stringa italiana): il renderer chiama `GestoreLingua.t("combo." + valore)` al momento del disegno, così il modello non deve mai cambiare quando la lingua viene cambiata.

---

## 9. Sistema di Log

`GestoreLog.scriviLog()` aggiunge al file `~/ArrowClock_Logs/ArrowClock_Log.txt` usando `FileWriter(path, true)` (modalità append). La scrittura avviene solo quando `app.isGaraInCorso == true`.

Ogni voce è prefissata con `[HH:MM:SS][Parte N]`. Il log viene scritto nella **lingua attualmente attiva** al momento di ogni evento. Le stringhe di stato italiane hardcoded passate dalle classi Comando vengono intercettate e tradotte da `traduciStatoRecupero()` e `traduciNotifica()` all'interno di `GestoreLog`.

---

## 10. Scorciatoie da Tastiera

Le scorciatoie sono registrate sull'`InputMap` del `ContentPane` del pannello operatore con scope `WHEN_IN_FOCUSED_WINDOW`. Ogni scorciatoia verifica che il pulsante corrispondente sia abilitato prima di eseguire, rispettando lo stato di `ComandoBloccaInterfaccia`.

| Tasto | Classe Azione |
|---|---|
| SPAZIO | `ComandoAvviaOSalta` |
| INVIO | `ComandoEmergenza` |
| G | `ComandoReset` |
| R | `ComandoRecupero` |
| F | `MotoreAudio.eseguiFischi(1, ...)` |
| T | `btnAlternaMetata.doClick()` |
| M | `btnFormatoTempo.doClick()` |
| C | `btnTema.doClick()` |
| S | `btnSuono.doClick()` |
| D | `btnToggleTurniSpecial.doClick()` |
| I | `btnIdentificaMonitor.doClick()` |
| L | `ComandoCambiaLingua` |
| N | `DialogNomiScontro` (solo modalità scontro) |
| SHIFT | `DialogTempo` (solo durante emergenza) |
| ↑ / ↓ | Incrementa / decrementa `spinVolee` |

---

## 11. Anomalie Note (Minor Issues)

| # | Anomalia | Posizione | Gravità | Correzione Suggerita |
|---|---|---|---|---|
| 1 | `app.lastTickTime` dichiarato ma mai usato | `ArcherySoftwareMain` | Bassa (dead code) | Rimuovere il campo; `MotoreTimer` usa il proprio `lastTickTimeNano`. |
| 2 | `app.accumulatoreSecondi` ha doppia proprietà | `ArcherySoftwareMain`, `MotoreTimer` | Bassa | Spostare il campo in `MotoreTimer`; esporre accesso in lettura se necessario. |
| 3 | `fasePreRecupero`, `tempoSxPreRecupero`, `tempoDxPreRecupero`, `colorePreRecupero` scritti ma mai letti | `ArcherySoftwareMain`, `ComandoInnescaRecupero` | Bassa (dead code) | Rimuovere tutti e quattro i campi e le relative istruzioni di scrittura in `ComandoInnescaRecupero`. |
| 4 | `ComandoAggiornaTema.applicaRicorsivo()` è `public` | `ComandoAggiornaTema` | Bassa (violazione incapsulamento) | Rendere `package-private`; estrarre un helper dedicato `applicaTema(Container)` usato da `DialogTempo`. |
| 5 | `faseSalvata` può essere null al primo richiamo | `ArcherySoftwareMain` | Bassa (robustezza) | Inizializzare a `Fase.ATTESA` nel costruttore per prevenire potenziali NPE. |

---

*End of Technical Documentation / Fine della Documentazione Tecnica*
