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

public class GoSourceFileImpl extends ASTWrapperPsiElement implements GoSourceFile {

  public GoSourceFileImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoVisitor visitor) {
    visitor.visitSourceFile(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<GoImportDecl> getImportDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, GoImportDecl.class);
  }

  @Override
  @NotNull
  public GoPackageClause getPackageClause() {
    return findNotNullChildByClass(GoPackageClause.class);
  }

  @Override
  @NotNull
  public List<GoTopLevelDecl> getTopLevelDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, GoTopLevelDecl.class);
  }

}
