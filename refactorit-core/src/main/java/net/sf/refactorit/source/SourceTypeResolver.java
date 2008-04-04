/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.source;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;



/**
 * Defines class for resolving name in the given context.
 */
final class SourceTypeResolver extends Resolver {

  /**
   * FIXME: If there is Inner defined in Owner itself,
   * then it will on same level of visibility as Inners in superclass
   */
  SourceTypeResolver(BinTypeRef aType, CompilationUnit compilationUnit) {
    super(aType);
    this.compilationUnit = compilationUnit;
  }

  /**
   * Gets extra location information. Used to provide more detailed error
   * messages.
   *
   * @return extra location information or <code>null</code> no information
   * available.
   */
  String getLocationInfo() {
    return compilationUnit.getDisplayPath();
  }

  //============================================================================
  /**
   * Resolves type name in given context, also used for resolving interfaces.
   *
   * @see #getInnerTypeRefForName
   */
  public BinTypeRef resolve(String name) throws SourceParsingException,
      LocationlessSourceParsingException {
    BinTypeRef retVal = null;

    // try to resolve name as inner
    // NOTE: in inners lookup it can fall with e.g. cyclic inheritance,
    // but let's try to recover at least something - it can be just an import!
    try {
      retVal = getAllInnerForName(name);
    } catch (SourceParsingException e) {
      getProject().getProjectLoader().getErrorCollector().addUserFriendlyError(e.getUserFriendlyError());
    }
    if (retVal != null) {
      return retVal;
    }

    // try to resolve name as import
    try {
      retVal = compilationUnit.getImportResolver().resolve(name);
    } catch (LocationlessSourceParsingException e) {
      AppRegistry.getExceptionLogger().warn(e, "Failed to resolve: " + name);

      // let's let it continue with resolving and leave that broken import alone
      (compilationUnit.getProject().getProjectLoader().getErrorCollector())
          .addNonCriticalUserFriendlyError(new UserFriendlyError(
              e.getMessage(), e.getCompilationUnit(), type.getBinCIType()
                  .getOffsetNode()));
//      throw e;
    }
    if (retVal != null) {
      return retVal;
    }

    // try to resolve as full qualified name
    retVal = getProject().getTypeRefForName(name);
    if (retVal != null) {
      return retVal;
    }

    // try to resolve as full qualified name for inner class
    // (eg. java.text.AttributedCharacterIterator.Attribute)
    retVal = getTypeRefForQualifiedNameInSource(name);
    if (retVal != null) {
      return retVal;
    }

    // FIXME: What is the case when code below is needed for [Sander]?
    int pos = name.lastIndexOf('.');
    if (pos != -1) {
      String tmpName = name.substring(0, pos);
      BinTypeRef tmpType = resolve(tmpName);
      if (tmpType == null || tmpType.isPrimitiveType()) {
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
        return resolver.resolve(remainingName);
      }
    }

    return null;
  }

  /**
   * Resolves superclass for given context.
   *
   * @see #getOwnerInnerForName
   */
  BinTypeRef resolveAsSuperclass(String name) throws SourceParsingException,
      LocationlessSourceParsingException {

    BinTypeRef retVal = null;

    // try to resolve name as inner
    retVal = getOwnerInnerForName(name);
    if (retVal != null) {
      return retVal;
    }

    // try to resolve name as import
    retVal = compilationUnit.getImportResolver().resolve(name);
    if (retVal != null) {
      return retVal;
    }

    // try to resolve as full qualified name
    retVal = getProject().getTypeRefForName(name);
    if (retVal != null) {
      return retVal;
    }

    // try to resolve as full qualified name for inner class
    // (eg. java.text.AttributedCharacterIterator.Attribute)
    retVal = getTypeRefForQualifiedNameInSource(name);
    if (retVal != null) {
      return retVal;
    }

    return null;
  }

  //==========================================================================

  /**
   * Searches inner if it has a qualified name (ie package.inner.inner).
   */
  private BinTypeRef getTypeRefForQualifiedNameInSource(String qualifiedName) throws
      SourceParsingException {
    return getProject().getTypeRefForSourceName(qualifiedName);
  }

  private final CompilationUnit compilationUnit;
}
