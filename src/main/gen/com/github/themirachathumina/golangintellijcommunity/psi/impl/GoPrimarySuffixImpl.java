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

public class GoPrimarySuffixImpl extends ASTWrapperPsiElement implements GoPrimarySuffix {

  public GoPrimarySuffixImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitPrimarySuffix(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoArguments getArguments() {
    return findChildByClass(GoArguments.class);
  }

  @Override
  @Nullable
  public GoIndex getIndex() {
    return findChildByClass(GoIndex.class);
  }

  @Override
  @Nullable
  public GoSelector getSelector() {
    return findChildByClass(GoSelector.class);
  }

  @Override
  @Nullable
  public GoSlice getSlice() {
    return findChildByClass(GoSlice.class);
  }

  @Override
  @Nullable
  public GoTypeAssertion getTypeAssertion() {
    return findChildByClass(GoTypeAssertion.class);
  }

}
