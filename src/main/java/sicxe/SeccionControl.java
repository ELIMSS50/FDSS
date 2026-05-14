package sicxe;

import java.util.*;

/**
 * Encapsula todo el estado independiente de una sección de control.
 * Cada CSECT (o la sección principal START) tiene su propia instancia.
 */
public class SeccionControl {

    // ── Identidad ─────────────────────────────────────────────────────
    public String  nombre;
    public boolean esPrincipal;     // true = sección del START
    public int     direccionInicio; // 0 para CSECTs, valor del START para la principal

    // ── Tabla de símbolos (Pass 1) ────────────────────────────────────
    public Map<String, Integer> tablaSimbolos  = new LinkedHashMap<>();
    public Map<String, String>  tipoSimbolos   = new LinkedHashMap<>(); // A, R, E
    public Map<String, Integer> bloqueSimbolos = new LinkedHashMap<>();

    // ── Referencias externas ──────────────────────────────────────────
    // EXTREF: símbolos usados aquí, definidos en otro módulo
    public Set<String>  simbolosExternos   = new LinkedHashSet<>();
    // EXTDEF: símbolos definidos aquí, exportados a otros módulos (en orden de aparición)
    public List<String> simbolosExportados = new ArrayList<>();

    // Registro ordenado de directivas EXTDEF/EXTREF tal como aparecen en fuente
    // (necesario para generar D/R en el orden correcto)
    public List<DirectivaExt> directivasExt = new ArrayList<>();

    // ── Listado intermedio y errores ──────────────────────────────────
    public List<EntradaListado> listado = new ArrayList<>();
    public List<String>         errores = new ArrayList<>();

    // ── Tabla de bloques internos (USE) ───────────────────────────────
    public List<int[]>           tablaBloques   = new ArrayList<>();
    public List<String>          nombresBloques = new ArrayList<>();
    public Map<String, Integer>  nombreABloque  = new LinkedHashMap<>();
    public Map<Integer, Integer> cpBloques      = new LinkedHashMap<>();

    // ── Estado mutable del ensamblador (Pass 1) ───────────────────────
    public int    bloqueActual   = 0;
    public int    contadorBloque = 0;
    public int    contloc        = 0;
    public int    valorBase      = 0;
    public String simboloBase    = "";

    // ── Pass 2 ────────────────────────────────────────────────────────
    public List<String> registrosObjeto = new ArrayList<>();
    public int          longitudTotal   = 0;
    public int          dirEntrada      = 0;

    // ─────────────────────────────────────────────────────────────────
    public SeccionControl(String nombre, boolean esPrincipal, int direccionInicio) {
        this.nombre          = nombre;
        this.esPrincipal     = esPrincipal;
        this.direccionInicio = direccionInicio;
        this.contloc         = esPrincipal ? direccionInicio : 0;
    }

    /** Calcula la longitud total de la sección sumando todos sus bloques. */
    public int calcularLongitud() {
        if (tablaBloques.isEmpty()) return 0;
        int total = 0;
        for (int[] b : tablaBloques) total += b[2];
        return total;
    }

    // ─────────────────────────────────────────────────────────────────
    /** Representa una directiva EXTDEF o EXTREF con sus símbolos. */
    public static class DirectivaExt {
        public final boolean    esExtdef; // true=EXTDEF, false=EXTREF
        public final List<String> simbolos;

        public DirectivaExt(boolean esExtdef, List<String> simbolos) {
            this.esExtdef = esExtdef;
            this.simbolos = new ArrayList<>(simbolos);
        }
    }
}
