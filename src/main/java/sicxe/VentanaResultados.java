package sicxe;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Ventana de resultados del ensamblador SIC/XE con secciones de control.
 *
 * Estructura:
 *   JTabbedPane externo — una pestaña por SeccionControl
 *     └─ JTabbedPane interno por sección:
 *          1. Listado       — líneas con código objeto
 *          2. Símbolos      — TABSIM de la sección (incluye EXTREF/EXTDEF)
 *          3. Bloques       — tabla de bloques USE de la sección
 *          4. Registros obj — H/D/R/T/M/E de esa sección
 *   Pestaña final — Errores globales
 */
public class VentanaResultados extends JFrame {

    private static final Color FONDO        = new Color(22, 27, 34);
    private static final Color FONDO_PANEL  = new Color(30, 36, 45);
    private static final Color FONDO_TABLA  = new Color(22, 27, 34);
    private static final Color FONDO_HEADER = new Color(40, 50, 65);
    private static final Color TEXTO        = new Color(210, 220, 230);
    private static final Color TEXTO_HEADER = new Color(140, 180, 220);
    private static final Color ACENTO_OK    = new Color(60, 200, 100);
    private static final Color ACENTO_ERR   = new Color(220, 70, 70);
    private static final Color ACENTO_WARN  = new Color(220, 160, 40);
    private static final Color FILA_ERR     = new Color(60, 20, 20);
    private static final Color FILA_NORMAL  = new Color(28, 34, 42);
    private static final Color FILA_ALT     = new Color(33, 40, 50);
    private static final Color BORDE        = new Color(50, 65, 85);

    // ─────────────────────────────────────────────────────────────────
    // Constructor principal — acepta lista de secciones
    // ─────────────────────────────────────────────────────────────────
    public VentanaResultados(String nombreArchivo,
                              List<SeccionControl> secciones,
                              List<String> erroresSintacticos,
                              List<String> erroresSemanticos,
                              List<String> erroresPaso2) {

        super("SIC/XE Ensamblador — Secciones de Control — " + nombreArchivo);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1380, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(0, 0));

        int totalLineas  = secciones.stream().mapToInt(s -> s.listado.size()).sum();
        int totalSimbls  = secciones.stream().mapToInt(s -> s.tablaSimbolos.size()).sum();
        int totalErrores = erroresSintacticos.size() + erroresSemanticos.size() + erroresPaso2.size();
        int longTotal    = secciones.stream().mapToInt(s -> s.longitudTotal).sum();

        add(construirCabecera(nombreArchivo, totalErrores, longTotal, secciones.size()),
            BorderLayout.NORTH);

        // ── JTabbedPane externo: una pestaña por sección + errores ────
        JTabbedPane tabsExternas = new JTabbedPane(JTabbedPane.TOP);
        estilizarTabs(tabsExternas);

        for (SeccionControl sec : secciones) {
            String titulo = "  " + (sec.esPrincipal ? "★ " : "") + sec.nombre + "  ";
            tabsExternas.addTab(titulo, construirPanelSeccion(sec));
        }

        // Pestaña de errores globales
        int nErr = totalErrores;
        String tituloErr = nErr == 0 ? "  ✔ Sin errores  " : "  ✘ Errores (" + nErr + ")  ";
        tabsExternas.addTab(tituloErr,
            construirTabErrores(erroresSintacticos, erroresSemanticos, erroresPaso2));
        if (nErr > 0) tabsExternas.setForegroundAt(tabsExternas.getTabCount()-1, ACENTO_ERR);

        tabsExternas.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        add(tabsExternas, BorderLayout.CENTER);
        add(construirBarraEstado(totalErrores, secciones.size(), totalLineas, totalSimbls),
            BorderLayout.SOUTH);

        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────
    // Constructor legado — compatibilidad con llamadas anteriores
    // ─────────────────────────────────────────────────────────────────
    public VentanaResultados(String nombreArchivo,
                              List<EntradaListado> listado,
                              Map<String, Integer> tablaSimbolos,
                              Map<String, String>  tipoSimbolos,
                              Map<String, Integer> bloqueSimbolos,
                              Set<String> relocalizables,
                              List<String> erroresSintacticos,
                              List<String> erroresSemanticos,
                              List<String> erroresPaso2,
                              int longitudPrograma,
                              List<String> registros,
                              List<int[]> tablaBloques,
                              List<String> nombresBloques) {

        // Construir una SeccionControl sintética a partir de los datos legacy
        SeccionControl sec = new SeccionControl("PROG", true, 0);
        sec.listado.addAll(listado);
        sec.tablaSimbolos.putAll(tablaSimbolos);
        sec.tipoSimbolos.putAll(tipoSimbolos);
        sec.bloqueSimbolos.putAll(bloqueSimbolos);
        sec.tablaBloques.addAll(tablaBloques);
        sec.nombresBloques.addAll(nombresBloques);
        sec.registrosObjeto.addAll(registros);
        sec.longitudTotal = longitudPrograma;

        // Delegar al constructor principal
        this.setTitle("SIC/XE Ensamblador — " + nombreArchivo);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(1380, 800);
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(FONDO);
        this.setLayout(new BorderLayout());

        int totalErrores = erroresSintacticos.size() + erroresSemanticos.size() + erroresPaso2.size();
        add(construirCabecera(nombreArchivo, totalErrores, longitudPrograma, 1), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        estilizarTabs(tabs);
        tabs.addTab("  PROG  ", construirPanelSeccion(sec));
        tabs.addTab(totalErrores == 0 ? "  ✔ Sin errores  " : "  ✘ Errores (" + totalErrores + ")  ",
            construirTabErrores(erroresSintacticos, erroresSemanticos, erroresPaso2));
        tabs.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        add(tabs, BorderLayout.CENTER);
        add(construirBarraEstado(totalErrores, 1, listado.size(), tablaSimbolos.size()), BorderLayout.SOUTH);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────
    // Panel de sección con pestañas internas
    // ─────────────────────────────────────────────────────────────────
    private JPanel construirPanelSeccion(SeccionControl sec) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FONDO);

        // Mini cabecera de sección
        JPanel infoSec = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        infoSec.setBackground(new Color(25, 32, 42));
        infoSec.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));
        infoSec.add(lbl(sec.esPrincipal ? "  Sección principal (START)" : "  Sección de control (CSECT)",
                        sec.esPrincipal ? ACENTO_OK : new Color(150, 200, 255)));
        infoSec.add(sep());
        infoSec.add(lbl("Nombre: " + sec.nombre, TEXTO_HEADER));
        infoSec.add(sep());
        infoSec.add(lbl(String.format("Longitud: %04Xh", sec.longitudTotal), TEXTO_HEADER));
        infoSec.add(sep());
        infoSec.add(lbl("Símbolos: " + sec.tablaSimbolos.size(), TEXTO_HEADER));
        infoSec.add(sep());
        infoSec.add(lbl("Bloques USE: " + sec.tablaBloques.size(), TEXTO_HEADER));
        if (!sec.simbolosExportados.isEmpty())
            infoSec.add(lbl("  EXTDEF: " + String.join(", ", sec.simbolosExportados),
                            new Color(255, 200, 80)));
        if (!sec.simbolosExternos.isEmpty())
            infoSec.add(lbl("  EXTREF: " + String.join(", ", sec.simbolosExternos),
                            new Color(180, 130, 255)));

        panel.add(infoSec, BorderLayout.NORTH);

        JTabbedPane tabsInt = new JTabbedPane(JTabbedPane.BOTTOM);
        estilizarTabs(tabsInt);

        tabsInt.addTab("  Listado (" + sec.listado.size() + ")  ",
                       construirTabListado(sec));
        tabsInt.addTab("  Símbolos (" + sec.tablaSimbolos.size() + ")  ",
                       construirTabSimbolos(sec));
        tabsInt.addTab("  Bloques (" + sec.tablaBloques.size() + ")  ",
                       construirTabBloques(sec));
        tabsInt.addTab("  Registros obj (" + sec.registrosObjeto.size() + ")  ",
                       construirTabRegistros(sec));

        panel.add(tabsInt, BorderLayout.CENTER);
        return panel;
    }

    // ── Listado de una sección ────────────────────────────────────────
    private JScrollPane construirTabListado(SeccionControl sec) {
        String[] cols = {"NL","CONTLOC","ETIQUETA","INSTRUCCIÓN","OPERANDO",
                         "FMT","MODO","CÓDIGO OBJ","BLOQUE","ERROR"};
        List<EntradaListado> listado = sec.listado;
        Object[][] datos = new Object[listado.size()][10];
        for (int i = 0; i < listado.size(); i++) {
            EntradaListado e = listado.get(i);
            datos[i][0] = e.numeroLinea;
            datos[i][1] = String.format("%04X", e.contloc);
            datos[i][2] = nvl(e.etiqueta);
            datos[i][3] = nvl(e.instruccion);
            datos[i][4] = nvl(e.operando);
            datos[i][5] = e.formato > 0 ? String.valueOf(e.formato) : "";
            datos[i][6] = nvl(e.modoDir);
            String cod  = nvl(e.codigoObjeto);
            if (!cod.isBlank() && ("R".equals(e.tipoExpresion) || "EX".equals(e.tipoExpresion)))
                cod += "*";
            datos[i][7] = cod;
            String blqStr = String.valueOf(e.noBloque);
            if (e.noBloque >= 0 && e.noBloque < sec.nombresBloques.size())
                blqStr = e.noBloque + " (" + sec.nombresBloques.get(e.noBloque) + ")";
            datos[i][8] = blqStr;
            String err = nvl(e.error);
            if (!nvl(e.errorP2).isBlank())
                err = err.isBlank() ? "[P2] " + e.errorP2 : err + " | [P2] " + e.errorP2;
            datos[i][9] = err;
        }
        DefaultTableModel modelo = new DefaultTableModel(datos, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = crearTabla(modelo);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Monospaced", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                String err = (String) t.getValueAt(row, 9);
                boolean tieneErr = err != null && !err.isBlank();
                if (sel) { setBackground(new Color(60,80,110)); setForeground(Color.WHITE); }
                else if (tieneErr) {
                    setBackground(FILA_ERR);
                    setForeground(col == 9 ? new Color(255,80,80) : new Color(255,160,160));
                } else {
                    setBackground(row % 2 == 0 ? FILA_NORMAL : FILA_ALT);
                    setForeground(col==1 ? new Color(100,200,255) : col==3 ? new Color(180,230,150)
                        : col==5 ? new Color(200,160,255) : col==7 ? new Color(255,215,100)
                        : col==8 ? new Color(180,200,255) : TEXTO);
                }
                return this;
            }
        });
        int[] anchos = {35, 65, 85, 100, 110, 35, 85, 120, 130, 220};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        return envolverScroll(tabla);
    }

    // ── TABSIM de una sección ─────────────────────────────────────────
    private JScrollPane construirTabSimbolos(SeccionControl sec) {
        String[] cols = {"Símbolo","Valor (hex)","Valor (dec)","Tipo","No. Bloque","Rol"};
        List<Object[]> filas = new ArrayList<>();
        for (Map.Entry<String, Integer> en : sec.tablaSimbolos.entrySet()) {
            String sym   = en.getKey();
            int    val   = en.getValue();
            String tipo  = sec.tipoSimbolos.getOrDefault(sym, "R");
            int    blk   = sec.bloqueSimbolos.getOrDefault(sym, -1);
            boolean esExt = sec.simbolosExternos.contains(sym);
            boolean esExp = sec.simbolosExportados.contains(sym);
            String blqStr = esExt ? "—" : (blk >= 0 && blk < sec.nombresBloques.size()
                ? blk + " (" + sec.nombresBloques.get(blk) + ")" : String.valueOf(blk));
            String tipoStr = "E".equals(tipo) ? "E (Externo)" : "A".equals(tipo) ? "A (Abs)" : "R (Rel)";
            String rol = esExt ? "EXTREF" : esExp ? "EXTDEF" : "";
            filas.add(new Object[]{sym, esExt ? "N/A" : String.format("%04X", val),
                esExt ? "—" : val, tipoStr, blqStr, rol});
        }
        Object[][] datos = filas.toArray(new Object[0][]);
        DefaultTableModel modelo = new DefaultTableModel(datos, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = crearTabla(modelo);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Monospaced", Font.PLAIN, 13));
                setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                if (sel) { setBackground(new Color(60,80,110)); setForeground(Color.WHITE); }
                else {
                    setBackground(row % 2 == 0 ? FILA_NORMAL : FILA_ALT);
                    String tipo = (String) t.getValueAt(row, 3);
                    Color cTipo = "E (Externo)".equals(tipo) ? new Color(180,130,255)
                                : "A (Abs)".equals(tipo) ? new Color(255,180,80)
                                : new Color(100,230,160);
                    String rol = (String) t.getValueAt(row, 5);
                    Color cRol = "EXTREF".equals(rol) ? new Color(180,130,255)
                               : "EXTDEF".equals(rol) ? new Color(255,200,80) : TEXTO;
                    setForeground(col==0 ? new Color(180,230,150) : col==1 ? new Color(100,200,255)
                        : col==3 ? cTipo : col==4 ? new Color(180,200,255) : col==5 ? cRol : TEXTO);
                }
                return this;
            }
        });
        int[] anchos = {140, 90, 90, 120, 170, 80};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        return envolverScroll(tabla);
    }

    // ── Tabla de bloques de una sección ───────────────────────────────
    private JScrollPane construirTabBloques(SeccionControl sec) {
        String[] cols = {"No. Bloque","Nombre","Longitud (hex)","Dir. Inicio (hex)"};
        Object[][] datos = new Object[sec.tablaBloques.size()][4];
        for (int i = 0; i < sec.tablaBloques.size(); i++) {
            int[] b = sec.tablaBloques.get(i);
            datos[i][0] = b[0];
            datos[i][1] = (b[0] >= 0 && b[0] < sec.nombresBloques.size())
                          ? sec.nombresBloques.get(b[0]) : "?";
            datos[i][2] = String.format("%04X", b[2]);
            datos[i][3] = String.format("%04X", b[3]);
        }
        DefaultTableModel modelo = new DefaultTableModel(datos, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = crearTabla(modelo);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Monospaced", Font.PLAIN, 14));
                setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
                if (sel) { setBackground(new Color(60,80,110)); setForeground(Color.WHITE); }
                else {
                    setBackground(row % 2 == 0 ? FILA_NORMAL : FILA_ALT);
                    setForeground(col==0 ? new Color(200,160,255) : col==1 ? new Color(180,230,150)
                        : col==2 ? new Color(255,215,100) : col==3 ? new Color(100,200,255) : TEXTO);
                }
                return this;
            }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        return envolverScroll(tabla);
    }

    // ── Registros objeto de una sección ───────────────────────────────
    private JScrollPane construirTabRegistros(SeccionControl sec) {
        List<String> regs = sec.registrosObjeto;
        String[] cols = {"Tipo","Registro"};
        Object[][] datos = new Object[regs.size()][2];
        for (int i = 0; i < regs.size(); i++) {
            String r = regs.get(i).trim();
            datos[i][0] = r.isEmpty() ? "" : String.valueOf(r.charAt(0));
            datos[i][1] = r;
        }
        DefaultTableModel modelo = new DefaultTableModel(datos, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = crearTabla(modelo);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Monospaced", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                if (sel) { setBackground(new Color(60,80,110)); setForeground(Color.WHITE); }
                else {
                    setBackground(row % 2 == 0 ? FILA_NORMAL : FILA_ALT);
                    String tipo = (String) t.getValueAt(row, 0);
                    Color c = switch (nvl(tipo)) {
                        case "H" -> new Color(100,200,255);
                        case "D" -> new Color(255,200,80);
                        case "R" -> new Color(180,130,255);
                        case "T" -> new Color(255,215,100);
                        case "M" -> new Color(200,100,255);
                        case "E" -> new Color(60,200,100);
                        default  -> TEXTO;
                    };
                    setForeground(c);
                }
                return this;
            }
        });
        tabla.getColumnModel().getColumn(0).setPreferredWidth(35);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(600);
        return envolverScroll(tabla);
    }

    // ── Pestaña de errores globales ───────────────────────────────────
    private JScrollPane construirTabErrores(List<String> eS, List<String> eSem, List<String> eP2) {
        StringBuilder sb = new StringBuilder();
        if (!eS.isEmpty()) {
            sb.append("═══ Errores Sintácticos (").append(eS.size()).append(") ═══\n");
            eS.forEach(e -> sb.append(e).append('\n'));
            sb.append('\n');
        }
        if (!eSem.isEmpty()) {
            sb.append("═══ Errores Semánticos (").append(eSem.size()).append(") ═══\n");
            eSem.forEach(e -> sb.append(e).append('\n'));
            sb.append('\n');
        }
        if (!eP2.isEmpty()) {
            sb.append("═══ Errores Paso 2 (").append(eP2.size()).append(") ═══\n");
            eP2.forEach(e -> sb.append(e).append('\n'));
        }
        if (sb.length() == 0) sb.append("✔  Sin errores.");
        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBackground(FONDO_TABLA);
        area.setForeground(eS.isEmpty() && eSem.isEmpty() && eP2.isEmpty()
            ? ACENTO_OK : new Color(255, 110, 110));
        area.setEditable(false);
        area.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JScrollPane sp = new JScrollPane(area);
        sp.setBackground(FONDO); sp.getViewport().setBackground(FONDO_TABLA);
        sp.setBorder(BorderFactory.createLineBorder(BORDE));
        return sp;
    }

    // ── Cabecera ──────────────────────────────────────────────────────
    private JPanel construirCabecera(String archivo, int totalErr,
                                      int longTotal, int numSecciones) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FONDO_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,BORDE),
            BorderFactory.createEmptyBorder(10,16,10,16)));
        JLabel titulo = new JLabel("  Ensamblador SIC/XE — Secciones de Control");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        titulo.setForeground(TEXTO);
        JPanel info = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        info.setBackground(FONDO_PANEL);
        info.add(lbl(archivo, TEXTO_HEADER)); info.add(sep());
        info.add(lbl(numSecciones + " sección(es)", TEXTO_HEADER)); info.add(sep());
        info.add(lbl(String.format("%04Xh bytes totales", longTotal), TEXTO_HEADER)); info.add(sep());
        JLabel estado = totalErr == 0 ? lbl("✔ Sin errores", ACENTO_OK)
                                      : lbl("✘ " + totalErr + " error(es)", ACENTO_ERR);
        estado.setFont(estado.getFont().deriveFont(Font.BOLD));
        info.add(estado);
        panel.add(titulo, BorderLayout.WEST);
        panel.add(info,   BorderLayout.EAST);
        return panel;
    }

    private JLabel construirBarraEstado(int totalErr, int nSec, int nLineas, int nSimbs) {
        String txt = totalErr == 0
            ? String.format("  ✔  %d sección(es) | %d líneas | %d símbolos — Ensamblado OK", nSec, nLineas, nSimbs)
            : String.format("  ✘  %d error(es) — %d sección(es) | %d líneas | %d símbolos", totalErr, nSec, nLineas, nSimbs);
        JLabel b = new JLabel(txt);
        b.setFont(new Font("Monospaced", Font.BOLD, 12));
        b.setForeground(totalErr == 0 ? ACENTO_OK : ACENTO_ERR);
        b.setOpaque(true); b.setBackground(FONDO_PANEL);
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        return b;
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private void estilizarTabs(JTabbedPane tp) {
        tp.setBackground(FONDO_PANEL);
        tp.setForeground(TEXTO);
        tp.setFont(new Font("Monospaced", Font.BOLD, 12));
    }

    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setBackground(FONDO_TABLA); tabla.setForeground(TEXTO);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tabla.setRowHeight(24); tabla.setGridColor(BORDE);
        tabla.setShowGrid(true);
        tabla.setSelectionBackground(new Color(60,80,110));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getTableHeader().setBackground(FONDO_HEADER);
        tabla.getTableHeader().setForeground(TEXTO_HEADER);
        tabla.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
        tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDE));
        return tabla;
    }

    private JScrollPane envolverScroll(JTable tabla) {
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBackground(FONDO); sp.getViewport().setBackground(FONDO_TABLA);
        sp.setBorder(BorderFactory.createLineBorder(BORDE));
        return sp;
    }

    private JLabel lbl(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        l.setForeground(color);
        return l;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 16));
        s.setForeground(BORDE);
        return s;
    }

    private String nvl(String v) { return v != null ? v : ""; }
}
