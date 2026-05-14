package sicxe;

import java.io.*;
import java.util.*;

/**
 * Paso 2 del ensamblador SIC/XE — Generación de código objeto por sección de control.
 *
 * Novedades (secciones de control):
 *  - Instancia independiente por SeccionControl
 *  - Registros D (EXTDEF) y R (EXTREF) en el orden de aparición en el fuente
 *  - Registro M con tag *R para referencias relocalizables internas
 *  - Registro M con tag *SE para referencias a símbolos externos
 *  - Expresiones con símbolos externos: valor=0, M record *SE por cada externo
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
    private static final Set<String> F2_SHIFT   = new HashSet<>(Arrays.asList("SHIFTL", "SHIFTR"));

    // ── Datos del Paso 1 (de la sección activa) ───────────────────────
    private final SeccionControl            seccion;
    private final List<EntradaListado>      listado;
    private final Map<String, Integer>      tablaSimbolos;
    private final Map<String, String>       tipoSimbolos;
    private final Map<String, Integer>      bloqueSimbolos;
    private       int                       baseVal;
    private final String                    simboloBase;
    private final List<int[]>               tablaBloques;
    private final int                       direccionIni;

    private final List<String>  errores   = new ArrayList<>();
    private final List<String>  registros = new ArrayList<>();

    private int     dirEntrada      = 0;
    private boolean errorDirEntrada = false;

    // Sólo se usa en la sección principal para buscar END en otras secciones
    private List<SeccionControl> todasSecciones = null;

    public void setTodasSecciones(List<SeccionControl> todas) {
        this.todasSecciones = todas;
    }

    // ── Constructor que acepta una SeccionControl ─────────────────────
    public GeneradorCodigo(SeccionControl sec) {
        this.seccion        = sec;
        this.listado        = sec.listado;
        this.tablaSimbolos  = sec.tablaSimbolos;
        this.tipoSimbolos   = sec.tipoSimbolos;
        this.bloqueSimbolos = sec.bloqueSimbolos;
        this.baseVal        = sec.valorBase;
        this.simboloBase    = sec.simboloBase;
        this.tablaBloques   = sec.tablaBloques;
        this.direccionIni   = sec.direccionInicio;
    }

    // ── Getters ───────────────────────────────────────────────────────
    public List<String>  getErrores()          { return errores; }
    public List<String>  getRegistrosObjeto()  { return registros; }
    public int           getDirEntrada()        { return dirEntrada; }
    public boolean       isErrorDirEntrada()    { return errorDirEntrada; }

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
                    && !err.contains("no existe") && !err.contains("no definida")
                    && !err.contains("no reconocida") && !err.contains("de sintaxis")
                    && !err.contains("instrucción no existe");
                if (!soloEsDuplicado) continue;
            }

            // PC = dirección de la siguiente instrucción en memoria
            // = dirección absoluta de esta instrucción + su tamaño en bytes
            // NO se usa listado[idx+1] porque ese entry puede estar en otro bloque,
            // lo que daría dirAbsoluta(bloque_distinto) = inicio de ese bloque (incorrecto).
            int pc = dirAbsoluta(e) + (e.formato > 0 ? e.formato : 0);

            String ins  = e.instruccion.trim();
            String mnem = ins.startsWith("+") ? ins.substring(1) : ins;

            try {
                switch (ins) {
                    case "BYTE"  -> e.codigoObjeto = generarByte(e.operando);
                    case "WORD"  -> e.codigoObjeto = generarWord(e);
                    case "START","END","RESB","RESW","BASE","EQU","ORG","USE",
                         "CSECT","EXTDEF","EXTREF" -> { /* sin código */ }
                    default -> {
                        if (!CODOP.containsKey(mnem)) {
                            registrarErrorP2(e, "Sin código de operación: " + mnem);
                            continue;
                        }
                        switch (e.formato) {
                            case 1 -> e.codigoObjeto = generarF1(mnem);
                            case 2 -> e.codigoObjeto = generarF2(mnem, e.operando);
                            case 3 -> {
                                if (mnem.equals("RSUB") && e.operando.isBlank())
                                    e.codigoObjeto = ensamblarF3(CODOP.get("RSUB") >> 2, 1,1,0,0,0,0);
                                else if (mnem.equals("RSUB") && !e.operando.isBlank())
                                    registrarErrorP2(e, "RSUB no acepta operando");
                                else
                                    e.codigoObjeto = generarF3(e, mnem, pc);
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
    // FORMATOS 1, 2
    // ═════════════════════════════════════════════════════════════════
    private String generarF1(String mnem) {
        return String.format("%02X", CODOP.get(mnem));
    }

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
    // FORMATO 3
    // ═════════════════════════════════════════════════════════════════
    private String generarF3(EntradaListado e, String mnem, int pc) throws Exception {
        int op6 = CODOP.get(mnem) >> 2;
        String opr = e.operando.trim();
        int n = 1, i = 1, x = 0;

        if (opr.toUpperCase().endsWith(",X")) { x = 1; opr = opr.substring(0, opr.length()-2).trim(); }
        if      (opr.startsWith("#")) { n = 0; i = 1; opr = opr.substring(1).trim(); }
        else if (opr.startsWith("@")) { n = 1; i = 0; opr = opr.substring(1).trim(); }

        EvalResult ev = evaluarOperandoP2(opr);
        if (ev.error != null) {
            registrarErrorP2(e, ev.error);
            return ensamblarF3(op6, n, i, x, 1, 1, 0xFFF);
        }

        int    ta   = ev.valor;
        String tipo = ev.tipo();

        int b = 0, p = 0, disp = 0;
        if (tipo.equals("A")) {
            if (ta < 0) {
                registrarErrorP2(e, "Operando fuera de rango: valor absoluto negativo (" + ta + ")");
                return ensamblarF3(op6, n, i, x, 1, 1, 0xFFF);
            }
            if (ta <= 4095) { b = 0; p = 0; disp = ta; }
            else {
                ResultadoDisp rd = calcularDispF3(e, ta, pc);
                b = rd.b; p = rd.p; disp = rd.disp;
            }
        } else {
            // tipo R o EX
            ResultadoDisp rd = calcularDispF3(e, ta, pc);
            b = rd.b; p = rd.p; disp = rd.disp;
        }

        return ensamblarF3(op6, n, i, x, b, p, disp);
    }

    // ═════════════════════════════════════════════════════════════════
    // FORMATO 4
    // ═════════════════════════════════════════════════════════════════
    private String generarF4(EntradaListado e, String mnem) throws Exception {
        int op6 = CODOP.get(mnem) >> 2;
        String opr = e.operando.trim();
        int n = 1, i = 1, x = 0;

        if (opr.toUpperCase().endsWith(",X")) { x = 1; opr = opr.substring(0, opr.length()-2).trim(); }
        if      (opr.startsWith("#")) { n = 0; i = 1; opr = opr.substring(1).trim(); }
        else if (opr.startsWith("@")) { n = 1; i = 0; opr = opr.substring(1).trim(); }

        EvalResult ev = evaluarOperandoP2(opr);
        if (ev.error != null) {
            registrarErrorP2(e, ev.error);
            int b1 = (op6 << 2) | (n << 1) | i;
            int b2 = (x << 7) | (1 << 6) | (1 << 5) | (1 << 4);
            return String.format("%02X%02X%02X%02X", b1, b2, 0xFF, 0xFF);
        }

        int dir = ev.valor;

        // F4 con expresión absolutamente pequeña solo sin externos
        if (ev.tipo().equals("A") && dir >= 0 && dir <= 4095 && !ev.tieneExternos()) {
            registrarErrorP2(e, "Modo de direccionamiento no existe: valor absoluto " + dir + " ≤ 4095 en Formato 4");
            dir = 0xFFFFF;
        }

        int byte1 = (op6 << 2) | (n << 1) | i;
        int byte2 = (x << 7) | (0 << 6) | (0 << 5) | (1 << 4) | ((dir >> 16) & 0xF);
        int byte3 = (dir >> 8) & 0xFF;
        int byte4 =  dir        & 0xFF;

        // Actualizar tipoExpresion para la GUI
        if (ev.tieneExternos()) e.tipoExpresion = "EX";

        return String.format("%02X%02X%02X%02X", byte1, byte2, byte3, byte4);
    }

    // ═════════════════════════════════════════════════════════════════
    // WORD con expresión
    // ═════════════════════════════════════════════════════════════════
    private String generarWord(EntradaListado e) throws Exception {
        String opr = e.operando.trim();
        EvalResult ev = evaluarOperandoP2(opr);
        if (ev.error != null) {
            registrarErrorP2(e, ev.error);
            return "FFFFFF";
        }
        if (ev.tieneExternos()) e.tipoExpresion = "EX";
        else                    e.tipoExpresion = ev.tipo();
        return String.format("%06X", ev.valor & 0xFFFFFF);
    }

    // ═════════════════════════════════════════════════════════════════
    // EVALUADOR DE EXPRESIONES — Paso 2
    // ═════════════════════════════════════════════════════════════════

    static class EvalResult {
        final int    valor;
        final int    netoR;   // relativos internos sin parear
        final String error;
        final List<AnalizadorSemantico.TermExterno> externos;

        EvalResult(int v, int nr, String e, List<AnalizadorSemantico.TermExterno> ext) {
            valor = v; netoR = nr; error = e;
            externos = ext != null ? ext : new ArrayList<>();
        }

        static EvalResult ok(int v, int nr, List<AnalizadorSemantico.TermExterno> ext) {
            return new EvalResult(v, nr, null, ext);
        }
        static EvalResult err(String msg) {
            return new EvalResult(0, 0, msg, new ArrayList<>());
        }

        boolean tieneExternos() { return !externos.isEmpty(); }

        String tipo() {
            if (error != null) return "A";
            if (!externos.isEmpty()) return "EX";
            return netoR == 0 ? "A" : "R";
        }
    }

    EvalResult evaluarOperandoP2(String expr) {
        expr = expr.trim();
        if (expr.isEmpty()) return EvalResult.err("Expresión vacía");
        try {
            ExprParser ep = new ExprParser(expr);
            AnalizadorSemantico.ResultadoExpr r = ep.parse();
            List<AnalizadorSemantico.TermExterno> ext = r.externos;

            if (ext.isEmpty()) {
                // Solo términos locales: aplicar reglas A/R
                if (r.netoR == 0)  return EvalResult.ok(r.valor, 0, ext);
                if (r.netoR == 1)  return EvalResult.ok(r.valor, 1, ext);
                if (r.netoR == -1) return EvalResult.err("Expresión inválida: relativo negativo no apareado");
                if (r.netoR > 1)   return EvalResult.err("Expresión inválida: " + r.netoR + " relativos positivos sin aparear");
                return EvalResult.err("Expresión inválida: " + (-r.netoR) + " relativos negativos sin aparear");
            }
            // Con externos: el netoR solo refleja los relativos internos; los externos van aparte
            return EvalResult.ok(r.valor, r.netoR, ext);
        } catch (Exception ex) {
            return EvalResult.err(ex.getMessage());
        }
    }

    private class ExprParser {
        private final String input;
        private int pos = 0;

        ExprParser(String input) { this.input = input.trim(); }

        AnalizadorSemantico.ResultadoExpr parse() throws Exception {
            AnalizadorSemantico.ResultadoExpr r = parseExpr();
            skipSpaces();
            if (pos < input.length())
                throw new Exception("Carácter inesperado en posición " + pos + ": '" + input.charAt(pos) + "'");
            return r;
        }

        AnalizadorSemantico.ResultadoExpr parseExpr() throws Exception {
            int valorTotal = 0, netoRelativos = 0;
            List<AnalizadorSemantico.TermExterno> todosExt = new ArrayList<>();

            AnalizadorSemantico.ResultadoExpr primero = parseTerm();
            valorTotal    += primero.valor;
            netoRelativos += primero.netoR;
            agregarExt(todosExt, primero.externos, 1);

            skipSpaces();
            while (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
                char op = input.charAt(pos++);
                AnalizadorSemantico.ResultadoExpr r = parseTerm();
                int signo = op == '+' ? 1 : -1;
                valorTotal    += signo * r.valor;
                netoRelativos += signo * r.netoR;
                agregarExt(todosExt, r.externos, signo);
                skipSpaces();
            }
            return new AnalizadorSemantico.ResultadoExpr(valorTotal, netoRelativos, todosExt);
        }

        AnalizadorSemantico.ResultadoExpr parseTerm() throws Exception {
            AnalizadorSemantico.ResultadoExpr acum = parseFactor();
            skipSpaces();
            while (pos < input.length() && (input.charAt(pos) == '*' || input.charAt(pos) == '/')) {
                char op = input.charAt(pos++);
                AnalizadorSemantico.ResultadoExpr r = parseFactor();
                if (acum.netoR != 0 || r.netoR != 0)
                    throw new Exception("Expresión inválida: relativos no pueden usarse en * o /");
                if (acum.tieneExternos() || r.tieneExternos())
                    throw new Exception("Expresión inválida: externos no pueden usarse en * o /");
                int va = acum.valor, vb = r.valor;
                if (op == '/' && vb == 0) throw new Exception("División por cero");
                acum = new AnalizadorSemantico.ResultadoExpr(op == '*' ? va*vb : va/vb, 0);
                skipSpaces();
            }
            return acum;
        }

        AnalizadorSemantico.ResultadoExpr parseFactor() throws Exception {
            skipSpaces();
            if (pos >= input.length()) throw new Exception("Se esperaba factor en posición " + pos);
            char c = input.charAt(pos);

            if (c == '(') {
                pos++;
                AnalizadorSemantico.ResultadoExpr r = parseExpr();
                skipSpaces();
                if (pos >= input.length() || input.charAt(pos) != ')')
                    throw new Exception("Falta ')' en expresión");
                pos++;
                return r;
            }

            if (c == '-') {
                pos++;
                AnalizadorSemantico.ResultadoExpr inner = parseFactor();
                List<AnalizadorSemantico.TermExterno> negados = new ArrayList<>();
                for (var te : inner.externos)
                    negados.add(new AnalizadorSemantico.TermExterno(te.nombre, -te.signo));
                return new AnalizadorSemantico.ResultadoExpr(-inner.valor, -inner.netoR, negados);
            }

            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos)))
                    sb.append(input.charAt(pos++));
                String id = sb.toString().toUpperCase();
                if (!tablaSimbolos.containsKey(id))
                    throw new Exception("Símbolo no encontrado en TABSIM: '" + id + "'");
                String tipo = tipoSimbolos.getOrDefault(id, "A");
                // Símbolo externo (EXTREF)
                if (tipo.equals("E")) {
                    List<AnalizadorSemantico.TermExterno> ext = new ArrayList<>();
                    ext.add(new AnalizadorSemantico.TermExterno(id, 1));
                    return new AnalizadorSemantico.ResultadoExpr(0, 0, ext);
                }
                int val = symAbsoluto(id);
                return new AnalizadorSemantico.ResultadoExpr(val, tipo);
            }

            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos)))
                    sb.append(input.charAt(pos++));
                String numStr = sb.toString().toUpperCase();
                int val = numStr.endsWith("H")
                    ? Integer.parseInt(numStr.substring(0, numStr.length()-1), 16)
                    : Integer.parseInt(numStr);
                return new AnalizadorSemantico.ResultadoExpr(val, "A");
            }

            throw new Exception("Carácter no esperado '" + c + "' en posición " + pos);
        }

        void skipSpaces() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
        }

        private void agregarExt(List<AnalizadorSemantico.TermExterno> dest,
                                  List<AnalizadorSemantico.TermExterno> src, int signo) {
            for (var te : src)
                dest.add(new AnalizadorSemantico.TermExterno(te.nombre, te.signo * signo));
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // CÁLCULO DE DESPLAZAMIENTO F3
    // ═════════════════════════════════════════════════════════════════
    private ResultadoDisp calcularDispF3(EntradaListado e, int ta, int pc) {
        int dispPC = ta - pc;
        if (dispPC >= -2048 && dispPC <= 2047) return new ResultadoDisp(0, 1, dispPC);
        int dispB = ta - baseVal;
        if (dispB >= 0 && dispB <= 4095)       return new ResultadoDisp(1, 0, dispB);
        registrarErrorP2(e, "Operando fuera de rango: TA=" + String.format("%04X", ta)
            + " relPC=" + dispPC + " relB=" + dispB);
        return new ResultadoDisp(1, 1, 0xFFF);
    }

    private String ensamblarF3(int op6, int n, int i, int x, int b, int p, int disp) {
        int d12   = disp & 0xFFF;
        int byte1 = (op6 << 2) | (n << 1) | i;
        int byte2 = (x << 7) | (b << 6) | (p << 5) | ((d12 >> 8) & 0xF);
        int byte3 = d12 & 0xFF;
        return String.format("%02X%02X%02X", byte1, byte2, byte3);
    }

    private static class ResultadoDisp {
        final int b, p, disp;
        ResultadoDisp(int b, int p, int disp) { this.b = b; this.p = p; this.disp = disp; }
    }

    // ═════════════════════════════════════════════════════════════════
    // BYTE
    // ═════════════════════════════════════════════════════════════════
    private String generarByte(String operando) throws Exception {
        operando = operando.trim();
        if (operando.toUpperCase().startsWith("C'")) {
            String texto = operando.substring(2, operando.length()-1);
            StringBuilder sb = new StringBuilder();
            for (char c : texto.toCharArray()) sb.append(String.format("%02X", (int) c));
            return sb.toString();
        } else if (operando.toUpperCase().startsWith("X'")) {
            String hex = operando.substring(2, operando.length()-1).toUpperCase();
            if (hex.length() % 2 != 0) hex = "0" + hex;
            return hex;
        }
        throw new Exception("BYTE con formato desconocido: " + operando);
    }

    // ═════════════════════════════════════════════════════════════════
    // RESOLUCIÓN DE DIRECCIÓN DE ENTRADA
    // ═════════════════════════════════════════════════════════════════
    private void resolverDirEntrada() {
        // Buscar END en el listado propio
        for (EntradaListado e : listado) {
            if (!e.instruccion.equals("END")) continue;
            procesarEnd(e); return;
        }
        // Si no está aquí (caso: sección principal cuando END cayó en otra sección),
        // buscarlo en todas las secciones disponibles.
        if (todasSecciones != null) {
            for (SeccionControl sec : todasSecciones) {
                for (EntradaListado e : sec.listado) {
                    if (!e.instruccion.equals("END")) continue;
                    procesarEnd(e); return;
                }
            }
        }
        dirEntrada = obtenerDirInicio();
    }

    private void procesarEnd(EntradaListado e) {
        if (e.operando.isBlank()) {
            dirEntrada = obtenerPrimeraInstrEjecutable();
            return;
        }
        String sym = e.operando.trim();
        if (tablaSimbolos.containsKey(sym)) {
            // valor en TABSIM + dirección de inicio del bloque donde se definió
            dirEntrada = symAbsoluto(sym);
        } else {
            errorDirEntrada = true;
            registrarErrorP2(e, "Símbolo de END no encontrado en tabla de la sección principal: '" + sym + "'");
            dirEntrada = obtenerDirInicio();
        }
    }

    private int obtenerPrimeraInstrEjecutable() {
        // Primero: primera instrucción en bloque 0 (el bloque que inicia con START)
        for (EntradaListado e : listado)
            if (e.noBloque == 0 && e.formato >= 1 && e.formato <= 4 && e.error.isBlank())
                return dirAbsoluta(e);
        // Fallback: primera instrucción ejecutable de cualquier bloque
        for (EntradaListado e : listado)
            if (e.formato >= 1 && e.formato <= 4 && e.error.isBlank())
                return dirAbsoluta(e);
        return obtenerDirInicio();
    }

    private int obtenerDirInicio() {
        for (EntradaListado e : listado)
            if (e.instruccion.equals("START") || e.instruccion.equals("CSECT"))
                try { return parsearNumero(e.operando.isBlank() ? "0" : e.operando); } catch (Exception ex) {}
        return 0;
    }

    // ═════════════════════════════════════════════════════════════════
    // ESCRIBIR ARCHIVO OBJETO: H, D, R, T, M, E
    // ═════════════════════════════════════════════════════════════════
    public void escribirObj(String rutaObj) throws IOException {
        String nom6 = seccion.nombre.length() > 6
            ? seccion.nombre.substring(0, 6) : seccion.nombre;
        int longitud = seccion.longitudTotal;
        int dirIni   = seccion.esPrincipal ? direccionIni : 0;

        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaObj))) {

            // ── Registro H ────────────────────────────────────────────
            String regH = String.format("H%-6s%06X%06X", nom6, dirIni, longitud);
            pw.println(regH); registros.add(regH);

            // ── Registros D / R (en el orden de aparición en el fuente)
            for (SeccionControl.DirectivaExt dr : seccion.directivasExt) {
                if (dr.esExtdef) {
                    // Hasta 6 símbolos por registro D (6 nombre + 6 dir = 12 chars * 6 = 72)
                    for (int i = 0; i < dr.simbolos.size(); i += 6) {
                        StringBuilder d = new StringBuilder("D");
                        for (int j = i; j < Math.min(i+6, dr.simbolos.size()); j++) {
                            String sym = dr.simbolos.get(j);
                            int dir = symRelativo(sym);
                            d.append(String.format("%-6s%06X", sym, dir));
                        }
                        pw.println(d); registros.add(d.toString());
                    }
                } else {
                    // Hasta 12 símbolos por registro R (6 chars * 12 = 72)
                    for (int i = 0; i < dr.simbolos.size(); i += 12) {
                        StringBuilder r = new StringBuilder("R");
                        for (int j = i; j < Math.min(i+12, dr.simbolos.size()); j++)
                            r.append(String.format("%-6s", dr.simbolos.get(j)));
                        pw.println(r); registros.add(r.toString());
                    }
                }
            }

            // ── Registros T ───────────────────────────────────────────
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
                    emitirT(pw, tDir, tBytes, buf.toString());
                    buf.setLength(0); tBytes = 0; tDir = -1;
                }
                if (tDir == -1) tDir = dirAbs;
                buf.append(e.codigoObjeto);
                tBytes += bytesLinea;
                dirSig  = dirAbs + bytesLinea;

                // ── Registros M para F4 ────────────────────────────────
                if (e.formato == 4 && e.errorP2.isBlank()) {
                    String opr = limpiarOpr(e.operando);
                    EvalResult ev = evaluarOperandoP2(opr);
                    if (ev.error == null) {
                        // Relativos internos sin parear → *R
                        if (ev.netoR == 1)
                            agregarRegM(regM, String.format("M%06X05 +%s", dirAbs+1, nom6));
                        else if (ev.netoR == -1)
                            agregarRegM(regM, String.format("M%06X05 -%s", dirAbs+1, nom6));
                        // Externos → *SE
                        for (var te : ev.externos) {
                            String sig = te.signo > 0 ? "+" : "-";
                            agregarRegM(regM, String.format("M%06X05 %s%s", dirAbs+1, sig, te.nombre));
                        }
                    }
                }

                // ── Registros M para WORD ──────────────────────────────
                if ("WORD".equals(e.instruccion) && e.errorP2.isBlank()) {
                    String opr = e.operando.trim();
                    EvalResult ev = evaluarOperandoP2(opr);
                    if (ev.error == null) {
                        if (ev.netoR == 1)
                            agregarRegM(regM, String.format("M%06X06 +%s", dirAbs, nom6));
                        else if (ev.netoR == -1)
                            agregarRegM(regM, String.format("M%06X06 -%s", dirAbs, nom6));
                        for (var te : ev.externos) {
                            String sig = te.signo > 0 ? "+" : "-";
                            agregarRegM(regM, String.format("M%06X06 %s%s", dirAbs, sig, te.nombre));
                        }
                    }
                }
            }
            if (tBytes > 0) emitirT(pw, tDir, tBytes, buf.toString());

            // ── Registros M ───────────────────────────────────────────
            for (String m : regM) pw.println(m);

            // ── Registro E ────────────────────────────────────────────
            // Sección principal: dirección del operando de END (o primera instrucción)
            // Secciones CSECT: solo "E", sin dirección
            String regE = seccion.esPrincipal
                ? String.format("E%06X", dirEntrada & 0xFFFFFF)
                : "E";
            pw.println(regE); registros.add(regE);
        }
    }

    private void emitirT(PrintWriter pw, int dir, int bytes, String cod) {
        String regT = String.format("T%06X%02X%s", dir, bytes, cod);
        pw.println(regT); registros.add(regT);
    }

    private void agregarRegM(List<String> dest, String regM) {
        dest.add(regM); registros.add(regM);
    }

    /** Quita prefijos de modo (#,@) y sufijo ,X del operando. */
    private String limpiarOpr(String opr) {
        opr = opr.trim();
        if (opr.toUpperCase().endsWith(",X")) opr = opr.substring(0, opr.length()-2).trim();
        if (opr.startsWith("#") || opr.startsWith("@")) opr = opr.substring(1).trim();
        return opr;
    }

    // ═════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═════════════════════════════════════════════════════════════════

    /** Dirección absoluta en memoria de una entrada del listado. */
    private int dirAbsoluta(EntradaListado e) {
        if (e.noBloque == 0) return e.contloc;
        for (int[] b : tablaBloques)
            if (b[0] == e.noBloque) return e.contloc + b[3] + direccionIni;
        return e.contloc;
    }

    /** Dirección absoluta en memoria de un símbolo (bloque ajustado). */
    private int symAbsoluto(String id) {
        int val = tablaSimbolos.getOrDefault(id, 0);
        String tipo = tipoSimbolos.getOrDefault(id, "A");
        if (tipo.equals("A") || tipo.equals("E")) return val;
        int blk = bloqueSimbolos.getOrDefault(id, 0);
        if (blk == 0) return val;
        for (int[] b : tablaBloques)
            if (b[0] == blk) return val + b[3] + direccionIni;
        return val;
    }

    /** Dirección relativa al inicio de la sección (para registros D). */
    private int symRelativo(String id) {
        return symAbsoluto(id) - direccionIni;
    }

    private int obtenerReg(String nombre) throws Exception {
        nombre = nombre.toUpperCase();
        if (!REG.containsKey(nombre)) throw new Exception("Registro no válido: '" + nombre + "'");
        return REG.get(nombre);
    }

    private int parsearNumero(String valor) throws Exception {
        if (valor == null || valor.isBlank()) throw new Exception("Operando vacío");
        valor = valor.trim().toUpperCase();
        if (valor.endsWith("H"))
            return Integer.parseInt(valor.substring(0, valor.length()-1), 16);
        return Integer.parseInt(valor);
    }

    private void registrarErrorP2(EntradaListado e, String msg) {
        e.errorP2 = msg;
        errores.add(String.format("x Error P2 en línea %d [%s %s]: %s",
            e.numeroLinea, e.instruccion, e.operando, msg));
    }
}
