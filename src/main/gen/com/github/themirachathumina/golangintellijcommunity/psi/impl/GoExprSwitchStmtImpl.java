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

public class GoExprSwitchStmtImpl extends ASTWrapperPsiElement implements GoExprSwitchStmt {

  public GoExprSwitchStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitExprSwitchStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<GoExprCaseClause> getExprCaseClauseList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, GoExprCaseClause.class);
  }

  @Override
  @Nullable
  public GoExpressionNoStruct getExpressionNoStruct() {
    return findChildByClass(GoExpressionNoStruct.class);
  }

  @Override
  @Nullable
  public GoSimpleStmtNoStruct getSimpleStmtNoStruct() {
    return findChildByClass(GoSimpleStmtNoStruct.class);
  }

}
