/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.hierarchy;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.loader.ProjectChangedListener;
import net.sf.refactorit.netbeans.common.BinItemNotFoundException;
import net.sf.refactorit.netbeans.common.ElementInfo;
import net.sf.refactorit.netbeans.common.NBContext;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.tree.JTypeInfoPanel;
import net.sf.refactorit.ui.tree.LockObserver;

import org.apache.log4j.Logger;

import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;


/**
 * Hierarchy explorer dockable window impl.
 * Obtains active node and shows TypeInfoPanel according with this node.
 *
 * @author Vladislav Vislogubov
 * @author Tonis Vaga (redesign and impl for multiple instances)
 * TODO: multiple instances should have same propertyChangedListener event listener.
 */
public class HierarchyView extends TopComponent implements
    ProjectChangedListener, LockObserver {
  
  private static final Logger log = Logger.getLogger(HierarchyView.class);
  
  private static final String name = "Class Details";
  private static final Image icon
      = ResourceUtil.getImage(net.sf.refactorit.netbeans.common.NBIcons.class,
          "hierarchy.gif");
//  private static HierarchyView instance = null;

  boolean initialized;

  private Node[] lastActivated;

  private final JPanel emptyPanel = new JPanel();
  private final JButton startAnalyze = new JButton("Activate View");

  private PropertyChangeListener listener = null;
  private BinMember currentBin = null;
  private JTypeInfoPanel typeInfo = null;
  private boolean empty = true;
  private BinItemReference binRef = null;
  private JLabel selectMsg = new JLabel(
      "Select node or source to see hierarchy for.");

  protected HierarchyView() {
    super();

    saveLastActivatedNodes();

    setIcon(icon);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // Empty panel
    emptyPanel.setBackground(Color.white);
    emptyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    selectMsg.setHorizontalTextPosition(JLabel.CENTER);
    selectMsg.setBackground(Color.white);

    startAnalyze.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!RefactorITLock.lock()) {
          return;
        }
        try {
          activateInitalizedState();
        } catch (Exception ex) {
          // don't let exceptions fall out of RIT
          log.warn(ex.getMessage(), ex);
        } finally {
          RefactorITLock.unlock();
        }
        //checkInitialize();
        //saveLastActivatedNodes();
        //reload();
      }
    });

    //add(emptyPanel);
  }

  public int getPersistenceType() {
    return 0; //TopComponent.PERSISTENCE_ALWAYS;
  }

  protected String preferredID() {
    return "ClassDetails";
  }

//  public List availableModes (List modes) {
//System.err.println("available: " + modes);
//      return modes;
//  }

  boolean locked() {
    if (typeInfo == null) {
      return false;
    }
    return!typeInfo.isNotLocked();
  }

  /**
   * Shows TypeInfoPanel. Should be called after activateUnitializedState.
   */
  protected void activateInitalizedState() {
    if (!checkInitialize()) {
      return;
    }
    //saveLastActivatedNodes();
    reload();

    // to be sure
    validate();
    repaint();
  }

  protected void clearComponents() {
    this.removeAll();
    emptyPanel.removeAll();

    if (typeInfo != null) {
      typeInfo.removeAll();
    }

    typeInfo = null;
  }

  /**
   * Removes typeInfo from panel and shows activate button.
   */
  protected void activateUninitializedState() {
    this.removeAll();
    initialized = false;
    empty = true;
    //System.out.println(
    //   "activateUninitialState: parent, bounds: "
    //     + getParent()
    //    + ","
    //    + getBounds());
    //System.out.println("isOpened=" + isOpened());

//    setIcon(icon);
//    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//
//    // Empty panel
//    emptyPanel.setBackground(Color.white);
//    emptyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//
//    l.setHorizontalTextPosition(JLabel.CENTER);
//    l.setBackground(Color.white);

//    Dimension dim = new Dimension(250, 500);
//    emptyPanel.setMinimumSize(dim);
//    emptyPanel.setPreferredSize(dim);
    emptyPanel.removeAll();
    emptyPanel.add(startAnalyze);
    emptyPanel.add(selectMsg);
    this.add(emptyPanel);
    this.validate();
    this.repaint();
  }

  public void componentClosed() {
//    System.out.println("componentClosed()");
    myComponentClosed();
  }

  public void componentOpened() {
//    System.out.println("componentOpened()");
    myComponentOpened();
  }

  protected void openNotify() {
//    System.out.println("openNotify()");
    myComponentOpened();
  }

  protected void closeNotify() {
//    System.out.println("closeNotify()");
    myComponentClosed();
  }

  /**
   * Workaround. Seems that S1Studio 4.1 doesnt call componentOpened,
   *  componentClosed methods and calls openNotify and closeNotify instead
   *
   */
  protected void myComponentClosed() {
    //System.out.println("componentClosed()");
    clearComponents();
    if (IDEController.getInstance().getActiveProject() != null) {
      IDEController.getInstance().getActiveProject().getProjectLoader().removeProjectChangedListener(this);
    }

    //instance = null;
  }

  /*
    protected void componentShowing() {
      System.out.println("componentShowing");
    }
   */

  protected void myComponentOpened() {
//    if (instance == null) {
//      System.out.println("createing instance");
//      instance = create();
//    }
    //System.out.println("componentOpen()");
    activateUninitializedState();

    //didn't work
//    if( isValidProject() ) {
//      System.out.println("valid project!!");
//      initialized=true;
//      clearComponents();
//      saveLastActivatedNodes();
//      emptyPanel.add(selectMsg);
//      this.add(emptyPanel);
//      validate();
//      repaint();
//      reload();
//    } else {
//      System.out.println("project not valid yet!");
//      activateUninitializedState();
//    }
  }

  public boolean checkInitialize() {
    //System.out.println("checkInitialized!!");
    //System.out.println("parent=" + getParent());
    //System.out.println("isOpened=" + isOpened());
    if (!initialized) {
      if (!IDEController.getInstance().ensureProject()) {
        return false;
      }

      // for example the case when first time ever loading bombs
      if (IDEController.getInstance().getActiveProject() == null) {
        return false;
      }

      IDEController.getInstance().getActiveProject().getProjectLoader().addProjectChangedListener(this);
      initialized = true;
      emptyPanel.remove(startAnalyze);
      emptyPanel.repaint();
      if (getParent() == null) {
        //System.out.println("checkInitialize bound are:" + getBounds());
      }
    }
    return true;
  }

  public static HierarchyView create() {
    HierarchyView instance = new HierarchyView();
    //System.out.println("created instance:parent=" + instance.getParent());
    return instance;
  }

  /*
    public HierarchyView getInstance() {
//    if (HierarchyView.instance == null) {
//      System.out.println("getInstance:instance==null!!");
//      return null;
//    } else {
        //System.out.println("isOpened=" + isOpened());
        Container parent = getParent();
        //System.out.println("parent=" + parent);
        if (parent != null) {
          //System.out.println("parent bounds=" + parent.getBounds());
        }
        if ( getTypeInfoPanel().isNotLocked()) {
          saveLastActivatedNodes();
          reload();
        }
      return this;
    }
   */

  public String getName() {
    return name;
  }

  public void addNotify() {
    //System.out.println("!!! addNotify");
    super.addNotify();

    TopComponent.getRegistry().addPropertyChangeListener(this.getListener());
  }

  public void removeNotify() {
    //System.out.println("removeNofify");
    super.removeNotify();

    if (listener != null) {
      TopComponent.getRegistry().removePropertyChangeListener(listener);
    }

    listener = null;
  }

  private PropertyChangeListener getListener() {
    if (listener != null) {
      return listener;
    }

    listener = new ActiveNodesChangedListener() {
      protected void activeNodesChanged() {
        if (locked()) {
          return;
        }

        Runnable runnable = new Runnable() {
          public void run() {
            //System.out.println("Property changed listener");
            if (initialized) {
              reload();
            } else {
              //System.out.println("PropChanged:not initialized!");
            }

            /*if (HierarchyView.instance.getTypeInfoPanel().isNotLocked()) {
              reload();
                   }*/
          }
        };
        // hanged once
        //SwingUtil.invokeInEdtUnderNetBeans(runnable);
        runnable.run();
        /*
          System.out.println( "****** " + count++ + ") " + SwingUtilities.isEventDispatchThread() );
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              reload();
            }
          });
         */
      }
    };

    return listener;
  }

  private void saveLastActivatedNodes() {
    TopComponent tc = TopComponent.getRegistry().getActivated();
    if ((tc != null) && !tc.equals(this)) {
      Node[] node = tc.getActivatedNodes();
      if (node != null && node.length != 0 && node[0] != null) {
        lastActivated = node;
      }
    }
  }

  /**
   * Reload is always called in another thread so it will not be blocking
   */
  public void reload() {
    //System.out.println("reload");
    if (IDEController.getInstance().getActiveProject() == null) {
      //System.out.println("getProject()==null");
      return;
    } else {
      if (!initialized) {
        if (!checkInitialize()) {
          return;
        }
      }
    }

    //System.out.println("reload():initialized=="+initialized);
    if (!initialized) {
      //System.out.println("not initialized!!");
      return;
    }

    if (!locked()) {
      saveLastActivatedNodes();
    }

    Thread.yield();

    if ((lastActivated == null)
        || (lastActivated.length == 0)
        || (lastActivated[0] == null)) {
      //System.out.println("lastActivate==null");
      return;
    }
    if (getParent() == null && !isOpened()) {
      // It happens sometimes in NetBeans/S1S, can't do anything
      //System.out.println("parent==null && !isOpened!!");
      return;
//      this.removeAll();
//      this.add(new JLabel("parent:null"));
//      validate();
//      repaint();
//      return;
    }

    BinMember bin = null;
    try {
      bin = new ElementInfo(lastActivated[0]).getBinMember();
    } catch (BinItemNotFoundException ignoreBecauseBinIsNullAnyway) {
      //System.out.println("BinItemNotFoundException");
    }

    if (bin == null) {
      // IDEA: perhaps we should show some non-intrusive message on the panel in here?
      // For example, "unknown class XXX -- please hit rebuild to update RefactorIT's cache" or smth...
      //System.out.println("binItem==null");
      return;
    }

    if (!(bin instanceof BinCIType)) {
      bin = bin.getOwner().getBinCIType();
    }
    //System.out.println("binMember=" + bin.getName());

    if (bin == currentBin) {
      // Can also happen when members are different, but owners are the same.
//      if(RefactorItConstants.debugInfo) {
//        DebugInfo.trace("same binObject");
//      }
      return;
    }

    currentBin = bin;

    binRef = bin.createReference();
    setBinCIType(bin, new NBContext(IDEController.getInstance()
        .getActiveProject()));
  }

  private JTypeInfoPanel getTypeInfoPanel() {
    //System.out.println("getTypeInfoPanel");
    if (typeInfo == null) {
      createTypeInfoPanel();
    } else if (empty == true) {
      this.removeAll();
      this.add(typeInfo);

      this.validate();
      this.repaint();

      empty = false;
    }

    return typeInfo;
  }

  private void createTypeInfoPanel() {
    typeInfo = new JTypeInfoPanel(
        new NBContext(IDEController.getInstance().getActiveProject()),
        false, true);

    typeInfo.addLockListener(this);

    this.removeAll();
    this.add(typeInfo);

    this.validate();
    this.repaint();

    empty = false;
  }

  public void update(JTypeInfoPanel panel) {
    if (panel != null) {
      //System.out.println("panel update!!");
      //saveLastActivatedNodes();
      reload();
    }
  }

  void processRebuildStarted(Project project) {
    if (typeInfo != null) {
      setBinCIType(null, null);
    }
//        else {
//        return;
//      }
    activateUninitializedState();
//
//      this.removeAll();
//      this.add(emptyPanel);
//
//      this.validate();
//      this.repaint();
//
//      if (empty) {
//        binRef = null;
//        currentBin = null;
//      } else {
//        empty = true;
//      }
  }

  public void rebuildStarted(final Project project) {
    //System.out.println("rebuildStarted");
    if (SwingUtilities.isEventDispatchThread()) {
      processRebuildStarted(project);
    } else {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            processRebuildStarted(project);
          }
        });
      } catch (Exception ignore) {}
    }
  }

  void processRebuildPerformed(Project project) {
    //activateInitalizedState();
    initialized = true;

    if (binRef == null) {
      AppRegistry.getLogger(this.getClass()).debug("processRebuild: binRef==null");
      reload();
      return;
    }

    Object bin = binRef.restore(project);
    if (bin == null || !(bin instanceof BinCIType)) {
      currentBin = null;
      reload();
      return;
    }

    setBinCIType(bin, new NBContext(project));

  }

  public void rebuildPerformed(final Project project) {
    //System.out.println("rebuildPerformed");
//    activateInitalizedState();
//    if (binRef == null)
//      return;
    if (SwingUtilities.isEventDispatchThread()) {
      processRebuildPerformed(project);
    } else {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            processRebuildPerformed(project);
          }
        });
      } catch (Exception ignore) {}
    }
  }

  /**
   * @param project
   * @param bin
   */
  private void setBinCIType(final Object bin, NBContext context) {
    getTypeInfoPanel().setBinCIType((BinCIType) bin, context);
  }

  /**
   * Check if project is loaded and inf can be shown for BinMembers but does not force loading.
   * @return true if project is loaded.
   */
  public boolean isValidProject() {
    // FIXME: maybe it isn't good way to check?
    Project project = IDEController.getInstance().getActiveProject();
    if (project == null) {
      return false;
    }

    //SubMenuModel.initializeProject();
    return project.getProjectLoader().isLoadingCompleted();
  }
}
