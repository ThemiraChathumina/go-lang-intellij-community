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

public class GoTopLevelDeclImpl extends ASTWrapperPsiElement implements GoTopLevelDecl {

  public GoTopLevelDeclImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitTopLevelDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoDeclaration getDeclaration() {
    return findChildByClass(GoDeclaration.class);
  }

  @Override
  @Nullable
  public GoFunctionDecl getFunctionDecl() {
    return findChildByClass(GoFunctionDecl.class);
  }

  @Override
  @Nullable
  public GoMethodDecl getMethodDecl() {
    return findChildByClass(GoMethodDecl.class);
  }

}
