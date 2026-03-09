// This is a generated file. Not intended for manual editing.
package com.github.themirachathumina.golangintellijcommunity.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import static com.github.themirachathumina.golangintellijcommunity.psi.GoTypes.*;
import com.github.themirachathumina.golangintellijcommunity.highlight.GoStringLiteralMixin;
import com.github.themirachathumina.golangintellijcommunity.psi.*;

public class GoStringLiteralImpl extends GoStringLiteralMixin implements GoStringLiteral {

  public GoStringLiteralImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitStringLiteral(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getStringLit() {
    return findNotNullChildByType(STRING_LIT);
  }

}
