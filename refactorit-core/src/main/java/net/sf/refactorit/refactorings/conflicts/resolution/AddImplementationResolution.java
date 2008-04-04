/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.conflicts.resolution;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.FormatSettings;

import java.util.List;


/**
 *
 * @author vadim
 */
public class AddImplementationResolution extends CreateDeclarationResolution {
  private List needImplementation;
  private BinMember upMember;
  private ConflictResolver resolver;
  private int implementAccess;

  public AddImplementationResolution(ConflictResolver resolver,
      BinMember upMember, List needImplementation,
      int implementAccess) {
    super(upMember);

    this.resolver = resolver;
    this.upMember = upMember;
    this.needImplementation = needImplementation;
    this.implementAccess = implementAccess;
  }

  public Editor[] getEditors(ConflictResolver resolver) {
    if (!isResolved()) {
      return new Editor[0];
    }

    Editor[] editors = new Editor[needImplementation.size()];

    for (int i = 0, max = needImplementation.size(); i < max; i++) {
      BinCIType type = (BinCIType) needImplementation.get(i);
      String indent =
          FormatSettings.getIndentString(new BinTypeFormatter(type).
          getMemberIndent());
      editors[i] = new StringInserter(type.getCompilationUnit(),
          type.findNewMethodPosition(),
          composeMethodDefinitionForImplementer(indent));
    }

    return editors;
  }

  private String composeMethodDefinitionForImplementer(String indent) {
    BinMethod method = (BinMethod) upMember;
    StringBuffer result = new StringBuffer(FormatSettings.LINEBREAK + indent);

    BinModifierFormatter modifierFormatter = new BinModifierFormatter(implementAccess);
    modifierFormatter.needsPostfix(true);
    result.append(modifierFormatter.print());
    
    result.append(composeMethodSignature(method, "", false) + " {"
        + FormatSettings.LINEBREAK);
    result.append(indent);
    result.append(indent);
    result.append("throw new RuntimeException(\"method " + method.getName() +
        " is not implemented\");" + FormatSettings.LINEBREAK);
    result.append(indent);
    result.append("}").append(FormatSettings.LINEBREAK);

    return result.toString();
  }

  public String getDescription() {
    return "Add implementation of " + BinFormatter.format(upMember) +
        " into the following types";
  }

  public void runResolution(ConflictResolver resolver) {
    setIsResolved(true);
  }

  public List getNeedImplementation() {
    return needImplementation;
  }

  public List getDownMembers() {
    return getNeedImplementation();
  }
}
