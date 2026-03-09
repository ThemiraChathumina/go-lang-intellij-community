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

public class GoUnaryExprNoStructImpl extends ASTWrapperPsiElement implements GoUnaryExprNoStruct {

  public GoUnaryExprNoStructImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitUnaryExprNoStruct(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoPrimaryExprNoStruct getPrimaryExprNoStruct() {
    return findChildByClass(GoPrimaryExprNoStruct.class);
  }

  @Override
  @Nullable
  public GoUnaryExprNoStruct getUnaryExprNoStruct() {
    return findChildByClass(GoUnaryExprNoStruct.class);
  }

  @Override
  @Nullable
  public GoUnaryOp getUnaryOp() {
    return findChildByClass(GoUnaryOp.class);
  }

}
