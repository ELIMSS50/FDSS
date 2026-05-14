package sicxe;

import org.antlr.v4.runtime.tree.*;
import java.io.*;
import java.util.*;

/**
 * Analizador semántico — Práctica con Secciones de Control.
 *
 * Paso 1 con soporte de:
 *  - Secciones de control (CSECT) — tabla de símbolos, bloques y CP independientes
 *  - EXTDEF / EXTREF
 *  - Bloques de programa (USE) dentro de cada sección
 *  - EQU, ORG, expresiones, tipos A/R/E
 *  - Símbolo externo en expresión: valor=0, genera TermExterno para Pass 2
 */
public class AnalizadorSemantico extends SICXEBaseListener {

    // ── Clases de datos compartidas ───────────────────────────────────

    /** Término externo (EXTREF) hallado en una expresión con su signo. */
    public static class TermExterno {
        public final String nombre;
        public final int    signo; // +1 o -1
        public TermExterno(String nombre, int signo) {
            this.nombre = nombre; this.signo = signo;
        }
    }

    static class ResultadoExpr {
        final int    valor;
        final String tipo;   // "A", "R", "RN", "EX" (con externos), "?"
        final int    netoR;  // conteo neto de relativos internos con signo
        final List<TermExterno> externos;

        ResultadoExpr(int v, String t) {
            valor = v; tipo = t; netoR = tipoANeto(t);
            externos = new ArrayList<>();
        }
        ResultadoExpr(int v, int neto) {
            valor = v; netoR = neto;
            tipo = neto == 0 ? "A" : neto == 1 ? "R" : neto == -1 ? "RN" : "?";
            externos = new ArrayList<>();
        }
        ResultadoExpr(int v, int neto, List<TermExterno> ext) {
            valor = v; netoR = neto;
            externos = ext != null ? new ArrayList<>(ext) : new ArrayList<>();
            tipo = !externos.isEmpty() ? "EX"
                 : neto == 0 ? "A" : neto == 1 ? "R" : neto == -1 ? "RN" : "?";
        }
        private static int tipoANeto(String t) {
            return t.equals("R") ? 1 : t.equals("RN") ? -1 : 0;
        }
        boolean tieneExternos() { return !externos.isEmpty(); }
    }

    // ── Secciones de control ──────────────────────────────────────────
    private final List<SeccionControl> secciones = new ArrayList<>();
    private       SeccionControl       seccionActual;
    private boolean enCabeceraSeccion = true; // validar posición de EXTDEF/EXTREF

    // ── Alias que siempre apuntan a los datos de seccionActual ────────
    // Colecciones: se reasignan en activarSeccion()
    private Map<String, Integer> tablaSimbolos;
    private Map<String, String>  tipoSimbolos;
    private Map<String, Integer> bloqueSimbolos;
    private List<String>         errores;
    private List<EntradaListado> listado;
    private List<int[]>          tablaBloques;
    private List<String>         nombresBloques;
    private Map<String, Integer> nombreABloque;
    private Map<Integer, Integer> cpBloques;

    // Primitivas: se sincronizan mediante guardarEstado() / activarSeccion()
    private int    bloqueActual   = 0;
    private int    contadorBloque = 0;
    private int    contloc        = 0;
    private int    direccionIni   = 0;
    private int    valorBase      = 0;
    private String simboloBase    = "";

    // Global (no pertenece a ninguna sección)
    private int numeroLinea = 1;

    // ── Bytes por instrucción ─────────────────────────────────────────
    private static final Map<String, Integer> BYTES = new HashMap<>();
    static {
        for (String op : new String[]{"FIX","FLOAT","HIO","NORM","SIO","TIO"})
            BYTES.put(op, 1);
        for (String op : new String[]{"ADDR","COMPR","DIVR","MULR","RMO","SUBR",
                                       "SHIFTL","SHIFTR","SVC","CLEAR","TIXR"})
            BYTES.put(op, 2);
        for (String op : new String[]{"ADD","ADDF","AND","COMP","COMPF","DIV","DIVF",
                "J","JEQ","JGT","JLT","JSUB","LDA","LDB","LDCH","LDF","LDL","LDS",
                "LDT","LDX","LPS","MUL","MULF","OR","RD","RSUB","SSK","STA","STB",
                "STCH","STF","STI","STL","STS","STSW","STT","STX","SUB","SUBF",
                "TD","TIX","WD"})
            BYTES.put(op, 3);
    }

    // ═════════════════════════════════════════════════════════════════
    // GESTIÓN DE SECCIONES
    // ═════════════════════════════════════════════════════════════════

    /** Guarda el estado mutable (primitivas) de vuelta en la sección activa. */
    private void guardarEstado() {
        if (seccionActual == null) return;
        seccionActual.bloqueActual   = bloqueActual;
        seccionActual.contadorBloque = contadorBloque;
        seccionActual.contloc        = contloc;
        seccionActual.valorBase      = valorBase;
        seccionActual.simboloBase    = simboloBase;
    }

    /** Activa una sección: redirige todos los alias a sus colecciones. */
    private void activarSeccion(SeccionControl sec) {
        guardarEstado();
        seccionActual  = sec;
        tablaSimbolos  = sec.tablaSimbolos;
        tipoSimbolos   = sec.tipoSimbolos;
        bloqueSimbolos = sec.bloqueSimbolos;
        errores        = sec.errores;
        listado        = sec.listado;
        tablaBloques   = sec.tablaBloques;
        nombresBloques = sec.nombresBloques;
        nombreABloque  = sec.nombreABloque;
        cpBloques      = sec.cpBloques;
        bloqueActual   = sec.bloqueActual;
        contadorBloque = sec.contadorBloque;
        contloc        = sec.contloc;
        direccionIni   = sec.direccionInicio;
        valorBase      = sec.valorBase;
        simboloBase    = sec.simboloBase;
    }

    /** Inicializa el bloque por omisión de la sección activa (bloque 0). */
    private void inicializarBloqueDefault() {
        bloqueActual = 0;
        contadorBloque = 0;
        nombreABloque.put("(por omisión)", 0);
        cpBloques.put(0, contloc);
        tablaBloques.add(new int[]{0, 0, 0, 0});
        nombresBloques.add("(por omisión)");
    }

    // ═════════════════════════════════════════════════════════════════
    // GETTERS PÚBLICOS
    // ═════════════════════════════════════════════════════════════════
    public List<SeccionControl> getSecciones()       { return secciones; }

    // Getters que agregan datos de todas las secciones (para CSV y compatibilidad)
    public List<EntradaListado> getListado() {
        List<EntradaListado> all = new ArrayList<>();
        for (SeccionControl s : secciones) all.addAll(s.listado);
        return all;
    }
    public List<String> getErrores() {
        List<String> all = new ArrayList<>();
        for (SeccionControl s : secciones) all.addAll(s.errores);
        return all;
    }

    // Getters de la sección principal (primera)
    public Map<String, Integer> getTablaSimbolos()  {
        return secciones.isEmpty() ? new LinkedHashMap<>() : secciones.get(0).tablaSimbolos;
    }
    public Map<String, String>  getTipoSimbolos()   {
        return secciones.isEmpty() ? new LinkedHashMap<>() : secciones.get(0).tipoSimbolos;
    }
    public Map<String, Integer> getBloqueSimbolos() {
        return secciones.isEmpty() ? new LinkedHashMap<>() : secciones.get(0).bloqueSimbolos;
    }
    public int getValorBase()      { return secciones.isEmpty() ? 0 : secciones.get(0).valorBase; }
    public String getSimboloBase() { return secciones.isEmpty() ? "" : secciones.get(0).simboloBase; }
    public int getDireccionInicio(){ return secciones.isEmpty() ? 0 : secciones.get(0).direccionInicio; }
    public List<int[]> getTablaBloques()   {
        return secciones.isEmpty() ? new ArrayList<>() : secciones.get(0).tablaBloques;
    }
    public List<String> getNombresBloques(){
        return secciones.isEmpty() ? new ArrayList<>() : secciones.get(0).nombresBloques;
    }

    public int getLongitudPrograma() {
        int total = 0;
        for (SeccionControl s : secciones) total += s.calcularLongitud();
        return total;
    }

    // ═════════════════════════════════════════════════════════════════
    // ENCABEZADO (START) — crea la sección principal
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void enterEncabezado(SICXEParser.EncabezadoContext ctx) {
        try {
            String num   = ctx.numeral().getText().toUpperCase();
            int    dirIni = parsearNumero(num);
            String etiq  = ctx.etiqueta() != null ? ctx.etiqueta().ID().getText() : "";

            SeccionControl principal = new SeccionControl(
                etiq.isBlank() ? "PROG" : etiq, true, dirIni);
            secciones.add(principal);
            activarSeccion(principal);
            inicializarBloqueDefault();
            cpBloques.put(0, contloc);
            enCabeceraSeccion = true;

            EntradaListado e = construir(numeroLinea++, contloc,
                etiq.isBlank() ? null : etiq, "START", num, 0, "", "");
            e.noBloque = 0;
            e.nombreSeccion = seccionActual.nombre;
            listado.add(e);

        } catch (Exception ex) {
            // sección mínima de emergencia
            SeccionControl fallback = new SeccionControl("PROG", true, 0);
            secciones.add(fallback);
            activarSeccion(fallback);
            inicializarBloqueDefault();
            errores.add("x Error en START: " + ex.getMessage());
            numeroLinea++;
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // LÍNEA NORMAL
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void enterLinea(SICXEParser.LineaContext ctx) {
        try {
            String etiq       = ctx.etiqueta() != null ? ctx.etiqueta().ID().getText() : null;
            int    locActual  = contloc;

            String[] res;
            if (ctx.enunciado().instruccion() != null) {
                enCabeceraSeccion = false; // instrucciones terminan la cabecera
                res = procesarInstruccion(ctx.enunciado().instruccion());
            } else if (ctx.enunciado().directiva() != null) {
                res = procesarDirectiva(ctx.enunciado().directiva(), etiq, locActual);
            } else {
                enCabeceraSeccion = false;
                res = new String[]{"", "", "0", "", "Error: Línea no reconocida", "0"};
            }

            String errorInstr = res[4];
            int    delta      = Integer.parseInt(res[5]);

            boolean esEQU  = res[0].equals("EQU");
            boolean esUSE  = res[0].equals("USE");
            boolean esCsect = res[0].equals("CSECT");
            boolean esExt   = res[0].equals("EXTDEF") || res[0].equals("EXTREF");

            int bloqueParaLinea = bloqueActual;
            int locParaLinea    = locActual;
            if (esUSE) {
                bloqueParaLinea = bloqueActual;
                locParaLinea    = contloc;
            }

            String errorEtiqueta = "";
            if (etiq != null && !etiq.isBlank() && !esEQU && !esUSE && !esCsect && !esExt) {
                if (tablaSimbolos.containsKey(etiq)) {
                    errorEtiqueta = "Símbolo duplicado '" + etiq + "'";
                } else {
                    tablaSimbolos.put(etiq, locActual);
                    tipoSimbolos.put(etiq, "R");
                    bloqueSimbolos.put(etiq, bloqueParaLinea);
                }
            }

            String errorFinal = combinar(errorInstr, errorEtiqueta);

            if (!esUSE && !esCsect) {
                if (delta < 0) {
                    contloc = -(delta + 1);
                } else {
                    contloc += delta;
                }
                cpBloques.put(bloqueActual, contloc);
            }

            // El listado de la línea CSECT ya fue agregado dentro de procesarDirectiva
            // (a la nueva sección). No lo agregamos de nuevo aquí.
            if (!esCsect) {
                EntradaListado entrada = construir(
                    numeroLinea++, locParaLinea, etiq,
                    res[0], res[1],
                    Integer.parseInt(res[2]),
                    res[3], errorFinal
                );
                entrada.noBloque = bloqueParaLinea;
                entrada.nombreSeccion = seccionActual.nombre;
                if (res.length > 6 && res[6] != null) entrada.tipoExpresion = res[6];
                listado.add(entrada);

                if (!errorFinal.isBlank())
                    errores.add(String.format("x Error en línea %d [%s]: %s",
                        numeroLinea - 1,
                        res[0].isBlank() ? "?" : res[0],
                        errorFinal));
            } else {
                // CSECT: el número de línea ya fue consumido dentro de procesarDirectiva
            }

        } catch (Exception ex) {
            errores.add("x Error inesperado en línea " + numeroLinea + ": " + ex.getMessage());
            numeroLinea++;
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // LÍNEA CON ERROR SINTÁCTICO
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void enterLineaError(SICXEParser.LineaErrorContext ctx) {
        try {
            java.util.List<SICXEParser.TokensErrorContext> toksList = ctx.tokensError();
            java.util.List<String> tokTexts = new java.util.ArrayList<>();
            for (SICXEParser.TokensErrorContext tok : toksList)
                tokTexts.add(tok.getText());

            String etiq = "", instruccion = "", operando = "";

            if (tokTexts.size() >= 2 && esOpcodeConocido(tokTexts.get(1))) {
                etiq        = tokTexts.get(0);
                instruccion = tokTexts.get(1);
                operando    = String.join(" ", tokTexts.subList(2, tokTexts.size()));
            } else if (!tokTexts.isEmpty()) {
                instruccion = tokTexts.get(0);
                operando    = String.join(" ", tokTexts.subList(1, tokTexts.size()));
            }

            if (!etiq.isBlank() && !tablaSimbolos.containsKey(etiq)) {
                tablaSimbolos.put(etiq, contloc);
                tipoSimbolos.put(etiq, "R");
                bloqueSimbolos.put(etiq, bloqueActual);
            }

            boolean primerEsOpcode = esOpcodeConocido(instruccion);
            String error = primerEsOpcode
                ? "Error de sintaxis: instrucción '" + instruccion
                  + (operando.isBlank() ? "' con operando inválido" : "' con operando inválido '" + operando + "'")
                : "Error: Instrucción no existe '" + instruccion + "'"
                  + (operando.isBlank() ? "" : " (operando: '" + operando + "')");

            EntradaListado e = construir(numeroLinea++, contloc, etiq,
                instruccion, operando, 0, "", error);
            e.noBloque = bloqueActual;
            e.nombreSeccion = seccionActual.nombre;
            listado.add(e);

            errores.add(String.format("x Error en línea %d [%s]: %s",
                numeroLinea - 1, instruccion.isBlank() ? "?" : instruccion, error));

        } catch (Exception ex) {
            errores.add("x Error inesperado en línea " + numeroLinea + ": " + ex.getMessage());
            numeroLinea++;
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // CIERRE (END) — finaliza la última sección activa
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void enterCierre(SICXEParser.CierreContext ctx) {
        cpBloques.put(bloqueActual, contloc);
        guardarEstado();

        // El contloc y nombre de sección del END pertenecen a la sección PRINCIPAL,
        // aunque la instrucción se queda en la posición del listado donde apareció.
        SeccionControl secPrincipal = secciones.stream()
            .filter(s -> s.esPrincipal).findFirst().orElse(seccionActual);
        int cpBloque0 = secPrincipal.cpBloques.getOrDefault(0, secPrincipal.contloc);

        String etiq    = ctx.etiqueta() != null ? ctx.etiqueta().ID().getText() : null;
        String operand = ctx.destino()  != null ? ctx.destino().expresion().getText() : "";
        EntradaListado e = construir(numeroLinea++, cpBloque0, etiq, "END", operand, 0, "", "");
        e.noBloque = 0;
        e.nombreSeccion = secPrincipal.nombre;
        listado.add(e);   // se queda en el listado de la sección activa (posición fuente)

        finalizarBloques();

        // Finalizar todas las secciones anteriores que aún no se finalizaron
        // (ya fueron finalizadas en procesarDirectiva CSECT, pero por si acaso)
    }

    /** Calcula longitudes y direcciones de inicio de cada bloque de la sección activa. */
    private void finalizarBloques() {
        for (int[] b : tablaBloques) {
            int num = b[0];
            int cp  = cpBloques.getOrDefault(num, 0);
            b[2] = (num == 0) ? (cp - direccionIni) : cp;
        }
        tablaBloques.get(0)[3] = 0;
        for (int i = 1; i < tablaBloques.size(); i++)
            tablaBloques.get(i)[3] = tablaBloques.get(i-1)[3] + tablaBloques.get(i-1)[2];

        // Actualizar longitud en la SeccionControl
        seccionActual.longitudTotal = seccionActual.calcularLongitud();
    }

    // ═════════════════════════════════════════════════════════════════
    // PROCESAR INSTRUCCIÓN
    // ═════════════════════════════════════════════════════════════════
    private String[] procesarInstruccion(SICXEParser.InstruccionContext ctx) {
        enCabeceraSeccion = false;
        String nombre = "", operand = "", modoDir = "", error = "", tipoExp = "";
        int formato = 0, delta = 0;

        if (ctx.MAS() != null && ctx.OPCODE() != null) {
            nombre  = "+" + ctx.OPCODE().getText();
            formato = 4;
            if (ctx.modoAcceso() != null) {
                operand = ctx.modoAcceso().getText();
                modoDir = detectarModo(operand);
                tipoExp = evaluarTipoModoAcceso(ctx.modoAcceso());
                delta   = 4;
            } else {
                error = "Error semántico: Formato 4 (+" + ctx.OPCODE().getText()
                      + ") requiere operando — no se incrementa CONTLOC";
            }

        } else if (ctx.OPCODE_F2_RR() != null) {
            nombre  = ctx.OPCODE_F2_RR().getText();
            formato = 2;
            if (ctx.reg().size() == 2) {
                operand = ctx.reg(0).getText() + "," + ctx.reg(1).getText(); delta = 2;
            } else {
                operand = ctx.getText().replace(nombre, "").trim();
                error   = "Error semántico: " + nombre + " requiere dos registros";
            }

        } else if (ctx.OPCODE_F2_RN() != null) {
            nombre  = ctx.OPCODE_F2_RN().getText();
            formato = 2;
            if (!ctx.reg().isEmpty() && ctx.numeral() != null) {
                operand = ctx.reg(0).getText() + "," + ctx.numeral().getText(); delta = 2;
            } else {
                operand = ctx.getText().replace(nombre, "").trim();
                error   = "Error semántico: " + nombre + " requiere registro y número";
            }

        } else if (ctx.OPCODE_F2_R() != null) {
            nombre  = ctx.OPCODE_F2_R().getText();
            formato = 2;
            if (!ctx.reg().isEmpty()) {
                operand = ctx.reg(0).getText(); delta = 2;
            } else {
                operand = ctx.getText().replace(nombre, "").trim();
                error   = "Error semántico: " + nombre + " requiere un registro";
            }

        } else if (ctx.OPCODE() != null && ctx.modoAcceso() != null) {
            nombre  = ctx.OPCODE().getText();
            formato = 3; delta = 3;
            operand = ctx.modoAcceso().getText();
            modoDir = detectarModo(operand);
            tipoExp = evaluarTipoModoAcceso(ctx.modoAcceso());

        } else if (ctx.KRSUB() != null) {
            nombre = "RSUB"; formato = 3; delta = 3;

        } else if (ctx.OPCODE_F1() != null) {
            nombre = ctx.OPCODE_F1().getText(); formato = 1; delta = 1;

        } else if (ctx.OPCODE() != null) {
            nombre  = ctx.OPCODE().getText();
            formato = 3;
            error   = "Error semántico: " + nombre + " requiere un operando";

        } else {
            nombre = ctx.getText().trim();
            error  = "Error: Instrucción no definida '" + nombre + "'";
        }

        return new String[]{nombre, operand, String.valueOf(formato), modoDir,
                            error, String.valueOf(delta), tipoExp};
    }

    private String evaluarTipoModoAcceso(SICXEParser.ModoAccesoContext ctx) {
        SICXEParser.ExpresionContext exprCtx = ctx.expresion();
        if (exprCtx == null) return "";
        try {
            ResultadoExpr r = evaluarExpresion(exprCtx, false);
            return r.tieneExternos() ? "EX" : r.tipo;
        } catch (Exception e) { return ""; }
    }

    private String detectarModo(String op) {
        if (op == null || op.isBlank()) return "";
        if (op.startsWith("#"))              return "Inmediato";
        if (op.startsWith("@"))              return "Indirecto";
        if (op.toUpperCase().endsWith(",X")) return "Indexado";
        return "Simple";
    }

    // ═════════════════════════════════════════════════════════════════
    // PROCESAR DIRECTIVA
    // ═════════════════════════════════════════════════════════════════
    private String[] procesarDirectiva(SICXEParser.DirectivaContext ctx,
                                        String etiq, int locActual) {
        String nombre  = ctx.getChild(0).getText().toUpperCase();
        String operand = "", error = "", tipoExp = "";
        int delta = 0;

        switch (nombre) {

            // ── Directivas existentes (sin cambio) ────────────────────
            case "BYTE" -> {
                if (ctx.contenidoByte() != null) {
                    if (ctx.contenidoByte().LITC() != null) {
                        operand = ctx.contenidoByte().LITC().getText();
                        delta   = operand.substring(2, operand.length()-1).length();
                    } else if (ctx.contenidoByte().LITX() != null) {
                        operand = ctx.contenidoByte().LITX().getText();
                        String hex = operand.substring(2, operand.length()-1);
                        if (!esHexValido(hex)) { error = "Error: Hex inválido en BYTE X'" + hex + "'"; }
                        else delta = (hex.length() + 1) / 2;
                    }
                } else { error = "Error de sintaxis: BYTE requiere C'texto' o X'hex'"; }
            }
            case "WORD" -> {
                operand = ctx.expresion() != null ? ctx.expresion().getText() : "";
                enCabeceraSeccion = false;
                if (ctx.expresion() != null) {
                    try {
                        ResultadoExpr r = evaluarExpresion(ctx.expresion(), false);
                        tipoExp = r.tieneExternos() ? "EX" : r.tipo;
                    } catch (Exception ex) { tipoExp = ""; }
                }
                delta = 3;
            }
            case "RESB" -> {
                enCabeceraSeccion = false;
                operand = ctx.expresion() != null ? ctx.expresion().getText() : "";
                try {
                    ResultadoExpr r = evaluarExpresion(ctx.expresion(), true);
                    if (r.tieneExternos()) { error = "Error: RESB no permite símbolos externos"; delta = 0; }
                    else if (!r.tipo.equals("A")) { error = "Error: RESB requiere expresión absoluta"; delta = 0; }
                    else if (r.valor <= 0) { error = "Error: RESB requiere valor positivo (recibido: " + r.valor + ")"; delta = 0; }
                    else delta = r.valor;
                } catch (Exception ex) { error = "Error: Operando inválido en RESB: " + ex.getMessage(); }
            }
            case "RESW" -> {
                enCabeceraSeccion = false;
                operand = ctx.expresion() != null ? ctx.expresion().getText() : "";
                try {
                    ResultadoExpr r = evaluarExpresion(ctx.expresion(), true);
                    if (r.tieneExternos()) { error = "Error: RESW no permite símbolos externos"; delta = 0; }
                    else if (!r.tipo.equals("A")) { error = "Error: RESW requiere expresión absoluta"; delta = 0; }
                    else if (r.valor <= 0) { error = "Error: RESW requiere valor positivo (recibido: " + r.valor + ")"; delta = 0; }
                    else delta = 3 * r.valor;
                } catch (Exception ex) { error = "Error: Operando inválido en RESW: " + ex.getMessage(); }
            }
            case "BASE" -> {
                if (ctx.ID() != null) { simboloBase = ctx.ID().getText(); operand = simboloBase; }
                else if (ctx.numeral() != null) {
                    operand = ctx.numeral().getText();
                    try { valorBase = parsearNumero(operand); }
                    catch (Exception ex) { error = "Error: Valor no numérico en BASE '" + operand + "'"; }
                } else { error = "Error de sintaxis: BASE requiere símbolo o número"; }
            }
            case "EQU" -> {
                if (etiq == null || etiq.isBlank()) { error = "Error: EQU requiere una etiqueta"; break; }
                boolean esAsterisco = (ctx.MUL() != null && ctx.expresion() == null);
                if (esAsterisco) {
                    operand = "*";
                    if (tablaSimbolos.containsKey(etiq)) { error = "Símbolo duplicado '" + etiq + "'"; }
                    else {
                        tablaSimbolos.put(etiq, locActual);
                        tipoSimbolos.put(etiq, "R");
                        bloqueSimbolos.put(etiq, bloqueActual);
                        tipoExp = "R";
                    }
                } else if (ctx.expresion() != null) {
                    operand = ctx.expresion().getText();
                    try {
                        ResultadoExpr r = evaluarExpresionEQU(ctx.expresion());
                        tipoExp = r.tipo;
                        if (tablaSimbolos.containsKey(etiq)) { error = "Símbolo duplicado '" + etiq + "'"; }
                        else {
                            tablaSimbolos.put(etiq, r.valor);
                            tipoSimbolos.put(etiq, r.tipo);
                            bloqueSimbolos.put(etiq, bloqueActual);
                        }
                    } catch (Exception ex) {
                        error = "Error en EQU '" + operand + "': " + ex.getMessage();
                        if (!tablaSimbolos.containsKey(etiq)) {
                            tablaSimbolos.put(etiq, 0xffffff);
                            tipoSimbolos.put(etiq, "A");
                            bloqueSimbolos.put(etiq, bloqueActual);
                        }
                    }
                } else { error = "Error de sintaxis en EQU"; }
            }
            case "ORG" -> {
                enCabeceraSeccion = false;
                if (ctx.expresion() == null) { error = "Error: ORG requiere un valor"; break; }
                operand = ctx.expresion().getText();
                try { int nuevoCP = parsearNumero(operand); delta = -(nuevoCP + 1); }
                catch (Exception ex) { error = "Error: ORG solo acepta número literal. Valor: '" + operand + "'"; }
            }
            case "USE" -> {
                cpBloques.put(bloqueActual, contloc);
                String nomBlq;
                if (ctx.ID() != null) { nomBlq = ctx.ID().getText(); operand = nomBlq; }
                else { nomBlq = "(por omisión)"; operand = ""; }
                if (nombreABloque.containsKey(nomBlq)) {
                    bloqueActual = nombreABloque.get(nomBlq);
                    contloc = cpBloques.getOrDefault(bloqueActual, 0);
                } else {
                    contadorBloque++;
                    bloqueActual = contadorBloque;
                    nombreABloque.put(nomBlq, bloqueActual);
                    cpBloques.put(bloqueActual, 0);
                    tablaBloques.add(new int[]{bloqueActual, 0, 0, 0});
                    nombresBloques.add(nomBlq);
                    contloc = 0;
                }
            }

            // ── CSECT: inicia nueva sección de control ────────────────
            case "CSECT" -> {
                if (etiq == null || etiq.isBlank()) {
                    error = "Error: CSECT requiere un nombre (etiqueta)";
                    break;
                }
                // Finalizar sección actual
                cpBloques.put(bloqueActual, contloc);
                guardarEstado();
                finalizarBloques();

                // Crear nueva sección
                SeccionControl nueva = new SeccionControl(etiq, false, 0);
                secciones.add(nueva);
                activarSeccion(nueva);
                inicializarBloqueDefault();
                enCabeceraSeccion = true;
                operand = etiq;

                // Agregar entrada CSECT al listado de la NUEVA sección
                EntradaListado eCsect = construir(numeroLinea++, 0, etiq, "CSECT", "", 0, "", "");
                eCsect.noBloque = 0;
                eCsect.nombreSeccion = seccionActual.nombre;
                listado.add(eCsect);

                // Indicar a enterLinea que ya manejamos el listado
                return new String[]{"CSECT", operand, "0", "", "", "0", ""};
            }

            // ── EXTDEF: exportar símbolos ─────────────────────────────
            case "EXTDEF" -> {
                if (!enCabeceraSeccion) {
                    error = "Error de sintaxis: EXTDEF debe aparecer inmediatamente después de START o CSECT";
                    if (ctx.listaIds() != null)
                        operand = ctx.listaIds().getText();
                    break;
                }
                List<String> syms = new ArrayList<>();
                if (ctx.listaIds() != null)
                    for (var id : ctx.listaIds().ID()) syms.add(id.getText());
                operand = String.join(",", syms);
                for (String s : syms) {
                    if (!seccionActual.simbolosExportados.contains(s))
                        seccionActual.simbolosExportados.add(s);
                }
                seccionActual.directivasExt.add(
                    new SeccionControl.DirectivaExt(true, syms));
            }

            // ── EXTREF: importar símbolos externos ────────────────────
            case "EXTREF" -> {
                if (!enCabeceraSeccion) {
                    error = "Error de sintaxis: EXTREF debe aparecer inmediatamente después de START o CSECT";
                    if (ctx.listaIds() != null)
                        operand = ctx.listaIds().getText();
                    break;
                }
                List<String> syms = new ArrayList<>();
                if (ctx.listaIds() != null)
                    for (var id : ctx.listaIds().ID()) syms.add(id.getText());
                operand = String.join(",", syms);
                for (String s : syms) {
                    if (!tablaSimbolos.containsKey(s)) {
                        tablaSimbolos.put(s, 0);
                        tipoSimbolos.put(s, "E");      // tipo Externo
                        bloqueSimbolos.put(s, -1);     // sin bloque definido
                        seccionActual.simbolosExternos.add(s);
                    }
                }
                seccionActual.directivasExt.add(
                    new SeccionControl.DirectivaExt(false, syms));
            }

            case "END" -> operand = ctx.ID() != null ? ctx.ID().getText() : "";

            default -> error = "Error: Directiva no reconocida '" + nombre + "'";
        }

        // Para todo lo que no sea EXTDEF/EXTREF/CSECT: no son cabecera
        if (!nombre.equals("EXTDEF") && !nombre.equals("EXTREF"))
            enCabeceraSeccion = false;

        return new String[]{nombre, operand, "0", "", error, String.valueOf(delta), tipoExp};
    }

    // ═════════════════════════════════════════════════════════════════
    // EVALUACIÓN DE EXPRESIONES
    // ═════════════════════════════════════════════════════════════════

    /** EQU: exige símbolos previos, mismo bloque, y prohíbe externos. */
    private ResultadoExpr evaluarExpresionEQU(SICXEParser.ExpresionContext ctx) throws Exception {
        boolean multiBloque = tablaBloques.size() > 1;
        ResultadoExpr r = evaluarExpresionConBloque(ctx, true, multiBloque);
        if (r.tieneExternos())
            throw new Exception("EQU no permite símbolos externos en la expresión");
        return validarTipoFinal(r);
    }

    ResultadoExpr evaluarExpresion(SICXEParser.ExpresionContext ctx, boolean exigirDefinidos)
            throws Exception {
        ResultadoExpr r = evaluarExpresionConBloque(ctx, exigirDefinidos, false);
        // Si hay externos: no validar el netoR interno (se manejan en Pass 2)
        if (r.tieneExternos()) return r;
        return validarTipoFinal(r);
    }

    private ResultadoExpr validarTipoFinal(ResultadoExpr r) throws Exception {
        if (r.netoR == 0) return new ResultadoExpr(r.valor, "A");
        if (r.netoR == 1) return new ResultadoExpr(r.valor, "R");
        if (r.netoR == -1)
            throw new Exception("Expresión inválida: término relativo con signo negativo no apareado");
        if (r.netoR > 1)
            throw new Exception("Expresión inválida: " + r.netoR + " términos relativos positivos sin aparear");
        throw new Exception("Expresión inválida: " + (-r.netoR) + " términos relativos negativos sin aparear");
    }

    private ResultadoExpr evaluarExpresionConBloque(SICXEParser.ExpresionContext ctx,
            boolean exigirDefinidos, boolean verificarBloque) throws Exception {

        List<SICXEParser.TerminoContext> terminos = ctx.termino();
        int valorTotal = 0, netoRelativos = 0;
        List<TermExterno> todosExternos = new ArrayList<>();

        ResultadoExpr primero = evaluarTerminoConBloque(terminos.get(0), exigirDefinidos, verificarBloque);
        valorTotal    += primero.valor;
        netoRelativos += primero.netoR;
        agregarExternos(todosExternos, primero.externos, 1);

        int tIdx = 1;
        for (int h = 1; h < ctx.getChildCount(); h += 2) {
            String op = ctx.getChild(h).getText();
            ResultadoExpr r = evaluarTerminoConBloque(terminos.get(tIdx++), exigirDefinidos, verificarBloque);
            int signo = op.equals("+") ? 1 : -1;
            valorTotal    += signo * r.valor;
            netoRelativos += signo * r.netoR;
            agregarExternos(todosExternos, r.externos, signo);
        }

        return new ResultadoExpr(valorTotal, netoRelativos, todosExternos);
    }

    private void agregarExternos(List<TermExterno> dest, List<TermExterno> src, int signo) {
        for (TermExterno te : src)
            dest.add(new TermExterno(te.nombre, te.signo * signo));
    }

    private ResultadoExpr evaluarTerminoConBloque(SICXEParser.TerminoContext ctx,
            boolean exigirDefinidos, boolean verificarBloque) throws Exception {
        List<SICXEParser.FactorContext> factores = ctx.factor();
        ResultadoExpr acum = evaluarFactorConBloque(factores.get(0), exigirDefinidos, verificarBloque);
        int fIdx = 1;
        for (int h = 1; h < ctx.getChildCount(); h += 2) {
            String op = ctx.getChild(h).getText();
            ResultadoExpr r = evaluarFactorConBloque(factores.get(fIdx++), exigirDefinidos, verificarBloque);
            acum = aplicarMulDiv(acum, op, r);
        }
        return acum;
    }

    private ResultadoExpr evaluarFactorConBloque(SICXEParser.FactorContext ctx,
            boolean exigirDefinidos, boolean verificarBloque) throws Exception {

        if (ctx.numeral() != null) {
            return new ResultadoExpr(parsearNumero(ctx.numeral().getText()), "A");

        } else if (ctx.ID() != null) {
            String id = ctx.ID().getText();
            if (!tablaSimbolos.containsKey(id)) {
                if (exigirDefinidos)
                    throw new Exception("Símbolo no definido previamente: '" + id + "'");
                throw new Exception("Símbolo no encontrado: '" + id + "'");
            }
            // Símbolo externo (EXTREF)
            if (seccionActual.simbolosExternos.contains(id)) {
                List<TermExterno> ext = new ArrayList<>();
                ext.add(new TermExterno(id, 1));
                return new ResultadoExpr(0, 0, ext);
            }
            // Símbolo interno normal
            if (verificarBloque) {
                int bloqueSym = bloqueSimbolos.getOrDefault(id, -1);
                if (bloqueSym != bloqueActual)
                    throw new Exception("Símbolo '" + id + "' pertenece al bloque " + bloqueSym
                        + ", se requiere del bloque " + bloqueActual);
            }
            int    val  = tablaSimbolos.get(id);
            String tipo = tipoSimbolos.getOrDefault(id, "A");
            return new ResultadoExpr(val, tipo);

        } else if (ctx.PAREN_IZQ() != null) {
            return evaluarExpresionConBloque(ctx.expresion(), exigirDefinidos, verificarBloque);

        } else if (ctx.MENOS() != null && ctx.factor() != null) {
            ResultadoExpr inner = evaluarFactorConBloque(ctx.factor(), exigirDefinidos, verificarBloque);
            List<TermExterno> negados = new ArrayList<>();
            for (TermExterno te : inner.externos)
                negados.add(new TermExterno(te.nombre, -te.signo));
            return new ResultadoExpr(-inner.valor, -inner.netoR, negados);

        } else {
            throw new Exception("Factor no reconocido: " + ctx.getText());
        }
    }

    private ResultadoExpr aplicarMulDiv(ResultadoExpr a, String op, ResultadoExpr b)
            throws Exception {
        if (a.netoR != 0 || b.netoR != 0)
            throw new Exception("Expresión inválida: términos relativos no pueden usarse en * o /");
        if (a.tieneExternos() || b.tieneExternos())
            throw new Exception("Expresión inválida: símbolos externos no pueden usarse en * o /");
        int va = a.valor, vb = b.valor;
        if (op.equals("/") && vb == 0) throw new Exception("División por cero en expresión");
        return new ResultadoExpr(op.equals("*") ? va * vb : va / vb, 0);
    }

    // ═════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═════════════════════════════════════════════════════════════════
    private boolean esOpcodeConocido(String nombre) {
        if (nombre == null) return false;
        String n = nombre.toUpperCase().replace("+", "");
        return BYTES.containsKey(n) || n.equals("RSUB")
            || java.util.Arrays.asList("FIX","FLOAT","HIO","NORM","SIO","TIO").contains(n)
            || java.util.Arrays.asList("BYTE","WORD","RESB","RESW","BASE","EQU","ORG",
                                        "USE","CSECT","EXTDEF","EXTREF","END","START").contains(n);
    }

    private String combinar(String a, String b) {
        if (!a.isBlank() && !b.isBlank()) return a + " | " + b;
        return a.isBlank() ? b : a;
    }

    int parsearNumero(String valor) throws Exception {
        if (valor == null || valor.isBlank()) throw new Exception("Operando vacío");
        valor = valor.trim().toUpperCase();
        if (valor.endsWith("H"))
            return Integer.parseInt(valor.substring(0, valor.length()-1), 16);
        return Integer.parseInt(valor);
    }

    private boolean esHexValido(String hex) {
        if (hex == null || hex.isBlank()) return false;
        return hex.chars().allMatch(c ->
            Character.isDigit(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
    }

    private EntradaListado construir(int nl, int loc, String etiq,
                                      String ins, String opr, int fmt,
                                      String modo, String err) {
        EntradaListado e = new EntradaListado();
        e.numeroLinea = nl;
        e.contloc     = loc;
        e.etiqueta    = etiq  != null ? etiq  : "";
        e.instruccion = ins   != null ? ins   : "";
        e.operando    = opr   != null ? opr   : "";
        e.formato     = fmt;
        e.modoDir     = modo  != null ? modo  : "";
        e.error       = err   != null ? err   : "";
        return e;
    }

    // ═════════════════════════════════════════════════════════════════
    // GENERAR CSV (todas las secciones combinadas)
    // ═════════════════════════════════════════════════════════════════
    public void generarCSV(String ruta) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {
            pw.println("NL,CONTLOC,ETIQ,INS,OPR,FNT,MOD,COD,ERR,BLOQUE,SECCION");
            for (SeccionControl sec : secciones) {
                for (EntradaListado e : sec.listado) {
                    pw.println(String.join(",",
                        csv(String.valueOf(e.numeroLinea)),
                        csv(String.format("%04X", e.contloc)),
                        csv(e.etiqueta),
                        csv(e.instruccion),
                        csv(e.operando),
                        csv(e.formato > 0 ? String.valueOf(e.formato) : ""),
                        csv(e.modoDir),
                        csv(e.codigoObjeto),
                        csv(!e.errorP2.isBlank() ? e.error + " | P2: " + e.errorP2 : e.error),
                        csv(String.valueOf(e.noBloque)),
                        csv(sec.nombre)
                    ));
                }
            }
        }
    }

    private String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n"))
            return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }
}
