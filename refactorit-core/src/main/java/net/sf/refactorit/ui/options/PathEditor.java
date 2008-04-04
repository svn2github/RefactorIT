/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;


/**
 * Insert the type's description here.
 *
 * @author Igor Malinin
 */
public class PathEditor extends DefaultCellEditor {
  private static final ResourceBundle resources =
      ResourceUtil.getBundle(PathEditor.class);

  private static final JCheckBox dummyCheckBox = new JCheckBox();

  Path path;

  private JButton pathContents;

  /** @param sourcePathOrClassPathFromPathChooser : JPathChooser.SOURCEPATH or JPathChooser.CLASSPATH */
  public PathEditor(Component parent, int sourcePathOrClassPathFromPathChooser) {
    // we have no constructor for JButton
    super(dummyCheckBox);

    // setup real editorComponent
    pathContents = new JButton();
    pathContents.setBackground(Color.white);
    pathContents.setBorderPainted(false);
    pathContents.setDefaultCapable(false);
    pathContents.setFont(new Font("SansSerif", Font.PLAIN, 12));
    pathContents.setHorizontalAlignment(JButton.LEFT);
    pathContents.setMargin(new Insets(0, 0, 0, 0));

    JButton editorInvoker = new JButton("...");
    editorInvoker.setBackground(Color.lightGray);
    editorInvoker.setDefaultCapable(true);
    editorInvoker.setFont(new Font("SansSerif", Font.PLAIN, 12));
    editorInvoker.setMargin(new Insets(0, 0, 0, 0));
    editorInvoker.setToolTipText(resources.getString("pathchooser.tooltip"));

    JPanel editorPanel = new JPanel();
    editorPanel.setLayout(new BorderLayout());
    editorPanel.add(pathContents, BorderLayout.CENTER);
    editorPanel.add(editorInvoker, BorderLayout.EAST);
    editorPanel.setOpaque(true);

    editorComponent = editorPanel;

    final JPathChooser chooser = JPathChooser.getJPathChooser(
        sourcePathOrClassPathFromPathChooser);

    final ActionListener okListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        path = chooser.getPath();
        stopCellEditing();
      }
    };

    pathContents.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          chooser.setPath(path);
          // TODO: XXX Doublecheck null is OK
          final JPathChooserDialog dialog = new JPathChooserDialog(
              IDEController.getInstance().createProjectContext(),
              "Pick a Path", chooser, okListener, null);
          dialog.show();
        }
      }
    });

    editorInvoker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooser.setPath(path);
        // TODO: XXX Doublecheck null is OK
        final JPathChooserDialog dialog = new JPathChooserDialog(
            IDEController.getInstance().createProjectContext(),
            "Pick a Path", chooser, okListener, null);
        dialog.show();
      }
    });
  }

  public Object getCellEditorValue() {
    return path;
  }

  public Component getTableCellEditorComponent(JTable table,
      Object value, boolean isSelected, int row, int column) {
    path = (Path) value;

    pathContents.setText(path.toString());

    return editorComponent;
  }
}
