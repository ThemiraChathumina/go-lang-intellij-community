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

public class GoIfStmtImpl extends ASTWrapperPsiElement implements GoIfStmt {

  public GoIfStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitIfStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<GoBlock> getBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, GoBlock.class);
  }

  @Override
  @Nullable
  public GoExpressionNoStruct getExpressionNoStruct() {
    return findChildByClass(GoExpressionNoStruct.class);
  }

  @Override
  @Nullable
  public GoIfStmt getIfStmt() {
    return findChildByClass(GoIfStmt.class);
  }

  @Override
  @Nullable
  public GoSimpleStmtNoStruct getSimpleStmtNoStruct() {
    return findChildByClass(GoSimpleStmtNoStruct.class);
  }

}
