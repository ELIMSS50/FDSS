package sicxe;

/**
 * Representa una línea del listado intermedio SIC/XE.
 * El campo codigoObjeto se llena en el Paso 2.
 */
public class EntradaListado {

    // ── Paso 1 ────────────────────────────────────────────────────────
    public int    numeroLinea;
    public int    contloc;
    public String etiqueta;
    public String instruccion;
    public String operando;
    public int    formato;      // 1, 2, 3, 4 — 0 si directiva sin código
    public String modoDir;      // Simple / Inmediato / Indirecto / Indexado
    public String error;
    public int    noBloque;     // Número de bloque al que pertenece la línea

    // ── Paso 2 ────────────────────────────────────────────────────────
    public String codigoObjeto; // código objeto generado (hex, sin espacios)
    public String errorP2;      // error detectado en el paso 2

    // ── Tipo de expresión del operando (para WORD/F3/F4) ─────────────
    // "A" = Absoluto, "R" = Relativo, "" = no aplica / no evaluado
    public String tipoExpresion;

    public EntradaListado() {
        etiqueta       = "";
        instruccion    = "";
        operando       = "";
        modoDir        = "";
        error          = "";
        codigoObjeto   = "";
        errorP2        = "";
        tipoExpresion  = "";
    }
}