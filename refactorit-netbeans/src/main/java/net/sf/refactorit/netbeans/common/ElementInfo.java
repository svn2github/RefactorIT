/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.ItemByCoordinateFinder;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.ui.module.type.TypeAction;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.local.LocalSource;

import org.apache.log4j.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.ConstructorElement;
import org.openide.src.Element;
import org.openide.src.FieldElement;
import org.openide.src.Identifier;
import org.openide.src.InitializerElement;
import org.openide.src.MemberElement;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceElement;
import org.openide.src.Type;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;


/**
 * @author Vlad
 * @author Risto
 * @author Anton Safonov
 */
public class ElementInfo {
  private static final Logger log = Logger.getLogger(ElementInfo.class);

  private boolean jsp;
  private boolean projectwide;
  private boolean packagewide;
  private Element element;
  private Node node;

  private static ElementInfoVersionState versionState;

  /** to help identifying VCS virtual nodes without Element */
  private FileObject file;

  public ElementInfo(Node node) {
    this.node = node;

    try {
      // hack for jspfiles
      // WebLookNode isnt in openIde API
      Method m = node.getClass().getMethod("getRepresentedObject", null);
      if (m != null) {
        Node jn = (Node) m.invoke(node, null);
        DataObject dop = (DataObject) jn.getCookie(DataObject.class);
        file = dop.getPrimaryFile();
        if (file != null) {
          this.node = jn;
          jsp = true;
          return;
        }
      }
    } catch (Exception e) {
      //  means not jsp file.
    }

    this.element = (Element) node.getCookie(Element.class);

    if (this.element == null) {
      SourceCookie src = (SourceCookie) node.getCookie(SourceCookie.class);
      if (src != null) {
        element = src.getSource();
      } else {
        DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
        if (versionState.refactoringsShouldBeProjectWide(node)) {
          projectwide = true;
        } else if (versionState.isFolderNode(node)) {
          packagewide = true;
        } else if (isVcsVirtualNode(dataObject)) {
          file = dataObject.getPrimaryFile();
        } else if (isJspNode(dataObject)) {
          //JSP hack
          jsp = true;
          file = dataObject.getPrimaryFile();
//          if( file!=null ) {
//            System.out.println("file="+file.getName());
//          } else {
//            System.out.println("file=null");
//          }
        } else if ( ! isFile(node)) {
          throw new RuntimeException("Unknown node");
        }
      }
    }
  }

  private boolean isFile(Node aNode) {
    return aNode.getCookie(DataObject.class) != null &&
        aNode.getCookie(DataFolder.class) == null;
  }

  private boolean isJspNode(DataObject dataObject) {
    return dataObject.getClass().getName().indexOf("JspDataObject")
        != -1;
  }

  private boolean isVcsVirtualNode(DataObject dataObject) {
    return dataObject.getClass().getName()
        .endsWith("VirtualsDataObject");
  }

  public boolean isProjectwide() {
    return projectwide;
  }

  public boolean isJsp() {
    return jsp;
  }

  public Object getBinItem() throws BinItemNotFoundException {
    if (isProjectwide()) {
      return IDEController.getInstance().getActiveProject();
    }

    if (packagewide) {
      return getPackageForNode();
    }

    if (file == null) {
      return getBinMember();
    }

    CompilationUnit source = FileObjectUtil.getCompilationUnit(file);
    if (source != null) {
      BinTypeRef typeRef = source.getMainType();
      if (typeRef != null) {
        return typeRef.getBinCIType();
      }
    }

    throw new BinItemNotFoundException("Couldn't find an item for node: "
        + node);
  }

  /**
   * Same as getBinItem().getClass() except that this one
   * works even when project is not loaded yet.
   */
  public Class getBinItemClass() throws BinItemNotFoundException {
    if (packagewide) {
      return BinPackage.class;
    } else if (projectwide) {
      return Project.class;
    } else if (getMemberElement() instanceof ClassElement) {
      return BinClass.class;
    } else if (getMemberElement() instanceof FieldElement) {
      return BinField.class;
    } else if (getMemberElement() instanceof MethodElement) {
      return BinMethod.class;
    } else if (getMemberElement() instanceof ConstructorElement) {
      return BinConstructor.class;
    } else if (getMemberElement() == null && file != null) {
      if (getCompilationUnit() != null) {
        BinTypeRef typeRef = getCompilationUnit().getMainType();
        if (typeRef != null) {
          return typeRef.getBinCIType().getClass();
        }
      }
    }

    throw new BinItemNotFoundException("Couldn't find a class for node: "
        + node);
  }

  private BinPackage getPackageForNode() {

    String packageName = getPackageName();
    if (packageName == null) {
      return null;
    }
    BinPackage binPackage = ItemByNameFinder.findBinPackage(IDEController.getInstance().
        getActiveProject(), packageName);

    return binPackage;
  }

  private String getPackageName() {
    String packageName = null;

    if (node != null) {
      DataFolder folder = (DataFolder) node.getCookie(DataFolder.class);
      FileObject f = folder.getPrimaryFile();

      Project p = IDEController.getInstance().getActiveProject();

      List l = p.getCompilationUnits();
      foundPackage:
          for (int i = 0; i < l.size(); ++i) {
        CompilationUnit sf = (CompilationUnit) l.get(i);
        Source src = sf.getSource();
        FileObject sfo = null;
        if (sf.getSource() == null) {
          continue;
        } else if (sf.getSource() instanceof NBSource) {
          sfo = ((NBSource) src).getFileObject();
        }
        if (sf.getSource() instanceof LocalSource) {
          sfo = NBContext.findFileObjectForFile(
              src.getFileOrNull());
        }

        if (sfo == null) {
          log.warn("Couldn't find file object for: " + src);
          continue;
        }

        int count = 0;
        for (FileObject parent = sfo.getParent(); parent != null;
            parent = parent.getParent()) {
          if (FileObjectUtil.samePhysicalFile(parent, f)) {

            packageName = sf.getPackage().getQualifiedName();
            for (int remdot = count; remdot != 0; --remdot) {
              int lastDot = packageName.lastIndexOf('.');
              if (lastDot == -1) {
                log.warn("INTERNAL ERROR: Expected at least " + count
                    + " dots in " + sf.getPackage().getQualifiedName());
                packageName = null;
                break;
              } else {
                packageName = packageName.substring(0, lastDot);
              }
            }

            break foundPackage;
          }
          count++;
        }

      }
    }

    return packageName;
  }

  public BinMember getBinMember() throws BinItemNotFoundException {
    if (getBinMemberOrNull() == null) {
      throw new BinItemNotFoundException();
    }

    return getBinMemberOrNull();
  }

  private Element getMemberElement() throws BinItemNotFoundException {
    if (element instanceof SourceElement) {
      return getMainClassInSource((SourceElement) element);
    }

    return element;
  }

  private BinMember getBinMemberOrNull() throws BinItemNotFoundException {
    if (getBinCIType() == null) {
      return null;
    }

    if (getMemberElement() instanceof ClassElement) {
      return getBinCIType();
    } else if (getMemberElement() instanceof InitializerElement) {
      // FIXME: Initializers not supported (gives BinCIType,
      // which is good enough for HierarchyView)
      return getBinCIType();
    } else if (getMemberElement() instanceof FieldElement) {
      FieldElement fe = (FieldElement) getMemberElement();

      Identifier fid = fe.getName();

      return ItemByNameFinder.findBinField(getBinCIType(), fid.getName());
    } else if (getMemberElement() instanceof MethodElement) {
      MethodElement me = (MethodElement) getMemberElement();

      Identifier mid = me.getName();

      return ItemByNameFinder.findBinMethod(getBinCIType(),
          mid.getName(), getFqns(me.getParameters()));
    } else if (getMemberElement() instanceof ConstructorElement) {
      ConstructorElement me = (ConstructorElement) getMemberElement();

      return ItemByNameFinder.findBinConstructor((BinClass) getBinCIType(),
          getFqns(me.getParameters()));
    } else {
      return null;
    }
  }

  private String[] getFqns(final MethodParameter[] params) {
    int len = params.length;
    String result[] = new String[len];
    for (int i = 0; i < len; i++) {
      Type t = params[i].getType();
      result[i] = t.getFullString();
    }

    return result;
  }

  private Element getMainClassInSource(SourceElement se) throws
      BinItemNotFoundException {

    Element result = se;

    ClassElement[] ces = se.getClasses();

    if (ces.length == 0) {
      if (se.getPackage() == null) {
        throw new BinItemNotFoundException(
            "file had some parsing errors and is reported to contain no classes");
      } else {
        throw new BinItemNotFoundException("file contains no classes: "
            + se.getPackage().getSourceName());
      }
    }

    for (int i = 0, len = ces.length; i < len; i++) {
      ClassElement ce = ces[i];
      if (Modifier.isPublic(ce.getModifiers())) {
        result = ce;
        break;
      }
    }

    if (result instanceof SourceElement) {
      DataObject cookie = (DataObject) se.getCookie(DataObject.class);
      String name = cookie.getName();

      for (int i = 0, len = ces.length; i < len; i++) {
        ClassElement ce = ces[i];
        if (ce.getName().getName().equals(name)) {
          result = ce;
          break;
        }
      }

      if (result instanceof SourceElement) {
        result = ces[0];
      }
    }

    return result;
  }

  public BinItem getBinItemFromCompilationUnit(int l, int c, String actionKey) {
    if (l == -1 || c == -1) {
      return null;
    }

    CompilationUnit compilationUnit = getCompilationUnit();

    SourceCoordinate sc = new SourceCoordinate(l, c);

    ItemByCoordinateFinder finder = new ItemByCoordinateFinder(compilationUnit);
    BinItem item = finder.findItemAt(sc);
    if ((item == null) && TypeAction.KEY.equals(actionKey)) {
      BinCIType type = getBinCIType();
      if (type != null) {
        return type;
      }
    }

    return item;
  }

  public BinCIType getBinCIType() {
    String name = getBinCITypeName();
    if (name == null) {
      return null;
    }

    BinTypeRef ref = IDEController.getInstance()
        .getActiveProject().getTypeRefForSourceName(name);

    if (ref == null) {
      return null;
    }

    return ref.getBinCIType();
  }

  /** @return null if not found (not on sourcepath, for example) */
  public CompilationUnit getCompilationUnit() {
    if (IDEController.getInstance().getActiveProject() == null) {
      //  SubMenuModel.ensureProject();// do we need this?
      return null;
    }

    if (file != null) {
      return FileObjectUtil.getCompilationUnit(file);
    }

    BinCIType type = getBinCIType();
    if (type == null) {
      return null;
    }

    return type.getCompilationUnit();
  }

  private ClassElement getClassElement() {
    if (element instanceof ClassElement) {
      return (ClassElement) element;
    }

    if (element instanceof MemberElement) {
      return ((MemberElement) element).getDeclaringClass();
    }

    if (element instanceof InitializerElement) {
      return ((InitializerElement) element).getDeclaringClass();
    }

    if (element instanceof SourceElement) {
      SourceElement sourceElement = ((SourceElement) element);
      if (sourceElement.getAllClasses().length == 0) {
        // Is there a way to make it work here? I used to think there was,
        // but I do not remember when this happens or how to fix it.
        return null;
      }

      return sourceElement.getAllClasses()[0];
    }

    return null;
  }

  private String getBinCITypeName() {
    ClassElement classElement = getClassElement();
    if (classElement == null) {
      return null;
    }

    return classElement.getName().getFullName();
  }

  public FileObject getFileObject() {
    DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
    return dataObject.getPrimaryFile();
  }

  //---- Util methods for ElementInfo arrays

  public static Object[] getBinItems(ElementInfo[] elements) throws
      BinItemNotFoundException {
    Object[] objs = new Object[elements.length];

    for (int i = 0; i < elements.length; i++) {
      objs[i] = elements[i].getBinItem();
      if (objs[i] == null) {
        return null;
      }
    }

    return objs;
  }

  public static Class[] getBinItemClasses(ElementInfo[] infos) throws
      BinItemNotFoundException {
    Class[] result = new Class[infos.length];

    for (int i = 0; i < infos.length; i++) {
      result[i] = infos[i].getBinItemClass();
    }

    return result;
  }

  public static ElementInfo[] getElementsFromNodes(Node[] nodes) {
    ElementInfo[] elements = new ElementInfo[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      elements[i] = new ElementInfo(nodes[i]);
    }

    return elements;
  }

  public static boolean isJsp(ElementInfo[] elements) {
    for (int i = 0; i < elements.length; i++) {
      if (elements[i].isJsp()) {
        return true;
      }
    }

    return false;
  }

  public String toString() {
    String name = this.getClass().getName();

    String fileName;
    if (file == null) {
      fileName = "";
    } else {
      try {
        fileName = file.getURL().toExternalForm();
      } catch (Exception e) {
        fileName = ClassPath.getClassPath(file, ClassPath.COMPILE)
  		      .getResourceName(file, File.separatorChar, true);
      }
    }

    return name.substring(name.lastIndexOf('.') + 1) + ": \""
        + fileName + "\", node: " + node + ", element: " + element
        + ", jsp: " + jsp + ", packagewide: " + packagewide
        + ", projectwide: " + projectwide;
  }

  public static void setVersionState(ElementInfoVersionState s) {
    versionState = s;
  }
}
