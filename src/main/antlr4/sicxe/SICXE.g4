grammar SICXE;

// ╔══════════════════════════════════════════════════════════════╗
// ║         ANALIZADOR SINTÁCTICO SIC/XE  —  Práctica 3         ║
// ║  Gramática con soporte de EQU, ORG y expresiones completas  ║
// ╚══════════════════════════════════════════════════════════════╝

programa
    : encabezado
      cuerpo
      cierre
      EOF
    ;

encabezado
    : etiqueta? KSTART numeral NEWLINE
    ;

cuerpo
    : lineaOk*
    ;

lineaOk
    : linea
    | lineaError
    ;

lineaError
    : tokensError+ NEWLINE
    ;

tokensError
    : ID | NUMBER | HEXNUM | LITC | LITX
    | MAS | MENOS | MUL | DIV | HASH | ARROBA | COMA | PAREN_IZQ | PAREN_DER
    | OPCODE | OPCODE_F1 | OPCODE_F2_RR | OPCODE_F2_RN | OPCODE_F2_R
    | KRSUB | KBYTE | KWORD | KRESB | KRESW | KBASE | KEQU | KORG | KUSE
    | REG | XREG
    ;

cierre
    : etiqueta? KEND destino? NEWLINE?
    ;

// destino puede ser un símbolo simple o una expresión
destino
    : expresion
    ;

linea
    : etiqueta? enunciado NEWLINE
    ;

enunciado
    : instruccion
    | directiva
    ;

etiqueta
    : ID
    ;

// ─────────────────────────────────────────────────────────────────
// INSTRUCCIONES
// ─────────────────────────────────────────────────────────────────

instruccion
    : MAS OPCODE modoAcceso?
    | OPCODE_F2_RR reg COMA reg
    | OPCODE_F2_RN reg COMA numeral
    | OPCODE_F2_R  reg
    | OPCODE modoAcceso
    | KRSUB
    | OPCODE_F1
    ;

// ─────────────────────────────────────────────────────────────────
// MODOS DE DIRECCIONAMIENTO — ahora aceptan expresiones completas
// ─────────────────────────────────────────────────────────────────

modoAcceso
    : HASH expresion              // Inmediato: #expr   ej: #NUEVO-4
    | ARROBA expresion            // Indirecto: @expr
    | expresion COMA XREG         // Indexado:  expr,X
    | expresion                   // Simple:    expr
    ;

referencia
    : ID
    | numeral
    ;

reg
    : REG
    | XREG
    ;

// ─────────────────────────────────────────────────────────────────
// DIRECTIVAS
// ─────────────────────────────────────────────────────────────────

directiva
    : KBYTE contenidoByte
    | KWORD expresion
    | KRESB expresion
    | KRESW expresion
    | KBASE (ID | numeral)
    | KEQU MUL                  // EQU *  → valor del CP (relativo)
    | KEQU expresion            // EQU expresión o constante
    | KORG expresion            // ORG valor  → modifica CP
    | KUSE ID?                   // USE [nombre] → cambio de bloque
    ;

contenidoByte
    : LITC
    | LITX
    ;

// ─────────────────────────────────────────────────────────────────
// EXPRESIONES — precedencia: paréntesis > mul/div > suma/resta
// factor también puede ser asterisco (*) para el CP actual
// ─────────────────────────────────────────────────────────────────

expresion
    : termino ((MAS | MENOS) termino)*
    ;

termino
    : factor ((MUL | DIV) factor)*
    ;

factor
    : numeral
    | ID
    | PAREN_IZQ expresion PAREN_DER
    | MENOS factor                    // Negación unaria: -expr
    ;

numeral
    : NUMBER
    | HEXNUM
    ;


// ╔══════════════════════════════════════════════════════════════╗
// ║                    REGLAS DEL LÉXICO                        ║
// ╚══════════════════════════════════════════════════════════════╝

KSTART : 'START' ;
KEND   : 'END'   ;
KBYTE  : 'BYTE'  ;
KWORD  : 'WORD'  ;
KRESW  : 'RESW'  ;
KRESB  : 'RESB'  ;
KBASE  : 'BASE'  ;
KEQU   : 'EQU'   ;
KORG   : 'ORG'   ;
KUSE   : 'USE'   ;
KRSUB  : 'RSUB'  ;

OPCODE_F1
    : 'FIX'
    | 'FLOAT'
    | 'HIO'
    | 'NORM'
    | 'SIO'
    | 'TIO'
    ;

OPCODE_F2_RR
    : 'ADDR'
    | 'COMPR'
    | 'DIVR'
    | 'MULR'
    | 'RMO'
    | 'SUBR'
    ;

OPCODE_F2_RN
    : 'SHIFTL'
    | 'SHIFTR'
    | 'SVC'
    ;

OPCODE_F2_R
    : 'CLEAR'
    | 'TIXR'
    ;

OPCODE
    : 'LDA'  | 'STA'   | 'LDX'   | 'ADD'   | 'COMP'  | 'JEQ'
    | 'JSUB' | 'LDB'   | 'TIX'   | 'JLT'   | 'J'     | 'JGT'
    | 'LDCH' | 'LDF'   | 'LDL'   | 'LDS'   | 'LDT'   | 'LPS'
    | 'ADDF' | 'COMPF' | 'DIVF'  | 'MULF'  | 'OR'    | 'RD'
    | 'SSK'  | 'STB'   | 'STCH'  | 'STF'   | 'STI'   | 'STL'
    | 'STS'  | 'STSW'  | 'STT'   | 'STX'   | 'SUB'   | 'SUBF'
    | 'TD'   | 'WD'    | 'MUL'   | 'AND'   | 'DIV'
    ;

XREG : 'X' ;
REG  : 'A' | 'L' | 'B' | 'S' | 'T' | 'F' ;

LITC   : 'C\'' (~['\r\n])* '\'' ;
LITX   : 'X\'' [0-9A-F]+ '\'' ;
HEXNUM : [0-9A-Fa-f]+ 'H' ;
ID     : [A-Z] [A-Z0-9]? [A-Z0-9]? [A-Z0-9]? [A-Z0-9]? [A-Z0-9]? [A-Z0-9]? [A-Z0-9]? ;
NUMBER : [0-9]+ ;

MAS       : '+' ;
MENOS     : '-' ;
MUL       : '*' ;
DIV       : '/' ;
HASH      : '#' ;
ARROBA    : '@' ;
COMA      : ',' ;
PAREN_IZQ : '(' ;
PAREN_DER : ')' ;

COM_PUNTO : '.' ~[\r\n]* -> skip ;
COM_PCOMA : ';' ~[\r\n]* -> skip ;
ESPACIOS  : [ \t\r]+ -> skip ;
NEWLINE   : '\n' ;