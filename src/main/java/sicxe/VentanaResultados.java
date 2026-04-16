package sicxe;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Ventana gráfica del ensamblador SIC/XE — Práctica 3 con Bloques.
 *
 * Pestañas:
 *   1. Listado       — líneas con código objeto y No. Bloque
 *   2. Símbolos      — tabla de símbolos con bloque
 *   3. Tabla Bloques — tabla de bloques (número, nombre, longitud, dirIni)
 *   4. Registros obj — registros H/T/M/E
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

        super("SIC/XE Ensamblador P03 — " + nombreArchivo);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(0, 0));

        int totalErrores = erroresSintacticos.size()
                         + erroresSemanticos.size()
                         + erroresPaso2.size();

        add(construirCabecera(nombreArchivo, totalErrores, longitudPrograma,
                              erroresPaso2.isEmpty() && erroresSemanticos.isEmpty()
                              && erroresSintacticos.isEmpty()),
            BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(FONDO_PANEL);
        tabs.setForeground(TEXTO);
        tabs.setFont(new Font("Monospaced", Font.BOLD, 12));

        tabs.addTab("  Listado (" + listado.size() + ")",
                    construirTabListado(listado, relocalizables, nombresBloques));

        tabs.addTab("  Símbolos (" + tablaSimbolos.size() + ")",
                    construirTabSimbolos(tablaSimbolos, tipoSimbolos, bloqueSimbolos, relocalizables, nombresBloques));

        tabs.addTab("  Bloques (" + tablaBloques.size() + ")",
                    construirTabBloques(tablaBloques, nombresBloques));

        tabs.addTab("Registros objeto", construirTabRegistrosObjeto(registros));

        tabs.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        add(tabs, BorderLayout.CENTER);
        add(construirBarraEstado(totalErrores), BorderLayout.SOUTH);
        setVisible(true);
    }

    // ── Cabecera ──────────────────────────────────────────────────────
    private JPanel construirCabecera(String archivo, int totalErr,
                                      int longitud, boolean paso2ok) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FONDO_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JLabel titulo = new JLabel("⚙  Ensamblador SIC/XE — Práctica 3 (Bloques)");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        titulo.setForeground(TEXTO);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        info.setBackground(FONDO_PANEL);
        info.add(lbl(" " + archivo, TEXTO_HEADER));
        info.add(sep());
        info.add(lbl(String.format(" %04Xh bytes", longitud), TEXTO_HEADER));
        info.add(sep());
        info.add(lbl(paso2ok ? "✔ Código objeto generado" : "✔ Código objeto generado",
                     paso2ok ? ACENTO_OK : ACENTO_WARN));
        info.add(sep());
        JLabel estado = totalErr == 0
            ? lbl("✔ Sin errores", ACENTO_OK)
            : lbl("✘ " + totalErr + " error(es)", ACENTO_ERR);
        estado.setFont(estado.getFont().deriveFont(Font.BOLD));
        info.add(estado);

        panel.add(titulo, BorderLayout.WEST);
        panel.add(info,   BorderLayout.EAST);
        return panel;
    }

    private JLabel construirBarraEstado(int totalErr) {
        String txt = totalErr == 0
            ? "  ✔  Ensamblado correctamente"
            : "  ✘  " + totalErr + " error(es) — revisa las pestañas de errores";
        JLabel b = new JLabel(txt);
        b.setFont(new Font("Monospaced", Font.BOLD, 12));
        b.setForeground(totalErr == 0 ? ACENTO_OK : ACENTO_ERR);
        b.setOpaque(true);
        b.setBackground(FONDO_PANEL);
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        return b;
    }

    // ── Pestaña 1: Listado ────────────────────────────────────────────
    private JScrollPane construirTabListado(List<EntradaListado> listado,
                                             Set<String> relocalizables,
                                             List<String> nombresBloques) {
        String[] cols = {"NL","CONTLOC","ETIQUETA","INSTRUCCIÓN","OPERANDO",
                         "FMT","MODO","CÓDIGO OBJ","BLQ","ERROR"};
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

            String cod = nvl(e.codigoObjeto);
            if (!cod.isBlank()) {
                if (e.formato == 4) {
                    String opr = nvl(e.operando).trim();
                    if (opr.toUpperCase().endsWith(",X")) opr = opr.substring(0, opr.length() - 2).trim();
                    if (opr.startsWith("#") || opr.startsWith("@")) opr = opr.substring(1).trim();
                    if (relocalizables.contains(opr)) cod = cod + "*";
                } else if (e.instruccion.equals("WORD") && "R".equals(e.tipoExpresion)) {
                    cod = cod + "*";
                }
            }
            datos[i][7] = cod;

            // Número de bloque con nombre
            String blqStr = String.valueOf(e.noBloque);
            if (e.noBloque >= 0 && e.noBloque < nombresBloques.size())
                blqStr = e.noBloque + " (" + nombresBloques.get(e.noBloque) + ")";
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
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Monospaced", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                String err = (String) t.getValueAt(row, 9);
                boolean tieneError = err != null && !err.isBlank();
                if (sel) {
                    setBackground(new Color(60, 80, 110));
                    setForeground(Color.WHITE);
                } else if (tieneError) {
                    setBackground(FILA_ERR);
                    setForeground(col == 9
                        ? new Color(255, 80, 80)
                        : new Color(255, 160, 160));
                } else {
                    setBackground(row % 2 == 0 ? FILA_NORMAL : FILA_ALT);
                    setForeground(
                        col == 1 ? new Color(100, 200, 255) :
                        col == 3 ? new Color(180, 230, 150) :
                        col == 5 ? new Color(200, 160, 255) :
                        col == 7 ? new Color(255, 215, 100) :
                        col == 8 ? new Color(180, 200, 255) :
                        TEXTO);
                }
                return this;
            }
        });

        int[] anchos = {35, 65, 85, 100, 110, 35, 85, 120, 120, 200};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        return envolverScroll(tabla);
    }

    // ── Pestaña 2: Tabla de símbolos ──────────────────────────────────
    private JScrollPane construirTabSimbolos(Map<String, Integer> simbolos,
                                              Map<String, String>  tipoSimbolos,
                                              Map<String, Integer> bloqueSimbolos,
                                              Set<String> relocalizables,
                                              List<String> nombresBloques) {
        String[] cols = {"Símbolo", "Valor (hex)", "Valor (dec)", "Tipo", "No. Bloque"};
        Object[][] datos = new Object[simbolos.size()][5];
        int i = 0;
        for (Map.Entry<String, Integer> entry : simbolos.entrySet()) {
            String sym  = entry.getKey();
            int    val  = entry.getValue();
            String tipo = tipoSimbolos.getOrDefault(sym, "R");
            int    blk  = bloqueSimbolos.getOrDefault(sym, 0);
            datos[i][0] = sym;
            datos[i][1] = String.format("%04X", val);
            datos[i][2] = val;
            datos[i][3] = tipo.equals("A") ? "A (Absoluto)" : "R (Relativo)";
            String blqStr = String.valueOf(blk);
            if (blk >= 0 && blk < nombresBloques.size())
                blqStr = blk + " (" + nombresBloques.get(blk) + ")";
            datos[i][4] = blqStr;
            i++;
        }

        DefaultTableModel modelo = new DefaultTableModel(datos, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabla = crearTabla(modelo);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Monospaced", Font.PLAIN, 13));
                setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                if (sel) {
                    setBackground(new Color(60, 80, 110));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? FILA_NORMAL : FILA_ALT);
                    Color colorTipo = "A (Absoluto)".equals(t.getValueAt(row, 3))
                        ? new Color(255, 180, 80) : new Color(100, 230, 160);
                    setForeground(
                        col == 0 ? new Color(180, 230, 150) :
                        col == 1 ? new Color(100, 200, 255) :
                        col == 3 ? colorTipo :
                        col == 4 ? new Color(180, 200, 255) :
                        TEXTO);
                }
                return this;
            }
        });

        tabla.getColumnModel().getColumn(0).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(160);
        return envolverScroll(tabla);
    }

    // ── Pestaña 3: Tabla de bloques ───────────────────────────────────
    private JScrollPane construirTabBloques(List<int[]> tablaBloques, List<String> nombresBloques) {
        String[] cols = {"No. Bloque", "Nombre", "Longitud (hex)", "Dir. Inicio (hex)"};
        Object[][] datos = new Object[tablaBloques.size()][4];
        for (int i = 0; i < tablaBloques.size(); i++) {
            int[] b = tablaBloques.get(i);
            datos[i][0] = b[0];
            datos[i][1] = (b[0] >= 0 && b[0] < nombresBloques.size()) ? nombresBloques.get(b[0]) : "?";
            datos[i][2] = String.format("%04X", b[2]);
            datos[i][3] = String.format("%04X", b[3]);
        }

        DefaultTableModel modelo = new DefaultTableModel(datos, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabla = crearTabla(modelo);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Monospaced", Font.PLAIN, 14));
                setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
                if (sel) {
                    setBackground(new Color(60, 80, 110));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? FILA_NORMAL : FILA_ALT);
                    setForeground(
                        col == 0 ? new Color(200, 160, 255) :
                        col == 1 ? new Color(180, 230, 150) :
                        col == 2 ? new Color(255, 215, 100) :
                        col == 3 ? new Color(100, 200, 255) :
                        TEXTO);
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

    // ── Pestaña 4: Registros objeto ───────────────────────────────────
    private JScrollPane construirTabRegistrosObjeto(List<String> registros) {
        String[] cols = {"Tipo", "Registro"};
        Object[][] datos = new Object[registros.size()][2];

        for (int i = 0; i < registros.size(); i++) {
            String reg = registros.get(i).trim();
            datos[i][0] = reg.isEmpty() ? "" : String.valueOf(reg.charAt(0));
            datos[i][1] = reg;
        }

        DefaultTableModel modelo = new DefaultTableModel(datos, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabla = crearTabla(modelo);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Monospaced", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                if (sel) {
                    setBackground(new Color(60, 80, 110));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? FILA_NORMAL : FILA_ALT);
                    String tipo = (String) t.getValueAt(row, 0);
                    Color c = switch (tipo) {
                        case "H" -> new Color(100, 200, 255);
                        case "T" -> new Color(255, 215, 100);
                        case "M" -> new Color(180, 130, 255);
                        case "E" -> new Color(60,  200, 100);
                        default  -> TEXTO;
                    };
                    setForeground(c);
                }
                return this;
            }
        });

        tabla.getColumnModel().getColumn(0).setPreferredWidth(35);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(500);
        return envolverScroll(tabla);
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setBackground(FONDO_TABLA);
        tabla.setForeground(TEXTO);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tabla.setRowHeight(24);
        tabla.setGridColor(BORDE);
        tabla.setShowGrid(true);
        tabla.setSelectionBackground(new Color(60, 80, 110));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getTableHeader().setBackground(FONDO_HEADER);
        tabla.getTableHeader().setForeground(TEXTO_HEADER);
        tabla.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
        tabla.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));
        return tabla;
    }

    private JScrollPane envolverScroll(JTable tabla) {
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBackground(FONDO);
        scroll.getViewport().setBackground(FONDO_TABLA);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        return scroll;
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