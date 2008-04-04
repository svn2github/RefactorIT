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
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.PhraseSplitter;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.common.util.graph.WeightedGraph;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.DelegatingVisitor;
import net.sf.refactorit.query.rename.DependenciesFinder;
import net.sf.refactorit.query.text.ManagingNonJavaIndexer;
import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.query.text.PathOccurrence;
import net.sf.refactorit.query.text.QualifiedNameIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeIndexer;
import net.sf.refactorit.query.usage.TypeNameIndexer;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.StaticImports;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.FileRenamer;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * Renames class or interface.
 */
public class RenameType extends RenameMember {

  /**
   * Keeps InvocationData usages: all and those ones, that needed are unckecked by
   * the default in the confirmation model
   */
  public class ItemUsages {
    private final List allUsages;
    private final List nonCheckedUsages;

    public ItemUsages(List allUsages, List nonCheckedUsages) {
      this.allUsages = allUsages;
      this.nonCheckedUsages = nonCheckedUsages;
    }

    public List getAllUsages() {
      return allUsages;
    }

    public List getNonCheckedUsages() {
      return nonCheckedUsages;
    }
  }


  public static String key = "refactoring.rename.type";

  private ManagingIndexer supervisor;

  private boolean renameInNonJavaFiles;
  private boolean semanticRename;

  private BinMember[] additionalItems = new BinMember[]{};


  public RenameType(final RefactorItContext context, final BinCIType type) {
    super("RenameType", context, type);
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = super.checkPreconditions();

    if (getItem() instanceof BinCIType) {
      BinCIType item = (BinCIType) getItem();

      if (!item.isFromCompilationUnit()) {
        status.addEntry(StringUtil.capitalizeFirstLetter(item.getMemberType())
            + " " + item.getQualifiedName() + "\n"
            + "is outside of the source path and could not be renamed!",
            RefactoringStatus.ERROR);
      }
    }
    return status;
  }

  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = super.checkUserInput();
    BinCIType type = (BinCIType) getItem();

    if (!NameUtil.isValidIdentifier(getNewName())) {
      status.addEntry("Not a valid Java 2 type identifier",
          RefactoringStatus.ERROR);
    }

    final BinPackage pkg = type.getPackage();
    if (pkg != null) {
      final BinTypeRef existingType = pkg.findTypeForShortName(getNewName());
      if (existingType != null) {
        List existing = new ArrayList(1);
        existing.add(existingType.getBinCIType());
        status.merge(new RefactoringStatus(
            "Class or interface exists in the same package", existing,
            RefactoringStatus.ERROR));
      }

      List innerConflicts = innerTypeExists(type, getNewName());
      if (innerConflicts.size() > 0) {
        status.merge(new RefactoringStatus(
            "Inner classes or interfaces with the same name already exist",
            innerConflicts, RefactoringStatus.ERROR));
      }
    }

    status.merge(checkImportConflicts(type, getNewName()));

    // Check if there are some packages/variables around who would mask the
    // name
    final IdentifierChecker identifierChecker = new IdentifierChecker(type,
        getNewName());
    if (type.isPrivate()) {
      identifierChecker.visit(type.getCompilationUnit());
    } else {
      identifierChecker.visit(type.getProject());
    }
    final List identConflicts = identifierChecker.getConflicts();
    if (identConflicts.size() > 0) {
      status.merge(new RefactoringStatus(
          "The selected name matches with existing field/local",
          identConflicts, RefactoringStatus.WARNING));
    }

    return status;
  }











  private List showSemanticRenameItemsToUser(RenameType.ItemUsages usages,
      final RefactorItContext context) {

    ConfirmationTreeTableModel model =
        new ConfirmationTreeTableModel("", usages.getAllUsages(), usages.getNonCheckedUsages());

    model = (ConfirmationTreeTableModel) DialogManager
        .getInstance().showSettings("Semantic rename", context, model,
        "RefactorIT detected the following semantically coupled items: ", "refact.semantic_rename");

    if (model == null) {
      // null - when user pressed cancel button, so return empty list to remove
      return new ArrayList();
    }

    return model.getCheckedUsages();
  }









  public TransformationList performChange() {
    TransformationList transList = super.performChange();

    if (!transList.getStatus().isOk()) {
      return transList;
    }




      if((this.isSemanticRename())) {
        RenameType.ItemUsages usages = this.getSemanticRenameItemUsages();
        List invocationDatas = new ArrayList();
        //Show the dialog only if there is at least one semantically connected item
        if(usages.allUsages.size() > 0)
          invocationDatas = showSemanticRenameItemsToUser(usages, getContext());
        if(invocationDatas.size() > 0) {
          this.setAdditionalItems(InvocationData.getInvocationObjects(invocationDatas));
        }
      } else {

      }

    final BinCIType type = (BinCIType) getItem();

    final List invocations = getSupervisor().getInvocations();
    final List nonJavaOccurrences = getNonJavaOccurrences();

    //final BinMember[] memberList = getSemanticRenameItems();

    ExtendedConfirmationTreeTableModel model = new
        ExtendedConfirmationTreeTableModel(
        type, invocations, Collections.EMPTY_LIST, nonJavaOccurrences);

// changed to generic preview
//    model = (ExtendedConfirmationTreeTableModel) DialogManager.getInstance()
//        .showConfirmations(getContext(), model, "refact.rename.type");

    if (model == null) {
      transList.getStatus().merge(
          new RefactoringStatus("", RefactoringStatus.CANCEL));
      return transList;
    }

    final MultiValueMap usages = ManagingIndexer.getInvocationsMap(model.
        getCheckedUsages());

    /*
     * Deletes old .class file if exists Must be before
     * type.setName(getNewName());
     */
    String cls = type.getQualifiedName().replace('.', '/')
        + ClassFilesLoader.CLASS_FILE_EXT;
    type.getProject().getPaths().getClassPath().delete(cls);

    String oldName = type.getName();

    // Alter sources

    transList.add(
        new FileRenamer(type.getCompilationUnit(), type.getName(), getNewName()));
    for (final Iterator i = usages.entrySet().iterator(); i.hasNext(); ) {
      final Map.Entry entry = (Map.Entry) i.next();

      final CompilationUnit compilationUnit = (CompilationUnit) entry.getKey();
      if (Assert.enabled) {
        Assert.must(compilationUnit != null, "CompilationUnit is null during rename");
      }

      RenameTransformation renameTypeTransformation = new RenameTransformation(
          compilationUnit, (List) entry.getValue(), getNewName());

      transList.add(renameTypeTransformation);
    }



    String oldWord = ((BinMember)getItem()).getName();
    String newWord = getNewName();

    SemanticRenamer renamer = new SemanticRenamer(oldWord, newWord);
    transList.merge(renamer.rename(getContext(), getAdditionalItems(),
        renameInNonJavaFiles, isRenameInJavadocs()));

    addNonJavaEditors(type, model, oldName, transList);
    addStaticImportChanges(transList, invocations);
    return transList;
  }


  /**
   * Checks all places where member is invoked via single static import and
   * either adds a new single static import or changes the existing import,
   * depending whether some other invocatons depend on that single static import.
   *
   * @param transList
   * @param invocations
   */
  protected void addStaticImportChanges(TransformationList transList, List invocations) {
  	Map staticImportChanges = new HashMap();
  	for (Iterator iter = invocations.iterator(); iter.hasNext();) {
  		InvocationData invocation = (InvocationData) iter.next();
  		CompilationUnit compilationUnit = invocation.getCompilationUnit();
  		if (staticImportChanges.get(compilationUnit) == null) {
  			if ((invocation.getInConstruct() != null)) {
  				SourceConstruct expr = invocation.getInConstruct();
  				//BinTypeRef typeRef = expr.
  				StaticImports.SingleStaticImport single =  compilationUnit.getStaticImports().getSingleStaticImportFor( (BinCIType) getItem());
  				if (single != null) {
  					// type is invoked via single static import
  					MultiValueMap singleStaticImportUsages = single.getUsages();
  					boolean otherUsages = false;
  					if (singleStaticImportUsages.keySet().size() > 1 ) {
  						otherUsages = true;
  					}
  					if (!otherUsages) {
  						staticImportChanges.put(compilationUnit,
  								new RenameTransformation(
  										compilationUnit,
											single.getMemberNameNode(), getNewName()));
  					} else {
  						staticImportChanges.put(compilationUnit,
  								new StringInserter(
  										compilationUnit,
											single.getMemberNameNode().getEndLine(), single.getMemberNameNode().getEndColumn(),
											FormatSettings.LINEBREAK + "import static " + this.getItem().getParentType().getQualifiedName() + "." + getNewName() + ";"));
  					}

  				}

  			}
  		}

  	}
  	for (Iterator iter = staticImportChanges.values().iterator(); iter.hasNext();) {
  		transList.add(iter.next());

  	}
  }

  /**
   * Adds non-java file edits to global editor.
   */
  private void addNonJavaEditors(final BinCIType type,
      final ExtendedConfirmationTreeTableModel model, final String oldName,
      final TransformationList transList) {
    final MultiValueMap nonJavaUsages = new MultiValueMap();
    for (Iterator i = model.getCheckedNonJavaOccurrences().iterator();
        i.hasNext(); ) {
      Occurrence o = (Occurrence) i.next();
      nonJavaUsages.putAll(o.getLine().getSource(), o);
    }

    if (!nonJavaUsages.isEmpty()) {
      String newQualifiedName = type.getQualifiedName().substring(0,
          type.getQualifiedName().length() - oldName.length())
          + this.getNewName();

      String oldQualifiedName = ((BinType) this.getItem()).getQualifiedName();
      
      
      for (final Iterator i = nonJavaUsages.entrySet().iterator(); i.hasNext(); ) {
        final Map.Entry entry = (Map.Entry) i.next();
        // create temporary SourceHolder
        SourceHolder sf = new SimpleSourceHolder((Source) entry.getKey(),
            getContext().getProject());

        for (Iterator i2 = ((List) entry.getValue()).iterator(); i2.hasNext(); ) {
          String changedName = newQualifiedName;
          Occurrence o = (Occurrence) i2.next();
          if(o instanceof PathOccurrence) {
            PathOccurrence po = (PathOccurrence)o;
            if(po.isSlashedPath()) {
              changedName = StringUtil.getSlashedPath(newQualifiedName);
            } else if(po.isBackslashedPath()) {
              changedName = StringUtil.getBackslashedPath(newQualifiedName);
            }
          }
            transList.add(
              new RenameTransformation(sf, oldQualifiedName, changedName, o));
        }
      }
    }
  }

  private List selectInvocationsOfAsts(List invocations, List asts) {
    List result = new ArrayList(invocations.size());

    for (int i = 0; i < invocations.size(); i++) {
      InvocationData d = (InvocationData) invocations.get(i);
      if (asts.contains(d.getWhereAst())) {
        result.add(d);
      }
    }

    return result;
  }

  protected ManagingIndexer getSupervisor() {
    if (supervisor == null) {
      supervisor = new ManagingIndexer(true);

      new TypeNameIndexer(supervisor, (BinCIType) getItem(),
          isRenameInJavadocs());

      supervisor.visit(((BinMember) getItem()).getProject());
    }
    return supervisor;
  }

  private RefactoringStatus checkImportConflicts(BinCIType type, String newName) {
    final RefactoringStatus status = new RefactoringStatus();

    status.merge(importConflictsInOwnSource(type, newName));

    final List compilationUnitList = type.getProject().getCompilationUnits();
    for (int i = 0, max = compilationUnitList.size(); i < max; i++) {
      CompilationUnit source = (CompilationUnit) compilationUnitList.get(i);

      status.merge(RenameType.importConflictsInOtherSource(source, type,
          newName));
    }

    return status;
  }

  private RefactoringStatus importConflictsInOwnSource(BinCIType type,
      String newName) {
    List importConflicts = new ArrayList();
    final CompilationUnit source = type.getCompilationUnit();

    List importedPackages = source.getImportedPackages();
    if (importedPackages != null) {
      List importedPackageNodes = source.getImportedPackageNodes();

      for (int p = 0, pMax = importedPackages.size(); p < pMax; ++p) {
        final BinPackage pkg = (BinPackage) importedPackages.get(p);

        if (pkg.findTypeForShortName(newName) != null) {
          if (importedPackageNodes != null && p < importedPackageNodes.size()) {
            ASTImpl pkgNode = (ASTImpl) importedPackageNodes.get(p);
            if(pkgNode == null) {
              // this is happening when newName is located in the java.lang.* package
              // java.lang.* package is appended by default, therefore, should
              // not be treated as a possible package with name conflict
              continue; 
            }
            BinSelection clickable = RenameType.generateClickableImport(source,
                pkgNode);
            CollectionUtil.addNew(importConflicts, clickable);
          } else {
            new Exception("no nodes for package import in "
                + source).printStackTrace(System.err);
            CollectionUtil.addNew(importConflicts, pkg);
          }
        }
      }
    }

    // Check imported types
    List importedTypeNames = source.getImportedTypeNames();
    List importedTypeNameNodes = source.getImportedTypeNameNodes();
    if (importedTypeNames != null) {
      for (int p = 0, pMax = importedTypeNames.size(); p < pMax; ++p) {
        final String fqn = (String) importedTypeNames.get(p);

        if (newName.equals(fqn) || fqn.endsWith('.' + newName)) {
          if (importedTypeNameNodes != null && p < importedTypeNameNodes.size()) {
            ASTImpl typeNode = (ASTImpl) importedTypeNameNodes.get(p);
            BinSelection clickable = RenameType.generateClickableImport(source,
                typeNode);
            CollectionUtil.addNew(importConflicts, clickable);
          } else {
            new Exception("no nodes for type import in "
                + source).printStackTrace(System.err);
            CollectionUtil.addNew(importConflicts, fqn);
          }
        }
      }
    }

    // check single static imports
    StaticImports.SingleStaticImport singleStaticImport = source.getStaticImports().getSingleStaticImport(newName);
    if (singleStaticImport != null) {
      ASTImpl typeNode = singleStaticImport.getMemberNameNode();
      BinSelection clickable = RenameType.generateClickableImport(source,
          typeNode);
      CollectionUtil.addNew(importConflicts, clickable);
    }

    // check on-demand static imports
    List onDemandImports = source.getStaticImports().getOnDemandImports();
    if (onDemandImports != null) {
    	for (Iterator iter = onDemandImports.iterator(); iter.hasNext();) {
    		StaticImports.OnDemandStaticImport onDemand = (StaticImports.OnDemandStaticImport) iter.next();
    		BinTypeRef existingType = onDemand.getType(newName);
    		if (existingType != null) {
    			ManagingIndexer supervisor = new ManagingIndexer();
    			new TypeIndexer(supervisor, existingType.getBinCIType(),
    					new BinClassSearchFilter(true, false, false, false, false, false,
    							true, false, false, false, false));
    			supervisor.visit(source);
    			if (supervisor.getInvocations().size() != 0) {
    				BinSelection clickable = RenameType.generateClickableImport(source, onDemand.getNode());
    				CollectionUtil.addNew(importConflicts, clickable);
    			}
    		}
    	}
    }




    // Check if import-conflicts do exist
    if (importConflicts.size() > 0) {
      return new RefactoringStatus("Import conflicts or ambiguous imports",
          importConflicts, RefactoringStatus.ERROR);
    }

    return null;
  }

  /*
   * Returns if given BinType <CODE> type </CODE> has accessible inner types
   * with name <CODE> name </CODE>
   */
  private List innerTypeExists(BinCIType type, String name) {
    List inners = type.getAccessibleInners(type); // FIXME or
    // getDeclaredTypes ?
    List conflicts = new ArrayList();

    for (int i = 0, max = inners.size(); i < max; i++) {
      final BinType inner = (BinType) inners.get(i);
      if (name.equals(inner.getName())) {
        CollectionUtil.addNew(conflicts, inner);
      }
    }

    return conflicts;
  }

  public static RefactoringStatus importConflictsInOtherSource(
      CompilationUnit source, BinCIType type, String newName) {

    final List importConflicts = new ArrayList();
    boolean typeWasImported = false;

    List importedTypeNames = source.getImportedTypeNames();
    if (importedTypeNames != null) {
      for (int p = 0, pMax = importedTypeNames.size(); p < pMax; ++p) {
        final String fqn = (String) importedTypeNames.get(p);

        if (fqn.equals(type.getQualifiedName())) { // full name import
          typeWasImported = true;
          break;
        } else if (source.getPackage().isIdentical(type.getPackage())
            && fqn.equals(type.getName())) { // direct import within
          // the same package
          typeWasImported = true;
          break;
        }
      }
    }


    if ((type.isInnerType() && (type.isStatic()))) {
    	// check single static imports
    	StaticImports staticImports = source.getStaticImports();
    	if (staticImports != null) {
    		List singleStaticImports = staticImports.getSingleImports();
    		if (singleStaticImports != null) {
    			for (Iterator iter = singleStaticImports.iterator(); iter.hasNext();) {
    				StaticImports.SingleStaticImport singleStatic = (StaticImports.SingleStaticImport) iter.next();
    				if (type.getQualifiedName().replace('$', '.').equals(singleStatic.getQualifiedName())) {
    					typeWasImported = true;
    					break;
    				}
    			}
    		}

    		//	check on-demand imports
    		List onDemandImports = staticImports.getOnDemandImports();
    		if (onDemandImports != null) {
    			for (Iterator iter = onDemandImports.iterator(); iter.hasNext();) {
    				StaticImports.OnDemandStaticImport onDemand = (StaticImports.OnDemandStaticImport) iter.next();
    				BinTypeRef existingType = onDemand.getType(newName);
    				if (existingType != null) {
    					ManagingIndexer supervisor = new ManagingIndexer();
    					new TypeIndexer(supervisor, existingType.getBinCIType(),
    							new BinClassSearchFilter(true, false, false, false, false, false,
    									true, false, false, false, false));
    					supervisor.visit(source);
    					if (supervisor.getInvocations().size() != 0) {
    						BinSelection clickable = RenameType.generateClickableImport(source, onDemand.getNode());
    						CollectionUtil.addNew(importConflicts, clickable);
    					}
    				}
    			}
    		}
    	}
    }



    // Check the package under consideration is imported
    final List importedPackages = source.getImportedPackages();
    if (importedPackages != null
        && importedPackages.contains(type.getPackage())) {
      typeWasImported = true; // implicitly

      for (int p = 0, pMax = importedPackages.size(); p < pMax; ++p) {
        final BinPackage pkg = (BinPackage) importedPackages.get(p);

        final BinTypeRef existingType = pkg.findTypeForShortName(newName);
        if (existingType != null) {
          ManagingIndexer supervisor = new ManagingIndexer();
          new TypeIndexer(supervisor, existingType.getBinCIType(),
              new BinClassSearchFilter(true, false, false, false, false, false,
              true, false, false, false, false));
          supervisor.visit(source);
          if (supervisor.getInvocations().size() == 0) {
            // type was implicitly imported and not really used -
            // compiler won't complain
            continue;
          }

          List importedPackageNodes = source.getImportedPackageNodes();
          if (importedPackageNodes != null && p < importedPackageNodes.size()) {
            ASTImpl pkgNode = (ASTImpl) importedPackageNodes.get(p);
            BinSelection clickable = generateClickableImport(source, pkgNode);
            CollectionUtil.addNew(importConflicts, clickable);
          } else {
            new Exception("no nodes for package import in "
                + source).printStackTrace(System.err);
            CollectionUtil.addNew(importConflicts, pkg);
          }

          // Do not bother to check the other packages in this source
          // file
          // or there had to be conflict already before running rename
          break;
        }
      }
    }

    if (!typeWasImported) {
      if (importConflicts.size() > 0) {
        return new RefactoringStatus(
            "Import conflicts or ambiguous imports in other sources",
            importConflicts, RefactoringStatus.ERROR);
      } else {
        return null;
      }
    }

    if (importedTypeNames != null) {
      List importedTypeNameNodes = source.getImportedTypeNameNodes();
      for (int p = 0, pMax = importedTypeNames.size(); p < pMax; ++p) {
        final String fqn = (String) importedTypeNames.get(p);

        if (fqn.equals(newName) || fqn.endsWith('.' + newName)) {
          if (importedTypeNameNodes != null && p < importedTypeNameNodes.size()) {
            ASTImpl typeNode = (ASTImpl) importedTypeNameNodes.get(p);
            BinSelection clickable = generateClickableImport(source, typeNode);
            CollectionUtil.addNew(importConflicts, clickable);
          } else {
            new Exception("no nodes for type import in "
                + source).printStackTrace(System.err);
            CollectionUtil.addNew(importConflicts, fqn);
          }
        }
      }
    }

    if ((type.isInnerType() && (type.isStatic()))) {
    	StaticImports staticImports = source.getStaticImports();
    	if (staticImports != null) {
    		List singleStaticImports = staticImports.getSingleImports();
    		if (singleStaticImports != null) {
    			for (Iterator iter = singleStaticImports.iterator(); iter.hasNext();) {
    				StaticImports.SingleStaticImport singleStatic = (StaticImports.SingleStaticImport) iter.next();
    				if (singleStatic.getQualifiedName().endsWith("." + newName)) {
              BinSelection clickable = generateClickableImport(source, singleStatic.getMemberNameNode());
              CollectionUtil.addNew(importConflicts, clickable);
    				}
    			}
    		}
    	}
    }


    List independentTypes = source.getIndependentDefinedTypes();
    for (int p = 0, pMax = independentTypes.size(); p < pMax; ++p) {
      final BinTypeRef typeRef = ((BinTypeRef) independentTypes.get(p));
      if (typeRef.getName().equals(newName)) {
        CollectionUtil.addNew(importConflicts, typeRef.getBinType());
      }
    }

    if (importConflicts.size() > 0) {
      return new RefactoringStatus(
          "Import conflicts or ambiguous imports in other sources",
          importConflicts, RefactoringStatus.ERROR);
    }

    return null;
  }

  public static BinSelection generateClickableImport(final CompilationUnit source,
      ASTImpl pkgNode) {
    pkgNode = CompoundASTImpl.compoundImportAST(pkgNode);
    BinSelection clickable = new BinSelection(pkgNode.getText(),
        pkgNode.getStartLine(), pkgNode.getStartColumn(), pkgNode.getEndLine(),
        pkgNode.getEndColumn());
    clickable.setCompilationUnit(source);

    return clickable;
  }

  protected List getNonJavaOccurrences() {
    if (renameInNonJavaFiles) {
      Project project = getContext().getProject();

      String qualifiedName = ((BinType) getItem()).getQualifiedName();
      ManagingNonJavaIndexer supervisor = new ManagingNonJavaIndexer(
          project.getOptions().getNonJavaFilesPatterns());

      QualifiedNameIndexer indexer = new QualifiedNameIndexer(
          supervisor, qualifiedName, 
          QualifiedNameIndexer.SLASH_AND_BACKSLASH_PATH);

      supervisor.visit(project);
      List results = supervisor.getOccurrences();

      return results;
    } else {
      return Collections.EMPTY_LIST;
    }
  }



  public ItemUsages getSemanticRenameItemUsages() {
      Project project = getContext().getProject();
      final BinCIType type = (BinCIType)getItem();

      List allUsages = new ArrayList();
      List nonCheckedUsages = new ArrayList();
      //List

      DelegatingVisitor sup = new DelegatingVisitor(true);

        final String[] typeName = new PhraseSplitter(type.getName()).getAllWords();
        DependenciesFinder.Rule rule = new DependenciesFinder.Rule() {
          public boolean isOkFor(Object o) {
            if (o instanceof BinMember) {
              String name = ((BinMember) o).getName();
              int[][] indexes = StringUtil.indexesOfSubPhrase(
                  new PhraseSplitter(name).getAllWords(), typeName);
              return indexes.length > 0;
            }
            return false;
          }
        };


        DependenciesFinder finder = new DependenciesFinder(sup, rule);
        sup.visit(project);


        WeightedGraph graph = finder.getDependenciesGraph();


        WeightedGraph.Entry[] entries = graph.getDependenciesFor(getItem());
        for(int i = 0; i < entries.length; i++) {
          if(rule.isOkFor(entries[i].getObject()) && entries[i].getObject() instanceof BinMember) {
            BinMember member = (BinMember)entries[i].getObject();
            //Exclude constructors of the class being renamed
            if (!((member instanceof BinConstructor) && member.getOwner()
            .getBinCIType() == getItem())){
              Object where = (member instanceof BinCIType)
                                ? member
                                : member.getOwner().getBinCIType();
              
              if((member instanceof BinCIType) && !((BinCIType)member).isFromCompilationUnit()) {
                // skip BinCITypes that are not from the compilation units, but
                // are from the JAR libraries.
                continue;
              }
                
              ASTImpl whereAst = member.getNameAstOrNull();
              InvocationData data = new InvocationData(member, where, whereAst);
              allUsages.add(data);
  
  
              if(entries[i].getLength() == WeightedGraph.INFINITY ||
                  entries[i].getLength() == 0) {
                nonCheckedUsages.add(data);
              }
            }
          }
        }

        return new ItemUsages(allUsages, nonCheckedUsages);
  }
  

  public void setRenameInNonJavaFiles(boolean renameInNonJavaFiles) {
    this.renameInNonJavaFiles = renameInNonJavaFiles;
  }

  public void setSemanticRename(boolean b) {
    this.semanticRename = b;
  }

  public boolean isSemanticRename() {
    return this.semanticRename;
  }

  /**
   * Ok - what I understand is - this class walks the project tree And will
   * check if any field, variable or parameter name is the same as the new
   * classname + the class is accessible in this package
   */
  private static class IdentifierChecker extends AbstractIndexer {

    // The new name for BinType
    private String name = null;

    // The BinType we are trying to rename
    private BinCIType type = null;

    private List conflicts = new ArrayList();

    private IdentifierChecker(BinCIType type, String name) {
      this.name = name;
      this.type = type;
    }

    public void visit(BinField field) {
      if (this.name.equals(field.getName())) {
        addConflict(field);
      }

      super.visit(field);
    }

    public void visit(BinLocalVariable local) {
      if (this.name.equals(local.getName())) {
        addConflict(local);
      }

      super.visit(local);
    }

    public void visit(BinFieldInvocationExpression expression) {
      if (this.name.equals(expression.getField().getName())) {
        addConflict(expression.getField());
      }

      super.visit(expression);
    }

    public List getConflicts() {
      return this.conflicts;
    }

    private void addConflict(BinMember conflictWith) {

      // Check if the the type we are renaming is actualy accessible here
      // (is either in imported package or in the same package)

      BinCIType context;
      if (conflictWith instanceof BinCIType) {
        context = (BinCIType) conflictWith;
      } else {
        context = conflictWith.getOwner().getBinCIType();
      }

      if (type.isAccessible(context)
          && !ImportUtils.needsTypeImported(context.getCompilationUnit(), type,
          type.getPackage())) {
        CollectionUtil.addNew(conflicts, conflictWith);
      }
    }
  }


  protected void invalidateCache() {
    this.supervisor = null;
  }

  public String getDescription() {
    return super.getDescription();
  }

  public String getKey() {
    return key;
  }

  public void setAdditionalItems(List list) {
    additionalItems = (BinMember[])list.toArray(new BinMember[list.size()]);
  }

  public BinMember[] getAdditionalItems() {
    if(additionalItems == null) {
      additionalItems = new BinMember[] {};
    }

    return additionalItems;
  }
}
