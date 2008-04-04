/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
 package net.sf.refactorit.audit;


import net.sf.refactorit.audit.AuditRule.Priority;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.apache.log4j.Logger;

import java.util.List;



/**
 * Base class for an auditing rule violation.
 *
 * Because of speed RuleViolations are not all recreated on rebuilds,
 * therefore they should hold all references to BinItems and CompilationUnits
 * via BinItemReferences.
 *
 * @author Sander Magi
 * @author Igor Malinin
 */
public abstract class RuleViolation implements Comparable{
  public static final Logger log = AppRegistry.getLogger(RuleViolation.class);
  private static MultiValueMap usedTags = new MultiValueMap();
  
  private final BinItemReference projectReference;
  private final BinItemReference binCITypeRef;
  private BinItemReference targetItemReference = null;
  //private Priority priority;
  private BinMember skipedOn = null;
  private BinTreeTableNode selectionNode = null;

  private float density;

  private AuditRule auditRule;
  
  /**
   * For corrective actions grouping in popup of AuditTreeTable. First level of
   * grouping is audit branch, second level is audit name.
   */
//  private String auditName;
//  private String categoryName;

  public RuleViolation(final BinTypeRef binCITypeRef) {
    this.projectReference = binCITypeRef.getProject().createReference();
    this.binCITypeRef = binCITypeRef.createReference();
  }

  public CompilationUnit getCompilationUnit() {
    final BinTypeRef binTypeRef = getBinTypeRef();
    if (binTypeRef == null){
      return null;
    }
    return binTypeRef.getBinCIType().getCompilationUnit();
  }

  public BinTypeRef getBinTypeRef() {
    final BinTypeRef result = (BinTypeRef) binCITypeRef.restore(getProject());
    if (result == null){
      log.warn("Could not get BinTypeRef for RuleViolation!");
    }
    return result;
  }

  /**
   * @return project instance restored from BinItemReference
   */
  
  public int compareTo(Object o){
  	return this.getAst().compareTo(((RuleViolation)o).getAst());
  	
  }
  
  public Project getProject() {
    return (Project) projectReference.restore(null);
  }

  public int getLine() {
    try {
      return getAst().getLine();
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e,
          "Failed to get line for: " + this, this);

      return 0;
    }
  }

  public Priority getPriority() {
    return auditRule.getPriority();
  }

  public float getDensity() {
    return density;
  }

  public void setDensity(float density) {
    this.density = density;
  }

  public String toString() {
    return "RuleViolation: " + getMessage() + " - " + auditRule + " - " + binCITypeRef ;
  }

  public BinMember getOwnerMember(){
    BinMember result = null;
    try {
      result = getSpecificOwnerMember();
    } catch (Exception e){
      log.warn("Unexpected exception during RuleViolation.getOwnerMember()" 
          + " invocation! Returning null", e);
      result = null;
    }
    return result;
  }
  
  public abstract BinMember getSpecificOwnerMember();

  public abstract ASTImpl getAst();

  public abstract String getMessage();

  public abstract String getTypeShortName();

  // TODO CorrectiveAction
  public abstract List getCorrectiveActions();

  public abstract String getHelpTopicId();

  public BinMember getSkipedOn(){
    return this.skipedOn;
  }

  public boolean isSkipped() {
    boolean hidden = false;

    String option = getTypeShortName();
    BinMember owner = getOwnerMember();
    while (owner != null) {
      JavadocComment comment = Comment.findJavadocFor(owner);
      if (comment != null && SkipTagHelper.isSkipped(comment.getText(), option)) {
        hidden = true;
        this.skipedOn = owner;
        usedTags.put(comment,getAuditRule().getKey());
        break;
      }
      owner = owner.getParentMember();
    }
    if (!hidden){
      // HACK: the getParentMember() method doesn`t return BinCIType as
      // parent member, so we will go through getOwner()`s too.
      owner = getOwnerMember();
      if (owner != null){
        BinTypeRef citype = owner.getOwner();
        while (citype != null){
          JavadocComment comment = Comment.findJavadocFor(citype.getBinCIType());
          if (comment != null && SkipTagHelper.isSkipped(comment.getText(), option)) {
            hidden = true;
            this.skipedOn = citype.getBinCIType();
            usedTags.put(comment,getAuditRule().getKey());
            break;
          }
          citype = citype.getBinCIType().getOwner();
        }
      }
    }
    return hidden;
  }

  public BinTreeTableNode getSelectionNode() {
    return this.selectionNode;
  }

  public void setSelectionNode(final BinTreeTableNode selectionNode) {
    this.selectionNode = selectionNode;
  }
  
  public String getAuditName() {
    return auditRule.getAuditName();
  }
  
  public String getCategoryName() {
    return auditRule.getCategoryName();
  }

  public void setAuditRule(AuditRule rule) {
    this.auditRule = rule;
  }

  public AuditRule getAuditRule() {
    return auditRule;
  }
  
  public static MultiValueMap getUsedTags() {
    // HACK: let`s rely on garbage collector
    MultiValueMap map = usedTags;
    usedTags = new MultiValueMap();
    return map;
  }

  public BinItemReference getTargetItemReference(){
    return this.targetItemReference;
  }
  
  public void setTargetItemReference(BinItemReference reference){
    this.targetItemReference = reference;
  }

  public int hashCode() {
    CompilationUnit compilationUnit = getCompilationUnit();
    if (compilationUnit == null){
      return super.hashCode();
    }
    return compilationUnit.getSource().hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof RuleViolation){
      return equals((RuleViolation) obj);
    }
    return false;
  }
  
  public boolean equals(RuleViolation rule){
    if (!getClass().equals(rule.getClass())){
      return false;
    }
    Object targetItem = getTargetItem();
    Object ruleTargetItem = rule.getTargetItem();
    return targetItem != null && targetItem.equals(ruleTargetItem);
  }

  public Object getTargetItem() {
    BinItemReference targetItemReference = getTargetItemReference();
    return (targetItemReference != null) ? targetItemReference.restore(
        getProject()) : null;
  }
  
  public void setTargetItem(Object targetItem){
    setTargetItemReference(BinItemReference.create(targetItem));
  }
}
