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
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Arseni Grigorjev
 * 
 * This rule audits two things: 
 *    1) if a serializable class has a serialVersionUID member field declared
 *    2) if it is declared - is it declared properly? (should be final,
 *        static and long type)
 *
 */
public class SerialVersionUIDRule extends AuditRule {
  public static final String NAME = "serial_version_uid";

  public void visit(BinCIType citype) {
    // check if it is a class and it is serilizable
    // HACK, FIXME: !citype.getOwner().getBinCIType().isEnum() -- to avoid
    // violating on synthetic anonymous classes, which initialize enum constant
    // fields; MAYBE should not traverse here at all??
    if (citype.isClass() && (citype.getOwner() == null || !citype.getOwner()
        .getBinCIType().isEnum()) && SerializationUtils.isSerializable(citype)){
      BinClass clas = (BinClass) citype;
      
      // get list of declared fields in that class
      BinField[] fields = clas.getDeclaredFields();
      
      // if there is no field with name serialVersionUID - add violation
      boolean foundUID = false;
      for (int i = 0; i < fields.length; i++){
        if (fields[i].getName().equals("serialVersionUID")){
          foundUID = true;
          
          // if it has no proper modifiers - add violation
          BinPrimitiveType type = getPrimitiveType(fields[i]);
          ArrayList errs = new ArrayList();
          if (!fields[i].isStatic()){
            errs.add("static");
          }
          if (!fields[i].isFinal()){
            errs.add("final");
          }
          if (type != BinPrimitiveType.LONG){
            errs.add("as long type");
          }
          
          if (errs.size() > 0){
            String errMsg = "serialVersionUID is not decalred ";
            for (int k = 0; k < errs.size(); k++){
              if (k == errs.size()-1 && k != 0){
                errMsg += " and ";
              } else if (k != 0) {
                errMsg += ", ";
              }
              errMsg += (String) errs.get(k);
            }
            
            addViolation(new BadSerialVersionUID(fields[i], errMsg));
          }
          break;
        }  
      }
      if (!foundUID){
        addViolation(new NoSerialVersionUID(clas));
      }
    }
    
    super.visit(citype);
  }
    
  /**
   * Returns primitive return type of given field.
   * if field type not primitive returns null.
   * if given null field - returns null.
   */
  private static BinPrimitiveType getPrimitiveType(BinField field){
    try{
      BinType btype;
      btype = field.getTypeRef().getBinType();
            
      if (btype instanceof BinPrimitiveType){
        //System.out.println("ARS3>> " + btype.getQualifiedName());
        return (BinPrimitiveType) btype;
      } else {
        return null;
      }
    } catch(NullPointerException e) {
      return null;
    }
  }
}

/**
 *  Violation for no serialVersionUID member field
 */
class NoSerialVersionUID extends AwkwardMember {
  public NoSerialVersionUID(BinClass clas) {
    super(clas, "Serializable class " + clas.getName() 
        + " has no serialVersionUID member.", "refact.audit.serial_version_uid");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(AddVersionUIDAction.instance);
  }
}

/**
 *  Violation for serialVersionUID member field with no proper modifiers
 */
class BadSerialVersionUID extends AwkwardMember {

  public BadSerialVersionUID(BinField vers, String errMsg) {
    super(vers, errMsg, "refact.audit.serial_version_uid");
  }
  
  public List getCorrectiveActions() {
    return Collections.singletonList(CorrectVersionUIDAction.instance);
  }
}

/**
 *  Adds 'protected final static long serialVersionUID = <curdateandtime>L;'
 *  line to the start line of class body.
 */
class AddVersionUIDAction extends MultiTargetCorrectiveAction {
  public static final AddVersionUIDAction instance = new AddVersionUIDAction();
    
  public String getKey() {
    return "refactorit.audit.action.serial_version_uid.add";
  }
  
  public String getName() {
    return "Add serialVersionUID member field";
  }
  
  public String getMultiTargetName() {
    return "Add serialVersionUID member field(s)";
  }
    
  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation){
    if (!(violation instanceof NoSerialVersionUID)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    BinClass clas = (BinClass) violation.getOwnerMember();
    CompilationUnit compilationUnit = violation.getCompilationUnit();
    
    String indentString = FormatSettings.getIndentString(clas.getIndent() 
        + FormatSettings.getBlockIndent());
    StringInserter inserter = new StringInserter(
        compilationUnit, 
        clas.getBodyAST().getStartLine(), 
        clas.getBodyAST().getStartColumn(),
        FormatSettings.LINEBREAK + indentString + "/**" 
        + FormatSettings.LINEBREAK + indentString 
        + " * FIXME: serialVersionUID field auto-generated by RefactorIT" 
        + FormatSettings.LINEBREAK + indentString + " */" 
        + FormatSettings.LINEBREAK + indentString
        + "protected static final long serialVersionUID = " 
        + getUIDString() + ";");
    manager.add(inserter);
    
    return Collections.singleton(compilationUnit);
  } 
  
  private String getUIDString(){
    if (isTestRun()){
      return "1234567890L";
    }
    Calendar calendar = Calendar.getInstance();
    String UID = "" + calendar.get(Calendar.YEAR);
    int tmp = calendar.get(Calendar.MONTH);
    UID += ((""+tmp).length() > 1) ? "" + tmp : "0" + tmp;
    tmp = calendar.get(Calendar.DATE);
    UID += ((""+tmp).length() > 1) ? "" + tmp : "0" + tmp;
    tmp = calendar.get(Calendar.HOUR_OF_DAY);
    UID += ((""+tmp).length() > 1) ? "" + tmp : "0" + tmp;
    tmp = calendar.get(Calendar.MINUTE);
    UID += ((""+tmp).length() > 1) ? "" + tmp : "0" + tmp;
        
    return UID + "L";
  }
}

/**
 *  Removes all existing modifiers and adds correct modifiers (remembers
 *  original access modifier)
 */
class CorrectVersionUIDAction extends MultiTargetCorrectiveAction{
  public static final CorrectVersionUIDAction instance 
      = new CorrectVersionUIDAction();
    
  public String getKey() {
    return "refactorit.audit.action.serial_version_uid.correct";
  }
  
  public String getName() {
    return "Correct serialVersionUID modifiers";
  }
    
  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation){
    if (!(violation instanceof BadSerialVersionUID)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
        
    CompilationUnit compilationUnit = violation.getCompilationUnit();
    BinField field 
        = (BinField) violation.getOwnerMember();
    ASTImpl fieldDecl = field.getRootAst();
    ASTImpl fieldName = field.getNameAstOrNull();
        
    // erase all old modifiers
    StringEraser eraser = new StringEraser(compilationUnit,
        fieldDecl.getStartLine(), fieldDecl.getStartColumn() - 1,
        fieldName.getEndLine(), fieldName.getStartColumn() - 1);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);
        
    // insert new modifiers considering original access modifiers
    String correctLine = "";
    if (field.isPrivate()){
      correctLine += "private";
    } else if (field.isPublic()){
      correctLine += "public";
    } else if (field.isProtected()){
      correctLine += "protected";
    }
    StringInserter inserter = new StringInserter(
        compilationUnit, 
        fieldDecl.getStartLine(), 
        fieldDecl.getStartColumn() - 1,
        correctLine + " static final long ");
    manager.add(inserter);
    
    return Collections.singleton(compilationUnit);
  } 
}
