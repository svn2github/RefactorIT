/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.query.text.ManagingNonJavaIndexer;
import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.query.text.PackageQualifiedNameIndexer;
import net.sf.refactorit.query.text.PathOccurrence;
import net.sf.refactorit.query.text.QualifiedNameIndexer;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.PackageNameIndexer;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.DirCreator;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.RenamePackageTransformation;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class RenamePackage extends RenameRefactoring {
  public static String key = "refacotirng.rename.package";

  private boolean renamePrefix = false;
  private String prefix = null;

  private boolean disableRelocation = false;

  private boolean renameInNonJavaFiles = true;

  private boolean ignoreFilenameCase = RuntimePlatform.isWindows();

  public RenamePackage(RefactorItContext context, BinPackage aPackage) {
    super("RenamePackage", context, aPackage);
  }

  public void setRenamePrefix(boolean renamePrefix) {
    this.renamePrefix = renamePrefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setDisableRelocation(boolean d) {
    disableRelocation = d;
  }

  public void setIgnoreFilenameCase(boolean b) {
    ignoreFilenameCase = b;
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();
    status.merge(super.checkPreconditions());

    List packages = CollectionUtil.singletonArrayList(getItem());

    status.merge(isPackagesFromSource(packages));

    // JRenamePackage dialog calls checkUserInput() per each user keypress,
    // that's why this (slow) super.checkUserInput() call is put here.
    status.merge(super.checkUserInput());

    return status;
  }

  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();

    if (!NameUtil.isValidPackageName(getNewName())) {
      status.addEntry("Invalid package name", RefactoringStatus.ERROR);
    }

    if (renamePrefix) { // base package was already checked in checkPreconditions

      if (!NameUtil.isValidPackageName(prefix)) {
        status.addEntry("Invalid prefix name", RefactoringStatus.ERROR);
      }
      List packages = collectPackagesToBeRenamed(prefix);
      status.merge(isPackagesFromSource(packages));
      if (!validPrefixForSomePackage(prefix)) {
        status.addEntry("No packages in sourcepath with that prefix",
            RefactoringStatus.ERROR);
      }
      checkNewNameDiffersFrom(prefix, status);

      status.merge(checkPrefixConflictWithExistingPackages(packages));
    } else {

      BinPackage target = (BinPackage) getItem();
      checkNewNameDiffersFrom(target.getQualifiedName(), status);

      if (getProject().hasNonEmptyPackage(getNewName())) {
        //<FIX> Aleksei Sosnovski 08.2005
        BinPackage[] packages = getProject().getAllPackages();
        boolean check = false;

        for (int i = 0; i < packages.length; i++) {

          if (getNewName() != null && !getNewName().equals("")) {

            if (packages[i].getQualifiedName().equals(getNewName())) {
               // && packages[i].getBaseDir().equals(target.getBaseDir())) {
               Set dirs1 = packages[i].getBaseDirs();
               Set dirs2 = target.getBaseDirs();

               for (Iterator iter = dirs1.iterator(); iter.hasNext(); ) {

                 if (dirs2.contains(iter.next())) {
                   check = true;
                   break;
                 }
               }
            }
          }
        }

        if (check) {
          status.addEntry("Package with that name already exists",
              RefactoringStatus.ERROR);
        }
//        status.addEntry("Package with that name already exists",
//              RefactoringStatus.ERROR);
        //</FIX>
      }
    }

    return status;
  }

  private void checkNewNameDiffersFrom(String oldName,
      final RefactoringStatus status) {
    if (getNewName().equals(oldName)) {
      status.addEntry("New name equals old name", RefactoringStatus.ERROR);
    }
    if (ignoreFilenameCase
        && (getNewName().equalsIgnoreCase(oldName)
        && !getNewName().equals(oldName))) {
      status.addEntry(
          "New name differs from old name only in case, which is not supported under most filesystems under Windows",
          RefactoringStatus.ERROR);
    }
  }

  private RefactoringStatus checkPrefixConflictWithExistingPackages(List
      packagesWithPrefix) {
    for (int i = 0; i < packagesWithPrefix.size(); i++) {
      BinPackage aPackage = (BinPackage) packagesWithPrefix.get(i);
      String newName = getNewName()
          + aPackage.getQualifiedName().substring(prefix.length());
      if (getProject().hasNonEmptyPackage(newName)) {

        //<FIX> Aleksei Sosnovski 08.2005
        BinPackage[] packages = getProject().getAllPackages();
        boolean check = false;

        for (int j = 0; j < packages.length; j++) {
          if (packages[j].getQualifiedName().equals(newName)) {
            //&& packages[j].getBaseDir().equals(aPackage.getBaseDir())) {
            Set dirs1 = packages[j].getBaseDirs();
            Set dirs2 = aPackage.getBaseDirs();

            for (Iterator iter = dirs1.iterator(); iter.hasNext(); ) {

              if (dirs2.contains(iter.next())) {
                check = true;
                break;
              }
            }
          }
        }

        if (check) {
          return new RefactoringStatus(
              "New name conflicts with an existing package name",
              RefactoringStatus.ERROR);
        }
//        return new RefactoringStatus(
//            "New name conflicts with an existing package name",
//            RefactoringStatus.ERROR);
         //</FIX>
      }
    }

    return new RefactoringStatus();
  }

  private boolean validPrefixForSomePackage(String aPrefix) {
    if (validPrefixForPackage(aPrefix,
        ((BinPackage) getItem()).getQualifiedName())) {
      return true;
    }

    BinPackage[] packages = getProject().getAllPackages();
    for (int i = 0; i < packages.length; i++) {
      if (validPrefixForPackage(aPrefix, packages[i].getQualifiedName())) {
        return true;
      }
    }

    return false;
  }

  private boolean validPrefixForPackage(String aPrefix, String aPackage) {
    return
        aPackage.startsWith(aPrefix + ".") ||
        aPackage.equals(aPrefix);
  }

  private RefactoringStatus isPackagesFromSource(List packages) {
    RefactoringStatus status = new RefactoringStatus();
    List notSourcePackages = new ArrayList(10);
    List ignoredPackages = new ArrayList();
    List outsideTypes = new ArrayList(10);

    for (int i = 0, max = packages.size(); i < max; i++) {
      final BinPackage aPackage = (BinPackage) packages.get(i);

      if (!aPackage.hasTypesWithSources()) {
        if (!BinPackage.isIgnored(aPackage)) {
          notSourcePackages.add(aPackage);
        } else {
          ignoredPackages.add(aPackage);
        }

      } else {
        Iterator types = aPackage.getAllTypes();
        while (types.hasNext()) {
          final BinCIType type = ((BinTypeRef) types.next()).getBinCIType();
          if (!type.isFromCompilationUnit()) {
            CollectionUtil.addNew(outsideTypes, type);
          }
        }
      }
    }

    if (notSourcePackages.size() > 0) {
      status.addEntry(
          "Package is not from the source path",
          notSourcePackages,
          RefactoringStatus.ERROR);
    }

    if (outsideTypes.size() > 0) {
      status.addEntry(
          "Package contains types not from the source path",
          outsideTypes,
          RefactoringStatus.ERROR);
    }

    if (ignoredPackages.size() > 0) {
      status.addEntry(
          "Package belongs to ignored sourcepath \nor is not from sourcepath",
          ignoredPackages,
          RefactoringStatus.ERROR);
    }

    return status;
  }

  public TransformationList performChange() {
    /*SwingUtil.invokeInEdtUnderNetBeans(
      new Runnable() {
        public void run() {*/
    return performChangeInCurrentThread();
    /*}
           }
         );
     */
  }

  private TransformationList performChangeInCurrentThread() {
    TransformationList transList = super.performChange();

    if (!transList.getStatus().isOk()) {
      return transList;
    }

    if (renamePrefix) {
      transList.getStatus().merge(renamePrefix(transList));
    } else {
      transList.getStatus().merge(rename(transList));
    }

    return transList;
  }

  protected ManagingIndexer getSupervisor() {
    // TODO because of this not_implemented check for writes in guarded blocks
    // will not wotk when renaming packages
    final ManagingIndexer managingIndexer = new ManagingIndexer();
    managingIndexer.visit(getProject());
    return managingIndexer;
  }

  private RefactoringStatus rename(final TransformationList transList) {
    RefactoringStatus status = new RefactoringStatus();

    BinPackage aPackage = (BinPackage) getItem();

/*  DEBUG
    BinPackage[] packs = getProject().getAllPackages();
    for (int i = 0; i < packs.length; i++) {

      Set baseDirs = packs[i].getBaseDirs();
      Set dirs = packs[i].getDirs();

      if (dirs.isEmpty()) {
        continue;
      }

      System.out.println(packs[i].getQualifiedName());

      Iterator it1 = baseDirs.iterator();
      Iterator it2 = dirs.iterator();

      while (it1.hasNext() || it2.hasNext()) {

        if (it1.hasNext()) {
          System.out.println("    " + ((Source) it1.next()).getAbsolutePath());
        }

        if (it2.hasNext()) {
          System.out.println("        " + ((Source) it2.next()).getAbsolutePath());
        }
      }
    }
*/

    java.util.List singlePackageList = new ArrayList(1);
    singlePackageList.add(aPackage);
    final List nonJavaOccurrences = getNonJavaOccurrences(singlePackageList);
    List checkedNonJavaOccurrences = Collections.EMPTY_LIST;
    if (nonJavaOccurrences.size() > 0) {
      ExtendedConfirmationTreeTableModel model = new
          ExtendedConfirmationTreeTableModel(getItem(), Collections.EMPTY_LIST,
          Collections.EMPTY_LIST, nonJavaOccurrences);
      model = (ExtendedConfirmationTreeTableModel) DialogManager.getInstance()
          .showConfirmations(getContext(), model, "refact.rename.package");

      if (model == null) {
        return new RefactoringStatus("", RefactoringStatus.CANCEL);
      }

      checkedNonJavaOccurrences = model.getCheckedNonJavaOccurrences();
    }

    PackageUsers users = collectUsageOfSinglePackage(aPackage);

    if (users.usedList == null) {
      return status;
    }

    if (shouldRelocateSources(Collections.singletonList(aPackage))) {
      status.merge(relocateTypesOfPackage(aPackage, getNewName(), transList));
    }

    renameSinglePackage(aPackage, getNewName(), transList, users);

    if (checkedNonJavaOccurrences.size() > 0) {
      renamePackagesInNonJavaFiles(checkedNonJavaOccurrences, aPackage
          .getQualifiedName(), getNewName(), transList);
    }

    return status;
  }

  private PackageUsers collectUsageOfSinglePackage(BinPackage aPackage) {
    ManagingIndexer supervisor = new ManagingIndexer(true);

    new PackageNameIndexer(supervisor, aPackage);
    supervisor.visit(aPackage.getProject());

    PackageUsers result = new PackageUsers();
    result.usedList = supervisor.getInvocations();
    result.used = supervisor.getInvocationsMap();
    return result;
  }

  private RefactoringStatus renamePrefix(final TransformationList transList) {
    //System.err.println("Prefix: " + prefix + ", new prefix: " + newPrefix);
    RefactoringStatus status = new RefactoringStatus();

    List packages = collectPackagesToBeRenamed(prefix);

    //System.err.println("Packages to be renamed: " + packages);

    status.merge(renamePrefixes(packages, prefix, getNewName(), transList));

    return status;
  }

  private List collectPackagesToBeRenamed(String prefix) {
    BinPackage[] packages = getProject().getAllPackages();
    List result = new ArrayList(10);

    for (int i = 0; i < packages.length; i++) {
      if (packages[i].getQualifiedName().startsWith(prefix)) {
        result.add(packages[i]);
      }
    }

    // descending in order to rename deepest first
    Comparator sorter = new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((BinPackage) o2).getQualifiedName().compareTo(
            ((BinPackage) o1).getQualifiedName());
      }
    };

    Collections.sort(result, sorter);

    return result;
  }

  private RefactoringStatus renamePrefixes(
      List packages,
      String oldPrefix, String newPrefix,
      final TransformationList transList
  ) {
    RefactoringStatus status = new RefactoringStatus();

    final List nonJavaOccurrences = getNonJavaOccurrences(packages);
    List checkedNonJavaOccurrences = Collections.EMPTY_LIST;
    if (nonJavaOccurrences.size() > 0) {
      ExtendedConfirmationTreeTableModel model = new
          ExtendedConfirmationTreeTableModel(getItem(), Collections.EMPTY_LIST,
          Collections.EMPTY_LIST, nonJavaOccurrences);
      model = (ExtendedConfirmationTreeTableModel) DialogManager.getInstance()
          .showConfirmations(getContext(), model, "refact.rename.package");

      if (model == null) {
        return new RefactoringStatus("", RefactoringStatus.CANCEL);
      }
      checkedNonJavaOccurrences = model.getCheckedNonJavaOccurrences();
    }

    Map packageUsers = collectUsagesOfPackages(packages);

    if (shouldRelocateSources(packages)) {
      //SourceRelocator r = new SourceRelocator(getProject());
      //List errors = new ArrayList();
      //r.relocate(packages, oldPrefix, newPrefix, errors);
      //status.merge(createRelocationWarningsFor(errors));
      transList.add(new RenamePackageTransformation(packages, oldPrefix, newPrefix,
          getProject(), RenamePackageTransformation.RELOCATE_SIMPLE));
    }

    for (int i = 0, max = packages.size(); i < max; i++) {
      final BinPackage aPackage = (BinPackage) packages.get(i);
      String newName = newPrefix + aPackage.getQualifiedName().substring(
          oldPrefix.length());
      //System.err.println("Renaming: " + aPackage.getQualifiedName() + " to "
      //    + newName);

      PackageUsers users = (PackageUsers) packageUsers.get(aPackage);

      renameSinglePackage(aPackage, newName, transList, users);
    }

    if (checkedNonJavaOccurrences.size() > 0) {
      renamePackagesInNonJavaFiles(checkedNonJavaOccurrences, oldPrefix,
          newPrefix, transList);
    }
    return status;
  }

  private void renamePackagesInNonJavaFiles(java.util.List nonJavaOccurrences,
      String oldPrefix, String newPrefix, final TransformationList transList) {
    final MultiValueMap nonJavaUsages = new MultiValueMap();
    for (Iterator i = nonJavaOccurrences.iterator(); i.hasNext(); ) {
      Occurrence o = (Occurrence) i.next();
      nonJavaUsages.putAll(o.getLine().getSource(), o);
    }

    if (!nonJavaUsages.isEmpty()) {
      for (final Iterator i = nonJavaUsages.entrySet().iterator(); i.hasNext(); ) {
        final Map.Entry entry = (Map.Entry) i.next();
        // create temporary CompilationUnit
        /*CompilationUnit sf = new CompilationUnit((Source) entry.getKey(),
            getContext().getProject());*/
        CompilationUnit sf = getContext().getProject().getNonJavaUnit((Source) entry.getKey());
        for (Iterator i2 = ((List) entry.getValue()).iterator(); i2.hasNext(); ) {
          Occurrence o = (Occurrence) i2.next();
          String oldName = o.getText();
          String newName = newPrefix + oldName.substring(oldName.length());
          if(o instanceof PathOccurrence) {
            PathOccurrence po = (PathOccurrence)o;
            if(po.isSlashedPath()) {
              newName = StringUtil.getSlashedPath(newName);
            } else if(po.isBackslashedPath()) {
              newName = StringUtil.getBackslashedPath(newName);
            }
          }
          transList.add(new RenameTransformation(sf, oldName, newName, o));
        }
      }
    }
  }

  private List getNonJavaOccurrences(java.util.List packages) {
    if (renameInNonJavaFiles) {
      Project project = getContext().getProject();

      ManagingNonJavaIndexer supervisor = new ManagingNonJavaIndexer(
          project.getOptions().getNonJavaFilesPatterns());
      for (Iterator i = packages.iterator(); i.hasNext(); ) {
        BinPackage pack = (BinPackage) i.next();
        if(pack.getQualifiedName().trim().length() > 0) {
          new PackageQualifiedNameIndexer(supervisor, pack,
            QualifiedNameIndexer.SLASH_AND_BACKSLASH_PATH, renamePrefix);
        }
      }
      supervisor.visit(project);
      List results = supervisor.getOccurrences();
      return results;
    } else {
      return Collections.EMPTY_LIST;
    }
  }

  /**
   * @param packages packages list
   * @return usages map
   */
  private Map collectUsagesOfPackages(final List packages) {
    Map packageUsers = new HashMap();
    for (int i = 0; i < packages.size(); i++) {
      BinPackage aPackage = (BinPackage) packages.get(i);
      packageUsers.put(aPackage, collectUsageOfSinglePackage(aPackage));
    }

    return packageUsers;
  }

  private Source getSomeSource(List packages) {
    for (int i = 0; i < packages.size(); i++) {
      Source result = ((BinPackage) packages.get(i)).getDir();
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  public boolean shouldRelocateSources(List packages) {
    if (disableRelocation) {
      return false;
    }

    Source source = getSomeSource(packages);
    if (source == null || source.getParent() == null) {
      return true;
    }

    boolean hasCvsInFolder = source.getParent().getChild("CVS") != null;
    if (supportsVcs(source) || (!hasCvsInFolder)) {
      return true;
    }

    int result = DialogManager.getInstance().showYesNoQuestion(
        getContext(), "rename.package.relocate.types.of.packages");
    return result == DialogManager.YES_BUTTON;
  }

  protected boolean supportsVcs(Source source) {
    return source.shouldSupportVcsInFilesystem();
  }

  private void renameSinglePackage(BinPackage aPackage,
      String newName,
      final TransformationList transList,
      PackageUsers users) {
//    for (int i = 0, max = this.usedList.size(); i < max; i++) {
//      final InvocationData data = (InvocationData) this.usedList.get(i);
//      System.err.println("Used: " + data);
///*      (new rantlr.debug.misc.ASTFrame(
//          new CompoundASTImpl((ASTImpl) data.getAst()).getText(),
//          (ASTImpl) data.getAst())).setVisible(true);*/
//    }

    Iterator sources = users.used.keySet().iterator();
    while (sources.hasNext()) {
      final CompilationUnit compilationUnit = (CompilationUnit) sources.next();
      List nodes = users.used.get(compilationUnit);

      List packageNodes = new ArrayList(nodes.size());
      for (int i = 0, max = nodes.size(); i < max; i++) {
        ASTImpl packageNode = (ASTImpl) nodes.get(i);

        //<FIX> Aleksei Sosnovski 08.2005
        //if compilation unit is in default package
        if (packageNode instanceof SimpleASTImpl //just extra precaution
            && packageNode.getStartLine() == 1 && packageNode.getEndLine() == 1
            && packageNode.getStartColumn() == 0
            && packageNode.getEndColumn() == 0) {
          transList.add(new StringInserter
              (compilationUnit, 1 ,0 , "package " + newName
              + ";" + FormatSettings.LINEBREAK + FormatSettings.LINEBREAK));
        } else {
        //</FIX>

          if (packageNode.getFirstChild() != null
              && "*".equals(packageNode.getFirstChild().getNextSibling().
              getText())) {
            packageNode = (ASTImpl) packageNode.getFirstChild();
          }
          packageNode = new CompoundASTImpl(packageNode);
          packageNodes.add(packageNode);
        }
      }

      transList.add(new RenameTransformation(compilationUnit, packageNodes, newName));
    }
  }

  /**
   * It should be called before <em>rename</em> so that we can detect if the
   * old path matches package name and types can be moved.
   *
   * @param aPackage containing types to be moved to new location
   * @param newName name of the package, will define new probable path
   *
   * @return true if succeeded
   */
  private RefactoringStatus relocateTypesOfPackage(BinPackage aPackage,
      String newName, final TransformationList transList) {
    RefactoringStatus status = new RefactoringStatus();
    if (!aPackage.isNameMatchesDir()) {
      status.addEntry(DirCreator.PACKAGE_NOT_RELOCATED_MSG,
          RefactoringStatus.WARNING);
      return status;
    }

    //Source oldDir = aPackage.getDir();
    //Source newDir = aPackage.getBaseDir().mkdirs(newName.replace('.', Source.RIT_SEPARATOR_CHAR),
    //    oldDir.inVcs());
    //
    //if (newDir == null) {
    //  status.addEntry(FileEraser.PACKAGE_NOT_RELOCATED_MSG, RefactoringStatus.WARNING);
    //  return status;
    //}

    //<FIX> Aleksei Sosnovski 08.2005
//    Source oldDir = aPackage.getDir();
//    SourceHolder newDir = new SimpleSourceHolder(getProject());
//
//    transList.add(new DirCreator(
//        new SimpleSourceHolder(aPackage.getBaseDir(), getProject()),
//        newDir, newName, oldDir.inVcs()));
//
//    transList.add(new RenamePackageTransformation(oldDir, getProject(), newDir,
//        RenamePackageTransformation.RELOCATE_FILES_TRANSACTION));

    for (Iterator iter = aPackage.getBaseDirs().iterator(); iter.hasNext(); ) {
      Source baseDir = (Source) iter.next();

      Iterator iter2 = aPackage.getDirs().iterator();
      Source oldDir = (Source) iter2.next();

      String oldPath = AbstractSource.normalize(oldDir.getAbsolutePath())
          + Source.RIT_SEPARATOR_CHAR;
      String basePath = AbstractSource.normalize(baseDir.getAbsolutePath())
          + Source.RIT_SEPARATOR_CHAR;

      while (!oldPath.startsWith(basePath) && iter2.hasNext()) {
        oldDir = (Source) iter2.next();
      }

      SourceHolder newDir = new SimpleSourceHolder(getProject());

      transList.add(new DirCreator(
          new SimpleSourceHolder(baseDir, getProject()),
          newDir, newName, oldDir.inVcs()));

      transList.add(new RenamePackageTransformation(oldDir, getProject(), newDir,
        RenamePackageTransformation.RELOCATE_FILES_TRANSACTION));
    }
    //</FIX>



    //List errors = new ArrayList();
    //SourceRelocator sourceRelocator = new SourceRelocator(getProject());

    // sourceRelocator.relocateFilesTransaction(oldDir.getChildren(), newDir, errors);

     // failed to move some files for some reason, let's leave it untouched
    // if (errors.size() == 0) {
    //  oldDir.delete();
    //}

   // status.merge(createRelocationWarningsFor(errors));

    return status;
  }

  protected void invalidateCache() {
  }

  public void setRenameInNonJavaFiles(boolean renameInNonJavaFiles) {
    this.renameInNonJavaFiles = renameInNonJavaFiles;
  }

  private static class PackageUsers {
    public MultiValueMap used;
    public List usedList;
  }


  public String getDescription() {
    return super.getDescription();
  }

  public String getKey() {
    return key;
  }

}
