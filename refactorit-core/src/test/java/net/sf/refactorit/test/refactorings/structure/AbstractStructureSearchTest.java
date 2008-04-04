/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.structure;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.TypeConversionRules;
import net.sf.refactorit.query.structure.AbstractSearch;
import net.sf.refactorit.query.structure.FindRequest;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import java.util.Iterator;
import java.util.List;



/**
 * This is an abstract class for the structure search. It encapsulates common 
 * verifing methods for all refactorings.
 * 
 * @author Sergey Fedulov
 */
public abstract class AbstractStructureSearchTest extends RefactoringTestCase {
  
  public AbstractStructureSearchTest(String name) {
    super(name);
  }

  public abstract String getTemplate();
  
  /**
   * Returns searchType - constant defined in class FindRequest
   */
  abstract int getSearchtype();
  
  /**
   * Checks, whether found Binary member has correct type, and returning its
   * binary type reference.
   */
  abstract BinTypeRef getFoundTypeRef(BinItem foundItem);

  protected void checkMatches(String searchableType, boolean includeSubtypes,
      int expectedCount) throws Exception {
    Project project = getInitialProject();
    project.getProjectLoader().build();
    assertFalse("Project has errors", (project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());

    FindRequest fr = new FindRequest();
    fr.searchType = getSearchtype();
    
    BinArrayType.ArrayType arrayType = 
        BinArrayType.extractArrayTypeFromString(searchableType);
    fr.searchableType = project.findTypeRefForName(arrayType.type);
    if (arrayType.dimensions > 0){
      fr.searchableType = project.createArrayTypeForType(fr.searchableType, 
          arrayType.dimensions);
    }
    fr.includeSubtypes = includeSubtypes;

    BinTypeRef ref = project.findTypeRefForName("Test");
    BinCIType type = ref.getBinCIType();

    AbstractSearch search = fr.createSearch(new NullContext(project), type);
    AbstractSearch.SearchVisitor visitor = search.createVisitor();
    visitor.visit(type);
    
    List results = visitor.getResults();
    assertEquals("wrong number of matches", expectedCount, results.size());
    
    for (Iterator it=results.iterator(); it.hasNext();){
      BinItem foundItem = (BinItem) it.next();
      BinTypeRef foundTypeRef = getFoundTypeRef(foundItem);
      checkFoundType(foundTypeRef, fr.searchableType, includeSubtypes);
    }
  }
  
  
  /**
   * Checks, whether the type of found object is correct.
   */
  protected void checkFoundType(BinTypeRef foundType, BinTypeRef searhableType, 
      boolean includeSubtypes){
    
    if (includeSubtypes){
      if (!TypeConversionRules.isSubtypingConversion(foundType, searhableType)) {
        
        if (searhableType.isArray()){
          //If searching for array, then only arrays of that type should be found
          fail("got an incompatible BinClassRef back " + foundType);
        } else if (!TypeConversionRules.isSubtypingConversion(
            foundType.getNonArrayType(), searhableType)) {
          //If searching for non array type, then arrays and non-arrays
          //could be found
          fail("got an incompatible BinClassRef back " + foundType);
        }
      }
    } else {
      if (!TypeConversionRules.isIdentityConversion(foundType, searhableType)){
        if (searhableType.isArray()){
          //If searching for array, then only arrays of that type should be found
          fail("got an incompatible BinClassRef back " + foundType);
        } else if (!TypeConversionRules.isIdentityConversion(
            foundType.getNonArrayType(), searhableType)) {
          fail("got an incompatible BinClassRef back " + foundType);
        }
      }
    }
    
    //If search by arrays, then should find only arrays
    if (searhableType.isArray()){
      checkFoundArrayType(searhableType, foundType, includeSubtypes);
    }
  }

  
  /**
   * In case of arrays checks, whether the found object type is correct array.
   */
  protected void checkFoundArrayType(BinTypeRef searhableType, BinTypeRef foundTypeRef, 
      boolean includeSubtypes) {
    if (!foundTypeRef.isArray()){
      fail("got non array type " + foundTypeRef);
    }
    
    BinArrayType foundArrayType = (BinArrayType) foundTypeRef.getBinCIType();
    BinArrayType searchableArrayType = (BinArrayType) searhableType.getBinCIType();
    
    if (searhableType.getNonArrayType().getQualifiedName().equals("java.lang.Object") &&
        includeSubtypes){
      //If search was made by array of instances of class Object, 
      //and subtypes are included, then found array dimensions 
      //could be bigger or equals, than searchable type array dimensions
      //For example, "String[][][]" is subtype of "Object[][]"
      if (foundArrayType.getDimensions() < searchableArrayType.getDimensions()){
        fail("got an incorrect array dimension " + foundArrayType.getDimensions());
      }
    } else {
      //In this case array dimensions should be the same
      if (foundArrayType.getDimensions() != searchableArrayType.getDimensions()){
        fail("got an incorrect array dimension " + foundArrayType.getDimensions());
      }
    }
  }
}
