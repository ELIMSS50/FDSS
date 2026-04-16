// Generated from java-escape by ANTLR 4.11.1
package sicxe;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class SICXEParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		KSTART=1, KEND=2, KBYTE=3, KWORD=4, KRESW=5, KRESB=6, KBASE=7, KEQU=8, 
		KORG=9, KUSE=10, KRSUB=11, OPCODE_F1=12, OPCODE_F2_RR=13, OPCODE_F2_RN=14, 
		OPCODE_F2_R=15, OPCODE=16, XREG=17, REG=18, LITC=19, LITX=20, HEXNUM=21, 
		ID=22, NUMBER=23, MAS=24, MENOS=25, MUL=26, DIV=27, HASH=28, ARROBA=29, 
		COMA=30, PAREN_IZQ=31, PAREN_DER=32, COM_PUNTO=33, COM_PCOMA=34, ESPACIOS=35, 
		NEWLINE=36;
	public static final int
		RULE_programa = 0, RULE_encabezado = 1, RULE_cuerpo = 2, RULE_lineaOk = 3, 
		RULE_lineaError = 4, RULE_tokensError = 5, RULE_cierre = 6, RULE_destino = 7, 
		RULE_linea = 8, RULE_enunciado = 9, RULE_etiqueta = 10, RULE_instruccion = 11, 
		RULE_modoAcceso = 12, RULE_referencia = 13, RULE_reg = 14, RULE_directiva = 15, 
		RULE_contenidoByte = 16, RULE_expresion = 17, RULE_termino = 18, RULE_factor = 19, 
		RULE_numeral = 20;
	private static String[] makeRuleNames() {
		return new String[] {
			"programa", "encabezado", "cuerpo", "lineaOk", "lineaError", "tokensError", 
			"cierre", "destino", "linea", "enunciado", "etiqueta", "instruccion", 
			"modoAcceso", "referencia", "reg", "directiva", "contenidoByte", "expresion", 
			"termino", "factor", "numeral"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'START'", "'END'", "'BYTE'", "'WORD'", "'RESW'", "'RESB'", "'BASE'", 
			"'EQU'", "'ORG'", "'USE'", "'RSUB'", null, null, null, null, null, "'X'", 
			null, null, null, null, null, null, "'+'", "'-'", "'*'", "'/'", "'#'", 
			"'@'", "','", "'('", "')'", null, null, null, "'\\n'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "KSTART", "KEND", "KBYTE", "KWORD", "KRESW", "KRESB", "KBASE", 
			"KEQU", "KORG", "KUSE", "KRSUB", "OPCODE_F1", "OPCODE_F2_RR", "OPCODE_F2_RN", 
			"OPCODE_F2_R", "OPCODE", "XREG", "REG", "LITC", "LITX", "HEXNUM", "ID", 
			"NUMBER", "MAS", "MENOS", "MUL", "DIV", "HASH", "ARROBA", "COMA", "PAREN_IZQ", 
			"PAREN_DER", "COM_PUNTO", "COM_PCOMA", "ESPACIOS", "NEWLINE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "java-escape"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SICXEParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramaContext extends ParserRuleContext {
		public EncabezadoContext encabezado() {
			return getRuleContext(EncabezadoContext.class,0);
		}
		public CuerpoContext cuerpo() {
			return getRuleContext(CuerpoContext.class,0);
		}
		public CierreContext cierre() {
			return getRuleContext(CierreContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SICXEParser.EOF, 0); }
		public ProgramaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_programa; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterPrograma(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitPrograma(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitPrograma(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramaContext programa() throws RecognitionException {
		ProgramaContext _localctx = new ProgramaContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_programa);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(42);
			encabezado();
			setState(43);
			cuerpo();
			setState(44);
			cierre();
			setState(45);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EncabezadoContext extends ParserRuleContext {
		public TerminalNode KSTART() { return getToken(SICXEParser.KSTART, 0); }
		public NumeralContext numeral() {
			return getRuleContext(NumeralContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(SICXEParser.NEWLINE, 0); }
		public EtiquetaContext etiqueta() {
			return getRuleContext(EtiquetaContext.class,0);
		}
		public EncabezadoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_encabezado; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterEncabezado(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitEncabezado(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitEncabezado(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EncabezadoContext encabezado() throws RecognitionException {
		EncabezadoContext _localctx = new EncabezadoContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_encabezado);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(47);
				etiqueta();
				}
			}

			setState(50);
			match(KSTART);
			setState(51);
			numeral();
			setState(52);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CuerpoContext extends ParserRuleContext {
		public List<LineaOkContext> lineaOk() {
			return getRuleContexts(LineaOkContext.class);
		}
		public LineaOkContext lineaOk(int i) {
			return getRuleContext(LineaOkContext.class,i);
		}
		public CuerpoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cuerpo; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterCuerpo(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitCuerpo(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitCuerpo(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CuerpoContext cuerpo() throws RecognitionException {
		CuerpoContext _localctx = new CuerpoContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_cuerpo);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(54);
					lineaOk();
					}
					} 
				}
				setState(59);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LineaOkContext extends ParserRuleContext {
		public LineaContext linea() {
			return getRuleContext(LineaContext.class,0);
		}
		public LineaErrorContext lineaError() {
			return getRuleContext(LineaErrorContext.class,0);
		}
		public LineaOkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lineaOk; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterLineaOk(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitLineaOk(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitLineaOk(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LineaOkContext lineaOk() throws RecognitionException {
		LineaOkContext _localctx = new LineaOkContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_lineaOk);
		try {
			setState(62);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(60);
				linea();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(61);
				lineaError();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LineaErrorContext extends ParserRuleContext {
		public TerminalNode NEWLINE() { return getToken(SICXEParser.NEWLINE, 0); }
		public List<TokensErrorContext> tokensError() {
			return getRuleContexts(TokensErrorContext.class);
		}
		public TokensErrorContext tokensError(int i) {
			return getRuleContext(TokensErrorContext.class,i);
		}
		public LineaErrorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lineaError; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterLineaError(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitLineaError(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitLineaError(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LineaErrorContext lineaError() throws RecognitionException {
		LineaErrorContext _localctx = new LineaErrorContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_lineaError);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(64);
				tokensError();
				}
				}
				setState(67); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((_la) & ~0x3f) == 0 && ((1L << _la) & 8589934584L) != 0 );
			setState(69);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TokensErrorContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SICXEParser.ID, 0); }
		public TerminalNode NUMBER() { return getToken(SICXEParser.NUMBER, 0); }
		public TerminalNode HEXNUM() { return getToken(SICXEParser.HEXNUM, 0); }
		public TerminalNode LITC() { return getToken(SICXEParser.LITC, 0); }
		public TerminalNode LITX() { return getToken(SICXEParser.LITX, 0); }
		public TerminalNode MAS() { return getToken(SICXEParser.MAS, 0); }
		public TerminalNode MENOS() { return getToken(SICXEParser.MENOS, 0); }
		public TerminalNode MUL() { return getToken(SICXEParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(SICXEParser.DIV, 0); }
		public TerminalNode HASH() { return getToken(SICXEParser.HASH, 0); }
		public TerminalNode ARROBA() { return getToken(SICXEParser.ARROBA, 0); }
		public TerminalNode COMA() { return getToken(SICXEParser.COMA, 0); }
		public TerminalNode PAREN_IZQ() { return getToken(SICXEParser.PAREN_IZQ, 0); }
		public TerminalNode PAREN_DER() { return getToken(SICXEParser.PAREN_DER, 0); }
		public TerminalNode OPCODE() { return getToken(SICXEParser.OPCODE, 0); }
		public TerminalNode OPCODE_F1() { return getToken(SICXEParser.OPCODE_F1, 0); }
		public TerminalNode OPCODE_F2_RR() { return getToken(SICXEParser.OPCODE_F2_RR, 0); }
		public TerminalNode OPCODE_F2_RN() { return getToken(SICXEParser.OPCODE_F2_RN, 0); }
		public TerminalNode OPCODE_F2_R() { return getToken(SICXEParser.OPCODE_F2_R, 0); }
		public TerminalNode KRSUB() { return getToken(SICXEParser.KRSUB, 0); }
		public TerminalNode KBYTE() { return getToken(SICXEParser.KBYTE, 0); }
		public TerminalNode KWORD() { return getToken(SICXEParser.KWORD, 0); }
		public TerminalNode KRESB() { return getToken(SICXEParser.KRESB, 0); }
		public TerminalNode KRESW() { return getToken(SICXEParser.KRESW, 0); }
		public TerminalNode KBASE() { return getToken(SICXEParser.KBASE, 0); }
		public TerminalNode KEQU() { return getToken(SICXEParser.KEQU, 0); }
		public TerminalNode KORG() { return getToken(SICXEParser.KORG, 0); }
		public TerminalNode KUSE() { return getToken(SICXEParser.KUSE, 0); }
		public TerminalNode REG() { return getToken(SICXEParser.REG, 0); }
		public TerminalNode XREG() { return getToken(SICXEParser.XREG, 0); }
		public TokensErrorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokensError; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterTokensError(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitTokensError(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitTokensError(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TokensErrorContext tokensError() throws RecognitionException {
		TokensErrorContext _localctx = new TokensErrorContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_tokensError);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 8589934584L) != 0) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CierreContext extends ParserRuleContext {
		public TerminalNode KEND() { return getToken(SICXEParser.KEND, 0); }
		public EtiquetaContext etiqueta() {
			return getRuleContext(EtiquetaContext.class,0);
		}
		public DestinoContext destino() {
			return getRuleContext(DestinoContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(SICXEParser.NEWLINE, 0); }
		public CierreContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cierre; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterCierre(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitCierre(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitCierre(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CierreContext cierre() throws RecognitionException {
		CierreContext _localctx = new CierreContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_cierre);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(73);
				etiqueta();
				}
			}

			setState(76);
			match(KEND);
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 2195718144L) != 0) {
				{
				setState(77);
				destino();
				}
			}

			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NEWLINE) {
				{
				setState(80);
				match(NEWLINE);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DestinoContext extends ParserRuleContext {
		public ExpresionContext expresion() {
			return getRuleContext(ExpresionContext.class,0);
		}
		public DestinoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_destino; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterDestino(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitDestino(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitDestino(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DestinoContext destino() throws RecognitionException {
		DestinoContext _localctx = new DestinoContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_destino);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			expresion();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LineaContext extends ParserRuleContext {
		public EnunciadoContext enunciado() {
			return getRuleContext(EnunciadoContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(SICXEParser.NEWLINE, 0); }
		public EtiquetaContext etiqueta() {
			return getRuleContext(EtiquetaContext.class,0);
		}
		public LineaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_linea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterLinea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitLinea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitLinea(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LineaContext linea() throws RecognitionException {
		LineaContext _localctx = new LineaContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_linea);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(85);
				etiqueta();
				}
			}

			setState(88);
			enunciado();
			setState(89);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnunciadoContext extends ParserRuleContext {
		public InstruccionContext instruccion() {
			return getRuleContext(InstruccionContext.class,0);
		}
		public DirectivaContext directiva() {
			return getRuleContext(DirectivaContext.class,0);
		}
		public EnunciadoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enunciado; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterEnunciado(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitEnunciado(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitEnunciado(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnunciadoContext enunciado() throws RecognitionException {
		EnunciadoContext _localctx = new EnunciadoContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_enunciado);
		try {
			setState(93);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KRSUB:
			case OPCODE_F1:
			case OPCODE_F2_RR:
			case OPCODE_F2_RN:
			case OPCODE_F2_R:
			case OPCODE:
			case MAS:
				enterOuterAlt(_localctx, 1);
				{
				setState(91);
				instruccion();
				}
				break;
			case KBYTE:
			case KWORD:
			case KRESW:
			case KRESB:
			case KBASE:
			case KEQU:
			case KORG:
			case KUSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				directiva();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EtiquetaContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SICXEParser.ID, 0); }
		public EtiquetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_etiqueta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterEtiqueta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitEtiqueta(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitEtiqueta(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EtiquetaContext etiqueta() throws RecognitionException {
		EtiquetaContext _localctx = new EtiquetaContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_etiqueta);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InstruccionContext extends ParserRuleContext {
		public TerminalNode MAS() { return getToken(SICXEParser.MAS, 0); }
		public TerminalNode OPCODE() { return getToken(SICXEParser.OPCODE, 0); }
		public ModoAccesoContext modoAcceso() {
			return getRuleContext(ModoAccesoContext.class,0);
		}
		public TerminalNode OPCODE_F2_RR() { return getToken(SICXEParser.OPCODE_F2_RR, 0); }
		public List<RegContext> reg() {
			return getRuleContexts(RegContext.class);
		}
		public RegContext reg(int i) {
			return getRuleContext(RegContext.class,i);
		}
		public TerminalNode COMA() { return getToken(SICXEParser.COMA, 0); }
		public TerminalNode OPCODE_F2_RN() { return getToken(SICXEParser.OPCODE_F2_RN, 0); }
		public NumeralContext numeral() {
			return getRuleContext(NumeralContext.class,0);
		}
		public TerminalNode OPCODE_F2_R() { return getToken(SICXEParser.OPCODE_F2_R, 0); }
		public TerminalNode KRSUB() { return getToken(SICXEParser.KRSUB, 0); }
		public TerminalNode OPCODE_F1() { return getToken(SICXEParser.OPCODE_F1, 0); }
		public InstruccionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instruccion; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterInstruccion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitInstruccion(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitInstruccion(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InstruccionContext instruccion() throws RecognitionException {
		InstruccionContext _localctx = new InstruccionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_instruccion);
		int _la;
		try {
			setState(118);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAS:
				enterOuterAlt(_localctx, 1);
				{
				setState(97);
				match(MAS);
				setState(98);
				match(OPCODE);
				setState(100);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 3001024512L) != 0) {
					{
					setState(99);
					modoAcceso();
					}
				}

				}
				break;
			case OPCODE_F2_RR:
				enterOuterAlt(_localctx, 2);
				{
				setState(102);
				match(OPCODE_F2_RR);
				setState(103);
				reg();
				setState(104);
				match(COMA);
				setState(105);
				reg();
				}
				break;
			case OPCODE_F2_RN:
				enterOuterAlt(_localctx, 3);
				{
				setState(107);
				match(OPCODE_F2_RN);
				setState(108);
				reg();
				setState(109);
				match(COMA);
				setState(110);
				numeral();
				}
				break;
			case OPCODE_F2_R:
				enterOuterAlt(_localctx, 4);
				{
				setState(112);
				match(OPCODE_F2_R);
				setState(113);
				reg();
				}
				break;
			case OPCODE:
				enterOuterAlt(_localctx, 5);
				{
				setState(114);
				match(OPCODE);
				setState(115);
				modoAcceso();
				}
				break;
			case KRSUB:
				enterOuterAlt(_localctx, 6);
				{
				setState(116);
				match(KRSUB);
				}
				break;
			case OPCODE_F1:
				enterOuterAlt(_localctx, 7);
				{
				setState(117);
				match(OPCODE_F1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModoAccesoContext extends ParserRuleContext {
		public TerminalNode HASH() { return getToken(SICXEParser.HASH, 0); }
		public ExpresionContext expresion() {
			return getRuleContext(ExpresionContext.class,0);
		}
		public TerminalNode ARROBA() { return getToken(SICXEParser.ARROBA, 0); }
		public TerminalNode COMA() { return getToken(SICXEParser.COMA, 0); }
		public TerminalNode XREG() { return getToken(SICXEParser.XREG, 0); }
		public ModoAccesoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modoAcceso; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterModoAcceso(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitModoAcceso(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitModoAcceso(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModoAccesoContext modoAcceso() throws RecognitionException {
		ModoAccesoContext _localctx = new ModoAccesoContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_modoAcceso);
		try {
			setState(129);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(120);
				match(HASH);
				setState(121);
				expresion();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(122);
				match(ARROBA);
				setState(123);
				expresion();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(124);
				expresion();
				setState(125);
				match(COMA);
				setState(126);
				match(XREG);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(128);
				expresion();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReferenciaContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SICXEParser.ID, 0); }
		public NumeralContext numeral() {
			return getRuleContext(NumeralContext.class,0);
		}
		public ReferenciaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referencia; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterReferencia(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitReferencia(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitReferencia(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenciaContext referencia() throws RecognitionException {
		ReferenciaContext _localctx = new ReferenciaContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_referencia);
		try {
			setState(133);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(131);
				match(ID);
				}
				break;
			case HEXNUM:
			case NUMBER:
				enterOuterAlt(_localctx, 2);
				{
				setState(132);
				numeral();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RegContext extends ParserRuleContext {
		public TerminalNode REG() { return getToken(SICXEParser.REG, 0); }
		public TerminalNode XREG() { return getToken(SICXEParser.XREG, 0); }
		public RegContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterReg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitReg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitReg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegContext reg() throws RecognitionException {
		RegContext _localctx = new RegContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_reg);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			_la = _input.LA(1);
			if ( !(_la==XREG || _la==REG) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DirectivaContext extends ParserRuleContext {
		public TerminalNode KBYTE() { return getToken(SICXEParser.KBYTE, 0); }
		public ContenidoByteContext contenidoByte() {
			return getRuleContext(ContenidoByteContext.class,0);
		}
		public TerminalNode KWORD() { return getToken(SICXEParser.KWORD, 0); }
		public ExpresionContext expresion() {
			return getRuleContext(ExpresionContext.class,0);
		}
		public TerminalNode KRESB() { return getToken(SICXEParser.KRESB, 0); }
		public TerminalNode KRESW() { return getToken(SICXEParser.KRESW, 0); }
		public TerminalNode KBASE() { return getToken(SICXEParser.KBASE, 0); }
		public TerminalNode ID() { return getToken(SICXEParser.ID, 0); }
		public NumeralContext numeral() {
			return getRuleContext(NumeralContext.class,0);
		}
		public TerminalNode KEQU() { return getToken(SICXEParser.KEQU, 0); }
		public TerminalNode MUL() { return getToken(SICXEParser.MUL, 0); }
		public TerminalNode KORG() { return getToken(SICXEParser.KORG, 0); }
		public TerminalNode KUSE() { return getToken(SICXEParser.KUSE, 0); }
		public DirectivaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directiva; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterDirectiva(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitDirectiva(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitDirectiva(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectivaContext directiva() throws RecognitionException {
		DirectivaContext _localctx = new DirectivaContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_directiva);
		int _la;
		try {
			setState(160);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(137);
				match(KBYTE);
				setState(138);
				contenidoByte();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(139);
				match(KWORD);
				setState(140);
				expresion();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(141);
				match(KRESB);
				setState(142);
				expresion();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(143);
				match(KRESW);
				setState(144);
				expresion();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(145);
				match(KBASE);
				setState(148);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ID:
					{
					setState(146);
					match(ID);
					}
					break;
				case HEXNUM:
				case NUMBER:
					{
					setState(147);
					numeral();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(150);
				match(KEQU);
				setState(151);
				match(MUL);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(152);
				match(KEQU);
				setState(153);
				expresion();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(154);
				match(KORG);
				setState(155);
				expresion();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(156);
				match(KUSE);
				setState(158);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(157);
					match(ID);
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ContenidoByteContext extends ParserRuleContext {
		public TerminalNode LITC() { return getToken(SICXEParser.LITC, 0); }
		public TerminalNode LITX() { return getToken(SICXEParser.LITX, 0); }
		public ContenidoByteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_contenidoByte; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterContenidoByte(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitContenidoByte(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitContenidoByte(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ContenidoByteContext contenidoByte() throws RecognitionException {
		ContenidoByteContext _localctx = new ContenidoByteContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_contenidoByte);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			_la = _input.LA(1);
			if ( !(_la==LITC || _la==LITX) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpresionContext extends ParserRuleContext {
		public List<TerminoContext> termino() {
			return getRuleContexts(TerminoContext.class);
		}
		public TerminoContext termino(int i) {
			return getRuleContext(TerminoContext.class,i);
		}
		public List<TerminalNode> MAS() { return getTokens(SICXEParser.MAS); }
		public TerminalNode MAS(int i) {
			return getToken(SICXEParser.MAS, i);
		}
		public List<TerminalNode> MENOS() { return getTokens(SICXEParser.MENOS); }
		public TerminalNode MENOS(int i) {
			return getToken(SICXEParser.MENOS, i);
		}
		public ExpresionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expresion; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterExpresion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitExpresion(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitExpresion(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpresionContext expresion() throws RecognitionException {
		ExpresionContext _localctx = new ExpresionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_expresion);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
			termino();
			setState(169);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MAS || _la==MENOS) {
				{
				{
				setState(165);
				_la = _input.LA(1);
				if ( !(_la==MAS || _la==MENOS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(166);
				termino();
				}
				}
				setState(171);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TerminoContext extends ParserRuleContext {
		public List<FactorContext> factor() {
			return getRuleContexts(FactorContext.class);
		}
		public FactorContext factor(int i) {
			return getRuleContext(FactorContext.class,i);
		}
		public List<TerminalNode> MUL() { return getTokens(SICXEParser.MUL); }
		public TerminalNode MUL(int i) {
			return getToken(SICXEParser.MUL, i);
		}
		public List<TerminalNode> DIV() { return getTokens(SICXEParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(SICXEParser.DIV, i);
		}
		public TerminoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termino; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterTermino(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitTermino(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitTermino(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TerminoContext termino() throws RecognitionException {
		TerminoContext _localctx = new TerminoContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_termino);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(172);
			factor();
			setState(177);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MUL || _la==DIV) {
				{
				{
				setState(173);
				_la = _input.LA(1);
				if ( !(_la==MUL || _la==DIV) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(174);
				factor();
				}
				}
				setState(179);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FactorContext extends ParserRuleContext {
		public NumeralContext numeral() {
			return getRuleContext(NumeralContext.class,0);
		}
		public TerminalNode ID() { return getToken(SICXEParser.ID, 0); }
		public TerminalNode PAREN_IZQ() { return getToken(SICXEParser.PAREN_IZQ, 0); }
		public ExpresionContext expresion() {
			return getRuleContext(ExpresionContext.class,0);
		}
		public TerminalNode PAREN_DER() { return getToken(SICXEParser.PAREN_DER, 0); }
		public TerminalNode MENOS() { return getToken(SICXEParser.MENOS, 0); }
		public FactorContext factor() {
			return getRuleContext(FactorContext.class,0);
		}
		public FactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_factor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitFactor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitFactor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FactorContext factor() throws RecognitionException {
		FactorContext _localctx = new FactorContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_factor);
		try {
			setState(188);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case HEXNUM:
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(180);
				numeral();
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(181);
				match(ID);
				}
				break;
			case PAREN_IZQ:
				enterOuterAlt(_localctx, 3);
				{
				setState(182);
				match(PAREN_IZQ);
				setState(183);
				expresion();
				setState(184);
				match(PAREN_DER);
				}
				break;
			case MENOS:
				enterOuterAlt(_localctx, 4);
				{
				setState(186);
				match(MENOS);
				setState(187);
				factor();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumeralContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(SICXEParser.NUMBER, 0); }
		public TerminalNode HEXNUM() { return getToken(SICXEParser.HEXNUM, 0); }
		public NumeralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numeral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).enterNumeral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SICXEListener ) ((SICXEListener)listener).exitNumeral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SICXEVisitor ) return ((SICXEVisitor<? extends T>)visitor).visitNumeral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumeralContext numeral() throws RecognitionException {
		NumeralContext _localctx = new NumeralContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_numeral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			_la = _input.LA(1);
			if ( !(_la==HEXNUM || _la==NUMBER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001$\u00c1\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0003\u00011\b\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0005\u0002"+
		"8\b\u0002\n\u0002\f\u0002;\t\u0002\u0001\u0003\u0001\u0003\u0003\u0003"+
		"?\b\u0003\u0001\u0004\u0004\u0004B\b\u0004\u000b\u0004\f\u0004C\u0001"+
		"\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0003\u0006K\b"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006O\b\u0006\u0001\u0006\u0003"+
		"\u0006R\b\u0006\u0001\u0007\u0001\u0007\u0001\b\u0003\bW\b\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0003\t^\b\t\u0001\n\u0001\n\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0003\u000be\b\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0003\u000bw\b\u000b\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0082\b\f\u0001\r\u0001"+
		"\r\u0003\r\u0086\b\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0095\b\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0003\u000f\u009f\b\u000f\u0003\u000f\u00a1\b\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u00a8\b\u0011"+
		"\n\u0011\f\u0011\u00ab\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0005"+
		"\u0012\u00b0\b\u0012\n\u0012\f\u0012\u00b3\t\u0012\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0003\u0013\u00bd\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0000\u0000"+
		"\u0015\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"+
		"\u001a\u001c\u001e \"$&(\u0000\u0006\u0001\u0000\u0003 \u0001\u0000\u0011"+
		"\u0012\u0001\u0000\u0013\u0014\u0001\u0000\u0018\u0019\u0001\u0000\u001a"+
		"\u001b\u0002\u0000\u0015\u0015\u0017\u0017\u00ce\u0000*\u0001\u0000\u0000"+
		"\u0000\u00020\u0001\u0000\u0000\u0000\u00049\u0001\u0000\u0000\u0000\u0006"+
		">\u0001\u0000\u0000\u0000\bA\u0001\u0000\u0000\u0000\nG\u0001\u0000\u0000"+
		"\u0000\fJ\u0001\u0000\u0000\u0000\u000eS\u0001\u0000\u0000\u0000\u0010"+
		"V\u0001\u0000\u0000\u0000\u0012]\u0001\u0000\u0000\u0000\u0014_\u0001"+
		"\u0000\u0000\u0000\u0016v\u0001\u0000\u0000\u0000\u0018\u0081\u0001\u0000"+
		"\u0000\u0000\u001a\u0085\u0001\u0000\u0000\u0000\u001c\u0087\u0001\u0000"+
		"\u0000\u0000\u001e\u00a0\u0001\u0000\u0000\u0000 \u00a2\u0001\u0000\u0000"+
		"\u0000\"\u00a4\u0001\u0000\u0000\u0000$\u00ac\u0001\u0000\u0000\u0000"+
		"&\u00bc\u0001\u0000\u0000\u0000(\u00be\u0001\u0000\u0000\u0000*+\u0003"+
		"\u0002\u0001\u0000+,\u0003\u0004\u0002\u0000,-\u0003\f\u0006\u0000-.\u0005"+
		"\u0000\u0000\u0001.\u0001\u0001\u0000\u0000\u0000/1\u0003\u0014\n\u0000"+
		"0/\u0001\u0000\u0000\u000001\u0001\u0000\u0000\u000012\u0001\u0000\u0000"+
		"\u000023\u0005\u0001\u0000\u000034\u0003(\u0014\u000045\u0005$\u0000\u0000"+
		"5\u0003\u0001\u0000\u0000\u000068\u0003\u0006\u0003\u000076\u0001\u0000"+
		"\u0000\u00008;\u0001\u0000\u0000\u000097\u0001\u0000\u0000\u00009:\u0001"+
		"\u0000\u0000\u0000:\u0005\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000"+
		"\u0000<?\u0003\u0010\b\u0000=?\u0003\b\u0004\u0000><\u0001\u0000\u0000"+
		"\u0000>=\u0001\u0000\u0000\u0000?\u0007\u0001\u0000\u0000\u0000@B\u0003"+
		"\n\u0005\u0000A@\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000CA\u0001"+
		"\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DE\u0001\u0000\u0000\u0000"+
		"EF\u0005$\u0000\u0000F\t\u0001\u0000\u0000\u0000GH\u0007\u0000\u0000\u0000"+
		"H\u000b\u0001\u0000\u0000\u0000IK\u0003\u0014\n\u0000JI\u0001\u0000\u0000"+
		"\u0000JK\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000LN\u0005\u0002"+
		"\u0000\u0000MO\u0003\u000e\u0007\u0000NM\u0001\u0000\u0000\u0000NO\u0001"+
		"\u0000\u0000\u0000OQ\u0001\u0000\u0000\u0000PR\u0005$\u0000\u0000QP\u0001"+
		"\u0000\u0000\u0000QR\u0001\u0000\u0000\u0000R\r\u0001\u0000\u0000\u0000"+
		"ST\u0003\"\u0011\u0000T\u000f\u0001\u0000\u0000\u0000UW\u0003\u0014\n"+
		"\u0000VU\u0001\u0000\u0000\u0000VW\u0001\u0000\u0000\u0000WX\u0001\u0000"+
		"\u0000\u0000XY\u0003\u0012\t\u0000YZ\u0005$\u0000\u0000Z\u0011\u0001\u0000"+
		"\u0000\u0000[^\u0003\u0016\u000b\u0000\\^\u0003\u001e\u000f\u0000][\u0001"+
		"\u0000\u0000\u0000]\\\u0001\u0000\u0000\u0000^\u0013\u0001\u0000\u0000"+
		"\u0000_`\u0005\u0016\u0000\u0000`\u0015\u0001\u0000\u0000\u0000ab\u0005"+
		"\u0018\u0000\u0000bd\u0005\u0010\u0000\u0000ce\u0003\u0018\f\u0000dc\u0001"+
		"\u0000\u0000\u0000de\u0001\u0000\u0000\u0000ew\u0001\u0000\u0000\u0000"+
		"fg\u0005\r\u0000\u0000gh\u0003\u001c\u000e\u0000hi\u0005\u001e\u0000\u0000"+
		"ij\u0003\u001c\u000e\u0000jw\u0001\u0000\u0000\u0000kl\u0005\u000e\u0000"+
		"\u0000lm\u0003\u001c\u000e\u0000mn\u0005\u001e\u0000\u0000no\u0003(\u0014"+
		"\u0000ow\u0001\u0000\u0000\u0000pq\u0005\u000f\u0000\u0000qw\u0003\u001c"+
		"\u000e\u0000rs\u0005\u0010\u0000\u0000sw\u0003\u0018\f\u0000tw\u0005\u000b"+
		"\u0000\u0000uw\u0005\f\u0000\u0000va\u0001\u0000\u0000\u0000vf\u0001\u0000"+
		"\u0000\u0000vk\u0001\u0000\u0000\u0000vp\u0001\u0000\u0000\u0000vr\u0001"+
		"\u0000\u0000\u0000vt\u0001\u0000\u0000\u0000vu\u0001\u0000\u0000\u0000"+
		"w\u0017\u0001\u0000\u0000\u0000xy\u0005\u001c\u0000\u0000y\u0082\u0003"+
		"\"\u0011\u0000z{\u0005\u001d\u0000\u0000{\u0082\u0003\"\u0011\u0000|}"+
		"\u0003\"\u0011\u0000}~\u0005\u001e\u0000\u0000~\u007f\u0005\u0011\u0000"+
		"\u0000\u007f\u0082\u0001\u0000\u0000\u0000\u0080\u0082\u0003\"\u0011\u0000"+
		"\u0081x\u0001\u0000\u0000\u0000\u0081z\u0001\u0000\u0000\u0000\u0081|"+
		"\u0001\u0000\u0000\u0000\u0081\u0080\u0001\u0000\u0000\u0000\u0082\u0019"+
		"\u0001\u0000\u0000\u0000\u0083\u0086\u0005\u0016\u0000\u0000\u0084\u0086"+
		"\u0003(\u0014\u0000\u0085\u0083\u0001\u0000\u0000\u0000\u0085\u0084\u0001"+
		"\u0000\u0000\u0000\u0086\u001b\u0001\u0000\u0000\u0000\u0087\u0088\u0007"+
		"\u0001\u0000\u0000\u0088\u001d\u0001\u0000\u0000\u0000\u0089\u008a\u0005"+
		"\u0003\u0000\u0000\u008a\u00a1\u0003 \u0010\u0000\u008b\u008c\u0005\u0004"+
		"\u0000\u0000\u008c\u00a1\u0003\"\u0011\u0000\u008d\u008e\u0005\u0006\u0000"+
		"\u0000\u008e\u00a1\u0003\"\u0011\u0000\u008f\u0090\u0005\u0005\u0000\u0000"+
		"\u0090\u00a1\u0003\"\u0011\u0000\u0091\u0094\u0005\u0007\u0000\u0000\u0092"+
		"\u0095\u0005\u0016\u0000\u0000\u0093\u0095\u0003(\u0014\u0000\u0094\u0092"+
		"\u0001\u0000\u0000\u0000\u0094\u0093\u0001\u0000\u0000\u0000\u0095\u00a1"+
		"\u0001\u0000\u0000\u0000\u0096\u0097\u0005\b\u0000\u0000\u0097\u00a1\u0005"+
		"\u001a\u0000\u0000\u0098\u0099\u0005\b\u0000\u0000\u0099\u00a1\u0003\""+
		"\u0011\u0000\u009a\u009b\u0005\t\u0000\u0000\u009b\u00a1\u0003\"\u0011"+
		"\u0000\u009c\u009e\u0005\n\u0000\u0000\u009d\u009f\u0005\u0016\u0000\u0000"+
		"\u009e\u009d\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000"+
		"\u009f\u00a1\u0001\u0000\u0000\u0000\u00a0\u0089\u0001\u0000\u0000\u0000"+
		"\u00a0\u008b\u0001\u0000\u0000\u0000\u00a0\u008d\u0001\u0000\u0000\u0000"+
		"\u00a0\u008f\u0001\u0000\u0000\u0000\u00a0\u0091\u0001\u0000\u0000\u0000"+
		"\u00a0\u0096\u0001\u0000\u0000\u0000\u00a0\u0098\u0001\u0000\u0000\u0000"+
		"\u00a0\u009a\u0001\u0000\u0000\u0000\u00a0\u009c\u0001\u0000\u0000\u0000"+
		"\u00a1\u001f\u0001\u0000\u0000\u0000\u00a2\u00a3\u0007\u0002\u0000\u0000"+
		"\u00a3!\u0001\u0000\u0000\u0000\u00a4\u00a9\u0003$\u0012\u0000\u00a5\u00a6"+
		"\u0007\u0003\u0000\u0000\u00a6\u00a8\u0003$\u0012\u0000\u00a7\u00a5\u0001"+
		"\u0000\u0000\u0000\u00a8\u00ab\u0001\u0000\u0000\u0000\u00a9\u00a7\u0001"+
		"\u0000\u0000\u0000\u00a9\u00aa\u0001\u0000\u0000\u0000\u00aa#\u0001\u0000"+
		"\u0000\u0000\u00ab\u00a9\u0001\u0000\u0000\u0000\u00ac\u00b1\u0003&\u0013"+
		"\u0000\u00ad\u00ae\u0007\u0004\u0000\u0000\u00ae\u00b0\u0003&\u0013\u0000"+
		"\u00af\u00ad\u0001\u0000\u0000\u0000\u00b0\u00b3\u0001\u0000\u0000\u0000"+
		"\u00b1\u00af\u0001\u0000\u0000\u0000\u00b1\u00b2\u0001\u0000\u0000\u0000"+
		"\u00b2%\u0001\u0000\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b4"+
		"\u00bd\u0003(\u0014\u0000\u00b5\u00bd\u0005\u0016\u0000\u0000\u00b6\u00b7"+
		"\u0005\u001f\u0000\u0000\u00b7\u00b8\u0003\"\u0011\u0000\u00b8\u00b9\u0005"+
		" \u0000\u0000\u00b9\u00bd\u0001\u0000\u0000\u0000\u00ba\u00bb\u0005\u0019"+
		"\u0000\u0000\u00bb\u00bd\u0003&\u0013\u0000\u00bc\u00b4\u0001\u0000\u0000"+
		"\u0000\u00bc\u00b5\u0001\u0000\u0000\u0000\u00bc\u00b6\u0001\u0000\u0000"+
		"\u0000\u00bc\u00ba\u0001\u0000\u0000\u0000\u00bd\'\u0001\u0000\u0000\u0000"+
		"\u00be\u00bf\u0007\u0005\u0000\u0000\u00bf)\u0001\u0000\u0000\u0000\u0013"+
		"09>CJNQV]dv\u0081\u0085\u0094\u009e\u00a0\u00a9\u00b1\u00bc";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}