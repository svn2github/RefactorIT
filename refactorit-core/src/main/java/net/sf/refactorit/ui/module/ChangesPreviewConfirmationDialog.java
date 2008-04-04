/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.preview.ChangesPreviewModel;
import net.sf.refactorit.source.preview.SourceLineNode;
import net.sf.refactorit.source.preview.SourceNode;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import javax.swing.JButton;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * ChangesPreviewDialog
 *
 * @author Kirill Buhhalko
 */


public class ChangesPreviewConfirmationDialog extends JConfirmationDialog {

  private final JButton viewChangesButton = new JButton("View Changes");

  public ChangesPreviewConfirmationDialog(
      String title, String help,
      BinTreeTableModel model, RefactorItContext context,
      String description, String helpId
      ) {
    super(title, help, model, context, description, helpId, false);

    // to disable viewChangesButton when it can not be pressed
    getTable().getTree().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        Object source = e.getPath().getLastPathComponent(); //e.getSource();
        viewChangesButton.setEnabled(source instanceof SourceNode
            || source instanceof SourceLineNode);
      }
    });

    // add to viewChangesButton listener, for CompareDialog opening
    viewChangesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BinTreeTable table = getTable();

        BinTreeTableModel binModel = getModel();
        if (binModel instanceof ChangesPreviewModel) {
          ChangesPreviewModel model = (ChangesPreviewModel) binModel;

          List nodes = table.getSelectedNodes();
          Object node = null;

          if (nodes != null) {
            if (nodes.size() > 0) {
              node = nodes.get(0);
            }

            BinTreeTableNode nd;
            if (node instanceof BinTreeTableNode) {
              nd = (BinTreeTableNode) node;

              if (nd != null) {
                SourceHolder source = nd.getSource();
                if (source != null && nd.queryLineNumber() > 0) {
                  List lines = (List) model.getMap().get(source);

                  if (lines != null) {
                    new CompareDialog(getContext(), lines,
                        Integer.parseInt(nd.getLineNumber()), source).show();
                  }
                }
              }
            }
          }
        }
      }
    });


    // add viewChangesButton to left side of button panel
    addLeftButton(viewChangesButton);
    viewChangesButton.setEnabled(false);

    viewChangesButton.setDefaultCapable(false);
    viewChangesButton.setMnemonic('s');
  }
}
