/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.structure;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.TypeConversionRules;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Encapsulates common logic for all structure searches
 *
 * @author Sergey Fedulov
 */
public abstract class AbstractSearch {
  private static final Logger log = AppRegistry.getLogger(AbstractSearch.class);

  FindRequest findRequest;
  BinItem visitables[];

  AbstractSearch(RefactorItContext context, Object object, FindRequest findRequest){
    this.findRequest = findRequest;
    visitables = AbstractSearch.getVisitables(context, object);
  }

  //This
  public abstract SearchVisitor createVisitor();
  abstract String getSearchable();


  /**
   * Makes search, and creates a binary table with results.
   * @return root node of the binary table
   */
  public BinTreeTableNode doSearch(){
    SearchVisitor searchVisitor = createVisitor();

    for (int i = 0; i < visitables.length; ++i) {
      visitables[i].accept(searchVisitor);
    }

    BinTreeTableNode rootNode = new BinTreeTableNode(getTableCaption());
    List results = searchVisitor.getResults();

    addResultsToTable(results, rootNode);

    return rootNode;
  }

  /**
   * Adds results from list into the binary table
   */
  protected void addResultsToTable(List results, BinTreeTableNode rootNode){
    for (int i = 0; i < results.size(); ++i) {
      BinMember binMember = (BinMember) results.get(i);
      ParentTreeTableNode parent = rootNode.findParent(binMember.getOwner(), true);
      parent.addChild(new BinTreeTableNode(binMember));
    }
  }

  private String getTableCaption(){
    StringBuffer caption = new StringBuffer("Search results for ");
    caption.append(getSearchable());
    caption.append(" ");
    caption.append(findRequest.searchableType.getQualifiedName());
		if (findRequest.includeSubtypes){
		  caption.append(" and subtypes");
		}

		return caption.toString();
  }

  /**
   * REUSE: This can be reused I'm sure
   */
  private static BinItem[] getVisitables(RefactorItContext context, Object target) {
    if (target instanceof Object[]) {
      Object targets[] = (Object[]) target;
      BinItem[][] results = new BinItem[targets.length][];
      int length = 0;
      for (int i = 0; i < targets.length; ++i) {
        results[i] = getVisitables(context, targets[i]);
        length += results[i].length;
      }

      BinItem[] finalResult = new BinItem[length];
      int curPos = 0;
      for (int i = 0; i < targets.length; ++i) {
        System.arraycopy(results[i], 0, finalResult, curPos, results[i].length);
        curPos += results[i].length;
      }
      return finalResult;
    }
    if (target instanceof BinCIType) {
      return new BinItem[] {(BinCIType) target};
    } else if (target instanceof Project) {
      List definedTypes = context.getProject().getDefinedTypes();
      ArrayList result = new ArrayList();
      for (int i = 0; i < definedTypes.size(); ++i) {
        result.add(((BinTypeRef) definedTypes.get(i)).getBinCIType());
      }
      return (BinCIType[]) result.toArray(new BinCIType[0]);
    } else if (target instanceof BinPackage) {
      BinPackage aPackage = (BinPackage) target;
      ArrayList result = new ArrayList();
      BinPackage packageList[] = context.getProject().getAllPackages();
      for (int i = 0; i < packageList.length; ++i) {
        if (packageList[i].hasTypesWithSources()
            && packageList[i].getQualifiedName().startsWith(aPackage.
            getQualifiedName())) {
          for (Iterator t = packageList[i].getAllTypes(); t.hasNext(); ) {
            result.add(((BinTypeRef) t.next()).getBinType());
          }
        }
      }

      return (BinType[]) result.toArray(new BinType[0]);
    }

    Assert.must(false, "Invalid target " + target + " for FindAction");
    return null;
  }


  /**
   * This class encapsulates common logic of search visitor
   */
  public abstract class SearchVisitor extends BinItemVisitor {
    protected final List results;

    SearchVisitor() {
      super(true);
      results = new ArrayList();
    }

    public List getResults() {
      return results;
    }

    boolean isMatching(BinTypeRef testable){
      if (findRequest.includeSubtypes && findRequest.searchableType.isReferenceType()) {
        return checkSubtyping(testable);
      }

      return checkIdentity(testable);
    }

    private boolean checkIdentity(BinTypeRef testable){
      BinTypeRef toCheck = testable;
      if (!findRequest.searchableType.isArray()){
        toCheck = testable.getNonArrayType();
      }

      return TypeConversionRules.isIdentityConversion(toCheck,
          findRequest.searchableType);
    }

    private boolean checkSubtyping(BinTypeRef testable){
      if (TypeConversionRules.isSubtypingConversion(testable,
          findRequest.searchableType)){
        return true;
      }

      if (!findRequest.searchableType.isArray()){
        return TypeConversionRules.isSubtypingConversion(testable.getNonArrayType(),
            findRequest.searchableType);
      }

      return false;
    }
  }
}
