/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 *
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.Workspace;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.refactorings.undo.SourceInfo;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.test.commonIDE.MockWorkspace;
import net.sf.refactorit.test.commonIDE.NullController;
import net.sf.refactorit.test.commonIDE.NullWorkspace;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.utils.ClasspathUtil;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.utils.LinePositionUtil;
import net.sf.refactorit.vfs.AbstractClassPath;
import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.FileChangeListener;
import net.sf.refactorit.vfs.FileChangeMonitor;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.local.LocalClassPath;
import net.sf.refactorit.vfs.local.LocalSource;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Utilities used during the testing.
 */
public class Utils {
  private static File testFileDir;

  private static Projects testProjects = null;

  static Project fakeProject;

  public static boolean initialized;

  private static HashMap lastSetOfFiles = new HashMap();

  /** Hidden constructor */
  private Utils() {}

  /**
   * Gets directory where test files are located.
   *
   * @return directory.
   */
  public static synchronized File getTestFileDirectory() {
    if (testFileDir != null) {
      return testFileDir;
    }

    File test = new File("../src/test");
    if (test.exists()) {
      if (test.isDirectory()) {
        return testFileDir = test;
      }
    }

    throw new IllegalStateException(
        "Test projects are not configured properly");
  }

  public static void setUpTestingEnvironment() {
    if (initialized) {
      return;
    }

    GlobalOptions.setOption("debug.checkIntegrityAfterLoad", "false");
    GlobalOptions.setOption("misc.verbose", "false");

    DialogManager.setInstance(new NullDialogManager());

    new NullController();

    AppRegistry.getLogger(Utils.class).debug("setting up testing environment");

    initialized = true;
  }

  /**
   * Gets directory where test projects are located.
   *
   * @return directory.
   */
  public static File getTestProjectsDirectory() {
    File projects = new File(getTestFileDirectory(), "projects");
    if (projects.exists()) {
      if (!projects.isDirectory()) {
        throw new IllegalStateException(
            "Test projects are not configured properly");
      }

      return projects;
    }

    throw new IllegalStateException(
        "Test projects are not configured properly");
  }

  public static void setTestFileDirectory(File f) {
    testFileDir = f;
  }

  /**
   * Loads properties from the specified file.
   *
   * @param file file to load properties from.
   * @return properties. Never returns <code>null</code>.
   *
   * @throws IOException if I/O exception occurs.
   */
  public static Properties loadProperties(File file) throws IOException {
    FileInputStream input = null;
    try {
      input = new FileInputStream(file);
      Properties properties = new Properties();
      properties.load(input);
      return properties;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          throw new RuntimeException("Failed to close file:\n"
              + e);
        }
      }
    }
  }

  /**
   * Loads test projects.
   *
   * @return projects.
   */
  public static Projects getTestProjects() {
    if (testProjects == null) {
      final File projectDefinitions =
          new File(getTestProjectsDirectory(), "projects.xml");
      testProjects = new Projects(projectDefinitions);
    }

    return testProjects;
  }

  /**
   * Creates test RB project from metadata.
   *
   * @param metadata metadata.
   *
   * @return test project. Never returns <code>null</code>.
   *
   * @throws NullPointerException if <code>metadata</code> is <code>null</code>.
   */
  public static Project createTestRbProject(ProjectMetadata metadata) {

    if (metadata == null) {
      throw new NullPointerException("metadata is null");
    }

    final String sourcePath = makeLocalPathsAbsolute(metadata.getSourcePaths(),
        "");
    final String classPath = makeLocalPathsAbsolute(metadata.getClassPaths(),
        ClasspathUtil.getDefaultClasspath());

    String copyPath = metadata.getCopyPath();
    if (copyPath != null) {
      copyPath = FileUtil.getAbsolutePath(copyPath);

      if (!pathExists(copyPath)) {
        throw new RuntimeException("Copy path does not exist: " + copyPath);
      }
    }

    String ignoredPath = null;
    List ignoredPaths = metadata.getIgnoredPaths();
    if (ignoredPaths != null && ignoredPaths.size() > 0) {
      ignoredPath = makeLocalPathsAbsolute(ignoredPaths, "");
    }

    try {
      // was before: Project project = createNewProjectFrom(new TestProject( ...
      final TestProject testProject = new TestProject(
          metadata.getId(),
          LocalSourcePath.createTestLocalSource(sourcePath, copyPath, ignoredPath),
          new LocalClassPath(classPath),
          null);
      Project project = createNewProjectFrom(testProject);
      //NullWorkspace.getInstance().addIdeProject(testProject);
      //fakeProject = (Project) NullWorkspace.getInstance().getProject(testProject);
      fakeProject = project;
    } catch (IOException e) {
      AppRegistry.getExceptionLogger().error(e,Utils.class);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
    return fakeProject;
  }

  public static String makeLocalPathsAbsolute(List paths,
      String resultPrefix) throws RuntimeException {

    final StringBuffer result = new StringBuffer(resultPrefix);
    for (final Iterator j = paths.iterator(); j.hasNext(); ) {

      String path = FileUtil.getAbsolutePath((String) j.next());
      if (!pathExists(path)) {
        throw new RuntimeException("Source path does not exist: " + path);
      }

      if (result.length() > 0) {
        result.append(File.pathSeparatorChar);
      }
      result.append(path);
    }

    return result.toString();
  }

  private static boolean pathExists(String path) {
    return (new File(path)).exists();
  }

  private static void emptyFolder(Source folder) {
    Source[] children = folder.getChildren();

    for (int i = 0; i < children.length; i++) {
      if (children[i].isDirectory()) {
        emptyFolder(children[i]);
      }
    }

    for (int i = 0; i < children.length; i++) {
      if (!children[i].delete()) {
        throw new RuntimeException("Unable to delete "
            + children[i].getAbsolutePath());
      }
    }
  }

  private static Source createTempFolder(Source parent, String name) {
    if (parent.getChild(name) != null) {
      emptyFolder(parent.getChild(name));
    }

    Source folder = parent.mkdirs(name);
    if (folder == null) {
      throw new RuntimeException("Unable to create folder called " + name
          + " under test projects directory");
    }

    return folder;
  }

  private static Source createTempFile(Source folder,
      String fileName) throws IOException {

    IUndoableTransaction trans = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;

    if (trans != null) {
      undo = trans.createCreateFileUndo(new SourceInfo(folder, fileName));
    }

    Source result = folder.createNewFile(fileName);

    if (trans != null && result != null) {
      trans.addEdit(undo);
    }

    if (result == null) {
      throw new RuntimeException(
          "Unable to create a new file called " + fileName);
    }

    return result;
  }

  private static Source writeNewTempFile(Source folder, String fileName,
      String contents) {
    PrintWriter out;
    Source newFile;
    try {
      newFile = createTempFile(folder, fileName);

      out = new PrintWriter(newFile.getOutputStream());
    } catch (IOException e) {
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
    out.print(contents);
    out.close();

    return newFile;
  }

  /** Just creates ONE folder with the specified name -- even if the name contains dots. */
  private static Source createPackageFolder(Source rootFolder,
      String packageName) {
    return createTempFolder(rootFolder, packageName);
  }

  /** Uses default package and comes up with a random file name (like "Axxx.java") */
  public static Project createTestRbProjectFromString(String compilationUnitContents)  {
    return createTestRbProjectFromString(new TempCompilationUnit[] {
        new TempCompilationUnit(compilationUnitContents)});
  }

  public static Project createTestRbProjectFromArray(
      String[] singleCompilationUnitParts) throws Exception {
    return createTestRbProjectFromString(
        StringUtil.join(singleCompilationUnitParts, "\n"));
  }

  /**
   * @param manyCompilationUnitsContents 0 - file1 name, 1 - file1 contents etc.
   */
  public static Project createTestRbProjectWithManyFiles(
      String[] manyCompilationUnitsContents) throws Exception {
    TempCompilationUnit[] units
        = new TempCompilationUnit[manyCompilationUnitsContents.length >> 1];
    for (int i = 0; i < manyCompilationUnitsContents.length; i += 2) {
      String fileName = manyCompilationUnitsContents[i];
      String originalName = fileName;
      String dirName = null;
      int pos = fileName.lastIndexOf("/");
      if (pos != -1) {
        dirName = fileName.substring(0, pos);
        fileName = fileName.substring(pos + 1, fileName.length());
      }
      TempCompilationUnit tempCompilationUnit
          = new TempCompilationUnit(
          manyCompilationUnitsContents[i+1], fileName, dirName);
      units[i >> 1] = tempCompilationUnit;
      lastSetOfFiles.put(originalName, tempCompilationUnit);
    }
    units = (TempCompilationUnit[]) lastSetOfFiles.values()
        .toArray(new TempCompilationUnit[lastSetOfFiles.size()]);
    return createTestRbProjectFromString(units, true, false);
  }

  public static void flushHistory() {
    lastSetOfFiles.clear();
  }

  /**
   * @param  folderName  dots are alowed (not slashes), but then files are put in wrong folders (folder names will contain
   *         dots) -- for tests this should be OK because parser reckognizes the right package names anyway.
   *         Null is for default package.
   *
   * @param compilationUnitContents  must contain a "package" statement (if not default package)
   */
  public static Project createTestRbProjectFromString(String compilationUnitContents,
      String compilationUnitName, String folderName) throws Exception {
    return createTestRbProjectFromString(new TempCompilationUnit[] {new
        TempCompilationUnit(compilationUnitContents, compilationUnitName, folderName)});
  }

  public static Project createTestRbProjectFromStringRe(
      TempCompilationUnit[] compilationUnit) {
    try {
      return createTestRbProjectFromString(compilationUnit);
    } catch (Exception e) {
      throw new ChainableRuntimeException(e);
    }
  }

  /**
   * NOTE: Unlike other methods in this class, XXXfromString methods load the project (Project.load()).
   */
  public static Project createTestRbProjectFromString(
      TempCompilationUnit[] compilationUnit){

    return createTestRbProjectFromString(compilationUnit, true, false);
  }

  public static Project createTestRbProjectFromString(
      TempCompilationUnit[] compilationUnits, boolean doLoad,
      boolean ignoreParsingExceptions) {

    Source projectFolder = TempFileCreator.getInstance().createRootDirectory();

    HashMap foldersForPackages = new HashMap();

    for (int i = 0; i < compilationUnits.length; i++) {
      Source compilationUnitFolder;

      if (compilationUnits[i].folderName != null
          && (!"".equals(compilationUnits[i].folderName))) {
        compilationUnitFolder
            = (Source) foldersForPackages.get(compilationUnits[i].folderName);

        if (compilationUnitFolder == null) {
          compilationUnitFolder
              = createPackageFolder(projectFolder, compilationUnits[i].folderName);
          foldersForPackages.put(compilationUnits[i].folderName, compilationUnitFolder);
        }
      } else {
        compilationUnitFolder = projectFolder;
      }

      if (!compilationUnits[i].virtual) {
        if (compilationUnits[i].name == null) {
          compilationUnits[i].name = "X" + i + ".java";
        }

        if (compilationUnits[i].name != null) {
          writeNewTempFile(compilationUnitFolder, compilationUnits[i].name,
              compilationUnits[i].contents);
        }
      }
    }

    SourcePath sourcePath = TempFileCreator.getInstance().createSourcePath(
        projectFolder);
    ClassPath classPath = new LocalClassPath(ClasspathUtil.getDefaultClasspath());

    final Project result = createNewProjectFrom(new TestProject(
        "TestProject_"+System.currentTimeMillis(), sourcePath, classPath, null));
    if (doLoad) {
      try {
        result.getProjectLoader().build();
      } catch (SourceParsingException e) {
        for (Iterator i = (result.getProjectLoader().getErrorCollector()).getUserFriendlyErrors(); i.hasNext(); ) {
          System.out.println("UserFriendly Error on project loading: " + i.next());
        }

        if (!ignoreParsingExceptions) {
          throw new SystemException("TestError",e);
        }
      } catch (Exception e) {
        throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
      }
    }
    return result;
  }

  /** Example: createTestRbProject("bug107"); */
  public static Project createTestRbProject(String sourcePathFolderName)
  throws Exception {
    List sourcePaths = new ArrayList(1);

    StringTokenizer tokens = new StringTokenizer(sourcePathFolderName, ";");
    while (tokens.hasMoreTokens()) {
      sourcePaths.add(tokens.nextToken());
    }

    List classPaths = new ArrayList(1);
    for (int i = 0, max = sourcePaths.size(); i < max; i++) {
      final String sourcePath = (String) sourcePaths.get(i);
      if (sourcePath.endsWith(".java")) {
        int pos = sourcePath.lastIndexOf(File.separatorChar);
        if (pos < 0) {
          pos = sourcePath.lastIndexOf("/");
        }
        if (pos >= 0) {
          classPaths.add(sourcePath.substring(0, pos));
        }
      }
    }

    return createTestRbProject(
        new ProjectMetadata(sourcePathFolderName, sourcePathFolderName,
            null, sourcePaths, classPaths, null, false));
  }

  /**
   * Creates public class type with specified name.
   *
   * @param fqn FQN.
   *
   * @return class type.
   */
  public static BinClass createClass(String fqn) {
    return createClass(fqn, null);
  }

  /**
   * Creates public class type with specified name and superclass.
   *
   * @param fqn FQN.
   * @param superclass superclass.
   *
   * @return class type.
   */
  public static BinClass createClass(String fqn, BinCIType superclass) {
    BinTypeRef superclassRef = null;
    if (superclass != null) {
      superclassRef = superclass.getTypeRef();
      if (superclassRef == null) {
        superclassRef = new BinCITypeRef(superclass);
      }
    }

    final BinClass result
        = Utils.FakeBinClass.createClass(fqn, BinTypeRef.NO_TYPEREFS);

    // to be sure
    result.setProject(fakeProject);

    if (superclassRef == null && !"java.lang.Object".equals(fqn)) {
      superclassRef = result.getProject().getObjectRef();
    }

    if (superclassRef != null) {
      result.getTypeRef().setSuperclass(superclassRef);
    }

    return result;
  }

  /**
   * Creates fake project.
   *
   * @return project. Never returns <code>null</code>.
   */
  public static Project createFakeProject() {
    final Project project = createNewProjectFrom(
        new TestProject("", new FakeSourcePath(), new FakeClassPath(), null));

    fakeProject = project;

    createInterface("java.lang.Cloneable");
    createInterface("java.io.Serializable");

    project.objectRef = createClass(Project.OBJECT).getTypeRef();
    if (project.getTypeRefForName("java.lang.String") == null) {
      createClass("java.lang.String", project.objectRef.getBinCIType());
    }

    if (Assert.enabled) {
      Assert.must(project.objectRef != null, "Could not create Object");
    }

    FastJavaLexer.setJvmMode(Project.getDefaultOptions().getJvmMode());

    return project;
  }

  /**
   * Creates {@link BinTypeRef} for specified array type.
   *
   * @param elementTypeRef array element type.
   * @param dimensions number of dimensions of the array.
   *
   * @return type reference. Never returns <code>null</code>.
   */
  public static BinTypeRef createArrayTypeRef(BinTypeRef elementTypeRef,
      int dimensions) {
    if (fakeProject == null) {
      createFakeProject();
    }
    return fakeProject.createArrayTypeForType(elementTypeRef,
        dimensions);
  }

  /**
   * Creates public interface type with specified name.
   *
   * @param fqn FQN.
   *
   * @return interface type.
   */
  public static BinInterface createInterface(String fqn) {
    BinInterface result = FakeBinInterface.createInterface(fqn);
    result.setProject(fakeProject);
    return result;
  }

  public static Project createTestRbProjectFromXml(String projectName) {
    return Utils.createTestRbProject(
        Utils.getTestProjects().getProject(projectName));
  }

  public static Project createTestRbProjectFromFile(String fileName) throws
      Exception {
    return Utils.createTestRbProjectFromString(FileCopier.readFileToString(new
        File(fileName)),
        "A.java", null);
  }

  /** Fake BinClass. */
  private static class FakeBinClass extends BinClass {
    private final String fqn;

    FakeBinClass(String fqn,
        String name,
        String packageName,
        int modifiers,
        BinTypeRef[] implementedInterfaces) {
      super(new BinPackage(packageName, null, true),
          name,
          BinMethod.NO_METHODS,
          BinField.NO_FIELDS,
          null,
          BinConstructor.NO_CONSTRUCTORS,
          BinInitializer.NO_INITIALIZERS,
          BinTypeRef.NO_TYPEREFS,
          null,
          modifiers,
          null);

      this.fqn = fqn;

      if (fakeProject == null) {
        fakeProject = createFakeProject();
      }

      fakeProject.createCITypeRefForType(this);
      if(!"java.lang.Object".equals(getTypeRef().getQualifiedName())) {
        getTypeRef().setSuperclass(fakeProject.getObjectRef());
      }

      getTypeRef().setInterfaces(implementedInterfaces);
    }

    public static BinClass createClass(
        String fqn,
        BinTypeRef[] implementedInterfaces) {

      return createClass(fqn, implementedInterfaces, BinModifier.PUBLIC);
    }

    public static BinClass createClass(
        String fqn,
        BinTypeRef[] implementedInterfaces,
        int modifiers) {

      final int lastIndexOfDot = fqn.lastIndexOf('.');
      final String name;
      final String packageName;
      if (lastIndexOfDot < 0) {
        name = fqn;
        packageName = "";
      } else {
        name = fqn.substring(lastIndexOfDot + 1);
        packageName = fqn.substring(0, lastIndexOfDot);
      }

      return new FakeBinClass(fqn, name, packageName, modifiers,
          implementedInterfaces);
    }

    public String getQualifiedName() {
      return fqn;
    }

    public String toString() {
      return getQualifiedName();
    }
  }


  /** Fake BinInterface. */
  private static class FakeBinInterface extends BinInterface {
    private final String fqn;

    FakeBinInterface(String fqn,
        String name,
        String packageName,
        int modifiers) {
      super(new BinPackage(packageName, null, true),
          name,
          BinMethod.NO_METHODS,
          BinField.NO_FIELDS,
          null,
          BinTypeRef.NO_TYPEREFS,
          null,
          modifiers,
          null);

      this.fqn = fqn;

      fakeProject.createCITypeRefForType(this);
    }

    public static BinInterface createInterface(String fqn) {
      final int lastIndexOfDot = fqn.lastIndexOf('.');
      final String name;
      final String packageName;
      if (lastIndexOfDot < 0) {
        name = fqn;
        packageName = "";
      } else {
        name = fqn.substring(lastIndexOfDot + 1);
        packageName = fqn.substring(0, lastIndexOfDot);
      }

      return new FakeBinInterface(fqn,
          name,
          packageName,
          BinModifier.INTERFACE | BinModifier.PUBLIC);
    }

    public String getQualifiedName() {
      return fqn;
    }
  }


  /** Fake SourcePath */
  private static class FakeSourcePath implements SourcePath {
    public List getAllSources() {
      return new ArrayList();
    }

    public List getNonJavaSources(net.sf.refactorit.common.util.WildcardPattern[] patterns) {
      return new ArrayList();
    }

    public Source[] getRootSources() {
      return Source.NO_SOURCES;
    }

    public List getIgnoredSources() {
      return Collections.EMPTY_LIST;
    }

    private FileChangeMonitor silentMonitor =
        new FileChangeMonitor() {
      public void addFileChangeListener(FileChangeListener listener) {
      }

      public void removeFileChangeListener(FileChangeListener listener) {
      }

      public boolean hasPossiblePendingEvents() {
        return false;
      }
    };

    public FileChangeMonitor getFileChangeMonitor() {
      return silentMonitor;
    }

    /* (non-Javadoc)
     * @see net.sf.refactorit.vfs.SourcePath#getAutodetectedElements()
     */
    public Source[] getAutodetectedElements() {
      return null;
    }

    /**
     * method stub
     */
    public boolean isIgnoredPath(String pathStr) {
      return false;
    }

    /**
     * @see net.sf.refactorit.vfs.SourcePath#getPossibleRootSources()
     */
    public Source[] getPossibleRootSources() {
      throw new UnsupportedOperationException("method not implemented yet");
      //return null;
    }

  }


  /** Fake ClassPath */
  private static class FakeClassPath extends AbstractClassPath {
    public ClassPathElement[] getCachedElements() {
      return new ClassPathElement[0];
    }

    protected ClassPathElement[] createElements() {
      return new ClassPathElement[0];
    }

    public boolean delete(String cls) {
      return false;
    }

    public long lastModified(String cls) {
      return 0;
    }

    public long length(String cls) {
      return 0;
    }

    public InputStream getInputStream(String cls) {
      throw new RuntimeException("Not implemented");
    }

    public void release() {}

    public boolean isReleased() {
      return false;
    }

    public String getStringForm() {
      return "FAKE";
    }
  }


  /** FIXME: Here null means default package -- should be an empty string instead */
  public static class TempCompilationUnit {
    public String contents;
    public String name;
    public String folderName;
    public boolean virtual = false;

    /** Uses default package and creates a random name ("Axxx.java") for the class */
    public TempCompilationUnit(String contents) {
      this(contents, null, null);
    }

    public TempCompilationUnit(String[] contentLines) {
      this(StringUtil.join(contentLines, "\n"));
    }

    public TempCompilationUnit(String contents, String name,
        String folderNameWithoutSlashes) {
      this.contents = contents;
      this.name = name;
      this.folderName = folderNameWithoutSlashes;
    }
  }


  public static class TempSourceEmptyPackage extends TempCompilationUnit {
    public TempSourceEmptyPackage(String name) {
      super(null, null, name);
      virtual = true;
    }
  }


  /** Variable must be named as "i" */
  public static BinLocalVariable createLocalVariableDeclarationFromString(
      String declaration) {
    try {
      return (BinLocalVariable) ItemByNameFinder.findVariable(
          createTestRbProjectFromMethodBody(declaration), "i");
    } catch (Exception e) {
      throw new ChainableRuntimeException(e);
    }
  }

  public static Project createTestRbProjectFromMethodBody(String body) throws
      Exception {
    return createTestRbProjectFromArray(new String[] {
        "class X{ void m(){", body, "} }", });
  }

  public static Project createSimpleProject() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "class X { void m(){return;} }\n", "X.java", null);

    return p;
  }

  public static void assertEqualsIgnoreWhitespace(String expected, String got) {
    assertEqualsIgnoreWhitespace("", expected, got);
  }

  public static void assertEqualsIgnoreWhitespace(String message,
      String expected, String got) {
    junit.framework.Assert.assertEquals(message,
        normalizeWhitespace(expected), normalizeWhitespace(got));
  }

  public static String normalizeWhitespace(String s) {
    s = LinePositionUtil.useUnixNewlines(s);
    s = StringUtil.replace(s, "\t", " ");
    for (int i = 0; i < s.length(); i++) {
      s = StringUtil.replace(s, "  ", " ");
    }
    s = StringUtil.replace(s, "\"", "'");
    return s;
  }

  private static String normalizeAllWhitespace(String s) {
    s = normalizeWhitespace(s);
    s = StringUtil.replace(s, "\n", "");
    s = StringUtil.replace(s, " ", "");
    return s;
  }

  public static void assertEqualsIgnoreAllWhitespace(String expected,
      String got) {
    junit.framework.Assert.assertEquals(
        normalizeAllWhitespace(expected), normalizeAllWhitespace(got));
  }

  public static File createTempCopy(File real) throws IOException {
    Source tempDir = TempFileCreator.getInstance().createRootDirectory();
    FileUtil.copy(new LocalSource(real), tempDir);
    return tempDir.getChildren()[0].getFileOrNull();
  }

  public static Project createNewProjectFrom(TestProject prj) {
    Workspace workspace = IDEController.getInstance().getWorkspace();
    if (workspace instanceof NullWorkspace) { // for tests to emulate IDE projects
      NullWorkspace space = (NullWorkspace) workspace;
      space.addIdeProject(prj);
      return space.getProject(prj);
    } else if (workspace instanceof MockWorkspace) { // for tests to emulate IDE projects
      MockWorkspace space = (MockWorkspace) workspace;
      space.addIdeProject(prj);
      return space.getProject(prj);
    } else {
      return new Project(prj.getUniqueID(), prj.getSourcePath(), prj.getClassPath(),
          prj.getJavadocPath());
    }
  }

  /** Contains one class, LibraryClass */
  public static File getSomeJarFile() {
    return new File(new File(getTestProjectsDirectory(), "NbIntegrationTests"),
        "library.jar");
  }
}
