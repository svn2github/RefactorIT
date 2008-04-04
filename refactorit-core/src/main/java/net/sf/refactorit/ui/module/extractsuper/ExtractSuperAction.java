/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.extractsuper;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.extractsuper.ExtractSuper;
import net.sf.refactorit.refactorings.usesupertype.UseSuperTypeRefactoring;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactoringStatusViewer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.common.UseSuperTypeAction;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class ExtractSuperAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.ExtractSuperAction";
  public static final String NAME = "Extract Superclass/Interface";

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean isReadonly() {return false;
  }

  public String getName() {
    return NAME;
  }

  public String getKey() {
    return KEY;
  }


  public boolean isAvailableForType(Class type) {
    if (BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        || BinMethod.class.isAssignableFrom(type)
        || BinField.class.isAssignableFrom(type)
        || BinMemberInvocationExpression.class.isAssignableFrom(type)) {
      return true;
    }
    return false;
  }
  /**
   * Module execution.
   *
   * @param context
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, final Object object) {
    BinType target = (BinType) unwrapTarget(context, object);
    if (target == null) {
      return false;
    }

    final ExtractSuper extractor = new ExtractSuper(context, target.getTypeRef());
    extractor.setMembersToExtract(getMembersToPreselect(object));

    RefactoringStatus status = extractor.checkPreconditions();

    ExtractSuperDialog dialog;
    if (status.isOk()) {
      status = null;
      dialog = new ExtractSuperDialog(context, extractor);
      do {
        dialog.show();
        if (dialog.isOkPressed()) {
          status = extractor.checkUserInput();
          if (status.isOk()) {
            break;
          } else {
            showStatusMessage(context, status, "Wrong input data.");
            status = null;
          }
        } else {
          status = null;
          break;
        }
      } while (true);
    } else {
      return false;
    }

    if (status != null && status.isOk()) {
      // FIXME: temporary solution to RIM-765 just to make RIT stable
      extractor.getTransformationManager().setShowPreview(true);
      extractor.getTransformationManager().getEditorManager()
          .setPersistantLineManager(true);
      status = extractor.apply();
      if (status != null && !status.isOk()) {
        if (status.hasSomethingToShow()) {
          showStatusMessage(context, status, "Not possible to extract.");
        }
        return false;
      }

      if (dialog.isRefactorToSupertypeEnabled()) {
        context.rebuildAndUpdateEnvironment();
        
        String supertypeName = extractor.getSupertypeQualifiedName();
        String subtypeName = extractor.getSubtypeQualifiedName();

        BinTypeRef subtypeRef = context.getProject().findTypeRefForName(
            subtypeName);
        BinTypeRef supertypeRef = context.getProject().findTypeRefForName(
            supertypeName);

        if (subtypeRef == null) {
          DialogManager.getInstance().showCustomError(context, "Type "
              + subtypeName + " not found after rebuild");
          return false;
        }

        if (supertypeRef == null) {
          DialogManager.getInstance().showCustomError(context, "Type "
              + supertypeName + " not found after rebuild");
          return false;
        }

        final UseSuperTypeRefactoring ust = new UseSuperTypeRefactoring(
            supertypeRef.getBinCIType(),
            IDEController.getInstance().createProjectContext()){
          
          public String getDescription() {
            return extractor.getDescription() + " and use it where possible";
          }
          
          public String getName(){
            return extractor.getName();
          }
        };
        
        ust.getTransformationManager().getEditorManager()
            .setPersistantLineManager(true);
        ust.setSupertype(supertypeRef);
        ust.setSubtypes(Collections.singletonList(subtypeRef));
        ust.setUseInstanceOf(false);

        status.addEntry(ust.checkPreconditions());
        status.addEntry(ust.checkUserInput());
        if (status != null && status.isOk()){
          // FIXME: temporary solution to RIM-765 just to make RIT stable
          /*LineManager lineManager = extractor.getTransformationManager()
              .getEditorManager().getLineManager();
          lineManager.remapSources(context);
          ust.getTransformationManager().getEditorManager().setLineManager(
              lineManager);*/
          status.addEntry(ust.apply());
        }
      }

      return status != null && status.isOk();
    }

    return false;
  }

  static class LocalUseSupertypeAction extends UseSuperTypeAction {
    final UseSuperTypeRefactoring ust;

    LocalUseSupertypeAction(BinTypeRef subtype, BinTypeRef supertype) {
      ust = new UseSuperTypeRefactoring(supertype.getBinCIType(),
          IDEController.getInstance().createProjectContext());
      ust.setSupertype(supertype);
      ust.setSubtypes(Collections.singletonList(subtype));
      ust.setUseInstanceOf(false);
    }

    public boolean readUserInput(Refactoring refactoring) {
      return true;
    }

    public Refactoring createRefactoring(RefactorItContext context, Object object) {
      return ust;
    }
  }

  private Object unwrapTarget(IdeWindowContext context, Object target) {
    if (target instanceof BinMemberInvocationExpression) {
      target = ((BinMemberInvocationExpression) target).getMember();
    }

    if (target instanceof BinCIType) {
      // already ok
    } else if (target instanceof BinMethod.Throws) {
      target = ((BinMethod.Throws) target).getException().getBinCIType();
    } else if (target instanceof BinThrowStatement) {
      target = ((BinThrowStatement) target).getExpression().getReturnType().
          getBinType();
    } else if (target instanceof BinMember) {
      target = ((BinMember) target).getOwner().getBinCIType();
    } else if (target instanceof Object[]) {
      int types = 0;
      BinCIType type = null;
      for (int i = 0; i < ((Object[]) target).length; i++) {
        Object some = ((Object[]) target)[i];
        if (some instanceof BinCIType) {
          ++types;
          type = (BinCIType) some;
        }
      }
      if (types > 1) {
        RitDialog.showMessageDialog(context,
            "Can not extract from several classes at once.",
            "Multiply classes selected",
            JOptionPane.ERROR_MESSAGE);
        target = null;
      } else if (types == 1) {
        target = type;
      } else {
        for (int i = 0; i < ((Object[]) target).length; i++) {
          Object some = unwrapTarget(context, ((Object[]) target)[i]);
          if (some != null) {
            target = some;
            break;
          }
          if (i == ((Object[]) target).length - 1) {
            target = null;
          }
        }
      }
    } else {
      target = null;
    }

    return target;
  }

  private List getMembersToPreselect(Object object) {
    List members = new ArrayList(1);
    if (object instanceof BinMemberInvocationExpression) {
      members.add(((BinMemberInvocationExpression) object).getMember());
    } else if (object instanceof BinField
        || (object instanceof BinMethod
        && !(object instanceof BinConstructor))) {
      members.add(object);
    } else if (object instanceof Object[]) {
      for (int i = 0; i < ((Object[]) object).length; i++) {
        members.addAll(getMembersToPreselect(((Object[]) object)[i]));
      }
    }

    return members;
  }

  public char getMnemonic() {
    return 'A';
  }

  private void showStatusMessage(
      RefactorItContext context, RefactoringStatus status, String helpText
  ) {
    RefactoringStatusViewer viewer = new RefactoringStatusViewer(
        context, helpText, "refact.extract_super");
    viewer.display(status);

//    JOptionPane.showMessageDialog(
//        DialogManager.findOwnerWindow(parent),
//        status.getAllMessages(), helpText,
//        JOptionPane.ERROR_MESSAGE);
  }
}
