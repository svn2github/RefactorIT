/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.optionsui;


import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import net.sf.refactorit.commonIDE.options.JavadocPathEditingPanel;
import net.sf.refactorit.ui.options.JIgnoredPathChooser;
import net.sf.refactorit.ui.options.JPathChooser;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A convenience ui object that encloses all the path choosers and the
 * "autodetect paths" option. The path choosers are put into a tabbed pane one
 * per tab. The "autodetect paths" option radiobuttons are shown just above this tabbed
 * pane. Besides wrapping visual elements, implements the ui procedures that must
 * be prjocessed on autodetect on an off events.
 *
 * @author juri
 */
public class PathEditPanel extends JPanel {
  RefactorItPropPage parentPropertyPage;

  JPathChooser sourcepathChooser;
  JIgnoredPathChooser ignoredSourcepath;
  JPathChooser classpathChooser;
  JavadocPathEditingPanel javadocPathEditingPanel;
  JPanel method;
  JRadioButton autodetect = new JRadioButton("Detect classpath and sourcepath");
  JRadioButton manual = new JRadioButton("Specify classpath and sourcepath:");

  public PathEditPanel(RefactorItPropPage parentPropertyPage,
      JPathChooser sourcepath, JIgnoredPathChooser ignoredSourcepath,
      JPathChooser classpath, JavadocPathEditingPanel javadocPathEditingPanel) {
    super(new BorderLayout());

    this.parentPropertyPage = parentPropertyPage;
    this.sourcepathChooser = sourcepath;
    this.ignoredSourcepath = ignoredSourcepath;
    this.classpathChooser = classpath;
    this.javadocPathEditingPanel = javadocPathEditingPanel;

    ButtonGroup group = new ButtonGroup();
    group.add(autodetect);
    group.add(manual);

    method = new JPanel(new GridLayout(2, 1));
    method.add(autodetect);
    method.add(manual);

    JComponent pathsPanel = createTabbedPathsPanel(sourcepath,
        ignoredSourcepath, classpath, javadocPathEditingPanel);
    add(method, BorderLayout.NORTH);
    add(pathsPanel);

    addListeners();
  }

  private JComponent createTabbedPathsPanel(JComponent sourcepath,
      JComponent ignoredSourcepath, JComponent classpath, JComponent javadocPathEditingPanel) {
    JTabbedPane result = new JTabbedPane();
    result.addTab("Classpath", classpath);
    result.addTab("Sourcepath", sourcepath);
    result.addTab("Ignored sourcepath", ignoredSourcepath);
    result.addTab("Javadoc", javadocPathEditingPanel);
    return result;
  }

  public void addListeners() {
    this.manual.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sourcepathChooser.setEnabled(true);
        javadocPathEditingPanel.setEnabled(true);
        classpathChooser.setEnabled(true);
        ignoredSourcepath.setEnabled(true);
      }
    });

    this.autodetect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAutoDetect(true);
      }
    });
  }

  public void setAutoDetect(boolean b) {
    if (!b) {
      return;
    }
    classpathChooser.setContent(RefactorItPropPage.
        getAutoDetectedClasspathAsStringList());
    classpathChooser.setEnabled(false);
    classpathChooser.selectNone();
    parentPropertyPage.readAutodetectedSourcepath(sourcepathChooser);
    sourcepathChooser.setEnabled(false);
    sourcepathChooser.selectNone();
    javadocPathEditingPanel.setEnabled(false);
    javadocPathEditingPanel.selectNone();
    ignoredSourcepath.setEnabled(true);
    ignoredSourcepath.selectNone();
  }
}
