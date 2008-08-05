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
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.module.OptionsAction;
import net.sf.refactorit.ui.options.JOptionsTable;
import net.sf.refactorit.ui.options.Option;
import net.sf.refactorit.ui.options.Options;
import net.sf.refactorit.ui.options.OptionsTab;
import net.sf.refactorit.ui.options.OptionsTableModel;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import javax.swing.BorderFactory;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.util.Properties;


/**
 *
 *
 * @author Igor Malinin
 */
public class RitPreferencePage extends PreferencePage
implements IWorkbenchPreferencePage {
  private static final int EMBEDDED_STYLE = SWT.NO_BACKGROUND | SWT.EMBEDDED;

  private static final String MESSAGE_PREFIX = "RefactorIT Options ... ";

  private static final Options VIEW_OPTIONS = OptionsAction.VIEW_OPTIONS;

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
  JTabbedPane tabs;

  /**
   * Edited copy of global options.
   */
  private Properties properties;

  /*
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
    properties = (Properties) GlobalOptions.getProperties().clone();

    tabs = new JTabbedPane();

    int tabCount = VIEW_OPTIONS.getTabCount();
    for (int i = 0; i < tabCount; i++) {
      OptionsTab tab = VIEW_OPTIONS.getTab(i);

      OptionsTableModel model = new OptionsTableModel(
          tab, properties, UIResources.resLocalizedStrings);

      JTable table = new JOptionsTable(tabs, model);

      String title = tab.getName();
      String local = UIResources.resLocalizedStrings.getString(title);
      if (local != null) {
        title = local;
      }

      tabs.addTab(title, new JScrollPane(table));
    }

    tabs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    tabs.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        notifyTabSelected();
      }
    });

    notifyTabSelected(0);
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

    root.getContentPane().add(tabs);

    Dimension size = tabs.getPreferredSize();
    composite.setLayout(new FixedLayout(new Point(size.width, size.height)));

    return composite;
  }

  /**
   * Called from AWT event thread when tab selection changes
   */
  void notifyTabSelected() {
    composite.getDisplay().asyncExec(new Runnable() {
      public void run() {
        notifyTabSelected(tabs.getSelectedIndex());
      }
    });
  }

  /**
   * Called from SWT event thread when title should be adjusted
   * according to the current tab selection.
   */
  void notifyTabSelected(int index) {
    setMessage(MESSAGE_PREFIX + tabs.getTitleAt(index));
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  protected void performDefaults() {
    int selected = tabs.getSelectedIndex();
    OptionsTab tab = VIEW_OPTIONS.getTab(selected);
    tab.setDefault();

    for (int i = 0; i < tab.getVisibleOptionsCount(); i++) {
      Option opt = tab.getVisibleOption(i);

      String key = opt.getKey();
      String value = GlobalOptions.getDefaultOption(key);
      if (value == null) {
        if (properties.getProperty(key) != null) {
          // it is neccessary for instance in warning options
          properties.remove(key);
        }
      } else {
        properties.setProperty(key, value);
      }
    }

    tabs.revalidate();
    tabs.repaint();

    tabs.setSelectedIndex(selected);
  }

  /*
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk() {
    GlobalOptions.setProperties(properties);
    GlobalOptions.fireOptionsChanged();
    GlobalOptions.save();
    return true;
  }
}
