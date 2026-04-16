package sicxe;

import org.antlr.v4.runtime.tree.*;
import java.io.*;
import java.util.*;

/**
 * Analizador semántico — Práctica 3 SIC/XE con Bloques de Programa
 *
 * Paso 1 completo con:
 *  - Directiva USE (bloques de programa)
 *  - Tabla de bloques: número, nombre, longitud, dirección de inicio
 *  - CP independiente por bloque
 *  - TABSIM con columna NoBloque
 *  - Directiva EQU, ORG, expresiones, tipos A/R
 */
public class AnalizadorSemantico extends SICXEBaseListener {

    // ── Resultados ────────────────────────────────────────────────────
    private final Map<String, Integer> tablaSimbolos   = new LinkedHashMap<>();
    private final Map<String, String>  tipoSimbolos    = new LinkedHashMap<>();
    private final Map<String, Integer> bloqueSimbolos  = new LinkedHashMap<>();
    private final List<String>         errores         = new ArrayList<>();
    private final List<EntradaListado> listado         = new ArrayList<>();

    // ── Tabla de bloques ──────────────────────────────────────────────
    // Cada entrada: [0]=número, [1]=(unused), [2]=longitud, [3]=dirIni
    private final List<int[]>          tablaBloques    = new ArrayList<>();
    private final List<String>         nombresBloques  = new ArrayList<>(); // índice = noBloque
    private final Map<String,Integer>  nombreABloque   = new LinkedHashMap<>();
    private final Map<Integer,Integer> cpBloques       = new LinkedHashMap<>();
    private int bloqueActual   = 0;
    private int contadorBloque = 0;

    // ── Contador de localidades ───────────────────────────────────────
    private int contloc      = 0;
    private int direccionIni = 0;

    // ── Valor BASE para Paso 2 ────────────────────────────────────────
    private int    valorBase   = 0;
    private String simboloBase = "";

    private int numeroLinea = 1;

    // ── Bytes por instrucción conocida ────────────────────────────────
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

    // ── Getters ───────────────────────────────────────────────────────
    public Map<String, Integer> getTablaSimbolos()  { return tablaSimbolos; }
    public Map<String, String>  getTipoSimbolos()   { return tipoSimbolos; }
    public Map<String, Integer> getBloqueSimbolos()  { return bloqueSimbolos; }
    public List<String>         getErrores()        { return errores; }
    public List<EntradaListado> getListado()        { return listado; }
    public int getValorBase()                       { return valorBase; }
    public String getSimboloBase()                  { return simboloBase; }
    public int    getDireccionInicio()              { return direccionIni; }
    public List<int[]> getTablaBloques()            { return tablaBloques; }
    public List<String> getNombresBloques()         { return nombresBloques; }

    public int getLongitudPrograma() {
        if (tablaBloques.isEmpty()) return contloc - direccionIni;
        int total = 0;
        for (int[] b : tablaBloques) total += b[2];
        return total;
    }

    // ═════════════════════════════════════════════════════════════════
    // encabezado
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void enterEncabezado(SICXEParser.EncabezadoContext ctx) {
        // Siempre inicializar bloque por omisión (bloque 0)
        bloqueActual = 0;
        contadorBloque = 0;
        nombreABloque.put("(por omisión)", 0);
        cpBloques.put(0, 0);
        tablaBloques.add(new int[]{0, 0, 0, 0});
        nombresBloques.add("(por omisión)");

        try {
            String num   = ctx.numeral().getText().toUpperCase();
            direccionIni = parsearNumero(num);
            contloc      = direccionIni;
            cpBloques.put(0, contloc);
            String etiq  = ctx.etiqueta() != null ? ctx.etiqueta().ID().getText() : null;

            EntradaListado e = construir(numeroLinea++, contloc, etiq, "START", num, 0, "", "");
            e.noBloque = bloqueActual;
            listado.add(e);
        } catch (Exception ex) {
            String etiq = null;
            String num  = "";
            try { etiq = ctx.etiqueta() != null ? ctx.etiqueta().ID().getText() : null; } catch (Exception ig) {}
            try { num  = ctx.numeral() != null ? ctx.numeral().getText() : ""; } catch (Exception ig) {}
            String errorMsg = "Error en START: " + ex.getMessage();
            EntradaListado e = construir(numeroLinea++, contloc, etiq, "START", num, 0, "", errorMsg);
            e.noBloque = bloqueActual;
            listado.add(e);
            errores.add("x " + errorMsg);
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // linea
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void enterLinea(SICXEParser.LineaContext ctx) {
        try {
            String etiq   = ctx.etiqueta() != null ? ctx.etiqueta().ID().getText() : null;
            int locActual = contloc;
            int bloqueAnterior = bloqueActual; // Guardar antes de procesar (USE lo cambia)

            String[] res;
            if (ctx.enunciado().instruccion() != null) {
                res = procesarInstruccion(ctx.enunciado().instruccion());
            } else if (ctx.enunciado().directiva() != null) {
                res = procesarDirectiva(ctx.enunciado().directiva(), etiq, locActual);
            } else {
                res = new String[]{"", "", "0", "", "Error: Línea no reconocida", "0"};
            }

            String errorInstr = res[4];
            int    delta      = Integer.parseInt(res[5]);

            boolean esEQU = res[0].equals("EQU");
            boolean esUSE = res[0].equals("USE");

            // Para USE: el noBloque y contloc deben ser del bloque NUEVO (destino)
            // Para todo lo demás: del bloque actual
            int bloqueParaLinea = bloqueActual;
            int locParaLinea    = locActual;
            if (esUSE) {
                // Después de procesarDirectiva, bloqueActual ya es el nuevo bloque
                // y contloc ya es el CP del nuevo bloque
                bloqueParaLinea = bloqueActual;
                locParaLinea    = contloc;
            }

            String errorEtiqueta = "";
            if (etiq != null && !etiq.isBlank() && !esEQU && !esUSE) {
                if (tablaSimbolos.containsKey(etiq)) {
                    errorEtiqueta = "Símbolo duplicado '" + etiq + "'";
                } else {
                    tablaSimbolos.put(etiq, locActual);
                    tipoSimbolos.put(etiq, "R");
                    bloqueSimbolos.put(etiq, bloqueParaLinea);
                }
            }

            String errorFinal = combinar(errorInstr, errorEtiqueta);

            if (!esUSE) {
                // Solo actualizar CP si NO es USE (USE ya lo cambió en procesarDirectiva)
                if (delta < 0) {
                    contloc = -(delta + 1);
                } else {
                    contloc += delta;
                }
                cpBloques.put(bloqueActual, contloc);
            }

            EntradaListado entrada = construir(
                numeroLinea++, locParaLinea, etiq,
                res[0], res[1],
                Integer.parseInt(res[2]),
                res[3], errorFinal
            );
            entrada.noBloque = bloqueParaLinea;
            if (res.length > 6 && res[6] != null) entrada.tipoExpresion = res[6];
            listado.add(entrada);

            if (!errorFinal.isBlank())
                errores.add(String.format("x Error en línea %d [%s]: %s",
                    numeroLinea - 1,
                    res[0].isBlank() ? "?" : res[0],
                    errorFinal));

        } catch (Exception ex) {
            errores.add("x Error inesperado en línea " + numeroLinea + ": " + ex.getMessage());
            numeroLinea++;
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // lineaError
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void enterLineaError(SICXEParser.LineaErrorContext ctx) {
        try {
            java.util.List<SICXEParser.TokensErrorContext> toksList = ctx.tokensError();
            java.util.List<String> tokTexts = new java.util.ArrayList<>();
            for (SICXEParser.TokensErrorContext tok : toksList)
                tokTexts.add(tok.getText());

            String etiq        = "";
            String instruccion = "";
            String operando    = "";

            if (tokTexts.size() >= 2 && esOpcodeConocido(tokTexts.get(1))) {
                etiq       = tokTexts.get(0);
                instruccion = tokTexts.get(1);
                operando   = String.join(" ", tokTexts.subList(2, tokTexts.size()));
            } else if (!tokTexts.isEmpty()) {
                instruccion = tokTexts.get(0);
                operando   = String.join(" ", tokTexts.subList(1, tokTexts.size()));
            }

            if (!etiq.isBlank()) {
                if (!tablaSimbolos.containsKey(etiq)) {
                    tablaSimbolos.put(etiq, contloc);
                    tipoSimbolos.put(etiq, "R");
                    bloqueSimbolos.put(etiq, bloqueActual);
                }
            }

            String error;
            boolean primerTokenEsOpcode = esOpcodeConocido(instruccion);
            if (primerTokenEsOpcode) {
                error = "Error de sintaxis: instrucción '" + instruccion
                      + (operando.isBlank() ? "' con operando inválido" : "' con operando inválido '" + operando + "'");
            } else {
                error = "Error: Instrucción no existe '" + instruccion + "'"
                      + (operando.isBlank() ? "" : " (operando: '" + operando + "')");
            }

            EntradaListado e = construir(
                numeroLinea++, contloc, etiq,
                instruccion, operando, 0, "", error
            );
            e.noBloque = bloqueActual;
            listado.add(e);

            errores.add(String.format("x Error en línea %d [%s]: %s",
                numeroLinea - 1, instruccion.isBlank() ? "?" : instruccion, error));

        } catch (Exception ex) {
            errores.add("x Error inesperado en línea " + numeroLinea + ": " + ex.getMessage());
            numeroLinea++;
        }
    }

    private boolean esOpcodeConocido(String nombre) {
        if (nombre == null) return false;
        String n = nombre.toUpperCase().replace("+", "");
        return BYTES.containsKey(n)
            || n.equals("RSUB")
            || java.util.Arrays.asList("FIX","FLOAT","HIO","NORM","SIO","TIO").contains(n)
            || java.util.Arrays.asList("BYTE","WORD","RESB","RESW","BASE","EQU","ORG","USE","END","START").contains(n);
    }

    // ═════════════════════════════════════════════════════════════════
    // cierre — registra END y finaliza bloques
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void enterCierre(SICXEParser.CierreContext ctx) {
        // Guardar CP del bloque actual antes de finalizar
        cpBloques.put(bloqueActual, contloc);

        // END siempre pertenece al bloque por omisión (bloque 0)
        int cpBloque0 = cpBloques.getOrDefault(0, 0);

        String etiq    = ctx.etiqueta() != null ? ctx.etiqueta().ID().getText() : null;
        String operand = ctx.destino()  != null ? ctx.destino().expresion().getText() : "";
        EntradaListado e = construir(numeroLinea++, cpBloque0, etiq, "END", operand, 0, "", "");
        e.noBloque = 0;
        listado.add(e);

        finalizarBloques();
    }

    /**
     * Al terminar el paso 1: calcula longitudes, direcciones de inicio
     * de cada bloque y ajusta todas las direcciones absolutas.
     */
    private void finalizarBloques() {
        // Calcular longitud de cada bloque
        for (int[] b : tablaBloques) {
            int num = b[0];
            int cp  = cpBloques.getOrDefault(num, 0);
            if (num == 0)
                b[2] = cp - direccionIni;
            else
                b[2] = cp;
        }

        // Si solo hay un bloque, dirección de inicio = 0
        tablaBloques.get(0)[3] = 0;
        for (int i = 1; i < tablaBloques.size(); i++) {
            tablaBloques.get(i)[3] = tablaBloques.get(i-1)[3] + tablaBloques.get(i-1)[2];
        }

        // Solo ajustar si hay más de un bloque
        if (tablaBloques.size() <= 1) return;

        // NO ajustar TABSIM — los valores se quedan relativos al bloque.
        // El Paso 2 calcula direcciones absolutas internamente cuando las necesita.
    }

    private int obtenerDirInicioBloque(int noBloque) {
        for (int[] b : tablaBloques)
            if (b[0] == noBloque) return b[3];
        return 0;
    }

    // ═════════════════════════════════════════════════════════════════
    // PROCESAR INSTRUCCIÓN
    // ═════════════════════════════════════════════════════════════════
    private String[] procesarInstruccion(SICXEParser.InstruccionContext ctx) {
        String nombre  = "";
        String operand = "";
        String modoDir = "";
        String error   = "";
        int    formato = 0;
        int    delta   = 0;
        String tipoExp = "";

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
                delta = 0;
            }

        } else if (ctx.OPCODE_F2_RR() != null) {
            nombre  = ctx.OPCODE_F2_RR().getText();
            formato = 2;
            if (ctx.reg().size() == 2) {
                operand = ctx.reg(0).getText() + "," + ctx.reg(1).getText();
                delta   = 2;
            } else {
                operand = ctx.getText().replace(nombre, "").trim();
                error   = "Error semántico: " + nombre + " requiere dos registros";
                delta   = 0;
            }

        } else if (ctx.OPCODE_F2_RN() != null) {
            nombre  = ctx.OPCODE_F2_RN().getText();
            formato = 2;
            if (!ctx.reg().isEmpty() && ctx.numeral() != null) {
                operand = ctx.reg(0).getText() + "," + ctx.numeral().getText();
                delta   = 2;
            } else {
                operand = ctx.getText().replace(nombre, "").trim();
                error   = "Error semántico: " + nombre + " requiere registro y número";
                delta   = 0;
            }

        } else if (ctx.OPCODE_F2_R() != null) {
            nombre  = ctx.OPCODE_F2_R().getText();
            formato = 2;
            if (!ctx.reg().isEmpty()) {
                operand = ctx.reg(0).getText();
                delta   = 2;
            } else {
                operand = ctx.getText().replace(nombre, "").trim();
                error   = "Error semántico: " + nombre + " requiere un registro";
                delta   = 0;
            }

        } else if (ctx.OPCODE() != null && ctx.modoAcceso() != null) {
            nombre  = ctx.OPCODE().getText();
            formato = 3;
            delta   = 3;
            operand = ctx.modoAcceso().getText();
            modoDir = detectarModo(operand);
            tipoExp = evaluarTipoModoAcceso(ctx.modoAcceso());

        } else if (ctx.KRSUB() != null) {
            nombre  = "RSUB";
            formato = 3;
            delta   = 3;

        } else if (ctx.OPCODE_F1() != null) {
            nombre  = ctx.OPCODE_F1().getText();
            formato = 1;
            delta   = 1;

        } else if (ctx.OPCODE() != null) {
            nombre  = ctx.OPCODE().getText();
            formato = 3;
            delta   = 0;
            error   = "Error semántico: " + nombre + " requiere un operando";

        } else {
            nombre = ctx.getText().trim();
            error  = "Error: Instrucción no definida '" + nombre + "'";
            delta  = 0;
        }

        return new String[]{
            nombre, operand,
            String.valueOf(formato), modoDir,
            error, String.valueOf(delta), tipoExp
        };
    }

    private String evaluarTipoModoAcceso(SICXEParser.ModoAccesoContext ctx) {
        SICXEParser.ExpresionContext exprCtx = null;
        if (ctx.expresion() != null) exprCtx = ctx.expresion();
        if (exprCtx == null) return "";
        try {
            ResultadoExpr r = evaluarExpresion(exprCtx, false);
            return r.tipo;
        } catch (Exception e) {
            return "";
        }
    }

    private String detectarModo(String op) {
        if (op == null || op.isBlank()) return "";
        if (op.startsWith("#"))  return "Inmediato";
        if (op.startsWith("@"))  return "Indirecto";
        if (op.toUpperCase().endsWith(",X"))   return "Indexado";
        return "Simple";
    }

    // ═════════════════════════════════════════════════════════════════
    // PROCESAR DIRECTIVA
    // ═════════════════════════════════════════════════════════════════
    private String[] procesarDirectiva(SICXEParser.DirectivaContext ctx,
                                        String etiq, int locActual) {
        String nombre  = ctx.getChild(0).getText().toUpperCase();
        String operand = "";
        String error   = "";
        int    delta   = 0;
        String tipoExp = "";

        switch (nombre) {

            case "BYTE" -> {
                if (ctx.contenidoByte() != null) {
                    if (ctx.contenidoByte().LITC() != null) {
                        operand = ctx.contenidoByte().LITC().getText();
                        String interior = operand.substring(2, operand.length() - 1);
                        delta = interior.length();
                    } else if (ctx.contenidoByte().LITX() != null) {
                        operand = ctx.contenidoByte().LITX().getText();
                        String hex = operand.substring(2, operand.length() - 1);
                        if (!esHexValido(hex)) {
                            error = "Error: Hex inválido en BYTE X'" + hex + "'";
                            delta = 0;
                        } else {
                            delta = (hex.length() + 1) / 2;
                        }
                    }
                } else {
                    error = "Error de sintaxis: BYTE requiere C'texto' o X'hex'";
                }
            }

            case "WORD" -> {
                operand = ctx.expresion() != null ? ctx.expresion().getText() : "";
                if (ctx.expresion() != null) {
                    try {
                        ResultadoExpr r = evaluarExpresion(ctx.expresion(), false);
                        tipoExp = r.tipo;
                    } catch (Exception ex) {
                        tipoExp = "";
                    }
                }
                delta = 3;
            }

            case "RESB" -> {
                operand = ctx.expresion() != null ? ctx.expresion().getText() : "";
                try {
                    ResultadoExpr r = evaluarExpresion(ctx.expresion(), true);
                    if (!r.tipo.equals("A")) {
                        error = "Error: RESB requiere expresión absoluta";
                        delta = 0;
                    } else if (r.valor <= 0) {
                        error = "Error: RESB requiere valor positivo (recibido: " + r.valor + ")";
                        delta = 0;
                    } else {
                        delta = r.valor;
                    }
                } catch (Exception ex) {
                    error = "Error: Operando inválido en RESB '" + operand + "': " + ex.getMessage();
                    delta = 0;
                }
            }

            case "RESW" -> {
                operand = ctx.expresion() != null ? ctx.expresion().getText() : "";
                try {
                    ResultadoExpr r = evaluarExpresion(ctx.expresion(), true);
                    if (!r.tipo.equals("A")) {
                        error = "Error: RESW requiere expresión absoluta";
                        delta = 0;
                    } else if (r.valor <= 0) {
                        error = "Error: RESW requiere valor positivo (recibido: " + r.valor + ")";
                        delta = 0;
                    } else {
                        delta = 3 * r.valor;
                    }
                } catch (Exception ex) {
                    error = "Error: Operando inválido en RESW '" + operand + "': " + ex.getMessage();
                    delta = 0;
                }
            }

            case "BASE" -> {
                if (ctx.ID() != null) {
                    simboloBase = ctx.ID().getText();
                    operand     = simboloBase;
                } else if (ctx.numeral() != null) {
                    operand = ctx.numeral().getText();
                    try {
                        valorBase = parsearNumero(operand);
                    } catch (Exception ex) {
                        error = "Error: Valor no numérico en BASE '" + operand + "'";
                    }
                } else {
                    error = "Error de sintaxis: BASE requiere símbolo o número";
                }
            }

            case "EQU" -> {
                if (etiq == null || etiq.isBlank()) {
                    error = "Error: EQU requiere una etiqueta";
                    break;
                }
                boolean esAsterisco = (ctx.MUL() != null && ctx.expresion() == null);
                if (esAsterisco) {
                    operand = "*";
                    if (tablaSimbolos.containsKey(etiq)) {
                        error = "Símbolo duplicado '" + etiq + "'";
                    } else {
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
                        if (tablaSimbolos.containsKey(etiq)) {
                            error = "Símbolo duplicado '" + etiq + "'";
                        } else {
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
                } else {
                    error = "Error de sintaxis en EQU";
                }
            }

            case "ORG" -> {
                if (ctx.expresion() == null) {
                    error = "Error: ORG requiere un valor";
                    break;
                }
                operand = ctx.expresion().getText();
                try {
                    int nuevoCP = parsearNumero(operand);
                    delta = -(nuevoCP + 1);
                } catch (Exception ex) {
                    error = "Error: ORG solo acepta número literal decimal o hexadecimal. Valor recibido: '" + operand + "'";
                }
            }

            // ── USE ───────────────────────────────────────────────────
            case "USE" -> {
                cpBloques.put(bloqueActual, contloc);

                String nombreBloque;
                if (ctx.ID() != null) {
                    nombreBloque = ctx.ID().getText();
                    operand = nombreBloque;
                } else {
                    nombreBloque = "(por omisión)";
                    operand = "";
                }

                if (nombreABloque.containsKey(nombreBloque)) {
                    bloqueActual = nombreABloque.get(nombreBloque);
                    contloc = cpBloques.getOrDefault(bloqueActual, 0);
                } else {
                    contadorBloque++;
                    bloqueActual = contadorBloque;
                    nombreABloque.put(nombreBloque, bloqueActual);
                    cpBloques.put(bloqueActual, 0);
                    tablaBloques.add(new int[]{bloqueActual, 0, 0, 0});
                    nombresBloques.add(nombreBloque);
                    contloc = 0;
                }
                // delta = 0
            }

            case "END" -> {
                operand = ctx.ID() != null ? ctx.ID().getText() : "";
            }

            default -> {
                error = "Error: Directiva no reconocida '" + nombre + "'";
            }
        }

        return new String[]{nombre, operand, "0", "", error, String.valueOf(delta), tipoExp};
    }

    // ═════════════════════════════════════════════════════════════════
    // EVALUACIÓN DE EXPRESIONES
    // ═════════════════════════════════════════════════════════════════

    static class ResultadoExpr {
        final int    valor;
        final String tipo;   // "A", "R", "RN" (derivado de netoR)
        final int    netoR;  // conteo neto exacto de relativos con signo
        ResultadoExpr(int v, String t) { valor = v; tipo = t; netoR = tipoANeto(t); }
        ResultadoExpr(int v, int neto) {
            valor = v; netoR = neto;
            tipo = neto == 0 ? "A" : neto == 1 ? "R" : neto == -1 ? "RN" : "?";
        }
        private static int tipoANeto(String t) {
            return t.equals("R") ? 1 : t.equals("RN") ? -1 : 0;
        }
    }

    /**
     * Evalúa expresión para EQU: exige símbolos previamente definidos
     * y del mismo bloque (restricción de bloques de programa).
     */
    private ResultadoExpr evaluarExpresionEQU(SICXEParser.ExpresionContext ctx) throws Exception {
        boolean multiBloque = tablaBloques.size() > 1;
        ResultadoExpr r = evaluarExpresionConBloque(ctx, true, multiBloque);
        return validarTipoFinal(r);
    }

    ResultadoExpr evaluarExpresion(SICXEParser.ExpresionContext ctx, boolean exigirDefinidos)
            throws Exception {
        ResultadoExpr r = evaluarExpresionConBloque(ctx, exigirDefinidos, false);
        return validarTipoFinal(r);
    }

    /** Convierte netoR fuera de {0,1} a error. Una expresión final solo puede ser A o R. */
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

        int valorTotal    = 0;
        int netoRelativos = 0;

        ResultadoExpr primero = evaluarTerminoConBloque(terminos.get(0), exigirDefinidos, verificarBloque);
        valorTotal    += primero.valor;
        netoRelativos += primero.netoR;

        int hijosExpr = ctx.getChildCount();
        int tIdx = 1;
        for (int h = 1; h < hijosExpr; h += 2) {
            String op = ctx.getChild(h).getText();
            ResultadoExpr r = evaluarTerminoConBloque(terminos.get(tIdx++), exigirDefinidos, verificarBloque);
            int signo = op.equals("+") ? 1 : -1;
            valorTotal    += signo * r.valor;
            netoRelativos += signo * r.netoR;
        }

        return new ResultadoExpr(valorTotal, netoRelativos);
    }

    private ResultadoExpr evaluarTerminoConBloque(SICXEParser.TerminoContext ctx,
            boolean exigirDefinidos, boolean verificarBloque) throws Exception {
        List<SICXEParser.FactorContext> factores = ctx.factor();
        ResultadoExpr acum = evaluarFactorConBloque(factores.get(0), exigirDefinidos, verificarBloque);

        int hijosT = ctx.getChildCount();
        int fIdx   = 1;
        for (int h = 1; h < hijosT; h += 2) {
            String op      = ctx.getChild(h).getText();
            ResultadoExpr r = evaluarFactorConBloque(factores.get(fIdx++), exigirDefinidos, verificarBloque);
            acum = aplicarMulDiv(acum, op, r);
        }
        return acum;
    }

    private ResultadoExpr evaluarFactorConBloque(SICXEParser.FactorContext ctx,
            boolean exigirDefinidos, boolean verificarBloque) throws Exception {
        if (ctx.numeral() != null) {
            int val = parsearNumero(ctx.numeral().getText());
            return new ResultadoExpr(val, "A");

        } else if (ctx.ID() != null) {
            String id = ctx.ID().getText();
            if (!tablaSimbolos.containsKey(id)) {
                if (exigirDefinidos)
                    throw new Exception("Símbolo no definido previamente: '" + id + "'");
                throw new Exception("Símbolo no encontrado: '" + id + "'");
            }
            if (verificarBloque) {
                int bloqueSym = bloqueSimbolos.getOrDefault(id, -1);
                if (bloqueSym != bloqueActual) {
                    throw new Exception("Símbolo '" + id + "' pertenece al bloque "
                        + bloqueSym + ", se requiere del bloque " + bloqueActual
                        + " (EQU exige símbolos del mismo bloque)");
                }
            }
            int    val  = tablaSimbolos.get(id);
            String tipo = tipoSimbolos.getOrDefault(id, "A");
            return new ResultadoExpr(val, tipo);

        } else if (ctx.PAREN_IZQ() != null) {
            return evaluarExpresionConBloque(ctx.expresion(), exigirDefinidos, verificarBloque);

        } else if (ctx.MENOS() != null && ctx.factor() != null) {
            // Negación unaria: -factor → invierte valor Y netoR
            ResultadoExpr inner = evaluarFactorConBloque(ctx.factor(), exigirDefinidos, verificarBloque);
            return new ResultadoExpr(-inner.valor, -inner.netoR);

        } else {
            throw new Exception("Factor no reconocido: " + ctx.getText());
        }
    }

    private ResultadoExpr aplicarSumaResta(ResultadoExpr a, String op, ResultadoExpr b)
            throws Exception {
        int resultado = op.equals("+") ? a.valor + b.valor : a.valor - b.valor;
        String ta = a.tipo, tb = b.tipo;

        // Normalizar RN: invertir la operación para determinar tipo
        // A - RN  es como  A + R → R    |  A + RN  es como  A - R → error
        // R - RN  es como  R + R → error |  R + RN  es como  R - R → A
        // RN ± A  → tratar RN como R    |  RN - R  → R - R → A
        // RN + R  → R + R → error       |  RN ± RN → doble inversión
        if (tb.equals("RN")) {
            String opEfectivo = op.equals("+") ? "-" : "+";
            tb = "R";
            // Determinar tipo con operación invertida, pero VALOR ya es correcto
            return determinarTipoSumaResta(resultado, ta, opEfectivo, tb);
        }
        if (ta.equals("RN")) {
            ta = "R"; // valor ya está negado
        }

        return determinarTipoSumaResta(resultado, ta, op, tb);
    }

    private ResultadoExpr determinarTipoSumaResta(int resultado, String ta, String op, String tb)
            throws Exception {
        if (ta.equals("A") && tb.equals("A"))
            return new ResultadoExpr(resultado, "A");
        if (ta.equals("R") && tb.equals("R") && op.equals("-"))
            return new ResultadoExpr(resultado, "A");
        if (ta.equals("R") && tb.equals("A") && op.equals("+"))
            return new ResultadoExpr(resultado, "R");
        if (ta.equals("R") && tb.equals("A") && op.equals("-"))
            return new ResultadoExpr(resultado, "R");
        if (ta.equals("A") && tb.equals("R") && op.equals("+"))
            return new ResultadoExpr(resultado, "R");
        if (ta.equals("A") && tb.equals("R") && op.equals("-"))
            throw new Exception("Expresión inválida: término relativo con signo negativo no apareado (A - R)");
        if (ta.equals("R") && tb.equals("R") && op.equals("+"))
            throw new Exception("Expresión inválida: dos términos relativos con el mismo signo (R + R)");

        throw new Exception("Combinación de tipos no soportada: " + ta + " " + op + " " + tb);
    }

    private ResultadoExpr aplicarMulDiv(ResultadoExpr a, String op, ResultadoExpr b)
            throws Exception {
        if (a.netoR != 0 || b.netoR != 0)
            throw new Exception("Expresión inválida: términos relativos no pueden usarse en multiplicación/división");
        int va = a.valor, vb = b.valor;
        if (op.equals("/") && vb == 0)
            throw new Exception("División por cero en expresión");
        int resultado = op.equals("*") ? va * vb : va / vb;
        return new ResultadoExpr(resultado, 0);
    }

    // ═════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═════════════════════════════════════════════════════════════════

    private String combinar(String a, String b) {
        if (!a.isBlank() && !b.isBlank()) return a + " | " + b;
        if (!a.isBlank()) return a;
        return b;
    }

    int parsearNumero(String valor) throws Exception {
        if (valor == null || valor.isBlank()) throw new Exception("Operando vacío");
        valor = valor.trim().toUpperCase();
        if (valor.endsWith("H"))
            return Integer.parseInt(valor.substring(0, valor.length() - 1), 16);
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
        e.etiqueta    = etiq != null ? etiq : "";
        e.instruccion = ins  != null ? ins  : "";
        e.operando    = opr  != null ? opr  : "";
        e.formato     = fmt;
        e.modoDir     = modo != null ? modo : "";
        e.error       = err  != null ? err  : "";
        return e;
    }

    // ═════════════════════════════════════════════════════════════════
    // GENERAR CSV
    // ═════════════════════════════════════════════════════════════════
    public void generarCSV(String ruta) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {
            pw.println("NL,CONTLOC,ETIQ,INS,OPR,FNT,MOD,COD,ERR,BLOQUE");
            for (EntradaListado e : listado) {
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
                    csv(String.valueOf(e.noBloque))
                ));
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