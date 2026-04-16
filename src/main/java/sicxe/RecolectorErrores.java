package sicxe;

import org.antlr.v4.runtime.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recolector de errores léxicos y sintácticos.
 * Equivale a VerboseErrorListener.cs del proyecto C#.
 */
public class RecolectorErrores extends BaseErrorListener {

    private final List<String> errores = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object simbolo,
                            int linea,
                            int columna,
                            String mensaje,
                            RecognitionException ex) {

        String tokenInfo = "";
        if (simbolo instanceof Token tk) {
            String txt = tk.getText();
            if (txt != null && !txt.isBlank() && !txt.equals("<EOF>")) {
                tokenInfo = " '" + txt + "'";
            }
        }

        errores.add(String.format(
            "x Error en linea %d, columna %d: %s%s",
            linea, columna,
            mensaje.replace("'", "'"),
            tokenInfo
        ));
    }

    public List<String> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }
}
