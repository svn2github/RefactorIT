/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.source;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class holds all imports for source file & resolves type names what are
 * imported or are in current package.
 * <BR>
 * N.B! It shares it's collections with source files!
 */
public final class ImportResolver {

  // TODO: Maybe locate line and column for all the SourceParsingExceptions?

  /**
   * This class holds all imports for source file & resolves type names what are
   * imported or are in current package.
   *
   * @param importedTypes all direct imports for source file
   *        (eg. java.util.Date) (String list)
   * @param importedPackages all package imports for source file
   *        (eg. java.util.*) (BinPackage list)
   * @param aPackage current package.
   */
  public ImportResolver(CompilationUnit compilationUnit,
      List importedTypes,
      List importedPackages,
      BinPackage aPackage,
      Project project) throws SourceParsingException {

    this.compilationUnit = compilationUnit;
    this.importedPackages = importedPackages;
    this.importedTypes = importedTypes;
//    this.project = project;
    this.myPackage = aPackage;
  }

  public void setImportedInners(List importedInners) {
    this.importedInners = importedInners;
  }

  /**
   * Resolves only type names what are imported or are in current package.
   * Note: it is unable to resolve qualified names
   * @param name - type name to resolve
   *
   */
  public BinTypeRef resolve(String name) throws
      LocationlessSourceParsingException, SourceParsingException {

    BinTypeRef cached = (BinTypeRef) resolveCache.get(name);
    if (cached != null) {
      return cached;
    }

    cached = resolveImpl(name);
    if (cached != null) {
      resolveCache.put(name, cached);
    }

    return cached;
  }

  /**
   * FIXME: no access rules checking
   */
  private BinTypeRef resolveImpl(String name) throws
      LocationlessSourceParsingException, SourceParsingException {

    int pos = name.indexOf('.');
    if (pos != -1) {
      String tmpName = name.substring(0, pos);
      BinTypeRef tmpType = resolve(tmpName);
      if (tmpType == null) {
        return null;
      }
      String remainingName = name.substring(pos + 1);
      Resolver resolver = tmpType.getResolver();
      if (resolver == null) {
        String mess = "No resolver for type: " + tmpType.getQualifiedName()
            + ", extracted from:" + name;
        AppRegistry.getLogger(this.getClass()).warn(mess);
        if (Assert.enabled) {
          Assert.must(false, mess);
        }
      } else {
        return resolver.getInnerTypeRefForName(remainingName);
      }
    }

    BinTypeRef result = getImportedTypesByShortname(name); // direct type
    // import
    if (result != null) {
      return result;
    }

    result = getSingleStaticImportByShortname(name); //  single static import of static type
    // import
    if (result != null) {
      return result;
    }

    result = myPackage.findTypeForShortName(name); // is type in current
    // package
    if (result != null) {
      return result;
    }

    Set candidates = new HashSet(); // multiple _different_ types for a name means ambiguity
    BinTypeRef tmpResult;

    if (importedPackages != null) {
      for (int i = 0; i < importedPackages.size(); ++i) { // look in all imported packages
        BinPackage aPackage = (BinPackage) importedPackages.get(i);

        tmpResult = aPackage.findTypeForShortName(name);

        if ((tmpResult != null)
            && ((tmpResult.getBinType().isPublic())
            || (tmpResult.getBinType().isProtected()))) {

          candidates.add(tmpResult);
          result = tmpResult;
        }
      }
    }

    if (importedInners != null) {
      for (int i = 0; i < importedInners.size(); ++i) {
        BinTypeRef container = (BinTypeRef) importedInners.get(i);
        tmpResult = container.getBinCIType().getDeclaredType(name);
        // FIXME: inadequate access checking - should we lose access checking alltogether?
        // just add a check later sayng - class X is protected and not accessible in ...
        if (tmpResult != null) {
          candidates.add(tmpResult);
          result = tmpResult;
        }
      }
    }


    if (candidates.size() > 1) {
      StringBuffer conflictList = new StringBuffer(80);
      boolean firstTime = true;
      for (java.util.Iterator i = candidates.iterator(); i.hasNext(); ) {
        if (!firstTime) {
          conflictList.append(",");
        } else {
          firstTime = false;
        }

        conflictList.append(((BinTypeRef) i.next()).getQualifiedName());
      }

      throw new LocationlessSourceParsingException(
          "Ambigous reference: type " + name + " is defined in: "
          + conflictList,
          compilationUnit
          );
    }

    //finally check static-imports-on-demand
    if (result == null) {
    	result = getOnDemandStaticImportByShortname(name);
    }

    return result;
  }



  private void ensureImportedTypesByShortname() throws
	LocationlessSourceParsingException {
  	if (importedTypesByShortname == null) {
      List exceptions = new ArrayList(1);
      Map retVal = new HashMap();
      if (importedTypes != null) {
        for (int i = 0; i < importedTypes.size(); ++i) {
          String qualifiedName = (String) importedTypes.get(i);
          BinTypeRef tmpType = getTypeRefForDirectImport(qualifiedName);

          // try maybe it's import from current package
          if (tmpType == null) {
            String samePackageName = this.myPackage.getQualifiedForShortname(
                qualifiedName);
            //System.err.println("Trying with " + samePackageName);
            tmpType = getTypeRefForDirectImport(samePackageName);
          }

          if (tmpType == null) {
            exceptions.add(new LocationlessSourceParsingException(
                qualifiedName + " not found in import",
                compilationUnit
                ));
            continue;
          }

          if (retVal.get(tmpType.getName()) != null) {
            BinTypeRef conflict = (BinTypeRef) retVal.get(tmpType.getName());
            // This makes legal to type for example
            // import java.io.File; twice
            // ANTLR 2.7.1 was using such code
            if (!conflict.equals(tmpType)) {
              exceptions.add(new LocationlessSourceParsingException(
                  "Ambiguos direct import " + tmpType.getQualifiedName()
                  + " and "
                  + conflict.getQualifiedName(),
                  compilationUnit
                  ));
              continue;
            }
          }
          retVal.put(tmpType.getName(), tmpType);
        }
      }

      importedTypesByShortname = retVal;

      // FIXME: looses other exceptions if many
      if (exceptions.size() > 0) {
        throw (LocationlessSourceParsingException) exceptions.get(0);
      }
    }
  }

  private BinTypeRef getImportedTypesByShortname(String name) throws
      LocationlessSourceParsingException {

    ensureImportedTypesByShortname();
    return (BinTypeRef) importedTypesByShortname.get(name);
  }

  private BinTypeRef getTypeRefForDirectImport(String typeName) {
    String tmpTypeName = typeName;

    BinTypeRef retVal =
        compilationUnit.getProject().getTypeRefForName(tmpTypeName);
    if (retVal != null) {
      return retVal;
    }

    int dotI = tmpTypeName.lastIndexOf('.');
    if (dotI != -1) {
      tmpTypeName = tmpTypeName.substring(0, dotI) + '$'
          + tmpTypeName.substring(dotI + 1);

      return getTypeRefForDirectImport(tmpTypeName);
    }

    return null;
  }

  /**
   * @param name
   * @return
   */
  private BinTypeRef getSingleStaticImportByShortname(String name) {
  	StaticImports staticImports = compilationUnit.getStaticImports();
  	if (staticImports != null) {
  		return staticImports.getSingleImportType(name);
  	}
  	return null;
  }

  /**
   * @param name
   * @return
   */
  private BinTypeRef getOnDemandStaticImportByShortname(String name) {
  	StaticImports staticImports = compilationUnit.getStaticImports();
  	if (staticImports != null) {
  		return staticImports.getOnDemandType(name);
  	}
  	return null;
  }


  private Map importedTypesByShortname;
  private Map staticSingleImportedTypesByShortName;
  private final List importedPackages;
  private final List importedTypes;
  private List importedInners = null;
  private final BinPackage myPackage;
  private final CompilationUnit compilationUnit;
//  private final Project project; // FIXME use project from package
  private final Map resolveCache = new HashMap(20);
}
