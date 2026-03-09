package com.github.themirachathumina.golangintellijcommunity;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.github.themirachathumina.golangintellijcommunity.psi.GoTypes.*;

%%

%{
  public _GoLexer() {
    this((java.io.Reader)null);
  }

  // --- AUTOMATIC SEMICOLON INSERTION (ASI) STATE ---
  private IElementType previousToken = null;

  /**
   * Checks if the last emitted token requires an automatic semicolon
   * when followed by a newline or EOF.
   */
  private boolean shouldInsertSemicolon() {
    if (previousToken == null) return false;
    return previousToken == IDENTIFIER ||
           previousToken == INT_LIT ||
           previousToken == FLOAT_LIT ||
           previousToken == IMAGINARY_LIT ||
           previousToken == RUNE_LIT ||
           previousToken == STRING_LIT ||
           previousToken == BREAK_KW ||
           previousToken == CONTINUE_KW ||
           previousToken == FALLTHROUGH_KW ||
           previousToken == RETURN_KW ||
           previousToken == PLUS_PLUS ||
           previousToken == MINUS_MINUS ||
           previousToken == RPAREN ||
           previousToken == RBRACK ||
           previousToken == RBRACE;
  }

  /**
   * Pushes the matched newline/comment back into the stream,
   * emits a SEMICOLON, and updates the state.
   */
  private IElementType insertSemicolon() {
    yypushback(yylength());
    previousToken = SEMICOLON;
    return SEMICOLON;
  }

  /**
   * Wrapper to emit standard tokens and record them for ASI logic.
   */
  private IElementType processToken(IElementType type) {
    previousToken = type;
    return type;
  }
%}

%public
%class _GoLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

NL=\r\n|\n|\r
WS=[ \t\f]+

// Comments
LINE_COMMENT="//"[^\r\n]*
BLOCK_COMMENT="/*" !([^]* "*/" [^]*) ("*/")?

// Identifiers
IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*

// Strings and Runes
STRING_LIT=\"([^\"\\]|\\.)*(\"|\\)?
RAW_STRING_LIT=`[^`]*`?
RUNE_LIT='([^'\\]|\\.)*'?

// Numbers (Go 1.13+ Specification)
DECIMAL_LIT=[1-9][0-9_]* | 0
OCTAL_LIT=0[oO]?[0-7_]+
HEX_LIT=0[xX][0-9a-fA-F_]+
BINARY_LIT=0[bB][01_]+
INT_LIT={DECIMAL_LIT} | {OCTAL_LIT} | {HEX_LIT} | {BINARY_LIT}

DECIMAL_FLOAT_LIT=[0-9_]+"."[0-9_]*([eE][+-]?[0-9_]+)? | "."[0-9_]+([eE][+-]?[0-9_]+)? | [0-9_]+[eE][+-]?[0-9_]+
HEX_FLOAT_LIT=0[xX]([0-9a-fA-F_]+"."[0-9a-fA-F_]* | "."[0-9a-fA-F_]+ | [0-9a-fA-F_]+)[pP][+-]?[0-9_]+
FLOAT_LIT={DECIMAL_FLOAT_LIT} | {HEX_FLOAT_LIT}

IMAGINARY_LIT=({DECIMAL_LIT} | {FLOAT_LIT})[iI]

%%
<YYINITIAL> {

  // Horizontal whitespace doesn't affect ASI
  {WS} { return WHITE_SPACE; }

  // Newlines trigger ASI
  {NL} {
    if (shouldInsertSemicolon()) {
      return insertSemicolon();
    }
    previousToken = null; // ASI chain broken
    return WHITE_SPACE;
  }

  // Line comments act like Newlines in Go
  {LINE_COMMENT} {
    if (shouldInsertSemicolon()) {
      return insertSemicolon();
    }
    previousToken = null;
    return LINE_COMMENT;
  }

  // Block comments act like Newlines ONLY if they contain a newline character
  {BLOCK_COMMENT} {
    if (yytext().toString().contains("\n") || yytext().toString().contains("\r")) {
      if (shouldInsertSemicolon()) {
        return insertSemicolon();
      }
      previousToken = null;
    }
    return BLOCK_COMMENT;
  }

  // Keywords
  "package"             { return processToken(PACKAGE_KW); }
  "import"              { return processToken(IMPORT_KW); }
  "func"                { return processToken(FUNC_KW); }
  "type"                { return processToken(TYPE_KW); }
  "var"                 { return processToken(VAR_KW); }
  "const"               { return processToken(CONST_KW); }
  "struct"              { return processToken(STRUCT_KW); }
  "interface"           { return processToken(INTERFACE_KW); }
  "map"                 { return processToken(MAP_KW); }
  "chan"                { return processToken(CHAN_KW); }
  "go"                  { return processToken(GO_KW); }
  "defer"               { return processToken(DEFER_KW); }
  "return"              { return processToken(RETURN_KW); }
  "break"               { return processToken(BREAK_KW); }
  "continue"            { return processToken(CONTINUE_KW); }
  "goto"                { return processToken(GOTO_KW); }
  "fallthrough"         { return processToken(FALLTHROUGH_KW); }
  "if"                  { return processToken(IF_KW); }
  "else"                { return processToken(ELSE_KW); }
  "switch"              { return processToken(SWITCH_KW); }
  "case"                { return processToken(CASE_KW); }
  "default"             { return processToken(DEFAULT_KW); }
  "select"              { return processToken(SELECT_KW); }
  "for"                 { return processToken(FOR_KW); }
  "range"               { return processToken(RANGE_KW); }

  // Multi-character Operators
  "..."                 { return processToken(ELLIPSIS); }
  "=="                  { return processToken(EQEQ); }
  "!="                  { return processToken(NEQ); }
  "<="                  { return processToken(LESS_EQ); }
  ">="                  { return processToken(GREATER_EQ); }
  ":="                  { return processToken(ASSIGN); }
  "||"                  { return processToken(COND_OR); }
  "&&"                  { return processToken(COND_AND); }
  "<-"                  { return processToken(SEND_CHANNEL); }
  "++"                  { return processToken(PLUS_PLUS); }
  "--"                  { return processToken(MINUS_MINUS); }
  "<<"                  { return processToken(SHIFT_LEFT); }
  ">>"                  { return processToken(SHIFT_RIGHT); }
  "&^"                  { return processToken(BIT_CLEAR); }

  // Single-character Operators
  "{"                   { return processToken(LBRACE); }
  "}"                   { return processToken(RBRACE); }
  "["                   { return processToken(LBRACK); }
  "]"                   { return processToken(RBRACK); }
  "("                   { return processToken(LPAREN); }
  ")"                   { return processToken(RPAREN); }
  ":"                   { return processToken(COLON); }
  ";"                   { return processToken(SEMICOLON); }
  ","                   { return processToken(COMMA); }
  "."                   { return processToken(DOT); }
  "="                   { return processToken(EQ); }
  "<"                   { return processToken(LESS); }
  ">"                   { return processToken(GREATER); }
  "+"                   { return processToken(PLUS); }
  "-"                   { return processToken(MINUS); }
  "|"                   { return processToken(BIT_OR); }
  "^"                   { return processToken(BIT_XOR); }
  "*"                   { return processToken(MUL); }
  "/"                   { return processToken(QUOTIENT); }
  "%"                   { return processToken(REMAINDER); }
  "&"                   { return processToken(BIT_AND); }
  "!"                   { return processToken(NOT); }
  "~"                   { return processToken(TILDE); }

  // Literals & Identifiers
  {IDENTIFIER}          { return processToken(IDENTIFIER); }
  {STRING_LIT}          { return processToken(STRING_LIT); }
  {RAW_STRING_LIT}      { return processToken(STRING_LIT); }
  {INT_LIT}             { return processToken(INT_LIT); }
  {FLOAT_LIT}           { return processToken(FLOAT_LIT); }
  {IMAGINARY_LIT}       { return processToken(IMAGINARY_LIT); }
  {RUNE_LIT}            { return processToken(RUNE_LIT); }

}

// Handle EOF (End Of File) ASI
<<EOF>> {
  if (shouldInsertSemicolon()) {
    previousToken = SEMICOLON; // Set to semicolon so we don't insert infinitely
    return SEMICOLON;
  }
  return null;
}

[^] { return BAD_CHARACTER; }