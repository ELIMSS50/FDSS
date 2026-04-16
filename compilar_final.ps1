# ═══════════════════════════════════════════════════════════════════
#  compilar_final.ps1 — Ensamblador SIC/XE (Práctica 3)
#  Compatible con Windows PowerShell 5.1+
#
#  Uso:  .\compilar_final.ps1            → solo compila
#        .\compilar_final.ps1 -run       → compila y ejecuta
#        .\compilar_final.ps1 -run archivo.asm  → compila y ejecuta con archivo
# ═══════════════════════════════════════════════════════════════════

param(
    [string]$mode = "compile",
    [string]$archivo = ""
)

$SCRIPT_DIR = Get-Location
$ANTLR_JAR = Join-Path $SCRIPT_DIR "lib\antlr-4.11.1-complete.jar"
$GRAMMAR = Join-Path $SCRIPT_DIR "src\main\antlr4\sicxe\SICXE.g4"
$SRC_JAVA = Join-Path $SCRIPT_DIR "src\main\java"
$GEN_DIR = Join-Path $SCRIPT_DIR "target\gen"
$CLASS_DIR = Join-Path $SCRIPT_DIR "target\classes"

function Info { Write-Host ">> $args" -ForegroundColor Cyan }
function Ok { Write-Host "[OK] $args" -ForegroundColor Green }
function Err { Write-Host "[ERROR] $args" -ForegroundColor Red }

# Verificar dependencias
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Err "Java no encontrado. Instala JDK 21 o superior."
    exit 1
}

if (-not (Test-Path $ANTLR_JAR)) {
    Err "No se encontró: $ANTLR_JAR"
    exit 1
}

# Crear directorios
$null = New-Item -ItemType Directory -Path $GEN_DIR -Force
$null = New-Item -ItemType Directory -Path $CLASS_DIR -Force

# 1. Generar clases desde ANTLR4
Info "Generando parser desde SICXE.g4 ..."
$output = java -jar $ANTLR_JAR -package sicxe -o $GEN_DIR $GRAMMAR 2>&1
if ($LASTEXITCODE -ne 0) {
    Err "Error al generar el parser ANTLR4."
    Write-Host $output
    exit 1
}
Ok "Parser generado en $GEN_DIR"

# 2. Compilar fuentes Java
Info "Compilando fuentes Java ..."
$sourceFiles = @()
$sourceFiles += Get-ChildItem -Recurse -Path "$SRC_JAVA\sicxe" -Include "*.java" | ForEach-Object { $_.FullName }
$sourceFiles += Get-ChildItem -Recurse -Path $GEN_DIR -Include "*.java" | ForEach-Object { $_.FullName }

$output = javac -encoding UTF-8 -cp $ANTLR_JAR -d $CLASS_DIR $sourceFiles 2>&1
if ($LASTEXITCODE -ne 0) {
    Err "Error de compilación."
    Write-Host $output
    exit 1
}

Ok "Compilación exitosa → $CLASS_DIR"

# 3. Ejecutar (si se pasó -run)
if ($mode -eq "-run" -or $mode -eq "run") {
    Write-Host ""
    Info "Ejecutando ensamblador SIC/XE ..."
    Write-Host ""
    
    if ($archivo) {
        java -cp "$CLASS_DIR;$ANTLR_JAR" sicxe.Main "$archivo"
    } else {
        java -cp "$CLASS_DIR;$ANTLR_JAR" sicxe.Main
    }
    exit $LASTEXITCODE
}

Ok "Listo para ejecutar: .\compilar_final.ps1 -run"
