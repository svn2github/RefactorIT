/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


import net.sf.refactorit.common.util.Assert;

import java.util.ArrayList;


/**
 * Utilities for working with method invocations. Based on JLS section 15.12.
 * <p>
 * Basically there are two types of situations that need to be treated
 * differently:
 * <ul>
 * <li>Case 1: invocation in its simplest form: <i>Identifier </i> which is
 * method name. In this case you first need to find out to innermost enclosing
 * type containing method with the name. When found you execute
 * {@link #getMethodDeclaration getMethodDeclaration}
 * <code>(innermostType, thisType, name, parameters)</code></li>
 * <li>Case 2: invocation with dot (<code>.</code>). In this case you
 * evaluate the type of left expression and execute
 * {@link #getMethodDeclaration getMethodDeclaration}
 * <code>(leftType, leftType, name, parameters)</code></li>
 * </ul>
 * </p>
 * <h3>Examples</h3>
 *
 * <p>
 * <b>Case 1 </b> <code><pre>
 *
 * class A {
 *   void test() {}
 *
 *   class B {
 *     {
 *       test();
 *     }
 *   }
 * }
 *
 * BinCIType b = ...;
 * type = MethodInvocationRules.getTypeForDotlessInvocation(b, &quot;test&quot;);
 *   // type now points to A
 * final BinMethod method =
 *   MethodInvocationRules.getMethodDeclaration(
 *     b,
 *     type,
 *     &quot;test&quot;,
 *     new BinExpressionList(new BinExpression[0]));
 *
 * </pre></code>
 * </p>
 *
 * <p>
 * <b>Case 2 </b> <code><pre>
 *
 * class A {
 *   static void test() {}
 *
 *   class B {
 *     {
 *       A.test();
 *     }
 *   }
 * }
 *
 * BinCIType a = ...;
 * BinCIType b = ...;
 * final BinMethod method =
 *   MethodInvocationRules.getMethodDeclaration(
 *     b,
 *     a,
 *     &quot;test&quot;,
 *     new BinExpressionList(new BinExpression[0]));
 *
 * </pre></code>
 */
public final class MethodInvocationRules {

  /**
   * Gets compile-time type declaration the method is invoked on from the
   * specified context. See JLS 15.12.1 for more details. This method applies to
   * the following type of method invocation: <code><pre>
   *
   * MethodInvocation:
   *   MethodName (ArgumentList&lt;sub&gt;opt&lt;/sub&gt;)
   *
   * </pre></code>.
   *
   * @param context
   *          this method is invoked in -- class in body of which the method is
   *          invoked.
   *
   * @return type declaration method is invoked on or <code>null</code> if no
   *         suitable type found.
   */
  public static BinCIType getTypeForDotlessInvocation(final BinCIType context,
      final String methodName) {
    final BinMethod[] ownMethods = context.getAccessibleMethods(methodName,
        context);
    if (ownMethods.length != 0) {
      return context;
    }

    // No own accessible method(s) with this name found yet.

    if (context.isInnerType()) {
      // If inner type -- try owner class scope -- widen scope.
      return getTypeForDotlessInvocation(context.getOwner().getBinCIType(),
          methodName);
    } else {
      // Cannot widen scope anymore
      return null; // Not found
    }
  }

  /**
   * Determines compile-time method declaration for the method invocation. Based
   * on JLS section 15.12.
   *
   * @param context
   *          class where the invocation is located.
   * @param invokedOn
   *          compile-time type this method is invoked on.
   * @param methodName
   *          name of the method invoked.
   * @param argumentTypes
   *          arguments passed to the method.
   *
   * @return method declaration.
   */
  public static BinMethod getMethodDeclaration(final BinCIType context,
      final BinTypeRef invokedOn, final String methodName,
      final BinTypeRef[] argumentTypes) {
    if (Assert.enabled) {
      Assert.must(invokedOn != null, "invokedOn is null");
    }

//System.err.println("invokedon: " + invokedOn + " - " + methodName
//        + " - " + Arrays.asList(argumentTypes));

    // Find applicable and accessible methods
    BinMethod[] methods
        = invokedOn.getBinCIType().getAccessibleMethods(methodName, context);

    return findSuitableMethod(methods, argumentTypes);
  }

  //============================================================

  /**
   * Finds potentially applicable methods. Refer to JLS3 �15.12.2.1 for details.
   */
  private static BinMethod[] findPotentiallyApplicable(BinMethod[] methods,
      BinTypeRef[] argumentTypes) {

    final ArrayList results = new ArrayList(methods.length);
    final int methodInvocationArity = argumentTypes.length;
    for (int i = 0, max = methods.length; i < max; i++) {
      final BinMethod candidate = methods[i];

      final int n = candidate.getArity();
      if (candidate.isVariableArity()) {

        // The method(member) is a variable arity method with arity n, the arity
        // of the method invocation is greater or equal to n-1.
        if (!(methodInvocationArity >= n - 1)) {
          continue; // not applicable
        }
      } else {
        // If the method(member) is a fixed arity method with arity n, the arity
        // of
        // the method invocation is equal to n.
        if (!(methodInvocationArity == n)) {
          continue; //not applicable
        }
      }
      results.add(candidate);
      /*
       * TODO: If the method invocation includes explicit type parameters, and
       * the member is a generic method, then the number of actual type
       * parameters is equal to the number of formal type parameters.
       */

      /*
       * TODO: If the method invocation has, before the left parenthesis, a
       * MethodName of the form Identifier, then the search process also
       * examines all methods that are (a) imported by single-static-import
       * declarations (�7.5.3) and staticimport- on-demand declarations (�7.5.4)
       * within the compilation unit (�7.3) within which the method invocation
       * occurs, and (b) not shadowed (�6.3.1) at the place where the method
       * invocation appears.
       */
    }

    BinMethod[] applicableMethods
        = (BinMethod[]) results.toArray(new BinMethod[results.size()]);
    return applicableMethods;
  }

  /**
   * Phase 1: Identifying Matching Arity Methods Applicable by Subtyping. Refer
   * to JLS3 �15.12.2.2 for details. Never returns null, but may return an empty
   * array.
   */
  private static BinMethod[] findMatchingArityApplicableBySubtyping(
      BinMethod[] methods, BinTypeRef[] argumentTypes) {
    /*
     * TODO: add support for generic methods here (refer JLS3)
     */

    // Each actual argument type of the method invocation type shall be subtype
    // of the formal method parameter type
    final ArrayList results = new ArrayList(methods.length);
    for (int i = 0, max = methods.length; i < max; i++) {
      final BinMethod candidate = methods[i];

      if (isApplicableBySubtyping(candidate, argumentTypes)) {
        results.add(candidate);
      }
    }

    BinMethod[] applicableMethods
        = (BinMethod[]) results.toArray(new BinMethod[results.size()]);

    /*
     * TODO: Each actual argument type of the method invocation type can be
     * converted to some type by unchecked conversion (JLS3:�5.1.9), what is
     * subtype of the formal method parameter type
     */
    return applicableMethods;
  }

  /**
   * Phase 2: Identify Matching Arity Methods Applicable by Method Invocation
   * Conversion. Refer to JLS3 �15.12.2.3 for details. Return null if none
   * found.
   */
  private static BinMethod[]
      findMatchingArityApplicableByMethodInvocationConversion(
      BinMethod[] methods, BinTypeRef[] argumentTypes) {
    /*
     * TODO: add support for generic methods here (refer JLS3)
     */

    // Each actual argument type of the method invocation can be converted by
    // method invocation conversion (JLS3:�5.3) to the formal method parameter
    // type
    final ArrayList results = new ArrayList(methods.length);
    for (int i = 0, max = methods.length; i < max; i++) {
      final BinMethod candidate = methods[i];

      if (isApplicable(candidate, argumentTypes)) {
        results.add(candidate);
      }
    }

    return (BinMethod[]) results.toArray(new BinMethod[results.size()]);
  }

  /**
   * Phase 3: Identify Applicable Variable Arity Methods. Refer to JLS3
   * �15.12.2.4 for details. Return null if none found.
   * @param argumentTypes
   */
  private static BinMethod[] findVariableArityApplicable(BinMethod[] methods, BinTypeRef[] argumentTypes) {
    /*
     * TODO: add support for generic methods here (refer JLS3)
     */

    // Method is applicable only if all three condtions are hold
    // TODO: add for generics the last condition
    final ArrayList results = new ArrayList(methods.length);
    for (int i = 0, max = methods.length; i < max; i++) {
      BinMethod candidate = methods[i];
      if (isPreEllipsisParamsOk(candidate, argumentTypes)
          && isEllipsisParamsOk(candidate, argumentTypes)) {
        results.add(candidate);
      }
    }
    return (BinMethod[]) results.toArray(new BinMethod[results.size()]);
  }

  /**
   * For 1 <= i < n, the type of ei, Ai, can be converted by method invocation
   * conversion to Si.
   * @return true if condition above is hold, otherwise - false
   */
  private static boolean isPreEllipsisParamsOk(BinMethod method,
      BinTypeRef[] argumentTypes) {
    BinParameter[] parameters = method.getParameters();
    for (int j = 0, max = method.getArity() - 1; j < max; j++) {
      if (!TypeConversionRules.isMethodInvocationConversion(
          argumentTypes[j], parameters[j].getTypeRef())) {
        return false;
      }
    }
    return true;
  }

  /**
   * If k >= n, then for n <= i <= k, the type of ei, Ai, can be converted by
   * method invocation conversion to the component type of Sn.
   * @return true if condition above is hold, otherwise - false
   */
  private static boolean isEllipsisParamsOk(BinMethod method,
      BinTypeRef[] argumentTypes) {
    int n = method.getArity() - 1;
    int k = argumentTypes.length;
    BinTypeRef variableArityParam = method.getParameters()[n].getTypeRef();
    BinTypeRef simpleVariableArityParam = variableArityParam.getNonArrayType();
    for (int i = n; i < k; i++) {
      // JAVA5: total magic is here - something is really wrong on upperlevel
      if (!TypeConversionRules.isMethodInvocationConversion(
          argumentTypes[i] == null ? null : argumentTypes[i].getNonArrayType(),
          simpleVariableArityParam)
          && !TypeConversionRules.isMethodInvocationConversion(
          argumentTypes[i], simpleVariableArityParam)
          && !TypeConversionRules.isMethodInvocationConversion(
          argumentTypes[i], variableArityParam)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Are method.parameterTypes(Ai) a subtype (�4.10) of specified types(Si) (Ai <:
   * Si)
   *
   * @param method
   * @param types
   * @return true if method is applicable, otherwise - false
   */
  private static boolean isApplicableBySubtyping(BinMethod method,
      BinTypeRef[] types) {
    final int parameterCount = types.length;
    final BinParameter[] methodParameters = method.getParameters();

    if (parameterCount != methodParameters.length) {
      return false;
    }

    if(method.isVariableArity()) {
      return false;
    }

    for (int i = 0; i < parameterCount; i++) {
      final BinTypeRef parameterType = methodParameters[i].getTypeRef();
      final BinTypeRef argumentType = types[i];
      if (!TypeConversionRules.isSubtypingConversion(
          argumentType, parameterType)) {
        return false;
        // Argument cannot be converted to the declared formal
        // parameter type by subtyping conversion.
      }
    }

    return true;
  }

  /**
   * Finds the method, what is mostly specific one. It takes methods, looks
   * through them and finds the one, what mostly suits to the specified
   * parameterTypes
   *
   * @param methods
   *          where to look for
   * @param argumentTypes
   *          what to look for
   * @return BinMethod or null, if most specific method cannot be determined
   *         (invocation ambiguous)
   */
  public static BinMethod findSuitableMethod(BinMethod[] methods,
      final BinTypeRef[] argumentTypes) {
    methods = findSuitableCandidates(methods, argumentTypes);

    // Find most specific method from the list of candidates
    final BinMethod candidate = findMostSpecific(methods);

    return candidate;
  }

  //=============================================================
  //  /**
  //   * Finds the method, what is mostly specific one.
  //   * It takes methods, looks through them and finds the one, what mostly suits
  //   * to the specified parameterTypes
  //   * @param methods where to look for
  //   * @param parameterTypes what to look for
  //   * @return BinMethod or null, if most specific method cannot be determined
  //   * (invocation ambiguous)
  //   */
  //  public static BinMethod findSuitableMethod(BinMethod[] methods, final
  // BinTypeRef[] parameterTypes) {
  //    methods = findApplicable(methods, parameterTypes);
  //
  //    // Leave only those, what are conform to boxing conversion
  //    //candidates = filterBoxingSpecific(candidates, parameterTypes);
  //
  //    // Leave only maximally specific methods in the list of candidates
  //    methods = findMaximallySpecific(methods);
  //
  //    // Find most specific method from the list of candidates
  //    final BinMethod candidate = findMostSpecific(methods);
  //
  //    // Compile-time declaration (JLS 15.12.3) for the method invocation found.
  //    // Seems like we don't need to check these conditions, since
  //    // the assumption is that the code compiles.
  //    return candidate;
  //  }

  /**
   * Filter those methods, what are potentially applicable and maximally specific
   * @param methods to filter
   * @param argumentTypes
   * @return filtered method
   */
  public static BinMethod[] findSuitableCandidates(
      BinMethod[] methods, final BinTypeRef[] argumentTypes) {
    methods = findPotentiallyApplicable(methods, argumentTypes);
    BinMethod[] candidates
        = findMatchingArityApplicableBySubtyping(methods, argumentTypes);

    if (candidates.length == 0) { // if no results - run phase 2
      candidates = findMatchingArityApplicableByMethodInvocationConversion(
          methods, argumentTypes);
    }

    if(candidates.length == 0) { // if no results - run phase 3
      candidates = findVariableArityApplicable(methods,argumentTypes);
    }

    methods = findMaximallySpecific(candidates);

    return methods;
  }

//  /**
//   * Finds applicable methods from the provided list of methods.
//   *
//   * @param methods
//   *          list of methods to search in.
//   * @param parameters
//   *          list of parameter types passed to the invocation.
//   * @return
//   */
//  private static BinMethod[] filterBoxingSpecific(BinMethod[] methods,
//      BinTypeRef[] parameters) {
//    final ArrayList results = new ArrayList(methods.length);
//    for (int i = 0; i < methods.length; i++) {
//      final BinMethod candidate = methods[i];
//
//      if (isApplicable(candidate, parameters)) {
//        results.add(candidate);
//      }
//    }
//
//    return (BinMethod[]) results.toArray(new BinMethod[results.size()]);
//  }

//  /**
//   * Finds applicable methods from the provided list of methods. See JLS
//   * 15.12.2.1.
//   *
//   * @param methods
//   *          list of methods to search in.
//   * @param parameters
//   *          list of parameter types passed to the invocation.
//   *
//   * @return list of methods that are applicable for this invocation and belong
//   *         to the provided list of methods. Never returns <code>null</code>.
//   */
//  private static BinMethod[] findApplicable(final BinMethod[] methods,
//      final BinTypeRef[] parameters) {
//
//    final ArrayList results = new ArrayList(methods.length);
//    for (int i = 0; i < methods.length; i++) {
//      final BinMethod candidate = methods[i];
//
//      if (isApplicable(candidate, parameters)) {
//        results.add(candidate);
//      }
//    }
//
//    return (BinMethod[]) results.toArray(new BinMethod[results.size()]);
//  }

  /**
   * Finds maximally specific (JLS 15.12.2.2) methods from the provided list of
   * methods.
   *
   * @param methods list of methods to search from.
   *
   * @return list of methods that are maximally specific and belong to the
   *         provided list of methods. Never returns <code>null</code>.
   */
  private static BinMethod[] findMaximallySpecific(final BinMethod[] methods) {
    // A method m1(otherCandidate) is strictly more specific than another method
    // m2(candidate) if and only if m1 is more specific than m2 and m2 is not
    // more specific than m1
//System.err.println("methods: " + Arrays.asList(methods));

    final int methodsLen = methods.length;
    final ArrayList results = new ArrayList(methodsLen);
    for (int i = 0; i < methodsLen; i++) {
      final BinMethod candidate = methods[i];

      boolean moreSpecificFound = false;
      for (int j = 0; j < methodsLen; j++) {
        final BinMethod otherCandidate = methods[j];
        if (otherCandidate == candidate) {
          continue; // Don't compare with self.
        }

        if (isMoreSpecific(otherCandidate, candidate)) {
          moreSpecificFound = true;
          break;
        }
      }

      if (!moreSpecificFound) {
//System.err.println("add: " + candidate);
        results.add(candidate);
      }
    }

    if (results.size() == 0 && methodsLen != 0) {
      // all were equally specific :)
      results.add(methods[0]);
    }
//System.err.println("result: " + results);

    return (BinMethod[]) results.toArray(new BinMethod[results.size()]);
  }

  /**
   * Finds most specific (JLS3 �15.12.2.5) method from the provided list of
   * methods.
   *
   * @param methods
   *          list of methods to search in.
   *
   * @return method that is maximally specific and is taken from the list of
   *         methods, or <code>null</code> if maximally specific method cannot
   *         be found.
   */
  private static BinMethod findMostSpecific(final BinMethod[] methods) {
    /*
     * 1) If all the maximally specific methods have override-equivalent
     * (�8.4.2) signatures, then: a) If exactly one of the maximally specific
     * methods is not declared abstract, it is the most specific method.
     * b)Otherwise, if all the maximally specific methods are declared abstract,
     * and the signatures of all of the maximally specific methods have the same
     * erasure (�4.6), then the most specific method is chosen arbitrarily among
     * the subset of the maximally specific methods that have the most specific
     * return type. However, the most specific method is considered to throw a
     * checked exception if and only if that exception or its erasure is
     * declared in the throws clauses of each of the maximally specific methods.
     *
     * 2) Otherwise, we say that the method invocation is ambiguous, and a
     * compiletime error occurs.
     */
    if (methods.length == 1) {
      return methods[0]; // The only method in list must be maximally specific
    } else if (methods.length < 1) {
      return null; // No methods in list
    } else {
      // Check whether all methods have the same signature
      BinParameter[] firstMethodParams = methods[0].getParameters();
      final int parameterCount = firstMethodParams.length;
      boolean parameterTypesMatch = true;
      outer: for (int i = 0; i < parameterCount; i++) {
        BinTypeRef firstMethodParam = firstMethodParams[i].getTypeRef();
        for (int j = 1; j < methods.length; j++) {
          BinTypeRef otherMethodParam = methods[j].getParameters()[i].getTypeRef();
          if (!(TypeConversionRules.isIdentityConversion(
              firstMethodParam, otherMethodParam) 
              || TypeConversionRules.isSuitablePrimitiveConversion(
              firstMethodParam, otherMethodParam))) {
            parameterTypesMatch = false;
            break outer;
          }
        }
      }

      if (parameterTypesMatch) {
        // Check whether one of the methods is not declared abstract
        for (int i = 0; i < methods.length; i++) {
          final BinMethod method = methods[i];
          if (!method.isAbstract()) {
            return method; // FOUND!
          }
        }

        // All methods declared abstract
        // Choose arbitrary one -- the first one.
        return methods[0];
      } else {
        return null; // Invocation ambiguous
      }
    }
  }

  /**
   * Checks whether invocation of method with the specified parameters is
   * applicable.
   *
   * @param method
   *          method to check for applicability.
   * @param parameters
   *          parameter types passed to the method during the invocation.
   *
   * @return <code>true</code> if and only if the invocation is applicable to
   *         the method, <code>false</code> otherwise.
   */
  public static boolean isApplicable(final BinMethod method,
      final BinTypeRef[] parameters) {
    final int parameterCount = parameters.length;
    final BinParameter[] methodParameters = method.getParameters();

    if (parameterCount != methodParameters.length) {
      // Number of parameters different.
      return false;
    }

    if(method.isVariableArity()) {
      return false;
    }

    for (int i = 0; i < parameterCount; i++) {
      final BinTypeRef candidateParamType = methodParameters[i].getTypeRef();
      final BinTypeRef invokedParamType = parameters[i];
      if (!TypeConversionRules
          .isMethodInvocationConversion(invokedParamType, candidateParamType)) {
        return false; // Parameter cannot be converted to the declared formal
        // parameter type by method invocation expression.
      }
    }

    return true; // Applicable
  }

  /**
   * Checks whether specified method is more specific (JLS3 �15.12.2.5) than the
   * other method.
   *
   * @param method1 method to check.
   * @param method2 method to compare with.
   *
   * @return <code>true</code> if and only if <code>method1</code> if more
   *         specific than <code>method2</code>,<code>false</code> otherwise.
   */
  private static boolean isMoreSpecific(final BinMethod method1,
      final BinMethod method2) {
//System.err.println("moreSpec: " + method1 + " -- " + method2);
    final BinTypeRef owner1 = method1.getOwner();
    final BinTypeRef owner2 = method2.getOwner();
//System.err.println("xxx: " + TypeConversionRules.isMethodInvocationConversion(owner1, owner2));

    if (!TypeConversionRules.isMethodInvocationConversion(owner1, owner2)) {
      return false;
    }
    if (!method1.isVariableArity() || !method2.isVariableArity()) {
	    /*
	     * One fixed-arity member method named m is more specific than another
	     * member method of the same name and arity if all of the following
	     * conditions hold:
	     * 1) The declared types of the parameters of the first
	     * member method are T1, ... , Tn.
	     *
	     * 2) The declared types of the parameters of the other method are U1, ... , Un.
	     */
	    /*
	     * TODO: (generics) If the second method is generic then let S1, .., Sn be the types
	     * inferred (�15.12.2.7) for its parameters under the initial constraints
       * Ti << Ui 1 <= i <= n;
	     * otherwise let 1 <= i <= n. Then, for all j from 1 to n, Tj <: Sj.
	     */

	    BinParameter[] method1Parameters = method1.getParameters();
      for (int i = 0, max = method1Parameters.length; i < max; i++) {
	      final BinTypeRef param1 = method1Parameters[i].getTypeRef();
	      final BinTypeRef param2 = method2.getParameters()[i].getTypeRef();
	      if (!TypeConversionRules.isMethodInvocationConversion(param1, param2)) {
	        return false;
	      }
	    }
    } else {
      /*
       * In addition, one variable arity member method named m is more
       * specific than another variable arity member method of the same name if
       *
       * 1) One member method has n parameters and the other has k parameters,
       * where n >= k . The types of the parameters of the first member method are
       * T1, . . . , Tn-1 , Tn[], the types of the parameters of the other method
       * are U1, . . . , Uk-1 , Uk[].
       *
       * TODO: (generics) If the second method is generic then let S1,
       * .., Sk be the types inferred (�15.12.2.7) for its parameters under the
       * initial constraints Ti << Ui 1 <= i <= k-1, Ti << Uk k <= i <= n;
       * otherwise let Si = Ui 1 <= i <= k. Then: a) for all j from 1 to k-1 , Tj
       * <:Sj , and, b) for all j from k to n , Tj <: Sk .
       */
      int n = method1.getArity();
      int k = method2.getArity();
      if (n < k) {
        return false;
      }

      for (int j = 0; j < k - 1; j++) {
        BinTypeRef param1 = method1.getParameters()[j].getTypeRef();
        BinTypeRef param2 = method2.getParameters()[j].getTypeRef();
        if (!TypeConversionRules.isMethodInvocationConversion(param1, param2)) {
          return false;
        }
      }
      for (int j = k - 1; j < n; j++) {
        BinTypeRef param1 = method1.getParameters()[j].getTypeRef().getNonArrayType();
        BinTypeRef param2 = method2.getParameters()[k - 1].getTypeRef().getNonArrayType();
        if (!TypeConversionRules.isMethodInvocationConversion(param1, param2)) {
          return false;
        }
      }
    }
    return true;
  }

}
