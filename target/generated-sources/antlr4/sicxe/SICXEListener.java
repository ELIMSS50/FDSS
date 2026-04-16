// Generated from java-escape by ANTLR 4.11.1
package sicxe;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SICXEParser}.
 */
public interface SICXEListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SICXEParser#programa}.
	 * @param ctx the parse tree
	 */
	void enterPrograma(SICXEParser.ProgramaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#programa}.
	 * @param ctx the parse tree
	 */
	void exitPrograma(SICXEParser.ProgramaContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#encabezado}.
	 * @param ctx the parse tree
	 */
	void enterEncabezado(SICXEParser.EncabezadoContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#encabezado}.
	 * @param ctx the parse tree
	 */
	void exitEncabezado(SICXEParser.EncabezadoContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#cuerpo}.
	 * @param ctx the parse tree
	 */
	void enterCuerpo(SICXEParser.CuerpoContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#cuerpo}.
	 * @param ctx the parse tree
	 */
	void exitCuerpo(SICXEParser.CuerpoContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#lineaOk}.
	 * @param ctx the parse tree
	 */
	void enterLineaOk(SICXEParser.LineaOkContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#lineaOk}.
	 * @param ctx the parse tree
	 */
	void exitLineaOk(SICXEParser.LineaOkContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#lineaError}.
	 * @param ctx the parse tree
	 */
	void enterLineaError(SICXEParser.LineaErrorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#lineaError}.
	 * @param ctx the parse tree
	 */
	void exitLineaError(SICXEParser.LineaErrorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#tokensError}.
	 * @param ctx the parse tree
	 */
	void enterTokensError(SICXEParser.TokensErrorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#tokensError}.
	 * @param ctx the parse tree
	 */
	void exitTokensError(SICXEParser.TokensErrorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#cierre}.
	 * @param ctx the parse tree
	 */
	void enterCierre(SICXEParser.CierreContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#cierre}.
	 * @param ctx the parse tree
	 */
	void exitCierre(SICXEParser.CierreContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#destino}.
	 * @param ctx the parse tree
	 */
	void enterDestino(SICXEParser.DestinoContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#destino}.
	 * @param ctx the parse tree
	 */
	void exitDestino(SICXEParser.DestinoContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#linea}.
	 * @param ctx the parse tree
	 */
	void enterLinea(SICXEParser.LineaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#linea}.
	 * @param ctx the parse tree
	 */
	void exitLinea(SICXEParser.LineaContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#enunciado}.
	 * @param ctx the parse tree
	 */
	void enterEnunciado(SICXEParser.EnunciadoContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#enunciado}.
	 * @param ctx the parse tree
	 */
	void exitEnunciado(SICXEParser.EnunciadoContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#etiqueta}.
	 * @param ctx the parse tree
	 */
	void enterEtiqueta(SICXEParser.EtiquetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#etiqueta}.
	 * @param ctx the parse tree
	 */
	void exitEtiqueta(SICXEParser.EtiquetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#instruccion}.
	 * @param ctx the parse tree
	 */
	void enterInstruccion(SICXEParser.InstruccionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#instruccion}.
	 * @param ctx the parse tree
	 */
	void exitInstruccion(SICXEParser.InstruccionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#modoAcceso}.
	 * @param ctx the parse tree
	 */
	void enterModoAcceso(SICXEParser.ModoAccesoContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#modoAcceso}.
	 * @param ctx the parse tree
	 */
	void exitModoAcceso(SICXEParser.ModoAccesoContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#referencia}.
	 * @param ctx the parse tree
	 */
	void enterReferencia(SICXEParser.ReferenciaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#referencia}.
	 * @param ctx the parse tree
	 */
	void exitReferencia(SICXEParser.ReferenciaContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#reg}.
	 * @param ctx the parse tree
	 */
	void enterReg(SICXEParser.RegContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#reg}.
	 * @param ctx the parse tree
	 */
	void exitReg(SICXEParser.RegContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#directiva}.
	 * @param ctx the parse tree
	 */
	void enterDirectiva(SICXEParser.DirectivaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#directiva}.
	 * @param ctx the parse tree
	 */
	void exitDirectiva(SICXEParser.DirectivaContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#contenidoByte}.
	 * @param ctx the parse tree
	 */
	void enterContenidoByte(SICXEParser.ContenidoByteContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#contenidoByte}.
	 * @param ctx the parse tree
	 */
	void exitContenidoByte(SICXEParser.ContenidoByteContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpresion(SICXEParser.ExpresionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpresion(SICXEParser.ExpresionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#termino}.
	 * @param ctx the parse tree
	 */
	void enterTermino(SICXEParser.TerminoContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#termino}.
	 * @param ctx the parse tree
	 */
	void exitTermino(SICXEParser.TerminoContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#factor}.
	 * @param ctx the parse tree
	 */
	void enterFactor(SICXEParser.FactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#factor}.
	 * @param ctx the parse tree
	 */
	void exitFactor(SICXEParser.FactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SICXEParser#numeral}.
	 * @param ctx the parse tree
	 */
	void enterNumeral(SICXEParser.NumeralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SICXEParser#numeral}.
	 * @param ctx the parse tree
	 */
	void exitNumeral(SICXEParser.NumeralContext ctx);
}