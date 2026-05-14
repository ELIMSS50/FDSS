# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Two-pass SIC/XE assembler (Práctica 3) with a Swing GUI. Parses `.asm` files using an ANTLR4 grammar, performs semantic analysis (Pass 1), generates object code (Pass 2), and displays results in a tabbed window. Supports program blocks (`USE`), control sections (`CSECT`), and external symbols (`EXTDEF`/`EXTREF`).

## Build and run

**Maven (recommended):**
```bash
mvn package                          # compile + package fat JAR → target/p03-sicxe.jar
java -jar target/p03-sicxe.jar       # launch GUI
java -jar target/p03-sicxe.jar <file.asm>   # launch and pre-load a file
```

**Shell script (manual javac, works without Maven):**
```bash
./compilar_final.sh            # compile only
./compilar_final.sh run        # compile + run (prompts for file)
./compilar_final.sh run <file> # compile + run with file
```

There are no automated tests. The `ejemplos/` directory contains `.asm` sample files for manual testing.

## Architecture

### Two-pass assembly pipeline

```
.asm file
  └─ ANTLR4 lexer/parser (SICXE.g4)
       └─ AnalizadorSemantico  (Pass 1 — SICXEBaseListener)
            builds: list of SeccionControl (each with TABSIM, tabla de bloques, listado)
       └─ GeneradorCodigo      (Pass 2 — one instance per SeccionControl)
            generates: object code per instruction, H/D/R/T/M/E records per section
```

### Key classes

| Class | Role |
|---|---|
| `SICXE.g4` | ANTLR4 grammar; defines all SIC/XE tokens, instructions, directives, and expressions |
| `SeccionControl` | Data container for one control section (START or CSECT); holds its own symbol table, block table, listado, EXTDEF/EXTREF sets, and Pass 2 results |
| `AnalizadorSemantico` | Pass 1 listener: manages a list of `SeccionControl`, switches active section on `CSECT`, builds symbol tables, handles `EQU`/`ORG`/`USE`, evaluates expression types |
| `GeneradorCodigo` | Pass 2: one instance per `SeccionControl`; generates F1/F2/F3/F4 bytes, `BYTE`/`WORD` data, D/R/T/M records, and writes `.obj` file |
| `EntradaListado` | One source line's data: Pass 1 fields + `codigoObjeto`/`errorP2`/`tipoExpresion`/`nombreSeccion` filled during assembly |
| `RecolectorErrores` | ANTLR `BaseErrorListener` that collects lex/parse errors into a list |
| `InterfazGrafica` | Main Swing window: source editor + inline results; triggers full two-pass assembly; stores `ultimasSecciones` list |
| `VentanaResultados` | Secondary results window: outer `JTabbedPane` per section, inner tabs (Listado, Símbolos, Bloques, Registros obj), plus a global Errors tab |
| `Main` | Entry point; optionally pre-loads a file passed as CLI argument |

### Control sections (`CSECT`)

Each `CSECT` (and the initial `START`) creates a new `SeccionControl`. `AnalizadorSemantico` maintains `secciones` (a list) and `seccionActual`. On `CSECT`, the current section's state is saved and a new one is activated via `activarSeccion()`. Alias fields (`tablaSimbolos`, `listado`, etc.) always point to `seccionActual`'s collections.

`EXTDEF` marks symbols exported from a section; `EXTREF` declares symbols used here but defined elsewhere. Both are recorded in `SeccionControl.directivasExt` (ordered list of `DirectivaExt`) to preserve source order for D/R record generation.

### Program blocks (`USE`) within a section

Each `USE [name]` switches the active block inside the current section. `SeccionControl` maintains:
- `tablaBloques` — list of `int[4]` arrays: `[noBloque, unused, longitud, dirIni]`
- `cpBloques` — independent location counter per block
- `bloqueSimbolos` — maps each symbol to its block number

After Pass 1, `finalizarBloques()` computes block lengths and start addresses. Pass 2 uses `dirAbsoluta()` and `symAbsoluto()` to convert block-relative addresses to absolute addresses when generating code.

### Expression type system (A / R / EX)

`ResultadoExpr` (inner class of `AnalizadorSemantico`) carries:
- `netoR` — net count of relative terms (internal symbols)
- `externos` — list of `TermExterno` (EXTREF symbols with sign)
- `tipo`: `"A"` (netoR=0, no externals), `"R"` (netoR=1), `"RN"` (netoR=-1), `"EX"` (has externals), `"?"` (invalid)

Rules: `R + R` and `A - R` are illegal; `R - R` yields `A`; `R ± A` yields `R`. Relative terms cannot appear in `*` or `/`. External symbols contribute value=0 at assembly time; Pass 2 emits `M` records tagged `*SE` for each external term.

### Object file format (per section)

`GeneradorCodigo.escribirObj()` produces standard SIC/XE object records:
- `H` — header (section name, start address, length)
- `D` — define record (EXTDEF symbols and their addresses)
- `R` — refer record (EXTREF symbol names)
- `T` — text (up to 30 bytes of contiguous object code)
- `M` — modification record: `*R` for internal relocatable references (F4 instructions, relocatable `WORD`), `*SE` for external symbol references
- `E` — end (transfer address from `END` operand, only in the primary section)

### ANTLR4 generated sources

Maven auto-generates `SICXELexer.java`, `SICXEParser.java`, `SICXEBaseListener.java`, etc. from `SICXE.g4` during `mvn package`. The `compilar_final.sh` script uses `antlr-4.11.1-complete.jar` in `lib/` for the same purpose. Generated files land in `target/gen/` (script) or `target/generated-sources/antlr4/` (Maven).
