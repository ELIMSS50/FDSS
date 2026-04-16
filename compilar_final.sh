#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
#  compilar.sh — Ensamblador SIC/XE  (Práctica 3)
#  Compatible con Linux, macOS y Windows (Git Bash / MSYS2)
#
#  Uso:  ./compilar.sh            → solo compila
#        ./compilar.sh run        → compila y ejecuta (pide archivo)
#        ./compilar.sh run <arch> → compila y ejecuta con archivo dado
# ═══════════════════════════════════════════════════════════════════

# ── Rutas del proyecto ───────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ANTLR_JAR="$SCRIPT_DIR/lib/antlr-4.11.1-complete.jar"
GRAMMAR="$SCRIPT_DIR/src/main/antlr4/sicxe/SICXE.g4"
SRC_JAVA="$SCRIPT_DIR/src/main/java"
GEN_DIR="$SCRIPT_DIR/target/gen"
CLASS_DIR="$SCRIPT_DIR/target/classes"
SOURCES_TMP="$SCRIPT_DIR/target/sources.txt"

# ── Detectar sistema operativo ───────────────────────────────────────
# En Windows con Git Bash, javac.exe necesita rutas estilo C:\... no /c/...
# cygpath -w convierte /c/Users/... → C:\Users\...
# En Linux/macOS simplemente devuelve la ruta sin cambios.
to_native() {
    if command -v cygpath &>/dev/null; then
        cygpath -w "$1"
    else
        echo "$1"
    fi
}

# El separador de classpath es ';' en Windows y ':' en Unix
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
    CP_SEP=";"
else
    CP_SEP=":"
fi

# ── Colores para la terminal ─────────────────────────────────────────
VERDE="\033[0;32m"
ROJO="\033[0;31m"
CYAN="\033[0;36m"
RESET="\033[0m"

ok()   { echo -e "${VERDE}✔ $*${RESET}"; }
err()  { echo -e "${ROJO}✘ $*${RESET}"; }
info() { echo -e "${CYAN}▶ $*${RESET}"; }

# ════════════════════════════════════════════════════════════════════
# 0. Verificar dependencias
# ════════════════════════════════════════════════════════════════════
if ! command -v java &>/dev/null; then
    err "Java no encontrado. Instala JDK 11 o superior."
    exit 1
fi

if [ ! -f "$ANTLR_JAR" ]; then
    err "No se encontró: $ANTLR_JAR"
    exit 1
fi

# ════════════════════════════════════════════════════════════════════
# 1. Crear directorios de salida
# ════════════════════════════════════════════════════════════════════
mkdir -p "$GEN_DIR" "$CLASS_DIR"

# ════════════════════════════════════════════════════════════════════
# 2. Generar clases desde la gramática ANTLR4
# ════════════════════════════════════════════════════════════════════
info "Generando parser desde SICXE.g4 ..."

java -jar "$(to_native "$ANTLR_JAR")" \
    -package sicxe \
    -o "$(to_native "$GEN_DIR")" \
    "$(to_native "$GRAMMAR")"

if [ $? -ne 0 ]; then
    err "Error al generar el parser ANTLR4."
    exit 1
fi
ok "Parser generado en $GEN_DIR"

# ════════════════════════════════════════════════════════════════════
# 3. Compilar todo el proyecto
# ════════════════════════════════════════════════════════════════════
info "Compilando fuentes Java ..."

# Recolectar .java y escribir cada ruta entre comillas (soporta espacios en el path)
> "$SOURCES_TMP"
while IFS= read -r f; do
    native="$(to_native "$f")"
    # El formato @argfile de javac acepta rutas entre comillas dobles
    echo "\"$native\"" >> "$SOURCES_TMP"
done < <(find "$SRC_JAVA/sicxe" "$GEN_DIR" -name "*.java")

javac \
    -encoding UTF-8 \
    -cp "$(to_native "$ANTLR_JAR")" \
    -d "$(to_native "$CLASS_DIR")" \
    @"$(to_native "$SOURCES_TMP")"

if [ $? -ne 0 ]; then
    err "Error de compilación. Revisa los mensajes anteriores."
    rm -f "$SOURCES_TMP"
    exit 1
fi

rm -f "$SOURCES_TMP"
ok "Compilación exitosa → $CLASS_DIR"

# ════════════════════════════════════════════════════════════════════
# 4. Ejecutar (solo si se pasó el argumento "run")
# ════════════════════════════════════════════════════════════════════
if [ "$1" == "run" ]; then
    echo ""
    info "Ejecutando ensamblador SIC/XE ..."
    echo ""
    java \
        -cp "$(to_native "$CLASS_DIR")${CP_SEP}$(to_native "$ANTLR_JAR")" \
        sicxe.Main "${@:2}"
fi
