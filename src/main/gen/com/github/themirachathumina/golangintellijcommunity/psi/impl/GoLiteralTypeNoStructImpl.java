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

public class GoLiteralTypeNoStructImpl extends ASTWrapperPsiElement implements GoLiteralTypeNoStruct {

  public GoLiteralTypeNoStructImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitLiteralTypeNoStruct(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoArrayType getArrayType() {
    return findChildByClass(GoArrayType.class);
  }

  @Override
  @Nullable
  public GoElementType getElementType() {
    return findChildByClass(GoElementType.class);
  }

  @Override
  @Nullable
  public GoMapType getMapType() {
    return findChildByClass(GoMapType.class);
  }

  @Override
  @Nullable
  public GoSliceType getSliceType() {
    return findChildByClass(GoSliceType.class);
  }

  @Override
  @Nullable
  public GoStructType getStructType() {
    return findChildByClass(GoStructType.class);
  }

}
