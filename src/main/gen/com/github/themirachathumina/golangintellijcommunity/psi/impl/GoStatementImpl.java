// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.themirachathumina.golangintellijcommunity.psi.GoTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.github.themirachathumina.golangintellijcommunity.psi.*;

public class GoStatementImpl extends ASTWrapperPsiElement implements GoStatement {

  public GoStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoBlock getBlock() {
    return findChildByClass(GoBlock.class);
  }

  @Override
  @Nullable
  public GoBreakStmt getBreakStmt() {
    return findChildByClass(GoBreakStmt.class);
  }

  @Override
  @Nullable
  public GoContinueStmt getContinueStmt() {
    return findChildByClass(GoContinueStmt.class);
  }

  @Override
  @Nullable
  public GoDeclaration getDeclaration() {
    return findChildByClass(GoDeclaration.class);
  }

  @Override
  @Nullable
  public GoDeferStmt getDeferStmt() {
    return findChildByClass(GoDeferStmt.class);
  }

  @Override
  @Nullable
  public GoFallthroughStmt getFallthroughStmt() {
    return findChildByClass(GoFallthroughStmt.class);
  }

  @Override
  @Nullable
  public GoForStmt getForStmt() {
    return findChildByClass(GoForStmt.class);
  }

  @Override
  @Nullable
  public GoGoStmt getGoStmt() {
    return findChildByClass(GoGoStmt.class);
  }

  @Override
  @Nullable
  public GoGotoStmt getGotoStmt() {
    return findChildByClass(GoGotoStmt.class);
  }

  @Override
  @Nullable
  public GoIfStmt getIfStmt() {
    return findChildByClass(GoIfStmt.class);
  }

  @Override
  @Nullable
  public GoLabeledStmt getLabeledStmt() {
    return findChildByClass(GoLabeledStmt.class);
  }

  @Override
  @Nullable
  public GoReturnStmt getReturnStmt() {
    return findChildByClass(GoReturnStmt.class);
  }

  @Override
  @Nullable
  public GoSelectStmt getSelectStmt() {
    return findChildByClass(GoSelectStmt.class);
  }

  @Override
  @Nullable
  public GoSimpleStmt getSimpleStmt() {
    return findChildByClass(GoSimpleStmt.class);
  }

  @Override
  @Nullable
  public GoSwitchStmt getSwitchStmt() {
    return findChildByClass(GoSwitchStmt.class);
  }

}
