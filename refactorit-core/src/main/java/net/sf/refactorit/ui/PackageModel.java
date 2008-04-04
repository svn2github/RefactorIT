/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author  Anton Safonov
 */
public class PackageModel extends BinTreeTableModel {
  private static final ModelOptions DEFAULT_OPTIONS
      = new ModelOptions(false, false, false);

  private BinTreeTableNode openNode;

  private Map allPackages;

  private Project project;
  private Object openItem;
  private ModelOptions options;
  private Source baseSource;

//  private BinTreeTableNode locationsNode;
  private BinTreeTableNode projectNode;

  private List branchingPoints = new ArrayList();

  public static class ModelOptions {
    boolean includeTypes = false;
    boolean includeClasspath = false;
    boolean includePrimitives = false;
    boolean includeVoid = false;

    public ModelOptions(boolean includeTypes, boolean includeClasspath,
        boolean includePrimitives) {
      this.includeClasspath = includeClasspath;
      this.includeTypes = includeTypes;
      this.includePrimitives = includePrimitives;
    }
    public ModelOptions(boolean includeTypes, boolean includeClasspath,
        boolean includePrimitives, boolean includeVoid) {
      this(includeTypes, includeClasspath, includePrimitives);
      this.includeVoid = includeVoid;
    }
  }


  /**
   * It is important here that we know when we don't want to call getBinType()
   * Because when we will call getBinType() the type and some of the other types
   * it references will be loaded.
   * This is often not a desired behavior.
   */
  public static class TypeRefNode extends BinTreeTableNode {
    private BinTypeRef typeRef;

    public TypeRefNode(Object bin) {
      super(bin, false);
      typeRef = (BinTypeRef) bin;
    }

    public BinTypeRef getTypeRef() {
      return typeRef;
    }

    public int getType() {
      return UITreeNode.NODE_UNRESOLVED_TYPE;
    }

    public boolean equals(Object object) {
      if (!(object instanceof TypeRefNode)) {
        return false;
      }

      return ((TypeRefNode) object).getTypeRef().equals(getTypeRef());
    }
  }


  public static class PackageNode extends BinTreeTableNode {
    public PackageNode(Object bin) {
      super(bin, false);
      setShowCheckBox(false);
    }

    public int getType() {
      return UITreeNode.NODE_PACKAGE;
    }
  }


  /**
   * @param project project
   * @param openPackage defines a package which will be opened by default
   */
  public PackageModel(Project project, BinPackage openPackage) {
    this(project, openPackage, null, DEFAULT_OPTIONS);
  }

  /**
   * @param project project
   * @param baseSource
   * @param openPackage defines a package which will be opened by default
   */
  public PackageModel(Project project, Source baseSource,
      BinPackage openPackage) {
    this(project, openPackage, baseSource, DEFAULT_OPTIONS);
  }

  /**
   * @param project project
   * @param baseSource
   * @param openPackages defines a packages which will be opened by default
   */
  public PackageModel(Project project, Source baseSource,
      List openPackages) {
    this(project, openPackages.get(0), // TODO open all packages later
        baseSource, DEFAULT_OPTIONS);
  }

  /**
   * @param project project
   * @param baseSource
   * @param openItem defines a package or class which will be opened by default.
   *  Can be instanceof {@link BinPackage} or {@link BinTypeRef} only!!
   * @param options
   */
  public PackageModel(Project project, Object openItem,
      Source baseSource, ModelOptions options) {
    super(new BinTreeTableNode("Packages")); // FIXME: i18n

    Assert.must(openItem == null || openItem instanceof BinPackage
        || openItem instanceof BinTypeRef);

    this.project = project;
    this.openItem = openItem;
    this.options = options;
    this.baseSource = baseSource;

    reload(baseSource);
  }

  public void updateNode(BinTreeTableNode node) {
    packageTreeStructureChanged(this, node.getParent().getPath(), null, null);
  }

  public final void packageTreeStructureChanged(
      final Object source, final Object[] path,
      final int[] childIndices, final Object[] children) {
    super.fireTreeStructureChanged(source, path, childIndices, children);
  }

  public void reload(Source source) {
    ((BinTreeTableNode) getRoot()).removeAllChildren();
    branchingPoints.clear();

    if (!options.includeClasspath && options.includeTypes) {

      initChildren(project, null, new ArrayList());

    } else {
      BinPackage[] packages = project.getAllPackages();

      this.allPackages = new HashMap(packages.length, 0.99f);
      for (int i = 0; i < packages.length; i++) {
        this.allPackages.put(packages[i].getQualifiedName(), packages[i]);
      }

      for (int i = 0; i < packages.length; i++) {
        final BinPackage pack = packages[i];

        if (!pack.hasTypesWithSources() && !options.includeClasspath) {
          continue;
        }

        Set packDirs = pack.getDirs();
        if (source != null) {
          boolean isChild = false;
          for (Iterator iter = packDirs.iterator(); iter.hasNext(); ) {
            Source element = (Source) iter.next();
            if (isChild(source, element)) {
              isChild = true;
            }
          }
          if (!isChild) {
            continue;
          }
        }

        collectBranchingPoints(pack);

        BinTreeTableNode node = getNodeForPath(pack.getQualifiedName());

        if (options.includeTypes) {
          Iterator types = null;
          // doesn't work, still overhead
//          if ( options.includeClasspath ) {
//            types = pack.getAllTypes();
//          } else {
//            List typeList=new ArrayList(project.getDefinedTypes().size());
//             List sourcesList = pack.getCompilationUnitList();
//             for ( int sourceIndex=0 ; sourceIndex < sourcesList.size(); ++sourceIndex) {
//               typeList.addAll( ((CompilationUnit)(sourcesList.get(sourceIndex))).getDefinedTypes());
//             }
//             types=typeList.iterator();
//          }
          types = pack.getAllTypes();
          for (; types.hasNext(); ) {
            BinTypeRef type = (BinTypeRef) types.next();
            if (options.includeClasspath) {
              type.getAllSupertypes(); // retrieve all binary supertypes
            }
            TypeRefNode typeRefNode = new PackageModel.TypeRefNode(type);
            node.addChild(typeRefNode);
            if (openItem instanceof BinTypeRef) {
              BinTypeRef ref = (BinTypeRef) openItem;

              if (ref.getCompilationUnit() != null && source != null
                  && (ref.getCompilationUnit().getSource().getAbsolutePath()+ File.separator)
                  .startsWith(source.getAbsolutePath() + File.separator)
                  && type.equals(openItem)) {
                openNode = typeRefNode;
              }

            }
          }
        }


        if ((source == baseSource) && pack.equals(openItem)) {
          this.openNode = node;
        }
      }

      addNonJavaDirs(source);

      ((BinTreeTableNode) getRoot()).sortAllChildren();
      projectNode = (BinTreeTableNode) getRoot();

    }

    if (options.includePrimitives) {
      addPrimitiveTypes(options.includeVoid);
    }

    fireSomethingChanged();
  }

  private class PackageBranchingPoint {
    public PackageBranchingPoint(BinPackage aPackage, Source branchingPoint) {
      this.aPackage = aPackage;
      this.branchingPoint = branchingPoint;
    }

    public String toString() {
      return "PackageBranchingPoint: " + aPackage + ", source: " +
          branchingPoint;
    }

    public BinPackage aPackage;
    public Source branchingPoint;
  }


  private void collectBranchingPoints(BinPackage pack) {
    StringTokenizer tokens = new StringTokenizer(pack.getQualifiedName(), ".");
    List packageParts = new ArrayList(8);
    while (tokens.hasMoreTokens()) {
      packageParts.add(tokens.nextToken());
    }

    Source dir = pack.getDir();
    Source prevDir = dir;
    // BUG: it doesn't check if the dir is still within our selected sourcePath
    for (int i = packageParts.size() - 1; dir != null && i >= 0; i--) {
      if (!packageParts.get(i).equals(dir.getName())) {
        break;
      }

      prevDir = dir;
      dir = dir.getParent();
    }
    if (dir == null) {
      dir = prevDir;
    }

    if (dir == null) {
      return; // hmm, shouldn't happen
    }
//System.out.println( "**********************************" );
//System.out.println( "package: " + pack.getQualifiedName() );
//System.out.println( "package dir: " + pack.getDir() );

    for (int i = 0; i < branchingPoints.size(); i++) {
      final PackageBranchingPoint branch
          = (PackageBranchingPoint) branchingPoints.get(i);
      final Source parent = branch.branchingPoint;
      //if (isChild(parent, dir)) {
      if (parent.equals(dir)) {
//System.out.println( "Skipped parent.getAbsolutePath(): " + parent.getAbsolutePath() );
//System.out.println( "Skipped dir: " + dir );
//System.out.println( "Skipped package: " + pack.getQualifiedName() );
//System.out.println( "Skipped package dir: " + pack.getDir() );
        return;
      }
    }

//System.out.println( "Added dir: " + dir );
//System.out.println( "Added package: " + pack.getQualifiedName() );

    PackageBranchingPoint branchingPoint = new PackageBranchingPoint(pack, dir);
    branchingPoints.add(branchingPoint);
  }

  public void addNonJavaDirs(Source source) {
    for (int i = 0; i < branchingPoints.size(); i++) {
      final PackageBranchingPoint branch
          = (PackageBranchingPoint) branchingPoints.get(i);
      final Source parent = branch.branchingPoint;

      String targetPath =
          BinPackage.convertPathToPackageName(parent.getAbsolutePath());

      String packagePath = branch.aPackage.getQualifiedName();

      String commonPath = "";

//System.err.println("Check pack: " + packagePath);
      if (!targetPath.endsWith(packagePath)) {
        String path = packagePath;
        int pos;
        while ((pos = path.lastIndexOf('.')) != -1) {
          path = path.substring(0, pos);
//System.err.println("Check pack: " + path);
          if (targetPath.endsWith(path)) {
            commonPath = path;
            break;
          }
        }
      } else {
        commonPath = packagePath;
      }

      addChildren(parent, commonPath, source);
    }
  }

  private void addChildren(Source parent, String commonPath, Source source) {
    Source[] children = parent.getChildren();
    if (children == null) {
      return;
    }
//System.out.println( "****** addChild *******" );
//System.out.println( "parent: " + parent.getAbsolutePath() );
//System.out.println( "commonPath: " + commonPath );

    for (int j = 0; j < children.length; j++) {
      if (!children[j].isDirectory()) {
        continue;
      }

      String childName = children[j].getName();
      if (isSkippedDir(childName)) {
        continue;
      }

      if (commonPath.length() > 0) {
        childName = commonPath + '.' + childName;
      }

      if (source == null
          || (children[j].getAbsolutePath() + File.separator)
          .startsWith(source.getAbsolutePath() + File.separator)) {
        getNodeForPath(childName);
      }

      addChildren(children[j], childName, source);
    }
  }

  private static final String[] toBeSkipped = new String[] {
      ".",
      "images",
      "resources"
  };

  private boolean isSkippedDir(String dir) {
    if (dir == null || dir.trim().length() == 0) {
      return true;
    }

    for (int i = 0; i < toBeSkipped.length; i++) {
      if (toBeSkipped[i].equalsIgnoreCase(dir)) {
        return true;
      }
    }

    String str = GlobalOptions.getOption("version.control.dir.list");
    if (str == null) {
      return false;
    }

    StringTokenizer token = new StringTokenizer(str, ";");
    while (token.hasMoreTokens()) {
      if (token.nextToken().equalsIgnoreCase(dir)) {
        return true;
      }
    }

    if (!NameUtil.isValidIdentifier(dir)) {
      return true;
    }

    return false;
  }

  public BinTreeTableNode getOpenNode() {
    return this.openNode;
  }

  public static boolean isChild(Source parent, Source child) {
//System.err.println("isChild, parent: " + parent + ", child: " + child);
    if (parent == null || child == null) {
      return false;
    }

    if (parent.equals(child)) {
      return true;
    }

    return isChild(parent, child.getParent());
  }

  private BinTreeTableNode getNodeForPath(String path) {
    if (Assert.enabled) {
      Assert.must(path != null, "Path is null!");
    }

    int ind = path.lastIndexOf('.');

    BinTreeTableNode parent;
    if (ind < 0) {
      parent = (BinTreeTableNode) getRoot();
    } else {
      String prefix = path.substring(0, ind);
      parent = getNodeForPath(prefix);
    }

    String shortName = path.substring(ind + 1);
    BinTreeTableNode node = null;
    if (parent != null) {
      node = (BinTreeTableNode) parent.findChild(shortName, false);
      if (node == null) {
        node = createPackageNode(parent, path, shortName);
      }
    }

    return node;
  }

  private PackageNode createPackageNode(BinTreeTableNode parent,
      String fullName, String name) {
    if (parent == null) {
      parent = (BinTreeTableNode) getRoot();
    }

    PackageNode node = (PackageNode) parent.findChild(name, false);
    if (node != null) {
      return node;
    }

    Object existing = this.allPackages.get(fullName);

    if (existing != null) {
      node = new PackageNode(existing);
    } else {
      node = new PackageNode(name);
    }

    parent.addChild(node);

    return node;
  }

  /** Test driver for {@link PackageModel}. */
  public static class TestDriver extends junit.framework.TestCase {
    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(TestDriver.class.getName());

    private IDEController old;

    public TestDriver(String name) {
      super(name);
    }

    public static junit.framework.Test suite() {
      final junit.framework.TestSuite suite = new junit.framework.TestSuite(
          TestDriver.class);
      suite.setName("PackageSelector tests");
      return suite;
    }



    /**
     * Tests if package tree model is built correctly.
     */
    public void testPackageModelInit() throws Exception {
      cat.info("Testing package tree model initialization");

      Project project =
          Utils.createTestRbProject(
          Utils.getTestProjects().getProject("PackageModel_init"));

      project.getProjectLoader().build();
      PackageModel model = new PackageModel(project, null);
      BinTreeTableNode root = (BinTreeTableNode) model.getRoot();

      assertEquals("Root children count", 6, root.getChildCount());

      for (int i = 0, max = root.getChildCount(); i < max; i++) {
        assertTrue("Children count of child \""
            + ((BinTreeTableNode) root.getChildAt(i)).getDisplayName()
            + "\" should be less or equal 1",
            ((BinTreeTableNode) root.getChildAt(i)).getChildCount() <=
            1);
      }

      for (int i = 0, max = root.getChildCount(); i < max; i++) {
//System.err.println("i: " + i + " - " + ( (BinTreeTableNode) root.getChildAt(i)).getBin());
        switch (i) {
          case 0:
          case 1:
          case 2:
          case 4:
            assertTrue("Package \""
                + ((BinTreeTableNode) root.getChildAt(i)).getDisplayName()
                + "\" should be BinPackage",
                ((BinTreeTableNode) root.getChildAt(i)).getBin()
                instanceof BinPackage);
            break;
          default:
            assertTrue("Package \""
                + ((BinTreeTableNode) root.getChildAt(i)).getDisplayName()
                + " should be String",
                ((BinTreeTableNode) root.getChildAt(i)).getBin()
                instanceof String);
            break;
        }
      }

      cat.info("SUCCESS");
    }
  }


  private void initChildren(final Project project,
      final BinCIType nativeType,
      final List probableTargetClasses) {
    final BinTreeTableNode root = (BinTreeTableNode) getRoot();

//    if (probableTargetClasses.size() > 0) {
//      locationsNode = new BinTreeTableNode("Probable target classes", false);
//      root.addChild(locationsNode);
//
//      for (int i = 0, max = probableTargetClasses.size(); i < max; i++) {
//        locationsNode.findParent( ( (BinType) probableTargetClasses.get(i)).
//                                 getTypeRef());
//      }
//
//      locationsNode.sortChildrenByNames();
//    }

    boolean openNodeFound = false;

    final List definedTypes = project.getDefinedTypes();
    projectNode = root;
    //projectNode = new BinTreeTableNode("Project", false);
    //root.addChild(projectNode);

    for (int i = 0, max = definedTypes.size(); i < max; i++) {
      final BinCIType type = ((BinTypeRef) definedTypes.get(i)).getBinCIType();
      if (type == nativeType || !type.isFromCompilationUnit()) {
        continue;
      }
      Object obj = projectNode.findParent(type.getTypeRef(), false); // small feature :)

      if (!openNodeFound && definedTypes.get(i).equals(openItem)) {
        if (!(obj instanceof BinTreeTableNode)) {
          AppRegistry.getLogger(this.getClass()).debug(obj.toString()
          + "  in PackageTree is not instanceof BinTreeTableNode");

        } else {
          openNode = (BinTreeTableNode) obj;
          openNodeFound = true;
        }
      }

    }

    projectNode.sortAllChildren();
  }

//  public void expandPath(JTree tree) {
//    TreePath path;
//
//    if (locationsNode != null) {
//      List children = locationsNode.getChildren();
//      for (int i = 0, max = children.size(); i < max; i++) {
//        path = new TreePath( ( (BinTreeTableNode) children.get(i)).getPath());
//        tree.expandPath(path);
//      }
//    }
//
//    path = new TreePath(projectNode.getPath());
//    tree.expandPath(path);
//  }
//
//  public String getColumnName(int column) {
//    switch (column) {
//      case 0:
//        return "Class hierarchy";
//    }
//
//    return null;
//  }
//
//  public Class getColumnClass(int column) {
//    switch (column) {
//      case 0:
//        return TreeTableModel.class;
//    }
//
//    return null;
//  }
//
//  public boolean isShowing(int column) {
//    return true;
//  }
//
//  public Object getChild(Object node, int num) {
//    return ( (BinTreeTableNode) node).getChildAt(num);
//  }
//
//  public int getChildCount(Object node) {
//    return ( (BinTreeTableNode) node).getChildCount();
//  }
//
//  public Object getValueAt(Object node, int column) {
//    switch (column) {
//      case 0:
//        return node;
//    }
//
//    return null;
//  }
//
//  public boolean isCellEditable(int column) {
//    return false;
//
//  }

  private void addPrimitiveTypes(boolean includeVoid) {
    BinTreeTableNode primitives = new BinTreeTableNode("Primitives");
    projectNode.addChild(primitives, true);

    if (includeVoid) {
      primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.
          VOID_REF));
    }
    primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.
        BOOLEAN_REF));
    primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.BYTE_REF));
    primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.CHAR_REF));
    primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.
        DOUBLE_REF));
    primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.FLOAT_REF));
    primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.INT_REF));
    primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.LONG_REF));
    primitives.addChild(new PackageModel.TypeRefNode(BinPrimitiveType.SHORT_REF));
  }
}
