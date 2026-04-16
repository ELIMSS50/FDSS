// Generated from java-escape by ANTLR 4.11.1
package sicxe;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SICXEParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SICXEVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SICXEParser#programa}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrograma(SICXEParser.ProgramaContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#encabezado}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEncabezado(SICXEParser.EncabezadoContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#cuerpo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCuerpo(SICXEParser.CuerpoContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#lineaOk}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLineaOk(SICXEParser.LineaOkContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#lineaError}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLineaError(SICXEParser.LineaErrorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#tokensError}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTokensError(SICXEParser.TokensErrorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#cierre}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCierre(SICXEParser.CierreContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#destino}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestino(SICXEParser.DestinoContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#linea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLinea(SICXEParser.LineaContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#enunciado}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnunciado(SICXEParser.EnunciadoContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#etiqueta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEtiqueta(SICXEParser.EtiquetaContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#instruccion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstruccion(SICXEParser.InstruccionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#modoAcceso}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModoAcceso(SICXEParser.ModoAccesoContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#referencia}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferencia(SICXEParser.ReferenciaContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#reg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReg(SICXEParser.RegContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#directiva}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectiva(SICXEParser.DirectivaContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#contenidoByte}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContenidoByte(SICXEParser.ContenidoByteContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpresion(SICXEParser.ExpresionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#termino}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermino(SICXEParser.TerminoContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(SICXEParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SICXEParser#numeral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeral(SICXEParser.NumeralContext ctx);
}