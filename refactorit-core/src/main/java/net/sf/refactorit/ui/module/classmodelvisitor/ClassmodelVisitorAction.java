/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.classmodelvisitor;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.HtmlUtil;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.standalone.BrowserContext;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import javax.swing.JToolTip;
import javax.swing.tree.TreePath;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;


/**
 * @author Anton Safonov
 */
public class ClassmodelVisitorAction extends AbstractRefactorItAction {
  private static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(ClassmodelVisitorAction.class);

  public static final String KEY = "refactorit.action.ClassmodelVisitorAction";
  public static final String NAME = resLocalizedStrings.getString("action.name");

  public String getName() {
    return NAME;
  }

  public boolean isReadonly() {
    return true;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
    if (BinItem.class.isAssignableFrom(cl)) {
      return true;
    }
    return false;
  }

  public String getKey() {
    return KEY;
  }

  public boolean isAvailableForType(Class type) {

    if (BinItem.class.isAssignableFrom(type)) {
      return true;
    }

    return false;
  }

  /**
   * Module execution.
   *
   * @param parent  any visible component on the screen
   * @param object  Bin object to operate
   * @param param   some native class (For native implemetation of modules)
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, final Object object) {
    // Catch incorrect parameters
    {
      Assert.must(context != null,
          "Attempt to pass NULL context into ClassmodelVisitorAction.run()");
      Assert.must(object != null,
          "Attempt to pass NULL object into ClassmodelVisitorAction.run()");
    }
System.err.println("target: " + object);
    ClassmodelVisitorModel model = new ClassmodelVisitorModel(context, object);

    BinTreeTable table = createTable(model, context);

    String moduleName = resLocalizedStrings.getString("tab.title");
    ResultArea results = ResultArea.create(table, context,
        ClassmodelVisitorAction.this);
    results.setTargetBinObject(object);
    BinPanel panel = BinPanel.getPanel(context, moduleName, results);

    table.smartExpand();

    // Register default help for panel's current toolbar
    panel.setDefaultHelp("refact");

    // we never change anything
    return false;
  }

  public static BinTreeTable createTable(final BinTreeTableModel model,
      final RefactorItContext context) {

    final BinTreeTable table = new BinTreeTable(model, context) {
      protected void openCompilationUnit(ParentTreeTableNode node,
          final RefactorItContext innerContext) {
        boolean shown = false;
        if (innerContext instanceof BrowserContext) {
          if (node.getBin() instanceof LocationAware) {
            ((BrowserContext) innerContext).show((LocationAware) node.getBin());
            shown = true;
          } else if (node.getBin() instanceof ASTImpl) {
            if (((ASTImpl) node.getBin()).getStartLine() > 0) {
              ((BrowserContext) innerContext).show(
                  ((BinTreeTableNode) node).getSource(),
                  (ASTImpl) node.getBin());
              shown = true;
            }
          }
        }

        if (!shown) {
          super.openCompilationUnit(node, innerContext);
        }
      }

      public JToolTip createToolTip() {
        JToolTip tip = new JToolTip();

//        tip.setPreferredSize(new Dimension(500, 10));
        tip.setBackground(new Color(255, 255, 220)/*getBackground()*/);
        tip.setForeground(getForeground());
        tip.setFont(getFont());

        tip.setComponent(this);

        return tip;
      }

      public Point getToolTipLocation(MouseEvent e) {
        Point p = e.getPoint();

        int row = -1;
        try {
          row = getTree().getRowForLocation(p.x, p.y);
        } catch (NullPointerException ex) {
        }
        if (row < 0) {
          return null;
        }

        Rectangle rect = getTree().getRowBounds(row);
//        if (!getVisibleRect().contains(rect)) {
//          p = null; // Swing will handle it itself
//        } else {
          p = rect.getLocation();
//        }

        return p;
      }

      public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();

        int row = -1;
        try {
          row = getTree().getRowForLocation(p.x, p.y);
        } catch (NullPointerException ex) {
        }
        if (row < 0) {
          return null;
        }

//        Rectangle rect = getTree().getRowBounds(row);
//        if (!getVisibleRect().contains(rect)) {
//          return null;
//        }

        TreePath path = getTree().getPathForRow(row);
        BinTreeTableNode node = (BinTreeTableNode) path.getLastPathComponent();

        String styleBody = HtmlUtil.styleBody(node.getBin() + "<br><hr>"
            + HtmlUtil.stripHtmlBody(node.getDisplayName()), getFont());
        styleBody = StringUtil.replace(styleBody, "white-space: nowrap;", "");
        styleBody = StringUtil.replace(styleBody, "&nbsp;", " ");
        return styleBody;
      }

    };

    // open all branches
    table.expandAll();

    table.getTree().setRootVisible(false);
    table.getTree().setShowsRootHandles(true);

    table.setTableHeader(null);

    return table;
  }

}
