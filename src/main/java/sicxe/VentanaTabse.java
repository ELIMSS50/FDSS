package sicxe;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Ventana secundaria que muestra la Tabla de Símbolos Externos (TABSE)
 * construida por el cargador-ligador.
 *
 * Columnas: Sección de control | Símbolo | Dirección | Longitud
 * La columna Longitud solo tiene valor en las filas raíz de cada sección.
 */
public class VentanaTabse extends JFrame {

    private static final Color BG_MAIN   = new Color(22, 27, 34);
    private static final Color BG_TABLE  = new Color(22, 27, 34);
    private static final Color BG_HEADER = new Color(40, 50, 65);
    private static final Color FG_TEXT   = new Color(210, 220, 230);
    private static final Color FG_HEADER = new Color(140, 180, 220);
    private static final Color FG_SEC    = new Color(100, 180, 255);
    private static final Color ROW_NORM  = new Color(28, 34, 42);
    private static final Color ROW_ALT   = new Color(33, 40, 50);
    private static final Color BORDER_C  = new Color(50, 65, 85);

    public VentanaTabse(List<CargadorLigador.EntradaTabse> tabse) {
        super("TABSE — Tabla de Símbolos Externos");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 450);
        setMinimumSize(new Dimension(400, 300));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        String[] cols = {"Sección de control", "Símbolo", "Dirección", "Longitud"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (CargadorLigador.EntradaTabse e : tabse) {
            String dir = String.format("%06X", e.direccion);
            String lon = e.simbolo.isEmpty() ? String.format("%06X", e.longitud) : "";
            modelo.addRow(new Object[]{e.seccion, e.simbolo, dir, lon});
        }

        JTable tabla = new JTable(modelo);
        tabla.setBackground(BG_TABLE);
        tabla.setForeground(FG_TEXT);
        tabla.setGridColor(BORDER_C);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tabla.setRowHeight(24);
        tabla.setSelectionBackground(BG_HEADER);

        JTableHeader hdr = tabla.getTableHeader();
        hdr.setBackground(BG_HEADER);
        hdr.setForeground(FG_HEADER);
        hdr.setFont(new Font("Monospaced", Font.BOLD, 13));
        hdr.setReorderingAllowed(false);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(c >= 2 ? CENTER : LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                // Filas raíz de sección (símbolo vacío) resaltadas
                boolean esSeccion = modelo.getValueAt(r, 1).toString().isEmpty();
                Color fg = sel ? FG_TEXT : (esSeccion ? FG_SEC : FG_TEXT);
                Color bg = sel ? BG_HEADER
                               : (esSeccion ? new Color(35, 50, 70)
                                            : (r % 2 == 0 ? ROW_NORM : ROW_ALT));
                setBackground(bg);
                setForeground(fg);
                if (esSeccion) setFont(new Font("Monospaced", Font.BOLD, 13));
                else           setFont(new Font("Monospaced", Font.PLAIN, 13));
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(tabla);
        sp.getViewport().setBackground(BG_TABLE);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sp, BorderLayout.CENTER);

        add(panel);
        setVisible(true);
    }
}
