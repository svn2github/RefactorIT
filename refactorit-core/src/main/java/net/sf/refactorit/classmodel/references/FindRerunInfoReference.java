/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.structure.FindRequest;
import net.sf.refactorit.refactorings.FindRerunInfo;

/**
 *
 * @author Arseni Grigorjev
 */
public class FindRerunInfoReference extends BinItemReference {
  
  private final boolean includeSubtypes;
  private final int searchType;
  private final BinItemReference objectReference;
  private final BinItemReference searchableTypeReference;
  
  public FindRerunInfoReference(final FindRerunInfo rerun) {
    includeSubtypes = rerun.request.includeSubtypes;
    searchType = rerun.request.searchType;
    objectReference = BinItemReference.create(rerun.object);
    searchableTypeReference = BinItemReference.create(rerun.request
        .searchableType);
  }
  
  public Object findItem(Project project) {
    Object object = objectReference.restore(project);
    BinTypeRef searchableType = (BinTypeRef) searchableTypeReference.restore(
        project);
    FindRequest request = new FindRequest();
    request.includeSubtypes = includeSubtypes;
    request.searchType = searchType;
    request.searchableType = searchableType;
    return new FindRerunInfo(object, request);
  }
  
}
