/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.source.BodyContext;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.format.BinItemFormatter;


/**
 * Defines enum constant.
 * @author Anton Safonov
 */
public final class BinEnumConstant extends BinField {

  public BinEnumConstant(String name, BinTypeRef b_owner, int b_modifiers,
      boolean isWithoutExpression) {
    super(name, b_owner, b_modifiers, isWithoutExpression);
  }

  protected void callBodyLoader(final BodyContext context)
      throws SourceParsingException {
    getProject().getProjectLoader().getMethodBodyLoader()
        .buildExpressionForEnumConstant(this, context);
  }

  public String getMemberType() {
    return memberType;
  }

  public static String getStaticMemberType() {
    return memberType;
  }

  public BinItemFormatter getFormatter() {
    return null; // JAVA5: new BinEnumConstantFormatter(this);
  }

  private static final String memberType = "enum constant";
}
