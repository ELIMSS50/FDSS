package sicxe;

import java.io.*;
import java.util.*;

/**
 * Paso 2 del ensamblador SIC/XE — Generación de código objeto.
 *
 * Novedades respecto a versión anterior:
 *  - Operandos de F3/F4 pueden ser expresiones (ej: SALTO-4, #NUEVO-4)
 *  - WORD puede contener expresiones simbólicas
 *  - WORD relativa genera registro M
 *  - TABSIM ahora incluye tipo A/R para validar expresiones
 *  - EQU y ORG no generan código (ya manejados en Paso 1)
 */
public class GeneradorCodigo {

    // ── Código de operación por mnemónico ─────────────────────────────
    private static final Map<String, Integer> CODOP = new HashMap<>();
    static {
        CODOP.put("ADD",   0x18); CODOP.put("ADDF",  0x58); CODOP.put("ADDR",  0x90);
        CODOP.put("AND",   0x40); CODOP.put("CLEAR", 0xB4); CODOP.put("COMP",  0x28);
        CODOP.put("COMPF", 0x88); CODOP.put("COMPR", 0xA0); CODOP.put("DIV",   0x24);
        CODOP.put("DIVF",  0x64); CODOP.put("DIVR",  0x9C); CODOP.put("FIX",   0xC4);
        CODOP.put("FLOAT", 0xC0); CODOP.put("HIO",   0xF4); CODOP.put("J",     0x3C);
        CODOP.put("JEQ",   0x30); CODOP.put("JGT",   0x34); CODOP.put("JLT",   0x38);
        CODOP.put("JSUB",  0x48); CODOP.put("LDA",   0x00); CODOP.put("LDB",   0x68);
        CODOP.put("LDCH",  0x50); CODOP.put("LDF",   0x70); CODOP.put("LDL",   0x08);
        CODOP.put("LDS",   0x6C); CODOP.put("LDT",   0x74); CODOP.put("LDX",   0x04);
        CODOP.put("LPS",   0xD0); CODOP.put("MUL",   0x20); CODOP.put("MULF",  0x60);
        CODOP.put("MULR",  0x98); CODOP.put("NORM",  0xC8); CODOP.put("OR",    0x44);
        CODOP.put("RD",    0xD8); CODOP.put("RMO",   0xAC); CODOP.put("RSUB",  0x4C);
        CODOP.put("SHIFTL",0xA4); CODOP.put("SHIFTR",0xA8); CODOP.put("SIO",   0xF0);
        CODOP.put("SSK",   0xEC); CODOP.put("STA",   0x0C); CODOP.put("STB",   0x78);
        CODOP.put("STCH",  0x54); CODOP.put("STF",   0x80); CODOP.put("STI",   0xD4);
        CODOP.put("STL",   0x14); CODOP.put("STS",   0x7C); CODOP.put("STSW",  0xE8);
        CODOP.put("STT",   0x84); CODOP.put("STX",   0x10); CODOP.put("SUB",   0x1C);
        CODOP.put("SUBF",  0x5C); CODOP.put("SUBR",  0x94); CODOP.put("SVC",   0xB0);
        CODOP.put("TD",    0xE0); CODOP.put("TIO",   0xF8); CODOP.put("TIX",   0x2C);
        CODOP.put("TIXR",  0xB8); CODOP.put("WD",    0xDC);
    }

    private static final Map<String, Integer> REG = new HashMap<>();
    static {
        REG.put("A", 0); REG.put("X", 1); REG.put("L", 2);
        REG.put("B", 3); REG.put("S", 4); REG.put("T", 5); REG.put("F", 6);
    }

    private static final Set<String> F2_UN_REG = new HashSet<>(Arrays.asList("CLEAR", "TIXR"));
    private static final Set<String> F2_SHIFT  = new HashSet<>(Arrays.asList("SHIFTL", "SHIFTR"));

    // ── Datos del Paso 1 ──────────────────────────────────────────────
    private final List<EntradaListado> listado;
    private final Map<String, Integer> tablaSimbolos;
    private final Map<String, String>  tipoSimbolos;   // A / R
    private final Map<String, Integer> bloqueSimbolos;  // símbolo → noBloque
    private       int                  baseVal;
    private final String               simboloBase;
    private final List<int[]>          tablaBloques;    // bloques: [noBloque, ?, longitud, dirIni]
    private final int                  direccionIni;    // dirección de inicio del programa

    private final List<String> errores = new ArrayList<>();
    private int     dirEntrada      = 0;
    private boolean errorDirEntrada = false;
    private final Set<String> simbolosRelocalizables = new LinkedHashSet<>();

    private List<String> registros = new ArrayList<>();

    public GeneradorCodigo(List<EntradaListado> listado,
                           Map<String, Integer> tablaSimbolos,
                           Map<String, String>  tipoSimbolos,
                           Map<String, Integer> bloqueSimbolos,
                           int                  valorBase,
                           String               simboloBase,
                           List<int[]>          tablaBloques,
                           int                  direccionIni) {
        this.listado        = listado;
        this.tablaSimbolos  = tablaSimbolos;
        this.tipoSimbolos   = tipoSimbolos;
        this.bloqueSimbolos = bloqueSimbolos;
        this.baseVal        = valorBase;
        this.simboloBase    = simboloBase;
        this.tablaBloques   = tablaBloques;
        this.direccionIni   = direccionIni;
    }

    /** Convierte contloc relativo al bloque → dirección absoluta */
    private int dirAbsoluta(EntradaListado e) {
        if (e.noBloque == 0) return e.contloc;
        for (int[] b : tablaBloques)
            if (b[0] == e.noBloque)
                return e.contloc + b[3] + direccionIni;
        return e.contloc;
    }

    /** Obtiene el valor absoluto de un símbolo (ajustado por bloque si es relativo) */
    private int symAbsoluto(String id) {
        int val = tablaSimbolos.get(id);
        String tipo = tipoSimbolos.getOrDefault(id, "R");
        if (tipo.equals("A")) return val; // absolutos no se ajustan
        int blk = bloqueSimbolos.getOrDefault(id, 0);
        if (blk == 0) return val; // bloque 0 ya es absoluto
        for (int[] b : tablaBloques)
            if (b[0] == blk)
                return val + b[3] + direccionIni;
        return val;
    }

    public List<String> getErrores()                { return errores; }
    public int          getDirEntrada()             { return dirEntrada; }
    public boolean      isErrorDirEntrada()         { return errorDirEntrada; }
    public Set<String>  getSimbolosRelocalizables() { return simbolosRelocalizables; }

    // ═════════════════════════════════════════════════════════════════
    // PUNTO DE ENTRADA
    // ═════════════════════════════════════════════════════════════════
    public void generar() {
        if (!simboloBase.isBlank() && tablaSimbolos.containsKey(simboloBase))
            baseVal = symAbsoluto(simboloBase);

        resolverDirEntrada();

        for (int idx = 0; idx < listado.size(); idx++) {
            EntradaListado e = listado.get(idx);

            if (!e.error.isBlank()) {
                String err = e.error.toLowerCase();
                boolean soloEsDuplicado = err.contains("duplicado")
                    && !err.contains("no existe")
                    && !err.contains("no definida")
                    && !err.contains("no reconocida")
                    && !err.contains("de sintaxis")
                    && !err.contains("instrucción no existe");
                if (!soloEsDuplicado) continue;
            }

            int pc = (idx + 1 < listado.size())
                   ? dirAbsoluta(listado.get(idx + 1))
                   : dirAbsoluta(e);

            String ins  = e.instruccion.trim();
            String mnem = ins.startsWith("+") ? ins.substring(1) : ins;

            try {
                switch (ins) {
                    case "BYTE"  -> e.codigoObjeto = generarByte(e.operando);
                    case "WORD"  -> {
                        e.codigoObjeto = generarWord(e);
                    }
                    case "START", "END", "RESB", "RESW", "BASE", "EQU", "ORG", "USE" -> { /* sin código */ }
                    default -> {
                        if (!CODOP.containsKey(mnem)) {
                            registrarErrorP2(e, "Sin código de operación: " + mnem);
                            continue;
                        }
                        switch (e.formato) {
                            case 1 -> e.codigoObjeto = generarF1(mnem);
                            case 2 -> e.codigoObjeto = generarF2(mnem, e.operando);
                            case 3 -> {
                                if (mnem.equals("RSUB") && e.operando.isBlank()) {
                                    e.codigoObjeto = ensamblarF3(CODOP.get("RSUB") >> 2, 1, 1, 0, 0, 0, 0);
                                } else if (mnem.equals("RSUB") && !e.operando.isBlank()) {
                                    registrarErrorP2(e, "RSUB no acepta operando");
                                } else {
                                    e.codigoObjeto = generarF3(e, mnem, pc);
                                }
                            }
                            case 4 -> e.codigoObjeto = generarF4(e, mnem);
                            default -> registrarErrorP2(e, "Formato desconocido: " + e.formato);
                        }
                    }
                }
            } catch (Exception ex) {
                registrarErrorP2(e, "Error interno: " + ex.getMessage());
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // FORMATO 1
    // ═════════════════════════════════════════════════════════════════
    private String generarF1(String mnem) {
        return String.format("%02X", CODOP.get(mnem));
    }

    // ═════════════════════════════════════════════════════════════════
    // FORMATO 2
    // ═════════════════════════════════════════════════════════════════
    private String generarF2(String mnem, String operando) throws Exception {
        int op = CODOP.get(mnem);
        int r1 = 0, r2 = 0;
        String[] p = operando.split(",");

        if (mnem.equals("SVC")) {
            r1 = parsearNumero(p[0].trim()); r2 = 0;
        } else if (F2_SHIFT.contains(mnem)) {
            r1 = obtenerReg(p[0].trim()); r2 = parsearNumero(p[1].trim()) - 1;
        } else if (F2_UN_REG.contains(mnem)) {
            r1 = obtenerReg(p[0].trim()); r2 = 0;
        } else {
            r1 = obtenerReg(p[0].trim()); r2 = obtenerReg(p[1].trim());
        }
        return String.format("%04X", (op << 8) | (r1 << 4) | r2);
    }

    // ═════════════════════════════════════════════════════════════════
    // FORMATO 3 — con soporte de expresiones en operando
    // ═════════════════════════════════════════════════════════════════
    private String generarF3(EntradaListado e, String mnem, int pc) throws Exception {
        int op  = CODOP.get(mnem);
        int op6 = op >> 2;

        // Extraer modo y expresión limpia
        String opr = e.operando.trim();
        int n = 1, i = 1, x = 0;

        if (opr.toUpperCase().endsWith(",X")) {
            x   = 1;
            opr = opr.substring(0, opr.length() - 2).trim();
        }
        if      (opr.startsWith("#")) { n = 0; i = 1; opr = opr.substring(1).trim(); }
        else if (opr.startsWith("@")) { n = 1; i = 0; opr = opr.substring(1).trim(); }

        // Evaluar la expresión del operando (Paso 2: todos los símbolos conocidos)
        EvalResult ev = evaluarOperandoP2(opr);
        if (ev.error != null) {
            registrarErrorP2(e, ev.error);
            return ensamblarF3(op6, n, i, x, 1, 1, 0xFFF);
        }

        int    ta   = ev.valor;
        String tipo = ev.tipo;   // A o R

        int b = 0, p = 0, disp = 0;

        if (tipo.equals("A")) {
            // Expresión ABSOLUTA
            // Caso 1: negativo → fuera de rango siempre (error)
            if (ta < 0) {
                registrarErrorP2(e, "Operando fuera de rango: valor absoluto negativo (" + ta + ") no tiene dirección válida");
                return ensamblarF3(op6, n, i, x, 1, 1, 0xFFF);
            }
            // Caso 2: 0 ≤ ta ≤ 4095 → dirección directa (b=0, p=0)
            //   Funciona para todos los modos: simple, inmediato, indirecto, indexado
            if (ta <= 4095) {
                b = 0; p = 0; disp = ta;
            } else {
                // Caso 3: ta > 4095 → tratar como dirección, PC/BASE relativo
                ResultadoDisp rd = calcularDispF3(e, ta, pc);
                b = rd.b; p = rd.p; disp = rd.disp;
            }
        } else {
            // Expresión RELATIVA → dirección de memoria, PC/BASE relativo
            ResultadoDisp rd = calcularDispF3(e, ta, pc);
            b = rd.b; p = rd.p; disp = rd.disp;
        }

        return ensamblarF3(op6, n, i, x, b, p, disp);
    }

    // ═════════════════════════════════════════════════════════════════
    // FORMATO 4 — con soporte de expresiones en operando
    // ═════════════════════════════════════════════════════════════════
    private String generarF4(EntradaListado e, String mnem) throws Exception {
        int op  = CODOP.get(mnem);
        int op6 = op >> 2;

        String opr = e.operando.trim();
        int n = 1, i = 1, x = 0;

        if (opr.toUpperCase().endsWith(",X")) {
            x   = 1;
            opr = opr.substring(0, opr.length() - 2).trim();
        }
        if      (opr.startsWith("#")) { n = 0; i = 1; opr = opr.substring(1).trim(); }
        else if (opr.startsWith("@")) { n = 1; i = 0; opr = opr.substring(1).trim(); }

        EvalResult ev = evaluarOperandoP2(opr);
        if (ev.error != null) {
            registrarErrorP2(e, ev.error);
            int byte1e = (op6 << 2) | (n << 1) | i;
            int byte2e = (x << 7) | (1 << 6) | (1 << 5) | (1 << 4);
            return String.format("%02X%02X%02X%02X", byte1e, byte2e, 0xFF, 0xFF);
        }

        int    dir  = ev.valor;
        String tipo = ev.tipo;

        boolean hayError = false;
        // F4 exige "m" (relativo o número grande); si es absoluto y pequeño, error
        if (tipo.equals("A") && dir <= 4095 && dir >= 0) {
            registrarErrorP2(e, "Modo de direccionamiento no existe: valor absoluto " + dir + " ≤ 4095 en Formato 4");
            dir = 0xFFFFF;
            hayError = true;
        }

        int b = hayError ? 1 : 0;
        int p = hayError ? 1 : 0;

        int byte1 = (op6 << 2) | (n << 1) | i;
        int byte2 = (x << 7) | (b << 6) | (p << 5) | (1 << 4) | ((dir >> 16) & 0xF);
        int byte3 = (dir >> 8) & 0xFF;
        int byte4 = dir & 0xFF;
        String cod = String.format("%02X%02X%02X%02X", byte1, byte2, byte3, byte4);

        // Registro M: solo si el tipo es relativo (expresión relativa o símbolo puro R)
        if (!hayError && tipo.equals("R")) {
            String nom6 = obtenerNombrePrograma();
            simbolosRelocalizables.add(opr);
            // Se agrega en escribirObj con acceso al nombre correcto
        }

        return cod;
    }

    // ═════════════════════════════════════════════════════════════════
    // WORD con expresión — evalúa en Paso 2, tipo puede ser A o R
    // ═════════════════════════════════════════════════════════════════
    private String generarWord(EntradaListado e) throws Exception {
        String opr = e.operando.trim();
        EvalResult ev = evaluarOperandoP2(opr);
        if (ev.error != null) {
            registrarErrorP2(e, ev.error);
            return "FFFFFF";
        }
        // Guardar el tipo en la entrada para que escribirObj sepa si genera M
        e.tipoExpresion = ev.tipo;
        return String.format("%06X", ev.valor & 0xFFFFFF);
    }

    // ═════════════════════════════════════════════════════════════════
    // EVALUADOR DE EXPRESIONES — Paso 2 (todos los símbolos disponibles)
    // Parsea la expresión como string y la evalúa directamente.
    // ═════════════════════════════════════════════════════════════════

    /** Resultado de evaluar un operando en Paso 2 */
    static class EvalResult {
        final int    valor;
        final String tipo;   // "A" o "R"
        final String error;  // null si OK
        EvalResult(int v, String t, String err) { valor = v; tipo = t; error = err; }
        static EvalResult ok(int v, String t)   { return new EvalResult(v, t, null); }
        static EvalResult err(String msg)       { return new EvalResult(0, "A", msg); }
    }

    /**
     * Evalúa una expresión dada como string usando tablaSimbolos (Paso 2).
     * Soporta: números, símbolos, +, -, *, /, paréntesis.
     * Respeta reglas de tipos A/R.
     */
    EvalResult evaluarOperandoP2(String expr) {
        expr = expr.trim();
        if (expr.isEmpty()) return EvalResult.err("Expresión vacía");
        try {
            ExprParser ep = new ExprParser(expr);
            AnalizadorSemantico.ResultadoExpr r = ep.parse();
            // Validar netoR final
            if (r.netoR == 0)      return EvalResult.ok(r.valor, "A");
            if (r.netoR == 1)      return EvalResult.ok(r.valor, "R");
            if (r.netoR == -1)     return EvalResult.err("Expresión inválida: término relativo con signo negativo no apareado");
            if (r.netoR > 1)       return EvalResult.err("Expresión inválida: " + r.netoR + " términos relativos positivos sin aparear");
            return EvalResult.err("Expresión inválida: " + (-r.netoR) + " términos relativos negativos sin aparear");
        } catch (Exception ex) {
            return EvalResult.err(ex.getMessage());
        }
    }

    /**
     * Mini evaluador recursivo de expresiones para Paso 2.
     * Trabaja directamente sobre el string.
     */
    private class ExprParser {
        private final String input;
        private int pos = 0;

        ExprParser(String input) {
            this.input = input.trim();
        }

        AnalizadorSemantico.ResultadoExpr parse() throws Exception {
            AnalizadorSemantico.ResultadoExpr r = parseExpr();
            skipSpaces();
            if (pos < input.length())
                throw new Exception("Carácter inesperado en posición " + pos + ": '" + input.charAt(pos) + "'");
            return r;
        }

        AnalizadorSemantico.ResultadoExpr parseExpr() throws Exception {
            int valorTotal    = 0;
            int netoRelativos = 0;

            AnalizadorSemantico.ResultadoExpr primero = parseTerm();
            valorTotal    += primero.valor;
            netoRelativos += primero.netoR;

            skipSpaces();
            while (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
                char op = input.charAt(pos++);
                AnalizadorSemantico.ResultadoExpr r = parseTerm();
                int signo = op == '+' ? 1 : -1;
                valorTotal    += signo * r.valor;
                netoRelativos += signo * r.netoR;
                skipSpaces();
            }

            return new AnalizadorSemantico.ResultadoExpr(valorTotal, netoRelativos);
        }

        AnalizadorSemantico.ResultadoExpr parseTerm() throws Exception {
            AnalizadorSemantico.ResultadoExpr acum = parseFactor();
            skipSpaces();
            while (pos < input.length() && (input.charAt(pos) == '*' || input.charAt(pos) == '/')) {
                char op = input.charAt(pos++);
                AnalizadorSemantico.ResultadoExpr r = parseFactor();
                acum = aplicarMulDiv(acum, String.valueOf(op), r);
                skipSpaces();
            }
            return acum;
        }

        AnalizadorSemantico.ResultadoExpr parseFactor() throws Exception {
            skipSpaces();
            if (pos >= input.length()) throw new Exception("Se esperaba factor en posición " + pos);
            char c = input.charAt(pos);

            // Paréntesis
            if (c == '(') {
                pos++;
                AnalizadorSemantico.ResultadoExpr r = parseExpr();
                skipSpaces();
                if (pos >= input.length() || input.charAt(pos) != ')')
                    throw new Exception("Falta ')' en expresión");
                pos++;
                return r;
            }

            // Negación unaria: -factor → invierte valor; R se convierte en RN
            if (c == '-') {
                pos++;
                AnalizadorSemantico.ResultadoExpr inner = parseFactor();
                String nuevoTipo = inner.tipo.equals("R") ? "RN" : inner.tipo.equals("RN") ? "R" : inner.tipo;
                return new AnalizadorSemantico.ResultadoExpr(-inner.valor, -inner.netoR);
            }

            // Identificador o número
            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos))))
                    sb.append(input.charAt(pos++));
                String id = sb.toString().toUpperCase();
                if (!tablaSimbolos.containsKey(id))
                    throw new Exception("Símbolo no encontrado en TABSIM: '" + id + "'");
                int    val  = symAbsoluto(id);
                String tipo = tipoSimbolos.getOrDefault(id, "A");
                return new AnalizadorSemantico.ResultadoExpr(val, tipo);

            } else if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos))))
                    sb.append(input.charAt(pos++));
                String numStr = sb.toString().toUpperCase();
                int val;
                if (numStr.endsWith("H"))
                    val = Integer.parseInt(numStr.substring(0, numStr.length()-1), 16);
                else
                    val = Integer.parseInt(numStr);
                return new AnalizadorSemantico.ResultadoExpr(val, "A");
            }

            throw new Exception("Carácter no esperado '" + c + "' en posición " + pos);
        }

        void skipSpaces() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
        }

        AnalizadorSemantico.ResultadoExpr aplicarSumaResta(
                AnalizadorSemantico.ResultadoExpr a, String op,
                AnalizadorSemantico.ResultadoExpr b) throws Exception {
            int resultado = op.equals("+") ? a.valor + b.valor : a.valor - b.valor;
            String ta = a.tipo, tb = b.tipo;
            // Normalizar RN
            if (tb.equals("RN")) {
                String opEfectivo = op.equals("+") ? "-" : "+";
                tb = "R";
                return determinarTipo(resultado, ta, opEfectivo, tb);
            }
            if (ta.equals("RN")) ta = "R";
            return determinarTipo(resultado, ta, op, tb);
        }

        AnalizadorSemantico.ResultadoExpr determinarTipo(int resultado, String ta, String op, String tb)
                throws Exception {
            if (ta.equals("A") && tb.equals("A")) return new AnalizadorSemantico.ResultadoExpr(resultado, "A");
            if (ta.equals("R") && tb.equals("R") && op.equals("-")) return new AnalizadorSemantico.ResultadoExpr(resultado, "A");
            if (ta.equals("R") && tb.equals("A")) return new AnalizadorSemantico.ResultadoExpr(resultado, "R");
            if (ta.equals("A") && tb.equals("R") && op.equals("+")) return new AnalizadorSemantico.ResultadoExpr(resultado, "R");
            if (ta.equals("A") && tb.equals("R") && op.equals("-"))
                throw new Exception("Expresión inválida: A - R produce relativo negativo no apareado");
            if (ta.equals("R") && tb.equals("R") && op.equals("+"))
                throw new Exception("Expresión inválida: R + R — dos relativos con mismo signo");
            throw new Exception("Combinación de tipos no soportada: " + ta + " " + op + " " + tb);
        }

        AnalizadorSemantico.ResultadoExpr aplicarMulDiv(
                AnalizadorSemantico.ResultadoExpr a, String op,
                AnalizadorSemantico.ResultadoExpr b) throws Exception {
            if (a.netoR != 0 || b.netoR != 0)
                throw new Exception("Expresión inválida: términos relativos no pueden usarse en * o /");
            int va = a.valor, vb = b.valor;
            if (op.equals("/") && vb == 0) throw new Exception("División por cero");
            int resultado = op.equals("*") ? va * vb : va / vb;
            return new AnalizadorSemantico.ResultadoExpr(resultado, 0);
        }
    }

    private ResultadoDisp calcularDispF3(EntradaListado e, int ta, int pc) {
        int dispPC = ta - pc;
        if (dispPC >= -2048 && dispPC <= 2047)
            return new ResultadoDisp(0, 1, dispPC);
        int dispB = ta - baseVal;
        if (dispB >= 0 && dispB <= 4095)
            return new ResultadoDisp(1, 0, dispB);
        registrarErrorP2(e,
            "Operando fuera de rango: TA=" + String.format("%04X", ta)
            + " relPC=" + dispPC + " relB=" + dispB);
        return new ResultadoDisp(1, 1, 0xFFF);
    }

    private String ensamblarF3(int op6, int n, int i, int x, int b, int p, int disp) {
        int disp12 = disp & 0xFFF;
        int byte1  = (op6 << 2) | (n << 1) | i;
        int byte2  = (x << 7) | (b << 6) | (p << 5) | (0 << 4) | ((disp12 >> 8) & 0xF);
        int byte3  = disp12 & 0xFF;
        return String.format("%02X%02X%02X", byte1, byte2, byte3);
    }

    private static class ResultadoDisp {
        final int b, p, disp;
        ResultadoDisp(int b, int p, int disp) { this.b = b; this.p = p; this.disp = disp; }
    }

    // ═════════════════════════════════════════════════════════════════
    // DIRECTIVAS — BYTE y WORD
    // ═════════════════════════════════════════════════════════════════
    private String generarByte(String operando) throws Exception {
        operando = operando.trim();
        if (operando.toUpperCase().startsWith("C'")) {
            String texto = operando.substring(2, operando.length() - 1);
            StringBuilder sb = new StringBuilder();
            for (char c : texto.toCharArray())
                sb.append(String.format("%02X", (int) c));
            return sb.toString();
        } else if (operando.toUpperCase().startsWith("X'")) {
            String hex = operando.substring(2, operando.length() - 1).toUpperCase();
            if (hex.length() % 2 != 0) hex = "0" + hex;
            return hex;
        }
        throw new Exception("BYTE con formato desconocido: " + operando);
    }

    // ═════════════════════════════════════════════════════════════════
    // RESOLUCIÓN DE DIRECCIÓN DE ENTRADA (END)
    // ═════════════════════════════════════════════════════════════════
    private int obtenerPrimeraInstrEjecutable() {
        for (EntradaListado e : listado)
            if (e.formato >= 1 && e.formato <= 4 && e.error.isBlank())
                return e.contloc;
        return obtenerDirInicio();
    }

    private int obtenerDirInicio() {
        for (EntradaListado e : listado)
            if (e.instruccion.equals("START"))
                try { return parsearNumero(e.operando); } catch (Exception ex) {}
        return 0;
    }

    private void resolverDirEntrada() {
        for (EntradaListado e : listado) {
            if (!e.instruccion.equals("END")) continue;
            if (e.operando.isBlank()) {
                dirEntrada = obtenerPrimeraInstrEjecutable();
                return;
            }
            // Intentar evaluar la expresión del operando de END
            EvalResult ev = evaluarOperandoP2(e.operando.trim());
            if (ev.error != null) {
                dirEntrada = 0xFFFFFF;
                errorDirEntrada = true;
                registrarErrorP2(e, "Error en operando de END '" + e.operando + "': " + ev.error);
            } else {
                dirEntrada = ev.valor;
            }
            return;
        }
        dirEntrada = obtenerDirInicio();
    }

    // ═════════════════════════════════════════════════════════════════
    // GENERAR ARCHIVO OBJETO — H / T / M / E
    // ═════════════════════════════════════════════════════════════════
    public void escribirObj(String rutaObj, String nombreProg,
                             int dirInicio, int longitud) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaObj))) {
            String nom6 = nombreProg.length() > 6
                        ? nombreProg.substring(0, 6) : nombreProg;
            String regH = String.format("H%-6s%06X%06X", nom6, dirInicio, longitud);
            pw.println(regH);
            registros.add(regH);

            StringBuilder buf    = new StringBuilder();
            List<String>  regM   = new ArrayList<>();
            int           tDir   = -1;
            int           tBytes = 0;
            int           dirSig = -1;

            for (EntradaListado e : listado) {
                boolean errorP1SinCodigo = !e.error.isBlank() && e.codigoObjeto.isBlank();
                if (errorP1SinCodigo || e.codigoObjeto.isBlank()) continue;

                int dirAbs     = dirAbsoluta(e);
                int bytesLinea = e.codigoObjeto.length() / 2;

                boolean hayHueco = (dirSig != -1) && (dirAbs != dirSig);
                if (tBytes > 0 && (tBytes + bytesLinea > 30 || hayHueco)) {
                    escribirRegistroT(pw, tDir, tBytes, buf.toString());
                    buf.setLength(0); tBytes = 0; tDir = -1;
                }

                if (tDir == -1) tDir = dirAbs;
                buf.append(e.codigoObjeto);
                tBytes += bytesLinea;
                dirSig  = dirAbs + bytesLinea;

                // ── Registro M para F4 con resultado relativo ──────────
                if (e.formato == 4 && e.errorP2.isBlank()) {
                    String opr = e.operando.trim();
                    if (opr.toUpperCase().endsWith(",X")) opr = opr.substring(0, opr.length()-2).trim();
                    if (opr.startsWith("#") || opr.startsWith("@")) opr = opr.substring(1).trim();
                    EvalResult ev = evaluarOperandoP2(opr);
                    if (ev.error == null && ev.tipo.equals("R")) {
                        regM.add(String.format("M%06X05+%s", dirAbs + 1, nom6));
                        registros.add(String.format("M%06X05+%s", dirAbs + 1, nom6));
                        simbolosRelocalizables.add(opr);
                    }
                }

                // ── Registro M para WORD con resultado relativo ────────
                if (e.instruccion.equals("WORD") && e.errorP2.isBlank()
                        && "R".equals(e.tipoExpresion)) {
                    registros.add(String.format("M%06X06+%s", dirAbs, nom6));
                    regM.add(String.format("M%06X06+%s", dirAbs, nom6));
                }
            }
            if (tBytes > 0) escribirRegistroT(pw, tDir, tBytes, buf.toString());

            for (String m : regM) pw.println(m);

            String regE = String.format("E%06X", dirEntrada & 0xFFFFFF);
            pw.println(regE);
            registros.add(regE);
        }
    }

    private void escribirRegistroT(PrintWriter pw, int dir, int bytes, String cod) {
        String regT = String.format("T%06X%02X%s", dir, bytes, cod);
        pw.println(regT);
        registros.add(regT);
    }

    //funcion para devolver los registros objeto e imprimir en la ventana 
    public List<String> getRegistrosObjeto() {
        return registros;
    }

    // ═════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═════════════════════════════════════════════════════════════════
    private int obtenerReg(String nombre) throws Exception {
        nombre = nombre.toUpperCase();
        if (!REG.containsKey(nombre))
            throw new Exception("Registro no válido: '" + nombre + "'");
        return REG.get(nombre);
    }

    private int parsearNumero(String valor) throws Exception {
        if (valor == null || valor.isBlank()) throw new Exception("Operando vacío");
        valor = valor.trim().toUpperCase();
        if (valor.endsWith("H"))
            return Integer.parseInt(valor.substring(0, valor.length() - 1), 16);
        return Integer.parseInt(valor);
    }

    private void registrarErrorP2(EntradaListado e, String msg) {
        e.errorP2 = msg;
        errores.add(String.format("x Error P2 en línea %d [%s %s]: %s",
            e.numeroLinea, e.instruccion, e.operando, msg));
    }

    private String obtenerNombrePrograma() {
        for (EntradaListado e : listado)
            if (e.instruccion.equals("START"))
                return e.etiqueta.isBlank() ? "PROG" : e.etiqueta;
        return "PROG";
    }
}