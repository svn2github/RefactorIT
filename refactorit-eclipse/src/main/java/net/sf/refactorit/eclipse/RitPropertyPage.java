/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.options.ProjectOptionsPanel;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import javax.swing.JRootPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;


/**
 *
 *
 * @author Igor Malinin
 */
public class RitPropertyPage extends PropertyPage {
  private static final int EMBEDDED_STYLE = SWT.NO_BACKGROUND | SWT.EMBEDDED;

  /**
   * Heavy-weight panel to enable AWT
   * mouse events on JRE 1.5 and higher VM.
   */
  final Panel embedded = new Panel(new BorderLayout());

  /**
   * Swing root pane - enable advanced features.
   */
  final JRootPane root = new JRootPane();

  /**
   * SWT Composite of embedded style.
   */
  private Composite composite;

  /**
   * Main AWT component.
   */
  private ProjectOptionsPanel panel;

  /**
   * Selected project.
   */
  private Project project;

  /**
   * Constructs RefactorIT Project Options property page.
   */
  public RitPropertyPage() {
    noDefaultAndApplyButton();
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {
    composite = new Composite(parent, EMBEDDED_STYLE);

    try {
      Frame frame = SWT_AWT.new_Frame(composite);
      frame.add(embedded);
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, this);
      RitPlugin.showSwtAwtErrorMessage();
      return composite;
    }

    embedded.add(root);

    IDEController controller = IDEController.getInstance();
    controller.ensureProjectWithoutParsing();

    project = controller.getActiveProject();

    panel = new ProjectOptionsPanel(project.getOptions());
    root.getContentPane().add(panel);

    Dimension size = panel.getPreferredSize();
    composite.setLayout(new FixedLayout(new Point(size.width, size.height)));

    return composite;
  }

  /*
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk() {
    panel.updateSettings();
    ProjectOptions options = project.getOptions();

    //ProjectSettings settings = new ProjectSettings(options);
    //settings.serialize();
    //options.
    options.serialize();

    //options.setProjectSettings(settings);
    project.fireProjectSettingsChangedEvent();

    IDEController controller = IDEController.getInstance();
    // TODO: actually we may need to recreate project
    // if sourcepath or classpath needs recreating
    controller.ensureProject();

    controller.checkSourcePathSanity(project.getPaths().getSourcePath(), true);
    controller.checkClassPathSanity(project.getPaths().getClassPath(), true);

    return true;
  }
}
