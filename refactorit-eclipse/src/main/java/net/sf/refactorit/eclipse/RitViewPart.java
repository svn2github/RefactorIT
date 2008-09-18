/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import net.sf.refactorit.common.util.AppRegistry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import javax.swing.JComponent;
import javax.swing.JRootPane;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Panel;


public class RitViewPart extends ViewPart {
  public static final String ID = "net.sf.refactorit.RitViewPart";

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
   * AWT Component shown inside RefactorIt Tab.
   */
  private JComponent component;

  // ==================== View Initialization ====================

  public void init(IViewSite site) throws PartInitException {
    super.init(site);

    setPartName(site.getSecondaryId());
  }

  public void createPartControl(Composite parent) {
    composite = new Composite(parent, EMBEDDED_STYLE);

    try {
      Frame frame = SWT_AWT.new_Frame(composite);
      frame.add(embedded);

      embedded.add(root);
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, this);

      RitPlugin.showSwtAwtErrorMessage();
    }
  }

	public void setFocus() {
    composite.setFocus();
	}

  public void setContent(JComponent content) {
    Container contentPane = root.getContentPane();

    if (component != null) {
      contentPane.remove(component);
    }

    if (content != null) {
      contentPane.add(content);
    }

    component = content;

    root.revalidate();
  }
}
