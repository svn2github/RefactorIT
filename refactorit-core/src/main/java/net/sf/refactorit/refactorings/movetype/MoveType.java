/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movetype;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.PackageUsageInfo;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.query.text.ManagingNonJavaIndexer;
import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.query.text.PathOccurrence;
import net.sf.refactorit.query.text.QualifiedNameIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.ExtendedConfirmationTreeTableModel;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.DirCreator;
import net.sf.refactorit.source.edit.FileCreator;
import net.sf.refactorit.source.edit.FileEraser;
import net.sf.refactorit.source.edit.FileRenamer;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.edit.MoveEditor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.PackageModel;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Performs "Move Type" refactoring implementation.
 *
 * @author Anton Safonov
 * @author Risto
 */
public class MoveType extends AbstractRefactoring {
  public static String key = "refactoring.movetype";

  private Set relocatedSources = new HashSet();

  private List types = null;
  private BinPackage targetPackage;
  private Source targetSource;
  private HashMap packageDirs = new HashMap();

  MoveTypeAnalyzer analyzer;

  private boolean userInputChecked = false;
  private boolean preconditionsChecked = false;

  private MultiValueMap importNodes = new MultiValueMap();
  private MultiValueMap removedNodes = new MultiValueMap();
  private List moveEditors = new ArrayList();

  private boolean changeMemberAccess;

  private boolean changeInNonJavaFiles = false;

  static final ProgressMonitor.Progress ANALYZER_FINDS_USAGES
      = new ProgressMonitor.Progress(0, 55);
  static final ProgressMonitor.Progress CONFLICT_CALCULATION
      = new ProgressMonitor.Progress(55, 70);
  private static final ProgressMonitor.Progress EDITOR_GENERATION
      = new ProgressMonitor.Progress(70, 80);
  private static final ProgressMonitor.Progress EDITORS_EDIT
      = new ProgressMonitor.Progress(80, 100);

  public MoveType(
      RefactorItContext context, Object objectsToMove,
      boolean changeMemberAccess) {
    super("Move Type", context);

    List types = new ArrayList();

    if (objectsToMove instanceof Object[]) {
      CollectionUtil.addAllNew(types, Arrays.asList((Object[]) objectsToMove));
    } else if (objectsToMove != null) {
      CollectionUtil.addNew(types, objectsToMove);
    }

    this.changeMemberAccess = changeMemberAccess;
    analyzer = new MoveTypeAnalyzer(changeMemberAccess);

    setTypes(types);
  }

  public MoveType(final RefactorItContext context, final Object objects) {
    this(context, objects, false);
  }

  public void setChangeMemberAccess(boolean b) {
    this.changeMemberAccess = b;
    this.analyzer.setChangeMemberAccess(b);
  }

  private RefactoringStatus extractType(final BinCIType type,
      final TypeDependencies usages, final TransformationList transList) {
    changeModifiersOfExtractedType(type, transList);
    RefactoringStatus status = fixInvocationsOfOuterTypeMembers(type, usages,
        transList);

    if (status.isErrorOrFatal()) {
      return status;
    }
    SourceHolder outputParent = getExtractedTypeNewDirectory(type, transList);

    // Source directory = null;
    // CompilationUnit packageUnit = new CompilationUnit(directory, type.getProject());

    SourceHolder output = new SimpleSourceHolder(getProject());
    output.setPackage(getTargetPackage());

    transList.add(new FileCreator(output, outputParent, type.getName(),
        FileCreator.CREATE_CRAZY_FILE));

    if (!targetPackage.isDefaultPackage()) {
      StringInserter packageStatementInserter
          = new StringInserter(output, 1, 0,
          "package " + targetPackage.getQualifiedName() + ";"
          + ImportUtils.generateNewlines(2));
      transList.add(packageStatementInserter);
    }

    //    boolean addedImportsForNewType =
    generateImportsForNewType(usages, transList, output);
    //    boolean addedImportsForOuterTypes =

    if (type.getOwner() == null){
      if (!targetPackage.isDefaultPackage()) {
        // FIXME: need to use editor to add Imports?
        addImportOfTypeToOtherTypes(type, usages, status);
      }
    }

    generateImportsForOuterTypes(usages, type);

    List toMove = new ArrayList();

    Comment comment = Comment.findFor(type);
    if (comment != null) {
      toMove.add(comment);
    }

    toMove.add(type);

    final MoveEditor mover = new MoveEditor(toMove, output,
        new SourceCoordinate(1, 0), 0 /* getShiftOnExtract(type) */);

    moveEditors.add(mover);

    return new RefactoringStatus();
  }

  private SourceHolder getExtractedTypeNewDirectory(final BinCIType type,
      TransformationList transList) {
    boolean changeDir = type.getPackage().isNameMatchesDir();

    SourceHolder parent = null;

    if (changeDir) {
      parent = findDestinationDir(type, transList);
    }

    if (parent == null) {
      Source input = type.getCompilationUnit().getSource();
      parent = new SimpleSourceHolder(input.getParent(), getProject());
    }

    return parent;
  }

  private void changeModifiersOfExtractedType(final BinCIType type,
      final TransformationList transList) {
    int newModifier = type.getModifiers();
    if (willBeTurnedToPublicOnExtract(type, targetPackage)) {
      newModifier = BinModifier.setFlags(newModifier, BinModifier.PUBLIC);
    }

    if (type.isStatic()) {
      newModifier = BinModifier.clearFlags(newModifier, BinModifier.STATIC);
    }

    if (newModifier != type.getModifiers()) {
      transList.add(new ModifierEditor(type, newModifier));
    }
  }

  private RefactoringStatus fixInvocationsOfOuterTypeMembers(
      BinCIType movedType, TypeDependencies usages,
      final TransformationList transList) {
    SpecialSinglePointVisitor visitor = new SpecialSinglePointVisitor(
        movedType, transList, usages);
    movedType.accept(visitor);

    visitor.clear();

    return visitor.getResult();
  }

  boolean usedTypeIsSuperclassOfSomeOwner(BinCIType usedType,
      BinCIType movedType) {
    BinTypeRef owner = movedType.getOwner();

    while (owner != null) {
      BinTypeRef[] superTypes = owner.getSupertypes();
      for (int i = 0, max = superTypes.length; i < max; i++) {
        BinCIType superTypeOfOwner = superTypes[i].getBinCIType();
        if (superTypeOfOwner.getQualifiedName().equals(
            usedType.getQualifiedName())) {
          return true;
        }
      }

      owner = owner.getBinCIType().getOwner();
    }

    return false;
  }

  static boolean typeIsInSameFileButNotInsideMovedType(BinCIType type,
      BinCIType movedType) {
    if (!type.isFromCompilationUnit()) {
      return false;
    }

    if (!type.getCompilationUnit().getSource().getAbsolutePath().equals(
        movedType.getCompilationUnit().getSource().getAbsolutePath())) {
      return false;
    }

    if (type.getQualifiedName().equals(movedType.getQualifiedName())) {
      return false;
    }

    if (type.getOwner() == null) {
      return true;
    } else {
      return typeIsInSameFileButNotInsideMovedType(type.getOwner()
          .getBinCIType(), movedType);
    }
  }

  private static boolean willBeTurnedToPublicOnExtract(final BinCIType type,
      BinPackage targetPackage) {
    return!type.isPublic()
        && (!type.isPackagePrivate() || !type.getPackage().isIdentical(
        targetPackage));
  }

  static boolean willBeTurnedToPublicOnExtract(BinCIType type,
      BinPackage targetPackage, List types) {
    // If the type is not a top-level type, then it is not turned to public even
    // if the outer
    // type is extracted and is turned to public.
    if (!types.contains(type)) {
      return false;
    }

    return willBeTurnedToPublicOnExtract(type, targetPackage);
  }

  // FIXME better use BinTypeFormatter
  /*
   * private int getShiftOnExtract(BinCIType type) { int tabSize =
   * FormatSettings.getTabSize();
   *
   * String typeStartLine = LinePositionUtil.extractLine( type.getStartLine(),
   * type.getCompilationUnit().getContent());
   *
   * int shift = 0;
   *
   * for (int x = 0; x < type.getStartColumn() - 1; x++) { if
   * (typeStartLine.charAt(x) == ' ') { shift++; } else if
   * (typeStartLine.charAt(x) == '\t') { shift += tabSize; } else { // Some
   * other characters on the line before the typedef begins, // let's not shift
   * at all.
   *
   * shift = 0; break; } } //System.err.println(type + ", shift: " + shift);
   *
   * return shift; }
   */

  private boolean generateImportsForOuterTypes(TypeDependencies usages,
      BinCIType type) {
    if (type.getPackage().isIdentical(targetPackage)) {
      return false;
    }

    boolean addedSomeImports = false;

    for (int i = 0; i < usages.usedList.size(); i++) {
      InvocationData data = (InvocationData) usages.usedList.get(i);
      if (!(data.getWhat() instanceof BinMember) || data.getWhereType() == null) {
        continue;
      }

      if (callerIsMovedButCalledItemIsNot(data)
          && (callerAndCalledItemFromSameFile(type, data)
              || callerInheritsCalledItem(type, data))
          && !ImportUtils.isFqnUsage(type.getCompilationUnit(),
              data.getWhereAst())) {
        importNodes.putAll(data.getCompilationUnit(),
            targetPackage.getQualifiedForShortname(MoveTypeUtil.getType(
            (BinMember) data.getWhat()).getName()));
        addedSomeImports = true;
      }
    }

    return addedSomeImports;
  }

  private boolean callerIsSubtypeOfItem(final BinCIType type,
      final InvocationData data){
    return data.getWhereType().isDerivedFrom(type.getTypeRef());
  }

  private boolean callerInheritsCalledItem(final BinCIType type,
      final InvocationData data) {
    return (data.getWhereType().getBinCIType().getAccessibleInners(data.
        getWhereType().getBinCIType()).contains(type));
  }

  private boolean callerAndCalledItemFromSameFile(final BinCIType type,
      final InvocationData data) {
    return data.getWhereType().getBinCIType().getCompilationUnit().getSource()
        .getAbsolutePath().equals(
        type.getCompilationUnit().getSource().getAbsolutePath());
  }

  private boolean callerIsMovedButCalledItemIsNot(final InvocationData data) {
    return analyzer.isMoving(MoveTypeUtil.getType((BinMember) data.getWhat()))
        && (!analyzer.isMoving(data.getWhereType().getBinCIType()));
  }

  private boolean generateImportsForNewType(final TypeDependencies usages,
      final TransformationList transList, SourceHolder output) {
    List toBeImported = new ArrayList();

    for (int i = 0, max = usages.usesList.size(); i < max; i++) {
      final InvocationData data = (InvocationData) usages.usesList.get(i);
      BinMember member = (BinMember) data.getWhat();
      member = getOwnerClass(member);

      if (analyzer.isMoving(member)) {
        continue;
      }

      if (member == null) {
        //System.err.println("Member called is null: " + data);
        continue;
      }

      // For usage of java.lang.Object, for example
      if (data.getCompilationUnit() == null) {
        continue;
      }

      if (ImportUtils.isFqnUsage(data.getCompilationUnit(), data.getWhereAst())) {
        continue;
      }

      if (!(member instanceof BinCIType)) {
        member = member.getOwner().getBinCIType();
      }

      if (member.getPackage().isIdentical(targetPackage)) {

        if (((BinCIType) member).isInnerType()) {
          if ((data.getWhereAst().getParent().getType() != JavaTokenTypes.DOT)
              || (data.getWhereAst().getParent().getFirstChild()
              != data.getWhereAst())) {
            CollectionUtil.addNew(toBeImported, member);
          }
        }
        continue;
      }

      if (member.getQualifiedName().startsWith("java.lang.")) {
        continue;
      }

      CollectionUtil.addNew(toBeImported, member);
    }

    Collections.sort(toBeImported, new BinMember.QualifiedNameSorter());

    for (int i = 0, max = toBeImported.size(); i < max; i++) {
      final BinType importable = (BinType) toBeImported.get(i);

      int newLines = i == max - 1 ? 2 : 1;
      StringBuffer linebreaks = ImportUtils.generateNewlines(newLines);

      StringInserter e = new StringInserter(output, 1, 0,
          ImportUtils.generateImportClause(importable.getQualifiedName()).
          toString()
          + linebreaks.toString());
      transList.add(e);
    }

    return toBeImported.size() > 0;
  }

  private RefactoringStatus relocate(final BinCIType type,
      TransformationList transList) {
    RefactoringStatus status = new RefactoringStatus();

    /**
     * TODO this code does not make sense, but it was made for some reason.
     * If "&& relocate" commented out below were used, it would be logically
     * correct, but in that case tests fall. Make out whether there is some bug
     * and these lines are actually needed or they are not needed at all and
     * can be deleted
     *
     *boolean relocate = true;
     *
     *relocate = type.isNameMatchesSourceName() && relocate;
     *
     * //it is not a bug here - relocate is called when we already know that
     * //nothing is left
     *relocate = type.getCompilationUnit().getIndependentDefinedTypes().size()
     *    == 1 && relocate;
     */

    boolean relocate = type.getPackage().isNameMatchesDir();// && relocate;

    if (relocate) {
      SourceHolder destination = findDestinationDir(type, transList);

      if (destination != null) {
        String path = AbstractSource.normalize(destination.getDisplayPath());

        if (doesSuchSourcealreadyExist(path, type.getName())) {
          List items = new ArrayList(1);
          items.add(type.getCompilationUnit());
          status.addEntry("File with name " + type.getName() +
              FileUtil.JAVA_FILE_EXT + " already exists in directory " +
              path, RefactoringStatus.ERROR);
        }

        relocate = moveFile(destination, type, transList);
      } else {
        relocate = false;
      }
    }

    if (!relocate) {
      List items = new ArrayList(1);
      items.add(type.getCompilationUnit());
      status
          .addEntry(
          "Unable to relocate source files (logical structures were updated sucessfully)",
          items, RefactoringStatus.WARNING);
    }

    return status;
  }

  private boolean doesSuchSourcealreadyExist(String path, String name) {
    Source[] roots =
        getProject().getPaths().getSourcePath().getRootSources();
    Source targetDir = SourceUtil.findSource(roots, path);
    //Source targetDir = getSourceForName(path);

    if (targetDir != null) {
      Source[] children = targetDir.getChildren();

      for (int i = 0; i < children.length; i++) {
        String childName = children[i].getName();

        if (childName.equals(name + FileUtil.JAVA_FILE_EXT)) {
          return true;
        }
      }
    }

    return false;
  }

  private SourceHolder findDestinationDir(final BinCIType type,
      TransformationList transList) {
    SourceHolder destination = null;


    // first - use existing path
    destination = findDestinationUseNewPackagePath();
    if (destination != null) {
      return destination;
    }

    // second - try to branch from old package path
    destination = findDestinationBranchFromOldPackage(type, transList);
    if (destination != null) {
      return destination;
    }

    // third - try to find new path from scratch based on overall project types
    destination = findDestinationBaseOnAllTypes(type, transList);
    if (destination != null) {
      return destination;
    }

    // forth - stick to given target path, create missing part of dirs
    destination = findDestinationStickToTargetPath(transList);
    if (destination != null) {
      return destination;
    }

    return null; //return destination;
  }

  private SourceHolder findDestinationUseNewPackagePath() {
    Source destination;
    destination = targetPackage.getDir();
    if (destination != null) {
      if (!PackageModel.isChild(getTargetSource(), destination)) {
        destination = null;
      }
    }

    if (destination != null) {
      return new SimpleSourceHolder(destination, getProject());
    } else {
      return null;
    }

  }

  private SourceHolder findDestinationBranchFromOldPackage(final BinCIType type,
      TransformationList transList) {
    Source packageDir = (Source) packageDirs.get(type.getPackage());
    if (packageDir == null) {
      packageDir = type.getPackage().getDir();
    }
    Source destination = type.getPackage().getNewDir(
        targetPackage.getQualifiedName(), packageDir);
    if (destination != null
        && PackageModel.isChild(getTargetSource(), destination)) {
      String path = FileUtil.getCommonPath(
          type.getPackage().getQualifiedName(), targetPackage
          .getQualifiedName());
      path = targetPackage.getQualifiedName().substring(path.length());

      if (path.startsWith(".")) {
        path = path.substring(1);
      }

      path = path.replace('.', Source.RIT_SEPARATOR_CHAR);
//      CompilationUnit destinationDirectory = new CompilationUnit(null, type.getProject());

      SourceHolder targetDirectory = new SimpleSourceHolder(getProject());

//      destination = destination.mkdirs(path);
      transList.add(new DirCreator(new SimpleSourceHolder(destination,
          getProject()), targetDirectory, path, true));

      return targetDirectory;
    } else {
      destination = null;
      return null;
    }
 }

 private SourceHolder findDestinationBaseOnAllTypes(final BinCIType oldType,
     TransformationList transList) {
   SourceHolder destination = null;

    List typeRefs = getProject().getDefinedTypes();
    for (int i = 0, max = typeRefs.size(); i < max; i++) {
      final BinCIType type = ((BinTypeRef) typeRefs.get(i)).getBinCIType();
      if (!oldType.getPackage().isIdentical(type.getPackage())
          && type.getPackage().isNameMatchesDir()
          && PackageModel.isChild(getTargetSource(), type.getCompilationUnit()
          .getSource())) {
        destination = findDestinationBranchFromOldPackage(type, transList);
        if (destination != null) {
          break;
        }
      }
    }

    return destination;
  }

  private SourceHolder findDestinationStickToTargetPath(TransformationList transList) {
    SourceHolder destination = null;

    String targetPath = getTargetSource().getAbsolutePath();
    targetPath = BinPackage.convertPathToPackageName(targetPath);

    String packagePath = targetPackage.getQualifiedName();

    if (!targetPath.endsWith(packagePath)) {
      int pos;

      // HACK: this is a hack actually - we analyze if package and target path
      // have at least 2 parts matching, then just don't move
      // this should happen when moving to package outside of too narrow
      // sourcepath
      pos = 0;
      for (int i = 0; i < 2; i++) {
        pos = packagePath.indexOf('.', pos);
        if (pos == -1) {
          break;
        }
        ++pos;
      }
      if (pos < 0) {
        pos = packagePath.length();
      }
      if (targetPath.indexOf(packagePath.substring(0, pos)) != -1) {
        return null; // don't move, there is smth strange with the paths
      }
      // end of hack

      String dirsToMake = null;

      String path = packagePath;
      while ((pos = path.lastIndexOf('.')) != -1) {
        path = path.substring(0, pos);
        if (targetPath.endsWith(path)) {
          dirsToMake = packagePath.substring(pos);
          break;
        }
      }
      if (dirsToMake == null && path != null && path.length() > 0) {
        dirsToMake = packagePath; // just create all packages into the target
        // source
      }

      if (dirsToMake != null && dirsToMake.length() > 0) {
        if (dirsToMake.startsWith(".")) {
          dirsToMake = dirsToMake.substring(1);
        }

        dirsToMake = dirsToMake.replace('.', Source.RIT_SEPARATOR_CHAR);

        //destination = getTargetSource().mkdirs(dirsToMake);

        destination = new SimpleSourceHolder(getProject());

        transList.add(new DirCreator(
            new SimpleSourceHolder(getTargetSource(), getProject()),
            destination, dirsToMake, true));

      }
    } else {
      Source destSource = getTargetSource();
      if (destSource != null) {
        destination = new SimpleSourceHolder(destSource, getProject());
      }
    }

    return destination;
  }

  private boolean moveFile(SourceHolder destination, BinCIType type,
      TransformationList transList) {
     final Source oldDir = type.getCompilationUnit().getSource().getParent();

    /*
     * Deletes old .class file if exists Must be before type.renameTo();
     */
    String cls = type.getQualifiedName().replace('.', '/')
        + ClassFilesLoader.CLASS_FILE_EXT;
    boolean deletedClass = type.getProject().getPaths().getClassPath().delete(cls);
    //    System.err.println("Deleted Class for type: " + type + " - " +
    // deletedClass);
    if (!deletedClass && oldDir.getChild(type.getName() +
        ClassFilesLoader.CLASS_FILE_EXT) != null) {
      //oldDir.getChild(type.getName() + ".class").delete();
      transList.add(new FileEraser(
          new SimpleSourceHolder(oldDir.getChild(type.getName() +
          ClassFilesLoader.CLASS_FILE_EXT),
          getProject())));
    }

    SourceHolder source = type.getCompilationUnit();
    moveAll(filesToMoveWithCompilationUnits.getFilesToMoveWith(source.getSource()),
        destination, transList, type.getProject());

    transList.add(new FileRenamer(source, destination, source.getName()));
    //System.err.println("newSource: " + newSource);
//    if (newSource == null) {
//      return false;
//    }

//    type.getCompilationUnit().setSource(newSource);
    type.setDefinitelyInWrongFolder(true);

    transList.add(new FileEraser(new SimpleSourceHolder(oldDir, type.getProject())));

    return true;
  }

/*
  private Source getSourceForName(String name) {
    name = BinPackage.convertPathToPackageName(name);

    Source[] roots = getProject().getPaths().getSourcePath().getRootSources();

    for (int i = 0; i < roots.length; i++) {
      String srcName =
          BinPackage.convertPathToPackageName(roots[i].getAbsolutePath());

      if ((name + ".").startsWith(srcName + ".")) {
        name = name.substring(srcName.length());

        if(name.startsWith(".")) {
          name = name.substring(1);
        }

        name = StringUtil.replace(name, '.', Source.RIT_SEPARATOR_CHAR);

        return roots[i].getChild(name);
      }
    }

    return null;
  }
*/

  private void changePackageDeclaration(final CompilationUnit source,
      final BinPackage oldPackage, final TransformationList transList) {

    if (!"".equals(oldPackage.getQualifiedName())) {
      List packages = source.getPackageUsageInfos();
      ASTImpl node = null;

      if (packages.size() > 0) {
        PackageUsageInfo data = (PackageUsageInfo) packages.get(0);
        if (data.getBinPackage().isIdentical(oldPackage)) {
          node = data.getNode();
        }
      }

      if (Assert.enabled) {
        Assert.must(node != null, "Couldn't find package definition node: "
            + source);
      }

      if (node != null) {
        if (!targetPackage.isDefaultPackage()) {
          final CompoundASTImpl packageNode = new CompoundASTImpl(node);

          transList.add(new RenameTransformation(source, CollectionUtil
              .singletonArrayList(packageNode), targetPackage
              .getQualifiedName()));
        } else {
          while (node != null && node.getType() != JavaTokenTypes.PACKAGE_DEF) {
            node = node.getParent();
          }

          if (Assert.enabled) {
            Assert.must(node != null, "Couldn't find PACKAGE_DEF node: "
                + source);
          }

          if (!this.removedNodes.contains(source, node)) {
            node = new CompoundASTImpl(node);
            final StringEraser eraser = new StringEraser(source, node, true);
            this.removedNodes.putAll(source, node);
            eraser.setTrimTrailingSpace(true);
            transList.add(eraser);
          }
        }
      }
    } else {
      transList.add(new StringInserter(source, 1, 0, "package "
          + targetPackage.getQualifiedName() + ";" + FormatSettings.LINEBREAK));
    }

    return;
  }

  /**
   * FQN imports are also modified in this method (and removed completely if
   * needed).
   */
  private RefactoringStatus updateQualifiedUsageOfType(TypeDependencies usages,
      final TransformationList transList) {

    RefactoringStatus status = new RefactoringStatus();

    Iterator it = usages.used.keySet().iterator();
    while (it.hasNext()) {
      final CompilationUnit source = (CompilationUnit) it.next();
      //System.err.println("Source: " + source);
      List nodes = usages.used.get(source);

      //System.err.println("Nodes size: " + nodes.size());

      // The list can contain duplicates when some node is considered to be part
      // of 2 classes -- for instance, a local class creation is one such
      // example.
      CollectionUtil.removeDuplicates(nodes, invocationDataAstComparator);

      for (int i = 0, max = nodes.size(); i < max; i++) {
        final InvocationData data = (InvocationData) nodes.get(i);

        final ASTImpl node = data.getWhereAst();
        //System.err.println("Node: " + node);

        if (ImportUtils.isChildOfImportNode(source, node)
            && targetPackage.isDefaultPackage()) {
          ASTImpl importNode = ImportUtils.getImportNode(source, node);
          if (importNode != null
              && !this.removedNodes.contains(source, importNode)) {
            if (FileUtil.isJspFile(source.getSource().getAbsolutePath())) {
              status.merge(removeImportFromJsp(source, importNode));
            } else {
              CompoundASTImpl compoundNode = new CompoundASTImpl(importNode);
              //System.err.println("importNode: " + importNode);
              //editor.addEditor(new StringEraser(source, compoundNode, true));
              transList.add(new StringEraser(source, compoundNode, true));
              this.removedNodes.putAll(source, importNode);
            }
          }
        } else {
          ASTImpl parent = ImportUtils.getPackageAndOwnersNode(source, node);
          if (parent != null && !this.removedNodes.contains(source, parent)) {
            CompoundASTImpl packageAndOwnersNode = new CompoundASTImpl(parent);
            //System.err.println("Updating package in FQN: " + packageNode);
            if (!targetPackage.isDefaultPackage()) {
              // We use different RenameEditor for every node for safety -
              // - some of them might refer to different names
              // (sometimes a type could be referenced via its owner,
              // sometimes via owner's subclass, etc).
              // For an example, see
              // MoveTypeTest.testDifferentFqnsReferingToSameType().

              transList.add(new RenameTransformation(source, CollectionUtil
                  .singletonArrayList(packageAndOwnersNode), targetPackage
                  .getQualifiedName()));
            } else {
              transList.add(new StringEraser(source, packageAndOwnersNode,
                  true));
              this.removedNodes.putAll(source, parent);
            }
          }
        }
      }
    }

    return status;
  }

  /**
   * If the target was imported in the old location then this method does not
   * touch the import statement -- it just assumes that the existing import
   * statements get updated (or even removed) elsewhere.
   */
  private boolean addImportOfTypeToOtherTypes(BinCIType type,
      TypeDependencies usages, RefactoringStatus status) {
    Iterator it = usages.used.keySet().iterator();
    while (it.hasNext()) {
      final CompilationUnit source = (CompilationUnit) it.next();

      // check if there is at least one non-FQN usage and we need the import
      boolean foundNonFQNUsage = false;
      List datas = usages.used.get(source);
      for (int i = 0, max = datas.size(); i < max; i++) {
        final InvocationData data = (InvocationData) datas.get(i);
        if (!ImportUtils.isFqnUsage(source, data.getWhereAst())) {
          Object location = data.getWhere();

          if (location instanceof CompilationUnit) {
            status.addEntry("Invalid import statement in "
                + data.getCompilationUnit().getDisplayPath() + ":"
                + data.getLineNumber(), RefactoringStatus.ERROR);
            return false;
          }

          if (location instanceof BinTypeRef
              && ((BinTypeRef) location).isReferenceType()) {
            location = ((BinTypeRef) location).getBinCIType();
          }

          if (analyzer.isMoving((BinMember) location)) {
            continue;
          }

          while (location != null && location != type) {
            BinTypeRef owner = ((BinMember) location).getOwner();
            if (owner != null) {
              location = owner.getBinCIType();
            } else {
              location = null;
            }
          }
          if (location != type) {
            foundNonFQNUsage = true;
            break;
          }
        }
      }

      if (foundNonFQNUsage
          && ImportUtils.needsTypeImported(source, type, targetPackage)) {
        importNodes.putAll(source, targetPackage.getQualifiedName() + "."
            + type.getName());
      }
    }

    return true;
  }

  private boolean addImportOfOldPackageTypesToType(BinCIType type,
      TypeDependencies usages) {
    //    ImportUtils.ImportPosition importPosition
    //       = ImportUtils.calculateNewImportPosition(type.getCompilationUnit(),
    //          type.getPackage().isDefaultPackage());

    //    List toBeImported = new ArrayList();

    Iterator it = usages.uses.keySet().iterator();
    while (it.hasNext()) {
      final CompilationUnit source = (CompilationUnit) it.next();
      //System.err.println("Source: " + source);
      //System.err.println("Type source: " + type.getCompilationUnit());

      final List datas = usages.uses.get(source);

      for (int i = 0, max = datas.size(); i < max; i++) {
        final InvocationData data = (InvocationData) datas.get(i);

        BinMember whatUsed = (BinMember) data.getWhat();

        if (usedTypeImportedDirectly(whatUsed, type)) {
          continue;
        }

        whatUsed = getTopLevelOwner(whatUsed);

        if (analyzer.isMoving(whatUsed)) {
          continue;
        }

        //System.err.println("what used: " + whatUsed);

        if (!((BinCIType) whatUsed).isFromCompilationUnit()) {
          // should be imported already, anyway not in our old package
          continue;
        }

        //        if (((BinCIType) whatUsed).getCompilationUnit() == type.getCompilationUnit()) {
        //          continue; // they are within the same file, so no imports
        //        }

        if (data.getWhereAst().getType() == JavaTokenTypes.SUPER_CTOR_CALL) {
          continue;
        }

        if (ImportUtils.isFqnUsage(type.getCompilationUnit(), data.getWhereAst())) {
          continue;
        }

        if (whatUsed.getQualifiedName().startsWith("java.lang.")) {
          continue;
        }

        BinPackage packageUsed = whatUsed.getPackage();

        // HACK setting future package to the source to get correct result
        type.getCompilationUnit().setPackage(targetPackage);
        if (!packageUsed.isIdentical(targetPackage)
            && ImportUtils.needsTypeImported(type.getCompilationUnit(),
            whatUsed,
            packageUsed)) {
          importNodes.putAll(type.getCompilationUnit(), whatUsed);
        }
        type.getCompilationUnit().setPackage(type.getPackage());
      }
    }

    return true;
  }

  /** Supports everything, incl inner types, array types and their combinations */
  private boolean usedTypeImportedDirectly(BinMember whatUsed,
      BinCIType movedType) {
    BinType t = MoveTypeUtil.getType(whatUsed);
    return movedType.getCompilationUnit().importsTypeDirectly(
        t.getTypeRef().getNonArrayType().getQualifiedName());
  }

  /** Supports everything, incl inner types, array types and their combinations */
  private BinCIType getTopLevelOwner(BinMember whatUsed) {
    return whatUsed.getTopLevelEnclosingType().getTypeRef().getNonArrayType()
        .getBinCIType().getTopLevelEnclosingType();
//
//    while (whatUsed.getOwner() != null) {
//      whatUsed = whatUsed.getOwner().getBinCIType();
//    }
//
//    // Now we definitely have some type, but it could be an array type
//    while (whatUsed instanceof BinArrayType) {
//      whatUsed = ((BinArrayType) whatUsed).getArrayType().getBinType();
//    }
//
//    // If the type was an array type then it might still be an inner type,
//    // otherwise we don't need this:
//    while (whatUsed.getOwner() != null) {
//      whatUsed = whatUsed.getOwner().getBinCIType();
//    }
//
//    return (BinCIType) whatUsed;
  }

  private BinCIType getOwnerClass(BinMember whatUsed) {
    while (!(whatUsed instanceof BinCIType)) {
      whatUsed = whatUsed.getOwner().getBinCIType();
    }

    // Now we definitely have some type, but it could be an array type
    while (whatUsed instanceof BinArrayType) {
      whatUsed = ((BinArrayType) whatUsed).getArrayType().getBinType();
    }

    return (BinCIType) whatUsed;
  }

  /**
   * To avoid such code:
   *
   * <pre>
   * package a;
   *
   * import a.X;
   * import a.*;
   *
   *
   * </pre>
   *
   * Also removes imports of these types that are already in target package.
   */
  private RefactoringStatus removeSamePackageImportsFromSource(
      final CompilationUnit source, final BinCIType type,
      final TransformationList transList) {

    RefactoringStatus status = new RefactoringStatus();
    final List toRemove = new ArrayList();
    List datas = source.getPackageUsageInfos();
    if (datas != null) {
      for (int i = 0, max = datas.size(); i < max; i++) {
        final PackageUsageInfo data = (PackageUsageInfo) datas.get(i);
        if (data.getBinPackage().isIdentical(targetPackage)) {
          ASTImpl node = ImportUtils.getImportNode(source, data.getNode());
          // NOTE: it is null also when it is package declaration,
          // which we actually don't want to remove here at all
          if (node != null) {
            if (!ImportUtils.isFqnInnerTypeUsage(source, data.getNode())) {
              // Inner types still need import statements, even in the sources
              // that are
              // in their own package.

              CollectionUtil.addNew(toRemove, node);
            }
          }
        }
      }
    }

    class TypeRefVisitor extends BinTypeRefVisitor {
      TypeRefVisitor() {
        setCheckTypeSelfDeclaration(false);
        setIncludeNewExpressions(true);
      }

      public void visit(BinTypeRef data) {
        if (data.getTypeRef().getPackage().isIdentical(targetPackage)
            || data.getTypeRef().getBinType() == type) {
          ASTImpl node = ImportUtils.getImportNode(source, data.getNode());

          if (node != null) {
            if (!ImportUtils.isFqnInnerTypeUsage(source, data.getNode())) {
              // Inner types still need import statements, even in the sources
              // that are
              // in their own package.

              CollectionUtil.addNew(toRemove, node);
            }
          }
        }

        super.visit(data);
      }
    };

    source.accept(new TypeRefVisitor());

    if (toRemove.size() > 0) {
      //      if (source.getSource().getAbsolutePath().indexOf("Rebuild") >= 0) {
      //        StringWriter sw = new StringWriter();
      //        new Exception("import remove").printStackTrace(new PrintWriter(sw));
      //        String message = sw.getBuffer().toString();
      //        message = "ToRemove: " + toRemove.toString() + "\n" + message;
      //        System.err.println(message);
      //        JOptionPane.showMessageDialog(DialogManager.getDialogParent(),
      //            message);
      //      }

      CollectionUtil.removeDuplicates(toRemove);
      List alreadyRemoved = this.removedNodes.get(source);
      if (alreadyRemoved != null) {
        toRemove.removeAll(alreadyRemoved);
      }
      this.removedNodes.putAll(source, toRemove);

      if (toRemove.size() > 0) {
        if (FileUtil.isJspFile(source.getSource().getAbsolutePath())) {
          for (int i = 0; i < toRemove.size(); i++) {
            status
                .merge(removeImportFromJsp(source, (ASTImpl) toRemove.get(i)));
          }
        } else {
          StringEraser.addNodeRemovingEditors(transList, toRemove, source, true);
        }
      }
    }

    return status;
  }

  /**
   * To avoid such code:
   *
   * <pre>
   * package a;
   *
   * import a.X;
   *
   *
   * </pre>
   *
   * When we move the type to the package where types of that package have an
   * import of this type already.
   */
  private RefactoringStatus removeTypeImportFromNewPackageSources(
      final BinCIType type, final TransformationList transList) {
    //System.err.println("removeTypeImportFromNewPackageSources: " + type);
    RefactoringStatus status = new RefactoringStatus();
    List sources = targetPackage.getCompilationUnitList();

    for (int i = 0; i < sources.size(); i++) {
      final CompilationUnit source = (CompilationUnit) sources.get(i);

      status.merge(removeSamePackageImportsFromSource(source, type, transList));
    }

    return status;
  }

  public List getTypes() {
    return this.types;
  }

  public void setTypes(final List types) {
    this.types = types;

    // remove inners which will be moved together with their owners
    Iterator it = types.iterator();
    while (it.hasNext()) {
      final BinCIType potentialInner = (BinCIType) it.next();
      if (potentialInner.isInnerType()) {
        for (int i = 0; i < types.size(); i++) {
          final BinCIType potentialOwner = (BinCIType) types.get(i);
          if (potentialInner.isOwnedBy(potentialOwner)) {
            it.remove();
            break;
          }
        }
      }
    }

    analyzer.setTypes(this.types);
  }

  public void setTargetPackage(final BinPackage newPackage) {
    this.targetPackage = newPackage;
  }

  public BinPackage getTargetPackage() {
    return this.targetPackage;
  }

  public void setTargetSource(Source targetSource) {
    this.targetSource = targetSource;
  }

  public Source getTargetSource() {
    if (targetSource == null) {
      targetSource = ((BinType) types.get(0)).getProject().getPaths().getSourcePath()
          .getRootSources()[0];
    }
    return this.targetSource;
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#checkPreconditions
   */
  public RefactoringStatus checkPreconditions() {
    preconditionsChecked = true;

    return analyzer.checkPreconditions();
  }

  public RefactoringStatus checkTargetPackage() {
    return MoveTypeAnalyzer.checkTargetPackage(types, targetPackage);
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#checkUserInput
   */
  public RefactoringStatus checkUserInput() {
    userInputChecked = true;

    RefactoringStatus status = new RefactoringStatus();

    if (targetPackage == null) {
      status.addEntry("", RefactoringStatus.CANCEL);
      return status;
    }

    status.merge(analyzer.calculateConflicts(targetPackage));

    return status;
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#performChange
   */
  public TransformationList performChange() {
    /*
     * SwingUtil.invokeInEdtUnderNetBeans( new Runnable() { public void run() {
     */
    return performChangeInCurrentThread();
    /*
     * } } );
     */
  }

  public TransformationList performChangeInCurrentThread() {
    TransformationList transList = new TransformationList();
    getTargetSource(); // ensure we have some target source, mainly for legacy
    // tests

    if (targetPackage == null) {
      transList.getStatus().merge(new RefactoringStatus(
          "Target package must be specified!",
          RefactoringStatus.ERROR));
      return transList;
    }

    removeTypesThatAreFromTargetPackage();

    // Check preconditions and user input if the caller forgot to do it --
    // mainly for tests
    {
      if (!preconditionsChecked) {
        transList.getStatus().merge(checkPreconditions());
        if (transList.getStatus().isErrorOrFatal()) {
          return transList;
        }
      }
      if (!userInputChecked) {
        transList.getStatus().merge(checkUserInput());
        if (transList.getStatus().isErrorOrFatal()) {
          return transList;
        }
      }
    }

    removeInnersToBeMovedInsideOwners();

    ProgressMonitor.Progress progress = EDITOR_GENERATION;

    // this is done firsts, because user may cancel the action
    if (changeInNonJavaFiles) {
      transList.getStatus().merge(changeTypesInNonJavaFiles(types, transList));
      if (transList.getStatus().isCancel()) {
        return transList;
      }
    }

    Set movedSources = new HashSet(types.size());
    for (int i = 0; i < types.size(); i++) {
      final BinCIType type = (BinCIType) types.get(i);
      //FIXME: hack to avoid using dir of an already moved type later
      BinPackage binPackage = type.getPackage();
      if (packageDirs.get(binPackage) == null) {
        packageDirs.put(binPackage, binPackage.getDir());
      }

      transList.getStatus().merge(
          moveType(type, movedSources, transList, progress.subdivision(i,
          types.size())));
    }

    transList.getStatus().merge(generateImports(transList));
    doChangeMemberAccess(transList);

    // movers should go last
    for (int i = 0; i < moveEditors.size(); i++) {
      transList.add(moveEditors.get(i));
    }

    // FIXME: do we need it here ?? uncomment if need
    //  if (!transList.getStatus().isErrorOrFatal() && !transList.getStatus().isCancel()) {
    //    transList.setProgressArea(EDITORS_EDIT);
    //  }

    return transList;
  }

  private void removeInnersToBeMovedInsideOwners() {
    for (Iterator i = types.iterator(); i.hasNext(); ) {
      BinCIType t = (BinCIType) i.next();
      if (t.isInnerType()) {
        BinTypeRef owner = t.getOwner();
        while (owner != null) {
          if (types.contains(owner.getBinCIType())) {
            i.remove();
            break;
          }
          owner = owner.getBinCIType().getOwner();
        }
      }
    }
  }

  private void doChangeMemberAccess(final TransformationList transList) {
    if (changeMemberAccess) {
      doChangeMemberAccess(transList, analyzer.getMembersToTurnToPublic(),
          BinModifier.PUBLIC);
      doChangeMemberAccess(transList, analyzer.getMembersToTurnToPackagePrivate(),
          BinModifier.PACKAGE_PRIVATE);
      doChangeMemberAccess(transList, analyzer.getMembersToTurnToProtected(),
          BinModifier.PROTECTED);
    }
  }

  private void doChangeMemberAccess(final TransformationList transList,
      List members,
      int newAccess) {
    for (int i = 0; i < members.size(); i++) {
      transList.add(new ModifierEditor((BinMember) members.get(i),
          BinModifier.setFlags(((BinMember) members.get(i)).getModifiers(),
          newAccess)));
    }
  }

  /**
   * All imports generated in various different places for same files get
   * inserted together in here so that the layout can be better (otherwise, when
   * inserting an import statement we just would not know if there were any more
   * import statements coming afterwards in that file or not). This method is
   * for non-extract type cases.
   */
  private RefactoringStatus generateImports(final TransformationList transList) {
    RefactoringStatus status = new RefactoringStatus();

    for (Iterator it = importNodes.keySet().iterator(); it.hasNext(); ) {
      CompilationUnit compilationUnit = (CompilationUnit) it.next();

      List toBeImported = importNodes.get(compilationUnit);
      if (toBeImported.size() == 0) {
        continue;
      }

      Collections.sort(toBeImported, stringAndBinCITypeQualifiedNameComparator);

      ImportUtils.ImportPosition importPosition = ImportUtils
          .calculateNewImportPosition(compilationUnit,
          compilationUnit.getPackage()
          .isDefaultPackage()
          && this.relocatedSources.contains(compilationUnit)
          /* ,this.removedNodes */
          );

      for (int i = 0; i < toBeImported.size(); i++) {
        String importClause;
        if (toBeImported.get(i) instanceof BinCIType) {
          importClause = ImportUtils.generateImportClause(
              ((BinType) toBeImported.get(i)).getQualifiedName()).toString();
        } else {
          importClause = ImportUtils.generateImportClause(
              toBeImported.get(i).toString()).toString();
        }

        if (FileUtil.isJspFile(compilationUnit.getSource().getAbsolutePath())) {
          importClause = importClause.substring("import ".length(),
              importClause.length() - ";".length());

          ASTImpl importNode = getFirstImportNode(compilationUnit);
          if (importNode == null) {
            status.merge(new RefactoringStatus("Add manually to "
                + compilationUnit.getDisplayPath() + ": import statement of '"
                + importClause + "'", RefactoringStatus.WARNING));
          } else {
            ASTImpl nameNode = new CompoundASTImpl(importNode);
            transList.add(new StringInserter(compilationUnit,
                nameNode.getEndLine(), nameNode.getEndColumn() - 1, ", "
                + importClause));
          }
        } else {
          insertImportClauseToSource(transList, compilationUnit, importPosition,
              i,
              toBeImported.size(), importClause);
        }
      }
    }

    return status;
  }

  private static ASTImpl getFirstImportNode(CompilationUnit compilationUnit) {
    List allImportNodes = new ArrayList(compilationUnit.getImportedPackageNodes());
    for (int i = 0; i < allImportNodes.size(); i++) {
      ASTImpl node = (ASTImpl) allImportNodes.get(i);
      if (node != null && node.getStartLine() != 0) {
        return node;
      }
    }

    return null;
  }

  /* FIXME: use ImportManager instead */
  private void insertImportClauseToSource(final TransformationList transList,
      CompilationUnit compilationUnit,
      ImportUtils.ImportPosition importPosition, int i,
      int max, String importClause) {
    StringBuffer importLine = new StringBuffer(80);

    if (i == 0) {
      importLine.append(ImportUtils.generateNewlines(importPosition.before)
          .toString());
    }

    importLine.append(importClause);

    int after = 1;
    if (i == max - 1) {
      after = importPosition.after;
    }
    importLine.append(ImportUtils.generateNewlines(after).toString());

    StringInserter e = new StringInserter(compilationUnit, importPosition.line,
        importPosition.column, importLine.toString());

    transList.add(e);
  }

  private void removeTypesThatAreFromTargetPackage() {
    for (Iterator i = types.iterator(); i.hasNext(); ) {
      BinCIType t = (BinCIType) i.next();
      if (t.getPackage().isIdentical(targetPackage) && (!isExtract(types, t))) {
        if (t.getCompilationUnit().getSource().getAbsolutePath().startsWith(
            targetSource.getAbsolutePath()+ File.separator)) {
          i.remove();
        }
      }
    }
  }

  /**
   * + move physically if matching dir-package + update qualified usage of type
   * in all sources + update imports of this type in all sources + add import of
   * this type to types in old package if used and not imported + add import of
   * old package if used in this type
   */
  private RefactoringStatus moveType(final BinCIType type,
      final Set movedSources, final TransformationList transList,
      ProgressMonitor.Progress progress) {
    RefactoringStatus status = new RefactoringStatus();

    TypeDependencies usages = new TypeDependencies();
    usages.findNameUsages(type, progress, true);

    boolean extract = isExtract(types, type);

    status.merge(updateQualifiedUsageOfType(usages, transList));
    if (status.isErrorOrFatal()) {
      return status;
    }

    if (!extract) {
      if (!targetPackage.isDefaultPackage()) {
        // FIXME: need to use editor to add Imports?
        addImportOfTypeToOtherTypes(type, usages, status);
      }

      status.merge(removeSamePackageImportsFromSource(type.getCompilationUnit(),
          type, transList));
      status.merge(removeTypeImportFromNewPackageSources(type, transList));

      if (sourcePackageBecomesEmpty(type.getPackage())) {
        status.merge(removeWholePackageImports(type.getPackage(), transList));
        if (status.isErrorOrFatal()) {
          return status;
        }
      }

      if (!type.getPackage().isDefaultPackage()) {
        addImportOfOldPackageTypesToType(type, usages);
      }

      if (!movedSources.contains(type.getCompilationUnit())) {
        if (!targetPackage.isSame(type.getPackage())) {
	        changePackageDeclaration(type.getCompilationUnit(), type.getPackage(),
	            transList);
        }

        // Sometimes a source is has _not_ been fully moved but it _has_ been
        // relocated -- the package declaration
        // is not changed, but the file itself _is_ moved to the right place.
        // This can happen when one MoveType
        // instance has worked with a source file but not commited the changes
        // (not called editor.performEdit()),
        // and another MoveType instance has started the move process from the
        // beginning.

        if (!relocatedSources.contains(type.getCompilationUnit())) {
          // NOTE: so if something has crashed before this, we have at least the
          // file at the old place
          RefactoringStatus relocationResult = relocate(type, transList);
          if (Assert.enabled) {
            /*
             * System.err.println("Allows to relocate source file: " +
             * (relocationResult == null || relocationResult.isOk() ? "true" :
             * relocationResult.getAllMessages()));
             */
          }
          relocatedSources.add(type.getCompilationUnit());
          status.merge(relocationResult);
        }

        movedSources.add(type.getCompilationUnit());
      }
    } else {
      status.merge(extractType(type, usages, transList));
    }

    // FIXME: may be even not needed!
    type.invalidateCache();

    usages.clear();

    return status;
  }

  private RefactoringStatus removeWholePackageImports(BinPackage aPackage,
      final TransformationList transList) {
    RefactoringStatus status = new RefactoringStatus();

    List compilationUnits = aPackage.getProject().getCompilationUnits();
    for (int i = 0; i < compilationUnits.size(); i++) {
      CompilationUnit compilationUnit = (CompilationUnit) compilationUnits.get(
          i);
      List renamePackageData = compilationUnit.getPackageUsageInfos();
      if (renamePackageData != null) {
        for (int j = 0; j < renamePackageData.size(); j++) {
          PackageUsageInfo data = (PackageUsageInfo) renamePackageData.get(j);
          if (data.getBinPackage().isIdentical(aPackage)) {
            if (ImportUtils.isChildOfImportNode(compilationUnit, data.getNode())
                && !ImportUtils.isFqnUsage(compilationUnit, data.getNode())) {
              status.merge(removePackageImport(compilationUnit, data.getNode(),
                  transList));
            }
          }
        }
      }
    }

    return status;
  }

  private RefactoringStatus removePackageImport(CompilationUnit compilationUnit,
      ASTImpl ast, final TransformationList transList) {
    if (FileUtil.isJspFile(compilationUnit.getSource().getAbsolutePath())) {
      return removeImportFromJsp(compilationUnit, ImportUtils.getImportNode(
          compilationUnit, ast));
    }

    CompoundASTImpl node = new CompoundASTImpl(ImportUtils.getImportNode(
        compilationUnit, ast));

    if (!this.removedNodes.contains(compilationUnit, ast)) {
      final StringEraser eraser = new StringEraser(compilationUnit, node, true);
      this.removedNodes.putAll(compilationUnit, ast);
      eraser.setTrimTrailingSpace(true);
      transList.add(eraser);
    }

    return new RefactoringStatus();
  }

  private RefactoringStatus removeImportFromJsp(CompilationUnit compilationUnit,
      ASTImpl importNode) {
    return new RefactoringStatus("Remove manually: import of '"
        + new CompoundASTImpl((ASTImpl) importNode.getFirstChild()).getText()
        + "' at " + compilationUnit.getDisplayPath() + ":"
        + ((ASTImpl) importNode.getFirstChild()).getStartLine(),
        RefactoringStatus.WARNING);
  }

  private boolean sourcePackageBecomesEmpty(BinPackage aPackage) {
    for (Iterator i = aPackage.getAllTypes(); i.hasNext(); ) {
      BinCIType type = ((BinTypeRef) i.next()).getBinCIType();
      if (!analyzer.isMoving(type)) {
        return false;
      }
    }

    return true;
  }

  public static boolean isExtract(List typesToMove, BinCIType typeToCheck) {
    // inners are always extracted
    if (typeToCheck.getOwner() != null) {
      return true;
    }

    List definedTypes = typeToCheck.getCompilationUnit()
        .getIndependentDefinedTypes();

    if (definedTypes.size() > 1 /* && !typeToCheck.isNameMatchesSourceName() */) {
      for (int i = 0, max = definedTypes.size(); i < max; ++i) {
        final BinCIType definedType = ((BinTypeRef) definedTypes.get(i))
            .getBinCIType();
        if (!typesToMove.contains(definedType)) {
          return true;
        }
      }
    }

    return false;
  }

  private static Comparator invocationDataAstComparator = new Comparator() {
    public int compare(final Object a, final Object b) {
      final ASTImpl node1 = ((InvocationData) a).getWhereAst();
      final ASTImpl node2 = ((InvocationData) b).getWhereAst();

      if (node1.equals(node2)) {
        return 0;
      } else {
        // Not meaningful in particular -- just something fast, consistent and
        // nonzero.
        return System.identityHashCode(node1) - System.identityHashCode(node2);
      }
    }
  };

  private static Comparator stringAndBinCITypeQualifiedNameComparator = new
      Comparator() {
    public int compare(Object a, Object b) {
      return getQualifiedName(a).compareTo(getQualifiedName(b));
    }

    private String getQualifiedName(Object a) {
      if (a instanceof BinType) {
        return ((BinType) a).getQualifiedName();
      } else if (a instanceof BinTypeRef) {
        return ((BinTypeRef) a).getQualifiedName();
      } else if (a instanceof String) {
        return a.toString();
      } else {
        throw new IllegalArgumentException("Excpected a String or a BinCIType");
      }
    }
  };

  private class SpecialSinglePointVisitor extends SinglePointVisitor {
    private BinCIType movedType;
    private RefactoringStatus result = new RefactoringStatus();
    private TransformationList transList;
    private TypeDependencies usages;

    public void onEnter(Object x) {
      if (x instanceof BinMemberInvocationExpression) {
        BinMemberInvocationExpression invocation = (
            BinMemberInvocationExpression) x;
        BinCIType usedType = invocation.getMember().getOwner().getBinCIType();

        if ((typeIsInSameFileButNotInsideMovedType(usedType,
            movedType) || usedTypeIsSuperclassOfSomeOwner(
            usedType, movedType))) {
          if (!invocation.getMember().isStatic()
              && isOwnerMemberDirectInvocation(invocation, usedType, movedType)) {
            result.addEntry("Direct usage of nonstatic member: "
                + invocation.getMember(), RefactoringStatus.ERROR);
          } else if (!invocation.getMember().isStatic()) {
            return;
          }

          addTypeImportStatement(invocation);
          addClassPrefixesToMemberInvocations(invocation, usedType, movedType);
        }
      }
    }

    private boolean isOwnerMemberDirectInvocation(
        final BinMemberInvocationExpression invocation,
        final BinCIType usedType, final BinCIType movedType) {
      return invocation.invokedViaThisReference()
          && (!movedType.getTypeRef().isDerivedFrom(usedType.getTypeRef()));
    }

    private void addClassPrefixesToMemberInvocations(
        final BinMemberInvocationExpression invocation,
        final BinCIType usedType, final BinCIType movedType) {
      BinExpression expr = invocation.getExpression();

      if (expr == null) {
        // direct invocation, add class prefix
        ASTImpl existingNameNode = (ASTImpl) invocation.getNameAst();
        String newName = usedType.getName() + "."
            + invocation.getMember().getName();
        transList.add(new RenameTransformation(movedType.getCompilationUnit(),
            CollectionUtil
            .singletonArrayList(existingNameNode), newName));

      }

    }

    private void addTypeImportStatement(
        final BinMemberInvocationExpression invocation) {
      CollectionUtil.addNew(usages.usesList, new InvocationData(invocation
          .getMember(), invocation.getMember(), invocation.getNameAst(),
          invocation));
    }

    public void onLeave(Object b) {
    }

    SpecialSinglePointVisitor(BinCIType movedType,
        final TransformationList transList,
        TypeDependencies usages) {
      this.movedType = movedType;
      this.transList = transList;
      this.usages = usages;
    }

    final void clear() {
      this.movedType = null;
      this.transList = null;
      this.usages = null;
    }

    final RefactoringStatus getResult() {
      return result;
    }
  }


  public static final FilesToMoveWithJavaCompilationUnits NONE = new
      FilesToMoveWithJavaCompilationUnits() {
    public Source[] getFilesToMoveWith(Source javaCompilationUnit) {
      return Source.NO_SOURCES;
    }
  };

  public static final FilesToMoveWithJavaCompilationUnits
      BACKUPS_ENDING_WITH_TILDE = new FilesToMoveWithJavaCompilationUnits() {
    public Source[] getFilesToMoveWith(Source javaCompilationUnit) {
      Source result = javaCompilationUnit.getParent().getChild(
          javaCompilationUnit.getName() + "~");

      if (result == null) {
        return Source.NO_SOURCES;
      } else {
        return new Source[] {result};
      }
    }
  };

  /**
   * Example: .bak files should be moved together with .java files; there could
   * be others (and the backup extension is not always the same); generally the
   * results depend on the IDE that RefactorIT is running under, _not_ on the
   * filesystem only (LocalFileSystem, for exampe, could be used under almost
   * all platforms).
   *
   * Note that support for form files is implemented separately -- see
   * vfs.Source
   */
  public interface FilesToMoveWithJavaCompilationUnits {
    Source[] getFilesToMoveWith(Source javaCompilationUnit);
  }


  private static FilesToMoveWithJavaCompilationUnits
      filesToMoveWithCompilationUnits =
      NONE;

  public static MoveType.FilesToMoveWithJavaCompilationUnits
      getFilesToMoveWithJavaCompilationUnits() {
    return filesToMoveWithCompilationUnits;
  }

  public static void setFilesToMoveWithJavaCompilationUnits(
      MoveType.FilesToMoveWithJavaCompilationUnits newValue) {
    filesToMoveWithCompilationUnits = newValue;
  }

  private static void moveAll(Source[] sources, SourceHolder destination,
      TransformationList transList, Project project) {
    for (int i = 0; i < sources.length; i++) {
      transList.add(new FileRenamer(
          new SimpleSourceHolder(sources[i], project),
          destination, sources[i].getName()));
    }
  }

  //-------- Start of conflicts resolve code

  public List resolveConflicts() {
    List result = new ArrayList();

    List toAdd = getTypesThatMustBeMovedAlong();
    while (!toAdd.isEmpty()) {
      addTypes(toAdd);
      CollectionUtil.addAllNew(result, toAdd);
      toAdd = getTypesThatMustBeMovedAlong();
    }

    return result;
  }

  private List getTypesThatMustBeMovedAlong() {
    RefactoringStatus status = checkUserInput();

    List result = new ArrayList();

    List entries = status.getEntries();
    for (int i = 0; i < entries.size(); i++) {
      RefactoringStatus.Entry entry = (RefactoringStatus.Entry) entries.get(i);
      getTypesThatMustBeMovedAlong(entry, result);
    }

    return result;
  }

  private BinCIType getMoveableOwner(BinMember member) {
    while (!(member instanceof BinCIType) || ((BinCIType) member).isAnonymous()
        || ((BinCIType) member).isLocal()) {
      member = member.getOwner().getBinCIType();
    }

    return (BinCIType) member;
  }

  private void getTypesThatMustBeMovedAlong(RefactoringStatus.Entry entry,
      List result) {
    if (entry.getBin() instanceof BinMember) {
      CollectionUtil.addNew(result, getMoveableOwner((BinMember) entry.getBin()));
    }

    List subEntries = entry.getSubEntries();

    for (int i = 0; i < subEntries.size(); i++) {
      RefactoringStatus.Entry sub = (RefactoringStatus.Entry) subEntries.get(i);
      getTypesThatMustBeMovedAlong(sub, result);
    }
  }

  public void addTypes(List typesToAdd) {
    List result = new ArrayList(types);
    CollectionUtil.addAllNew(result, typesToAdd);
    setTypes(result);
  }

  //----- end of conflicts resolve code

  //private RefactoringStatus changeTypesInNonJavaFiles(List types,
  //   SourceEditor editor) {
  private RefactoringStatus changeTypesInNonJavaFiles(List types,
      final TransformationList transList) {

    final Project project = getContext().getProject();

    ManagingNonJavaIndexer supervisor = new ManagingNonJavaIndexer(project
        .getOptions().getNonJavaFilesPatterns());
    for (int i = 0; i < types.size(); i++) {
      BinType type = (BinType) types.get(i);
      String qualifiedName = type.getQualifiedName();
      new QualifiedNameIndexer(supervisor, qualifiedName,
          QualifiedNameIndexer.SLASH_AND_BACKSLASH_PATH);
    }
    supervisor.visit(project);
    List nonJavaOccurrences = supervisor.getOccurrences();
    if (nonJavaOccurrences.size() > 0) {
      ExtendedConfirmationTreeTableModel model = new
          ExtendedConfirmationTreeTableModel(
          getTargetPackage(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
          nonJavaOccurrences);

//      String desc = "Move " + (types.size() == 1 ? "class" : "classes")
//          + " to: ";
      // moved to new preview
//      model = (ExtendedConfirmationTreeTableModel) DialogManager.getInstance()
//          .showConfirmations(getContext(), model, desc, "refact.move.type");

      if (model == null) {
        return new RefactoringStatus("", RefactoringStatus.CANCEL);
      }

      List checkedOccurrences = model.getCheckedNonJavaOccurrences();
      final MultiValueMap nonJavaUsages = new MultiValueMap();
      for (Iterator i = checkedOccurrences.iterator(); i.hasNext(); ) {
        Occurrence o = (Occurrence) i.next();
        nonJavaUsages.putAll(o.getLine().getSource(), o);
      }

      for (final Iterator i = nonJavaUsages.entrySet().iterator(); i.hasNext(); ) {
        final Map.Entry entry = (Map.Entry) i.next();
        // create temporary CompilationUnit
        CompilationUnit sf = new CompilationUnit((Source) entry.getKey(),
            project);
        for (Iterator i2 = ((List) entry.getValue()).iterator(); i2.hasNext(); ) {
          Occurrence o = (Occurrence) i2.next();
          String oldName = o.getText();

          // FIXME: this might be slow
          BinTypeRef type = project.findTypeRefForName(
              oldName);
          String newName = getTargetPackage().getQualifiedName()
              + oldName.substring(type.getBinType().getPackage()
              .getQualifiedName().length());
          if(o instanceof PathOccurrence) {
            PathOccurrence po = (PathOccurrence)o;
            if(po.isSlashedPath()) {
              newName = StringUtil.getSlashedPath(newName);
            } else if(po.isBackslashedPath()) {
              newName = StringUtil.getBackslashedPath(newName);
            }
          }

          StringEraser eraser = new StringEraser(sf, o.getLine()
              .getLineNumber(), o.getStartPos(), oldName.length());
          StringInserter inserter = new StringInserter(sf, o.getLine()
              .getLineNumber(), o.getStartPos(), newName);
          transList.add(eraser);
          transList.add(inserter);
        }
      }

    }
    return new RefactoringStatus();

  }

  public void setChangeInNonJavaFiles(final boolean changeNonJavaFiles) {
    this.changeInNonJavaFiles = changeNonJavaFiles;
  }

  public String getDescription() {
    if (types.size() > 7) {
      return "Move Type - moving many";
    } else {
      StringBuffer buf = new StringBuffer();

      for (int i = 0; i < types.size(); i++) {
        BinType type;
        type = (BinType) types.get(i);
        buf.append(' ' + type.getName() + ',');
      }
      buf.deleteCharAt(buf.length() - 1);

      return "Move" /*type" + ((types.size() > 1) ? "s" : "")*/
          + new String(buf)
          + " to " + getTargetPackage().getQualifiedName();
    }
  }

  public String getKey() {
    return key;
  }

}
