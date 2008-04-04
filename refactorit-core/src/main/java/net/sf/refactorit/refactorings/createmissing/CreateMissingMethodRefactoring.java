/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.createmissing;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.conflicts.ImportNotPossibleConflict;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinMethodFormatter;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Anton Safonov
 */
public class CreateMissingMethodRefactoring extends AbstractRefactoring {
  public static String key = "refactoring.createmissingmethod";

  private final CreateMethodContext[] nodes;
  private boolean exceptionExists;
  public CreateMissingMethodRefactoring(
      final RefactorItContext context, final CreateMethodContext[] nodes
  ) {
    super("Create Missing Method", context);
    exceptionExists = getProject().
      getTypeRefForName("java.lang.UnsupportedOperationException") != null;
    this.nodes = nodes;
  }

  public RefactoringStatus checkPreconditions() {
    return new RefactoringStatus();
  }

  public RefactoringStatus checkUserInput() {
    return new RefactoringStatus();
  }

  public TransformationList performChange() {
    final TransformationList transList = new TransformationList();
    final ImportManager importManager = new ImportManager();

    for (int i = 0; i < nodes.length; i++) {
      final CreateMethodContext node = nodes[i];
      if (!node.isSelected()) {
        continue;
      }

      addNewMethod(getContext(), transList, node, importManager);
    }

    importManager.createEditors(transList);

    return transList;
  }

  /**
   * Adds info about the needed imports for the new method to the ImportManager.
   *
   * @param method the new method
   * @param importManager import manager
   * @return a <code>Set</code> of typeRefs that can not be imported and for which
   * fully qualified name should be used
   **/
  private Set manageImports(final BinMethod method,
      final ImportManager importManager) {
    List conflicts = importManager.addImportsForMember(method, method.getOwner(), true);
    Set fqnImports = new HashSet();
    if ((conflicts != null) && (conflicts.size() > 0)) {
      for (Iterator i = conflicts.iterator(); i.hasNext(); ) {
        fqnImports.add(((ImportNotPossibleConflict) i.next()).getTypeToImport());
      }
    }

    return fqnImports;
  }

  /**
   * Adds editor for creating the method from the given node to the given editor.
   *
   * @param context context
   * @param srcEditor source editor
   * @param node node
   * @param importManager import manager
   */
  private void addNewMethod(final RefactorItContext context,
      final TransformationList transList, final CreateMethodContext node,
      final ImportManager importManager) {
    
    try {
      BinArrayType.ArrayType returnType
          = BinArrayType.extractArrayTypeFromString(node.getReturnType());
      BinTypeRef returnTypeRef = node.resolve(returnType.type);
      if (returnType.dimensions != 0) {
        returnTypeRef = context.getProject()
            .createArrayTypeForType(returnTypeRef, returnType.dimensions);
      }
      BinParameter[] params = new BinParameter[node.argumentTypes.length];
      for (int j = 0; j < node.argumentTypes.length; j++) {
        params[j] = new BinParameter(node.argumentNames[j],
            node.argumentTypes[j], 0);
      }
      BinMethod newMethod = new BinMethod(node.getMethodName(),
          params,
          returnTypeRef,
          node.getVisibility() | (BinModifier.STATIC * (node.isStaticMethod()
          ? 1 : 0)),
          BinMethod.Throws.NO_THROWS);
      final BinTypeRef baseClass = node.getBaseClass();
      newMethod.setOwner(baseClass);

      // set abstract modifyer, if it is interface method
      if(baseClass.getBinCIType().isInterface()) {
        newMethod.setModifiers(BinModifier.setFlags(newMethod.getModifiers(),
            BinModifier.ABSTRACT));
      }

      Set fqnTypes = manageImports(newMethod, importManager);

      BinMethodFormatter formatter = createMissingMethodFormatter(newMethod,
          baseClass);
      formatter.setFqnTypes(fqnTypes);

      StringBuffer buffer = new StringBuffer();
      buffer.append(FormatSettings.LINEBREAK);
      buffer.append(formatter.formHeader());
      buffer.append(formatter.formBody());
      buffer.append(formatter.formFooter());

      int line = node.getBaseClass().getBinCIType().getEndLine();

      StringInserter inserter =
          new StringInserter(node.getBaseClass().getBinCIType().getCompilationUnit(),
          line,
          0,
          buffer.toString());
      transList.add(inserter);

    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }
  }

  /**
   * @param newMethod new method
   * @param baseClass base class
   * @return formatter
   */
  private BinMethodFormatter createMissingMethodFormatter(final BinMethod
      newMethod, final BinTypeRef baseClass) {
    BinMethodFormatter formatter = new BinMethodFormatter(newMethod) {

      public String formBody() {
        if (baseClass.getBinCIType().isInterface()) {
          return "";
        } else {
          int baseIndent = new BinTypeFormatter(baseClass.getBinCIType()).
              getMemberIndent();
          String methodBody = FormatSettings.getIndentString(
              baseIndent + FormatSettings.getBlockIndent())
              + "//FIXME: implement this"
              + FormatSettings.LINEBREAK;
          if(exceptionExists) { 
            methodBody += 
              (FormatSettings.getIndentString(
              baseIndent + FormatSettings.getBlockIndent())
              + "throw new java.lang.UnsupportedOperationException(\"Method "
              + newMethod.getName() + "() not implemented\");"
              + FormatSettings.LINEBREAK);
          }
          return methodBody;
        }
      }
    };

    return formatter;
  }

  public String getDescription() {
    String out = "Create missing method";
    for (int i = 0; i < nodes.length; i++) {
      out += " " + nodes[i].getMethodName() + "(),";
    }

    out = out.substring(0, out.length()-1);

    out += " in " + nodes[0].getBaseClass().getName() + ".";


    return out;//super.getDescription();
  }

  public String getKey() {
    return key;
  }

}
