# Ensamblador SIC/XE

Ensamblador de dos pasadas para la arquitectura SIC/XE con interfaz gráfica. Soporta bloques de programa, secciones de control (CSECT), referencias externas (EXTREF/EXTDEF), expresiones, y un cargador-ligador integrado.

## Funcionalidades

- Ensamblado de dos pasadas (Pass 1 y Pass 2)
- Soporte de bloques de programa
- Secciones de control: `CSECT`, `EXTDEF`, `EXTREF`
- Generación de código objeto y registros de ligado (H, D, R, T, M, E)
- Cargador-ligador con algoritmo de dos pasadas
- Detección y reporte de errores semánticos
- Interfaz gráfica con editor de código fuente, tabla de símbolos, tabla de bloques y visor de código objeto

## Requisitos

- Java 21 o superior
- Apache Maven 3.8+ (solo si compilas desde fuente)

## Instalación de dependencias

### Java 21

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**Windows:**
Descargar el instalador desde https://adoptium.net y ejecutarlo.

**macOS:**
```bash
brew install openjdk@21
```

Verificar instalación:
```bash
java -version
```

### Apache Maven 3.8+

**Ubuntu/Debian:**
```bash
sudo apt install maven
```

**Windows:**
Descargar desde https://maven.apache.org/download.cgi, descomprimir y agregar `bin/` al PATH del sistema.

**macOS:**
```bash
brew install maven
```

Verificar instalación:
```bash
mvn -version
```

### ANTLR 4.11.1

No requiere instalación manual. Maven la descarga automáticamente al compilar gracias a la dependencia declarada en `pom.xml`.

---

## Ejecución (sin compilar)

Si ya tienes el JAR ejecutable:

```bash
java -jar target/p03-sicxe.jar
```

También puedes pasar un archivo `.asm` como argumento para abrirlo directamente:

```bash
java -jar target/p03-sicxe.jar ejemplos/BLOQUES1.asm
```

## Compilación desde fuente

```bash
mvn package
java -jar target/p03-sicxe.jar
```

O con el script incluido:

```bash
# Linux/macOS
./compilar_final.sh

# Windows
.\compilar_final.ps1
```

---

## Estructura del proyecto

```
p03-work/
├── src/
│   └── main/
│       ├── antlr4/          # Gramática SICXE.g4
│       └── java/sicxe/
│           ├── Main.java                # Punto de entrada
│           ├── InterfazGrafica.java     # Ventana principal
│           ├── AnalizadorSemantico.java # Visitor ANTLR, Pass 1
│           ├── GeneradorCodigo.java     # Pass 2, generación de objeto
│           ├── SeccionControl.java      # Manejo de CSECT
│           ├── CargadorLigador.java     # Cargador-ligador
│           ├── VentanaTabse.java        # Ventana del cargador
│           ├── VentanaResultados.java   # Visor de resultados
│           ├── EntradaListado.java      # Modelo de línea ensamblada
│           └── RecolectorErrores.java   # Manejo de errores ANTLR
├── ejemplos/                # Archivos .asm de prueba
├── lib/                     # ANTLR completo (herramienta)
├── target/
│   └── p03-sicxe.jar        # JAR ejecutable (fat jar)
└── pom.xml
```

## Archivos de ejemplo

La carpeta `ejemplos/` contiene programas `.asm` listos para probar:

| Archivo | Descripción |
|---|---|
| `BLOQUES1.asm` | Programa con bloques de programa |
| `ejemplo3secciones.asm` | Programa con tres secciones de control |
| `prog_ok.asm` | Programa sin errores |
| `prog_err.asm` | Programa con errores intencionales |
| `EJERCFINAL.asm` | Ejercicio final completo |

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Apache Maven | 3.8.7 | Construcción y dependencias |
| ANTLR | 4.11.1 | Parser/Lexer del lenguaje SIC/XE |
| Java Swing | (JDK) | Interfaz gráfica |
