/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.dependency;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.FileExtensionFilter;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.png.PngEncoder;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.PackageNameIndexer;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.graph.BinClassificator;
import net.sf.refactorit.ui.graph.BinEdge;
import net.sf.refactorit.ui.graph.BinNode;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.dependencies.DependenciesAction;
import net.sf.refactorit.ui.module.dependencies.DependencyLoopsAction;
import net.sf.refactorit.ui.panel.BinPaneToolBar;
import net.sf.refactorit.ui.panel.ResultArea;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.GLPanel;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.graphelements.TGForEachNode;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class GraphPanel extends GLPanel {
  private static final ImageIcon saveImage
      = ResourceUtil.getIcon(GraphPanel.class, "save.gif");

  RefactorItContext context;
  private ResultArea holder;

  JCheckBox moreDetailsButton;
  JCheckBox showLoopsButton;
  private JButton saveToFile;

  private HashMap nodePoints = new HashMap();

  private final Color normalBackground;

  public GraphPanel(final RefactorItContext context, final List target) {
    super();
    this.normalBackground = TGPanel.BACK_COLOR;

    this.context = context;

    if (target.size() == 1 && this.moreDetailsButton != null
        && !(target.get(0) instanceof Project)) {
      this.moreDetailsButton.setSelected(true);
    }

    rebuild(target);

    // FIXME: doesn't work - someone consumes it before...
    try {
      KeyStroke deleteStroke = KeyStroke.getKeyStroke("DELETE");
      registerKeyboardAction(new DeleteActionListener(),
          deleteStroke,
          WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      registerKeyboardAction(new DeleteActionListener(),
          deleteStroke,
          WHEN_FOCUSED);
      addKeyListener(new DeleteKeyListener());
    } catch (Exception e) {
      // failed to register, let's live without
    }
  }

  private class DeleteActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      System.err.println("e: " + e);
      if (tgPanel.getSelect() != null) {
        tgPanel.deleteNode(tgPanel.getSelect());
      }
    }
  }


  private class DeleteKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      System.err.println("ke: " + ke);
      switch (ke.getKeyCode()) {
        case KeyEvent.VK_DELETE:
          if (ke.isMetaDown() || ke.isShiftDown() || ke.isAltDown()
              || ke.isControlDown()) {
            return;
          }
          ke.consume();
          if (tgPanel.getSelect() != null) {
            tgPanel.deleteNode(tgPanel.getSelect());
          }
          break;
      }
    }
  }

  public void nodeDeleted(Node node) {
    if (node instanceof BinNode) {
      BinItem item = ((BinNode) node).getBin();
      // TODO: keep separate deleted targets list
      List target = getTarget();
      if (target.remove(item)) {
        setTarget(target);
      }
    }
  }

  public void saveNodePoints() {
    getTGPanel().getGES().forAllNodes(new TGForEachNode() {
      public void forEachNode(Node n) {
        if (n instanceof BinNode) {
          nodePoints.put(((BinNode) n).detouchFromClassmodel(), n.getLocation());
        }
      }
    });

    // Gray out background
    getTGPanel().setBackColor(new Color(230, 230, 230));
  }

  private void restoreNodePoints() {
    final HashMap restoredOldBins = new HashMap(nodePoints.size());
    Iterator oldBins = nodePoints.keySet().iterator();
    while (oldBins.hasNext()) {
      BinItemReference oldBin = (BinItemReference) oldBins.next();
      Object o = oldBin.restore(context.getProject());
      if (o != null) { // bin is still in the classmodel
        restoredOldBins.put(o, nodePoints.get(oldBin));
      }
    }

    getTGPanel().getGES().forAllNodes(new TGForEachNode() {
      public void forEachNode(Node n) {
        if (n instanceof BinNode) {
          Point point = (Point) restoredOldBins.get(((BinNode) n).getBin());
          if (point != null) {
            n.setLocation(point);
            getTGPanel().updateDrawPos(n);
          }
        }
      }
    });

    nodePoints.clear();

    getTGPanel().setBackColor(this.normalBackground);
  }


  public synchronized void rebuild(final List target) {
    saveNodePoints();
    boolean freeze = getTGPanel().isFreeze();
    if (!freeze) {
      getTGPanel().setFreeze(true);
    }

    getTGPanel().clearAll();

    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          new DependencyGraphBuilder(GraphPanel.this, target,
              context.getProject(), moreDetailsButton.isSelected());
        }
      }, target, false);
    } catch (SearchingInterruptedException ex) {
    }

    if (freeze) {
      getTGPanel().setFreeze(false); // unfreeze for a second to layout
      try {
        Thread.sleep(1000);
      } catch (InterruptedException x) {
      }
    }
    getTGPanel().setFreeze(freeze);

    restoreNodePoints();
    if(getTGPanel().isHighlightEnabled()){
      buildLoops();
    }
  }

  public void setHolder(ResultArea holder) {
    this.holder = holder;
  }

  public void buildLoops() {
    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          List loops = new GLGraphTraverser(getTGPanel()).findLoops();
          getTGPanel().setGLoops(loops);
        }
      }, true);
    } catch (SearchingInterruptedException e){
    }
  }

  public List getTarget() {
    return (List) this.holder.getTargetBinObjectReference()
        .restore(this.context.getProject());
  }

  public void setTarget(List target) {
    this.holder.setTargetBinObject(target);
  }

  /** overrides */
  public void randomGraph() throws TGException {
  }

  public JPopupMenu getExtraPanelPopup() {

    if(getTGPanel().isHighlightEnabled()) {
      List loops =  getTGPanel().getGLoops();
      if(loops!=null && loops.size()>0) {
        final JPopupMenu menu = ResultArea.createPopupForBinItem(
            null, this, context, null);
        Object[] obj = new Object[] {
            loops, new Integer(0)};
        RefactorItAction action = ModuleManager.getAction(
            ClassUtil.getClassesArray(obj), DependencyLoopsAction.KEY, false);

        List actions = new ArrayList();
        actions.add(action);

        ResultArea.addActionsToPopup(menu, actions, context, obj);

        return menu;
      }
    }
    return null;
  }

  public JPopupMenu getExtraNodePopup(final Node node) {

    final BinItem bin = ((BinNode) node).getBin();

    final JPopupMenu menu = ResultArea.createPopupForBinItem(
        bin, this, context, null);

    if (bin == null) {
      return menu;
    }

    menu.addSeparator();

    final JMenuItem item1 = new JMenuItem("Add Dependable Items (+?)   ");
    new Thread(new Runnable() {
      public void run() {
        List invocations;
        if (bin instanceof BinPackage) {
          ManagingIndexer supervisor = new ManagingIndexer();
          new PackageNameIndexer(supervisor, (BinPackage) bin);
          supervisor.callVisit(((BinPackage) bin).getProject(), false);
          invocations = supervisor.getInvocations();
        } else {
          invocations = Finder.getInvocations(bin);
        }
        List newTargets = extractTargets(bin, invocations, true);
        newTargets.removeAll(GraphPanel.this.getTarget());
        item1.setText(item1.getText().replaceFirst("[?]", "" + newTargets.size()));
      }
    }).start();
    item1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        List invocations;
        if (bin instanceof BinPackage) {
          ManagingIndexer supervisor = new ManagingIndexer();
          new PackageNameIndexer(supervisor, (BinPackage) bin);
          supervisor.callVisit(((BinPackage) bin).getProject(), false);
          invocations = supervisor.getInvocations();
        } else {
          invocations = Finder.getInvocations(bin);
        }

        List target = getTarget();
        int before = target.size();
        target = CollectionUtil.addAllNew(target, extractTargets(bin, invocations, true));
        if (target.size() != before) {
          setTarget(target);
          rebuild(target);
        }
      }

    });
    menu.add(item1);

    final JMenuItem item2 = new JMenuItem("Add Depends On Items (+?)   ");
    new Thread(new Runnable() {
      public void run() {
        List invocations = DependenciesModel
            .collectDependencies(bin, Project.getProjectFor(bin));
        List newTargets = extractTargets(bin, invocations, false);
        newTargets.removeAll(getTarget());
        item2.setText(item2.getText().replaceFirst("[?]", "" + newTargets.size()));
      }
    }).start();
    item2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        List invocations = DependenciesModel
            .collectDependencies(bin, Project.getProjectFor(bin));
        List target = getTarget();
        int before = target.size();
        target = CollectionUtil.addAllNew(target, extractTargets(bin, invocations, false));
        if (target.size() != before) {
          setTarget(target);
          rebuild(target);
        }
      }

    });
    menu.add(item2);

    if (bin instanceof BinCIType) {
      List newTargets = new ArrayList();
      newTargets.addAll(
          extractBinCITypes(((BinCIType) bin).getTypeRef().getAllSupertypes()));
      newTargets.removeAll(getTarget());
      JMenuItem item = new JMenuItem("Add Super Types (+" + newTargets.size() + ")");
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Collection types = ((BinCIType) bin).getTypeRef().getAllSupertypes();
          List target = getTarget();
          int before = target.size();
          target = CollectionUtil.addAllNew(target, extractBinCITypes(types));
          if (target.size() != before) {
            setTarget(target);
            rebuild(target);
          }
        }
      });
      menu.add(item);

      newTargets.clear();
      newTargets.addAll(
          extractBinCITypes(((BinCIType) bin).getTypeRef().getAllSubclasses()));
      newTargets.removeAll(getTarget());
      item = new JMenuItem("Add Sub Types (+" + newTargets.size() + ")");
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          List types = ((BinCIType) bin).getTypeRef().getAllSubclasses();
          List target = getTarget();
          int before = target.size();
          target = CollectionUtil.addAllNew(target, extractBinCITypes(types));
          if (target.size() != before) {
            setTarget(target);
            rebuild(target);
          }
        }
      });
      menu.add(item);
    }

    if (bin instanceof BinMember && ((BinMember) bin).getOwner() != null) {
      JMenuItem item = new JMenuItem("Add Owner");
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          List target = getTarget();
          int before = target.size();
          target = CollectionUtil.addNew(target, ((BinMember) bin).getOwner().getBinType());
          if (target.size() != before) {
            setTarget(target);
            rebuild(target);
          }
        }
      });
      menu.add(item);
    }



    if(node.isHighlighted()){
      List highlighted = new ArrayList(getTGPanel().getHighlightedLoops());
      Object[] obj = new Object[] {highlighted, (BinNode)node};

      RefactorItAction action = ModuleManager.getAction(
          ClassUtil.getClassesArray(obj), DependencyLoopsAction.KEY, false);

      List actions = new ArrayList();
      actions.add(action);

      ResultArea.addActionsToPopup(menu, actions, context, obj);

      //((JMenuItem) menu.getComponents()[1]).setText("Show Dependency Cycles");
	    /*// node right-click popup menu item
	    JMenuItem nodeItem = new JMenuItem("Show Dependency Cycles");
	    ActionListener showNodeCyclesAction = new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
          DependencyLoopsAction loopsAction = new DependencyLoopsAction();
          List highlighted = new ArrayList(getTGPanel().getHighlightedLoops());
          loopsAction.run(context, highlighted, (BinNode)node);
	      }
	    };
	    nodeItem.addActionListener(showNodeCyclesAction);
	    menu.add(nodeItem);*/
    }

    return menu;
  }

  List extractBinCITypes(Collection typeRefs) {
    ArrayList result = new ArrayList(typeRefs.size());
    for (Iterator types = typeRefs.iterator(); types.hasNext(); ) {
      result.add(((BinTypeRef) types.next()).getBinType());
    }
    return result;
  }

  public List extractTargets(BinItem bin, List invocations,
      boolean checkWhere) {
    List result = new ArrayList();

    int type = BinClassificator.getItemType(bin, true);
    for (Iterator it = invocations.iterator(); it.hasNext(); ) {
      InvocationData data = (InvocationData) it.next();
      BinItemVisitable item;
      if (checkWhere) {
        final Object where = data.getWhere();
        if (where instanceof BinTypeRef) {
          item = ((BinTypeRef) where).getBinType();
        } else if (where instanceof CompilationUnit) {
          item = ((CompilationUnit) where).getPackage();
        } else {
          item = (BinItem) where;
        }
      } else {
        item = data.getWhat();
      } while (item != null && BinClassificator.getItemType(item, true) > type) {
        item = BinClassificator.getParent(item);
      }

      if (item != null && BinClassificator.getItemType(item, true) == type) {
        if (DependencyGraphBuilder.isAllowedPackage(getPackage(item))) {
          result.add(item);
        }
      }
    }

    return result;
  }

  private BinPackage getPackage(BinItemVisitable item) {
    while (item != null && !(item instanceof BinPackage)) {
      item = BinClassificator.getParent(item);
    }

    return (BinPackage) item;
  }

  public JPopupMenu getExtraEdgePopup(final Edge edge) {
  	JPopupMenu menu = new JPopupMenu();

  	if (((BinEdge) edge).isDependency()) {
      Object obj;
      boolean errors = false;

      if (((BinEdge) edge).isBidirectional()) {
        BinItem binFrom = ((BinEdge) edge).getBinFrom();
        BinItem binTo = ((BinEdge) edge).getBinTo();
        if (binFrom == null || binTo == null) {
          errors = true;
        }
        obj = new Object[] {binFrom, binTo};
      } else {
        obj = ((BinEdge) edge).getBinFrom();
        if (obj == null) {
        	errors = true;
        }
      }

      if(!errors) {
	      RefactorItAction action = ModuleManager.getAction(
	          ClassUtil.getClassesArray(obj), DependenciesAction.KEY, false);

	      List actions = new ArrayList();
	      actions.add(action);

	      if (((BinEdge) edge).isBidirectional()) {
	        obj = new Object[] {
	            ((BinEdge) edge).getBinFrom(), ((BinEdge) edge).getBinTo(),
	            new ResultFilter(obj)};
	      } else {
	        obj = new Object[] {
	            ((BinEdge) edge).getBinFrom(),
	            new ResultFilter(((BinEdge) edge).getBinTo())};
	      }

	      ResultArea.addActionsToPopup(menu, actions, context, obj);

	      ((JMenuItem) menu.getComponents()[0]).setText("Show Dependency Details");
      }
    }

    if(edge.isHighlighted()) {
      // edge right-click popup menu item
      List highlighted = new ArrayList(getTGPanel().getHighlightedLoops());
      Object[] obj = new Object[] {highlighted, (BinNode)edge.from};

      RefactorItAction action = ModuleManager.getAction(
          ClassUtil.getClassesArray(obj), DependencyLoopsAction.KEY, false);

      List actions = new ArrayList();
      actions.add(action);

      ResultArea.addActionsToPopup(menu, actions, context, obj);
      //((JMenuItem) menu.getComponents()[1]).setText("Show Dependency Cycles");
    }


  	/*if(edge.isHighlighted()) {
	    // edge right-click popup menu item
      RefactorItAction action = ModuleManager.getAction();
	    JMenuItem edgeItem = new JMenuItem("Show Dependency Cycles");
	    ActionListener showEdgeCyclesAction = new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
          List highlighted = new ArrayList(getTGPanel().getHighlightedLoops());
          DependencyLoopsAction loopsAction = new DependencyLoopsAction();
          loopsAction.run(context, highlighted, (BinNode)edge.from);
	      }
	    };
	    edgeItem.addActionListener(showEdgeCyclesAction);
	    menu.add(edgeItem);
  	}*/

    return menu;
  }

  public Component[] getExtraControls() {
    if (moreDetailsButton == null) {
      moreDetailsButton = new JCheckBox("More Details");
      moreDetailsButton.setMargin(new Insets(0, 2, 0, 2));
      moreDetailsButton.setSelected(false);
      moreDetailsButton.setToolTipText(
          "Shows one more detail level if possible, e.g. shows members also if you were analysing class dependency initially");
      this.moreDetailsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          rebuild(getTarget());
        }
      });
    }

    if (showLoopsButton == null) {
    	showLoopsButton = new JCheckBox("Show cycles");
    	showLoopsButton.setMargin(new Insets(0, 2, 0, 2));
    	showLoopsButton.setSelected(getTGPanel().isHighlightEnabled());
    	showLoopsButton.setToolTipText(
        "Highlights dependency cycles which include graph element (node or edge) pointed by mouse cursor");
    this.showLoopsButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        getTGPanel().setHighlightEnabled(showLoopsButton.isSelected());
        if(showLoopsButton.isSelected() &&
            getTGPanel().getGLoops() == null) {
            buildLoops();
            if(getTGPanel().getGLoops() == null) {
              showLoopsButton.setSelected(false);
            }
        }
      }
    });
    }

    if (saveToFile == null) {
      saveToFile = new JButton(saveImage);
      BinPaneToolBar.tuneSmallButtonBorder(saveToFile);
      saveToFile.setMargin(new Insets(0, 2, 0, 2));
      saveToFile.setToolTipText(
          "Save current nodes and edges layout to the graphics file");
      this.saveToFile.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser chooser = new JFileChooser();
          chooser.setMultiSelectionEnabled(false);
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          FileFilter pngFilter = new FileExtensionFilter(".png", "PNG (*.PNG)");
          chooser.setFileFilter(pngFilter);
          chooser.resetChoosableFileFilters();
          chooser.addChoosableFileFilter(pngFilter);

          chooser.setCurrentDirectory(GlobalOptions.getLastDirectory());

          chooser.setDialogType(JFileChooser.SAVE_DIALOG);
          int rc = RitDialog.showFileDialog(
              IDEController.getInstance().createProjectContext(), chooser);

          GlobalOptions.setLastDirectory(chooser.getCurrentDirectory());

          if (rc != JFileChooser.APPROVE_OPTION) {
            return;
          }

//          TGPoint2D topLeft = tgPanel.getTopLeftDraw();
//          TGPoint2D bottomRight = tgPanel.getBottomRightDraw();

          Dimension size = tgPanel.getSize();
          BufferedImage myImage = new BufferedImage(
              (int) size.getWidth(), (int) size.getHeight(),
              BufferedImage.TYPE_INT_RGB);
          Graphics2D g2 = myImage.createGraphics();
          tgPanel.paint(g2);

          try {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".png")) {
              path += ".png";
            }
            PngEncoder.encode(myImage, path);
//            OutputStream out = new FileOutputStream(file);
//            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//            encoder.encode(myImage);
//            out.close();
          } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
          }
        }
      });
    }

    return new Component[] {moreDetailsButton, showLoopsButton, saveToFile};
  }

  // overrides GLPanel handleGraphMouseEvent()
  public void handleGraphMouseEvent(MouseEvent e) {
    if(e.getClickCount() == 2) {
      Node node = getTGPanel().getSelect();
      if(node instanceof BinNode) {
        BinItem item = ((BinNode)node).getBin();
        RefactorItAction action =
          ModuleManager.getAction(item.getClass(),"refactorit.action.GoToAction");
        if(action != null) {
          action.run(IDEController.getInstance().createProjectContext(), item);
        }
      }
    }
  }
}
