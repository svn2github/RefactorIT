/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.OptionsChangeListener;
import net.sf.refactorit.ui.panel.BinPanel;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


/**
 * @author Vladislav Vislogubov
 */
public class JConsol extends JPanel
  implements
    ChangeListener,
    OptionsChangeListener
{
  static final Color color = new Color(58, 110, 165);

  private JTextArea area;
  JTabbedPane tabPane;

  boolean alarmed = true;

  Document document = new PlainDocument();

  private static Timer timer;

  private PrintStream stream = new PrintStream(new OutputStream() {
    public void write(int b) throws IOException {
      byte[] buf = {(byte) b};
      write(buf);
    }

    public void write(byte buf[], int off, int len) throws IOException {
      write(new String(buf, off, len)); // FIXME: Maybe use default encoding?
    }

    public void write(final String str) throws IOException {
      try {
        SwingUtilities.invokeLater(
            new Runnable() {
          public void run() {
            try {
              document.insertString(document.getLength(), str, null);
            } catch (BadLocationException ignore) {}
          }
        }
        );

        // Jikes can't stand 'catch(Exception ignore){}' here..
      } catch (RuntimeException ignore) {}
    }
  }, true);

  /**
   * JStatus constructor comment.
   */
  public JConsol(final MouseListener l) {
    setLayout(new GridLayout());

    tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
    tabPane.addChangeListener(this);

    JScrollPane scrollPane = new JScrollPane();
    area = new JTextArea(document);
    area.setEditable(false);
    scrollPane.setViewportView(area);
    tabPane.addTab(JRefactorItFrame.resLocalizedStrings.getString("tab.console"),
        scrollPane);

    scrollPane.getViewport().setBackground(Color.decode(GlobalOptions.getOption("source.background")));
    area.setBackground(Color.decode(GlobalOptions.getOption("source.background")));

    add(tabPane);

    setSize(200, 100);
    setMaximumSize(new Dimension(200, 100));

    GlobalOptions.registerOptionChangeListener(this);
    area.addMouseListener(l);
    tabPane.addMouseListener(l);

    tabPane.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        int kc = ke.getKeyCode();
        switch (kc) {
          case KeyEvent.VK_ESCAPE: {
            close();
            break;
          }
          case KeyEvent.VK_M: {
            if (ke.isControlDown()) {
              Component comp = tabPane.getSelectedComponent();

              l.mouseReleased(new MouseEvent(tabPane,
                  1,
                  System.currentTimeMillis(),
                  KeyEvent.VK_CONTROL,
                  comp.getX(),
                  comp.getY() + comp.getHeight(),
                  1,
                  true));
            }
            break;
          }
        }
      }
    });
  }

  /**
   * JStatus constructor comment.
   */
  public PrintStream allocateTab(String title, MouseListener l) {
    for (int i = 0; i < getTabCount(); i++) {
      String name = tabPane.getTitleAt(i);
      if (title.equals(name)) {
        JScrollPane scroll = (JScrollPane) tabPane.getComponentAt(i);
        JTextArea t = (JTextArea) scroll.getViewport().getComponent(0);
        document = t.getDocument();
        return stream;
      }
    }

    document = new PlainDocument();
    JTextArea ta = new JTextArea(document);
    ta.setEditable(false);
    ta.setBackground(Color.decode(GlobalOptions.getOption("source.background")));

    JScrollPane scrollPane = new JScrollPane(ta);
    tabPane.addTab(title, scrollPane);
    scrollPane.getViewport().setBackground(Color.decode(GlobalOptions.getOption("source.background")));

    // Bring the newly-created console to the front
    tabPane.setSelectedComponent(scrollPane);

    ta.addMouseListener(l);
    return stream;
  }

  public Object addTab(String title, final JComponent component) {
    tabPane.addTab(title, component);

    if (title.equals("Errors")) {
      if (timer != null) {
        timer.stop();
      }

      timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          int index = tabPane.indexOfComponent(component);
          if (index < 0 || index >= tabPane.getTabCount()) {
            return;
          }

          if (alarmed) {
            tabPane.setBackgroundAt(index, color);
          } else {
            tabPane.setBackgroundAt(index, null);
          }
          alarmed = !alarmed;
        }
      });
      timer.start();
    } else {
      int currentTabIndex = tabPane.getTabCount();
      if (currentTabIndex > 0) {
        tabPane.setSelectedIndex(currentTabIndex - 1);
      }
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          component.requestFocus();
        }
      });
    }

    return component;
  }

  public void removeTab(JComponent component) {
    tabPane.remove(component);

    // The following statement (commented out as 13.) caused Bug 1137
    // This function assumes that if removeTab() is called, then the showTab()
    // previously must have been called (setSelectedComponent(..) is called).
    // This should not be a requirement, because addTab() and then removeTab()
    // may happen, actually the probability is low. (Jaanek)
    // 13. ** tabPane.getSelectedComponent().requestFocus(); ** //
    final Component selectedComponent = tabPane.getSelectedComponent();
    if (selectedComponent != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          selectedComponent.requestFocus();
        }
      });
    }
  }

  public void showTab(JComponent component) {
    if (component instanceof BinPanel) {
      BinPanel bp = (BinPanel) component;
      if (bp.getName().equals("Errors")) {
        return; // skip this selection
      }

      bp.getCurrentPane().revalidate();
    }

    tabPane.setSelectedComponent(component);
    //component.requestFocus();

    // FIXME: should be checked whether the compoenent is visible
    // if not, then set it visible.
    //if (!component.isVisible()) {
    //  component.setVisible(true);
    //}
  }

  /**
   * JStatus constructor comment.
   */

  public boolean isClearable() {
    int index = tabPane.getSelectedIndex();
    Object obj = tabPane.getComponentAt(index);
    return ((obj instanceof JScrollPane) &&
        (tabPane.getTitleAt(index).
        equals(JRefactorItFrame.resLocalizedStrings.
        getString("tab.console"))));
  }

  public boolean isCloseable() {
    int index = tabPane.getSelectedIndex();
    Object obj = tabPane.getComponentAt(index);
    return!((obj instanceof JScrollPane) &&
        (tabPane.getTitleAt(index).
        equals(JRefactorItFrame.resLocalizedStrings.
        getString("tab.console"))));
  }

  public void clear() {
    JScrollPane scroll = (JScrollPane) tabPane.getComponentAt(tabPane.
        getSelectedIndex());
    ((JTextArea) scroll.getViewport().getComponent(0)).setText("");
  }

  /**
   * JStatus constructor comment.
   */
  public void close() {
    if (isCloseable()) {
      tabPane.removeTabAt(tabPane.getSelectedIndex());
      final Component selectedComponent = tabPane.getSelectedComponent();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          selectedComponent.requestFocus();
        }
      });
    }
    /*
      int ind = tabPane.getSelectedIndex();
      if ( ind < 0 ) {
     ind = tabPane.getTabCount() - 1;
     tabPane.setSelectedIndex( ind );
      }

     System.out.println( "(1)tabPane focus = " + tabPane.hasFocus() );
      tabPane.requestFocus();
     System.out.println( "(2)tabPane focus = " + tabPane.hasFocus() );
     System.out.println( "(1)tabPane.getComponentAt( ind ) focus = " + tabPane.getComponentAt( ind ).hasFocus() );
      Component c = tabPane.getComponentAt( ind );
      c.requestFocus();
      tabPane.setSelectedComponent( c );
     System.out.println( "(2)tabPane.getComponentAt( ind ) focus = " + tabPane.getComponentAt( ind ).hasFocus() );
     */

  }

  public void closeAll() {
    tabPane.removeAll();

    // clear panel factory
    BinPanel.removeAllPanels();

    JScrollPane scrollPane = new JScrollPane();
    area = new JTextArea(document);
    area.setEditable(false);
    scrollPane.setViewportView(area);

    scrollPane.getViewport().setBackground(Color.decode(GlobalOptions.getOption("source.background")));
    area.setBackground(Color.decode(GlobalOptions.getOption("source.background")));

    tabPane.addTab(JRefactorItFrame.resLocalizedStrings.getString("tab.console"),
        scrollPane);
  }

  /**
   * JStatus constructor comment.
   */
  public int getTabCount() {
    return tabPane.getTabCount();
  }

  /**
   */
  public Component[] getTabs() {
    return tabPane.getComponents();
  }

  public void stateChanged(ChangeEvent e) {
    if (timer == null) {
      return;
    }

    Component c = tabPane.getSelectedComponent();
    if (c instanceof BinPanel) {
      BinPanel bp = (BinPanel) c;
      if (bp.getName().equals("Errors")) {
        timer.stop();
        timer = null;

        tabPane.setBackgroundAt(tabPane.getSelectedIndex(), null);
      }
    }
  }

  public void optionChanged(String key, String newValue) {
    //System.out.println("BinPane.optionChanged key=" + key );
    if (key.startsWith("tree.")) {
      Component[] comps = getTabs();
      for (int i = 0, max = comps.length; i < max; i++) {
        if (comps[i] instanceof BinPanel) {
          ((BinPanel) comps[i]).optionChanged(key, newValue);
        }
      }
    } else if (key.startsWith("source.")) {
      Component[] comps = getTabs();
      for (int i = 0, max = comps.length; i < max; i++) {
        if (comps[i] instanceof JScrollPane) {
          ((JScrollPane) comps[i]).getViewport().setBackground(Color.decode(
              GlobalOptions.getOption("source.background")));
          ((JScrollPane) comps[i]).getViewport().getView().setBackground(Color.
              decode(GlobalOptions.getOption("source.background")));
        }
      }
    }
  }

  public void optionsChanged() {
    Component[] comps = getTabs();
    for (int i = 0, max = comps.length; i < max; i++) {
      if (comps[i] instanceof BinPanel) {
        ((BinPanel) comps[i]).optionsChanged();
      } else if (comps[i] instanceof JScrollPane) {
        ((JScrollPane) comps[i]).getViewport().setBackground(Color.decode(GlobalOptions.getOption("source.background")));
        ((JScrollPane) comps[i]).getViewport().getView().setBackground(Color.
            decode(GlobalOptions.getOption("source.background")));
      }
    }
  }
}
