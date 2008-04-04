/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.search;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.query.structure.AbstractSearch;
import net.sf.refactorit.query.structure.FindRequest;
import net.sf.refactorit.refactorings.FindRerunInfo;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.apache.log4j.Logger;




public class FindAction extends AbstractRefactorItAction {
  private static final Logger log = AppRegistry.getLogger(FindAction.class);

  public static final String KEY = "refactorit.action.FindAction";
  public static final String NAME = "Structure Search";

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean isAvailableForType(Class type) {
    if (Project.class.equals(type)
        || BinPackage.class.equals(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        ) {
      return true;
    }
    return false;
  }
  public boolean isReadonly() {
    return true;
  }

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'S';
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(final RefactorItContext context, Object inObject) {
    FindRequest req = null;

    if (inObject instanceof FindRerunInfo) {
      FindRerunInfo info = (FindRerunInfo) inObject;
      inObject = info.object;
      req = info.request;
    } else {
      FindDialog fd = new FindDialog(context, inObject);
      fd.show();
      req = fd.getRequest();
    }

    final FindRequest fr = req;
    final Object target = RefactorItActionUtils.unwrapTarget(inObject);

    if (fr == null) {
      return false;
    }

    try {
      final BinTreeTableNode[] rootNode = new BinTreeTableNode[1];
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          AbstractSearch search = fr.createSearch(context, target);
          rootNode[0] = search.doSearch();
        }
      }, true);

      rootNode[0].sortAllChildren();
      rootNode[0].reflectLeafNumberToParentName();

      BinTreeTableModel model = new BinTreeTableModel(rootNode[0]);
      BinTreeTable table = new BinTreeTable(model, context);
      ResultArea results = ResultArea.create(table, context, this);

      results.setTargetBinObject(new FindRerunInfo(target, fr));
      BinPanel panel = BinPanel.getPanel(context, getName(), results);
      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact.search");
    } catch (SearchingInterruptedException ex) {
      //Perhaps, something should be here?
    }

    return false;
  }
}
