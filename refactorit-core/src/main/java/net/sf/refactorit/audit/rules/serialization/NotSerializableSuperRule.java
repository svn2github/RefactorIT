/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.serialization;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.conflicts.Conflict;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.AuditProfileUtils;
import net.sf.refactorit.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author Arseni Grigorjev
 *
 */
public class NotSerializableSuperRule extends AuditRule {
  public static final String NAME = "not_serializable_super";
  private boolean skip_with_constructor = true;
  
  public void init(){
    super.init();
    skip_with_constructor = AuditProfileUtils.getBooleanOption(
        getConfiguration(), "skip", "noarg_constr", skip_with_constructor);
  }
   
  public void visit(BinCIType citype) {
     // check if it is a class and it is serilizable
    if (citype.isClass() && analyzeInheritance(citype)){
      BinClass clas = (BinClass) citype;
      BinClass superclass
          = (BinClass) clas.getTypeRef().getSuperclass().getBinCIType();
      BinConstructor superConstructor = superclass.getDefaultConstructor();
      if (!skip_with_constructor && superConstructor != null
          && superConstructor.isAccessible(superclass, clas)){
        addViolation(new NotSerializableSuperWithConstr(clas));
      } else if (skip_with_constructor
          && superConstructor != null
          && superConstructor.isAccessible(superclass, clas)){
        // skip, do nothing
      } else {
        addViolation(new NotSerializableSuper(clas));
      }
    }

    super.visit(citype);
  }

  /**
   *  This method checks recursively, if a class is serializable (consider
   *  serializable class, which implements java.io.Serializable interface
   *  or extends an other serializable class.
   */
  private static boolean analyzeInheritance(BinCIType clas){
    if (clas == null){
      return false;
    }

    BinTypeRef serializableRef
        = clas.getProject().getTypeRefForName("java.io.Serializable");

    BinTypeRef[] interfaces = clas.getTypeRef().getInterfaces();
    boolean serializable = false;
    for (int i = 0; i < interfaces.length; i++){
      if (interfaces[i].equals(serializableRef)){
        serializable = true;
        break;
      }
    }

    if (serializable){
      BinTypeRef superclass = clas.getTypeRef().getSuperclass();
      if (superclass == null){
        return false;
      }
      if (superclass.equals(clas.getProject().getObjectRef())){
        return false;
      }

      return !SerializationUtils.isSerializable(superclass.getBinCIType());
    }

    return false;
  }
}

/**
 * Violation for case: serializable class extends non-serializable
 * super, which has NO proper constuctor, which could be used for
 * deserialization.
 */
class NotSerializableSuper extends SimpleViolation {

  public NotSerializableSuper(BinClass clas) {
    this(clas, "Serializable class '"+clas.getName()+"' extends " +
    "non-serializable class (superclass has NO accessable no-arg. " +
    "constructor!)");
  }

  protected NotSerializableSuper(BinClass clas, String message){
    super(clas.getTypeRef(), clas.getNameAstOrNull(), message,
        "refact.audit.not_serializable_super");
    setTargetItem(clas);
  }

  public BinMember getSpecificOwnerMember() {
    return (BinMember) getTargetItem();
  }

  public List getCorrectiveActions() {
    List actions = new ArrayList();
    actions.add(AddSerializableToSuperclass.instance);
    actions.add(AddDefaultConstructor.instance);
    return actions;
  }
}

/**
 * Violation for case: serializable class extends non-serializable
 * super, which has altough a proper accessable no-arg. constructor.
 */
class NotSerializableSuperWithConstr extends NotSerializableSuper {

  public NotSerializableSuperWithConstr(BinClass clas) {
    super(clas, "Serializable class '"+clas.getName()+"' extends " +
        "non-serializable class (superclass has accessable no-arg. " +
        "constructor)");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(AddSerializableToSuperclass.instance);
  }
}

/**
 * This corrective action adds 'implements java.io.Serializable'
 * to direct superclass.
 */
class AddDefaultConstructor extends MultiTargetCorrectiveAction {
  public static final AddDefaultConstructor instance
      = new AddDefaultConstructor();

  public String getKey() {
    return "refactorit.audit.action.not_serializable_super.add_constructor";
  }

  public String getName() {
    return "Create accessable no-arg. constructor for superclass";
  }

  public static String getMultitargetName() {
    return "Create accessable no-arg. constructor for superclass(es)";
  }

  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof NotSerializableSuper)
        || violation instanceof NotSerializableSuperWithConstr) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    BinClass clas
        = (BinClass) ((NotSerializableSuper) violation).getOwnerMember();

    BinClass superclass
        = (BinClass) clas.getTypeRef().getSuperclass().getBinCIType();
    CompilationUnit compilationUnit = superclass.getCompilationUnit();

    // try to get superclass default constructor
    BinConstructor superConstructor = superclass.getDefaultConstructor();
    if (superConstructor == null){
    // if there is no such constructor -> add

      // calculate indent
      String indentString = FormatSettings.getIndentString(clas.getIndent()
          + FormatSettings.getBlockIndent());

      // insert FIXME comment
      StringInserter inserter = new StringInserter(
          compilationUnit,
          superclass.getBodyAST().getStartLine(),
          superclass.getBodyAST().getStartColumn(),
          FormatSettings.LINEBREAK + indentString + "/**"
          + FormatSettings.LINEBREAK + indentString
          + " * FIXME: this constructor was generated by RIT (audit rule: Not "
          + "serializable superclass)."
          + FormatSettings.LINEBREAK + indentString
          + " * Some manual changes may be needed."
          + FormatSettings.LINEBREAK + indentString + " */"
          + FormatSettings.LINEBREAK + indentString
          + "public " + superclass.getName() + "(){"
          + FormatSettings.LINEBREAK + indentString
          + FormatSettings.LINEBREAK + indentString + "}"
      );
      manager.add(inserter);
    } else {

      ASTImpl constrDecl = superConstructor.getRootAst();
      ASTImpl constrName = superConstructor.getNameAstOrNull();

      // erase old access modifier
      StringEraser eraser = new StringEraser(compilationUnit,
          constrDecl.getStartLine(), constrDecl.getStartColumn() - 1,
          constrName.getEndLine(), constrName.getStartColumn() - 1);
      eraser.setRemoveLinesContainingOnlyComments(true);
      manager.add(eraser);

      // add modifier 'public'
      StringInserter inserter = new StringInserter(
        compilationUnit,
        constrDecl.getStartLine(),
        constrDecl.getStartColumn() - 1,
        "public ");
      manager.add(inserter);

    }

    return Collections.singleton(compilationUnit);
  }
}

/**
 * This corrective action adds 'implements java.io.Serializable'
 * to direct superclass.
 */
class AddSerializableToSuperclass extends MultiTargetCorrectiveAction {
  public static final AddSerializableToSuperclass instance
      = new AddSerializableToSuperclass();

  public String getKey() {
    return "refactorit.audit.action.not_serializable_super.add_serializable";
  }

  public String getName() {
    return "Make superclass java.io.Serializable";
  }

  public static String getMultitargetName() {
    return "Make superclass(es) java.io.Serializable";
  }

  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof NotSerializableSuper)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    // put transformations here
    TransformationList transList = new TransformationList();

    BinClass clas
        = (BinClass) ((NotSerializableSuper) violation).getOwnerMember();

    BinClass superclass
        = (BinClass) clas.getTypeRef().getSuperclass().getBinCIType();
    CompilationUnit compilationUnit = superclass.getCompilationUnit();

    // find last interface implemented or null
    BinTypeRef lastInterface = null;
    List typeDatas = superclass.getTypeRef().getBinCIType().getSpecificSuperTypeRefs();
    for (int i = 0, max = typeDatas.size(); i < max; i++) {
      BinTypeRef data = (BinTypeRef) typeDatas.get(i);
      if (!data.getTypeRef().getBinCIType().isClass()){
        lastInterface = data;
      }
    }

    // try to import java.io.Serializable, if not possible: use qualified name
    // when implementing Serialiable.
    boolean useQName = false;
    // import needed
    if (!ImportUtils.hasTypeImported(compilationUnit,
        "java.io.Serializable",
        superclass.getProject().getPackageForName("java.io"))){

      BinTypeRef typeToImport = superclass.getProject()
          .getTypeRefForName("java.io.Serializable");

     // ambiguous import, better use QualifiedName then
     if (ImportUtils.isAmbiguousImport(typeToImport.getBinCIType(),superclass)){
        useQName = true;
      } else {
        ImportManager importManager = new ImportManager();
        Conflict conflict = importManager
            .addExtraImport(superclass, typeToImport, superclass.getTypeRef());
        if (conflict == null){
          importManager.createEditors(transList);
        } else {
          useQName = true;
        }
      }
    }

    String strToInsert = "Serializable";
    if (useQName){
      strToInsert = "java.io.Serializable";
    }

    if (lastInterface != null){
      // case 1: there are already some implements defined
      int startLine = lastInterface.getNode().getEndLine();
      int startCol = lastInterface.getNode().getEndColumn()-1;

      StringInserter inserter = new StringInserter(
        compilationUnit,
        startLine,
        startCol,
        ", " + strToInsert + " ");
      transList.add(inserter);
    } else {
      // case 2: this is the first interface that is implemented
      int startLine = superclass.getNameAstOrNull().getEndLine();
      int startCol = superclass.getNameAstOrNull().getEndColumn()-1;

      StringInserter inserter = new StringInserter(
        compilationUnit,
        startLine,
        startCol,
        " implements " + strToInsert);
      transList.add(inserter);
    }


    // insert FIXME comment
    String indentString = FormatSettings.getIndentString(clas.getIndent()
        + FormatSettings.getBlockIndent());
    StringInserter inserter = new StringInserter(
        compilationUnit,
        superclass.getBodyAST().getStartLine(),
        superclass.getBodyAST().getStartColumn(),
        FormatSettings.LINEBREAK + indentString + "/**"
        + FormatSettings.LINEBREAK + indentString
        + " * FIXME: java.io.Serializable implemented by RefactorIT."
        + FormatSettings.LINEBREAK + indentString
        + " * Some manual changes may be needed."
        + FormatSettings.LINEBREAK + indentString + " */"
    );

    transList.add(inserter);
    manager.add(transList);

    return Collections.singleton(compilationUnit);
  }
}
