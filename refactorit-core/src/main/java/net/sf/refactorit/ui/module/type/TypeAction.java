/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.type;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.javadoc.TypeInfoJavadoc;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.tree.JTypeInfoPanel;


public class TypeAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.TypeAction";
  public static final String NAME = "Type Info";

  public String getName() {
    return NAME;
  }

  public boolean isReadonly() {
    return true;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getKey() {
    return KEY;
  }

  /**
   * Module execution.
   *
   * @param context
   * @param parent  any visible component on the screen
   * @param object  Bin object to operate
   * @return false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, final Object object) {
    BinType type;
    try {
      type = (BinType) RefactorItActionUtils.unwrapTarget(object);
    } catch (ClassCastException e) {
      // something strange was given
      return false;
    }

    if (!(type instanceof BinCIType)) {
      return false;
    }

    // to find those local types which we are going to show in the tree
    try {
      JProgressDialog.run(context,
          new Runnable() {
            public void run() {
              context.getProject().discoverAllUsedTypes();
            }
          }, type.getName(), true);
    } catch (SearchingInterruptedException ex) {
      return false;
    }

    createDialog(context, type);

    return false;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
    if (BinCIType.class.isAssignableFrom(cl)) {
      return false;
    } else if (BinItem.class.isAssignableFrom(cl)) {
      return true;
    }
    return false;
  }

  private void createDialog(RefactorItContext context, BinType type) {
    JTypeInfoPanel p = new JTypeInfoPanel(context);
    p.setJavaDocRenderer(new TypeInfoJavadoc(context, type));
    p.setBinCIType((BinCIType) type);

    TypeInfoDialog typeDialog = new TypeInfoDialog(context, p);
    typeDialog.show();
  }

  public boolean isAvailableForType(Class type) {
    return BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type);
  }
}
