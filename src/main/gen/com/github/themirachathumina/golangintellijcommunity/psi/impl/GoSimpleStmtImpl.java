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

public class GoSimpleStmtImpl extends ASTWrapperPsiElement implements GoSimpleStmt {

  public GoSimpleStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitSimpleStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoAssignment getAssignment() {
    return findChildByClass(GoAssignment.class);
  }

  @Override
  @Nullable
  public GoExpressionStmt getExpressionStmt() {
    return findChildByClass(GoExpressionStmt.class);
  }

  @Override
  @Nullable
  public GoIncDecStmt getIncDecStmt() {
    return findChildByClass(GoIncDecStmt.class);
  }

  @Override
  @Nullable
  public GoSendStmt getSendStmt() {
    return findChildByClass(GoSendStmt.class);
  }

  @Override
  @Nullable
  public GoShortVarDecl getShortVarDecl() {
    return findChildByClass(GoShortVarDecl.class);
  }

}
