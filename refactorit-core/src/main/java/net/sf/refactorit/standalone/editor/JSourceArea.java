/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone.editor;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.ItemByCoordinateFinder;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.standalone.JBrowserPanel;
import net.sf.refactorit.standalone.StandaloneAction;
import net.sf.refactorit.standalone.StandaloneRunContext;
import net.sf.refactorit.ui.module.ActionProxy;
import net.sf.refactorit.ui.module.RunContext;

import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.TextAction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class JSourceArea extends JEditorPane {
  /* *********************************** */
  /*        START OF MISTIC CODE         */
  /* *********************************** */
  /**
   * Constructor for JSourceArea
   */
  public JSourceArea(JBrowserPanel browser) {
    super();

    setEditable(false);

    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent event) {
        processMouseClick(event);
      }

      public void mouseReleased(MouseEvent event) {
        processMouseClick(event);
      }
    });

    optionsChanged(); // load defaults
  }

  /**
   * Handles contenxt-sensitive mouse events.
   */
  void processMouseClick(MouseEvent event) {
    if (!event.isPopupTrigger()) {
      return;
    }

    BinItem item = null;

    String text = this.getSelectedText();
    if (text != null && text.length() > 0) {
      int start = this.getSelectionStart();
      int end = this.getSelectionEnd();

      item = new BinSelection((CompilationUnit) getSource(), text, start, end);
    } else {
      SourceCoordinate coordinate = getCoordinate(event);
      if (coordinate != null) {
        ItemByCoordinateFinder finder
            = new ItemByCoordinateFinder((CompilationUnit) getSource());
        item = finder.findItemAt(coordinate);
      }
    }

    // Create popup menu for specified BinVariable
    if (item != null) {
      JPopupMenu popup = getActionsForItem(item,
          SwingUtilities.convertPoint(
              event.getComponent(), event.getPoint(),
              SwingUtilities.getWindowAncestor(this)));

      // Display list of available choices
      if (popup != null) {
        popup.show(this, event.getX(), event.getY());
        Point point = popup.getLocationOnScreen();
        Dimension size = popup.getSize();

        Rectangle oldRect = new Rectangle(
            point.x, point.y, size.width, size.height);

        // helper function to move oldRect completely
        // onto screen (desktop) if necessary
        Rectangle newRect = ensureRectIsVisible(oldRect);

        // rects differ, need moving
        if (!oldRect.equals(newRect)) {
          Window window = SwingUtilities.getWindowAncestor(popup);
          if (window != null) {
            window.setLocation(newRect.x, newRect.y);
          }
        }
      }
    }
  }

  // helper function to move a rectangle onto the screen
  public static Rectangle ensureRectIsVisible(Rectangle bounds) {
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    // -30 to compensate for windows taskbar
    size = new Dimension((int) size.getWidth(), (int) size.getHeight() - 30);

    return new Rectangle(
        Math.max(0, Math.min(size.width - bounds.width, bounds.x)),
        Math.max(0, Math.min(size.height - bounds.height, bounds.y)),
        bounds.width, bounds.height);
  }

  private JPopupMenu getActionsForItem(BinItem anItem, Point point) {
//    JPopupMenu result = new JPopupMenu();
//    java.util.List actions = ModuleManager.getActions( anItem );
//
//    for ( int i = 0 ; i < actions.size() ; ++i ) {
//      final RefactorItAction action = (RefactorItAction) actions.get( i );
//      JMenuItem item = new JMenuItem( action.getName() );
//
//
//      ActionListener actionListener=new AbstractStandaloneAction(action,anItem,point);
//
//      item.addActionListener( actionListener );
//
//      // Attach to menu
//      result.add(item);
//    }
//
//    if(anItem instanceof BinSelection) {
//      result.add( new RefCopyAction(((BinSelection)anItem).getText()) );
//    }

//    result.add( OldUndoManager.getInstance( new BrowserContext(browser.getProject(), browser), browser).getMenu() );

//    return (result.getSubElements().length > 0 ? result : null);

    final BinMember parentMem = anItem.getParentMember();
    final int type = (parentMem != null && parentMem.isPreprocessedSource())
        ? RunContext.JSP_CONTEXT : RunContext.JAVA_CONTEXT;

    RunContext context = null;

    if (anItem instanceof BinSelection) {
      BinItem addItem = ((BinSelection) anItem).startsWith();
      if (addItem != null) {
        context = new StandaloneRunContext(type,
            new Object[] {anItem, addItem}, point, false);
      }
    }

    if (context == null) {
      context = new StandaloneRunContext(type, anItem, point, false);
    }

    MenuBuilder builder
        = IDEController.getInstance().createMenuBuilder("", (char) 0, null, false);

    builder.buildContextMenu(context);

    if (anItem instanceof BinSelection) {
      builder.addAction(
          new RefCopyAction(((BinSelection) anItem).getText()), true);
    }
    return (JPopupMenu) builder.getMenu();

  }

  /**
   */
  private SourceCoordinate getCoordinate(MouseEvent event) {
    int shift = -(getGraphics().getFontMetrics().charWidth('a') / 2);
    Point point = event.getPoint();
    point.translate(shift, 0);
    return getCoordinate(viewToModel(point));
  }

  /**
   * FIXME: When does this method ever return null?
   */
  private SourceCoordinate getCoordinate(int location) {
    Element root = (getSourceDocument() != null
        ? getSourceDocument().getDefaultRootElement() : null);

    // Scan through lines
    search:
        for (int pos = 0, max = (root != null ? root.getElementCount() : 0);
        pos < max; pos++) {
      Element element = root.getElement(pos);

      // Check if the locations resides withing this element
      if ((element.getStartOffset() <= location
          && element.getEndOffset() > location) == false) {
        continue search;
      }

      // Reached here -> so this must be the element we're interested in
      int line = pos + 1;
      int column = location - element.getStartOffset() + 1;

      // Construct response
      return new SourceCoordinate(line, column);
    }

    // Not found
    return null;
  }

  private SourceHolder getSource() {
    return getSourceDocument().getSource();
  }

  /* *********************************** */
  /*         END OF MISTIC CODE          */
  /* *********************************** */

  public void setFont(Font font) {
    super.setFont(font);
    SourceDocument doc = getSourceDocument();
    if (doc != null) {
      doc.setFont(font);
    }
  }

  public void paint(Graphics g) {
    super.paint(g);

//    FontMetrics fm = g.getFontMetrics();
//System.err.println("area font: " + area.getFont());
//    int fh = fm.getHeight();
//
//System.err.println("lines: " + (dim.height / fh));
//
  }

  public void optionsChanged() {
    setFont(Font.decode(GlobalOptions.getOption("source.font")));
    setBackground(Color.decode(GlobalOptions.getOption("source.background")));
    setForeground(Color.decode(GlobalOptions.getOption("source.foreground")));

    // let's notify rows header that we changed
    dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));

    // FIXME update highlight too
  }

  public SourceDocument getSourceDocument() {
    return (SourceDocument) getDocument();
  }

  /**
   * @see JTextPane#createDefaultEditorKit()
   */
  protected EditorKit createDefaultEditorKit() {
    return new SourceEditorKit();
  }

  static class RefCopyAction extends TextAction implements StandaloneAction {
    private String textToCopy;

    public RefCopyAction(String textToCopy) {
      super("Copy to clipboard");
      this.textToCopy = textToCopy;
    }

    /** Invoked when an action occurs.
     *
     */
    public void actionPerformed(ActionEvent e) {
      String normalText = StringUtil.replace(textToCopy, "\r", "");
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new
          StringSelection(normalText), null);
    }

    public String getName() {
      return "Copy to clipboard";
    }

    public ActionProxy getAction() {
      return null;
    }

  }
}
