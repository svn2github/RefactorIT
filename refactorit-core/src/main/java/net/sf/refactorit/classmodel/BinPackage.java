/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;



import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.BinPackageReference;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Logical package entity. Contains a list of types declared belonging to it.
 */
public final class BinPackage extends BinItem implements Scope {
  private static final String memberType = "package";

  public BinPackage(String name, Project project, boolean fromSource) {
    this.name = name;
    if (Assert.enabled && name == null) {
      Assert.must(false, "Creating package with \'null\' name");
    }
    makeNameDot();
    this.project = project;

    if (this.name.length() == 0) {
      myNonFounds = new HashSet(128);
    } else {
      myNonFounds = new HashSet(16);
    }
//    this.fromSource = fromSource;
  }

  /**
   * 1 - for net
   * 2 - for net.sf
   **/
  public int getPartCount() {
    String name = getQualifiedName();
    int count = 1;
    for (int i = 0; i < name.length(); ++i) {
      if (name.charAt(i) == '.') {
        count++;
      }
    }

    return count;
  }

  public String getQualifiedName() {
    return name;
  }

  public String getQualifiedDisplayName() {
    if (getQualifiedName().length() == 0) {
      return "<default package>";
    }

    return getQualifiedName();
  }

  public String getQualifiedForShortname(String shortTypeName) {
    return (nameDot + shortTypeName).intern();
  }

  /**
   * Needed to avoid crosslinks leading to heavy memory leaks.
   */
  public void cleanUpForRebuild() {
    if (this.myTypes != null) {
      this.myTypes.clear();
    }

    if (this.myNonFounds != null) {
      this.myNonFounds.clear();
    }
    this.project = null;
  }

  /**
   * N.B! This returns valid value only after rebuild - during rebuild it might be wrong sometimes
   */
// NOTE this was too confusing - should be rethought to be useful
//  public boolean isFromSource() {
//    return this.fromSource;
//  }

  public void setFromSource(boolean to) {
//    fromSource = to;
  }

  public BinTypeRef findTypeForQualifiedName(String qualifiedName) {
    BinTypeRef retVal = (BinTypeRef) myTypes.get(qualifiedName);
    if (retVal != null) {
      return retVal;
    }

    if (myNonFounds.contains(qualifiedName)) {
      return null;
    }

    retVal = getProject().getTypeRefForName(qualifiedName);

    if (retVal != null) {
      myTypes.put(qualifiedName, retVal);
    } else {
      myNonFounds.add(qualifiedName);
    }

    return retVal;
  }

  public boolean isContainsType(String fqName) {
    return myTypes.containsKey(fqName);
  }

  /**
   * Gets all entries in this package.
   *
   * @return entries ({@link BinTypeRef} instances). Never returns
   *         <code>null</code>.
   */
  public Iterator getAllTypes() {
    getProject().discoverAllUsedTypes();
    // some super binary classes might be added also during analysis, that's why it's made immutable
    try {
      return new ArrayList(myTypes.values()).iterator();
    } catch (NoSuchElementException e) {
      AppRegistry.getExceptionLogger().error(e,
          "Failed to get types from: " + this, this);
      return new ArrayList(0).iterator();
    }
  }

  /**
   * @return number of entries
   */
  public int getTypesNumber() {
    getProject().discoverAllUsedTypes();
    return myTypes.size();
  }

  public BinTypeRef findTypeForShortName(String shortname) {
    String qualifiedName = getQualifiedForShortname(shortname);
    return findTypeForQualifiedName(qualifiedName);
  }

  public void removeType(String qualifiedName) {
    Object previous = myTypes.remove(qualifiedName);
    if (Assert.enabled && previous == null) {
      Assert.must(false, "Attempted to remove nonexistant type: " + qualifiedName);
    }

    // when changed clear just in case
    myNonFounds.clear();
  }

  public void addType(BinTypeRef type) {
    Object previous = myTypes.put(type.getQualifiedName(), type);
    if (Assert.enabled && previous != null) {
      Assert.must(false,
          "Attempted to add duplicate entry to package! Type name: "
          + type.getQualifiedName());
    }
    if (false) {
      throw new RuntimeException("adding: " + type.getQualifiedName());
    }
    //new RuntimeException("adding: " + type.getQualifiedName()).printStackTrace(System.err);

    myNonFounds.remove(type.getQualifiedName());
  }

  public boolean hasTypesWithSources() {
    for (Iterator types = getAllTypes(); types.hasNext(); ) {
      final BinCIType type = ((BinTypeRef) types.next()).getBinCIType();

      if (type.isFromCompilationUnit()) {
        return true;
      }
    }

    List subPackages = getSubPackages();
    for (int i = 0, max = subPackages.size(); i < max; i++) {
      if (((BinPackage) subPackages.get(i)).hasTypesWithSources()) {
        return true;
      }
    }

    return false;
  }

  public boolean hasTypesWithoutSources() {
    for (Iterator types = getAllTypes(); types.hasNext(); ) {
      final BinCIType type = ((BinTypeRef) types.next()).getBinCIType();

      if (!type.isFromCompilationUnit()) {
        return true;
      }
    }

    List subPackages = getSubPackages();
    for (int i = 0, max = subPackages.size(); i < max; i++) {
      if (((BinPackage) subPackages.get(i)).hasTypesWithoutSources()) {
        return true;
      }
    }

    return false;
  }

  public boolean isIdentical(BinPackage other) {
    if (this == other) {
      return true;
    }

    if (other == null) {
      return false;
    }

    // Packages are identical only if qualified names are equal.
    return getQualifiedName().equals(other.getQualifiedName());
  }

  public String toString() {
    return ClassUtil.getShortClassName(this) + ": "
        + getQualifiedName() + " "
        + Integer.toHexString(System.identityHashCode(this));
  }

  //
  // Some accessor methods
  //

  private void makeNameDot() {
    // N.B! this is called from constructor
    if (name == null || name.length() == 0) {
      this.nameDot = "";
    } else {
      this.nameDot = name + '.';
    }
  }

  public Project getProject() {
    return this.project;
  }

  /**
   * @return true if e.g. package name is aaa.bbb.ccc and directory structure is
   * aaa/bbb/ccc; false otherwise
   *
   * NOTE it will also return true if directory structure ends with aaa/bbb/ccc,
   *      for example in case of somedir.aaa.bbb.ccc is it correct?
   *
   * @author unknown
   * @author aleksei sosnovski
   */
  public boolean isNameMatchesDir() {
    Iterator it = getAllTypes();

    while (it.hasNext()) {
      BinTypeRef type = (BinTypeRef) it.next();

      if (!type.getBinCIType().isFromCompilationUnit()) {
        continue;
      }

      String typeLocation
          = type.getBinCIType().getCompilationUnit().getSource().
          getAbsolutePath();

      // strip source file name from the end
      int ind = typeLocation.lastIndexOf('/');
      if (ind < 0) {
        ind = typeLocation.lastIndexOf('\\');
      }
      if (ind < 0) {
        ind = typeLocation.lastIndexOf(Source.LINK_SYMBOL);
      }
      if (ind >= 0) {
        typeLocation = typeLocation.substring(0, ind);
      } else {
        if (getQualifiedName().length() == 0) {
          return true;
        } else {
          continue;
        }
        //return getQualifiedName().length() == 0; // only type name was in the path
      }
      typeLocation = convertPathToPackageName(typeLocation);

//      Source[] srcPaths =
//          getProject().getPaths().getSourcePath().getRootSources();

//      for (int i = 0; i < srcPaths.length; i++) {
//        String srcPath = convertPathToPackageName(srcPaths[i].getAbsolutePath());

        if (getQualifiedName().length() == 0 && !typeLocation.endsWith(".")) {
//            && typeLocation.equals(srcPath)) {
          return true;
        }

        if (typeLocation.endsWith(getQualifiedName())) {
          int loc = typeLocation.length() - getQualifiedName().length() - 1;
          if (loc < 0) {
            return true; // type location contained package path only
          }

          if (typeLocation.charAt(loc) == '.') {
            // beginning of package name wasn't a continuous
            // string of something infront of it
            return true;
          }
        }
    }

    return false;
  }

  /**
   * @param path
   * @return package name
   */
  // FIXME: move to FileUtil and generalize over all RIT
  public static String convertPathToPackageName(String path) {
    path = path.replace('/', '.');
    path = path.replace('\\', '.');
    path = StringUtil.replace(path, Source.LINK_SYMBOL, ".");

    return path;
  }

  /**
   * @author Aleksei sosnovski
   *
   * @return source dirs
   */
  public Set getBaseDirs() {
    Set dirs = new HashSet();


    Set paths = getDirs();
    boolean match = isNameMatchesDir();

    for (Iterator iter = paths.iterator(); iter.hasNext(); ) {
      Source path = (Source) iter.next();

      if (match) {
        StringTokenizer tokens = new StringTokenizer(getQualifiedName(), ".");
        while (path != null && tokens.hasMoreTokens()) {
          /*String token = */tokens.nextToken();
          path = path.getParent();
        }
      }

      if (path != null) {
        dirs.add(path);
      }
    }

    return dirs;
  }

  /**
   * Detected by the first type entry, so could be wrong. //it IS wrong!
   *
   * @return source dir corresponding to the source files of types of this
   * package
   */

  public Source getDir() {
    Source path = null;
    Source somePath = null;

    for (Iterator it = getAllTypes(); it.hasNext();) {
      BinTypeRef type = (BinTypeRef) it.next();

      if (!type.getBinCIType().isFromCompilationUnit()
          || type.getBinCIType().isDefinitelyInWrongFolder()) {
        continue;
      }

      path = type.getBinCIType().getCompilationUnit().getSource().getParent();
      if (somePath == null) {
        somePath = path;
      }

      if (path != null) {
        String absolutePath = convertPathToPackageName(path.getAbsolutePath());

        if (absolutePath.endsWith(getQualifiedName())) {
          break;
        }

        path = null;
      }
    }

    return (path == null) ? somePath : path;
  }

  /**
   *
   * @return a <code>Set</code> of Sources that denote physical paths where
   * this package has its types.
   */
  public Set getDirs() {
    Set dirs = new HashSet();

    for (Iterator it = getAllTypes(); it.hasNext();) {
      BinTypeRef type = (BinTypeRef) it.next();

      if (!type.getBinCIType().isFromCompilationUnit()
          || type.getBinCIType().isDefinitelyInWrongFolder()) {
        continue;
      }

      Source path = type.getBinCIType().getCompilationUnit().getSource().getParent();


      if (path != null) {
        String absolutePath = path.getAbsolutePath();
        absolutePath = absolutePath.replace('/', '.').replace('\\', '.');
        absolutePath = StringUtil.replace(absolutePath,
            Source.LINK_SYMBOL, ".");

        if (absolutePath.endsWith(getQualifiedName())) {
          dirs.add(path);
        }

      }
    }

    return dirs;
  }

  /**
   * Detected by the first type entry, so could be wrong. // it IS wrong!
   * <UL>
   * <LI>name: aaa.bbb.ccc, path: xxx/yyy -> xxx/yyy</LI>
   * <LI>name: aaa.bbb.ccc, path: xxx/aaa/bbb/ccc -> xxx</LI>
   * </UL>
   *
   * @return either dir path of the source files of this package, or
   *         dir path which is the root of directory structure matching package
   *         name
   */
  public Source getBaseDir() {
    Source path = getDir();

    if (isNameMatchesDir()) {
      StringTokenizer tokens = new StringTokenizer(getQualifiedName(), ".");
      while (path != null && tokens.hasMoreTokens()) {
        /*String token = */tokens.nextToken();
        path = path.getParent();
//System.err.println("Token: " + token + ", newpath: " + path);
      }
    }

    // shouldn't happen, but let's assure we have at least something
    if (path == null) {
      System.err.println(
          "RefactorIT exception, PLEASE REPORT: Got BaseDir == null for package: "
          + getQualifiedName());
      path = getDir();
    }

    return path;
  }

  public Source getNewDir(String newPackageName, Source packageDir) {
    if (!isNameMatchesDir()) {
      return null;
    }
    Source path = packageDir;

    StringTokenizer oldPackageTokens
        = new StringTokenizer(getQualifiedName(), ".");
    StringTokenizer newPackageTokens
        = new StringTokenizer(newPackageName, ".");
    boolean startedBranch = false;
    while (oldPackageTokens.hasMoreTokens() && path != null) {
      String oldToken = oldPackageTokens.nextToken();
      String newToken = null;
      if (newPackageTokens.hasMoreTokens()) {
        newToken = newPackageTokens.nextToken();
      }

      if (!startedBranch
          && (newToken == null || !newToken.equalsIgnoreCase(oldToken))) {
        startedBranch = true;
      }

      if (startedBranch) {
        path = path.getParent();
      }
    }

    return path;
  }

  /**
   * Gets all source files of this package.
   *
   * @return source files ({@link CompilationUnit} instances).
   *         Never returns <code>null</code>.
   */
  public List getCompilationUnitList() {
    final List allSources = getProject().getCompilationUnits();
    final List packageSources = new ArrayList();
    for (int i = 0, len = allSources.size(); i < len; i++) {
      final CompilationUnit source = (CompilationUnit) allSources.get(i);
      if (isIdentical(source.getPackage())) {
        packageSources.add(source);
      }
    }

    return packageSources;
  }

  /** JLS does not specify subpackage definition, but this works intuitively */
  public List getSubPackages() {
    List result = new ArrayList();

    BinPackage[] allPackages = getProject().getAllPackages();
    for (int i = 0; i < allPackages.length; i++) {
      if (allPackages[i].isSubPackageOf(this)) {
        result.add(allPackages[i]);
      }
    }

    return result;
  }

  /** JLS does not specify subpackage definition, but this works intuitively */
  public boolean isSubPackageOf(BinPackage binPackage) {
    if (binPackage.isDefaultPackage()) {
      return false;
    }
    return getQualifiedName().startsWith(binPackage.getQualifiedName() + '.');
  }

  /** Java Language Specification does not specify a subpackage definition
      but this works intuitively */
  public List getDirectSubPackages() {
    List result = new ArrayList();

    BinPackage[] allPackages = getProject().getAllPackages();
    for (int i = 0; i < allPackages.length; i++) {
      if (allPackages[i].isDirectSubPackageOf(this)) {
        result.add(allPackages[i]);
      }
    }

    return result;
  }

  /** Tests to see if a given package from the project is a direct subpackage
      of binPackage */
  public boolean isDirectSubPackageOf(BinPackage binPackage) {
    int pos = getQualifiedName().lastIndexOf('.');
    if (pos == -1) {
      return false;
    }

    return binPackage.getQualifiedName().equals(
              getQualifiedName().substring(0, pos));
  }

  /**
   * Returns package for which this is direct subpackage based on qualified name.
   * For example, if package name is net.sf.refactorit it tries to find package net.sf if such exist.
   * @return BinPackage if exist or null otherwise.
   */
  public BinPackage getSuperPackage() {
    BinPackage result = null;

    String qualifiedName = getQualifiedName();
    int index = qualifiedName.lastIndexOf(".");
    if (index != -1) {
      result = getProject().getPackageForName(qualifiedName.substring(0, index));
    }
    return result;
  }

  public final String getMemberType() {
    return memberType;
  }

  public static String getStaticMemberType() {
    return memberType;
  }

  public boolean isDefaultPackage() {
    return "".equals(getQualifiedName());
  }

  public void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void defaultTraverse(BinItemVisitor visitor) {
    // source types first
    List allSources = getProject().getCompilationUnits();
    for (int i = 0, max = allSources.size(); i < max; i++) {
      final CompilationUnit source = (CompilationUnit) allSources.get(i);
      if (this.isIdentical(source.getPackage())) {
        visitor.visit(source);
      }
    }

    // binary types next
    Iterator types = getAllTypes();
    while (types.hasNext()) {
      BinTypeRef type = (BinTypeRef) types.next();
      if (!type.getBinCIType().isFromCompilationUnit()) {
        type.getBinCIType().accept(visitor);
      }
    }
  }

  public void defaultTraverseWithSubpackages(BinItemVisitor visitor) {
    defaultTraverse(visitor);

    List subs = getSubPackages();
    for (int i = 0, max = subs.size(); i < max; i++) {
      ((BinPackage) subs.get(i)).accept(visitor);
    }
  }

  public void initScope(HashMap variableMap, HashMap typeMap) {
//    myScopeRules = new ScopeRules(this, variableMap, typeMap);
  }

//  public ScopeRules getScopeRules() {
//    return myScopeRules;
//  }

  public boolean containsWithSubpackages(Scope other) {
    if (other instanceof BinPackage) {
      return isIdentical((BinPackage) other)
          || ((BinPackage) other).isSubPackageOf(this);
    }

    if (!(other instanceof BinCIType)) {
      other = ((BinItem) other).getParentType();
    }

    if (other instanceof BinCIType) {
      return getSubPackages().contains(((BinCIType) other).getPackage());
    }

    return false;
  }

  public boolean contains(Scope other) {
    if (other instanceof BinPackage) {
      return isIdentical((BinPackage) other);
    }

    if (!(other instanceof BinCIType)) {
      other = ((BinItem) other).getParentType();
    }

    if (other instanceof BinCIType) {
      return isIdentical(((BinCIType) other).getPackage());
    }

    return false;
  }

  public BinItemReference createReference() {
    return new BinPackageReference(this);
  }

  /**
   *
   * @param packageName String
   * @param project Project
   * @return boolean
   *
   * @author Aleksei Sosnovski
   *
   * This method will return true even if only a part of package belongs to
   * ignored sources, that true does not mean, that whole package is ignored.
   */
  public static boolean isIgnored
      (final String packageName, final Project project) {
    if (packageName != null && project != null) {
      String packName = packageName;
      packName = StringUtil.replace(packName, ".", "\\");

      SourcePath srcP = project.getPaths().getSourcePath();

      Source[] sources = srcP.getRootSources();

      //At lest under Eclipse BinPackage packaga is returned,
      //though it is in ignored sourcepath.
      //Under JBuilder2005 is not....
      /*
      if (project.getPackageForName(packageName) != null) {
        return false;
      }
      */

      List ignored = (srcP).getIgnoredSources();

      for (int m = 0; m < ignored.size(); m++) {
        String ignoredPath = (String) ignored.get(m) + "\\";

        for (int n = 0; n < sources.length; n++) {
          String path = sources[n].getAbsolutePath() + "\\";
          path = StringUtil.replace(path, "/", "\\");
          ignoredPath = StringUtil.replace(ignoredPath, "/", "\\");
          //path = StringUtil.replace(path, "\\", "\\");
          path = StringUtil.replace(path, Source.LINK_SYMBOL, "\\");
          ignoredPath = StringUtil.replace(ignoredPath, Source.LINK_SYMBOL, "\\");

          if (ignoredPath.startsWith(path)) {

            int ind = -1;
            do {
              if (ignoredPath.equals(path + packName + "\\")) {
                return true;
              }

              ind = packName.lastIndexOf("\\");

              if (ind > 0) {
                packName = packName.substring(0, ind);
              }
            } while (ind >= 0);
          }
        }
      }
    }

    return false;
  }

  public static boolean isIgnored(final BinPackage aPackage) {
    String packName = aPackage.getQualifiedName();
    Project p = aPackage.getProject();

    return isIgnored(packName, p);
  }

  private final String name;
  private String nameDot; // optimizing here

  private final Map myTypes = new HashMap();
  private final HashSet myNonFounds;
  private Project project;

//  private boolean fromSource = false;
}
