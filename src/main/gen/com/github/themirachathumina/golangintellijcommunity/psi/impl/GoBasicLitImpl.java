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

public class GoBasicLitImpl extends ASTWrapperPsiElement implements GoBasicLit {

  public GoBasicLitImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitBasicLit(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoStringLiteral getStringLiteral() {
    return findChildByClass(GoStringLiteral.class);
  }

  @Override
  @Nullable
  public PsiElement getFloatLit() {
    return findChildByType(FLOAT_LIT);
  }

  @Override
  @Nullable
  public PsiElement getImaginaryLit() {
    return findChildByType(IMAGINARY_LIT);
  }

  @Override
  @Nullable
  public PsiElement getIntLit() {
    return findChildByType(INT_LIT);
  }

  @Override
  @Nullable
  public PsiElement getRuneLit() {
    return findChildByType(RUNE_LIT);
  }

}
