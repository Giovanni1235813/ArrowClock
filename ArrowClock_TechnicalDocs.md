# ArrowClock — Technical Documentation / Documentazione Tecnica

> **Author / Autore:** Giovanni Zucchi — Arcieri Aquila Bianca, Modena  
> **Language / Linguaggio:** Java 17+ (Swing GUI)

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Class Inventory](#2-class-inventory)
3. [Design Patterns](#3-design-patterns)
4. [State Machine](#4-state-machine)
5. [Timer Engine](#5-timer-engine)
6. [Audio Engine](#6-audio-engine)
7. [Display System](#7-display-system)
8. [Localisation](#8-localisation)
9. [Logging](#9-logging)
10. [Keyboard Shortcuts](#10-keyboard-shortcuts)

---

## Indice

1. [Panoramica Architetturale](#1-panoramica-architetturale)
2. [Inventario delle Classi](#2-inventario-delle-classi)
3. [Design Pattern](#3-design-pattern)
4. [Macchina a Stati](#4-macchina-a-stati)
5. [Motore Timer](#5-motore-timer)
6. [Motore Audio](#6-motore-audio)
7. [Sistema Display](#7-sistema-display)
8. [Localizzazione](#8-localizzazione)
9. [Sistema di Log](#9-sistema-di-log)
10. [Scorciatoie](#10-scorciatoie-da-tastiera)

---

## ENGLISH

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

| Class | Pattern | Role                                                                                                                                                                                                                                                                                          |
|---|---|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `MotoreTimer` | — | Manages the Swing `Timer` at 100ms intervals. Accumulates fractional seconds with **separate accumulators** for left/right sides to prevent "bleeding" in match mode.                                                                                                                         |
| `MotoreAudio` | Singleton | Whistle sound engine. Uses a `SourceDataLine` and a single-thread `ExecutorService` to generate square-wave tones. `azzeraCodaFischi()` immediately stops any in-progress sound by incrementing a generation counter.                                                                         |
| `MotoreFontDinamico` | Static utility | Computes three base font sizes (numbers, STOP word, labels) using the display's DPI and physical diagonal. It also features a dynamic, character-by-character predictive algorithm to calculate the maximum safe font size for any custom text string, strictly preventing layout truncation. |
| `GestoreLingua` | Registry | Static map of all localised strings. Supports IT and EN. Keys are hierarchical (`btn.start`, `panel.tempi`, etc.). `t(key)` retrieves, `tf(key, args)` formats with `printf`-style arguments.                                                                                                 |
| `GestoreLog` | — | Writes timestamped events to `~/ArrowClock_Logs/ArrowClock_Log.txt`. Only writes when `isGaraInCorso == true`. Internally translates hardcoded Italian state strings via `traduciStatoRecupero()`.                                                                                            |
| `GestoreScorciatoie` | — | Maps keyboard shortcuts to commands using Swing `InputMap`/`ActionMap` on the operator panel.                                                                                                                                                                                                 |

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
                  │              │ T1 timeout                 │ ComandoFermaTutto
                  │              ▼                            │
                  │  ┌──────────────────────┐                 │
                  │  │  TIRO_VERDE_GIALLO   │                 │
                  │  │  (T2, GREEN → YELLOW)│                 │
                  │  └──────────┬───────────┘                 │
                  │             │ T2 timeout / SPACE          │
                  │             ▼                             │
                  │  ┌─────────────────────┐                  │
                  │  │  [all turns done?]  │ ──No──▶ back to PREPARAZIONE_ROSSO (next turn)
                  │  └──────────┬──────────┘                  │
                  │             │ Yes                         │
                  │      ┌──────┴──────┐                      │
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

### 7.3 Responsive & Predictive Fonts
`MotoreFontDinamico` acts as the centralized typography engine, employing two distinct strategies to strictly prevent layout truncation:

* **Static/Geometric Calculation (`calcolaDimensioni()`):** Computes three base sizes (numbers, STOP word, labels) using the container's physical diagonal and DPI.
    * **Numbers** target a strict ~25cm physical equivalent on full-size monitors to meet regulations, scaling down proportionally on smaller screens.
    * **STOP** applies an independent horizontal cap (`boundWidth / 5.2`) to prevent the wide word from being truncated horizontally.
    * **Labels** are structurally locked to 1/5 of the number size.
* **Dynamic/Predictive Calculation (`calcolaFontAdattivoPerTesto()`):** Scans custom text strings (e.g., team names, "RECUPERO") character by character to calculate their exact pixel footprint based on Arial Bold proportions. It dynamically returns the maximum safe font size that perfectly fits the current container without horizontal or vertical clipping.
* **Resize Handlers:** Both `DisplayArciere` and the operator miniature use `ComponentListener` events to instantly trigger this engine and recalculate metrics upon any layout or text change.

### 7.4 Rendering & Performance Optimizations
To ensure cross-platform fluidity, `ComandoAggiornaDisplay` employs two specific performance strategies:
- **State Filtering (`aggiornaTestoSicuro`):** A custom helper method filters out redundant `setText()` calls. This strictly prevents the Event Dispatch Thread (EDT) from being flooded with expensive font recalculations during fractional timer ticks when the time string hasn't actually changed.
- **Buffer Synchronization:** A manual `Toolkit.getDefaultToolkit().sync()` is invoked at the end of the rendering cycle. This forces the OS window manager (which is crucial for Linux/X11 systems) to flush pending graphics events immediately, guaranteeing simultaneous visual updates across all connected screens and preventing "waterfall" tearing effects.
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
| `MotoreFontDinamico` | Utility statica | Calcola tre dimensioni di base per i font (numeri, STOP, etichette) utilizzando i DPI e la diagonale fisica del monitor. Include inoltre un algoritmo predittivo dinamico che analizza carattere per carattere le stringhe di testo personalizzate, calcolandone la dimensione massima sicura per prevenirne il troncamento. |
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
                  │              │ Timeout T1                 │ ComandoFermaTutto
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

### 7.3 Font Responsivi e Predittivi
`MotoreFontDinamico` funge da motore tipografico centralizzato, utilizzando due strategie distinte per prevenire tassativamente il troncamento del layout:

* **Calcolo Statico/Geometrico (`calcolaDimensioni()`):** Calcola tre dimensioni di base (numeri, parola STOP, etichette) utilizzando la diagonale fisica e i DPI del contenitore.
    * I **Numeri** puntano a un equivalente fisico di ~25cm sui monitor da gara per soddisfare i regolamenti, scalandosi in modo proporzionale sugli schermi più piccoli.
    * Lo **STOP** applica un limite orizzontale indipendente (`boundWidth / 5.2`) per impedire che la parola venga troncata lateralmente.
    * Le **Etichette** sono bloccate strutturalmente a 1/5 della dimensione dei numeri.
* **Calcolo Dinamico/Predittivo (`calcolaFontAdattivoPerTesto()`):** Analizza le stringhe di testo personalizzate (es. nomi delle squadre, "RECUPERO") carattere per carattere, calcolando il loro ingombro esatto in pixel basato sulle proporzioni del font Arial Bold. Restituisce dinamicamente la dimensione massima del font sicura per riempire perfettamente il contenitore senza sforare i margini.
* **Gestione Eventi Resize:** Sia il `DisplayArciere` che la miniatura dell'operatore utilizzano eventi `ComponentListener` per invocare questo motore e ricalcolare istantaneamente le metriche ad ogni cambio di layout o di testo.

### 7.4 Ottimizzazioni di Rendering e Prestazioni
Per garantire una fluidità perfetta su qualsiasi sistema operativo, `ComandoAggiornaDisplay` adotta due strategie prestazionali:
- **Filtraggio dello Stato (`aggiornaTestoSicuro`):** Un metodo di supporto blocca le chiamate `setText()` ridondanti. Questo impedisce rigorosamente che l'Event Dispatch Thread (EDT) venga inondato da costosi ricalcoli dei font durante i tick frazionari in cui la stringa del tempo non è effettivamente cambiata.
- **Sincronizzazione del Buffer:** Alla fine del ciclo di rendering viene richiamato manualmente `Toolkit.getDefaultToolkit().sync()`. Questo forza il window manager del sistema operativo (fondamentale su Linux/X11) a svuotare immediatamente gli eventi grafici in sospeso, garantendo un aggiornamento visivo simultaneo su tutti gli schermi e annullando l'effetto "cascata" (tearing).
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

*End of Technical Documentation / Fine della Documentazione Tecnica*