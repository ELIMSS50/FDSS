package sicxe;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import javax.swing.SwingUtilities;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Ensamblador SIC/XE de dos pasos — Práctica 3 con Bloques de Programa.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        String ruta = resolverRuta(args);
        if (ruta == null) { System.out.println("No se seleccionó ningún archivo."); System.exit(1); }

        File archivo = new File(ruta);
        if (!archivo.exists()) { System.out.println("No existe: " + ruta); System.exit(1); }

        System.out.println("Archivo: " + archivo.getAbsolutePath());

        // ══════════════════════════════════════════════════════════════
        // PASO 1
        // ══════════════════════════════════════════════════════════════
        System.out.println("\n── Paso 1: Análisis ─────────────────────────────");

        CharStream flujo = CharStreams.fromFileName(ruta);
        SICXELexer lexer = new SICXELexer(flujo);
        RecolectorErrores recolector = new RecolectorErrores();
        lexer.removeErrorListeners();
        lexer.addErrorListener(recolector);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SICXEParser parser = new SICXEParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(recolector);
        SICXEParser.ProgramaContext arbol = parser.programa();

        AnalizadorSemantico semantico = new AnalizadorSemantico();
        new ParseTreeWalker().walk(semantico, arbol);

        List<String> errSintacticos = new ArrayList<>(recolector.getErrores());
        List<String> errSemanticos  = new ArrayList<>(semantico.getErrores());

        System.out.println("Tabla de Símbolos:");
        semantico.getTablaSimbolos().forEach((s, d) ->
            System.out.printf("  %-12s: %04X  blk=%d%n", s, d,
                semantico.getBloqueSimbolos().getOrDefault(s, 0)));
        System.out.printf("Longitud: %04Xh bytes%n", semantico.getLongitudPrograma());

        // Mostrar tabla de bloques
        System.out.println("Tabla de Bloques:");
        List<String> nombresBlq = semantico.getNombresBloques();
        for (int[] b : semantico.getTablaBloques()) {
            String nom = b[0] < nombresBlq.size() ? nombresBlq.get(b[0]) : "?";
            System.out.printf("  Bloque %d [%-15s]: longitud=%04X  dirIni=%04X%n",
                b[0], nom, b[2], b[3]);
        }

        if (!errSintacticos.isEmpty()) {
            System.out.println("Errores Sintácticos (" + errSintacticos.size() + "):");
            errSintacticos.forEach(e -> System.out.println("  " + e));
        }
        if (!errSemanticos.isEmpty()) {
            System.out.println("Errores Semánticos (" + errSemanticos.size() + "):");
            errSemanticos.forEach(e -> System.out.println("  " + e));
        }
        if (errSintacticos.isEmpty() && errSemanticos.isEmpty())
            System.out.println("✔ Paso 1 sin errores.");

        // ══════════════════════════════════════════════════════════════
        // PASO 2
        // ══════════════════════════════════════════════════════════════
        System.out.println("\n── Paso 2: Código objeto ────────────────────────");

        List<String> errP2 = new ArrayList<>();

        GeneradorCodigo generador = new GeneradorCodigo(
            semantico.getListado(),
            semantico.getTablaSimbolos(),
            semantico.getTipoSimbolos(),
            semantico.getBloqueSimbolos(),
            semantico.getValorBase(),
            semantico.getSimboloBase(),
            semantico.getTablaBloques(),
            semantico.getDireccionInicio()
        );
        generador.generar();
        errP2 = new ArrayList<>(generador.getErrores());

        if (!errP2.isEmpty()) {
            System.out.println("Errores en Paso 2 (" + errP2.size() + "):");
            errP2.forEach(e -> System.out.println("  " + e));
        }

        // ══════════════════════════════════════════════════════════════
        // ARCHIVOS DE SALIDA
        // ══════════════════════════════════════════════════════════════

        String nombreProg = archivo.getName().replaceFirst("\\.[^.]+$", "");
        for (var e : semantico.getListado())
            if (e.instruccion.equals("START") && !e.etiqueta.isBlank()) {
                nombreProg = e.etiqueta; break;
            }

        // .obj
        String rutaObj = cambiarExtension(ruta, ".obj");
        try {
            generador.escribirObj(rutaObj, nombreProg,
                semantico.getDireccionInicio(),
                semantico.getLongitudPrograma());
            System.out.println("\nCódigo objeto:    " + rutaObj);
        } catch (Exception ex) {
            System.out.println("Error al generar .obj: " + ex.getMessage());
        }

        // .txt
        String rutaTxt = cambiarExtension(ruta, ".txt");
        try (PrintWriter pw = new PrintWriter(rutaTxt)) {
            pw.println("=== Tabla de Símbolos ===");
            pw.printf("%-12s  %-8s  %-5s  %s%n", "Símbolo", "Valor", "Tipo", "Bloque");
            pw.println("-".repeat(40));
            semantico.getTablaSimbolos().forEach((s, d) -> {
                String tipo = semantico.getTipoSimbolos().getOrDefault(s, "R");
                int    blk  = semantico.getBloqueSimbolos().getOrDefault(s, 0);
                pw.printf("%-12s  %04X      %s     %d%n", s, d, tipo, blk);
            });
            pw.println();
            pw.println("=== Tabla de Bloques ===");
            pw.printf("%-6s  %-15s  %-8s  %s%n", "No.", "Nombre", "Longitud", "Dir.Ini");
            pw.println("-".repeat(45));
            for (int[] b : semantico.getTablaBloques()) {
                String nom = b[0] < nombresBlq.size() ? nombresBlq.get(b[0]) : "?";
                pw.printf("%-6d  %-15s  %04X      %04X%n", b[0], nom, b[2], b[3]);
            }
            pw.println();
            pw.printf("Longitud del programa: %04Xh bytes%n",
                semantico.getLongitudPrograma());
        }
        System.out.println("Tabla guardada:   " + rutaTxt);

        // .csv
        String rutaCsv = cambiarExtension(ruta, ".csv");
        semantico.generarCSV(rutaCsv);
        System.out.println("Listado CSV:      " + rutaCsv);

        // .err
        List<String> todosErrores = new ArrayList<>();
        todosErrores.addAll(errSintacticos);
        todosErrores.addAll(errSemanticos);
        todosErrores.addAll(errP2);

        if (!todosErrores.isEmpty()) {
            String rutaErr = cambiarExtension(ruta, ".err");
            List<String> lineasErr = new ArrayList<>();
            if (!errSintacticos.isEmpty()) {
                lineasErr.add("=== Errores Sintácticos (" + errSintacticos.size() + ") ===");
                lineasErr.addAll(errSintacticos);
                lineasErr.add("");
            }
            if (!errSemanticos.isEmpty()) {
                lineasErr.add("=== Errores Semánticos (" + errSemanticos.size() + ") ===");
                lineasErr.addAll(errSemanticos);
                lineasErr.add("");
            }
            if (!errP2.isEmpty()) {
                lineasErr.add("=== Errores Paso 2 (" + errP2.size() + ") ===");
                lineasErr.addAll(errP2);
            }
            Files.write(Paths.get(rutaErr), lineasErr);
            System.out.println("Errores:          " + rutaErr);
        }

        // ══════════════════════════════════════════════════════════════
        // VENTANA GRÁFICA
        // ══════════════════════════════════════════════════════════════
        final String       nomArch   = archivo.getName();
        final var          listado   = semantico.getListado();
        final var          simbolos  = semantico.getTablaSimbolos();
        final var          relocSym  = generador.getSimbolosRelocalizables();
        final int          longitud  = semantico.getLongitudPrograma();
        final List<String> registros = generador.getRegistrosObjeto();
        final List<String> esSint    = errSintacticos;
        final List<String> esSem     = errSemanticos;
        final List<String> esP2      = errP2;

        final var tipoSim    = semantico.getTipoSimbolos();
        final var bloqueSim  = semantico.getBloqueSimbolos();
        final var tabBloques = semantico.getTablaBloques();
        final var nomBloques = semantico.getNombresBloques();

        SwingUtilities.invokeLater(() ->
            new VentanaResultados(nomArch, listado, simbolos, tipoSim, bloqueSim,
                                  relocSym, esSint, esSem, esP2, longitud,
                                  registros, tabBloques, nomBloques)
        );
    }

    static String cambiarExtension(String ruta, String ext) {
        int p = ruta.lastIndexOf('.');
        return (p >= 0 ? ruta.substring(0, p) : ruta) + ext;
    }

    static String resolverRuta(String[] args) throws Exception {
        if (args != null && args.length > 0) {
            File f = new File(args[0]);
            if (f.exists()) return f.getAbsolutePath();
        }

        List<String> archivos = new ArrayList<>();
        for (String dir : directoriosCandidatos()) {
            File carpeta = new File(dir);
            if (!carpeta.exists()) continue;
            File[] found = carpeta.listFiles(f -> f.getName().endsWith(".asm"));
            if (found != null)
                Arrays.stream(found).map(File::getAbsolutePath).forEach(archivos::add);
        }
        archivos = archivos.stream().distinct().sorted().collect(Collectors.toList());

        if (archivos.size() == 1) return archivos.get(0);
        if (archivos.size() > 1) {
            System.out.println("Archivos .asm encontrados:");
            for (int i = 0; i < archivos.size(); i++)
                System.out.printf("  %d. %s%n", i + 1, archivos.get(i));
            System.out.print("Elige número (ENTER para cancelar): ");
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String l = sc.nextLine().trim();
                if (l.isEmpty()) return null;
                try {
                    int n = Integer.parseInt(l);
                    if (n >= 1 && n <= archivos.size()) return archivos.get(n - 1);
                } catch (NumberFormatException ignored) {}
                File f = new File(l.replace("\"", ""));
                if (f.exists()) return f.getAbsolutePath();
                System.out.print("No válido. Intenta de nuevo: ");
            }
            return null;
        }

        System.out.print("Ruta del .asm (ENTER para cancelar): ");
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String l = sc.nextLine().trim().replace("\"", "");
            if (l.isEmpty()) return null;
            File f = new File(l);
            if (f.exists()) return f.getAbsolutePath();
        }
        return null;
    }

    static List<String> directoriosCandidatos() {
        String base = System.getProperty("user.dir");
        return Arrays.asList(
            base + File.separator + "ejemplos",
            base,
            base + File.separator + ".." + File.separator + "ejemplos"
        );
    }
}