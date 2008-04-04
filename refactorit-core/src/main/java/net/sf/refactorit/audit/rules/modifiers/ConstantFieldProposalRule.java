/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.modifiers;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.AwkwardMemberModifiers;
import net.sf.refactorit.audit.MultiTargetGroupingAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.rules.misc.FieldWriteAccessFinder;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.MultiFieldRenameDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.AuditProfileUtils;
import net.sf.refactorit.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Arseni Grigorjev
 */
public class ConstantFieldProposalRule extends AuditRule {
  public static final String NAME = "constant_field";
  public static final int a = 5;
  public static final float b = 0.5F;
  private boolean upperCaseConsts = true;

  private FieldWriteAccessFinder finder;

  public ConstantFieldProposalRule() {
    this.finder = new FieldWriteAccessFinder();
  }

  public void init() {
    // for the first run get the options from profile
    this.upperCaseConsts = AuditProfileUtils.getBooleanOption(
        getConfiguration(), "skip", "upper_case_names", true);
    super.init();
  }

  public void visit(BinField field) {
    if (field.getExpression() != null
        && field.getExpression() instanceof BinLiteralExpression
        && (!field.isFinal() || !field.isStatic())
        && !finder.checkWriteAccess(field)) {

      // if the field is never used on write and is static and final
      // suggest making it static-final constant
      addViolation(new ConstantFieldProposal(field, upperCaseConsts));
    } else if (upperCaseConsts && field.getExpression() != null
        && field.isFinal() && field.isStatic()
        && !field.getName().equals(field.getName().toUpperCase())
        && !("serialVersionUID".equals(field.getName()) &&
            field.getTypeRef().equals(BinPrimitiveType.LONG.getTypeRef()) &&
            SerializationUtils.isSerializable(field.getParentType()))) {
      // if the option for name check is on, and static final constant has
      // a lower-case name -- suggest changing the name
      addViolation(new LowercaseConstant(field));
    }

    super.visit(field);
  }
}

/**
 * Violation: field is never rewritten and is not 'static final'
 */

class ConstantFieldProposal extends AwkwardMemberModifiers {

  public boolean rename = false;

  public ConstantFieldProposal(BinField field, boolean rename) {
    super(field, "Field is never re-written: can be static final", "refact.audit.constant_field");

    this.rename = rename;
  }

  public final BinField getField() {
    return (BinField) getTargetItem();
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(MakeSFConstAction.instance);
  }

  public final BinMember getSpecificOwnerMember(){
    return getField().getOwner().getBinCIType();
  }
}

/**
 * Violation: the field is static final, but is not properly named
 */

class LowercaseConstant extends AwkwardMember {

  public LowercaseConstant(BinField field) {
    super(field, "Constant field`s name should be uppercase",
        "refact.audit.constant_field");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(FieldNameToUppercaseAction.instance);
  }

  public final BinField getField() {
    return (BinField) getTargetItem();
  }

  public BinMember getSpecificOwnerMember() {
    return getField().getOwner().getBinCIType();
  }
}

/**
 * Action adds 'static' and 'final' modifiers to the field and properly renames
 * it if user has choosen specified option when running audit rule.
 */

class MakeSFConstAction extends MultiTargetGroupingAction {
  public static final MakeSFConstAction instance = new MakeSFConstAction();

  public String getKey() {
    return "refactorit.audit.action.make_sf_constant";
  }

  public String getName() {
    return "Make field a 'static final' constant";
  }

  public Set run(TransformationManager manager, TreeRefactorItContext context, 
      List violations) {
    Set sources = new HashSet(violations.size() * 2);
    RuleViolation sampleViolation = null;
    List fields = new ArrayList();

    for (Iterator it = violations.iterator(); it.hasNext();) {
      RuleViolation violation = (RuleViolation) it.next();
      if (violation instanceof ConstantFieldProposal) {
        if (sampleViolation == null) {
          sampleViolation = violation;
        }
        BinField field = ((ConstantFieldProposal) violation).getField();
        fields.add(field);
        sources.add(field.getCompilationUnit());
      }
    }
    
    boolean performChanges = true;
    // if audit was executed with "uppercase constants" option set
    if (sampleViolation instanceof ConstantFieldProposal
        && ((ConstantFieldProposal) sampleViolation).rename) {
      MultiFieldRenameDialog dialog = new MultiFieldRenameDialog(fields,
          context);
      dialog.show();
      performChanges = dialog.isOkPressed();
      if (performChanges) {
        manager.add(dialog.getRenameTransformations());
      }
    }

    // if process is not interrupted by cancel click on rename dialog
    if (performChanges) {
      for (int i = 0; i < fields.size(); i++) {
        final BinField field = (BinField) fields.get(i);
        int modifiers = field.getModifiers();
        modifiers = BinModifier.setFlags(modifiers, BinModifier.FINAL);
        modifiers = BinModifier.setFlags(modifiers, BinModifier.STATIC);
        manager.add(new ModifierEditor(field, modifiers));
      }
      return sources;
    }
    fields.clear();
    return Collections.EMPTY_SET;
  }
}

/**
 * Action renames static final fields properly from 'myField'-style to
 * 'MY_FIELD'-style.
 */

class FieldNameToUppercaseAction extends MultiTargetGroupingAction {
  public static final FieldNameToUppercaseAction instance = new FieldNameToUppercaseAction();

  public String getKey() {
    return "refactorit.audit.action.constant_field.to_uppercase";
  }

  public String getName() {
    return "Convert field`s name to uppercase style";
  }

  public Set run(TransformationManager manager, TreeRefactorItContext context, 
      List violations) {
    Set sources = new HashSet(violations.size());
    List fields = new ArrayList();

    for (Iterator it = violations.iterator(); it.hasNext();) {
      RuleViolation violation = (RuleViolation) it.next();
      if (violation instanceof LowercaseConstant) {
        BinField field = ((LowercaseConstant) violation).getField();
        fields.add(field);
        sources.add(field.getCompilationUnit());
      }
    }

    if (fields.size() > 0) {
      MultiFieldRenameDialog dialog = new MultiFieldRenameDialog(fields,
          context);
      dialog.show();
      if (dialog.isOkPressed()) {
        TransformationList transList = dialog.getRenameTransformations();
        // check for any changes
        if (!transList.getTransformationList().isEmpty()) {
          manager.add(transList);
        }
      }
    }
    return Collections.EMPTY_SET;
  }
}
