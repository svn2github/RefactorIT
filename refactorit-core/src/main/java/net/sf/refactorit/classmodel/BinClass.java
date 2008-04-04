/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;


import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.UserFriendlyError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Contains information about loaded class.
 */
public class BinClass extends BinCIType {

  //private static Set builded = new HashSet();

  public BinClass(BinPackage aPackage,
      String name,
      BinMethod[] methods,
      BinField[] fields,
      BinFieldDeclaration[] fieldDeclarations,
      BinConstructor[] constructors,
      BinInitializer[] initializers,
      BinTypeRef[] inners,
      BinTypeRef declaringType,
      int modifiers,
      Project project) {
    super(aPackage,
        name,
        methods,
        fields,
        fieldDeclarations,
        inners,
        declaringType,
        modifiers,
        project);

    /*    if (!builded.contains(getQualifiedName())) {
          builded.add(getQualifiedName());
        } else {
          System.err.println("Was already created: " + getQualifiedName());
     System.err.println("---- : " + getName() + ", package: " + getPackage());
          if (getName() == null) {
            new Exception("Class name is null").printStackTrace(System.err);
          }
        }*/

    this.declaredConstructors = constructors /*.clone()*/;
    this.initializers = initializers;
  }

  public final void setOwners(BinTypeRef myRef) {
    for (int i = 0; i < this.declaredConstructors.length; ++i) {
      this.declaredConstructors[i].setOwner(myRef);
      this.declaredConstructors[i].setParent(this);
    }

    for (int i = 0; i < this.initializers.length; ++i) {
      this.initializers[i].setOwner(myRef);
      this.initializers[i].setParent(this);
    }

    super.setOwners(myRef);
  }

  /**
   * @return constructors declared in the type
   */
  public final BinConstructor[] getDeclaredConstructors() {
    if (Assert.enabled) {
      return (BinConstructor[]) declaredConstructors.clone();
    } else {
      return declaredConstructors;
    }
  }

  public final boolean hasDeclaredConstructors() {
    return this.declaredConstructors != null
        && this.declaredConstructors.length > 0;
  }

  public final boolean hasDefaultConstructor() {
    return this.defaultConstructor != null
        && this.defaultConstructor.length > 0
        && defaultConstructor[0] != null;
  }

  /**
   * <b>For testing purposes only</b>
   * @param cnstrs declaredConstructors array
   */
  public final void setDeclaredConstructors(BinConstructor[] cnstrs) {
    this.declaredConstructors = cnstrs;
  }

  /**
   * Forms default constructor according to JLS 8.8.7 when needed.
   * @return constructors including fake default one if missing in type
   */
  public final BinConstructor[] getConstructors() {
    if (!hasDeclaredConstructors()) {
      ensureDefaultConstructor();
      if (Assert.enabled) {
        return (BinConstructor[])this.defaultConstructor.clone();
      } else {
        return this.defaultConstructor;
      }
    } else {
      return getDeclaredConstructors();
    }
  }

  /** It might be declared or synthetic, but in any case has no params */
  public final BinConstructor getDefaultConstructor() {
    BinTypeRef[] paramTypes;
    if ("java.lang.Enum".equals(getQualifiedName())) {
      paramTypes = getEnumConstructorParams();
    } else {
      paramTypes = BinTypeRef.NO_TYPEREFS;
    }
    return getConstructor(paramTypes);
  }

  public final BinConstructor getConstructor(BinTypeRef[] paramTypes) {
    return (BinConstructor) getDeclaredMethod(
        getConstructors(), getName(), paramTypes);
  }

  public final void ensureDefaultConstructor() {
    if (!hasDefaultConstructor() && !hasDeclaredConstructors()) {
      BinExpressionList expressionList;
      if (isEnum()) {
        expressionList = new BinExpressionList(getEnumConstructorExprs());
      } else {
        expressionList = BinExpressionList.NO_EXPRESSIONLIST;
      }
      createDefaultConstructor(expressionList);
    }
  }

  private BinTypeRef[] getEnumConstructorParams() {
    return new BinTypeRef[] {
        getProject().getTypeRefForName("java.lang.String"),
        BinPrimitiveType.INT_REF
    };
  }

  private BinExpression[] getEnumConstructorExprs() {
    return new BinExpression[] {
        new BinLiteralExpression("s", getEnumConstructorParams()[0], null),
        new BinLiteralExpression("i", getEnumConstructorParams()[1], null)
    };
  }

  public final void createDefaultConstructor(BinExpressionList expressionList) {
    createDefaultConstructor(expressionList, null);
  }

  /**
   * Follows in general JLS 8.8.7
   *
   * @param expressionList defines parameter types
   */
  public final void createDefaultConstructor(BinExpressionList expressionList,
      BinConstructor forward) {
    this.defaultConstructor = new BinConstructor[1];

    BinParameter[] params;

    if (forward == null) {
      params = new BinParameter[expressionList.getExpressions().length];
      BinTypeRef[] types = expressionList.getExpressionTypes();
      for (int i = 0, max = params.length; i < max; i++) {
        params[i] = new BinParameter(String.valueOf((char) ('a' + i)),
            types[i], BinModifier.PACKAGE_PRIVATE);
        // FIXME what about source and offset?
      }
    } else {
      BinParameter[] sourceParams = forward.getParameters();
      params = new BinParameter[sourceParams.length];
      for (int i = 0, max = params.length; i < max; ++i) {
        String paramName = sourceParams[i].getName();
        if (paramName == null) {
          paramName = String.valueOf((char) ('a' + i));
        }
        if (sourceParams[i] instanceof BinVariableArityParameter) {
          params[i] = new BinVariableArityParameter(paramName,
              sourceParams[i].getTypeRef(), sourceParams[i].getModifiers());
        } else {
          params[i] = new BinParameter(paramName,
              sourceParams[i].getTypeRef(), sourceParams[i].getModifiers());
        }
        // FIXME what about source and offset?
      }
    }

    this.defaultConstructor[0] = BinConstructor.createByPrototype(
        getTypeRef(), params,
        this.getModifiers() & BinModifier.PRIVILEGE_MASK,
        forward == null ? BinMethod.Throws.NO_THROWS : forward.getThrows(),
        true, getTypeRef());                // FIXME: not from forward, but should be fixed? read spec!

    if (Assert.enabled) {
      Assert.must(this.getTypeRef() != null,
          "No typeRef for type: " + getQualifiedName());
    }

    final ASTImpl nameNode = getNameAstOrNull();

    this.defaultConstructor[0].setOffsetNode(nameNode);
    this.defaultConstructor[0].setNameAst(nameNode);

    BinStatementList body;
    BinTypeRef superClass = getTypeRef().getSuperclass();
    boolean isObject = Project.OBJECT.equals(this.getQualifiedName());
    if (!isObject && superClass != null) {
      // invocation of super constructor
      BinExpression[] expressions;
      if (isEnum()
          || (superClass != null && superClass.getBinCIType().isEnum()
          && ((BinEnum) superClass.getBinCIType()).hasDefaultConstructor())
          || (isTypeParameter() && "java.lang.Enum".equals(superClass.getQualifiedName()))) {
        expressions = getEnumConstructorExprs();
      } else {
        expressions = new BinExpression[params.length];
        for (int i = 0; i < params.length; i++) {
          expressions[i]
              = new BinVariableUseExpression(params[i], nameNode);
        }
      }

      BinConstructorInvocationExpression invocation
          = new BinConstructorInvocationExpression(
          this.getTypeRef(), superClass,
          new BinExpressionList(expressions), true, nameNode);
      invocation.setNameAst(nameNode);

      ((DependencyParticipant) superClass).addDependable(getTypeRef());

      BinStatementList statementList;
      if (invocation.getConstructor() == null) {
        statementList = new BinStatementList(BinStatement.NO_STATEMENTS, nameNode);
        getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(new UserFriendlyError(
                "Wrong call of super constructor in default constructor: "
                + this + " -- " + superClass,
                getCompilationUnit(),
                this.getTypeRef().getBinCIType().getStartLine(),
                this.getTypeRef().getBinCIType().getStartColumn()));
      } else {
        BinStatement statement
            = new BinExpressionStatement(invocation, nameNode);
        statementList
            = new BinStatementList(new BinStatement[] {statement}, nameNode);
      }

      body = statementList;
    } else {
      if (Assert.enabled && !isObject && superClass == null) {
        AppRegistry.getLogger(this.getClass()).debug(
            "No superclass for: " + getTypeRef(),
            new Exception("No superclass for: " + getTypeRef()));
      }

      // no super invocation when constructing java.lang.Object
      body = null;
    }
    this.defaultConstructor[0].setBody(body);
    this.defaultConstructor[0].setParent(this);
    BinParentFinder.findParentsFor(this.defaultConstructor[0]);
  }

  /** Finds witch constructors are applicable at all, and then returns most specific one of these. */
  public final BinConstructor getAccessibleConstructor(BinCIType context,
      BinTypeRef[] paramTypes) {

    final BinConstructor[] cnstrs = getConstructors();
    if (context == null) {
      context = this;
    }

    // FIXME: filter out not accessible in given context

    BinMethod[] mostSpecific
        = MethodInvocationRules.findSuitableCandidates(cnstrs, paramTypes);
    if (mostSpecific == null || mostSpecific.length == 0) {
      return null;
    } else {
      return (BinConstructor) mostSpecific[0];
    }
  }

  public final BinInitializer[] getInitializers() {
    if (Assert.enabled) {
      return (BinInitializer[]) initializers.clone();
    } else {
      return initializers;
    }
  }

  /**
   * Gets all methods declared in this class. Same contract as
   * {@link java.lang.Class#getDeclaredMethods()}.
   * <p>
   * The rules can be summarized as follows:
   * <ul>
   *  <li>For concrete class returns all methods declared in the
   *      source file</li>
   *  <li>For abstract class returns all methods declared in the source file and
   *      implemented interfaces
   *      and their superinterfaces excluding methods
   *      declared in superclass</li>
   * </ul>
   * </p>
   *
   * @return methods. Never returns <code>null</code>.
   *
   * @see java.lang.Class#getDeclaredMethods()
   */
  public final BinMethod[] getDeclaredMethods() {
    ensureCopiedMethods();

    return super.getDeclaredMethods();
  }

  public final BinConstructor getDeclaredConstructor(BinParameter[] parameters) {
    return (BinConstructor) getDeclaredMethod(getDeclaredConstructors(),
        getName(), parameters);
  }

  public final void ensureCopiedMethods() {
    if (declaredMethodsConstructed) {
      return;
    }

    if (!isAbstract()) {
      // if it is not abstract then it cannot possibly
      // contain not overriden methods, or in other words
      // all methods are overriden for sure.
      return;
    }

    final List newMethodsList = new ArrayList();
    final BinTypeRef superclass = getTypeRef().getSuperclass();
    newMethodsList.addAll(Arrays.asList(this.declaredMethods));

    BinTypeRef[] interfacesArray = getTypeRef().getInterfaces();

    for (int interI = 0; interI < interfacesArray.length; interI++) {
      final BinTypeRef curInterface = interfacesArray[interI];

      // if superclass is already derived from interface,
      // then it's unimplemented abstract methods doesn't go to list ... :(
      if (superclass != null
          && superclass.isDerivedFrom(curInterface)) {
        continue;
      }

      // Methods of interface and interfaces it extends, they extends and
      // so on.
      BinMethod[] interfaceMethods
          = curInterface.getBinCIType().getAccessibleMethods(this);
      for (int interMethodI = 0;
          interMethodI < interfaceMethods.length;
          interMethodI++) {
        BinMethod interfaceMethod = interfaceMethods[interMethodI];
        boolean found = false;
        for (int methodI = 0; methodI < declaredMethods.length; methodI++) {
          BinMethod method = declaredMethods[methodI];
          if (method.sameSignature(interfaceMethod)) {
            found = true;
            break;
          }
        } // for methods

        // look also into superclass, interface could be possibly implemented
        // also in superclass!
        if (!found && superclass != null) {
          BinMethod[] superMethods = superclass.getBinCIType()
              .getAccessibleMethods(superclass.getBinCIType());
          for (int methodI = 0; methodI < superMethods.length; methodI++) {
            BinMethod method = superMethods[methodI];
            if (method.sameSignature(interfaceMethod)) {
              found = true;
              break;
            }
          } //for methods
        }

        if (!found) {
          final BinMethod method
              = interfaceMethod.cloneForInterfaceInheritance(getTypeRef());
          ((DependencyParticipant) interfaceMethod.getOwner()).addDependable(getTypeRef());
          method.setOwner(getTypeRef());
          newMethodsList.add(method);
        }
      } // for interface methods
    }

    this.declaredMethods = (BinMethod[]) newMethodsList.toArray(
        new BinMethod[newMethodsList.size()]);
    declaredMethodsConstructed = true;
  }

  public final void defaultTraverse(BinItemVisitor visitor) {
    super.defaultTraverse(visitor);

    BinConstructor[] allConstructors = getConstructors();
    for (int i = 0; i < allConstructors.length; ++i) {
      allConstructors[i].accept(visitor);
    }

    for (int i = 0; i < initializers.length; ++i) {
      initializers[i].accept(visitor);
    }
  }

  public final void cleanForPrototype() {
    super.cleanForPrototype();

    BinConstructor[] constructors = this.declaredConstructors; // getDeclaredConstructors();
    if (constructors != null) {
      for (int i = 0; i < constructors.length; ++i) {
        constructors[i].cleanForPrototype();
      }
    }

    BinInitializer[] inits = this.initializers; // getInitializers();
    if (inits != null) {
      for (int i = 0; i < inits.length; ++i) {
        inits[i].clean();
      }
    }
  }

  public void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public boolean isClass() {
    return true;
  }

  public boolean isInterface() {
    return false;
  }

  public boolean isEnum() {
    return false;
  }

  public boolean isAnnotation() {
    return false;
  }

  public String getMemberType() {
    return memberType;
  }

  public static String getStaticMemberType() {
    return memberType;
  }

  private BinConstructor[] declaredConstructors;
  private BinConstructor[] defaultConstructor;
  private final BinInitializer[] initializers;

  private boolean declaredMethodsConstructed = false;

  private static final String memberType = "class";


}
