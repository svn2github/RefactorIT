/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.DependencyParticipant;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTTree;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.FileChangeListener;
import net.sf.refactorit.vfs.FileChangeMonitor;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;
import net.sf.refactorit.vfs.SourcePath;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Sander M\u00E4gi
 * @author Anton Safonov
 */
public final class RebuildLogic implements FileChangeListener {
  private static final Logger log = Logger.getLogger(RebuildLogic.class);

  private final Project project;

  private boolean sourcesAnalyzed = false;
  private boolean forceFullRebuild = false;
  private boolean canDoIncrementalBuild = false;

  private Collection sourcesToBeBuilt = null;
  private HashMap lastBuildSourceStamps;
  private final ArrayList addedSources = new ArrayList(100);
  private final ArrayList removedSources = new ArrayList(100);
  private final ArrayList changedSources = new ArrayList(100);
  private boolean renamedSources = false;

  private final HashSet removedTypes = new HashSet();
  private final HashSet typesToBeRefreshed = new HashSet();

  private List allSources;

  private FileChangeMonitor fileChangeMonitor = null;

  private boolean stateIsUnknown = true;

  public static final boolean debug = false;

  RebuildLogic(final Project project) {
    this.project = project;
  }

  final boolean isForcedFullRebuild() {
    return forceFullRebuild;
  }

  final void forceFullRebuild() {
    if (debug) {
      System.err.println("Forcing full rebuild");
    }
    forceFullRebuild = true;
    project.getProjectLoader().getAstTreeCache().cleanAll();
    lastBuildSourceStamps = null;

    invalidateSourceCaches();
//    invalidateSourceCaches(project.getSourcePath().getAllSources());
  }

  final void forceNonIncremental() {
    if (debug) {
      System.err.println("Forcing nonincremental rebuild");
    }
    forceFullRebuild = true;
    lastBuildSourceStamps = null;

//    // debug
//    for (int i = 0; i < allSources.size(); ++i) { // for debug
//      forceSourceModified((Source) allSources.get(i));
//    }

    invalidateSourceCaches();
//    invalidateSourceCaches(project.getSourcePath().getAllSources());
  }

  private void invalidateSourceCaches() {
    invalidateSourceCaches(allSources);
    invalidateSourceCaches(addedSources);
    invalidateSourceCaches(changedSources);
    invalidateSourceCaches(removedSources);
    SourceMap.invalidateSourceCaches();
  }

  private void invalidateSourceCaches(final List sources) {
    if (sources != null) {
      for (int i = 0, max = sources.size(); i < max; i++) {
        ((Source) sources.get(i)).invalidateCaches();
      }
    }
  }

  final void startingRebuild() {
    sourcesToBeBuilt = null;
//    allSources = null;
    sourcesAnalyzed = false;
  }

  final void startingInitialLoad() {
    sourcesToBeBuilt = null;
    allSources = null;
    sourcesAnalyzed = false;
  }

  final void markSuccessfulEndBuild() {
    forceFullRebuild = false;
    saveSourcePathState();
    clearSourcesAndTypes();
  }

  public final void markUnsuccessfulEndBuild() {
    forceFullRebuild = false;
    lastBuildSourceStamps = new HashMap();
    clearSourcesAndTypes();

    allSources = null; // FIXME: not sure that it won't crash
  }

  private final void clearSourcesAndTypes() {
    addedSources.clear();
    removedSources.clear();
    removedTypes.clear();
    changedSources.clear();
    renamedSources = false;
    sourcesToBeBuilt = null;
  }

  private boolean isDoingIncrementalBuild() {
    if (Assert.enabled) {
      Assert.must(sourcesAnalyzed,
          "sources must be analyzed before checking for incremental build!");
    }

    return canDoIncrementalBuild;
  }

  final String getBuildType() {
    if (isForcedFullRebuild()) {
      return "forced full";
    } else if (isDoingIncrementalBuild()) {
      return "incremental";
    } else {
      return "full";
    }
  }

  public final Collection getSourceListToRebuild() {
    //System.err.println("sourcesToBeBuilt: " + sourcesToBeBuilt);
    //  NPE fix
    if (sourcesToBeBuilt == null) {
      this.sourcesToBeBuilt = new HashSet();
    }
    return sourcesToBeBuilt;
  }

  // FIXME: it doesn't fix for anonymous classes - is it a bug or meant that way?
  final void fixSubtypes() {
    for (Iterator i = project.getLoadedTypes(); i.hasNext(); ) {
      final BinTypeRef ref = (BinTypeRef) i.next();
      if (ref.isPrimitiveType()) {
        continue;
      }
      ref.clearSubclasses();
    }

    for (Iterator i = project.getLoadedTypes(); i.hasNext(); ) {
      final BinTypeRef ref = (BinTypeRef) i.next();
      if (ref.isPrimitiveType()) {
        continue;
      }
      if (!ref.isResolved()) {
        continue; // this will be added when resolved
      }

      final BinTypeRef[] superTypes = ref.getSupertypes();
      for (int s = 0, max = superTypes.length; s < max; ++s) {
        final BinTypeRef superRef = superTypes[s];
        superRef.addDirectSubclass(ref);
      }
    }
  }

  final void cleanRebuildableSources() {
    if (debug) {
      System.err.println("cleanRebuildableSources");
    }
    if (!project.getProjectLoader().isIncrementalRebuild() || !isDoingIncrementalBuild()) {
      removeAllSourceTypes();
    } else {
      removeRebuildableSourceTypes();
    }
  }

  private static void addDependants(final DependencyParticipant typeRef,
      final Set addables) {
    if (debug) {
      System.err.println("addDependants for: " + typeRef);
    }
    addables.add(typeRef.getBinCIType().getCompilationUnit().getSource());
    final Set dependencies = typeRef.getDependables();
    for (Iterator i = dependencies.iterator(); i.hasNext(); ) {
      final BinTypeRef user = (BinTypeRef) i.next();
      final CompilationUnit source = user.getBinCIType().getCompilationUnit();
      if (source != null) {
        addables.add(user.getBinCIType().getCompilationUnit().getSource());
        if (debug) {
          System.err.println("addDependant: "
              + user.getBinCIType().getCompilationUnit());
        }
      }
    }
  }

  private void getTypeNames(ASTImpl aNode, final List namesList,
      final FastStack parentNameStack, String packageName) {
    for (; aNode != null; aNode = (ASTImpl) aNode.getNextSibling()) {
      final int type = aNode.getType();
      boolean typeFound = false;

      switch (type) {
        case JavaTokenTypes.PACKAGE_DEF:
          packageName = LoadingASTUtil.extractPackageStringFromExpression(aNode);
          break;
        case JavaTokenTypes.CLASS_DEF:
        case JavaTokenTypes.INTERFACE_DEF:
        case JavaTokenTypes.ENUM_DEF:
        case JavaTokenTypes.ANNOTATION_DEF:
          String ownerName = null;
          if (!parentNameStack.isEmpty()) {
            ownerName = (String) parentNameStack.peek();
          }
          String definedTypeName = LoadingASTUtil.getTypeNameFromDef(aNode);
          if (ownerName != null) {
            definedTypeName = ownerName + '$' + definedTypeName;
          }
          parentNameStack.push(definedTypeName);
          // FIXME optimise
          String qualifiedName = packageName;
          if (qualifiedName == "") {
            qualifiedName = definedTypeName;
          } else {
            qualifiedName += '.' + definedTypeName;
          }
          namesList.add(qualifiedName);

          typeFound = true;
          break;
      }

      if (type == JavaTokenTypes.CLASS_DEF
          || type == JavaTokenTypes.INTERFACE_DEF
          || type == JavaTokenTypes.ENUM_DEF
          || type == JavaTokenTypes.ANNOTATION_DEF
          || type == JavaTokenTypes.OBJBLOCK) {
        final ASTImpl child = (ASTImpl) aNode.getFirstChild();
        if (child != null) {
          getTypeNames(child, namesList, parentNameStack, packageName);
        }
      }
      if (typeFound) {
        parentNameStack.pop();
      }

    }
  }

  private static final class TypeCheckErrorListener implements net.sf.refactorit.parser.ErrorListener {
    private boolean hadErrors = false;

    public final void onError(final String message, final String fileName,
        final int line, final int column) {
      hadErrors = true;
    }

    public final boolean hadErrors() {
      return hadErrors;
    }
  }


  /**
   * Need to test what happens if changed source has userFriendlyErrors
   */
  private void checkTypeChanges(final Source source,
      final List expectedTypes) throws UnhandleableIncrementalException {
    if (RefactorItConstants.alwaysIncrementalRebuild) {
      return;
    }

    try {
      final TypeCheckErrorListener errorListener = new TypeCheckErrorListener();
      final FileParsingData pd = project.getProjectLoader().getTypesLoader()
          .getParsingData(source, errorListener);
      if (errorListener.hadErrors()) {
        throw new Exception("had source parsing errors"); // will be caught in same method
      }
      final ArrayList newNames = new ArrayList(expectedTypes.size());
      getTypeNames(pd.astTree.getAstAt(0), newNames, new FastStack(), "");
      if (expectedTypes.size() != newNames.size()) {
        throw new UnhandleableIncrementalException(
            "DEBUG MESSAGE: Number of types in file differs");
      }

      for (int i = 0; i < expectedTypes.size(); ++i) {
        final String expectedName = ((BinTypeRef) expectedTypes.get(i)).
            getQualifiedName();
        if (!newNames.contains(expectedName)) {
          if (debug) {
            System.err.println("newNames: " + newNames.toString());
            System.err.println("expectedName: " + expectedName);
          }
          throw new UnhandleableIncrementalException(
              "DEBUG MESSAGE: Signature change - no more " + expectedName);
        }
      }
    } catch (Exception e) {
//      if(Assert.enabled && debug) {
//        e.printStackTrace();
//      }
      throw new UnhandleableIncrementalException(
          "DEBUG MESSAGE: Exception while getting parsing data!");
    }

  }

  /**
   * @param sources modified {@link Source sources} list
   * @return additional sources to rebuild
   */
  private Set markDependantSources(final Collection sources) throws
      UnhandleableIncrementalException {

    final ArrayList compilationUnits = getCompilationUnitsForSources(sources);

    final Set addables = new HashSet();

    for (int i = 0; i < compilationUnits.size(); ++i) {
      final CompilationUnit checkable = (CompilationUnit) compilationUnits.get(i);
      final List types = checkable.getDefinedTypes();
      checkTypeChanges(checkable.getSource(), types);
      for (int t = 0; t < types.size(); ++t) {
        final DependencyParticipant typeRef = (DependencyParticipant) types.get(t);
        addDependants(typeRef, addables);

        final List subclasses = typeRef.getAllSubclasses();
        for (int s = 0; s < subclasses.size(); ++s) {
          final DependencyParticipant subRef = (DependencyParticipant) subclasses.get(s);
          addDependants(subRef, addables);
        }
      }
    }

    return addables;
  }

//  private static class RebuildingMarker extends AbstractIndexer {
//    public void visit(BinCIType x) {
//      x.markRebuilding();
//    }
//
//    public void visit(BinMethod x) {
//      x.markRebuilding();
//    }
//
//    public void visit(BinConstructor x) {
//      x.markRebuilding();
//    }
//
//    public void visit(BinField x) {
//      x.markRebuilding();
//    }
//  }
//
//  private static class RebuildDependencyAnalyzer extends AbstractIndexer {
//    private Set rebuildable = new HashSet();
//
//    public Set getRebuildable() {
//      return this.rebuildable;
//    }
//
//    public void visit(BinMethodInvocationExpression x) {
//      if (x.getMethod().isRebuilding()) {
//        this.rebuildable.add(getCurrentType().getBinCIType()
//            .getCompilationUnit().getSource());
//      } else {
//        super.visit(x);
//      }
//    }
//
//    public void visit(BinFieldInvocationExpression x) {
//      if (x.getField().isRebuilding()) {
//        this.rebuildable.add(getCurrentType().getBinCIType()
//            .getCompilationUnit().getSource());
//      } else {
//        super.visit(x);
//      }
//    }
//
//    public void visit(BinNewExpression x) {
//      if (x.getTypeRef().getBinType().isRebuilding()) {
//        this.rebuildable.add(getCurrentType().getBinCIType()
//            .getCompilationUnit().getSource());
//      } else {
//        super.visit(x);
//      }
//    }
//  }

  private ArrayList getCompilationUnitsForSources(Collection sources) {
    // speed up
//    long start = 0;
//    if (debug) {
//      start = System.currentTimeMillis();
//    }
    if (!(sources instanceof Set)) {
      sources = new HashSet(sources);
    }

    final ArrayList compilationUnits = new ArrayList(sources.size());

    ArrayList comps = project.getCompilationUnits();
    for (int i = 0, max = comps.size(); i < max; ++i) {
      final CompilationUnit aCompilationUnit = (CompilationUnit) comps.get(i);
      if (sources.contains(aCompilationUnit.getSource())) {
        compilationUnits.add(aCompilationUnit);
      }
    }

//    if (debug) {
//      final long now = System.currentTimeMillis();
//      System.err.println("getCompilationUnitsForSources took " + (now - start)
//          + " ms");
//    }
//
    return compilationUnits;
  }

  private void setFileChangeMonitor(final FileChangeMonitor fileChangeMonitor) {
    stateIsUnknown = true;

    if (this.fileChangeMonitor != null) {
      this.fileChangeMonitor.removeFileChangeListener(this);
    }

    this.fileChangeMonitor = fileChangeMonitor;

    if (this.fileChangeMonitor != null) {
      this.fileChangeMonitor.addFileChangeListener(this);
    }
  }

  final boolean hasFileChangeMonitor() {
    return this.fileChangeMonitor != null;
  }

  private void checkSourcePathIsSet(final boolean useIDEEvents) {
    if (project.getPaths().hasSourcePathChanged()
        || useIDEEvents != hasFileChangeMonitor()) {
      final SourcePath sourcePath = project.getPaths().getSourcePath();
      if (useIDEEvents) {
        setFileChangeMonitor(sourcePath.getFileChangeMonitor());
      } else {
        setFileChangeMonitor(null);
      }
      project.getPaths().setSourcePathChanged(false);

      if (allSources == null) {
        allSources = sourcePath.getAllSources();
      }
    }
  }

  /* not used
      public FileChangeMonitor getFileChangeMonitor() {
      return fileChangeMonitor;
    }*/

//  public final boolean rebuildStatusIsKnown() {
//    checkSourcePathIsSet();
//
//    if (fileChangeMonitor == null || stateIsUnknown) {
//      return false;
//    }
//
//    if (fileChangeMonitor.hasPossiblePendingEvents()) {
//      return false;
//    }
//
//    return true;
//  }

  private final boolean minimalRebuildPossible(
      final int changedFilesLimit, boolean forceStrictCheck) {
    if (isForcedFullRebuild()) {
      if (debug) {
        System.err.println("WAS FORCED REBUILD");
      }
      return false;
    }

    if (!project.getProjectLoader().isIncrementalRebuild()) {
      if (debug) {
        System.err.println("WAS project setting");
      }
      return false;
    }

    if (project.getProjectLoader().isParsingCanceledLastTime()) {
      if (debug) {
        System.err.println("Canceled last time !");
      }
      if (changedFilesLimit == Integer.MAX_VALUE) {
        log.debug(
            "Forcing full rebuild because of cancel last time");
      }
      return false;
    }

    if (!RefactorItConstants.alwaysIncrementalRebuild || forceStrictCheck) {
      if ((project.getProjectLoader().getErrorCollector()).hasErrors()) {
        if (debug) {
          System.err.println("Were errors!");
        }
        if (changedFilesLimit == Integer.MAX_VALUE) {
          log.debug(
              "Forcing full rebuild because of parsing errors");
        }
        return false;
      }

      if (!removedSources.isEmpty() || renamedSources || !addedSources.isEmpty()) {
        if (debug) {
          if (addedSources.size() < 20) {
            System.err.println("WAS Added:" + addedSources);
          } else {
            System.err.println("WAS Added: " + addedSources.size() + " files");
          }
        }
        if (changedFilesLimit == Integer.MAX_VALUE) {
          log.debug(
              "Forcing full rebuild because of source files number change");
        }
        return false;
      }
    }

    return changedSources.size() <= changedFilesLimit;
  }

  public final void analyzeChanges() {
    sourcesAnalyzed = true;

    final long start = System.currentTimeMillis();

    try {
      canDoIncrementalBuild = true;

      if (!minimalRebuildPossible(Integer.MAX_VALUE, false)) {
        throw new UnhandleableIncrementalException(
            "Minimal rebuild is not possible");
      }

      if (RefactorItConstants.alwaysIncrementalRebuild) {
        this.sourcesToBeBuilt = new HashSet(this.changedSources);
		this.sourcesToBeBuilt.addAll(this.addedSources);
		this.sourcesToBeBuilt.addAll(this.removedSources);
		this.sourcesToBeBuilt.addAll(
        (this.project.getProjectLoader().getErrorCollector()).getErroneousSources());

        Collection dependant = findDependantSources(
            this.sourcesToBeBuilt,
            minimalRebuildPossible(Integer.MAX_VALUE, true));
        if (dependant != null) {
          this.sourcesToBeBuilt.addAll(dependant);
          this.sourcesToBeBuilt.removeAll(this.removedSources);
          return;
        }
      } else if (!minimalRebuildPossible(Integer.MAX_VALUE, true)) {
        throw new UnhandleableIncrementalException(
            "Minimal rebuild not possible");
      }

      // wow! we can even make Dependables based rebuild!!!
      this.sourcesToBeBuilt = new HashSet(changedSources);
      this.sourcesToBeBuilt.addAll(markDependantSources(this.sourcesToBeBuilt));
    } catch (Exception x) {
      if (!(x instanceof UnhandleableIncrementalException)) {
        if (debug) {
          x.printStackTrace();
        }
      }

      if (debug) {
        System.err.println(x.getMessage());

      }
      log.debug(
          "Forcing full rebuild because of type number changes or major parsing errors");

      canDoIncrementalBuild = false;
      this.sourcesToBeBuilt = allSources;
      if (debug) {
        log.debug("RebuildLogic: Everything will be built");
      }
    } finally {
      if (debug) {
        System.err.println("Analyzed changes in "
            + (System.currentTimeMillis() - start) + " ms");
        System.err.println("Rebuilding " + this.sourcesToBeBuilt.size()
            + " sources");
      }

      this.project.getProjectLoader().getAstTreeCache()
          .removeNonExistingSources(allSources);
    }

    // BUG 1987 - shouldn't it add all available JSPs explicitly here?
    // but rename forces full rebuild, so anyway all were added, strange :(
    // at least for incremental rebuild it must do something! or there are dependencies?
  }

  private final Collection findDependantSources(
      final Collection sourcesToAnalyze,
      final boolean dependablesBuildIsPossible) {
    this.removedTypes.clear();

    if (debug) {
      System.err.println("to analyze: " + sourcesToAnalyze);
    }
    HashSet typeNames = new HashSet();

    final TypeCheckErrorListener errorListener = new TypeCheckErrorListener();

    List tmp = new ArrayList(16);

    boolean detectedNewTypes = false;
    // gather type names in modified files
    Iterator it = sourcesToAnalyze.iterator();
    while (it.hasNext()) {
      Source source = (Source) it.next();
      if (debug) {
        System.err.println("source: " + source);
      }
      ASTTree oldTree = source.getASTTree();
      Set typesBefore = new HashSet();
      if (oldTree != null) {
        if (debug) {
          System.err.println("old tree exists: " + oldTree.getTypeNames());
        }
        if (debug) {
          System.err.println("old package: " + oldTree.getPackageName());
        }
        typeNames.addAll(oldTree.getTypeNames());
      }

      Set typesAfter = new HashSet();
      if (!this.removedSources.contains(source)) {
        try {
          final FileParsingData pd = project.getProjectLoader()
              .getTypesLoader().getParsingData(source, errorListener);
          final ASTTree newTree = pd.astTree;

          if (newTree == oldTree) {
            continue; // nothing changed for sure
          }

          if (debug) {
            System.err.println("build new tree: " + newTree.getTypeNames());
          }
          typeNames.addAll(newTree.getTypeNames());

          Iterator fullNames = newTree.getTypeFullNames().iterator();
          while (fullNames.hasNext()) {
            if (newTree.getPackageName().length() > 0) {
              typesAfter.add(newTree.getPackageName() + '.'
                  + ((String) fullNames.next()));
            } else {
              typesAfter.add(fullNames.next());
            }
          }
        } catch (Exception e) {
          // ignore - changes nothing for us
          if (debug) {
            e.printStackTrace();
          }
        }
      }

      if (oldTree != null) {
        Iterator fullNames = oldTree.getTypeFullNames().iterator();
        while (fullNames.hasNext()) {
          if (oldTree.getPackageName().length() > 0) {
            typesBefore.add(oldTree.getPackageName() + '.'
                + ((String) fullNames.next()));
          } else {
            typesBefore.add(fullNames.next());
          }
        }
      }

      tmp.clear();
      tmp.addAll(typesBefore);

      typesBefore.removeAll(typesAfter);
      this.removedTypes.addAll(typesBefore);

      typesAfter.removeAll(tmp);
      if (typesAfter.size() > 0) {
        detectedNewTypes = true;
      }
    }
    if (debug) {
      System.err.println("typeNames: " + typeNames);

    }
    if (dependablesBuildIsPossible && !errorListener.hadErrors()
        && this.removedTypes.size() == 0 && !detectedNewTypes) {

      if (debug) {
        System.err.println("Dependables build is possible");
      }
      return null; // we can use Dependables for rebuild
    } else {
      if (errorListener.hadErrors()) {
        if (debug) {
          System.err.println("had errors");
        }
      }
      if (dependablesBuildIsPossible) {
        if (debug) {
          System.err.println("Dependables build is NOT possible");
        }
      }

      List names = new ArrayList(this.removedTypes);
      this.removedTypes.clear();
      for (int i = 0, max = names.size(); i < max; i++) {
        String fqName = (String) names.get(i);
        BinTypeRef typeRef = this.project.findTypeRefForName(fqName);
        if (typeRef != null) {
//System.err.println("removed: " + typeRef);
          this.removedTypes.add(typeRef);
        } else if (Assert.enabled) {
          System.err.println("no typeRef for name: " + fqName);
        }
      }

      if (this.removedTypes.size() != 0) {
        if (debug) {
          System.err.println("removedTypes: " + this.removedTypes);
        }
      }

      if (detectedNewTypes && debug) {
        System.err.println("detected new types");
      }
    }

    // gather subtype names
    Set curSubTypes = typeNames;
    do {
      Set newSubTypes = new HashSet();
      Iterator all = this.allSources.iterator();
      while (all.hasNext()) {
        Source source = (Source) all.next();
        if (sourcesToAnalyze.contains(source)) {
          continue;
        }

        HashSet supers = source.getASTTree().getSuperTypeNames();
        Iterator types = curSubTypes.iterator();
        while (types.hasNext()) {
          if (supers.contains(types.next())) {
            newSubTypes.addAll(source.getASTTree().getTypeNames());
            break;
          }
        }
      }
      newSubTypes.removeAll(typeNames);
      typeNames.addAll(newSubTypes);
      curSubTypes = newSubTypes;
    } while (curSubTypes.size() > 0);
    if (debug) {
      System.err.println("typeNames2: " + typeNames);

      // gather dependants
    }
    ArrayList result = new ArrayList();
    Iterator all = this.allSources.iterator();
    while (all.hasNext()) {
      Source source = (Source) all.next();
      if (sourcesToAnalyze.contains(source)) {
        continue;
      }

      HashSet idents = source.getASTTree().getIdents();
      Iterator types = typeNames.iterator();
      while (types.hasNext()) {
        if (idents.contains(types.next())) {
          result.add(source);
          break;
        }
      }
    }
    if (debug) {
      System.err.println("dependant: " + result);

    }
    return result;
  }

  final boolean calculateSourcepathChanges(final boolean useIDEEvents) {
    boolean result = false;

    checkSourcePathIsSet(useIDEEvents);

    if (!hasFileChangeMonitor()) {
      result = fullCalculateSourcepathChanges();
      stateIsUnknown = true;
    } else {
      fileChangeMonitor.trigger(project); // this one is for those fake monitors

      if (stateIsUnknown) { // first run - no info to compare with yet
        result = fullCalculateSourcepathChanges();
        stateIsUnknown = false;
      } else {
        result = ideCalculateSourcepathChanges();
      }
    }

    final StringBuffer changes = new StringBuffer(16);
    synchronized (changes) {
      appendChangesToBuffer(changes, addedSources, "added");
      appendChangesToBuffer(changes, removedSources, "removed");
      if (renamedSources) {
        if (changes.length() > 0) {
          changes.append("; ");
        }
        changes.append("renamed some");
      }
      appendChangesToBuffer(changes, changedSources, "changed");
      if (changes.length() > 0) {
        log.debug("Analyzed sources - " + changes.toString());
      }
    }

    return result;
  }

  private static void appendChangesToBuffer(
      final StringBuffer changes, final List sources, final String prefix) {
    if (sources != null && sources.size() > 0) {
      if (changes.length() > 0) {
        changes.append("; ");
      }
      changes.append(prefix + ": ");
      if (sources.size() <= 15) { // magic number
        for (int i = 0, max = sources.size(); i < max; i++) {
          if (i > 0) {
            changes.append(", ");
          }
          changes.append(((Source) sources.get(i)).getDisplayPath());
        }
      } else {
        changes.append("many");
      }
    }
  }

  private boolean ideCalculateSourcepathChanges() {
    final long start = System.currentTimeMillis();

    try {
      final SourcePath sourcePath = project.getPaths().getSourcePath();

      if (allSources == null) {
        allSources = sourcePath.getAllSources();
      }
      invalidateSourceCaches();

      boolean changed = false;

      changed |= !addedSources.isEmpty();
      changed |= !removedSources.isEmpty();
      changed |= !changedSources.isEmpty();
      changed |= renamedSources;

      if (changed) {
        if (!addedSources.isEmpty()) {
          if (debug) {
            System.err.println("Added " + addedSources);
          }
        }
        if (!removedSources.isEmpty()) {
          if (debug) {
            System.err.println("Removed " + removedSources);
          }
        }
        if (!changedSources.isEmpty()) {
          if (debug) {
            System.err.println("Changed " + changedSources);
          }
        }
      }

      return changed;
    } finally {
      if (debug) {
        System.err.println("Checked source path for changes (ide events) "
            + (System.currentTimeMillis() - start) + "ms");
      }
    }
  }

  private boolean fullCalculateSourcepathChanges() {
    final long start = System.currentTimeMillis();
    long getAllEnd = 0;

    invalidateSourceCaches();
    addedSources.clear();
    removedSources.clear();
    changedSources.clear();
    renamedSources = false;

    try {
      boolean changed = false;
      allSources = project.getPaths().getSourcePath().getAllSources();
      invalidateSourceCaches(allSources);
      getAllEnd = System.currentTimeMillis();
      if (lastBuildSourceStamps == null) {
        addedSources.addAll(allSources);
        return true;
      }

      removedSources.addAll(lastBuildSourceStamps.keySet());

      for (int i = 0, max = allSources.size(); i < max; ++i) {
        final Source s = (Source) allSources.get(i);
        final SourceStamp stmp = (SourceStamp) lastBuildSourceStamps.get(s);

        if (stmp == null) {
          addedSources.add(s);
          changed = true;
        } else {
          if (stmp.lastmodified != s.lastModified()) {
            changedSources.add(s);
            changed = true;
          }
          removedSources.remove(s);
        }
      }

      changed |= !removedSources.isEmpty();

      if (changed) {
        if (!addedSources.isEmpty()) {
          if (debug) {
            System.err.println("Added " + addedSources);
          }
        }
        if (!removedSources.isEmpty()) {
          if (debug) {
            System.err.println("Removed " + removedSources);
          }
        }
        if (!changedSources.isEmpty()) {
          if (debug) {
            System.err.println("Changed " + changedSources);
          }
        }
      }

      return changed;
    } finally {
      if (debug) {
        System.err.println("Checked source path for changes (full scan) "
            + (System.currentTimeMillis() - start) + "ms ("
            + (getAllEnd - start) + ")");
      }
    }
  }

  public final void forceSourceModified(final Source source) {
    if (debug) {
      System.err.println("forceSourceModified: " + source);
    }
    final SourceStamp stmp = (SourceStamp) lastBuildSourceStamps.get(source);
    if (stmp != null) {
      stmp.lastmodified = -2; // FIXME: magic
    }
    project.getProjectLoader().getAstTreeCache().removeSource(source);
    source.invalidateCaches();
  }

  private void saveSourcePathState() {
    lastBuildSourceStamps = new HashMap();
    for (int i = 0; i < allSources.size(); ++i) {
      final Source s = (Source) allSources.get(i);
      final SourceStamp stmp = new SourceStamp(s);
      lastBuildSourceStamps.put(s, stmp);
    }
  }

  /**
   * N.B! Also updates prototype manager
   */
  private void removeRebuildableSourceTypes() {
    if (debug) {
      System.err.println("removeRebuildableSourceTypes: " + sourcesToBeBuilt);
    }

    final ArrayList rebuildableCompilationUnits = getCompilationUnitsForSources(
        sourcesToBeBuilt);
    HashSet removedCompilationUnits = new HashSet(
        getCompilationUnitsForSources(this.removedSources));
    removedCompilationUnits.addAll(
        getCompilationUnitsForSources(this.addedSources));
    if (debug) {
      System.err.println("removedCompilationUnits: " + removedCompilationUnits);

      // source files are to be removed
    }
    final ArrayList redefinableTypes = new ArrayList();
    for (int i = 0; i < rebuildableCompilationUnits.size(); ++i) {
      final CompilationUnit aCompilationUnit
          = (CompilationUnit) rebuildableCompilationUnits.get(i);
      project.getCompilationUnits().remove(aCompilationUnit);
      final List types = aCompilationUnit.getDefinedTypes();
//System.err.println("types: " + types);
      redefinableTypes.addAll(types);

      aCompilationUnit.cleanUp();
    }

    CollectionUtil.addAllNew(redefinableTypes, new ArrayList(removedTypes));

    if (debug) {
      System.err.println("removedTypes: " + removedTypes);

      // first we remove all arrays and locals/anonymous of redefinable types
    }

    typesToBeRefreshed.clear();

    final Set toBeRemoved = new HashSet();
    final Map workingMap = project.loadedTypes;
    for (Iterator entries = workingMap.entrySet().iterator(); entries.hasNext(); ) {
      final Map.Entry entry = (Map.Entry) entries.next();
      final BinTypeRef typeRef = (BinTypeRef) entry.getValue();
      if (typeRef.isResolved() && typeRef.isReferenceType()
          && !toBeRemoved.contains(entry.getKey())) {
        if (typeRef.isArray()) {
          final BinTypeRef atRef = ((BinArrayType) typeRef.getBinType()).
              getArrayType();
          if (redefinableTypes.contains(atRef)) {
            //toBeRemoved.add(entry.getKey());
            typesToBeRefreshed.add(typeRef);
          }
        }
        // NOTE: inners of local and anonymous will get here
        else if (typeRef.getBinCIType().isInnerType()
            && !redefinableTypes.contains(typeRef)) {
          final BinCIType type = typeRef.getBinCIType()
              .getTopLevelEnclosingType();
          if (type != null && redefinableTypes.contains(type.getTypeRef())) {
            toBeRemoved.add(entry.getKey());
          }
        }
      }
    }
    final Iterator tbr = toBeRemoved.iterator();
    while (tbr.hasNext()) {
      final String name = (String) tbr.next();
      project.loadedTypes.remove(name);
    }

    // Then we handle all actions that could need typerefs intact
    // for example getQualifiedName of inner type might not be yet cached
    // and so it calls its supertype ref for getName
    for (int i = 0, max = redefinableTypes.size(); i < max; ++i) {
      final BinTypeRef typeRef = (BinTypeRef) redefinableTypes.get(i);
      project.getDefinedTypeNames().remove(typeRef.getQualifiedName());
      typeRef.getPackage().removeType(typeRef.getQualifiedName());

      // clean subclass caches
      List subClasses = typeRef.getAllSubclasses();
      for (int k = 0, kMax = subClasses.size(); k < kMax; ++k) {
        ((BinTypeRef) subClasses.get(k)).getBinCIType().cleanMethodCaches();
      }
    }

    if (RefactorItConstants.alwaysIncrementalRebuild) {
      Iterator allTypes = project.loadedTypes.values().iterator();
      while (allTypes.hasNext()) {
        final DependencyParticipant typeRef = (DependencyParticipant) allTypes.next();
        typeRef.removeDependables(this.removedTypes);
        typeRef.removeDirectSublasses(this.removedTypes);
      }
    }

    // now we do not need typeRefs any more and clean them
    for (int i = 0, max = redefinableTypes.size(); i < max; ++i) {
      final DependencyParticipant typeRef
          = (DependencyParticipant) redefinableTypes.get(i);
      if (this.removedTypes.contains(typeRef)) {
        project.loadedTypes.remove(typeRef.getQualifiedName());
        typeRef.getBinCIType().cleanForPrototype();
        typeRef.cleanUp();
      } else {
        project.getProjectLoader().getPrototypeManager().registerPrototype(typeRef);
        typeRef.cleanForReuse();
      }
    }

    Iterator removedSrc = removedCompilationUnits.iterator();
    while (removedSrc.hasNext()) {
      ((CompilationUnit) removedSrc.next()).cleanUp();
    }
    project.getCompilationUnits().removeAll(removedCompilationUnits);

    if (debug) {
      System.err.println("End removeRebuildableSourceTypes");
    }
  }

  /**
   * Removes all sourceTypes - this is for non incremental compiling
   */
  private void removeAllSourceTypes() {
    if (debug) {
      System.err.println("removeAllSourceTypes");

    }
    final HashSet toBeRemoved = new HashSet();

    // remove all types read from source files
    for (Iterator definedNames = project.getDefinedTypeNames().iterator();
        definedNames.hasNext(); ) {
      final String name = (String) definedNames.next();
      toBeRemoved.add(name);
    }
    project.getDefinedTypeNames().clear();

    // remove all source related typerefs since it's not incremental rebuild
    final Map workingMap = project.loadedTypes;
    for (Iterator entries = new HashSet(workingMap.entrySet()).iterator();
        entries.hasNext(); ) {
      final Map.Entry entry = (Map.Entry) entries.next();
      final BinTypeRef typeRef = (BinTypeRef) entry.getValue();
      if (typeRef.isResolved() && !toBeRemoved.contains(entry.getKey())
          && typeRef.isReferenceType()) {
        if (typeRef.isArray()) {
          final BinTypeRef atRef = ((BinArrayType) typeRef.getBinType()).
              getArrayType();
          if (atRef.isResolved() && atRef.isReferenceType()) {
            if (atRef.getBinCIType().isFromCompilationUnit()) {
              toBeRemoved.add(entry.getKey());
            }
          }
        }
// NOTE: local and anonymous types typically are not added to the loaded list
// NOTE: this code is for the case of new Type(){class Inner{}}.new Inner() {};
// as of Jacks test T1591qa16
        else { // local and anonymous inners will get here
          if (typeRef.getBinCIType().isFromCompilationUnit()) {
            toBeRemoved.add(entry.getKey());
          }
        }
      }
    }

    final Set rebuildableSourcelessTypes = new HashSet();

    for (Iterator it = toBeRemoved.iterator(); it.hasNext(); ) {
      final String name = (String) it.next();
      final DependencyParticipant removed = (DependencyParticipant) project.loadedTypes.remove(
          name);

      rebuildableSourcelessTypes.addAll(removed.getAllBinaryDependables());

      try {
        final BinPackage apackage = removed.getPackage();
        if (apackage.isContainsType(name)) {
          // NOTE: packages usually contain only defined types, no arrays or locals
          apackage.removeType(name);
        }
      } catch (NullPointerException e) {
        if (Assert.enabled) {
          System.err.println("on remove of: " + removed);
          e.printStackTrace();
        }
      }

      removed.cleanUp();
    }

    final Iterator it = rebuildableSourcelessTypes.iterator();
    while (it.hasNext()) {
      final BinTypeRef sub = (BinTypeRef) it.next();

      // just to be sure
      if (!sub.isResolved() || sub.getBinCIType().isFromCompilationUnit()) {
        continue;
      }

      project.loadedTypes.remove(sub.getQualifiedName());

      // could be already removed or inner or local
      if (sub.getPackage().isContainsType(sub.getQualifiedName())) {
        sub.getPackage().removeType(sub.getQualifiedName());
      }

      sub.cleanUp();
    }

    ArrayList comps = project.getCompilationUnits();
    for (int i = 0, max = comps.size(); i < max; i++) {
      final CompilationUnit source = (CompilationUnit) comps.get(i);
      source.cleanUp();
    }
    comps.clear();

    if (debug) {
      System.err.println("End removeAllSourceTypes");
    }
  }

  static final class SourceStamp {
    final Source aSource;
    long lastmodified;

    SourceStamp(final Source forSource) {
      aSource = forSource;
      lastmodified = aSource.lastModified();
    }
  }


  public final void fileCreated(final Source source) {
    if (!hasFileChangeMonitor()) {
      return;
    }

    if (debug) {
      System.err.println("file created: " + source);

    }
    if (!removedSources.remove(source)) {
      if (!addedSources.contains(source)) {
        addedSources.add(source);
      }
    } else {
      if (!changedSources.contains(source)) {
        changedSources.add(source);
      }
    }

    if (allSources != null && !allSources.contains(source)) {
      allSources.add(source);
    }
  }

  public final void fileDeleted(final Source source) {
    if (!hasFileChangeMonitor()) {
      return;
    }

    if (debug) {
      System.err.println("file deleted: " + source);

    }
    if (!addedSources.remove(source)) {
      if (!removedSources.contains(source)) {
        removedSources.add(source);
      }

      changedSources.remove(source);
    }

    if (allSources != null) {
      allSources.remove(source);
    }
  }

  public final void fileContentsChanged(final Source source) {
    if (!hasFileChangeMonitor()) {
      return;
    }

    if (debug) {
      System.err.println("file changed: " + source);

    }
    if (!addedSources.contains(source)) {
      if (!changedSources.contains(source)) {
        changedSources.add(source);
      }
    }
  }

  public final void fileRenamed(final Source newSource, final String oldName) {
    if (!hasFileChangeMonitor()) {
      return;
    }

    if (debug) {
      System.err.println("file renamed: " + newSource);

    }
    if (newSource.isFile()) {
      renamedSources = true;

      fileCreated(newSource);

      // we don't know exactly which one was renamed, let's rebuild all similar
      List removed = SourceMap.getSourcesForName(oldName);
      for (int i = 0, max = removed.size(); i < max; i++) {
        fileDeleted((Source) removed.get(i));
      }
    }
  }

  public final void unknownChangesHappened() {
    stateIsUnknown = true;
  }

  public Set getTypesToBeRefreshed() {
    return this.typesToBeRefreshed;
  }
}


final class UnhandleableIncrementalException extends Exception {
  public UnhandleableIncrementalException(final String mess) {
    super(mess);
  }
}
