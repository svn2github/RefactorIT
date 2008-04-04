/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import com.borland.jbuilder.viewer.ClassNodeViewer;
import com.borland.primetime.editor.EditorPane;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.GutterIcons;
import com.borland.primetime.ide.Message;
import com.borland.primetime.ide.NodeViewer;
import com.borland.primetime.node.FileNode;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.Project;
import com.borland.primetime.vfs.Url;
import com.borland.primetime.viewer.AbstractTextNodeViewer;
import com.borland.primetime.viewer.TextNodeViewer;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import java.awt.Component;


public class PositionMessage extends Message {
  private Project project;
  private int length;
  private int column;
  private int line;
  private Url url;

  static int getLine(PositionMessage positionmessage) {
    return positionmessage.line;
  }

  static int getColumn(PositionMessage positionmessage) {
    return positionmessage.column;
  }

  static int getLength(PositionMessage positionmessage) {
    return positionmessage.length;
  }

//  static Class a(String s) {
//    try {
//      return Class.forName(s);
//    }
//    catch (ClassNotFoundException classnotfoundexception) {
//      throw new NoClassDefFoundError(classnotfoundexception.getMessage());
//    }
//  }

  public static PositionMessage createPositionMessage(Project project,
      String s, Url url, int line, int column, int length, Icon icon) {
    String s1 = ""; // line content to highlight?
    if (icon == null) {
      return new PositionMessage(project, url, line, column, length,
          String.valueOf(s) + String.valueOf(s1), url.toString());
    } else {
      return new PositionMessage(project, url, line, column, length,
          String.valueOf(s) + String.valueOf(s1), url.toString(), icon);
    }
  }

  public void displayResult(Browser browser, boolean flag) {
    FileNode filenode = project.getNode(url);
    try {
      Node node = browser.getActiveNode();
      requestFocusToNode(browser, node, flag);

      if (flag) {
        browser.navigateTo(filenode);
      } else {
        browser.setActiveNode(filenode, false);
      }

      NodeViewer nodeviewer = browser
          .getViewerOfType(filenode, TextNodeViewer.class);
      if (nodeviewer == null) {
        nodeviewer = browser.getViewerOfType(filenode, ClassNodeViewer.class);
      }

      browser.setActiveViewer(filenode, nodeviewer, flag);

      if (nodeviewer instanceof AbstractTextNodeViewer) {
        AbstractTextNodeViewer abstracttextnodeviewer
            = (AbstractTextNodeViewer) nodeviewer;
        EditorPane editorpane = abstracttextnodeviewer.getEditor();
        Component component = Browser.getActiveBrowser().getFocusOwner();
        editorpane.requestFocus();
        SwingUtilities.invokeLater(
            new Positioner(this, editorpane, flag, component, browser, filenode));
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void requestFocusToNode(Browser browser, Node node,
      boolean flag) throws Exception {
    NodeViewer nodeviewer = browser.getViewerOfType(
        node, com.borland.primetime.viewer.TextNodeViewer.class);
    if (nodeviewer == null) {
      nodeviewer = browser.getViewerOfType(
          node, com.borland.jbuilder.viewer.ClassNodeViewer.class);
    }
    browser.setActiveViewer(node, nodeviewer, flag);
    if (nodeviewer instanceof AbstractTextNodeViewer) {
      AbstractTextNodeViewer abstracttextnodeviewer
          = (AbstractTextNodeViewer) nodeviewer;
      EditorPane editorpane = abstracttextnodeviewer.getEditor();
//      Component component = Browser.getActiveBrowser().getFocusOwner();
      editorpane.requestFocus();
    }
  }

  public void selectAction(Browser browser) {
    displayResult(browser, false);
  }

  public void messageAction(Browser browser) {
    displayResult(browser, true);
  }

  public PositionMessage(Project project, Url url, int line, int column,
      int length, String highlight, String path,
      Icon icon) {
    super(highlight, icon, path);
    this.project = project;
    this.url = url;
    this.line = line;
    this.column = column;
    this.length = length;
  }

  public PositionMessage(Project project, Url url,
      int line, int column, int length, String highlight, String path) {
    this(project, url, line, column, length, highlight, path,
        GutterIcons.ICON_EXECLINE);
  }

  class Positioner implements Runnable {
    private final PositionMessage message; /* synthetic field */
    private final EditorPane editorpane; /* synthetic field */
    private final boolean flag; /* synthetic field */
    private final Component component; /* synthetic field */
    private final Browser browser; /* synthetic field */
    private final FileNode filenode; /* synthetic field */

    public void run() {
      try {
        int offset = editorpane.calcCaretPosition(
            PositionMessage.getLine(message), PositionMessage.getColumn(message));
        editorpane.gotoOffset(offset);
        editorpane.moveCaretPosition(offset + PositionMessage.getLength(message));
        if (!flag && component != null) {
          component.requestFocus();
        } else {
          browser.navigateTo(filenode);
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }

    Positioner(
        PositionMessage message, EditorPane editorpane, boolean flag,
        Component component, Browser browser, FileNode filenode
        ) {
      this.message = message;
      this.editorpane = editorpane;
      this.flag = flag;
      this.component = component;
      this.browser = browser;
      this.filenode = filenode;
    }
  }
}

// JBX code:
//package com.borland.jbuilder.refactor.view;
//
//import com.borland.primetime.editor.EditorPane;
//import com.borland.primetime.ide.Browser;
//import com.borland.primetime.ide.GutterIcons;
//import com.borland.primetime.ide.Message;
//import com.borland.primetime.node.FileNode;
//import com.borland.primetime.node.Project;
//import com.borland.primetime.util.Strings;
//import com.borland.primetime.vfs.Url;
//import com.borland.primetime.viewer.AbstractTextNodeViewer;
//import java.awt.Component;
//import java.awt.Window;
//import javax.swing.Icon;
//import javax.swing.JComponent;
//import javax.swing.SwingUtilities;
//import javax.swing.text.Document;
//import javax.swing.text.JTextComponent;
//
//public class PositionMessage extends Message {
//
//  private Project a = null;
//  private int d = 0;
//  private int e = 0;
//  private int c = 0;
//  private Url b = null;
//
//  static int a(PositionMessage positionmessage) {
//    return positionmessage.d;
//  }
//
//  static Class a(String s) {
//    try {
//      return Class.forName(s);
//    }
//    catch (ClassNotFoundException classnotfoundexception) {
//      throw new NoClassDefFoundError(((Throwable) (classnotfoundexception)).getMessage());
//    }
//  }
//
//  public static String getHint(int i, int j, Url url) {
//    String s = Strings.format("({0}:{1} {2})", ((Object) (new Integer(i))), ((Object) (new Integer(j))), ((Object) (url.getName())));
//    return s;
//  }
//
//  public void displayResult(Browser browser, boolean flag) {
//    FileNode filenode = a.getNode(b);
//    try {
//      com.borland.primetime.node.Node node = browser.getActiveNode();
//      if (flag) {
//        browser.navigateTo(((com.borland.primetime.node.Node) (filenode)));
//      } else {
//        browser.setActiveNode(((com.borland.primetime.node.Node) (filenode)), false);
//      }
//      com.borland.primetime.ide.NodeViewer nodeviewer = browser.getViewerOfType(((com.borland.primetime.node.Node) (filenode)), com.borland.primetime.viewer.TextNodeViewer.class);
//      if (nodeviewer == null) {
//        nodeviewer = browser.getViewerOfType(((com.borland.primetime.node.Node) (filenode)), com.borland.jbuilder.viewer.ClassNodeViewer.class);
//      }
//      browser.setActiveViewer(((com.borland.primetime.node.Node) (filenode)), nodeviewer, flag);
//      if (nodeviewer instanceof AbstractTextNodeViewer) {
//        AbstractTextNodeViewer abstracttextnodeviewer = (AbstractTextNodeViewer)nodeviewer;
//        EditorPane editorpane = abstracttextnodeviewer.getEditor();
//        int i = editorpane.calcCaretPosition(c, e);
//        Component component = ((Window) (Browser.getActiveBrowser())).getFocusOwner();
//        ((JComponent) (editorpane)).requestFocus();
//        SwingUtilities.invokeLater(((Runnable) (new a((PositionMessage)this, editorpane, i, flag, component, browser, filenode))));
//      }
//    }
//    catch (Exception exception) {
//      ((Throwable) (exception)).printStackTrace();
//    }
//  }
//
//  public void selectAction(Browser browser) {
//    displayResult(browser, false);
//  }
//
//  public void messageAction(Browser browser) {
//    displayResult(browser, true);
//  }
//
//  public PositionMessage(Project project, Url url, int i, int j, int k, String s, String s1,
//      Icon icon) {
//    super(s, icon, s1);
//    a = project;
//    b = url;
//    c = i;
//    e = j;
//    d = k;
//  }
//
//  public PositionMessage(Project project, Url url, int i, int j, int k, String s, String s1) {
//    this(project, url, i, j, k, s, s1, GutterIcons.ICON_EXECLINE);
//  }
//
//  public PositionMessage(Project project, Url url, int i, int j, int k, String s) {
//    this(project, url, i, j, k, s, getHint(i, j, url), GutterIcons.ICON_EXECLINE);
//  }
//
//  public PositionMessage(Project project, Url url, int i, int j, int k, String s, Icon icon) {
//    this(project, url, i, j, k, s, getHint(i, j, url), icon);
//  }
//
//  // Unreferenced inner class com/borland/jbuilder/refactor/view/a
//  class a
//    implements Runnable {
//
//    private final boolean a; /* synthetic field */
//    private final int b; /* synthetic field */
//    private final FileNode c; /* synthetic field */
//    private final Component d; /* synthetic field */
//    private final EditorPane e; /* synthetic field */
//    private final Browser f; /* synthetic field */
//    private final PositionMessage g; /* synthetic field */
//
//    public void run() {
//      try {
//        e.gotoOffset(b);
//        int i = ((JTextComponent) (e)).getDocument().getLength();
//        ((JTextComponent) (e)).moveCaretPosition(Math.min(b + PositionMessage.a(g), i - 1));
//        if (!a && d != null) {
//          d.requestFocus();
//        } else
//        if (a) {
//          f.navigateMark(((com.borland.primetime.node.Node) (c)));
//        }
//        ((Component) (e)).repaint();
//      }
//      catch (Exception exception) {
//        ((Throwable) (exception)).printStackTrace();
//      }
//    }
//
//      a(EditorPane editorpane, int i, boolean flag, Component component, Browser browser, FileNode filenode) {
//      }
//  }
//
//}
