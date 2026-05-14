package sicxe;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Cargador-ligador de dos pasos para SIC/XE.
 *
 * Paso 1: construye la TABSE leyendo registros H y D de cada módulo .obj.
 * Paso 2: copia registros T a memoria y aplica registros M usando la TABSE.
 *
 * Los bytes afectados por registros M se muestran con valor original (tachado)
 * y valor final superpuesto en la misma celda, con fondo resaltado si cambió.
 */
public class CargadorLigador extends JFrame {

    // ── Paleta oscura ─────────────────────────────────────────────────────
    private static final Color BG_MAIN    = new Color(22, 27, 34);
    private static final Color BG_PANEL   = new Color(30, 36, 45);
    private static final Color BG_TABLE   = new Color(22, 27, 34);
    private static final Color BG_HEADER  = new Color(40, 50, 65);
    private static final Color FG_TEXT    = new Color(210, 220, 230);
    private static final Color FG_HEADER  = new Color(140, 180, 220);
    private static final Color FG_OK      = new Color(60, 200, 100);
    private static final Color FG_ERR     = new Color(220, 70, 70);
    private static final Color ROW_NORM   = new Color(28, 34, 42);
    private static final Color ROW_ALT    = new Color(33, 40, 50);
    private static final Color BORDER_C   = new Color(50, 65, 85);
    private static final Color FG_UNINIT  = new Color(60, 70, 85);
    // Colores para celdas modificadas por registros M
    private static final Color BG_MOD_CAMB = new Color(60, 55, 15);  // cambió: fondo dorado oscuro
    private static final Color BG_MOD_IGUAL= new Color(30, 50, 30);  // en campo M, sin cambio

    // ── Estado ────────────────────────────────────────────────────────────
    private final DefaultListModel<File> modeloLista  = new DefaultListModel<>();
    private final List<EntradaTabse>     tabse        = new ArrayList<>();
    private int[]     memoria        = new int[0];
    private boolean[] memoriaEscrita = new boolean[0]; // true = byte puesto por T record
    private int   dirCargaInicio = 0;
    private int   dirCargaFin    = 0;

    /**
     * Historial de valores de cada byte afectado por registros M.
     * Clave: dirección absoluta del byte.
     * Valor: lista [valorDesdeT, trasPrimerM, trasSegundoM, ..., final]
     */
    private final Map<Integer, List<Integer>> modificaciones = new LinkedHashMap<>();

    // ── Componentes UI ────────────────────────────────────────────────────
    private JTextField        campoDir;
    private JTextField        campoTam;
    private JList<File>       listaArchivos;
    private DefaultTableModel modeloMemoria;
    private JTable            tablaMemoria;
    private JLabel            lblInfo;

    // ─────────────────────────────────────────────────────────────────────
    /** Abre el cargador sin archivos precargados. */
    public CargadorLigador() {
        this(Collections.emptyList());
    }

    /**
     * Abre el cargador precargando los archivos .obj generados por el ensamblador.
     * Si la lista está vacía el usuario puede agregar los suyos manualmente.
     */
    public CargadorLigador(List<File> archivosIniciales) {
        super("CARGADOR - LIGADOR SIC/XE");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1260, 720);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        add(construirToolbar(),       BorderLayout.NORTH);
        add(construirPanelCentral(),  BorderLayout.CENTER);
        add(construirBarraInferior(), BorderLayout.SOUTH);

        // Precargar archivos del ensamblador (solo los que existen en disco)
        for (File f : archivosIniciales)
            if (f != null && f.exists() && !modeloLista.contains(f))
                modeloLista.addElement(f);

        if (!modeloLista.isEmpty())
            lblInfo.setText("Archivos del ensamblador cargados. Ajusta la dirección y presiona Cargar.");

        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI — TOOLBAR
    // ═══════════════════════════════════════════════════════════════════════
    private JToolBar construirToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBackground(BG_PANEL);
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));

        tb.add(btn("Agregar .obj", e -> accionAgregar()));
        tb.add(btn("Quitar",       e -> accionQuitar()));
        tb.addSeparator();
        tb.add(lbl("  Dir. de Carga (hex):"));
        campoDir = campo("0000", 90);
        tb.add(campoDir);
        tb.add(lbl("  Tamaño (hex):"));
        campoTam = campo("", 80);
        campoTam.setToolTipText("Opcional: tamaño total del programa en hex. Si se omite, se calcula de los registros H.");
        tb.add(campoTam);
        tb.addSeparator();
        tb.add(btnColor("Cargar",            new Color(45, 130, 75),  e -> accionCargar()));
        tb.add(btnColor("Cargar módulo",      new Color(30, 100, 160), e -> accionCargarModulo()));
        tb.add(btnColor("Limpiar",           new Color(180, 70, 50),  e -> accionLimpiar()));
        tb.addSeparator();
        tb.add(btn("Ver TABSE", e -> accionVerTabse()));

        return tb;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI — PANEL CENTRAL
    // ═══════════════════════════════════════════════════════════════════════
    private JComponent construirPanelCentral() {
        // Panel izquierdo: lista de archivos
        listaArchivos = new JList<>(modeloLista);
        listaArchivos.setBackground(BG_TABLE);
        listaArchivos.setForeground(FG_TEXT);
        listaArchivos.setFont(new Font("Monospaced", Font.PLAIN, 12));
        listaArchivos.setSelectionBackground(BG_HEADER);
        listaArchivos.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                setBackground(sel ? BG_HEADER : (i % 2 == 0 ? ROW_NORM : ROW_ALT));
                setForeground(FG_TEXT);
                if (v instanceof File) setText(((File) v).getName());
                setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                return this;
            }
        });

        JScrollPane scrollLista = new JScrollPane(listaArchivos);
        scrollLista.getViewport().setBackground(BG_TABLE);

        JPanel btnMover = new JPanel(new GridLayout(1, 2, 4, 0));
        btnMover.setBackground(BG_PANEL);
        btnMover.add(btn("↑ Subir", e -> moverSeleccion(-1)));
        btnMover.add(btn("↓ Bajar", e -> moverSeleccion(+1)));

        JPanel panelIzq = new JPanel(new BorderLayout(0, 4));
        panelIzq.setBackground(BG_MAIN);
        panelIzq.setBorder(titulado("Orden de módulos .obj"));
        panelIzq.add(scrollLista, BorderLayout.CENTER);
        panelIzq.add(btnMover,    BorderLayout.SOUTH);
        panelIzq.setPreferredSize(new Dimension(260, 0));

        // Panel derecho: mapa de memoria + leyenda
        JPanel panelDer = new JPanel(new BorderLayout(0, 4));
        panelDer.setBackground(BG_MAIN);
        panelDer.setBorder(titulado("Mapa de Memoria"));
        panelDer.add(construirMapaMemoria(), BorderLayout.CENTER);
        panelDer.add(construirLeyenda(),     BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, panelDer);
        split.setDividerLocation(260);
        split.setBackground(BG_MAIN);
        split.setBorder(BorderFactory.createEmptyBorder(6, 6, 4, 6));
        split.setDividerSize(5);
        return split;
    }

    private JScrollPane construirMapaMemoria() {
        String[] cols = new String[17];
        cols[0] = "Dirección";
        for (int i = 0; i < 16; i++) cols[i + 1] = String.format("%X", i);

        modeloMemoria = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaMemoria = new JTable(modeloMemoria);
        tablaMemoria.setBackground(BG_TABLE);
        tablaMemoria.setForeground(FG_TEXT);
        tablaMemoria.setGridColor(BORDER_C);
        tablaMemoria.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tablaMemoria.setRowHeight(22);
        tablaMemoria.setSelectionBackground(BG_HEADER);

        JTableHeader hdr = tablaMemoria.getTableHeader();
        hdr.setBackground(BG_HEADER);
        hdr.setForeground(FG_HEADER);
        hdr.setFont(new Font("Monospaced", Font.BOLD, 12));
        hdr.setReorderingAllowed(false);

        tablaMemoria.getColumnModel().getColumn(0).setPreferredWidth(78);
        for (int i = 1; i <= 16; i++)
            tablaMemoria.getColumnModel().getColumn(i).setPreferredWidth(42);

        tablaMemoria.setDefaultRenderer(Object.class, new RendererMemoria());

        JScrollPane sp = new JScrollPane(tablaMemoria);
        sp.getViewport().setBackground(BG_TABLE);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        return sp;
    }

    private JPanel construirLeyenda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        p.add(celdaLeyenda(BG_MOD_CAMB,  "Byte modificado por registro M"));
        p.add(celdaLeyenda(BG_MOD_IGUAL, "En campo M, sin cambio"));
        p.add(celdaLeyenda(ROW_NORM,     "Código de T record"));
        return p;
    }

    private JPanel celdaLeyenda(Color bg, String texto) {
        JPanel muestra = new JPanel();
        muestra.setBackground(bg);
        muestra.setPreferredSize(new Dimension(14, 14));
        muestra.setBorder(BorderFactory.createLineBorder(BORDER_C));

        JLabel etiq = new JLabel(texto);
        etiq.setForeground(FG_TEXT);
        etiq.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        fila.setBackground(BG_PANEL);
        fila.add(muestra);
        fila.add(etiq);
        return fila;
    }

    private JPanel construirBarraInferior() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        lblInfo = new JLabel("Sin programa cargado.");
        lblInfo.setForeground(FG_TEXT);
        lblInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bar.add(lblInfo);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RENDERER DE CELDAS DEL MAPA DE MEMORIA
    // ═══════════════════════════════════════════════════════════════════════
    private static final String FILA_SEPARADOR = "···"; // "···"

    private class RendererMemoria extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

            // ── Fila separadora "···" (huecos de memoria vacía) ──────
            boolean esSeparador = FILA_SEPARADOR.equals(t.getModel().getValueAt(r, 0));
            if (esSeparador) {
                setBackground(BG_MAIN);
                setForeground(new Color(70, 85, 105));
                setFont(new Font("Monospaced", Font.PLAIN, 11));
                setText(c == 0 ? "   " + FILA_SEPARADOR : "");
                setHorizontalAlignment(LEFT);
                return this;
            }

            String txt = v == null ? "" : v.toString();
            setHorizontalAlignment(c == 0 ? LEFT : CENTER);

            // ── Columna de dirección ──────────────────────────────────
            if (c == 0) {
                setBackground(BG_HEADER);
                setForeground(FG_HEADER);
                setFont(new Font("Monospaced", Font.BOLD, 12));
                setText(txt);
                return this;
            }

            // ── Columna de datos: dirección absoluta del byte ─────────
            int absAddr = -1;
            try { absAddr = Integer.parseInt(t.getModel().getValueAt(r, 0).toString(), 16) + (c - 1); }
            catch (NumberFormatException ignored) {}

            if (txt.isEmpty()) {
                setBackground(sel ? BG_HEADER : (r % 2 == 0 ? ROW_NORM : ROW_ALT));
                setForeground(FG_UNINIT);
                setText("");
                return this;
            }

            // ── ¿Modificado por registro M? ───────────────────────────
            if (absAddr >= 0 && modificaciones.containsKey(absAddr)) {
                List<Integer> hist  = modificaciones.get(absAddr);
                int initVal  = hist.get(0);
                int finalVal = hist.get(hist.size() - 1);
                boolean netChange = (initVal != finalVal);

                setBackground(sel ? BG_HEADER : (netChange ? BG_MOD_CAMB : BG_MOD_IGUAL));

                if (hist.size() >= 3) {
                    // Múltiples M records: mostrar historial completo
                    StringBuilder sb = new StringBuilder("<html><center>");
                    sb.append(String.format("<font color='#888888'><s>%02X</s></font>", initVal));
                    for (int k = 1; k < hist.size() - 1; k++)
                        sb.append(String.format("<br><font color='#ffcc44'>%02X</font>", hist.get(k)));
                    String fc2 = netChange ? "#aaff88" : "#80c880";
                    sb.append(String.format("<br><font color='%s'><b>%02X</b></font>", fc2, finalVal));
                    sb.append("</center></html>");
                    setText(sb.toString());
                } else if (netChange) {
                    setText(String.format(
                        "<html><center><font color='#888888'><s>%02X</s></font><br>" +
                        "<font color='#aaff88'><b>%02X</b></font></center></html>",
                        initVal, finalVal));
                } else {
                    setForeground(new Color(150, 210, 150));
                    setText(txt);
                }
                return this;
            }

            // ── Byte normal de T record ───────────────────────────────
            setBackground(sel ? BG_HEADER : (r % 2 == 0 ? ROW_NORM : ROW_ALT));
            // 0xFF no inicializado → color tenue; 0xFF escrito por T record → color normal
            int memIdx2 = absAddr - dirCargaInicio;
            boolean esEscrito = memIdx2 >= 0 && memIdx2 < memoriaEscrita.length
                                && memoriaEscrita[memIdx2];
            setForeground((!esEscrito && "FF".equalsIgnoreCase(txt)) ? FG_UNINIT : FG_TEXT);
            setText(txt);
            return this;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ACCIONES
    // ═══════════════════════════════════════════════════════════════════════
    private void accionAgregar() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos objeto SIC/XE (*.obj)", "obj"));
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            for (File f : fc.getSelectedFiles())
                if (!modeloLista.contains(f)) modeloLista.addElement(f);
    }

    private void accionQuitar() {
        int[] sel = listaArchivos.getSelectedIndices();
        for (int i = sel.length - 1; i >= 0; i--) modeloLista.remove(sel[i]);
    }

    private void moverSeleccion(int delta) {
        int idx  = listaArchivos.getSelectedIndex();
        if (idx < 0) return;
        int nuevo = idx + delta;
        if (nuevo < 0 || nuevo >= modeloLista.size()) return;
        File tmp = modeloLista.get(idx);
        modeloLista.set(idx, modeloLista.get(nuevo));
        modeloLista.set(nuevo, tmp);
        listaArchivos.setSelectedIndex(nuevo);
    }

    private void accionCargarModulo() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos objeto SIC/XE (*.obj)", "obj"));
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        boolean hayNuevos = false;
        for (File f : fc.getSelectedFiles())
            if (!modeloLista.contains(f)) { modeloLista.addElement(f); hayNuevos = true; }
        if (!hayNuevos) return;

        // Usar la dirección base original si ya hay módulos cargados
        int dirBase;
        if (memoria.length > 0) {
            dirBase = dirCargaInicio;
        } else {
            try {
                dirBase = Integer.parseInt(campoDir.getText().trim(), 16);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Dirección de carga inválida. Use formato hexadecimal (ej: 1000).",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        List<File> modulos = new ArrayList<>();
        for (int i = 0; i < modeloLista.size(); i++) modulos.add(modeloLista.get(i));

        try {
            cargarYLigar(modulos, dirBase);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            JOptionPane.showMessageDialog(this,
                "Error durante la carga:\n" + msg, "Error de carga", JOptionPane.ERROR_MESSAGE);
            lblInfo.setText("Error: " + msg);
            lblInfo.setForeground(FG_ERR);
        }
    }

    private void accionLimpiar() {
        modeloLista.clear();
        tabse.clear();
        modificaciones.clear();
        memoria        = new int[0];
        memoriaEscrita = new boolean[0];
        dirCargaInicio = 0;
        dirCargaFin    = 0;
        modeloMemoria.setRowCount(0);
        tablaMemoria.setRowHeight(22);
        lblInfo.setText("Sin programa cargado.");
        lblInfo.setForeground(FG_TEXT);
    }

    private void accionVerTabse() {
        if (tabse.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No hay TABSE. Cargue uno o más programas primero.",
                "TABSE vacía", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new VentanaTabse(tabse);
    }

    private void accionCargar() {
        if (modeloLista.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Agregue al menos un archivo .obj a la lista.",
                "Sin módulos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int dirInicio;
        try {
            dirInicio = Integer.parseInt(campoDir.getText().trim(), 16);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Dirección de carga inválida. Use formato hexadecimal (ej: 1000).",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<File> modulos = new ArrayList<>();
        for (int i = 0; i < modeloLista.size(); i++) modulos.add(modeloLista.get(i));

        try {
            cargarYLigar(modulos, dirInicio);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            JOptionPane.showMessageDialog(this,
                "Error durante la carga:\n" + msg, "Error de carga", JOptionPane.ERROR_MESSAGE);
            lblInfo.setText("Error: " + msg);
            lblInfo.setForeground(FG_ERR);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ALGORITMO DE CARGA Y LIGADO DE DOS PASOS
    // ═══════════════════════════════════════════════════════════════════════
    private record ModuloInfo(String nombre, int dirIniH, int lonsc, int dirsc) {}

    private void cargarYLigar(List<File> modulos, int dirCargaInicial) throws Exception {
        tabse.clear();
        modificaciones.clear();

        // ── PASO 1: H + D  →  TABSE ──────────────────────────────────
        Map<String, Integer> tabseSim = new LinkedHashMap<>();
        List<ModuloInfo>     infos    = new ArrayList<>();
        int dirsc = dirCargaInicial;

        for (File f : modulos) {
            List<String> lns = leerLineas(f);
            if (lns.isEmpty()) throw new Exception("Archivo vacío: " + f.getName());
            String h = lns.get(0);
            if (!h.startsWith("H")) throw new Exception("No empieza con H: " + f.getName());
            // Rellenar con espacios en caso de que el campo de nombre sea menor a 6 chars
            if (h.length() < 19) h = String.format("%-19s", h);

            String nombre  = h.substring(1, 7).trim();
            int    dirIniH = Integer.parseInt(h.substring(7,  13).trim(), 16);
            int    lonsc   = Integer.parseInt(h.substring(13, 19).trim(), 16);

            infos.add(new ModuloInfo(nombre, dirIniH, lonsc, dirsc));
            tabse.add(new EntradaTabse(nombre, "", dirsc, lonsc));
            tabseSim.put(nombre, dirsc);

            for (String linea : lns) {
                if (!linea.startsWith("D")) continue;
                int i = 1;
                while (i < linea.length()) {
                    // Nombre: chars no-espacio hasta espacio o máximo 6 chars
                    int nameStart = i;
                    while (i < linea.length() && i - nameStart < 6 && linea.charAt(i) != ' ')
                        i++;
                    String sym = linea.substring(nameStart, i).trim();
                    // Saltar espacios de relleno
                    while (i < linea.length() && linea.charAt(i) == ' ') i++;
                    // Leer 6 hex como offset
                    if (i + 6 > linea.length()) break;
                    int offset;
                    try { offset = Integer.parseInt(linea.substring(i, i + 6), 16); }
                    catch (NumberFormatException ignored) { break; }
                    i += 6;
                    if (!sym.isEmpty()) {
                        int absDir = dirsc + offset;
                        tabse.add(new EntradaTabse(nombre, sym, absDir, 0));
                        tabseSim.put(sym, absDir);
                    }
                }
            }
            dirsc += lonsc;
        }

        // ── Inicializar memoria ───────────────────────────────────────
        int tamTotal = dirsc - dirCargaInicial;
        // Si el usuario especificó un tamaño, usarlo (trunca o extiende según corresponda)
        String txtTam = campoTam.getText().trim();
        if (!txtTam.isEmpty()) {
            try {
                int tamUser = Integer.parseInt(txtTam, 16);
                if (tamUser > 0) tamTotal = tamUser;
            } catch (NumberFormatException ignored) {
                throw new Exception("Tamaño inválido. Use formato hexadecimal (ej: 4000).");
            }
        }
        if (tamTotal <= 0) throw new Exception("Longitud total inválida (≤ 0).");
        memoria        = new int[tamTotal];
        memoriaEscrita = new boolean[tamTotal]; // false por defecto (no escrito)
        Arrays.fill(memoria, 0xFF);
        dirCargaInicio = dirCargaInicial;
        dirCargaFin    = dirsc;

        // ── PASO 2: T → copiar; M → modificar ────────────────────────
        for (int m = 0; m < modulos.size(); m++) {
            ModuloInfo   info = infos.get(m);
            List<String> lns  = leerLineas(modulos.get(m));

            for (String linea : lns) {

                // ── T record ─────────────────────────────────────────
                if (linea.startsWith("T")) {
                    int    tAddr = Integer.parseInt(linea.substring(1, 7), 16);
                    int    tLen  = Integer.parseInt(linea.substring(7, 9), 16);
                    String cod   = linea.substring(9);
                    int    base  = info.dirsc() + tAddr - info.dirIniH();
                    for (int i = 0; i < tLen; i++) {
                        int b   = Integer.parseInt(cod.substring(i * 2, i * 2 + 2), 16);
                        int idx = base + i - dirCargaInicio;
                        if (idx >= 0 && idx < memoria.length) {
                            memoria[idx]        = b;
                            memoriaEscrita[idx] = true; // marcar como byte real de T record
                        }
                    }

                // ── M record ─────────────────────────────────────────
                } else if (linea.startsWith("M")) {
                    if (linea.length() < 10) continue;
                    int  mAddr   = Integer.parseInt(linea.substring(1, 7), 16);
                    int  nibbles = Integer.parseInt(linea.substring(7, 9), 16);

                    // Buscar '+' o '-' (acepta formato actual y formato viejo con *R/*SE)
                    int signIdx = -1;
                    for (int i = 9; i < linea.length(); i++) {
                        char cc = linea.charAt(i);
                        if (cc == '+' || cc == '-') { signIdx = i; break; }
                    }
                    if (signIdx < 0) continue;

                    char   sign   = linea.charAt(signIdx);
                    String sym    = linea.substring(signIdx + 1).trim();
                    Integer symVal = resolverSimbolo(sym, tabseSim);
                    if (symVal == null) {
                        // Símbolo externo no encontrado en TABSE → detener carga (algoritmo paso 2)
                        actualizarMapaMemoria();
                        String errMsg = String.format(
                            "Símbolo '%s' no encontrado en TABSE.\nCarga interrumpida en módulo '%s' (registro M en %06X).",
                            sym, info.nombre(), info.dirsc() + mAddr - info.dirIniH());
                        lblInfo.setText(String.format(
                            "ERROR: símbolo '%s' no encontrado en TABSE — carga interrumpida", sym));
                        lblInfo.setForeground(FG_ERR);
                        JOptionPane.showMessageDialog(CargadorLigador.this,
                            errMsg, "Error de ligado", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int  absAddr   = info.dirsc() + mAddr - info.dirIniH();
                    int  byteCount = (nibbles + 1) / 2;
                    long mask      = (1L << (nibbles * 4)) - 1L;

                    // Guardar valor inicial (desde T record) antes del primer M
                    for (int i = 0; i < byteCount; i++) {
                        int absA = absAddr + i;
                        if (!modificaciones.containsKey(absA)) {
                            int idx = absA - dirCargaInicio;
                            int orig = (idx >= 0 && idx < memoria.length) ? memoria[idx] : 0xFF;
                            List<Integer> hist = new ArrayList<>();
                            hist.add(orig);
                            modificaciones.put(absA, hist);
                        }
                    }

                    // Leer campo y aplicar modificación
                    long value = 0;
                    for (int i = 0; i < byteCount; i++) {
                        int idx = absAddr + i - dirCargaInicio;
                        value = (value << 8) | ((idx >= 0 && idx < memoria.length)
                                ? memoria[idx] & 0xFFL : 0xFFL);
                    }
                    long field = value & mask;
                    field = (sign == '+') ? (field + symVal) & mask : (field - symVal) & mask;
                    value = (value & ~mask) | field;

                    // Escribir y agregar valor al historial (un estado por cada M record)
                    for (int i = byteCount - 1; i >= 0; i--) {
                        int idx = absAddr + i - dirCargaInicio;
                        int byteVal = (int)(value & 0xFF);
                        if (idx >= 0 && idx < memoria.length) memoria[idx] = byteVal;
                        int absA = absAddr + i;
                        if (modificaciones.containsKey(absA))
                            modificaciones.get(absA).add(byteVal); // un paso más en el historial
                        value >>= 8;
                    }
                }
            }
        }

        // ── Actualizar UI ─────────────────────────────────────────────
        actualizarMapaMemoria();
        int bytesTotal = infos.stream().mapToInt(ModuloInfo::lonsc).sum();
        lblInfo.setText(String.format(
            "Dir. inicio: %06X  |  Tamaño: %XH (%d bytes)  |  Módulos: %d  |  Bytes M: %d",
            dirCargaInicio, bytesTotal, bytesTotal, modulos.size(), modificaciones.size()));
        lblInfo.setForeground(FG_OK);
    }

    private Integer resolverSimbolo(String sym, Map<String, Integer> tabseSim) {
        Integer v = tabseSim.get(sym);
        if (v != null) return v;
        for (Map.Entry<String, Integer> e : tabseSim.entrySet())
            if (e.getKey().equalsIgnoreCase(sym)) return e.getValue();
        return null;
    }

    // ── Poblar tabla: colapsa huecos vacíos en filas "···" ───────────
    private void actualizarMapaMemoria() {
        modeloMemoria.setRowCount(0);
        tablaMemoria.setRowHeight(22);

        int desde = (dirCargaInicio / 16) * 16;
        int hasta = ((dirCargaFin   + 15) / 16) * 16;

        boolean huecoPendiente = false; // hay filas vacías acumuladas sin "···" aún

        for (int addr = desde; addr < hasta; addr += 16) {
            if (esFilaVacia(addr)) {
                // Acumular hueco; se representará con una sola fila "···"
                huecoPendiente = true;
                continue;
            }

            // Antes de agregar datos, volcar el "···" del hueco acumulado
            if (huecoPendiente) {
                Object[] sep = new Object[17];
                sep[0] = FILA_SEPARADOR;
                for (int i = 1; i < 17; i++) sep[i] = "";
                modeloMemoria.addRow(sep);
                huecoPendiente = false;
            }

            // Fila de datos normal
            int modelRow = modeloMemoria.getRowCount();
            Object[] fila = new Object[17];
            fila[0] = String.format("%04X", addr);
            int maxHistDepth = 1; // profundidad máxima de historial en esta fila

            for (int col = 0; col < 16; col++) {
                int absA = addr + col;
                int idx  = absA - dirCargaInicio;
                fila[col + 1] = (idx >= 0 && idx < memoria.length)
                    ? String.format("%02X", memoria[idx]) : "";

                List<Integer> hist = modificaciones.get(absA);
                if (hist != null) maxHistDepth = Math.max(maxHistDepth, hist.size());
            }
            modeloMemoria.addRow(fila);

            // Altura proporcional al número de estados en el historial (20px por estado)
            if (maxHistDepth > 1) tablaMemoria.setRowHeight(modelRow, 20 * maxHistDepth);
        }
    }

    /**
     * Retorna true si la fila (16 bytes) no tiene ningún dato significativo:
     * todos los bytes dentro del rango son 0xFF y ninguno pertenece a un campo M.
     */
    private boolean esFilaVacia(int rowAddr) {
        for (int col = 0; col < 16; col++) {
            int absAddr = rowAddr + col;
            if (modificaciones.containsKey(absAddr)) return false;
            int idx = absAddr - dirCargaInicio;
            // Usar memoriaEscrita: un byte 0xFF puesto por T record NO es vacío
            if (idx >= 0 && idx < memoriaEscrita.length && memoriaEscrita[idx]) return false;
        }
        return true;
    }

    // ── Leer líneas de un archivo .obj ────────────────────────────────
    private List<String> leerLineas(File f) throws IOException {
        List<String> list = new ArrayList<>();
        try (var br = new java.io.BufferedReader(new java.io.FileReader(f))) {
            String s;
            while ((s = br.readLine()) != null) { s = s.strip(); if (!s.isEmpty()) list.add(s); }
        }
        return list;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MODELO DE DATOS — TABSE
    // ═══════════════════════════════════════════════════════════════════════
    public static class EntradaTabse {
        public final String seccion;
        public final String simbolo;
        public final int    direccion;
        public final int    longitud;
        public EntradaTabse(String sec, String sim, int dir, int lon) {
            seccion = sec; simbolo = sim; direccion = dir; longitud = lon;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════════════
    private JButton btn(String texto, ActionListener a) {
        JButton b = new JButton(texto);
        b.setBackground(BG_PANEL); b.setForeground(FG_TEXT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(a);
        return b;
    }

    private JButton btnColor(String texto, Color bg, ActionListener a) {
        JButton b = btn(texto, a);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        return b;
    }

    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(FG_TEXT);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JTextField campo(String defecto, int width) {
        JTextField tf = new JTextField(defecto, 6);
        tf.setBackground(BG_TABLE); tf.setForeground(FG_TEXT);
        tf.setCaretColor(FG_TEXT);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createLineBorder(BORDER_C));
        tf.setMaximumSize(new Dimension(width, 28));
        tf.setPreferredSize(new Dimension(width, 28));
        return tf;
    }

    private javax.swing.border.Border titulado(String titulo) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_C), titulo,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 11), FG_HEADER);
    }
}
