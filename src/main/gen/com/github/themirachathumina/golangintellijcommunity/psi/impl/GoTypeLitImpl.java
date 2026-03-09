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

public class GoTypeLitImpl extends ASTWrapperPsiElement implements GoTypeLit {

  public GoTypeLitImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitTypeLit(this);
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
  public GoChannelType getChannelType() {
    return findChildByClass(GoChannelType.class);
  }

  @Override
  @Nullable
  public GoFunctionType getFunctionType() {
    return findChildByClass(GoFunctionType.class);
  }

  @Override
  @Nullable
  public GoInterfaceType getInterfaceType() {
    return findChildByClass(GoInterfaceType.class);
  }

  @Override
  @Nullable
  public GoMapType getMapType() {
    return findChildByClass(GoMapType.class);
  }

  @Override
  @Nullable
  public GoPointerType getPointerType() {
    return findChildByClass(GoPointerType.class);
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
