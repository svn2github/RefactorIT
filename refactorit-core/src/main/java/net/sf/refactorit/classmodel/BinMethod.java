/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.BinMethodOrConstructorReference;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.ejb.RitEjbModule;
import net.sf.refactorit.loader.CancelSupport;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.LoadingASTUtil;
import net.sf.refactorit.loader.ProjectLoader;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.BodyContext;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinMethodFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.PositionsForNewItems;

import org.apache.log4j.Logger;

import rantlr.collections.AST;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Class or interface method
 */
public class BinMethod extends BinMember
    implements BinTypeRefManager, Scope, BinTypeParameterManager {
  private static final Logger log = Logger.getLogger(BinMember.class);

  public static final BinMethod[] NO_METHODS = new BinMethod[0];

  public BinMethod(final String name, final BinParameter[] params,
      final BinTypeRef returnType,
      final int modifiers, final Throws[] throwses, final boolean isWithoutBody) {

    this(name, params, returnType, modifiers, throwses);
    if (isWithoutBody) {
      setBody(null);
    }
  }

  public BinMethod(final String name, final BinParameter[] params,
      final BinTypeRef returnType,
      final int modifiers, final Throws[] throwses) {
    // owner is null and will be set when it is attached to a type
    super(name, modifiers, null);

    this.returnType = returnType;
    setParams(params);
    setThrows(throwses);

    if (ProjectLoader.checkIntegrityAfterLoad) {
      ProjectLoader.registerCreatedItem(this);
    }
  }

  private void setThrows(final Throws[] throwses) {
    this.throwses = throwses;

    // set parents right away, somebody may need it before body loading
    for (int i = 0; i < throwses.length; i++) {
      throwses[i].setParent(this);
    }
  }

  public final void cleanForPrototype() {
    returnType = null;
    nameAst = -1;
    this.bodyEnsured = false;
//    if (this.body != null) {
//      this.body.clean();  // for debug
      this.body = null;
//    }
//    myScopeRules = null;
//    if (throwses != null) {
//      for (int i = 0; i < throwses.length; i++) {
//        throwses[i].clean(); // for debug
//      }
      throwses = null;
//    }

    super.cleanForPrototype();
  }

  public static BinMethod createByPrototype(final String name,
      final BinParameter[] params,
      final BinTypeRef returnType,
      final int modifiers, final Throws[] throwses,
      final BinTypeRef forType) {
    if (Assert.enabled && forType == null) {
      Assert.must(false, "For type is null for: " + name);
    }

    BinMethod method = forType.getProject().getProjectLoader().getPrototypeManager()
        .findMethod(forType, name, params);

    if (method == null) {
      method = new BinMethod(name, params, returnType, modifiers, throwses);
      method.setOwner(forType);
    } else {
      method.reInit(returnType, modifiers, throwses, params, forType);
    }

    return method;
  }

  protected final void reInit(final BinTypeRef returnType, final int modifiers,
      final Throws[] throwses,
      final BinParameter[] params, final BinTypeRef owner) {
    setThrows(throwses);
    setModifiers(modifiers);
    this.returnType = returnType;
    setParams(params);
    setOwner(owner);
  }

  private void setParams(BinParameter[] params) {
    this.params = params;

    for (int i = 0, max = params.length; i < max; i++) {
      params[i].setParent(this);
    }
  }

//  public final void setOwner(final BinTypeRef owner) {
//    super.setOwner(owner);
//  }

  public BinTypeRef getReturnType() {
    return this.returnType;
  }

  public final ASTImpl getReturnTypeAST() {
    return (ASTImpl) ASTUtil.getFirstChildOfType(getOffsetNode(),
        JavaTokenTypes.TYPE).getFirstChild();
  }

  public final BinParameter[] getParameters() {
    if (this.params == null) {
      this.params = BinParameter.NO_PARAMS;
    }
    return this.params;
  }

  public final Throws[] getThrows() {
    if (throwses == null) { // was cleaned probably, actually a severe bug somewhere
      AppRegistry.getLogger(this.getClass()).error(
          "Method has null throws: " + this);
      throwses = Throws.NO_THROWS;
    }
    return throwses;
  }

  public final SourceCoordinate findNewStatementPositionAtEnd() {
    return PositionsForNewItems.findNewStatementPositionAtEnd(this);
  }

  /**
   * Can be also used to set body to explicitly null
   * This is done for methods from .class file and synthetic methods
   */
  public final void setBody(final BinStatementList body) {
    this.body = body;
    this.bodyEnsured = true;
  }

  public final String getQualifiedNameWithParamTypes() {
    final StringBuffer result = new StringBuffer(128);
    final String separator = ", ";

    result.append(getQualifiedName()).append('(');

    for (int i = 0; i < this.params.length; i++) {
      if (i > 0) {
        result.append(separator);
      }
      result.append(this.params[i].getTypeRef().getQualifiedName());
    }

    result.append(')');

    return result.toString();
  }

  public BinStatementList getBodyWithoutEnsure() {
    if (this.body instanceof Reference) {
      BinStatementList actualBody
          = (BinStatementList) ((Reference) this.body).get();
      return actualBody;
    } else {
      return (BinStatementList) this.body;
    }
  }

  public synchronized final BinStatementList getBody() {
    try {
      if (getOwner() != null && !getOwner().getBinCIType().isLocal()) {
        CancelSupport.checkThreadInterrupted();
      }

      if (this.bodyEnsured) {
        if (this.body instanceof Reference) {
          BinStatementList actualBody
              = (BinStatementList) ((Reference) this.body).get();
          if (actualBody == null) {
//System.err.println("no body for: " + this.getQualifiedName());
            this.bodyEnsured = false;
          } else {
            return actualBody;
          }
        } else {
          return (BinStatementList) this.body;
        }
      }

      if (!hasCoordinates()) { // nothing to try to load anyway
        return (BinStatementList) this.body;
      }

      BinStatementList actualBody = null;
      try {
        if (this.isAbstract() || isNative()) {
          if (LoadingASTUtil.getStatementNode((ASTImpl) getOffsetNode()
              .getFirstChild()) != null) {
            SourceParsingException.throwWithUserFriendlyError(
                "Abstract and native methods can't have a body",
                this.getCompilationUnit(), this.getOffsetNode());
          }
        } else {
          BodyContext context = new BodyContext(
              getOwner().getBinCIType().getCompilationUnit() /*, this*/);

          getProject().getProjectLoader().getErrorCollector().startRecoverableUserErrorSection();
          context.startType(getOwner());
          context.addTypeRefs(getTypeParameters());
          try {
            actualBody = getProject().getProjectLoader().getMethodBodyLoader()
                .buildMethodBody(this, context);
          } finally {
            context.endType();
            getProject().getProjectLoader().getErrorCollector().endRecoverableUserErrorSection();
          }
          if (!context.isHasLocalTypes() && actualBody != null) {
//System.err.println("made body reference: " + this.getQualifiedName());
            if (ProjectLoader.isLowMemoryMode()) {
              this.body = new WeakReference(actualBody);
            } else {
              this.body = new SoftReference(actualBody);
            }
          }
        }
      } catch (SourceParsingException e) {
        if (!e.isUserFriendlyErrorReported()) {
          getProject().getProjectLoader().getErrorCollector().addUserFriendlyError(new UserFriendlyError(
                    e.getMessage(), getCompilationUnit(), getOffsetNode()));
        }
      } finally {
        this.bodyEnsured = true;
      }

      return actualBody;
    } catch (RuntimeException e) {
      log.warn("Parsing method " + getQualifiedName()
          + " body crashed with:", e);

      throw e;
    }
  }

  public final boolean hasBody() {
    return getBody() != null;
  }

  public final boolean isMain() {
    if ("main".equals(getName()) && isPublic() && isStatic()
        && (getReturnType().getBinType() == BinPrimitiveType.VOID)) {
      final BinParameter[] params = getParameters();

      return (params.length == 1)
          && params[0].getTypeRef().isArray()
          && "[Ljava.lang.String;".equals(
          params[0].getTypeRef().getQualifiedName());
    }

    return false;
  }

  public final boolean isToString() {
    return "toString".equals(getName())
        && isPublic()
        && (getParameters().length == 0)
        && getReturnType().isString();
  }

  private boolean bodyEnsured = false;

  public final boolean isBodyEnsured() {
    return this.bodyEnsured;
  }

  /**
   * Checks whether <code>other</code> method can be invoked instead of
   * <code>this</code> method.
   * Based on JLS 15.12.2.1 Find Methods that are Applicable and Accessible.
   * <p>
   * Notice: thus, <code>other</code> method is <em>wider</em> than
   * <code>this</code> method - <code>this</code> method parameters
   * can be casted to parameters of <code>other</code> method.
   *
   * @param other other method.
   *
   * @return <code>true</code> if and only if <code>other</code> method can be
   *         invoked instead of <code>this</code> method;
   *         <code>false</code> otherwise.
   */
  public final boolean isApplicable(final BinMethod other) {
    // The signature of a method consists of the name of the method and the
    // number and types of formal parameters to the method.

    if (this == other) {
      return true; // Shortcut
    }

    if (!this.getName().equals(other.getName())) {
      return false; // Names don't match
    }

    final BinTypeRef[] paramTypes
        = BinParameter.parameterTypes(this.getParameters());

    return MethodInvocationRules.isApplicable(other, paramTypes);
  }

  public final Project getProject() {
    return getOwner().getProject();
  }

  public void accept(final net.sf.refactorit.query.BinItemVisitor visitor) {
    if (visitor.isSkipSynthetic() && this.isSynthetic()) {
      return;
    }
    visitor.visit(this);
  }

  public void defaultTraverse(final BinItemVisitor visitor) {

    BinExpressionList annotations = getAnnotations();
    if (annotations != null) {
      annotations.accept(visitor);
    }

    // we need it first to get parents for parameters and throws
    final BinStatement aBodyStatement = getBody();

    BinTypeRef[] typeParams = getTypeParameters();
    if (typeParams != null) {
      for (int i = 0; i < typeParams.length; i++) {
        typeParams[i].getBinCIType().accept(visitor);
      }
    }

    final BinParameter[] curParams = getParameters();
    if (curParams != null) {
      for (int i = 0; i < curParams.length; i++) {
        curParams[i].accept(visitor);
      }
    }

    final Throws[] curThrows = getThrows();
    if (curThrows != null) {
      for (int i = 0; i < curThrows.length; i++) {
        curThrows[i].accept(visitor);
      }
    }

    if (aBodyStatement != null) {
      aBodyStatement.accept(visitor);
    }
  }

  public final List getLocalTypes() {

    final class Visitor extends AbstractIndexer {
      public final void visit(BinCIType type) {
        locals.add(type);
      }

      final List locals = new ArrayList();
    }


    Visitor finder = new Visitor();
    this.accept(finder);
    return finder.locals;
  }

  public void accept(BinTypeRefVisitor visitor) {
    if (this.returnType != null) {
      this.returnType.accept(visitor);
    }
  }

  public final BinTypeRef[] getTypeParameters() {
    if (this.typeParameters == null) {
      this.typeParameters = BinTypeRef.NO_TYPEREFS;
    }
    return this.typeParameters;
  }

  public final BinTypeRef getTypeParameter(final String name) {
    for (int i = 0,
        max = this.typeParameters == null ? 0 : this.typeParameters.length;
        i < max; i++) {
      if (name.equals(this.typeParameters[i].getName())) {
        return this.typeParameters[i];
      }
    }

    return null;
  }

  public final void setTypeParameters(final BinTypeRef[] typeParameters) {
    this.typeParameters = typeParameters;
    for (int i = 0,
        max = this.typeParameters == null ? 0 : this.typeParameters.length;
        i < max; i++) {
      this.typeParameters[i].getBinType().setParent(this);
    }
  }

  /**
   * Checks whether signature of this method and other method match. Based on
   * JLS 8.4.2 Method Signature.<br>
   * Signature comparing is one part of overriding checking,
   * another is inheritance.
   *
   * @param other other method.
   *
   * @return <code>true</code> if and only if signature of this method and the
   *         other method are same; <code>false</code> otherwise.
   */
  public final boolean sameSignature(final BinMethod other) {
    // The signature of a method consists of the name of the method and the
    // number and types of formal parameters to the method.

    if (this == other) {
      return true; // Shortcut
    }

    if (!getName().equals(other.getName())) {
      return false; // Names don't match
    }

    final BinParameter[] otherParams = other.getParameters();

    if (params.length != otherParams.length) {
      return false; // Different number of parameters
    }

    for (int i = 0, max = params.length; i < max; i++) {
      final BinTypeRef thisParameterType = params[i].getTypeRef();
      final BinTypeRef otherParameterType = otherParams[i].getTypeRef();

      if (!(thisParameterType == otherParameterType
          || thisParameterType.equals(otherParameterType)
          // FIXME: a hack to avoid resolving runtime types of type parameters
          || TypeConversionRules.isDerivedFromAll(thisParameterType, otherParameterType))
      ) {
        return false; // Parameter types don't match.
      }
    }

    return true;
  }

  /**
   * Finds methods that this method overrides. Note that according to JLS
   * method can override several methods.<br>
   * Example: A.method(), B.method(), C.method, D.method, A -> B -> C, D -> C.<br>
   * For C.method() overriden are B.method() and D.method(). And A.method()
   * is not, since it's overriden by B.method().
   *
   * @return list of methods in some super type which this method overrides;
   *         never returns <code>null</code>.
   */
  public final List findOverrides() {
    if (isPrivate()) {
      // Private method cannot override any other method.
      return CollectionUtil.EMPTY_ARRAY_LIST;
    }

    if (isStatic()) {
      // Static method cannot override any other method, it can only hide.
      return CollectionUtil.EMPTY_ARRAY_LIST;
    }

    return findOverridenInSupertypes(getOwner());
  }

  // FIXME: convert return type to Set
  public final List findAllOverrides() {
    final List overrides = findOverrides();
    final ArrayList result = new ArrayList(overrides.size());
    result.addAll(overrides);
    for (int i = 0, max = overrides.size(); i < max; i++) {
      CollectionUtil.addAllNew(result,
          ((BinMethod) overrides.get(i)).findAllOverrides());
    }

    return result;
  }

  /**
   * All the methods declared of in the given type this method overrides.
   *
   * @param typeRef type
   *
   * @return list of overriden methods or <code>null</code> if nothing was found
   */
  public List findOverriddenInType(final BinTypeRef typeRef) {
    return findOverrideOrOverridden(typeRef, true);
  }

  public List findOverrideInType(final BinTypeRef typeRef) {
    return findOverrideOrOverridden(typeRef, false);
  }

  /**
   * Searches for current method overrides or overridden in given type
   * depending on 'findOverrides' parameter passed.
   *
   * @param typeRef type
   * @param findOverrides If true, searches for methods in typeRef,
   * that are overridden by current method. If false - searches methods,
   * that override current.
   *
   * @return list of overriden methods or <code>null</code> if nothing was found
   */
  private List findOverrideOrOverridden(final BinTypeRef typeRef, boolean findOverridden) {
    final ArrayList overriden = new ArrayList(1);

    BinMethod[] typeMethods = null;
    try {
      typeMethods = typeRef.getBinCIType().getDeclaredMethods();
      if (typeMethods == null) {
        throw new Exception("Type " + typeRef.getQualifiedName() +
            " has null declared methods, really strange!");
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
      UserFriendlyError e1 = new UserFriendlyError(
            "Find overriden - failed to get declared methods for typeRef: "
            + (typeRef == null ? "null" : typeRef.getQualifiedName())
            + ", type: "
            + ((typeRef == null || typeRef.getBinCIType() == null) ? "null"
            : typeRef.getBinCIType().getName()),
            getCompilationUnit(), getNameAstOrNull());
      getProject().getProjectLoader().getErrorCollector()
          .addNonCriticalUserFriendlyError(e1);
      return overriden;
    }

    for (int i = 0; i < typeMethods.length; i++) {
      final BinMethod typeMethod = typeMethods[i];
      if (typeMethod.isPrivate()) {
        continue;
      }

      if (!sameSignature(typeMethod)) {
        continue;
      }

      BinCIType invokedOn = (findOverridden) ?
          typeMethod.getOwner().getBinCIType():getOwner().getBinCIType();
      BinCIType context = (findOverridden) ?
          getOwner().getBinCIType():typeMethod.getOwner().getBinCIType();
      if (!typeMethod.isAccessible(invokedOn, context)) {
        continue;
      }

      overriden.add(typeMethod);
    }
    return overriden;
  }




  /**
   * All the methods of supertypes of the type that this method overrides.
   * Note that it stops on a first found overriden method in the inheritance
   * tree.
   *
   * @param type type.
   *
   * @return all overriden methods or <code>null</code> if no overriden method
   *         found in supertypes of the <code>type</code>.
   */
  private List findOverridenInSupertypes(final BinTypeRef type) {
    final ArrayList overriden = new ArrayList(1);

    // No overriden method found from methods declared in the type provided,
    // so scan its supertype and interfaces.
    final BinTypeRef[] supertypes = type.getSupertypes();
    for (int i = 0, max = supertypes.length; i < max; i++) {
      final BinTypeRef supertype = supertypes[i];

      List overridenInType = findOverriddenInType(supertype);
      if (overridenInType == null || overridenInType.size() == 0) {
        overridenInType = findOverridenInSupertypes(supertype);
      }

      if (overridenInType != null) {
        overriden.addAll(overridenInType);
      }
    }

    return overriden;
  }

  /**
   * @return methods in supertypes where such method signature appears for the
   * first time; returns empty list, when this method itself is a top method
   */
  public final List getTopMethods() {
    final List result = new ArrayList(3);

    final List overrides = findOverrides();
    for (int i = 0, max = overrides.size(); i < max; i++) {
      final BinMethod overriden = (BinMethod) overrides.get(i);
      final List overrides2 = overriden.getTopMethods();
      if (overrides2.size() > 0) {
        CollectionUtil.addAllNew(result, overrides2);
      } else {
        CollectionUtil.addNew(result, overriden);
      }
    }

    return result;
  }

  public final BinMethod getTopSuperclassMethod() {
    BinTypeRef owner = getOwner();
    BinMethod result = this;
    while(owner.getSuperclass() != null) {
      owner = owner.getSuperclass();
      List overriddenInType = findOverriddenInType(owner);
      if(overriddenInType.size() > 0 ) {
        result = (BinMethod) overriddenInType.get(0);
      }
    }
    return result;
  }

  /**
   * Gets string representation of this method.
   * Same contract as {@link java.lang.reflect.Method#toString}.
   *
   * @return string representation.
   */
  public final String toString() {
    BinModifierFormatter modifierFormatter = new BinModifierFormatter(getModifiers());
    modifierFormatter.needsPostfix(true);
    final StringBuffer result = new StringBuffer(modifierFormatter.print());

    if (getReturnType() != null) {
      result.append(getReturnType().getQualifiedName());
    } else {
      result.append("<unknown yet>");
    }
    result.append(" ");
    result.append(getQualifiedNameWithParamTypes());
    final Throws[] declaredThrows = this.throwses; // getThrows();
    if (declaredThrows == null) {
      result.append(" throws CLEARED");
    } else {
      if (declaredThrows.length > 0) {
        result.append(" throws ");
        for (int i = 0; i < declaredThrows.length; i++) {
          final BinTypeRef throwableType = declaredThrows[i].getException();
          if (i > 0) {
            result.append(",");
          }
          result.append(throwableType.getQualifiedName());
        }
      }
    }

    //result.append(' ' + Integer.toHexString(hashCode())); // hashCode() makes tests fail differently each time

    return result.toString();
  }

  /**
   * Gets types declared inside this method's body.
   *
   * @return list of types ({@link BinCIType} instances).
   *         Never returns <code>null</code>.
   */
  public final List getDeclaredTypes() {
    final List types = new ArrayList();
    final BinStatementList body = getBody();
    if (body == null) {
      return types;
    }
    final AbstractIndexer indexer = new AbstractIndexer() {
      public void visit(final BinCIType type) {
        types.add(type);
      }
    };
    indexer.visit(body);

    return types;
  }

  /**
   * Gets AST node corresponding to the body of this method.
   * Body is part of declaration enclosed in <code>{</code> and
   * <code>}</code> brackets containing statements implementing this method.
   *
   * @return AST node or <code>null</code> if node is not known
   *         (e.g. if this method is abstract).
   */
  public final ASTImpl getBodyAST() {
    final ASTImpl offsetNode = getOffsetNode();
    if (offsetNode == null) {
      return null;
    }

    ASTImpl node = (ASTImpl) offsetNode.getFirstChild();
    while ((node != null) && (node.getType() != JavaTokenTypes.SLIST)) {
      node = (ASTImpl) node.getNextSibling();
    }

    return node;
  }

  /**
   * A single exception from the throws clause.
   */
  public static final class Throws extends BinSourceConstruct
      implements BinTypeRefManager {

    public static final Throws[] NO_THROWS = new Throws[0];

    public Throws(final BinTypeRef exception) {
      super(exception.getNodeIndex());
      this.exception = exception;
    }

    public final BinTypeRef getException() {
      return this.exception;
    }

    public final void accept(final BinItemVisitor visitor) {
      visitor.visit(this);
    }

    public final void defaultTraverse(final BinItemVisitor visitor) {
    }

    public final void accept(BinTypeRefVisitor visitor) {
      if (this.exception != null) {
        this.exception.accept(visitor);
      }
    }

    public final void clean() {
      this.exception = null;
    }

    private BinTypeRef exception;
  }

  public final void initScope(final HashMap variableMap, final HashMap typeMap) {
//    myScopeRules = new ScopeRules(this, variableMap, typeMap);
  }

//  public final ScopeRules getScopeRules() {
//    return myScopeRules;
//  }

  public final boolean contains(Scope other) {
    if (other instanceof LocationAware) {
      return contains((LocationAware) other);
    } else {
      return false;
    }
  }

  public final void setNameAst(final ASTImpl nameAst) {
    this.nameAst = ASTUtil.indexFor(nameAst);
  }

  /** Gives null if the method isClonedForInterfaceInheritance(), for example */
  public ASTImpl getNameAstOrNull() {
    final CompilationUnit source = getCompilationUnit();
    if (source != null && source.getSource() != null) {
//    try {
      return source.getSource().getASTByIndex(nameAst);
//    } catch (NullPointerException e) {
//System.err.println("source is null for method: " + this);
//    }
    }

    return null;
  }

  /**
   * @return method skeleton, name and interface same
   */
  public BinMethod cloneSkeleton() {
    return new BinMethod(this.getName(), this.getParameters(),
        this.getReturnType(), this.getModifiers(),
        this.getThrows());
  }

  public final BinMethod cloneForInterfaceInheritance(final BinTypeRef forType) {
    final BinParameter[] oldParameters = getParameters();
    final Throws[] oldThrows = getThrows();

    final BinParameter[] newParameters = new BinParameter[oldParameters.length];
    final Throws[] newThrows = new Throws[oldThrows.length];

    for (int i = 0; i < oldParameters.length; ++i) {
      newParameters[i] = new BinParameter(oldParameters[i].getName(),
          oldParameters[i].getTypeRef(), oldParameters[i].getModifiers());
    }

    for (int i = 0; i < oldThrows.length; ++i) {
      newThrows[i] = new Throws(oldThrows[i].getException());
    }

    final BinMethod result = BinMethod.createByPrototype(
        getName(),
        newParameters,
        getReturnType(),
        getModifiers(),
        newThrows,
        forType);

    result.setSynthetic(true);
    result.setParent(forType.getBinCIType());
    BinParentFinder.findParentsFor(result);

    return result;
  }

  public final boolean isSynthetic() {
    return this.synthetic;
  }

  public final void setSynthetic(final boolean synthetic) {
    this.synthetic = synthetic;
    if (this.synthetic) {
      setBody(null);
    }
  }

  public String getMemberType() {
    return memberType;
  }

  public static String getStaticMemberType() {
    return memberType;
  }

  public final BinItemFormatter getFormatter() {
    return new BinMethodFormatter(this);
  }

  public final ASTImpl getRootAst() {
    return getOffsetNode();
  }

  /**
   * @return	true if this member is deprecated
   */
  public final boolean isDeprecated() {
    throw new UnsupportedOperationException("noone used it ever");
//    return deprecated;
  }

  public final void setDeprecated(final boolean setTo) {
//    deprecated = setTo;
  }

  private static final String memberType = "method";

//  private ScopeRules myScopeRules;

  private Object body;
  private BinParameter[] params;
  private BinTypeRef returnType;
  private Throws[] throwses;
  private BinTypeRef[] typeParameters;

  protected int nameAst = -1;

  private boolean synthetic = false;

//  private boolean deprecated;

  private BinExpressionList annotationsList;


  private static void findUpHierarchy(
      BinTypeRef ref, HashSet types, BinMethod method) {
//    Collection supertypes = ref.getAllSupertypes();
    BinTypeRef[] supers = ref.getSupertypes();
    Collection supertypes = new ArrayList(supers.length);
    CollectionUtil.addAll(supertypes, supers);
    if (ref.getBinCIType().isClass()) {
      List ejbRelatedSupertypes = RitEjbModule.getRelatedInterfaces(ref);
      supertypes.addAll(ejbRelatedSupertypes);
    }

//      System.out.println("processing ref "+ref.getQualifiedName());

    String methodName = method.getName();
    BinCIType methodOwner = method.getOwner().getBinCIType();

    for (Iterator sups = supertypes.iterator(); sups.hasNext(); ) {
      BinTypeRef supertype = (BinTypeRef) sups.next();

      BinMethod potentialOverridden[] = supertype.getBinCIType()
          .getAccessibleMethods(methodName, methodOwner);
      for (int j = 0; j < potentialOverridden.length; j++) {
        if (method.sameSignature(potentialOverridden[j])) {
          findUpHierarchy(supertype, types, method);
          types.add(potentialOverridden[j].getOwner());
          break;
        }
      }
    }
  }

  private static List getRecursivelyAllOverridesList(
      BinMethod method, Set analyzedTypes) {

    if (method.isPrivate() || method.isStatic()
        || method instanceof BinConstructor) {
//      return new ArrayList(1);
      return CollectionUtil.EMPTY_ARRAY_LIST;
    }
    method.getProject().discoverAllUsedTypes();

    final BinTypeRef owner = method.getOwner();

    List subclasses = owner.getAllSubclasses();
    HashSet types = new HashSet(subclasses.size() + 4);

    if (subclasses.size() == 0) {
      findUpHierarchy(owner, types, method);
    } else {
      // must walk through all hierarchy, owner will be included from subclasses
      for (int i = 0, max = subclasses.size(); i < max; ++i) {
        BinTypeRef subClass = (BinTypeRef) subclasses.get(i);
        findUpHierarchy(subClass, types, method);
      }
    }

    types.removeAll(analyzedTypes);

    if (types.isEmpty()) {
      return CollectionUtil.EMPTY_ARRAY_LIST;
    }

    int expectedResults = types.size() * types.size() + 1;
    HashSet result = new HashSet(expectedResults, 0.9f);
    Iterator typesIt = types.iterator();
    BinTypeRef methodParams[] = BinParameter.parameterTypes(method.getParameters());

    // all collected types should already contain method overrides
    while (typesIt.hasNext()) {
      BinTypeRef type = (BinTypeRef) typesIt.next();

      BinMethod declaredMethods[] = type.getBinCIType().getDeclaredMethods();

      BinMethod typeMethod = null;
      for(int i = 0; i < declaredMethods.length; i++) {
        if(declaredMethods[i].sameSignature(method)) {
          typeMethod = declaredMethods[i];
          break;
        }
      }

      if (Assert.enabled && (typeMethod == null || typeMethod.isPrivate())) {
        Assert.must(false,
            "method " + method.getName() + "(" + Arrays.asList(methodParams) +
            ")" + " not found or private in " + type.getQualifiedName());
      }

      if (typeMethod == null) { // really strange though
        continue;
      }

      if (result.contains(typeMethod)) { // branch already analyzed by one of upper methods
        continue;
      }

      final List subMethods = type.getBinCIType().getSubMethods(typeMethod);
      if (subMethods != null) {
        result.addAll(subMethods);
      }
      result.add(typeMethod);
    }
    result.remove(method);

//      if (Assert.enabled && result.remove(null)) {
//        Assert.must(false, this.method.toString());
//      }

    Iterator res = result.iterator();
    while (res.hasNext()) {
      BinMethod meth = (BinMethod) res.next();
      analyzedTypes.add(meth.getOwner());
      if (meth.isSynthetic()) {
        res.remove();
      }
    }

    HashSet someMoreMethods = new HashSet(expectedResults);
    for (Iterator i = result.iterator(); i.hasNext();) {
      Object nextMethod = i.next();
      someMoreMethods.addAll(
          getRecursivelyAllOverridesList((BinMethod) nextMethod, analyzedTypes));
    }
    result.addAll(someMoreMethods);

    return new ArrayList(result);
  }

  public boolean isOverriddenOrOverrides() {
    if (this.findOverrides().size() > 0
        || this.getOwner().getBinCIType().getSubMethods(this).size() > 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * FIXME: bug, if do it on TreeASTImpl.initialize(final Token tok)
   * then ASTImpl.initialize(...) is not included in result!
   */
  public final List findAllOverridesOverriddenInHierarchy() {
    List recursivelyAllOverridesList
        = getRecursivelyAllOverridesList(this, new HashSet());
    if (!recursivelyAllOverridesList.isEmpty()) {
      recursivelyAllOverridesList.remove(this);
    } else { // empty is not modifiable list
      recursivelyAllOverridesList = new ArrayList(1);
    }
    return recursivelyAllOverridesList;
  }

  public final ASTImpl getParametersAst() {
    AST parametersAst = getRootAst().getFirstChild();

    while (parametersAst.getType() != JavaTokenTypes.PARAMETERS) {
      parametersAst = parametersAst.getNextSibling();
    }
    return (ASTImpl) parametersAst;
  }
  /**
   * If method is variable arity then returns true, otherwise - false
   * If the last formal parameter is a variable arity parameter of type T,
   * it is considered to define a formal parameter of type T[].
   * The method is then a variable arity method.
   * Otherwise, it is a fixed arity method.
   * Based on JLS3 (�8.4.1)
   * @return true if it is variable arity method or false otherwise
   */
  public final boolean isVariableArity() {
    if(getParameters().length > 0) {
      return getParameters()[getParameters().length - 1].isVariableArity();
    }
    return false;
  }

  /**
   * @return arity of the method
   */
  public final int getArity() {
    return getParameters().length;
  }

  public BinItemReference createReference() {
    return new BinMethodOrConstructorReference(this);
  }

  /**
   * @param annotationsList
   */
  public void setAnnotations(BinExpressionList annotationsList) {
    this.annotationsList = annotationsList;
  }

  public BinExpressionList getAnnotations() {
    return this.annotationsList;
  }

  public SourceCoordinate getParamsClosingBracket() {
    ASTImpl paramsAst = getParametersAst();
    if(paramsAst == null) {
      return null;
    }
    int line = paramsAst.getEndLine();
    int col = paramsAst.getEndColumn()-1;
    int endLine = hasBody()? getBodyAST().getLine(): getEndLine();
    int endCol = hasBody()? getBodyAST().getColumn(): getEndColumn();

    CompilationUnit cu = getCompilationUnit();
    try {
      while(line < endLine || col<=endCol) {
        String lineStr = cu.getSource().getContentOfLine(line);
        for(int max = lineStr.length(); col < max; col++) {
          if(lineStr.charAt(col) == ')' ) {
            return new SourceCoordinate(line, col+1);
          }
        }
        line++;
        col = 0;
      }
    } catch (Exception e){
    }
    return null;
  }

  public SourceCoordinate getThrowsCoordinate() {
    ASTImpl paramsAst = getParametersAst();
    if(paramsAst == null || getThrows().length == 0) {
      return null;
    }
    int line = paramsAst.getEndLine();
    int col = paramsAst.getEndColumn();
    int endLine = hasBody()? getBodyAST().getLine(): getEndLine();
    int endCol = hasBody()? getBodyAST().getColumn(): getEndColumn();

    CompilationUnit cu = getCompilationUnit();
    List comments = Comment.getCommentsIn(cu, line, col, endLine, endCol);

    try {
      while(line < endLine || col<=endCol) {
        String lineStr = cu.getSource().getContentOfLine(line);
        int start = 0;
        while((col = lineStr.indexOf("throws", start)) >= 0) {
          SourceCoordinate result = new SourceCoordinate(line, col+1);
          boolean isInsideComment = false;
          for(int i = 0; i < comments.size(); i++) {
            Comment c = (Comment)comments.get(i);
            if(result.isContainedBy(c)) {
              start = col+1;
              isInsideComment = true;
              break;
            }
          }
          if(!isInsideComment) {
            return result;
          }
        }
        line++;
        col = 0;
      }
    } catch (Exception e){
    }
    return null;
  }
}
