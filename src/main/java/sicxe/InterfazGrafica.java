package sicxe;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class InterfazGrafica extends JFrame {

    // ── Paleta de colores ─────────────────────────────────────────────
    private static final Color BG_MAIN    = new Color(22, 27, 34);
    private static final Color BG_PANEL   = new Color(30, 36, 45);
    private static final Color BG_TABLE   = new Color(22, 27, 34);
    private static final Color BG_HEADER  = new Color(40, 50, 65);
    private static final Color FG_TEXT    = new Color(210, 220, 230);
    private static final Color FG_HEADER  = new Color(140, 180, 220);
    private static final Color FG_OK      = new Color(60, 200, 100);
    private static final Color FG_ERR     = new Color(220, 70, 70);
    private static final Color ROW_ERR    = new Color(60, 20, 20);
    private static final Color ROW_NORM   = new Color(28, 34, 42);
    private static final Color ROW_ALT    = new Color(33, 40, 50);
    private static final Color BORDER_C   = new Color(50, 65, 85);

    // ── Estado del editor ─────────────────────────────────────────────
    private File    archivoActual = null;
    private boolean modificado    = false;

    // ── Componentes de UI ─────────────────────────────────────────────
    private JTextArea          editorFuente;
    private DefaultTableModel  modeloIntermedio;
    private DefaultTableModel  modeloSimbolos;
    private DefaultTableModel  modeloBloques;
    private JTextArea          areaErrores;
    private JTextPane          areaObjeto;
    private JLabel             lblRuta;
    private JLabel             lblEstado;
    private JLabel             lblTamano;

    // ── Último ensamblado ─────────────────────────────────────────────
    private List<SeccionControl> ultimasSecciones    = new ArrayList<>();
    private List<String>         ultimoRegistros     = new ArrayList<>();
    private int                  ultimaLongitud      = 0;
    private List<File>           ultimosObjs         = new ArrayList<>();
    // Compatibilidad con accionPaso1 (solo Pass 1, sin secciones completas)
    private List<EntradaListado> ultimoListado;
    private Map<String, Integer> ultimoSimbolos;
    private Map<String, String>  ultimoTipoSim;
    private Map<String, Integer> ultimoBloquesSim;
    private List<int[]>          ultimoTablaBloques;
    private List<String>         ultimoNombresBloques;

    // ─────────────────────────────────────────────────────────────────
    public InterfazGrafica() {
        super("ENSAMBLADOR SIC XE");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1420, 820);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { accionSalir(); }
        });

        setJMenuBar(construirMenuBar());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_PANEL);
        topBar.add(construirToolbar(), BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        add(construirPanelCentral(), BorderLayout.CENTER);
        add(construirBarraInferior(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════
    // BARRA DE MENÚ
    // ═══════════════════════════════════════════════════════════════════
    private JMenuBar construirMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));

        // ── ARCHIVO ──────────────────────────────────────────────────
        JMenu mArchivo = menu("ARCHIVO");

        JMenuItem miNuevo     = menuItem("Nuevo",          "Ctrl+N", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        JMenuItem miAbrir     = menuItem("Abrir...",       "Ctrl+O", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);
        JMenuItem miGuardar   = menuItem("Guardar",        "Ctrl+S", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        JMenuItem miGuardarC  = menuItem("Guardar como...", null,    0,             0);
        JMenuItem miSalir     = menuItem("Salir",           null,    0,             0);

        miNuevo   .addActionListener(e -> accionNuevo());
        miAbrir   .addActionListener(e -> accionAbrir());
        miGuardar .addActionListener(e -> accionGuardar());
        miGuardarC.addActionListener(e -> accionGuardarComo());
        miSalir   .addActionListener(e -> accionSalir());

        mArchivo.add(miNuevo); mArchivo.add(miAbrir);
        mArchivo.addSeparator();
        mArchivo.add(miGuardar); mArchivo.add(miGuardarC);
        mArchivo.addSeparator();
        mArchivo.add(miSalir);

        // ── ENSAMBLAR ─────────────────────────────────────────────────
        JMenu mEnsamblar = menu("ENSAMBLAR");

        JMenuItem miAnalizar  = menuItem("Analizar (lex/sint)", "Ctrl+Z", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
        JMenuItem miPaso1     = menuItem("Paso 1",              "Ctrl+Y", KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK);
        JMenuItem miPaso2     = menuItem("Paso 2",              "Ctrl+X", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);
        JMenuItem miEnsamblar = menuItem("Ensamblar",           "Ctrl+E", KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);

        miAnalizar .addActionListener(e -> accionAnalizar());
        miPaso1    .addActionListener(e -> accionPaso1());
        miPaso2    .addActionListener(e -> accionPaso2());
        miEnsamblar.addActionListener(e -> accionEnsamblar());

        mEnsamblar.add(miAnalizar); mEnsamblar.add(miPaso1); mEnsamblar.add(miPaso2);
        mEnsamblar.addSeparator();
        mEnsamblar.add(miEnsamblar);

        // ── CARGAR ────────────────────────────────────────────────────
        JMenu mCargar = menu("CARGAR");
        JMenuItem miCargador = menuItem("Cargador-Ligador...", null, 0, 0);
        miCargador.addActionListener(e -> new CargadorLigador(ultimosObjs));
        mCargar.add(miCargador);

        // ── SIMULAR (deshabilitado) ────────────────────────────────────
        JMenu mSimular = menu("SIMULAR"); mSimular.setEnabled(false);

        // ── Ventanas ──────────────────────────────────────────────────
        JMenu mVentanas = menu("Ventanas");
        JMenuItem miAcerca = menuItem("Acerca de...", null, 0, 0);
        miAcerca.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "ENSAMBLADOR SIC/XE\nPráctica 10 — Interfaz Gráfica de Usuario\n\n" +
            "Utiliza el menú ENSAMBLAR o los botones de la barra inferior.",
            "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        mVentanas.add(miAcerca);

        // ── Ayuda ─────────────────────────────────────────────────────
        JMenu mAyuda = menu("Ayuda");
        JMenuItem miManual = menuItem("Manual de uso", null, 0, 0);
        miManual.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "<html><b>Atajos de teclado</b><br>" +
            "Ctrl+N: Nuevo archivo<br>" +
            "Ctrl+O: Abrir archivo<br>" +
            "Ctrl+S: Guardar<br>" +
            "Ctrl+Z: Analizar léxico/sintáctico<br>" +
            "Ctrl+Y: Paso 1 (archivo intermedio)<br>" +
            "Ctrl+X: Paso 2 (código objeto)<br>" +
            "Ctrl+E: Ensamblar todo</html>",
            "Ayuda", JOptionPane.INFORMATION_MESSAGE));
        mAyuda.add(miManual);

        bar.add(mArchivo); bar.add(mEnsamblar); bar.add(mCargar);
        bar.add(mSimular); bar.add(mVentanas);  bar.add(mAyuda);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════
    // TOOLBAR
    // ═══════════════════════════════════════════════════════════════════
    private JToolBar construirToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBackground(BG_PANEL);
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));
        tb.add(btnTB("Nuevo",    e -> accionNuevo()));
        tb.add(btnTB("Abrir",    e -> accionAbrir()));
        tb.add(btnTB("Guardar",  e -> accionGuardar()));
        tb.addSeparator();
        tb.add(btnTB("Analizar", e -> accionAnalizar()));
        tb.add(btnTB("Paso 1",   e -> accionPaso1()));
        tb.add(btnTB("Paso 2",   e -> accionPaso2()));
        tb.add(btnTB("Ensamblar",e -> accionEnsamblar()));
        tb.addSeparator();
        tb.add(btnTBColor("Limpiar", new Color(180, 70, 50), e -> accionLimpiar()));
        tb.addSeparator();
        tb.add(btnTBColor("Cargador", new Color(60, 90, 140), e -> new CargadorLigador(ultimosObjs)));
        return tb;
    }

    // ═══════════════════════════════════════════════════════════════════
    // PANEL CENTRAL  (splits anidados)
    // ═══════════════════════════════════════════════════════════════════
    private Component construirPanelCentral() {

        // ── Fila superior: Editor | Intermedio | Símbolos | Errores ──
        JSplitPane edVsResto = split(JSplitPane.HORIZONTAL_SPLIT,
            construirPanelEditor(), construirMitadDerecha(), 310);

        // ── Fila inferior: Objeto | Bloques ──────────────────────────
        JSplitPane inferior = split(JSplitPane.HORIZONTAL_SPLIT,
            construirPanelObjeto(), construirPanelBloques(), 720);

        // ── Split vertical entre filas ────────────────────────────────
        return split(JSplitPane.VERTICAL_SPLIT, edVsResto, inferior, 430);
    }

    private Component construirMitadDerecha() {
        JSplitPane intermedioVsSimbErr = split(JSplitPane.HORIZONTAL_SPLIT,
            construirPanelIntermedio(), construirSimbErrores(), 530);
        return intermedioVsSimbErr;
    }

    private Component construirSimbErrores() {
        return split(JSplitPane.HORIZONTAL_SPLIT,
            construirPanelSimbolos(), construirPanelErrores(), 270);
    }

    // ═══════════════════════════════════════════════════════════════════
    // EDITOR DE CÓDIGO FUENTE
    // ═══════════════════════════════════════════════════════════════════
    private JPanel construirPanelEditor() {
        JPanel panel = panelTitulado("PROGRAMA FUENTE SICXE", FG_HEADER);

        editorFuente = new JTextArea();
        editorFuente.setFont(new Font("Monospaced", Font.PLAIN, 13));
        editorFuente.setBackground(BG_TABLE);
        editorFuente.setForeground(FG_TEXT);
        editorFuente.setCaretColor(FG_TEXT);
        editorFuente.setTabSize(8);
        editorFuente.setLineWrap(false);
        editorFuente.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        editorFuente.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { marcarModificado(); }
            public void removeUpdate(DocumentEvent e)  { marcarModificado(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        JScrollPane scroll = scroll(editorFuente);
        NumeroLineas gutter = new NumeroLineas(editorFuente);
        scroll.setRowHeaderView(gutter);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ARCHIVO INTERMEDIO
    // ═══════════════════════════════════════════════════════════════════
    private JPanel construirPanelIntermedio() {
        JPanel panel = panelTitulado("ARCHIVO INTERMEDIO", new Color(255, 200, 80));
        String[] cols = {"LN","BLQ","CP","ETIQUETA","INSTRUCCIÓN","OPERANDO","COD.OBJ","ERROR","SEC"};
        modeloIntermedio = modeloNoEditable(cols);
        JTable t = tabla(modeloIntermedio);
        t.setDefaultRenderer(Object.class, new RendererIntermedio());
        int[] w = {35, 60, 60, 80, 100, 110, 120, 200, 80};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        panel.add(scroll(t), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    // TABLA DE SÍMBOLOS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel construirPanelSimbolos() {
        JPanel panel = panelTitulado("TABLA DE SÍMBOLOS", new Color(100, 220, 160));
        String[] cols = {"Símbolo","Dirección/Valor","Tipo","No. Bloque","Ext.","Sección"};
        modeloSimbolos = modeloNoEditable(cols);
        JTable t = tabla(modeloSimbolos);
        t.setDefaultRenderer(Object.class, new RendererSimbolos());
        int[] w = {90, 100, 75, 85, 45, 80};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        panel.add(scroll(t), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    // PANEL DE ERRORES
    // ═══════════════════════════════════════════════════════════════════
    private JPanel construirPanelErrores() {
        JPanel panel = panelTitulado("ERRORES", FG_ERR);
        areaErrores = areaTexto(FG_ERR, false);
        panel.add(scroll(areaErrores), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CÓDIGO OBJETO
    // ═══════════════════════════════════════════════════════════════════
    private JPanel construirPanelObjeto() {
        JPanel panel = panelTitulado("CÓDIGO OBJETO (REGISTROS)", new Color(255, 215, 100));
        areaObjeto = new JTextPane();
        areaObjeto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaObjeto.setBackground(BG_TABLE);
        areaObjeto.setEditable(false);
        areaObjeto.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        panel.add(scroll(areaObjeto), BorderLayout.CENTER);
        return panel;
    }

    private void mostrarRegistrosColoreados(List<String> registros) {
        StyledDocument doc = areaObjeto.getStyledDocument();
        try { doc.remove(0, doc.getLength()); } catch (Exception ex) {}
        for (String r : registros) {
            char tipo = r.isEmpty() ? 0 : r.charAt(0);
            Color c = switch (tipo) {
                case 'H' -> new Color(100, 200, 255);
                case 'D' -> new Color(255, 200,  80);
                case 'R' -> new Color(180, 130, 255);
                case 'T' -> new Color(255, 215, 100);
                case 'M' -> new Color(200, 100, 255);
                case 'E' -> new Color( 60, 200, 100);
                default  -> FG_TEXT;
            };
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, c);
            StyleConstants.setFontFamily(attrs, "Monospaced");
            StyleConstants.setFontSize(attrs, 12);
            try { doc.insertString(doc.getLength(), r + "\n", attrs); }
            catch (Exception ex) {}
        }
        areaObjeto.setCaretPosition(0);
    }

    // ═══════════════════════════════════════════════════════════════════
    // TABLA DE BLOQUES
    // ═══════════════════════════════════════════════════════════════════
    private JPanel construirPanelBloques() {
        JPanel panel = panelTitulado("TABLA DE BLOQUES", new Color(180, 200, 255));
        String[] cols = {"No. Bloque","Nombre","Longitud","Dir. Inicio","Sección"};
        modeloBloques = modeloNoEditable(cols);
        JTable t = tabla(modeloBloques);
        t.setDefaultRenderer(Object.class, new RendererBloques());
        int[] w = {80, 130, 90, 90, 80};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        panel.add(scroll(t), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    // BARRA INFERIOR (botones + barra de estado)
    // ═══════════════════════════════════════════════════════════════════
    private JPanel construirBarraInferior() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BG_PANEL);
        container.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
        panelBotones.setBackground(BG_PANEL);
        panelBotones.add(btnAccion("Analizar código",            e -> accionAnalizar()));
        panelBotones.add(btnAccion("Paso 1: Archivo intermedio", e -> accionPaso1()));
        panelBotones.add(btnAccion("Paso 2: Código objeto",      e -> accionPaso2()));
        panelBotones.add(btnAccion("Ensamblar",                  e -> accionEnsamblar()));
        //JButton bCargar = btnAccion("Cargar en memoria", null); bCargar.setEnabled(false);
        JButton bAbrir  = btnAccion("Abrir prog. objeto", null); bAbrir .setEnabled(false);
        //panelBotones.add(bCargar);
        panelBotones.add(bAbrir);
        panelBotones.add(Box.createHorizontalStrut(12));
        JButton bLimpiar = btnAccionColor("Limpiar todo", new Color(160, 55, 40), e -> accionLimpiar());
        panelBotones.add(bLimpiar);

        // Barra de estado
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(20, 25, 32));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));

        lblRuta = new JLabel("  Sin archivo");
        lblRuta.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblRuta.setForeground(new Color(130, 145, 160));
        lblRuta.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 2));
        rightStatus.setBackground(new Color(20, 25, 32));
        lblTamano = lbl("Tamaño: ---", new Color(130, 145, 160));
        lblEstado  = lbl("●  Listo",   new Color(130, 145, 160));
        lblEstado.setFont(new Font("Monospaced", Font.BOLD, 11));
        rightStatus.add(lblTamano);
        rightStatus.add(lblEstado);

        statusBar.add(lblRuta,       BorderLayout.WEST);
        statusBar.add(rightStatus,   BorderLayout.EAST);

        container.add(panelBotones, BorderLayout.NORTH);
        container.add(statusBar,    BorderLayout.SOUTH);
        return container;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACCIONES DE ARCHIVO
    // ═══════════════════════════════════════════════════════════════════
    private void accionNuevo() {
        if (!confirmarCambios()) return;
        editorFuente.setText("");
        archivoActual = null;
        modificado    = false;
        resetUltimoEnsamblado();
        limpiarResultados();
        actualizarTitulo();
        lblRuta.setText("  Sin archivo");
        setStatus("●  Listo", new Color(130, 145, 160));
        lblTamano.setText("Tamaño: ---");
    }

    private void accionAbrir() {
        if (!confirmarCambios()) return;
        JFileChooser fc = fileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            abrirArchivo(fc.getSelectedFile());
    }

    public void abrirArchivo(File f) {
        try {
            editorFuente.setText(new String(Files.readAllBytes(f.toPath())));
            editorFuente.setCaretPosition(0);
            archivoActual = f;
            modificado    = false;
            resetUltimoEnsamblado();
            limpiarResultados();
            actualizarTitulo();
            lblRuta.setText("  " + f.getAbsolutePath());
            setStatus("●  Archivo cargado", FG_OK);
        } catch (IOException ex) {
            error("Error al abrir:\n" + ex.getMessage());
        }
    }

    private void accionGuardar() {
        if (archivoActual == null) { accionGuardarComo(); return; }
        guardarEn(archivoActual);
    }

    private void accionGuardarComo() {
        JFileChooser fc = fileChooser();
        if (archivoActual != null) fc.setSelectedFile(archivoActual);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".asm")) f = new File(f.getAbsolutePath() + ".asm");
            guardarEn(f);
        }
    }

    private void guardarEn(File f) {
        try {
            Files.write(f.toPath(), editorFuente.getText().getBytes());
            archivoActual = f;
            modificado    = false;
            actualizarTitulo();
            lblRuta.setText("  " + f.getAbsolutePath());
            setStatus("●  Guardado", FG_OK);
        } catch (IOException ex) {
            error("Error al guardar:\n" + ex.getMessage());
        }
    }

    private void accionSalir() {
        if (!confirmarCambios()) return;
        System.exit(0);
    }

    private void accionLimpiar() {
        if (modificado || !editorFuente.getText().isBlank()) {
            int r = JOptionPane.showConfirmDialog(this,
                "¿Limpiar el editor y todos los resultados?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
        }
        editorFuente.setText("");
        archivoActual = null;
        modificado    = false;
        resetUltimoEnsamblado();
        limpiarResultados();
        actualizarTitulo();
        lblRuta.setText("  Sin archivo");
        setStatus("●  Listo", new Color(130, 145, 160));
        lblTamano.setText("Tamaño: ---");
        editorFuente.requestFocus();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACCIONES DE ENSAMBLADO
    // ═══════════════════════════════════════════════════════════════════
    private void accionAnalizar() {
        if (!hayContenido()) return;
        try {
            CharStream cs = CharStreams.fromString(textoEditor());
            SICXELexer lexer = new SICXELexer(cs);
            RecolectorErrores rec = new RecolectorErrores();
            lexer.removeErrorListeners(); lexer.addErrorListener(rec);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SICXEParser parser = new SICXEParser(tokens);
            parser.removeErrorListeners(); parser.addErrorListener(rec);
            parser.programa();

            List<String> err = new ArrayList<>(rec.getErrores());
            limpiarResultados();
            mostrarErrores(err, Collections.emptyList(), Collections.emptyList());

            if (err.isEmpty())
                setStatus("●  Sin errores sintácticos", FG_OK);
            else
                setStatus("✘  " + err.size() + " error(es) sintáctico(s)", FG_ERR);

        } catch (Exception ex) { error("Error al analizar:\n" + ex.getMessage()); }
    }

    private void accionPaso1() {
        if (!hayContenido()) return;
        try {
            AnalizadorSemantico sem = ejecutarPaso1();
            List<String> errSem = new ArrayList<>(sem.getErrores());

            limpiarResultados();
            poblarIntermedio(ultimasSecciones, null);
            poblarSimbolos(ultimasSecciones);
            poblarBloques(ultimasSecciones);
            mostrarErrores(Collections.emptyList(), errSem, Collections.emptyList());

            lblTamano.setText(String.format("Tamaño: %04Xh", ultimaLongitud));
            setStatus(errSem.isEmpty() ? "●  Paso 1 OK" : "✘  " + errSem.size() + " error(es)",
                      errSem.isEmpty() ? FG_OK : FG_ERR);

        } catch (Exception ex) { error("Error en Paso 1:\n" + ex.getMessage()); }
    }

    private void accionPaso2() {
        if (!hayContenido()) return;
        try {
            AnalizadorSemantico sem = ejecutarPaso1();
            List<String> errSem = new ArrayList<>(sem.getErrores());

            List<GeneradorCodigo> gens = ejecutarPaso2(sem);
            List<String> errP2 = new ArrayList<>();
            for (GeneradorCodigo g : gens) errP2.addAll(g.getErrores());

            limpiarResultados();
            poblarIntermedio(ultimasSecciones, null);
            poblarSimbolos(ultimasSecciones);
            poblarBloques(ultimasSecciones);
            mostrarRegistrosColoreados(ultimoRegistros);
            mostrarErrores(Collections.emptyList(), errSem, errP2);

            int total = errSem.size() + errP2.size();
            lblTamano.setText(String.format("Tamaño: %04Xh", ultimaLongitud));
            setStatus(total == 0 ? "●  Paso 2 OK" : "✘  " + total + " error(es)",
                      total == 0 ? FG_OK : FG_ERR);

        } catch (Exception ex) { error("Error en Paso 2:\n" + ex.getMessage()); }
    }

    private void accionEnsamblar() {
        if (!hayContenido()) return;
        try {
            // ── Paso 1 ────────────────────────────────────────────────
            CharStream cs = CharStreams.fromString(textoEditor());
            SICXELexer lexer = new SICXELexer(cs);
            RecolectorErrores rec = new RecolectorErrores();
            lexer.removeErrorListeners(); lexer.addErrorListener(rec);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SICXEParser parser = new SICXEParser(tokens);
            parser.removeErrorListeners(); parser.addErrorListener(rec);
            SICXEParser.ProgramaContext arbol = parser.programa();

            AnalizadorSemantico sem = new AnalizadorSemantico();
            new ParseTreeWalker().walk(sem, arbol);

            List<String> errSint = new ArrayList<>(rec.getErrores());
            List<String> errSem  = new ArrayList<>(sem.getErrores());
            capturarPaso1(sem);

            // ── Paso 2 ────────────────────────────────────────────────
            List<GeneradorCodigo> gens = ejecutarPaso2(sem);
            List<String> errP2 = new ArrayList<>();
            for (GeneradorCodigo g : gens) errP2.addAll(g.getErrores());

            // ── Mostrar resultados ────────────────────────────────────
            limpiarResultados();
            poblarIntermedio(ultimasSecciones, null);
            poblarSimbolos(ultimasSecciones);
            poblarBloques(ultimasSecciones);
            mostrarRegistrosColoreados(ultimoRegistros);
            mostrarErrores(errSint, errSem, errP2);

            // ── Escribir archivos de salida ───────────────────────────
            if (archivoActual != null)
                escribirArchivos(sem, errSint, errSem, errP2);

            int total = errSint.size() + errSem.size() + errP2.size();
            lblTamano.setText(String.format("Tamaño: %04Xh bytes", ultimaLongitud));
            setStatus(total == 0 ? "●  Ensamblado correctamente" : "✘  " + total + " error(es)",
                      total == 0 ? FG_OK : FG_ERR);

            new VentanaResultados(
                archivoActual != null ? archivoActual.getName() : "Sin título",
                ultimasSecciones, errSint, errSem, errP2);

        } catch (Exception ex) { error("Error al ensamblar:\n" + ex.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // LÓGICA INTERNA DE ENSAMBLADO
    // ═══════════════════════════════════════════════════════════════════
    private AnalizadorSemantico ejecutarPaso1() throws Exception {
        CharStream cs = CharStreams.fromString(textoEditor());
        SICXELexer lexer = new SICXELexer(cs);
        RecolectorErrores rec = new RecolectorErrores();
        lexer.removeErrorListeners(); lexer.addErrorListener(rec);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SICXEParser parser = new SICXEParser(tokens);
        parser.removeErrorListeners(); parser.addErrorListener(rec);
        SICXEParser.ProgramaContext arbol = parser.programa();

        AnalizadorSemantico sem = new AnalizadorSemantico();
        new ParseTreeWalker().walk(sem, arbol);
        capturarPaso1(sem);
        return sem;
    }

    private void capturarPaso1(AnalizadorSemantico sem) {
        ultimasSecciones     = sem.getSecciones();
        ultimaLongitud       = sem.getLongitudPrograma();
        ultimoRegistros      = new ArrayList<>();
        // Compatibilidad legacy
        ultimoListado        = sem.getListado();
        ultimoSimbolos       = sem.getTablaSimbolos();
        ultimoTipoSim        = sem.getTipoSimbolos();
        ultimoBloquesSim     = sem.getBloqueSimbolos();
        ultimoNombresBloques = sem.getNombresBloques();
        ultimoTablaBloques   = sem.getTablaBloques();
    }

    private List<GeneradorCodigo> ejecutarPaso2(AnalizadorSemantico sem) throws Exception {
        List<GeneradorCodigo> generadores = new ArrayList<>();
        List<String>          todosRegs   = new ArrayList<>();
        ultimosObjs.clear();

        List<SeccionControl> todas = sem.getSecciones();
        for (SeccionControl sec : todas) {
            GeneradorCodigo gen = new GeneradorCodigo(sec);
            if (sec.esPrincipal) gen.setTodasSecciones(todas);
            gen.generar();

            String rutaObj;
            if (archivoActual != null) {
                String base = Main.cambiarExtension(archivoActual.getAbsolutePath(), "");
                rutaObj = base + (sem.getSecciones().size() > 1 ? "_" + sec.nombre : "") + ".obj";
            } else {
                File tmp = File.createTempFile("sicxe_" + sec.nombre + "_", ".obj");
                tmp.deleteOnExit();
                rutaObj = tmp.getAbsolutePath();
            }
            gen.escribirObj(rutaObj);
            ultimosObjs.add(new File(rutaObj));

            generadores.add(gen);

            if (!todosRegs.isEmpty()) todosRegs.add("");
            todosRegs.add("─── SECCIÓN: " + sec.nombre + " ───────────────────");
            todosRegs.addAll(gen.getRegistrosObjeto());
            sec.registrosObjeto.clear();
            sec.registrosObjeto.addAll(gen.getRegistrosObjeto());
        }

        ultimoRegistros = todosRegs;
        return generadores;
    }

    private void escribirArchivos(AnalizadorSemantico sem,
                                   List<String> eS, List<String> eSem, List<String> eP2) {
        try {
            String ruta = archivoActual.getAbsolutePath();
            String base = Main.cambiarExtension(ruta, "");
            boolean multi = sem.getSecciones().size() > 1;

            sem.generarCSV(Main.cambiarExtension(ruta, ".csv"));

            for (SeccionControl sec : sem.getSecciones()) {
                String sufijo = multi ? "_" + sec.nombre : "";
                escribirTablaSim(sec, base + sufijo + ".sim");
                escribirTablaBlk(sec, base + sufijo + ".blk");
            }

            List<String> todos = new ArrayList<>();
            todos.addAll(eS); todos.addAll(eSem); todos.addAll(eP2);
            if (!todos.isEmpty()) {
                List<String> lineas = new ArrayList<>();
                if (!eS.isEmpty())   { lineas.add("=== Errores Sintácticos ===");  lineas.addAll(eS);   lineas.add(""); }
                if (!eSem.isEmpty()) { lineas.add("=== Errores Semánticos ===");   lineas.addAll(eSem); lineas.add(""); }
                if (!eP2.isEmpty())  { lineas.add("=== Errores Paso 2 ===");       lineas.addAll(eP2); }
                Files.write(Paths.get(Main.cambiarExtension(ruta, ".err")), lineas);
            }
        } catch (Exception ex) {
            System.err.println("Advertencia — archivos de salida: " + ex.getMessage());
        }
    }

    private void escribirTablaSim(SeccionControl sec, String ruta) throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add("=== TABLA DE SÍMBOLOS: " + sec.nombre + " ===");
        lineas.add(String.format("%-12s %-10s %-6s %-20s %-8s", "SÍMBOLO", "VALOR(HEX)", "TIPO", "BLOQUE", "ROL"));
        lineas.add("-".repeat(60));
        for (Map.Entry<String, Integer> en : sec.tablaSimbolos.entrySet()) {
            String sym  = en.getKey();
            int    val  = en.getValue();
            String tipo = sec.tipoSimbolos.getOrDefault(sym, "R");
            int    blk  = sec.bloqueSimbolos.getOrDefault(sym, 0);
            boolean esExt = sec.simbolosExternos.contains(sym);
            boolean esExp = sec.simbolosExportados.contains(sym);
            String blqStr = esExt ? "—"
                : (blk >= 0 && blk < sec.nombresBloques.size()
                    ? blk + " (" + sec.nombresBloques.get(blk) + ")" : String.valueOf(blk));
            String rol = esExt ? "EXTREF" : esExp ? "EXTDEF" : "";
            lineas.add(String.format("%-12s %-10s %-6s %-20s %-8s",
                sym, esExt ? "N/A" : String.format("%04X", val), tipo, blqStr, rol));
        }
        Files.write(Paths.get(ruta), lineas);
    }

    private void escribirTablaBlk(SeccionControl sec, String ruta) throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add("=== TABLA DE BLOQUES: " + sec.nombre + " ===");
        lineas.add(String.format("%-10s %-14s %-12s %-12s", "NO.BLOQUE", "NOMBRE", "LONGITUD", "DIR.INICIO"));
        lineas.add("-".repeat(50));
        for (int[] b : sec.tablaBloques) {
            String nom = (b[0] >= 0 && b[0] < sec.nombresBloques.size())
                ? sec.nombresBloques.get(b[0]) : "?";
            lineas.add(String.format("%-10d %-14s %-12s %-12s",
                b[0], nom, String.format("%04X", b[2]), String.format("%04X", b[3])));
        }
        Files.write(Paths.get(ruta), lineas);
    }

    // ═══════════════════════════════════════════════════════════════════
    // POBLAR PANELES
    // ═══════════════════════════════════════════════════════════════════
    private void poblarIntermedio(List<SeccionControl> secciones, Set<String> reloc) {
        modeloIntermedio.setRowCount(0);
        if (secciones == null) return;
        for (SeccionControl sec : secciones) {
            for (EntradaListado e : sec.listado) {
                String blqStr = numBloque(e.noBloque, sec.nombresBloques);
                String cod    = nvl(e.codigoObjeto);
                // Marcar con * si tiene registro M
                if (!cod.isBlank() && ("EX".equals(e.tipoExpresion) || "R".equals(e.tipoExpresion)))
                    cod += "*";
                String err = nvl(e.error);
                if (!nvl(e.errorP2).isBlank())
                    err = err.isBlank() ? "[P2] " + e.errorP2 : err + " | [P2] " + e.errorP2;

                modeloIntermedio.addRow(new Object[]{
                    e.numeroLinea, blqStr,
                    String.format("%04X", e.contloc),
                    nvl(e.etiqueta), nvl(e.instruccion), nvl(e.operando),
                    cod, err, sec.nombre
                });
            }
        }
    }

    private void poblarSimbolos(List<SeccionControl> secciones) {
        modeloSimbolos.setRowCount(0);
        if (secciones == null) return;
        for (SeccionControl sec : secciones) {
            for (Map.Entry<String, Integer> en : sec.tablaSimbolos.entrySet()) {
                String sym  = en.getKey();
                int    val  = en.getValue();
                String tipo = sec.tipoSimbolos.getOrDefault(sym, "R");
                int    blk  = sec.bloqueSimbolos.getOrDefault(sym, -1);
                boolean esExt = sec.simbolosExternos.contains(sym);
                String blqStr = esExt ? "—" : numBloque(blk, sec.nombresBloques);
                String tipoStr;
                if (esExt)           tipoStr = "E (Ext)";
                else if ("A".equals(tipo)) tipoStr = "A (Abs)";
                else                       tipoStr = "R (Rel)";
                modeloSimbolos.addRow(new Object[]{
                    sym,
                    esExt ? "N/A" : String.format("%04X", val),
                    tipoStr, blqStr,
                    esExt ? "EXTREF" : (sec.simbolosExportados.contains(sym) ? "EXTDEF" : ""),
                    sec.nombre
                });
            }
        }
    }

    private void poblarBloques(List<SeccionControl> secciones) {
        modeloBloques.setRowCount(0);
        if (secciones == null) return;
        for (SeccionControl sec : secciones) {
            for (int[] b : sec.tablaBloques) {
                String nom = (b[0] >= 0 && b[0] < sec.nombresBloques.size())
                    ? sec.nombresBloques.get(b[0]) : "?";
                modeloBloques.addRow(new Object[]{
                    b[0], nom,
                    String.format("%04X", b[2]),
                    String.format("%04X", b[3]),
                    sec.nombre
                });
            }
        }
    }

    private void mostrarErrores(List<String> eS, List<String> eSem, List<String> eP2) {
        StringBuilder sb = new StringBuilder();
        if (!eS.isEmpty()) {
            sb.append("=== Errores Sintácticos (").append(eS.size()).append(") ===\n");
            eS.forEach(e -> sb.append(e).append('\n'));
            sb.append('\n');
        }
        if (!eSem.isEmpty()) {
            sb.append("=== Errores Semánticos (").append(eSem.size()).append(") ===\n");
            eSem.forEach(e -> sb.append(e).append('\n'));
            sb.append('\n');
        }
        if (!eP2.isEmpty()) {
            sb.append("=== Errores Paso 2 (").append(eP2.size()).append(") ===\n");
            eP2.forEach(e -> sb.append(e).append('\n'));
        }
        areaErrores.setText(sb.toString());
        if (sb.length() > 0) areaErrores.setCaretPosition(0);
    }

    private void limpiarResultados() {
        modeloIntermedio.setRowCount(0);
        modeloSimbolos.setRowCount(0);
        modeloBloques.setRowCount(0);
        areaErrores.setText("");
        areaObjeto.setText("");
    }

    private void resetUltimoEnsamblado() {
        ultimasSecciones = new ArrayList<>();
        ultimoRegistros  = new ArrayList<>();
        ultimaLongitud   = 0;
        ultimoListado    = null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS DE UI
    // ═══════════════════════════════════════════════════════════════════
    private JPanel panelTitulado(String titulo, Color colorTitulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        JLabel lbl = new JLabel("  " + titulo);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        lbl.setForeground(colorTitulo);
        lbl.setBackground(BG_HEADER);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG_MAIN);
        sp.getViewport().setBackground(c instanceof JTextArea ? BG_TABLE : BG_TABLE);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        return sp;
    }

    private JTextArea areaTexto(Color fg, boolean editable) {
        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setBackground(BG_TABLE);
        ta.setForeground(fg);
        ta.setEditable(editable);
        ta.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return ta;
    }

    private DefaultTableModel modeloNoEditable(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable tabla(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setBackground(BG_TABLE);
        t.setForeground(FG_TEXT);
        t.setFont(new Font("Monospaced", Font.PLAIN, 12));
        t.setRowHeight(22);
        t.setGridColor(BORDER_C);
        t.setShowGrid(true);
        t.setSelectionBackground(new Color(60, 80, 110));
        t.setSelectionForeground(Color.WHITE);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.getTableHeader().setBackground(BG_HEADER);
        t.getTableHeader().setForeground(FG_HEADER);
        t.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));
        return t;
    }

    private JSplitPane split(int orient, Component a, Component b, int divider) {
        JSplitPane sp = new JSplitPane(orient, a, b);
        sp.setDividerLocation(divider);
        sp.setBackground(BG_MAIN);
        sp.setBorder(null);
        sp.setDividerSize(4);
        return sp;
    }

    private JMenu menu(String texto) {
        JMenu m = new JMenu(texto);
        m.setFont(new Font("SansSerif", Font.BOLD, 12));
        m.setForeground(FG_TEXT);
        m.setBackground(BG_PANEL);
        return m;
    }

    private JMenuItem menuItem(String texto, String atajo, int key, int mod) {
        JMenuItem mi = new JMenuItem(texto);
        mi.setFont(new Font("SansSerif", Font.PLAIN, 12));
        mi.setBackground(BG_PANEL);
        mi.setForeground(FG_TEXT);
        if (key != 0) mi.setAccelerator(KeyStroke.getKeyStroke(key, mod));
        return mi;
    }

    private JButton btnTB(String texto, ActionListener al) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.PLAIN, 11));
        b.setBackground(BG_HEADER);
        b.setForeground(FG_TEXT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        if (al != null) b.addActionListener(al);
        return b;
    }

    private JButton btnTBColor(String texto, Color bg, ActionListener al) {
        JButton b = btnTB(texto, al);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        return b;
    }

    private JButton btnAccion(String texto, ActionListener al) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setBackground(new Color(45, 55, 72));
        b.setForeground(FG_TEXT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        if (al != null) b.addActionListener(al);
        return b;
    }

    private JButton btnAccionColor(String texto, Color bg, ActionListener al) {
        JButton b = btnAccion(texto, al);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        return b;
    }

    private JLabel lbl(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Monospaced", Font.PLAIN, 11));
        l.setForeground(color);
        return l;
    }

    private JFileChooser fileChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(
            archivoActual != null ? archivoActual.getParent()
                : System.getProperty("user.dir") + File.separator + "ejemplos"));
        fc.setFileFilter(new FileNameExtensionFilter("Archivos ensamblador (*.asm)", "asm"));
        return fc;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS GENERALES
    // ═══════════════════════════════════════════════════════════════════
    private boolean confirmarCambios() {
        if (!modificado) return true;
        int r = JOptionPane.showConfirmDialog(this,
            "Hay cambios sin guardar. ¿Desea guardarlos?",
            "Cambios sin guardar", JOptionPane.YES_NO_CANCEL_OPTION);
        if (r == JOptionPane.YES_OPTION) { accionGuardar(); return !modificado; }
        return r == JOptionPane.NO_OPTION;
    }

    private boolean hayContenido() {
        if (editorFuente.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay programa fuente en el editor.",
                "Sin contenido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void marcarModificado() {
        if (!modificado) { modificado = true; actualizarTitulo(); }
    }

    private void actualizarTitulo() {
        String nom = archivoActual != null ? archivoActual.getName() : "Sin título";
        setTitle("ENSAMBLADOR SIC XE — " + nom + (modificado ? " *" : ""));
    }

    private void setStatus(String texto, Color color) {
        lblEstado.setText(texto);
        lblEstado.setForeground(color);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String textoEditor() {
        String t = editorFuente.getText();
        return t.endsWith("\n") ? t : t + "\n";
    }

    private String nvl(String v) { return v != null ? v : ""; }

    private String numBloque(int blk, List<String> nombres) {
        if (blk >= 0 && blk < nombres.size()) return blk + " (" + nombres.get(blk) + ")";
        return String.valueOf(blk);
    }

    // ═══════════════════════════════════════════════════════════════════
    // RENDERERS DE TABLAS
    // ═══════════════════════════════════════════════════════════════════
    private class RendererIntermedio extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            setFont(new Font("Monospaced", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            String err = (String) t.getValueAt(row, 7);
            boolean hayError = err != null && !err.isBlank();
            if (sel) { setBackground(new Color(60, 80, 110)); setForeground(Color.WHITE); }
            else if (hayError) {
                setBackground(ROW_ERR);
                setForeground(col == 7 ? new Color(255, 80, 80) : new Color(255, 160, 160));
            } else {
                setBackground(row % 2 == 0 ? ROW_NORM : ROW_ALT);
                setForeground(
                    col == 2 ? new Color(100, 200, 255) :
                    col == 4 ? new Color(180, 230, 150) :
                    col == 6 ? new Color(255, 215, 100) :
                    col == 1 ? new Color(180, 200, 255) :
                    FG_TEXT);
            }
            return this;
        }
    }

    private class RendererSimbolos extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            setFont(new Font("Monospaced", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            if (sel) { setBackground(new Color(60, 80, 110)); setForeground(Color.WHITE); }
            else {
                setBackground(row % 2 == 0 ? ROW_NORM : ROW_ALT);
                boolean abs = "A (Abs)".equals(t.getValueAt(row, 2));
                setForeground(
                    col == 0 ? new Color(180, 230, 150) :
                    col == 1 ? new Color(100, 200, 255) :
                    col == 2 ? (abs ? new Color(255, 180, 80) : new Color(100, 230, 160)) :
                    col == 3 ? new Color(180, 200, 255) : FG_TEXT);
            }
            return this;
        }
    }

    private class RendererBloques extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            setFont(new Font("Monospaced", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            if (sel) { setBackground(new Color(60, 80, 110)); setForeground(Color.WHITE); }
            else {
                setBackground(row % 2 == 0 ? ROW_NORM : ROW_ALT);
                setForeground(
                    col == 0 ? new Color(200, 160, 255) :
                    col == 1 ? new Color(180, 230, 150) :
                    col == 2 ? new Color(255, 215, 100) :
                    col == 3 ? new Color(100, 200, 255) : FG_TEXT);
            }
            return this;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // COMPONENTE: NÚMEROS DE LÍNEA
    // ═══════════════════════════════════════════════════════════════════
    static class NumeroLineas extends JPanel implements DocumentListener {
        private final JTextArea editor;

        NumeroLineas(JTextArea ed) {
            this.editor = ed;
            ed.getDocument().addDocumentListener(this);
            setBackground(new Color(40, 50, 65));
            setForeground(new Color(100, 130, 160));
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50, 65, 85)));
            setPreferredSize(new Dimension(42, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(42, editor.getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setFont(editor.getFont());
            g.setColor(new Color(100, 130, 160));
            FontMetrics fm    = g.getFontMetrics();
            int         lh    = fm.getHeight();
            int         count = editor.getDocument().getDefaultRootElement().getElementCount();
            int         y     = editor.getInsets().top + fm.getAscent();
            for (int i = 1; i <= count; i++) {
                String s = String.valueOf(i);
                g.drawString(s, getWidth() - fm.stringWidth(s) - 5, y);
                y += lh;
            }
        }

        private void actualizar() { setPreferredSize(getPreferredSize()); repaint(); revalidate(); }
        @Override public void insertUpdate(DocumentEvent e)  { actualizar(); }
        @Override public void removeUpdate(DocumentEvent e)  { actualizar(); }
        @Override public void changedUpdate(DocumentEvent e) {}
    }
}
