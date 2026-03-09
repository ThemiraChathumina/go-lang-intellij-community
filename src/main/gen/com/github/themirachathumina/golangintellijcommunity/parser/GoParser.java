// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.github.themirachathumina.golangintellijcommunity.psi.GoTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class GoParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return goFile(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // MulExpr ( add_op MulExpr )*
  public static boolean AddExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ADD_EXPR, "<add expr>");
    result_ = MulExpr(builder_, level_ + 1);
    result_ = result_ && AddExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( add_op MulExpr )*
  private static boolean AddExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!AddExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "AddExpr_1", pos_)) break;
    }
    return true;
  }

  // add_op MulExpr
  private static boolean AddExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = add_op(builder_, level_ + 1);
    result_ = result_ && MulExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // MulExprNoStruct ( add_op MulExprNoStruct )*
  public static boolean AddExprNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExprNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ADD_EXPR_NO_STRUCT, "<add expr no struct>");
    result_ = MulExprNoStruct(builder_, level_ + 1);
    result_ = result_ && AddExprNoStruct_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( add_op MulExprNoStruct )*
  private static boolean AddExprNoStruct_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExprNoStruct_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!AddExprNoStruct_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "AddExprNoStruct_1", pos_)) break;
    }
    return true;
  }

  // add_op MulExprNoStruct
  private static boolean AddExprNoStruct_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExprNoStruct_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = add_op(builder_, level_ + 1);
    result_ = result_ && MulExprNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier ( TypeParameters )? "=" Type
  public static boolean AliasDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AliasDecl")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && AliasDecl_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQ);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, ALIAS_DECL, result_);
    return result_;
  }

  // ( TypeParameters )?
  private static boolean AliasDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AliasDecl_1")) return false;
    AliasDecl_1_0(builder_, level_ + 1);
    return true;
  }

  // ( TypeParameters )
  private static boolean AliasDecl_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AliasDecl_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeParameters(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "(" ( ( ExpressionList | Type ( "," ExpressionList )? ) ( "..." )? ( "," )? )? ")"
  public static boolean Arguments(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && Arguments_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, ARGUMENTS, result_);
    return result_;
  }

  // ( ( ExpressionList | Type ( "," ExpressionList )? ) ( "..." )? ( "," )? )?
  private static boolean Arguments_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments_1")) return false;
    Arguments_1_0(builder_, level_ + 1);
    return true;
  }

  // ( ExpressionList | Type ( "," ExpressionList )? ) ( "..." )? ( "," )?
  private static boolean Arguments_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Arguments_1_0_0(builder_, level_ + 1);
    result_ = result_ && Arguments_1_0_1(builder_, level_ + 1);
    result_ = result_ && Arguments_1_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ExpressionList | Type ( "," ExpressionList )?
  private static boolean Arguments_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionList(builder_, level_ + 1);
    if (!result_) result_ = Arguments_1_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Type ( "," ExpressionList )?
  private static boolean Arguments_1_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments_1_0_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Type(builder_, level_ + 1);
    result_ = result_ && Arguments_1_0_0_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( "," ExpressionList )?
  private static boolean Arguments_1_0_0_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments_1_0_0_1_1")) return false;
    Arguments_1_0_0_1_1_0(builder_, level_ + 1);
    return true;
  }

  // "," ExpressionList
  private static boolean Arguments_1_0_0_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments_1_0_0_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( "..." )?
  private static boolean Arguments_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments_1_0_1")) return false;
    consumeToken(builder_, ELLIPSIS);
    return true;
  }

  // ( "," )?
  private static boolean Arguments_1_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Arguments_1_0_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // Expression
  public static boolean ArrayLength(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArrayLength")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARRAY_LENGTH, "<array length>");
    result_ = Expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "[" ArrayLength "]" ElementType
  public static boolean ArrayType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArrayType")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && ArrayLength(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    result_ = result_ && ElementType(builder_, level_ + 1);
    exit_section_(builder_, marker_, ARRAY_TYPE, result_);
    return result_;
  }

  /* ********************************************************** */
  // ExpressionList assign_op ExpressionList
  public static boolean Assignment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Assignment")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ASSIGNMENT, "<assignment>");
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && assign_op(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // Type
  public static boolean BaseType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BaseType")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BASE_TYPE, "<base type>");
    result_ = Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // int_lit | float_lit | imaginary_lit | rune_lit | StringLiteral
  public static boolean BasicLit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BasicLit")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BASIC_LIT, "<basic lit>");
    result_ = consumeToken(builder_, INT_LIT);
    if (!result_) result_ = consumeToken(builder_, FLOAT_LIT);
    if (!result_) result_ = consumeToken(builder_, IMAGINARY_LIT);
    if (!result_) result_ = consumeToken(builder_, RUNE_LIT);
    if (!result_) result_ = StringLiteral(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "{" StatementList "}"
  public static boolean Block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Block")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BLOCK, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, StatementList(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // "break" ( Label )?
  public static boolean BreakStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BreakStmt")) return false;
    if (!nextTokenIs(builder_, BREAK_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BREAK_KW);
    result_ = result_ && BreakStmt_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, BREAK_STMT, result_);
    return result_;
  }

  // ( Label )?
  private static boolean BreakStmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BreakStmt_1")) return false;
    BreakStmt_1_0(builder_, level_ + 1);
    return true;
  }

  // ( Label )
  private static boolean BreakStmt_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BreakStmt_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Label(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Expression
  public static boolean Channel(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Channel")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CHANNEL, "<channel>");
    result_ = Expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ReceiveChannelType | SendOrBidiChannelType
  public static boolean ChannelType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ChannelType")) return false;
    if (!nextTokenIs(builder_, "<channel type>", CHAN_KW, SEND_CHANNEL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CHANNEL_TYPE, "<channel type>");
    result_ = ReceiveChannelType(builder_, level_ + 1);
    if (!result_) result_ = SendOrBidiChannelType(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "case" ( SendStmt | RecvStmt ) | "default"
  public static boolean CommCase(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommCase")) return false;
    if (!nextTokenIs(builder_, "<comm case>", CASE_KW, DEFAULT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMM_CASE, "<comm case>");
    result_ = CommCase_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "case" ( SendStmt | RecvStmt )
  private static boolean CommCase_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommCase_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CASE_KW);
    result_ = result_ && CommCase_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SendStmt | RecvStmt
  private static boolean CommCase_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommCase_0_1")) return false;
    boolean result_;
    result_ = SendStmt(builder_, level_ + 1);
    if (!result_) result_ = RecvStmt(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // CommCase ":" StatementList
  public static boolean CommClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommClause")) return false;
    if (!nextTokenIs(builder_, "<comm clause>", CASE_KW, DEFAULT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMM_CLAUSE, "<comm clause>");
    result_ = CommCase(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && StatementList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LiteralType LiteralValue
  public static boolean CompositeLit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CompositeLit")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMPOSITE_LIT, "<composite lit>");
    result_ = LiteralType(builder_, level_ + 1);
    result_ = result_ && LiteralValue(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LiteralTypeNoStruct LiteralValue
  public static boolean CompositeLitNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CompositeLitNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMPOSITE_LIT_NO_STRUCT, "<composite lit no struct>");
    result_ = LiteralTypeNoStruct(builder_, level_ + 1);
    result_ = result_ && LiteralValue(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ExpressionNoStruct
  public static boolean Condition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Condition")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONDITION, "<condition>");
    result_ = ExpressionNoStruct(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "const" ( ConstSpec | "(" ( ConstSpec ";" )* ")" )
  public static boolean ConstDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDecl")) return false;
    if (!nextTokenIs(builder_, CONST_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONST_DECL, null);
    result_ = consumeToken(builder_, CONST_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && ConstDecl_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ConstSpec | "(" ( ConstSpec ";" )* ")"
  private static boolean ConstDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDecl_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ConstSpec(builder_, level_ + 1);
    if (!result_) result_ = ConstDecl_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "(" ( ConstSpec ";" )* ")"
  private static boolean ConstDecl_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDecl_1_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && ConstDecl_1_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( ConstSpec ";" )*
  private static boolean ConstDecl_1_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDecl_1_1_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ConstDecl_1_1_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ConstDecl_1_1_1", pos_)) break;
    }
    return true;
  }

  // ConstSpec ";"
  private static boolean ConstDecl_1_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDecl_1_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ConstSpec(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IdentifierList ( ( Type )? "=" ExpressionList )?
  public static boolean ConstSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = IdentifierList(builder_, level_ + 1);
    result_ = result_ && ConstSpec_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, CONST_SPEC, result_);
    return result_;
  }

  // ( ( Type )? "=" ExpressionList )?
  private static boolean ConstSpec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec_1")) return false;
    ConstSpec_1_0(builder_, level_ + 1);
    return true;
  }

  // ( Type )? "=" ExpressionList
  private static boolean ConstSpec_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ConstSpec_1_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQ);
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( Type )?
  private static boolean ConstSpec_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec_1_0_0")) return false;
    ConstSpec_1_0_0_0(builder_, level_ + 1);
    return true;
  }

  // ( Type )
  private static boolean ConstSpec_1_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec_1_0_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "continue" ( Label )?
  public static boolean ContinueStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ContinueStmt")) return false;
    if (!nextTokenIs(builder_, CONTINUE_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CONTINUE_KW);
    result_ = result_ && ContinueStmt_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, CONTINUE_STMT, result_);
    return result_;
  }

  // ( Label )?
  private static boolean ContinueStmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ContinueStmt_1")) return false;
    ContinueStmt_1_0(builder_, level_ + 1);
    return true;
  }

  // ( Label )
  private static boolean ContinueStmt_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ContinueStmt_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Label(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Type "(" Expression ( "," )? ")"
  public static boolean Conversion(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Conversion")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONVERSION, "<conversion>");
    result_ = Type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && Expression(builder_, level_ + 1);
    result_ = result_ && Conversion_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "," )?
  private static boolean Conversion_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Conversion_3")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // ConstDecl | TypeDecl | VarDecl
  public static boolean Declaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Declaration")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECLARATION, "<declaration>");
    result_ = ConstDecl(builder_, level_ + 1);
    if (!result_) result_ = TypeDecl(builder_, level_ + 1);
    if (!result_) result_ = VarDecl(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "defer" Expression
  public static boolean DeferStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DeferStmt")) return false;
    if (!nextTokenIs(builder_, DEFER_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DEFER_KW);
    result_ = result_ && Expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, DEFER_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // Expression | LiteralValue
  public static boolean Element(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Element")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ELEMENT, "<element>");
    result_ = Expression(builder_, level_ + 1);
    if (!result_) result_ = LiteralValue(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // KeyedElement ( "," KeyedElement )*
  public static boolean ElementList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ELEMENT_LIST, "<element list>");
    result_ = KeyedElement(builder_, level_ + 1);
    result_ = result_ && ElementList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "," KeyedElement )*
  private static boolean ElementList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ElementList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ElementList_1", pos_)) break;
    }
    return true;
  }

  // "," KeyedElement
  private static boolean ElementList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && KeyedElement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Type
  public static boolean ElementType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementType")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ELEMENT_TYPE, "<element type>");
    result_ = Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ( "*" )? TypeName ( TypeArgs )?
  public static boolean EmbeddedField(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EmbeddedField")) return false;
    if (!nextTokenIs(builder_, "<embedded field>", IDENTIFIER, MUL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EMBEDDED_FIELD, "<embedded field>");
    result_ = EmbeddedField_0(builder_, level_ + 1);
    result_ = result_ && TypeName(builder_, level_ + 1);
    result_ = result_ && EmbeddedField_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "*" )?
  private static boolean EmbeddedField_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EmbeddedField_0")) return false;
    consumeToken(builder_, MUL);
    return true;
  }

  // ( TypeArgs )?
  private static boolean EmbeddedField_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EmbeddedField_2")) return false;
    EmbeddedField_2_0(builder_, level_ + 1);
    return true;
  }

  // ( TypeArgs )
  private static boolean EmbeddedField_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EmbeddedField_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeArgs(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ExprSwitchCase ":" StatementList
  public static boolean ExprCaseClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprCaseClause")) return false;
    if (!nextTokenIs(builder_, "<expr case clause>", CASE_KW, DEFAULT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_CASE_CLAUSE, "<expr case clause>");
    result_ = ExprSwitchCase(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && StatementList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "case" ExpressionList | "default"
  public static boolean ExprSwitchCase(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchCase")) return false;
    if (!nextTokenIs(builder_, "<expr switch case>", CASE_KW, DEFAULT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_SWITCH_CASE, "<expr switch case>");
    result_ = ExprSwitchCase_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "case" ExpressionList
  private static boolean ExprSwitchCase_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchCase_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CASE_KW);
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "switch" ( SimpleStmtNoStruct ";" )? ( ExpressionNoStruct )? "{" ExprCaseClause* "}"
  public static boolean ExprSwitchStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStmt")) return false;
    if (!nextTokenIs(builder_, SWITCH_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_SWITCH_STMT, null);
    result_ = consumeToken(builder_, SWITCH_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ExprSwitchStmt_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, ExprSwitchStmt_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, LBRACE)) && result_;
    result_ = pinned_ && report_error_(builder_, ExprSwitchStmt_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( SimpleStmtNoStruct ";" )?
  private static boolean ExprSwitchStmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStmt_1")) return false;
    ExprSwitchStmt_1_0(builder_, level_ + 1);
    return true;
  }

  // SimpleStmtNoStruct ";"
  private static boolean ExprSwitchStmt_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStmt_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = SimpleStmtNoStruct(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( ExpressionNoStruct )?
  private static boolean ExprSwitchStmt_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStmt_2")) return false;
    ExprSwitchStmt_2_0(builder_, level_ + 1);
    return true;
  }

  // ( ExpressionNoStruct )
  private static boolean ExprSwitchStmt_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStmt_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ExprCaseClause*
  private static boolean ExprSwitchStmt_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStmt_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ExprCaseClause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ExprSwitchStmt_4", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LogicalOrExpr
  public static boolean Expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION, "<expression>");
    result_ = LogicalOrExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // Expression ( "," Expression )*
  public static boolean ExpressionList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_LIST, "<expression list>");
    result_ = Expression(builder_, level_ + 1);
    result_ = result_ && ExpressionList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "," Expression )*
  private static boolean ExpressionList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ExpressionList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ExpressionList_1", pos_)) break;
    }
    return true;
  }

  // "," Expression
  private static boolean ExpressionList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && Expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LogicalOrExprNoStruct
  public static boolean ExpressionNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_NO_STRUCT, "<expression no struct>");
    result_ = LogicalOrExprNoStruct(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // Expression
  public static boolean ExpressionStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionStmt")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_STMT, "<expression stmt>");
    result_ = Expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ExpressionNoStruct
  public static boolean ExpressionStmtNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionStmtNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_STMT_NO_STRUCT, "<expression stmt no struct>");
    result_ = ExpressionNoStruct(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "fallthrough"
  public static boolean FallthroughStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FallthroughStmt")) return false;
    if (!nextTokenIs(builder_, FALLTHROUGH_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, FALLTHROUGH_KW);
    exit_section_(builder_, marker_, FALLTHROUGH_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // ( IdentifierList Type | EmbeddedField ) ( Tag )?
  public static boolean FieldDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDecl")) return false;
    if (!nextTokenIs(builder_, "<field decl>", IDENTIFIER, MUL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FIELD_DECL, "<field decl>");
    result_ = FieldDecl_0(builder_, level_ + 1);
    result_ = result_ && FieldDecl_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // IdentifierList Type | EmbeddedField
  private static boolean FieldDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDecl_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = FieldDecl_0_0(builder_, level_ + 1);
    if (!result_) result_ = EmbeddedField(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // IdentifierList Type
  private static boolean FieldDecl_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDecl_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = IdentifierList(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( Tag )?
  private static boolean FieldDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDecl_1")) return false;
    FieldDecl_1_0(builder_, level_ + 1);
    return true;
  }

  // ( Tag )
  private static boolean FieldDecl_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDecl_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Tag(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean FieldName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, FIELD_NAME, result_);
    return result_;
  }

  /* ********************************************************** */
  // ( InitStmtNoStruct )? ";" ( Condition )? ";" ( PostStmtNoStruct )?
  public static boolean ForClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FOR_CLAUSE, "<for clause>");
    result_ = ForClause_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && ForClause_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && ForClause_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( InitStmtNoStruct )?
  private static boolean ForClause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_0")) return false;
    ForClause_0_0(builder_, level_ + 1);
    return true;
  }

  // ( InitStmtNoStruct )
  private static boolean ForClause_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = InitStmtNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( Condition )?
  private static boolean ForClause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_2")) return false;
    ForClause_2_0(builder_, level_ + 1);
    return true;
  }

  // ( Condition )
  private static boolean ForClause_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Condition(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( PostStmtNoStruct )?
  private static boolean ForClause_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_4")) return false;
    ForClause_4_0(builder_, level_ + 1);
    return true;
  }

  // ( PostStmtNoStruct )
  private static boolean ForClause_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = PostStmtNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "for" ( RangeClause | ForClause | Condition )? Block
  public static boolean ForStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStmt")) return false;
    if (!nextTokenIs(builder_, FOR_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FOR_STMT, null);
    result_ = consumeToken(builder_, FOR_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ForStmt_1(builder_, level_ + 1));
    result_ = pinned_ && Block(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( RangeClause | ForClause | Condition )?
  private static boolean ForStmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStmt_1")) return false;
    ForStmt_1_0(builder_, level_ + 1);
    return true;
  }

  // RangeClause | ForClause | Condition
  private static boolean ForStmt_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStmt_1_0")) return false;
    boolean result_;
    result_ = RangeClause(builder_, level_ + 1);
    if (!result_) result_ = ForClause(builder_, level_ + 1);
    if (!result_) result_ = Condition(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // Block
  public static boolean FunctionBody(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionBody")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Block(builder_, level_ + 1);
    exit_section_(builder_, marker_, FUNCTION_BODY, result_);
    return result_;
  }

  /* ********************************************************** */
  // "func" FunctionName ( TypeParameters )? Signature ( FunctionBody )?
  public static boolean FunctionDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl")) return false;
    if (!nextTokenIs(builder_, FUNC_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_DECL, null);
    result_ = consumeToken(builder_, FUNC_KW);
    result_ = result_ && FunctionName(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, FunctionDecl_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, Signature(builder_, level_ + 1)) && result_;
    result_ = pinned_ && FunctionDecl_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( TypeParameters )?
  private static boolean FunctionDecl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_2")) return false;
    FunctionDecl_2_0(builder_, level_ + 1);
    return true;
  }

  // ( TypeParameters )
  private static boolean FunctionDecl_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeParameters(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( FunctionBody )?
  private static boolean FunctionDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_4")) return false;
    FunctionDecl_4_0(builder_, level_ + 1);
    return true;
  }

  // ( FunctionBody )
  private static boolean FunctionDecl_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = FunctionBody(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "func" Signature FunctionBody
  public static boolean FunctionLit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionLit")) return false;
    if (!nextTokenIs(builder_, FUNC_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, FUNC_KW);
    result_ = result_ && Signature(builder_, level_ + 1);
    result_ = result_ && FunctionBody(builder_, level_ + 1);
    exit_section_(builder_, marker_, FUNCTION_LIT, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean FunctionName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, FUNCTION_NAME, result_);
    return result_;
  }

  /* ********************************************************** */
  // "func" Signature
  public static boolean FunctionType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionType")) return false;
    if (!nextTokenIs(builder_, FUNC_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, FUNC_KW);
    result_ = result_ && Signature(builder_, level_ + 1);
    exit_section_(builder_, marker_, FUNCTION_TYPE, result_);
    return result_;
  }

  /* ********************************************************** */
  // "go" Expression
  public static boolean GoStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "GoStmt")) return false;
    if (!nextTokenIs(builder_, GO_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, GO_KW);
    result_ = result_ && Expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, GO_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // "goto" Label
  public static boolean GotoStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "GotoStmt")) return false;
    if (!nextTokenIs(builder_, GOTO_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, GOTO_KW);
    result_ = result_ && Label(builder_, level_ + 1);
    exit_section_(builder_, marker_, GOTO_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier ( "," identifier )*
  public static boolean IdentifierList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierList")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && IdentifierList_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, IDENTIFIER_LIST, result_);
    return result_;
  }

  // ( "," identifier )*
  private static boolean IdentifierList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!IdentifierList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "IdentifierList_1", pos_)) break;
    }
    return true;
  }

  // "," identifier
  private static boolean IdentifierList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMMA, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "if" ( SimpleStmtNoStruct ";" )? ExpressionNoStruct Block ( "else" ( IfStmt | Block ) )?
  public static boolean IfStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStmt")) return false;
    if (!nextTokenIs(builder_, IF_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_STMT, null);
    result_ = consumeToken(builder_, IF_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, IfStmt_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, ExpressionNoStruct(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, Block(builder_, level_ + 1)) && result_;
    result_ = pinned_ && IfStmt_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( SimpleStmtNoStruct ";" )?
  private static boolean IfStmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStmt_1")) return false;
    IfStmt_1_0(builder_, level_ + 1);
    return true;
  }

  // SimpleStmtNoStruct ";"
  private static boolean IfStmt_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStmt_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = SimpleStmtNoStruct(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( "else" ( IfStmt | Block ) )?
  private static boolean IfStmt_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStmt_4")) return false;
    IfStmt_4_0(builder_, level_ + 1);
    return true;
  }

  // "else" ( IfStmt | Block )
  private static boolean IfStmt_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStmt_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ELSE_KW);
    result_ = result_ && IfStmt_4_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // IfStmt | Block
  private static boolean IfStmt_4_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStmt_4_0_1")) return false;
    boolean result_;
    result_ = IfStmt(builder_, level_ + 1);
    if (!result_) result_ = Block(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // "import" ( ImportSpec | "(" ( ImportSpec ";" )* ")" )
  public static boolean ImportDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDecl")) return false;
    if (!nextTokenIs(builder_, IMPORT_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IMPORT_DECL, null);
    result_ = consumeToken(builder_, IMPORT_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && ImportDecl_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ImportSpec | "(" ( ImportSpec ";" )* ")"
  private static boolean ImportDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDecl_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ImportSpec(builder_, level_ + 1);
    if (!result_) result_ = ImportDecl_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "(" ( ImportSpec ";" )* ")"
  private static boolean ImportDecl_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDecl_1_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && ImportDecl_1_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( ImportSpec ";" )*
  private static boolean ImportDecl_1_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDecl_1_1_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ImportDecl_1_1_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ImportDecl_1_1_1", pos_)) break;
    }
    return true;
  }

  // ImportSpec ";"
  private static boolean ImportDecl_1_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDecl_1_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ImportSpec(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // string_lit
  public static boolean ImportPath(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportPath")) return false;
    if (!nextTokenIs(builder_, STRING_LIT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STRING_LIT);
    exit_section_(builder_, marker_, IMPORT_PATH, result_);
    return result_;
  }

  /* ********************************************************** */
  // ( "." | PackageName )? ImportPath
  public static boolean ImportSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpec")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IMPORT_SPEC, "<import spec>");
    result_ = ImportSpec_0(builder_, level_ + 1);
    result_ = result_ && ImportPath(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "." | PackageName )?
  private static boolean ImportSpec_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpec_0")) return false;
    ImportSpec_0_0(builder_, level_ + 1);
    return true;
  }

  // "." | PackageName
  private static boolean ImportSpec_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpec_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, DOT);
    if (!result_) result_ = PackageName(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // Expression ( "++" | "--" )
  public static boolean IncDecStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IncDecStmt")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INC_DEC_STMT, "<inc dec stmt>");
    result_ = Expression(builder_, level_ + 1);
    result_ = result_ && IncDecStmt_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "++" | "--"
  private static boolean IncDecStmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IncDecStmt_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, PLUS_PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS_MINUS);
    return result_;
  }

  /* ********************************************************** */
  // "[" Expression ( "," )? "]"
  public static boolean Index(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Index")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && Expression(builder_, level_ + 1);
    result_ = result_ && Index_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, INDEX, result_);
    return result_;
  }

  // ( "," )?
  private static boolean Index_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Index_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // SimpleStmtNoStruct
  public static boolean InitStmtNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InitStmtNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INIT_STMT_NO_STRUCT, "<init stmt no struct>");
    result_ = SimpleStmtNoStruct(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // MethodElem | TypeElem
  public static boolean InterfaceElem(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceElem")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INTERFACE_ELEM, "<interface elem>");
    result_ = MethodElem(builder_, level_ + 1);
    if (!result_) result_ = TypeElem(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "interface" "{" ( InterfaceElem ";"? )* "}"
  public static boolean InterfaceType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceType")) return false;
    if (!nextTokenIs(builder_, INTERFACE_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, INTERFACE_KW, LBRACE);
    result_ = result_ && InterfaceType_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, INTERFACE_TYPE, result_);
    return result_;
  }

  // ( InterfaceElem ";"? )*
  private static boolean InterfaceType_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceType_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!InterfaceType_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "InterfaceType_2", pos_)) break;
    }
    return true;
  }

  // InterfaceElem ";"?
  private static boolean InterfaceType_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceType_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = InterfaceElem(builder_, level_ + 1);
    result_ = result_ && InterfaceType_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ";"?
  private static boolean InterfaceType_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceType_2_0_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // FieldName | Expression | LiteralValue
  public static boolean Key(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Key")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY, "<key>");
    result_ = FieldName(builder_, level_ + 1);
    if (!result_) result_ = Expression(builder_, level_ + 1);
    if (!result_) result_ = LiteralValue(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // Type
  public static boolean KeyType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "KeyType")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEY_TYPE, "<key type>");
    result_ = Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ( Key ":" )? Element
  public static boolean KeyedElement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "KeyedElement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEYED_ELEMENT, "<keyed element>");
    result_ = KeyedElement_0(builder_, level_ + 1);
    result_ = result_ && Element(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( Key ":" )?
  private static boolean KeyedElement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "KeyedElement_0")) return false;
    KeyedElement_0_0(builder_, level_ + 1);
    return true;
  }

  // Key ":"
  private static boolean KeyedElement_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "KeyedElement_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Key(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean Label(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Label")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, LABEL, result_);
    return result_;
  }

  /* ********************************************************** */
  // Label ":" Statement
  public static boolean LabeledStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LabeledStmt")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Label(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && Statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, LABELED_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // BasicLit | CompositeLit | FunctionLit
  public static boolean Literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Literal")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LITERAL, "<literal>");
    result_ = BasicLit(builder_, level_ + 1);
    if (!result_) result_ = CompositeLit(builder_, level_ + 1);
    if (!result_) result_ = FunctionLit(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // StructType | ArrayType | "[" "..." "]" ElementType | SliceType | MapType | TypeName ( TypeArgs )?
  public static boolean LiteralType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralType")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LITERAL_TYPE, "<literal type>");
    result_ = StructType(builder_, level_ + 1);
    if (!result_) result_ = ArrayType(builder_, level_ + 1);
    if (!result_) result_ = LiteralType_2(builder_, level_ + 1);
    if (!result_) result_ = SliceType(builder_, level_ + 1);
    if (!result_) result_ = MapType(builder_, level_ + 1);
    if (!result_) result_ = LiteralType_5(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "[" "..." "]" ElementType
  private static boolean LiteralType_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralType_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LBRACK, ELLIPSIS, RBRACK);
    result_ = result_ && ElementType(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // TypeName ( TypeArgs )?
  private static boolean LiteralType_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralType_5")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeName(builder_, level_ + 1);
    result_ = result_ && LiteralType_5_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( TypeArgs )?
  private static boolean LiteralType_5_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralType_5_1")) return false;
    LiteralType_5_1_0(builder_, level_ + 1);
    return true;
  }

  // ( TypeArgs )
  private static boolean LiteralType_5_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralType_5_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeArgs(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // StructType | ArrayType | "[" "..." "]" ElementType | SliceType | MapType
  public static boolean LiteralTypeNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralTypeNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LITERAL_TYPE_NO_STRUCT, "<literal type no struct>");
    result_ = StructType(builder_, level_ + 1);
    if (!result_) result_ = ArrayType(builder_, level_ + 1);
    if (!result_) result_ = LiteralTypeNoStruct_2(builder_, level_ + 1);
    if (!result_) result_ = SliceType(builder_, level_ + 1);
    if (!result_) result_ = MapType(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "[" "..." "]" ElementType
  private static boolean LiteralTypeNoStruct_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralTypeNoStruct_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LBRACK, ELLIPSIS, RBRACK);
    result_ = result_ && ElementType(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "{" ( ElementList ( "," )? )? "}"
  public static boolean LiteralValue(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralValue")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && LiteralValue_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, LITERAL_VALUE, result_);
    return result_;
  }

  // ( ElementList ( "," )? )?
  private static boolean LiteralValue_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralValue_1")) return false;
    LiteralValue_1_0(builder_, level_ + 1);
    return true;
  }

  // ElementList ( "," )?
  private static boolean LiteralValue_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralValue_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ElementList(builder_, level_ + 1);
    result_ = result_ && LiteralValue_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( "," )?
  private static boolean LiteralValue_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralValue_1_0_1")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // RelationalExpr ( "&&" RelationalExpr )*
  public static boolean LogicalAndExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LOGICAL_AND_EXPR, "<logical and expr>");
    result_ = RelationalExpr(builder_, level_ + 1);
    result_ = result_ && LogicalAndExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "&&" RelationalExpr )*
  private static boolean LogicalAndExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!LogicalAndExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "LogicalAndExpr_1", pos_)) break;
    }
    return true;
  }

  // "&&" RelationalExpr
  private static boolean LogicalAndExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COND_AND);
    result_ = result_ && RelationalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // RelationalExprNoStruct ( "&&" RelationalExprNoStruct )*
  public static boolean LogicalAndExprNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExprNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LOGICAL_AND_EXPR_NO_STRUCT, "<logical and expr no struct>");
    result_ = RelationalExprNoStruct(builder_, level_ + 1);
    result_ = result_ && LogicalAndExprNoStruct_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "&&" RelationalExprNoStruct )*
  private static boolean LogicalAndExprNoStruct_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExprNoStruct_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!LogicalAndExprNoStruct_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "LogicalAndExprNoStruct_1", pos_)) break;
    }
    return true;
  }

  // "&&" RelationalExprNoStruct
  private static boolean LogicalAndExprNoStruct_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExprNoStruct_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COND_AND);
    result_ = result_ && RelationalExprNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LogicalAndExpr ( "||" LogicalAndExpr )*
  public static boolean LogicalOrExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LOGICAL_OR_EXPR, "<logical or expr>");
    result_ = LogicalAndExpr(builder_, level_ + 1);
    result_ = result_ && LogicalOrExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "||" LogicalAndExpr )*
  private static boolean LogicalOrExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!LogicalOrExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "LogicalOrExpr_1", pos_)) break;
    }
    return true;
  }

  // "||" LogicalAndExpr
  private static boolean LogicalOrExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COND_OR);
    result_ = result_ && LogicalAndExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LogicalAndExprNoStruct ( "||" LogicalAndExprNoStruct )*
  public static boolean LogicalOrExprNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExprNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LOGICAL_OR_EXPR_NO_STRUCT, "<logical or expr no struct>");
    result_ = LogicalAndExprNoStruct(builder_, level_ + 1);
    result_ = result_ && LogicalOrExprNoStruct_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "||" LogicalAndExprNoStruct )*
  private static boolean LogicalOrExprNoStruct_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExprNoStruct_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!LogicalOrExprNoStruct_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "LogicalOrExprNoStruct_1", pos_)) break;
    }
    return true;
  }

  // "||" LogicalAndExprNoStruct
  private static boolean LogicalOrExprNoStruct_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExprNoStruct_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COND_OR);
    result_ = result_ && LogicalAndExprNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "map" "[" KeyType "]" ElementType
  public static boolean MapType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MapType")) return false;
    if (!nextTokenIs(builder_, MAP_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MAP_KW, LBRACK);
    result_ = result_ && KeyType(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    result_ = result_ && ElementType(builder_, level_ + 1);
    exit_section_(builder_, marker_, MAP_TYPE, result_);
    return result_;
  }

  /* ********************************************************** */
  // "func" Receiver MethodName Signature ( FunctionBody )?
  public static boolean MethodDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodDecl")) return false;
    if (!nextTokenIs(builder_, FUNC_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, METHOD_DECL, null);
    result_ = consumeToken(builder_, FUNC_KW);
    result_ = result_ && Receiver(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, MethodName(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, Signature(builder_, level_ + 1)) && result_;
    result_ = pinned_ && MethodDecl_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( FunctionBody )?
  private static boolean MethodDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodDecl_4")) return false;
    MethodDecl_4_0(builder_, level_ + 1);
    return true;
  }

  // ( FunctionBody )
  private static boolean MethodDecl_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodDecl_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = FunctionBody(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // MethodName Signature
  public static boolean MethodElem(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodElem")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = MethodName(builder_, level_ + 1);
    result_ = result_ && Signature(builder_, level_ + 1);
    exit_section_(builder_, marker_, METHOD_ELEM, result_);
    return result_;
  }

  /* ********************************************************** */
  // ReceiverType "." MethodName
  public static boolean MethodExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, METHOD_EXPR, "<method expr>");
    result_ = ReceiverType(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, DOT);
    result_ = result_ && MethodName(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean MethodName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, METHOD_NAME, result_);
    return result_;
  }

  /* ********************************************************** */
  // UnaryExpr ( mul_op UnaryExpr )*
  public static boolean MulExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MulExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MUL_EXPR, "<mul expr>");
    result_ = UnaryExpr(builder_, level_ + 1);
    result_ = result_ && MulExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( mul_op UnaryExpr )*
  private static boolean MulExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MulExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!MulExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "MulExpr_1", pos_)) break;
    }
    return true;
  }

  // mul_op UnaryExpr
  private static boolean MulExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MulExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mul_op(builder_, level_ + 1);
    result_ = result_ && UnaryExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // UnaryExprNoStruct ( mul_op UnaryExprNoStruct )*
  public static boolean MulExprNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MulExprNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MUL_EXPR_NO_STRUCT, "<mul expr no struct>");
    result_ = UnaryExprNoStruct(builder_, level_ + 1);
    result_ = result_ && MulExprNoStruct_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( mul_op UnaryExprNoStruct )*
  private static boolean MulExprNoStruct_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MulExprNoStruct_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!MulExprNoStruct_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "MulExprNoStruct_1", pos_)) break;
    }
    return true;
  }

  // mul_op UnaryExprNoStruct
  private static boolean MulExprNoStruct_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MulExprNoStruct_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mul_op(builder_, level_ + 1);
    result_ = result_ && UnaryExprNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Literal | OperandName ( TypeArgs )? | "(" Expression ")"
  public static boolean Operand(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Operand")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERAND, "<operand>");
    result_ = Literal(builder_, level_ + 1);
    if (!result_) result_ = Operand_1(builder_, level_ + 1);
    if (!result_) result_ = Operand_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // OperandName ( TypeArgs )?
  private static boolean Operand_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Operand_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = OperandName(builder_, level_ + 1);
    result_ = result_ && Operand_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( TypeArgs )?
  private static boolean Operand_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Operand_1_1")) return false;
    Operand_1_1_0(builder_, level_ + 1);
    return true;
  }

  // ( TypeArgs )
  private static boolean Operand_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Operand_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeArgs(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "(" Expression ")"
  private static boolean Operand_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Operand_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && Expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier ( "." identifier )?
  public static boolean OperandName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && OperandName_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, OPERAND_NAME, result_);
    return result_;
  }

  // ( "." identifier )?
  private static boolean OperandName_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandName_1")) return false;
    OperandName_1_0(builder_, level_ + 1);
    return true;
  }

  // "." identifier
  private static boolean OperandName_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandName_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, DOT, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // BasicLit | CompositeLitNoStruct | FunctionLit | OperandName ( TypeArgs )? | "(" Expression ")"
  public static boolean OperandNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERAND_NO_STRUCT, "<operand no struct>");
    result_ = BasicLit(builder_, level_ + 1);
    if (!result_) result_ = CompositeLitNoStruct(builder_, level_ + 1);
    if (!result_) result_ = FunctionLit(builder_, level_ + 1);
    if (!result_) result_ = OperandNoStruct_3(builder_, level_ + 1);
    if (!result_) result_ = OperandNoStruct_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // OperandName ( TypeArgs )?
  private static boolean OperandNoStruct_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandNoStruct_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = OperandName(builder_, level_ + 1);
    result_ = result_ && OperandNoStruct_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( TypeArgs )?
  private static boolean OperandNoStruct_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandNoStruct_3_1")) return false;
    OperandNoStruct_3_1_0(builder_, level_ + 1);
    return true;
  }

  // ( TypeArgs )
  private static boolean OperandNoStruct_3_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandNoStruct_3_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeArgs(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "(" Expression ")"
  private static boolean OperandNoStruct_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandNoStruct_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && Expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "package" PackageName
  public static boolean PackageClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PackageClause")) return false;
    if (!nextTokenIs(builder_, PACKAGE_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PACKAGE_CLAUSE, null);
    result_ = consumeToken(builder_, PACKAGE_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && PackageName(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean PackageName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PackageName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, PACKAGE_NAME, result_);
    return result_;
  }

  /* ********************************************************** */
  // IdentifierList "..."? Type | "..."? Type
  public static boolean ParameterDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_DECL, "<parameter decl>");
    result_ = ParameterDecl_0(builder_, level_ + 1);
    if (!result_) result_ = ParameterDecl_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // IdentifierList "..."? Type
  private static boolean ParameterDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDecl_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = IdentifierList(builder_, level_ + 1);
    result_ = result_ && ParameterDecl_0_1(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "..."?
  private static boolean ParameterDecl_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDecl_0_1")) return false;
    consumeToken(builder_, ELLIPSIS);
    return true;
  }

  // "..."? Type
  private static boolean ParameterDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDecl_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ParameterDecl_1_0(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "..."?
  private static boolean ParameterDecl_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDecl_1_0")) return false;
    consumeToken(builder_, ELLIPSIS);
    return true;
  }

  /* ********************************************************** */
  // ParameterDecl ( "," ParameterDecl )*
  public static boolean ParameterList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_LIST, "<parameter list>");
    result_ = ParameterDecl(builder_, level_ + 1);
    result_ = result_ && ParameterList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "," ParameterDecl )*
  private static boolean ParameterList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ParameterList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ParameterList_1", pos_)) break;
    }
    return true;
  }

  // "," ParameterDecl
  private static boolean ParameterList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && ParameterDecl(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "(" ( ParameterList ( "," )? )? ")"
  public static boolean Parameters(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && Parameters_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, PARAMETERS, result_);
    return result_;
  }

  // ( ParameterList ( "," )? )?
  private static boolean Parameters_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters_1")) return false;
    Parameters_1_0(builder_, level_ + 1);
    return true;
  }

  // ParameterList ( "," )?
  private static boolean Parameters_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ParameterList(builder_, level_ + 1);
    result_ = result_ && Parameters_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( "," )?
  private static boolean Parameters_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters_1_0_1")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // "*" BaseType
  public static boolean PointerType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PointerType")) return false;
    if (!nextTokenIs(builder_, MUL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MUL);
    result_ = result_ && BaseType(builder_, level_ + 1);
    exit_section_(builder_, marker_, POINTER_TYPE, result_);
    return result_;
  }

  /* ********************************************************** */
  // SimpleStmtNoStruct
  public static boolean PostStmtNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PostStmtNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, POST_STMT_NO_STRUCT, "<post stmt no struct>");
    result_ = SimpleStmtNoStruct(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // PrimaryOperand PrimarySuffix*
  public static boolean PrimaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimaryExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMARY_EXPR, "<primary expr>");
    result_ = PrimaryOperand(builder_, level_ + 1);
    result_ = result_ && PrimaryExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // PrimarySuffix*
  private static boolean PrimaryExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimaryExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!PrimarySuffix(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "PrimaryExpr_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PrimaryOperandNoStruct PrimarySuffix*
  public static boolean PrimaryExprNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimaryExprNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMARY_EXPR_NO_STRUCT, "<primary expr no struct>");
    result_ = PrimaryOperandNoStruct(builder_, level_ + 1);
    result_ = result_ && PrimaryExprNoStruct_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // PrimarySuffix*
  private static boolean PrimaryExprNoStruct_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimaryExprNoStruct_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!PrimarySuffix(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "PrimaryExprNoStruct_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // Operand | Conversion | MethodExpr
  public static boolean PrimaryOperand(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimaryOperand")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMARY_OPERAND, "<primary operand>");
    result_ = Operand(builder_, level_ + 1);
    if (!result_) result_ = Conversion(builder_, level_ + 1);
    if (!result_) result_ = MethodExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // OperandNoStruct | Conversion | MethodExpr
  public static boolean PrimaryOperandNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimaryOperandNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMARY_OPERAND_NO_STRUCT, "<primary operand no struct>");
    result_ = OperandNoStruct(builder_, level_ + 1);
    if (!result_) result_ = Conversion(builder_, level_ + 1);
    if (!result_) result_ = MethodExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // Selector | Index | Slice | TypeAssertion | Arguments
  public static boolean PrimarySuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimarySuffix")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMARY_SUFFIX, "<primary suffix>");
    result_ = Selector(builder_, level_ + 1);
    if (!result_) result_ = Index(builder_, level_ + 1);
    if (!result_) result_ = Slice(builder_, level_ + 1);
    if (!result_) result_ = TypeAssertion(builder_, level_ + 1);
    if (!result_) result_ = Arguments(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ( ExpressionList ( "=" | ":=" ) )? "range" ExpressionNoStruct
  public static boolean RangeClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RangeClause")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RANGE_CLAUSE, "<range clause>");
    result_ = RangeClause_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RANGE_KW);
    result_ = result_ && ExpressionNoStruct(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( ExpressionList ( "=" | ":=" ) )?
  private static boolean RangeClause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RangeClause_0")) return false;
    RangeClause_0_0(builder_, level_ + 1);
    return true;
  }

  // ExpressionList ( "=" | ":=" )
  private static boolean RangeClause_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RangeClause_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && RangeClause_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "=" | ":="
  private static boolean RangeClause_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RangeClause_0_0_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, ASSIGN);
    return result_;
  }

  /* ********************************************************** */
  // "<-" "chan" Type
  public static boolean ReceiveChannelType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReceiveChannelType")) return false;
    if (!nextTokenIs(builder_, SEND_CHANNEL)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RECEIVE_CHANNEL_TYPE, null);
    result_ = consumeTokens(builder_, 2, SEND_CHANNEL, CHAN_KW);
    pinned_ = result_; // pin = 2
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // Parameters
  public static boolean Receiver(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Receiver")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Parameters(builder_, level_ + 1);
    exit_section_(builder_, marker_, RECEIVER, result_);
    return result_;
  }

  /* ********************************************************** */
  // Type
  public static boolean ReceiverType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReceiverType")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RECEIVER_TYPE, "<receiver type>");
    result_ = Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // Expression
  public static boolean RecvExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RECV_EXPR, "<recv expr>");
    result_ = Expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ( ExpressionList ( "=" | ":=" ) )? RecvExpr
  public static boolean RecvStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStmt")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RECV_STMT, "<recv stmt>");
    result_ = RecvStmt_0(builder_, level_ + 1);
    result_ = result_ && RecvExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( ExpressionList ( "=" | ":=" ) )?
  private static boolean RecvStmt_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStmt_0")) return false;
    RecvStmt_0_0(builder_, level_ + 1);
    return true;
  }

  // ExpressionList ( "=" | ":=" )
  private static boolean RecvStmt_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStmt_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && RecvStmt_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "=" | ":="
  private static boolean RecvStmt_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStmt_0_0_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, ASSIGN);
    return result_;
  }

  /* ********************************************************** */
  // AddExpr ( rel_op AddExpr )*
  public static boolean RelationalExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RELATIONAL_EXPR, "<relational expr>");
    result_ = AddExpr(builder_, level_ + 1);
    result_ = result_ && RelationalExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( rel_op AddExpr )*
  private static boolean RelationalExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!RelationalExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "RelationalExpr_1", pos_)) break;
    }
    return true;
  }

  // rel_op AddExpr
  private static boolean RelationalExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = rel_op(builder_, level_ + 1);
    result_ = result_ && AddExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // AddExprNoStruct ( rel_op AddExprNoStruct )*
  public static boolean RelationalExprNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExprNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RELATIONAL_EXPR_NO_STRUCT, "<relational expr no struct>");
    result_ = AddExprNoStruct(builder_, level_ + 1);
    result_ = result_ && RelationalExprNoStruct_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( rel_op AddExprNoStruct )*
  private static boolean RelationalExprNoStruct_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExprNoStruct_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!RelationalExprNoStruct_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "RelationalExprNoStruct_1", pos_)) break;
    }
    return true;
  }

  // rel_op AddExprNoStruct
  private static boolean RelationalExprNoStruct_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExprNoStruct_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = rel_op(builder_, level_ + 1);
    result_ = result_ && AddExprNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Parameters | Type
  public static boolean Result(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Result")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RESULT, "<result>");
    result_ = Parameters(builder_, level_ + 1);
    if (!result_) result_ = Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "return" ( ExpressionList )?
  public static boolean ReturnStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReturnStmt")) return false;
    if (!nextTokenIs(builder_, RETURN_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RETURN_KW);
    result_ = result_ && ReturnStmt_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, RETURN_STMT, result_);
    return result_;
  }

  // ( ExpressionList )?
  private static boolean ReturnStmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReturnStmt_1")) return false;
    ReturnStmt_1_0(builder_, level_ + 1);
    return true;
  }

  // ( ExpressionList )
  private static boolean ReturnStmt_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReturnStmt_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "select" "{" CommClause* "}"
  public static boolean SelectStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectStmt")) return false;
    if (!nextTokenIs(builder_, SELECT_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SELECT_STMT, null);
    result_ = consumeTokens(builder_, 1, SELECT_KW, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, SelectStmt_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // CommClause*
  private static boolean SelectStmt_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectStmt_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!CommClause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "SelectStmt_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "." identifier
  public static boolean Selector(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Selector")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, DOT, IDENTIFIER);
    exit_section_(builder_, marker_, SELECTOR, result_);
    return result_;
  }

  /* ********************************************************** */
  // "chan" "<-"? Type
  public static boolean SendOrBidiChannelType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SendOrBidiChannelType")) return false;
    if (!nextTokenIs(builder_, CHAN_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SEND_OR_BIDI_CHANNEL_TYPE, null);
    result_ = consumeToken(builder_, CHAN_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, SendOrBidiChannelType_1(builder_, level_ + 1));
    result_ = pinned_ && Type(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // "<-"?
  private static boolean SendOrBidiChannelType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SendOrBidiChannelType_1")) return false;
    consumeToken(builder_, SEND_CHANNEL);
    return true;
  }

  /* ********************************************************** */
  // Channel "<-" Expression
  public static boolean SendStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SendStmt")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SEND_STMT, "<send stmt>");
    result_ = Channel(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEND_CHANNEL);
    result_ = result_ && Expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ExpressionList ":=" ExpressionList
  public static boolean ShortVarDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ShortVarDecl")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SHORT_VAR_DECL, "<short var decl>");
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ASSIGN);
    pinned_ = result_; // pin = 2
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // Parameters ( Result )?
  public static boolean Signature(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Signature")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Parameters(builder_, level_ + 1);
    result_ = result_ && Signature_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, SIGNATURE, result_);
    return result_;
  }

  // ( Result )?
  private static boolean Signature_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Signature_1")) return false;
    Signature_1_0(builder_, level_ + 1);
    return true;
  }

  // ( Result )
  private static boolean Signature_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Signature_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Result(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ShortVarDecl | Assignment | SendStmt | IncDecStmt | ExpressionStmt
  public static boolean SimpleStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStmt")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SIMPLE_STMT, "<simple stmt>");
    result_ = ShortVarDecl(builder_, level_ + 1);
    if (!result_) result_ = Assignment(builder_, level_ + 1);
    if (!result_) result_ = SendStmt(builder_, level_ + 1);
    if (!result_) result_ = IncDecStmt(builder_, level_ + 1);
    if (!result_) result_ = ExpressionStmt(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ShortVarDecl | Assignment | SendStmt | IncDecStmt | ExpressionStmtNoStruct
  public static boolean SimpleStmtNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStmtNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SIMPLE_STMT_NO_STRUCT, "<simple stmt no struct>");
    result_ = ShortVarDecl(builder_, level_ + 1);
    if (!result_) result_ = Assignment(builder_, level_ + 1);
    if (!result_) result_ = SendStmt(builder_, level_ + 1);
    if (!result_) result_ = IncDecStmt(builder_, level_ + 1);
    if (!result_) result_ = ExpressionStmtNoStruct(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "[" ( Expression )? ":" ( Expression )? "]" | "[" ( Expression )? ":" Expression ":" Expression "]"
  public static boolean Slice(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Slice_0(builder_, level_ + 1);
    if (!result_) result_ = Slice_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, SLICE, result_);
    return result_;
  }

  // "[" ( Expression )? ":" ( Expression )? "]"
  private static boolean Slice_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && Slice_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && Slice_0_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( Expression )?
  private static boolean Slice_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice_0_1")) return false;
    Slice_0_1_0(builder_, level_ + 1);
    return true;
  }

  // ( Expression )
  private static boolean Slice_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( Expression )?
  private static boolean Slice_0_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice_0_3")) return false;
    Slice_0_3_0(builder_, level_ + 1);
    return true;
  }

  // ( Expression )
  private static boolean Slice_0_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice_0_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "[" ( Expression )? ":" Expression ":" Expression "]"
  private static boolean Slice_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && Slice_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && Expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && Expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( Expression )?
  private static boolean Slice_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice_1_1")) return false;
    Slice_1_1_0(builder_, level_ + 1);
    return true;
  }

  // ( Expression )
  private static boolean Slice_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Slice_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "[" "]" ElementType
  public static boolean SliceType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SliceType")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LBRACK, RBRACK);
    result_ = result_ && ElementType(builder_, level_ + 1);
    exit_section_(builder_, marker_, SLICE_TYPE, result_);
    return result_;
  }

  /* ********************************************************** */
  // package_clause_item import_item* top_level_item*
  public static boolean SourceFile(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SourceFile")) return false;
    if (!nextTokenIs(builder_, PACKAGE_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = package_clause_item(builder_, level_ + 1);
    result_ = result_ && SourceFile_1(builder_, level_ + 1);
    result_ = result_ && SourceFile_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, SOURCE_FILE, result_);
    return result_;
  }

  // import_item*
  private static boolean SourceFile_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SourceFile_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!import_item(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "SourceFile_1", pos_)) break;
    }
    return true;
  }

  // top_level_item*
  private static boolean SourceFile_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SourceFile_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_level_item(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "SourceFile_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // Declaration | ReturnStmt | GoStmt | DeferStmt | IfStmt | ForStmt | SwitchStmt | SelectStmt | BreakStmt | ContinueStmt | GotoStmt | FallthroughStmt | Block | LabeledStmt | SimpleStmt
  public static boolean Statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT, "<statement>");
    result_ = Declaration(builder_, level_ + 1);
    if (!result_) result_ = ReturnStmt(builder_, level_ + 1);
    if (!result_) result_ = GoStmt(builder_, level_ + 1);
    if (!result_) result_ = DeferStmt(builder_, level_ + 1);
    if (!result_) result_ = IfStmt(builder_, level_ + 1);
    if (!result_) result_ = ForStmt(builder_, level_ + 1);
    if (!result_) result_ = SwitchStmt(builder_, level_ + 1);
    if (!result_) result_ = SelectStmt(builder_, level_ + 1);
    if (!result_) result_ = BreakStmt(builder_, level_ + 1);
    if (!result_) result_ = ContinueStmt(builder_, level_ + 1);
    if (!result_) result_ = GotoStmt(builder_, level_ + 1);
    if (!result_) result_ = FallthroughStmt(builder_, level_ + 1);
    if (!result_) result_ = Block(builder_, level_ + 1);
    if (!result_) result_ = LabeledStmt(builder_, level_ + 1);
    if (!result_) result_ = SimpleStmt(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ( statement_item | ";" )*
  public static boolean StatementList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StatementList")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT_LIST, "<statement list>");
    while (true) {
      int pos_ = current_position_(builder_);
      if (!StatementList_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "StatementList", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  // statement_item | ";"
  private static boolean StatementList_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StatementList_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = statement_item(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // string_lit
  public static boolean StringLiteral(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StringLiteral")) return false;
    if (!nextTokenIs(builder_, STRING_LIT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STRING_LIT);
    exit_section_(builder_, marker_, STRING_LITERAL, result_);
    return result_;
  }

  /* ********************************************************** */
  // "struct" "{" (FieldDecl ";"?)* "}"
  public static boolean StructType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructType")) return false;
    if (!nextTokenIs(builder_, STRUCT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, STRUCT_KW, LBRACE);
    result_ = result_ && StructType_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, STRUCT_TYPE, result_);
    return result_;
  }

  // (FieldDecl ";"?)*
  private static boolean StructType_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructType_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!StructType_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "StructType_2", pos_)) break;
    }
    return true;
  }

  // FieldDecl ";"?
  private static boolean StructType_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructType_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = FieldDecl(builder_, level_ + 1);
    result_ = result_ && StructType_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ";"?
  private static boolean StructType_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructType_2_0_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // TypeSwitchStmt | ExprSwitchStmt
  public static boolean SwitchStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SwitchStmt")) return false;
    if (!nextTokenIs(builder_, SWITCH_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeSwitchStmt(builder_, level_ + 1);
    if (!result_) result_ = ExprSwitchStmt(builder_, level_ + 1);
    exit_section_(builder_, marker_, SWITCH_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // string_lit
  public static boolean Tag(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Tag")) return false;
    if (!nextTokenIs(builder_, STRING_LIT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STRING_LIT);
    exit_section_(builder_, marker_, TAG, result_);
    return result_;
  }

  /* ********************************************************** */
  // Declaration | FunctionDecl | MethodDecl
  public static boolean TopLevelDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TopLevelDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOP_LEVEL_DECL, "<top level decl>");
    result_ = Declaration(builder_, level_ + 1);
    if (!result_) result_ = FunctionDecl(builder_, level_ + 1);
    if (!result_) result_ = MethodDecl(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // TypeName ( TypeArgs )? | TypeLit | "(" Type ")"
  public static boolean Type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE, "<type>");
    result_ = Type_0(builder_, level_ + 1);
    if (!result_) result_ = TypeLit(builder_, level_ + 1);
    if (!result_) result_ = Type_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // TypeName ( TypeArgs )?
  private static boolean Type_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Type_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeName(builder_, level_ + 1);
    result_ = result_ && Type_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( TypeArgs )?
  private static boolean Type_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Type_0_1")) return false;
    Type_0_1_0(builder_, level_ + 1);
    return true;
  }

  // ( TypeArgs )
  private static boolean Type_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Type_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeArgs(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "(" Type ")"
  private static boolean Type_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Type_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && Type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "[" TypeList ( "," )? "]"
  public static boolean TypeArgs(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeArgs")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && TypeList(builder_, level_ + 1);
    result_ = result_ && TypeArgs_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, TYPE_ARGS, result_);
    return result_;
  }

  // ( "," )?
  private static boolean TypeArgs_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeArgs_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // "." "(" Type ")"
  public static boolean TypeAssertion(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeAssertion")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, DOT, LPAREN);
    result_ = result_ && Type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, TYPE_ASSERTION, result_);
    return result_;
  }

  /* ********************************************************** */
  // TypeSwitchCase ":" StatementList
  public static boolean TypeCaseClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeCaseClause")) return false;
    if (!nextTokenIs(builder_, "<type case clause>", CASE_KW, DEFAULT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_CASE_CLAUSE, "<type case clause>");
    result_ = TypeSwitchCase(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && StatementList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // TypeElem
  public static boolean TypeConstraint(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeConstraint")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_CONSTRAINT, "<type constraint>");
    result_ = TypeElem(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // "type" ( TypeSpec | "(" ( TypeSpec ";" )* ")" )
  public static boolean TypeDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDecl")) return false;
    if (!nextTokenIs(builder_, TYPE_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_DECL, null);
    result_ = consumeToken(builder_, TYPE_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && TypeDecl_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // TypeSpec | "(" ( TypeSpec ";" )* ")"
  private static boolean TypeDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDecl_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeSpec(builder_, level_ + 1);
    if (!result_) result_ = TypeDecl_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "(" ( TypeSpec ";" )* ")"
  private static boolean TypeDecl_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDecl_1_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && TypeDecl_1_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( TypeSpec ";" )*
  private static boolean TypeDecl_1_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDecl_1_1_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!TypeDecl_1_1_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeDecl_1_1_1", pos_)) break;
    }
    return true;
  }

  // TypeSpec ";"
  private static boolean TypeDecl_1_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDecl_1_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeSpec(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier ( TypeParameters )? Type
  public static boolean TypeDef(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDef")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && TypeDef_1(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, TYPE_DEF, result_);
    return result_;
  }

  // ( TypeParameters )?
  private static boolean TypeDef_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDef_1")) return false;
    TypeDef_1_0(builder_, level_ + 1);
    return true;
  }

  // ( TypeParameters )
  private static boolean TypeDef_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDef_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeParameters(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TypeTerm ( "|" TypeTerm )*
  public static boolean TypeElem(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeElem")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_ELEM, "<type elem>");
    result_ = TypeTerm(builder_, level_ + 1);
    result_ = result_ && TypeElem_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "|" TypeTerm )*
  private static boolean TypeElem_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeElem_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!TypeElem_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeElem_1", pos_)) break;
    }
    return true;
  }

  // "|" TypeTerm
  private static boolean TypeElem_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeElem_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BIT_OR);
    result_ = result_ && TypeTerm(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Type ( "," Type )*
  public static boolean TypeList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeList")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_LIST, "<type list>");
    result_ = Type(builder_, level_ + 1);
    result_ = result_ && TypeList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( "," Type )*
  private static boolean TypeList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!TypeList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeList_1", pos_)) break;
    }
    return true;
  }

  // "," Type
  private static boolean TypeList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ArrayType | StructType | PointerType | FunctionType | InterfaceType | SliceType | MapType | ChannelType
  public static boolean TypeLit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeLit")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_LIT, "<type lit>");
    result_ = ArrayType(builder_, level_ + 1);
    if (!result_) result_ = StructType(builder_, level_ + 1);
    if (!result_) result_ = PointerType(builder_, level_ + 1);
    if (!result_) result_ = FunctionType(builder_, level_ + 1);
    if (!result_) result_ = InterfaceType(builder_, level_ + 1);
    if (!result_) result_ = SliceType(builder_, level_ + 1);
    if (!result_) result_ = MapType(builder_, level_ + 1);
    if (!result_) result_ = ChannelType(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // identifier ( "." identifier )?
  public static boolean TypeName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && TypeName_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, TYPE_NAME, result_);
    return result_;
  }

  // ( "." identifier )?
  private static boolean TypeName_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeName_1")) return false;
    TypeName_1_0(builder_, level_ + 1);
    return true;
  }

  // "." identifier
  private static boolean TypeName_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeName_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, DOT, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IdentifierList TypeConstraint
  public static boolean TypeParamDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeParamDecl")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = IdentifierList(builder_, level_ + 1);
    result_ = result_ && TypeConstraint(builder_, level_ + 1);
    exit_section_(builder_, marker_, TYPE_PARAM_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // TypeParamDecl ( "," TypeParamDecl )*
  public static boolean TypeParamList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeParamList")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeParamDecl(builder_, level_ + 1);
    result_ = result_ && TypeParamList_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, TYPE_PARAM_LIST, result_);
    return result_;
  }

  // ( "," TypeParamDecl )*
  private static boolean TypeParamList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeParamList_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!TypeParamList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeParamList_1", pos_)) break;
    }
    return true;
  }

  // "," TypeParamDecl
  private static boolean TypeParamList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeParamList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && TypeParamDecl(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "[" TypeParamList ( "," )? "]"
  public static boolean TypeParameters(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeParameters")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && TypeParamList(builder_, level_ + 1);
    result_ = result_ && TypeParameters_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, TYPE_PARAMETERS, result_);
    return result_;
  }

  // ( "," )?
  private static boolean TypeParameters_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeParameters_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // AliasDecl | TypeDef
  public static boolean TypeSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSpec")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = AliasDecl(builder_, level_ + 1);
    if (!result_) result_ = TypeDef(builder_, level_ + 1);
    exit_section_(builder_, marker_, TYPE_SPEC, result_);
    return result_;
  }

  /* ********************************************************** */
  // "case" TypeList | "default"
  public static boolean TypeSwitchCase(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchCase")) return false;
    if (!nextTokenIs(builder_, "<type switch case>", CASE_KW, DEFAULT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_SWITCH_CASE, "<type switch case>");
    result_ = TypeSwitchCase_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "case" TypeList
  private static boolean TypeSwitchCase_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchCase_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CASE_KW);
    result_ = result_ && TypeList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ( identifier ":=" )? PrimaryExpr "." "(" "type" ")"
  public static boolean TypeSwitchGuard(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchGuard")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_SWITCH_GUARD, "<type switch guard>");
    result_ = TypeSwitchGuard_0(builder_, level_ + 1);
    result_ = result_ && PrimaryExpr(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, DOT, LPAREN, TYPE_KW, RPAREN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( identifier ":=" )?
  private static boolean TypeSwitchGuard_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchGuard_0")) return false;
    TypeSwitchGuard_0_0(builder_, level_ + 1);
    return true;
  }

  // identifier ":="
  private static boolean TypeSwitchGuard_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchGuard_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, IDENTIFIER, ASSIGN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "switch" ( SimpleStmtNoStruct ";" )? TypeSwitchGuard "{" TypeCaseClause* "}"
  public static boolean TypeSwitchStmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStmt")) return false;
    if (!nextTokenIs(builder_, SWITCH_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_SWITCH_STMT, null);
    result_ = consumeToken(builder_, SWITCH_KW);
    result_ = result_ && TypeSwitchStmt_1(builder_, level_ + 1);
    result_ = result_ && TypeSwitchGuard(builder_, level_ + 1);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LBRACE));
    result_ = pinned_ && report_error_(builder_, TypeSwitchStmt_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( SimpleStmtNoStruct ";" )?
  private static boolean TypeSwitchStmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStmt_1")) return false;
    TypeSwitchStmt_1_0(builder_, level_ + 1);
    return true;
  }

  // SimpleStmtNoStruct ";"
  private static boolean TypeSwitchStmt_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStmt_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = SimpleStmtNoStruct(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // TypeCaseClause*
  private static boolean TypeSwitchStmt_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStmt_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!TypeCaseClause(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeSwitchStmt_4", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // Type | UnderlyingType
  public static boolean TypeTerm(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeTerm")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_TERM, "<type term>");
    result_ = Type(builder_, level_ + 1);
    if (!result_) result_ = UnderlyingType(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // PrimaryExpr | unary_op UnaryExpr
  public static boolean UnaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnaryExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNARY_EXPR, "<unary expr>");
    result_ = PrimaryExpr(builder_, level_ + 1);
    if (!result_) result_ = UnaryExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // unary_op UnaryExpr
  private static boolean UnaryExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnaryExpr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unary_op(builder_, level_ + 1);
    result_ = result_ && UnaryExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // PrimaryExprNoStruct | unary_op UnaryExprNoStruct
  public static boolean UnaryExprNoStruct(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnaryExprNoStruct")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNARY_EXPR_NO_STRUCT, "<unary expr no struct>");
    result_ = PrimaryExprNoStruct(builder_, level_ + 1);
    if (!result_) result_ = UnaryExprNoStruct_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // unary_op UnaryExprNoStruct
  private static boolean UnaryExprNoStruct_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnaryExprNoStruct_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unary_op(builder_, level_ + 1);
    result_ = result_ && UnaryExprNoStruct(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "~" Type
  public static boolean UnderlyingType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnderlyingType")) return false;
    if (!nextTokenIs(builder_, TILDE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, TILDE);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, UNDERLYING_TYPE, result_);
    return result_;
  }

  /* ********************************************************** */
  // "var" ( VarSpec | "(" ( VarSpec ";" )* ")" )
  public static boolean VarDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDecl")) return false;
    if (!nextTokenIs(builder_, VAR_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VAR_DECL, null);
    result_ = consumeToken(builder_, VAR_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && VarDecl_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // VarSpec | "(" ( VarSpec ";" )* ")"
  private static boolean VarDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDecl_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = VarSpec(builder_, level_ + 1);
    if (!result_) result_ = VarDecl_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "(" ( VarSpec ";" )* ")"
  private static boolean VarDecl_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDecl_1_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && VarDecl_1_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( VarSpec ";" )*
  private static boolean VarDecl_1_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDecl_1_1_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!VarDecl_1_1_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "VarDecl_1_1_1", pos_)) break;
    }
    return true;
  }

  // VarSpec ";"
  private static boolean VarDecl_1_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDecl_1_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = VarSpec(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IdentifierList ( Type ( "=" ExpressionList )? | "=" ExpressionList )
  public static boolean VarSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = IdentifierList(builder_, level_ + 1);
    result_ = result_ && VarSpec_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, VAR_SPEC, result_);
    return result_;
  }

  // Type ( "=" ExpressionList )? | "=" ExpressionList
  private static boolean VarSpec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = VarSpec_1_0(builder_, level_ + 1);
    if (!result_) result_ = VarSpec_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Type ( "=" ExpressionList )?
  private static boolean VarSpec_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Type(builder_, level_ + 1);
    result_ = result_ && VarSpec_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( "=" ExpressionList )?
  private static boolean VarSpec_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1_0_1")) return false;
    VarSpec_1_0_1_0(builder_, level_ + 1);
    return true;
  }

  // "=" ExpressionList
  private static boolean VarSpec_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // "=" ExpressionList
  private static boolean VarSpec_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // "+" | "-" | "|" | "^"
  public static boolean add_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_op")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ADD_OP, "<add op>");
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, BIT_OR);
    if (!result_) result_ = consumeToken(builder_, BIT_XOR);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ( add_op | mul_op )? "="
  public static boolean assign_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assign_op")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ASSIGN_OP, "<assign op>");
    result_ = assign_op_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQ);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( add_op | mul_op )?
  private static boolean assign_op_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assign_op_0")) return false;
    assign_op_0_0(builder_, level_ + 1);
    return true;
  }

  // add_op | mul_op
  private static boolean assign_op_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assign_op_0_0")) return false;
    boolean result_;
    result_ = add_op(builder_, level_ + 1);
    if (!result_) result_ = mul_op(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // SourceFile
  static boolean goFile(PsiBuilder builder_, int level_) {
    return SourceFile(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // ImportDecl ( ";" )?
  static boolean import_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "import_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = ImportDecl(builder_, level_ + 1);
    result_ = result_ && import_item_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, GoParser::top_level_recover);
    return result_;
  }

  // ( ";" )?
  private static boolean import_item_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "import_item_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // "*" | "/" | "%" | "<<" | ">>" | "&" | "&^"
  public static boolean mul_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_op")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MUL_OP, "<mul op>");
    result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, QUOTIENT);
    if (!result_) result_ = consumeToken(builder_, REMAINDER);
    if (!result_) result_ = consumeToken(builder_, SHIFT_LEFT);
    if (!result_) result_ = consumeToken(builder_, SHIFT_RIGHT);
    if (!result_) result_ = consumeToken(builder_, BIT_AND);
    if (!result_) result_ = consumeToken(builder_, BIT_CLEAR);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // PackageClause ( ";" )?
  static boolean package_clause_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "package_clause_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = PackageClause(builder_, level_ + 1);
    result_ = result_ && package_clause_item_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, GoParser::top_level_recover);
    return result_;
  }

  // ( ";" )?
  private static boolean package_clause_item_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "package_clause_item_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // "==" | "!=" | "<" | "<=" | ">" | ">="
  public static boolean rel_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rel_op")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REL_OP, "<rel op>");
    result_ = consumeToken(builder_, EQEQ);
    if (!result_) result_ = consumeToken(builder_, NEQ);
    if (!result_) result_ = consumeToken(builder_, LESS);
    if (!result_) result_ = consumeToken(builder_, LESS_EQ);
    if (!result_) result_ = consumeToken(builder_, GREATER);
    if (!result_) result_ = consumeToken(builder_, GREATER_EQ);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // Statement
  static boolean statement_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = Statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, GoParser::statement_recover);
    return result_;
  }

  /* ********************************************************** */
  // !(";" | "}" | "case" | "default")
  static boolean statement_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !statement_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ";" | "}" | "case" | "default"
  private static boolean statement_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, CASE_KW);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    return result_;
  }

  /* ********************************************************** */
  // TopLevelDecl ( ";" )?
  static boolean top_level_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_level_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = TopLevelDecl(builder_, level_ + 1);
    result_ = result_ && top_level_item_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, GoParser::top_level_recover);
    return result_;
  }

  // ( ";" )?
  private static boolean top_level_item_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_level_item_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // !("package" | "import" | "func" | "var" | "const" | "type")
  static boolean top_level_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_level_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !top_level_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // "package" | "import" | "func" | "var" | "const" | "type"
  private static boolean top_level_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_level_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, PACKAGE_KW);
    if (!result_) result_ = consumeToken(builder_, IMPORT_KW);
    if (!result_) result_ = consumeToken(builder_, FUNC_KW);
    if (!result_) result_ = consumeToken(builder_, VAR_KW);
    if (!result_) result_ = consumeToken(builder_, CONST_KW);
    if (!result_) result_ = consumeToken(builder_, TYPE_KW);
    return result_;
  }

  /* ********************************************************** */
  // "+" | "-" | "!" | "^" | "*" | "&" | "<-"
  public static boolean unary_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_op")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNARY_OP, "<unary op>");
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, NOT);
    if (!result_) result_ = consumeToken(builder_, BIT_XOR);
    if (!result_) result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, BIT_AND);
    if (!result_) result_ = consumeToken(builder_, SEND_CHANNEL);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

}
