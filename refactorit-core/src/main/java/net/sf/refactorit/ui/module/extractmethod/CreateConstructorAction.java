/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.extractmethod;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.createconstructor.CreateConstructor;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.JOptionPane;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author Jaanek Oja
 * @author Anton Safonov
 * @author Tonis Vaga
 */
public class CreateConstructorAction extends AbstractRefactorItAction {
  /** The unique key for this action */
  public static final String KEY = "refactorit.action.CreateConstructorAction";

  /** The name of action (shown in menus) */
  public static final String NAME = "Create Constructor";

  /**
   * Name of action (shown in menus).
   *
   * @return  name
   */
  public String getName() {
    return NAME;
  }

  /**
   * Returns unique key for this action.
   *
   * @return  key
   */
  public String getKey() {
    return KEY;
  }

  /**
   * Determines whether this action can operate with
   * multiple selected objects.
   *
   * @return  true if action can operate with multiple targets.
   */
  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean isAvailableForType(Class type) {
    return BinSelection.class.equals(type)
        || BinField.class.isAssignableFrom(type);
  }

  /**
   * Module execution.
   *
   * It executes the RefactorItAction on target BinXXX object.
   * If you want to make a clean, first time execution of this RefactorItAction,
   * then you MUST provide a new Context() object into run(...) method,
   * otherwise, if you just want to ReRun the action on specified target,
   * then you provide the old context object into run(...) method it was
   * executed previously.
   *
   * Some module actions check the context object for old data they have
   * put into it. For example Metrics should check whether it should display
   * a Dialog full of metric selections to the user or not, it depends whether
   * it finds old data from Context or not.
   *
   * @param context  some native class (For native implemetation of modules)
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(RefactorItContext context, Object object) {
    CreateConstructor creator;
    Object state = null;
    if (object instanceof BinSelection) {
      BinSelection selection = (BinSelection) object;
      creator = new CreateConstructor(context, selection);
      state = selection;
    } else {
      // field
      List fields = null;
      if (object instanceof BinField) {
        fields = Collections.singletonList(object);
      } else if (object instanceof Object[]) {
        fields = Arrays.asList((Object[]) object);
      } else {
        Assert.must(false, "wrong args to CreateConstructor");
      }
      creator = new CreateConstructor(context, fields);
    }
    RefactoringStatus status = creator.checkPreconditions();
    if (!status.isOk()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Not possible to create constructor",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
    status = creator.apply();//TransformationManager.performTransformationFor(creator);
    if (!status.isOk()) {
      if (status.hasSomethingToShow()) {
        RitDialog.showMessageDialog(context,
            status.getAllMessages(), "Error while performing refactoring",
            JOptionPane.ERROR_MESSAGE);
      }

      return false;
    }

    context.setState(state); // small hack :)

    return true;
  }

  /**
   * Overrides super!
   */
  public void updateEnvironment(final RefactorItContext context) {
    if (context.getState() instanceof BinSelection) {
      BinSelection selection = (BinSelection) context.getState();

      SourceCoordinate start = selection.getStartSourceCoordinate();
      context.show(
          selection.getCompilationUnit(),
          start.getLine(),
          GlobalOptions.getOption("source.selection.highlight").equals("true"));
//			context.reload();
    }
    super.updateEnvironment(context);

    // new bugs could appear after rebuild
//		ErrorsTab.addNew(context, parent);
  }

  public boolean isReadonly() {
    return false;
  }
}
