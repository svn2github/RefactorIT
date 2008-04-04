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
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.FormatSettings;

import java.util.ArrayList;
import java.util.List;


public class CreateAbstractDeclarationResolution extends ConflictResolution {

  private BinMethod method;
  
  /**
   * 
   */
  public CreateAbstractDeclarationResolution(BinMethod method) {
    super();
    this.method = method;
  }



  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution#getDescription()
   */
  public String getDescription() {
    return "Create abstract declaration of " + BinFormatter.format(method) +
    	" in native type";
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution#getEditors(net.sf.refactorit.refactorings.conflicts.ConflictResolver)
   */
  public Editor[] getEditors(ConflictResolver resolver) {
    List editors = new ArrayList(2);
    BinCIType nativeType = method.getOwner().getBinCIType();
    BinMethod abstractMethod = method.cloneSkeleton();
    abstractMethod.setModifiers(abstractMethod.getModifiers() | BinModifier.ABSTRACT);
    if (abstractMethod.isPrivate()) {
      abstractMethod.setModifiers(BinModifier.setFlags(abstractMethod.getModifiers(),
          BinModifier.PROTECTED));
      editors.add(new ModifierEditor(method,
          BinModifier.setFlags(method.getModifiers(),
              BinModifier.PROTECTED)));
    }
    String indent =
      FormatSettings.getIndentString(new BinTypeFormatter(nativeType).getMemberIndent());
    editors.add(new StringInserter(nativeType.getCompilationUnit(),
        nativeType.findNewMethodPosition(),
        FormatSettings.LINEBREAK + abstractMethod.getFormatter().print()));
    return (Editor[])editors.toArray(new Editor[0]);
    
  }

  public void runResolution(ConflictResolver resolver) {
    
    setIsResolved(true);

  }

  public String toString() {
    return "CreateAbstractDeclarationResolution";
  }

  public List getDownMembers() {
    return new ArrayList();
  }

}
