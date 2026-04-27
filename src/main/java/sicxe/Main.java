package sicxe;

import javax.swing.SwingUtilities;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazGrafica gui = new InterfazGrafica();
            if (args != null && args.length > 0) {
                File f = new File(args[0]);
                if (f.exists()) gui.abrirArchivo(f);
            }
        });
    }

    static String cambiarExtension(String ruta, String ext) {
        int p = ruta.lastIndexOf('.');
        return (p >= 0 ? ruta.substring(0, p) : ruta) + ext;
    }
}