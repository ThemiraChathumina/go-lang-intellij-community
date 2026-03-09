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

public class GoTypeImpl extends ASTWrapperPsiElement implements GoType {

  public GoTypeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitType(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoType getType() {
    return findChildByClass(GoType.class);
  }

  @Override
  @Nullable
  public GoTypeArgs getTypeArgs() {
    return findChildByClass(GoTypeArgs.class);
  }

  @Override
  @Nullable
  public GoTypeLit getTypeLit() {
    return findChildByClass(GoTypeLit.class);
  }

  @Override
  @Nullable
  public GoTypeName getTypeName() {
    return findChildByClass(GoTypeName.class);
  }

}
