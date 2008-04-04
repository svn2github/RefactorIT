/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;


import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.ui.table.BinTable;
import net.sf.refactorit.ui.tree.BinTree;
import net.sf.refactorit.ui.tree.BinTreeCellRenderer;
import net.sf.refactorit.ui.tree.HtmlTableCellRenderer;
import net.sf.refactorit.ui.tree.UITreeNode;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;


/**
 * JTreeTable component,
 * by using a JTree as a renderer (and editor) for the cells in a
 * particular column in the JTable.
 *
 * @author Igor Malinin
 * @author Kirill Buhhalko
 * @author Anton Safonov
 */
public class JTreeTable extends BinTable {
  public static final int NONE_STYLE = 0;
  public static final int CHECKBOX_STYLE = 1;

  private int style;

  /** A subclass of JTree. */
  private TreeTableCellRenderer tree;

  public JTreeTable(final TreeTableModel model) {
    super();

    init(model, NONE_STYLE);
  }

  public JTreeTable(final TreeTableModel model, final int style) {
    super();

    init(model, style);
  }

  private void init(final TreeTableModel model, final int style) {
    this.style = style;

    // Create the tree. It will be used as a renderer and editor.
    tree = new TreeTableCellRenderer(model);
    tree.setParent(this);

    // Install a tableModel representing the visible rows in the tree.
    super.setModel(new TreeTableModelAdapter(model, tree));

    // Force the JTable and JTree to share their row selection models.
    final ListToTreeSelectionModelWrapper selectionWrapper
        = new ListToTreeSelectionModelWrapper();

    tree.setSelectionModel(selectionWrapper);
    setSelectionModel(selectionWrapper.getListSelectionModel());

    setDefaultRenderer(String.class, new HtmlTableCellRenderer());

    // Install the tree editor renderer and editor.
    setDefaultRenderer(TreeTableModel.class, tree);
    setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

    // No grid.
    setShowGrid(false);

    // No intercell spacing
    setIntercellSpacing(new Dimension(0, 0));

    if (style == CHECKBOX_STYLE) {
      addKeyListener(new KeyAdapter() {
        public void keyPressed(final KeyEvent ke) {
          final int kc = ke.getKeyCode();
          if (kc == KeyEvent.VK_SPACE) {
            ke.consume();

            final int[] rows = getSelectedRows();
            if (rows == null || rows.length != 1) {
              return;
            }
            final int row = rows[0];
            final TreePath path = getTree().getPathForRow(row);
            if (path == null) {
              return;
            }

            modifyCheckbox(path);
          }
        }
      });
    }

    addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent e) {
        if (getSelectedColumn() != 0) {
          return;
        }

        final Point mouseClickPoint = e.getPoint();
        final int row = rowAtPoint(mouseClickPoint);
        final TreePath path = getTree().getPathForRow(row);

        if (path == null) {
          return;
        }

        final Rectangle itemBounds = getTree().getPathBounds(path);
        final Rectangle cellBounds = getCellRect(row, 0, true);

        if (style == CHECKBOX_STYLE) {
          itemBounds.width = 16;
          itemBounds.height = 16;

          itemBounds.y = cellBounds.y + ((cellBounds.height - 16) / 2);

          if (itemBounds.contains(mouseClickPoint)) {
            modifyCheckbox(path);
          }
        }

        Icon openIcon = UIManager.getIcon("Tree.openIcon");
        Icon closedIcon = UIManager.getIcon("Tree.closedIcon");

        if(openIcon == null || closedIcon == null) {
          cellBounds.width = itemBounds.x - cellBounds.x;
        } else {
          if(getTree().isCollapsed(row)) {
            cellBounds.width = closedIcon.getIconWidth();
            cellBounds.x = itemBounds.x - cellBounds.width;
          } else {
            cellBounds.width = openIcon.getIconWidth();
            cellBounds.x = itemBounds.x - cellBounds.width;
          }
        }
        if (cellBounds.contains(mouseClickPoint)) {
          if (getTree().isCollapsed(row)) {
            getTree().expandPath(path);
          } else {
            getTree().collapsePath(path);
          }
          getTree().setSelectionPath(path);
        }
      }
    });

    optionsChanged(); // load default colors and font
  }

  public final TreeTableModel getTreeTableModel() {
    return ((TreeTableModelAdapter) getModel()).getModel();
  }

  final void modifyCheckbox(final TreePath path) {
    final ParentTreeTableNode node = (ParentTreeTableNode) path.
        getLastPathComponent();
    if (!node.isCheckBoxNeeded()) {
      return;
    }

    node.toggle();

    repaint();
  }

  /**
   * Sets the font for this component.
   * Needed to set the font for tree also.
   *
   * @see java.awt.Component#getFont
   */
  public final void setFont(final Font font) {
    super.setFont(font);

    if (tree != null) {
      tree.setFont(font);
    }

    invalidate();
//    validate();
//    repaint();
  }

  /*
    public void setBackground( Color c ) {
     super.setBackground(c);
     //if (tree != null) tree.setBackground( c );
   System.out.println("tree.setBackground( c ) c=" + c.toString());
    }

    public void setForeground( Color c ) {
     super.setForeground(c);
     //if (tree != null) tree.setForeground( c );
   System.out.println("tree.setForeground( c ) c=" + c.toString());
    }
   */

  // help us to skip jumping through cells by 'Tab' button
  public final boolean isManagingFocus() {
    return false;
  }

  /**
   * Overridden to message super and forward the method to the tree.
   * Since the tree is not actually in the component hierarchy it will
   * never receive this unless we forward it in this manner.
   */
  public final void updateUI() {
    super.updateUI();

    if (tree != null) {
      tree.updateUI(); // really needed?

      // Do this so that the editor is referencing the current renderer
      // from the tree. The renderer can potentially change each time
      // laf changes.
      setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
    }

    // Use the tree's default foreground and background colors in the
    // table.
//		LookAndFeel.installColorsAndFont(this,
//			"Tree.background", "Tree.foreground", "Tree.font");
  }

  public final void optionsChanged() {
    super.optionsChanged();

    // FIXME nodes with HTML text doesn't support selection coloring
    // at the moment - it is too CPU expensive to reparse the HTML all the time,
    // so fixed the color
    this.setSelectionBackground(Color.yellow);
    this.setSelectionForeground(Color.black);

    // Make the tree's cell renderer use the table's cell selection
    // colors.
    final TreeCellRenderer tcr = tree.getCellRenderer();
    if (tcr instanceof DefaultTreeCellRenderer) {
      final DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
//new Exception("Setting colors for JTreeTable renderer!").printStackTrace(System.err);

      dtcr.setFont(this.getFont());

      dtcr.setTextSelectionColor(this.getSelectionForeground());
      dtcr.setBackgroundSelectionColor(this.getSelectionBackground());

      dtcr.setTextNonSelectionColor(this.getForeground());
      dtcr.setBackgroundNonSelectionColor(this.getBackground());

      // JVM 1.2 has a bug that will cause an
      // exception to be thrown if the border selection color is null.
      final String version = System.getProperty("java.specification.version");
      if (version != null && !version.startsWith("1.2")) {
        dtcr.setBorderSelectionColor(null);
      } else {
        dtcr.setBorderSelectionColor(dtcr.getBackgroundSelectionColor());
      }
    }
  }

  /**
   * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
   * resize the editor. The UI currently uses different techniques to
   * paint the renderers and editors and overriding setBounds() below
   * is not the right thing to do for an editor. Returning -1 for the
   * editing row in this case, ensures the editor is never painted.
   */
  public final int getEditingRow() {
    return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1
        : editingRow;
  }

  /**
   * Returns the actual row that is editing (another method called <code>getEditingRow</code>
   * will always return -1).
   */
  final int realEditingRow() {
    return editingRow;
  }

  /**
   * This is overriden to invoke supers implementation, and then,
   * if the receiver is editing a Tree column, the editors bounds is
   * reset. The reason we have to do this is because JTable doesn't
   * think the table is being edited, as <code>getEditingRow</code> returns
   * -1, and therefore doesn't automaticly resize the editor for us.
   */
  public final void sizeColumnsToFit(final int resizingColumn) {
    super.sizeColumnsToFit(resizingColumn);
    if (getEditingColumn() != -1
        && getColumnClass(editingColumn) == TreeTableModel.class) {
      final Rectangle cellRect = getCellRect(realEditingRow(), getEditingColumn(), false);
      final Component component = getEditorComponent();
      component.setBounds(cellRect);
      component.validate();
    }
  }

  /**
   * Overridden to pass the new rowHeight to the tree.
   */
  public void setRowHeight(final int rowHeight) {
    //System.err.println("Setting row height to " + rowHeight);
    //new Exception().printStackTrace();
    super.setRowHeight(rowHeight);
    if (tree != null && tree.getRowHeight() != rowHeight) {
      tree.setRowHeight(getRowHeight());
    }
  }

  public void setRowHeight(int row, final int rowHeight) {
    //System.err.println("Setting row height to " + rowHeight);
    //new Exception().printStackTrace();
    super.setRowHeight(row, rowHeight);
    if (tree != null && tree.getRowHeight() != rowHeight) {
      tree.setRowHeight(getRowHeight());
    }
  }

  /**
   * Returns the tree that is being shared between the model.
   */
  public final JTree getTree() {
    return tree;
  }

  /**
   * Overriden to invoke repaint for the particular location if
   * the column contains the tree. This is done as the tree editor does
   * not fill the bounds of the cell, we need the renderer to paint
   * the tree in the background, and then draw the editor over it.
   */
  public final boolean editCellAt(final int row, final int column,
      final EventObject e) {
    final boolean retValue = super.editCellAt(row, column, e);
    if (retValue && getColumnClass(column) == TreeTableModel.class) {
      repaint(getCellRect(row, column, false));
    }

    return retValue;
  }

  /**
   * A TableCellRenderer that displays a JTree.
   */
  public final class TreeTableCellRenderer extends BinTree implements
      TableCellRenderer {
    /** Last table/tree row asked to renderer. */
    int visibleRow;
    private Container parent;

    /**
     * Border to draw around the tree, if this is non-null, it will
     * be painted.
     */
    Border highlightBorder;

    TreeTableCellRenderer(final TreeModel model) {
      super(model);

      if (style == NONE_STYLE) {
        setCellRenderer(new BinTreeCellRenderer());
      } else if (style == CHECKBOX_STYLE) {
        setCellRenderer(new CheckBoxCellRenderer());
      }
    }

    public final Container getParent() {
      return this.parent;
    }

    final void setParent(final Container parent) {
      this.parent = parent;
    }

    /**
     * Sets the row height of the tree, and forwards the row height to
     * the table.
     */
    public final void setRowHeight(final int rowHeight) {
      if (rowHeight > 0) {
        super.setRowHeight(rowHeight);
        if (JTreeTable.this != null
            && JTreeTable.this.getRowHeight(visibleRow) != rowHeight) {
          JTreeTable.this.setRowHeight(visibleRow, this.getRowHeight());
        }
      }
    }

    /**
     * This is overridden to set the height to match that of the JTable.
     */
    public final void setBounds(final int x, final int y, final int w,
        final int h) {
      super.setBounds(x, 0, w, JTreeTable.this.getHeight());
    }

    /**
     * Subclassed to translate the graphics such that the last visible
     * row will be drawn at 0,0.
     */
    public final void paint(final Graphics g) {
      g.translate(0, -visibleRow * this.getRowHeight());
      super.paint(g);
      // Draw the Table border if we have focus.
      if (highlightBorder != null) {
        highlightBorder.paintBorder(
            this, g, 0,
            visibleRow * this.getRowHeight(),
            this.getWidth(), this.getRowHeight());
      }
    }

    /**
     * TreeCellRenderer method. Overridden to update the visible row.
     */
    public final Component getTableCellRendererComponent(
        final JTable table,
        final Object value,
        final boolean isSelected,
        final boolean hasFocus,
        final int row, final int column
        ) {
      Color background;
      Color foreground;
      visibleRow = row;
      if (isSelected) {
        background = table.getSelectionBackground();
        foreground = table.getSelectionForeground();
      } else {
        background = table.getBackground();
        foreground = table.getForeground();
      }

      highlightBorder = null;
      if (realEditingRow() == row && getEditingColumn() == column) {
        background = UIManager.getColor("Table.focusCellBackground");
        foreground = UIManager.getColor("Table.focusCellForeground");
      } else if (hasFocus) {
        highlightBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
        /*
             if (isCellEditable(row, column)) {
         background = UIManager.getColor("Table.focusCellBackground");
         foreground = UIManager.getColor("Table.focusCellForeground");
             }
         */
      }

      this.setBackground(background);

      final TreeCellRenderer tcr = getCellRenderer();
      if (tcr instanceof DefaultTreeCellRenderer) {
        final DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
        if (isSelected) {
          dtcr.setTextSelectionColor(foreground);
          dtcr.setBackgroundSelectionColor(background);
        } else {
          dtcr.setTextNonSelectionColor(foreground);
          dtcr.setBackgroundNonSelectionColor(background);
        }
      }

      int lines = 1;

      if (!cache.containsKey(value)) {
        if (value instanceof UITreeNode) {
          JTreeTable binTreeTable = (JTreeTable) tree.getParent();
          TreeTableModel binTreeTableModel = binTreeTable.getTreeTableModel();
          if (binTreeTable.getColumnCount() >= 2) {
            Object valueAt2 = binTreeTableModel.getValueAt(value, 2);
            if (valueAt2 != null && valueAt2 instanceof String) {
              String str = (String) valueAt2;
              lines = StringUtil.count(str, "<br>")
                  + StringUtil.count(str, "<BR>") + 1;
              cache.put(value, new Integer(lines));
              this.setRowHeight(((JTable) tree.getParent()).getRowHeight()
                  * lines);
            } else {
              cache.put(value, new Integer(lines));
              this.setRowHeight(getRowHeight() * lines);
//              //this.setRowHeight(20);//((JTable) tree.getParent()).getRowHeight()
              //* lines);
            }
          }
        } else {
          cache.put(value, new Integer(lines));
          this.setRowHeight(getRowHeight() * lines);
//          //this.setRowHeight(((JTable) tree.getParent()).getRowHeight()*lines);
//          //this.setBackground(Color.red);
        }
      } else {
        lines = ((Integer) cache.get(value)).intValue();
        this.setRowHeight(((JTable) tree.getParent()).getRowHeight()*lines);
      }

      return this;
    }

    HashMap cache = new HashMap();
  }


  /**
   * An editor that can be used to edit the tree column. This extends
   * DefaultCellEditor and uses a JTextField (actually, TreeTableTextField)
   * to perform the actual editing.
   * <p>To support editing of the tree column we can not make the tree
   * editable. The reason this doesn't work is that you can not use
   * the same component for editing and renderering. The table may have
   * the need to paint cells, while a cell is being edited. If the same
   * component were used for the rendering and editing the component would
   * be moved around, and the contents would change. When editing, this
   * is undesirable, the contents of the text field must stay the same,
   * including the caret blinking, and selections persisting. For this
   * reason the editing is done via a TableCellEditor.
   * <p>Another interesting thing to be aware of is how tree positions
   * its render and editor. The render/editor is responsible for drawing the
   * icon indicating the type of node (leaf, branch...). The tree is
   * responsible for drawing any other indicators, perhaps an additional
   * +/- sign, or lines connecting the various nodes. So, the renderer
   * is positioned based on depth. On the other hand, table always makes
   * its editor fill the contents of the cell. To get the allusion
   * that the table cell editor is part of the tree, we don't want the
   * table cell editor to fill the cell bounds. We want it to be placed
   * in the same manner as tree places it editor, and have table message
   * the tree to paint any decorations the tree wants. Then, we would
   * only have to worry about the editing part. The approach taken
   * here is to determine where tree would place the editor, and to override
   * the <code>reshape</code> method in the JTextField component to
   * nudge the textfield to the location tree would place it. Since
   * JTreeTable will paint the tree behind the editor everything should
   * just work. So, that is what we are doing here. Determining of
   * the icon position will only work if the TreeCellRenderer is
   * an instance of DefaultTreeCellRenderer. If you need custom
   * TreeCellRenderers, that don't descend from DefaultTreeCellRenderer,
   * and you want to support editing in JTreeTable, you will have
   * to do something similiar.
   */
  public final class TreeTableCellEditor extends DefaultCellEditor {
    public TreeTableCellEditor() {
      super(new TreeTableTextField());
      // super(new JCheckBox());
    }

    /**
     * Overriden to determine an offset that tree would place the
     * editor at. The offset is determined from the
     * <code>getRowBounds</code> JTree method, and additionaly
     * from the icon DefaultTreeCellRenderer will use.
     * <p>The offset is then set on the TreeTableTextField component
     * created in the constructor, and returned.
     */
    public final Component getTableCellEditorComponent(
        final JTable table,
        final Object value,
        final boolean isSelected,
        final int row, final int col
        ) {
      super.getTableCellEditorComponent(table, value, isSelected, row, col);

      final JTree tree = getTree();

      final boolean rv = tree.isRootVisible();
      int offsetRow = rv ? row : row - 1;
      if (offsetRow < 0) { // strange, how row can be 0 when the root is not visible
        offsetRow = 0;
      }
      final Rectangle bounds = tree.getRowBounds(offsetRow);
      int offset = bounds.x;

      final TreeCellRenderer tcr = tree.getCellRenderer();
      if (tcr instanceof DefaultTreeCellRenderer) {
        final Object node = tree.getPathForRow(offsetRow).getLastPathComponent();

        final Icon icon;
        if (tree.getModel().isLeaf(node)) {
          icon = ((DefaultTreeCellRenderer) tcr).getLeafIcon();
        } else if (tree.isExpanded(offsetRow)) {
          icon = ((DefaultTreeCellRenderer) tcr).getOpenIcon();
        } else {
          icon = ((DefaultTreeCellRenderer) tcr).getClosedIcon();
        }

        if (icon != null) {
          offset += ((DefaultTreeCellRenderer) tcr)
              .getIconTextGap() + icon.getIconWidth();
        }
      }

      if (getComponent() instanceof TreeTableTextField) {
        ((TreeTableTextField) getComponent()).offset = offset;
      }

      //return component;
      return null;
    }

    /**
     * This is overriden to forward the event to the tree. This will
     * return true if the click count >= 3, or the event is null.
     */
//    public final boolean isCellEditable(final EventObject e) {
//      if (e instanceof MouseEvent) {
//        final MouseEvent me = (MouseEvent) e;
//
//        // If the modifiers are not 0 (or the left mouse button),
//        // tree may try and toggle the selection, and table
//        // will then try and toggle, resulting in the
//        // selection remaining the same. To avoid this, we
//        // only dispatch when the modifiers are 0 (or the left mouse
//        // button).
//
//        if (me.getModifiers() == 0 ||
//            me.getModifiers() == InputEvent.BUTTON1_MASK) {
//          for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
//            if (getColumnClass(counter) == TreeTableModel.class) {
//              final MouseEvent newME = new MouseEvent(JTreeTable.this.tree,
//                  me.getID(), me.getWhen(), me.getModifiers(),
//                  me.getX() - getCellRect(0, counter, true).x,
//                  me.getY(), me.getClickCount(), me.isPopupTrigger());
// // MiddleCommit
// //              System.out.println("Mouse: " + me.getPoint());
// //              System.out.println("newMouse: " + newME.getPoint());
// //              JTreeTable.this.tree.dispatchEvent(newME);
//
//       //       JTreeTable.this.tree.dispatchEvent(newME);
//              break;
//            }
//          }
//        }
//
//        return false; //( me.getClickCount() >= 3 );
//      }
//
//      return (e == null);
//    }
  }


  /**
   * Component used by TreeTableCellEditor. The only thing this does
   * is to override the <code>reshape</code> method, and to ALWAYS
   * make the x location be <code>offset</code>.
   */
  static final class TreeTableTextField extends JTextField {
    public int offset;

    public final void reshape(final int x, final int y, final int w,
        final int h) {
      final int newX = Math.max(x, offset);
      super.reshape(newX, y, w - (newX - x), h);
    }
  }


  /**
   * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
   * to listen for changes in the ListSelectionModel it maintains. Once
   * a change in the ListSelectionModel happens, the paths are updated
   * in the DefaultTreeSelectionModel.
   */
  final class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
    /** Set to true when we are updating the ListSelectionModel. */
    protected boolean updatingListSelectionModel;

    public ListToTreeSelectionModelWrapper() {
      super();

      getListSelectionModel()
          .addListSelectionListener(createListSelectionListener());
    }

    /**
     * Returns the list selection model. ListToTreeSelectionModelWrapper
     * listens for changes to this model and updates the selected paths
     * accordingly.
     */
    final ListSelectionModel getListSelectionModel() {
      return listSelectionModel;
    }

    /**
     * This is overridden to set <code>updatingListSelectionModel</code>
     * and message super. This is the only place DefaultTreeSelectionModel
     * alters the ListSelectionModel.
     */
    public final void resetRowSelection() {
      if (!updatingListSelectionModel) {
        updatingListSelectionModel = true;

        try {
          super.resetRowSelection();
        } finally {
          updatingListSelectionModel = false;
        }
      }

      // Notice how we don't message super if
      // updatingListSelectionModel is true. If
      // updatingListSelectionModel is true, it implies the
      // ListSelectionModel has already been updated and the
      // paths are the only thing that needs to be updated.
    }

    /**
     * Creates and returns an instance of ListSelectionHandler.
     */
    protected final ListSelectionListener createListSelectionListener() {
      return new ListSelectionHandler();
    }

    /**
     * If <code>updatingListSelectionModel</code> is false, this will
     * reset the selected paths from the selected rows in the list
     * selection model.
     */
    protected final void updateSelectedPathsFromSelectedRows() {
      if (!updatingListSelectionModel) {
        updatingListSelectionModel = true;

        try {
          // This is way expensive, ListSelectionModel needs an
          // enumerator for iterating.
          final int min = listSelectionModel.getMinSelectionIndex();
          final int max = listSelectionModel.getMaxSelectionIndex();

          this.clearSelection();

          if (min != -1 && max != -1) {
            for (int counter = min; counter <= max; counter++) {
              if (listSelectionModel.isSelectedIndex(counter)) {
                final TreePath selPath = tree.getPathForRow(counter);
                if (selPath != null) {
                  addSelectionPath(selPath);
                }
              }
            }
          }
        } finally {
          updatingListSelectionModel = false;
        }
      }
    }

    /**
     * Class responsible for calling updateSelectedPathsFromSelectedRows
     * when the selection of the list changse.
     */
    final class ListSelectionHandler implements ListSelectionListener {
      public final void valueChanged(final ListSelectionEvent e) {
        updateSelectedPathsFromSelectedRows();
      }
    }
  }
}
