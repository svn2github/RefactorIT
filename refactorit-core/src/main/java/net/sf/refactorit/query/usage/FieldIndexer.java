/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;


public class FieldIndexer extends TargetIndexer {

  /** Reference type relative to which super- and subtypes are defined. */
  private final BinTypeRef reference;

  public FieldIndexer(final ManagingIndexer supervisor,
      final BinFieldInvocationExpression target,
      final boolean includeSubclasses,
      final boolean includeSuperclasses) {
    this(supervisor,
        target.getField(),
        includeSubclasses,
        target.getInvokedOn(),
        includeSuperclasses);
  }

  public FieldIndexer(final ManagingIndexer supervisor, final BinField target,
      final boolean includeSubclasses) {

    this(supervisor,
        target,
        includeSubclasses,
        target.getOwner(),
        true // include superclasses -- doesn't change anything but makes
        // searching faster
        );
  }

  /**
   * Constructs new field indexer with specified reference type. Reference type
   * defines which types are supertypes and which ones are subclasses.
   * For example,
   *<code><pre>
   *class A {
   * int field = 13; // (1)
   *}
   *
   *class B extends A {
   *  {
   *    i = 14; // (2)
   *  }
   *}
   *
   *class C extends B {
   *  {
   *    i = 15; // (3)
   *  }
   *}
   *</pre></code>
   * Field searched for is <code>A.field</code>. If reference is <code>A</code>
   *then <code>includeSuperclasses</code> has no influence on the results,
   *whereas usages (2) and (3) will only be found in case
   * <code>includeSubclasses</code> is set. In case reference is <code>B</code>
   *usage (1) will be found only if <code>includeSuperclasses</code> is set, and
   *usage (3) will be found only if <code>includeSubclasses</code> is set.
   */
  public FieldIndexer(final ManagingIndexer supervisor,
      final BinField target,
      final boolean includeSubclasses,
      final BinTypeRef reference,
      final boolean includeSuperclasses) {

    super(supervisor,
        target,
        reference.getBinCIType(),
        includeSubclasses,
        includeSuperclasses);
    this.reference = reference;
  }

  /**
   * Checks field invocations.<br><br>
   *
   * <b>Note: this method uses BinField equality comparing! Strongly relies on
   * the correct classmodel! BE AWARE OF POTENTIAL BUGS!!!</b>
   */
  public void visit(final BinFieldInvocationExpression expression) {
    // this comparing is a place for potential bugs, but it is worth it
    if (getTarget() != expression.getField()) {
      return; // Doesn't match
    }

    // Shortcut. If both filters are set we don't care on which type field
    // was invoked on.
    if ((isIncludeSubtypes()) && (isIncludeSupertypes())) {
      addInvocation(expression);
      return; // Matched!
    }

    final BinTypeRef invokedOn = expression.getInvokedOn();
    if (invokedOn == reference || invokedOn.equals(reference)) {
      // Exact match -- no need to check for subtypes or supertypes
      addInvocation(expression);
      return; // Matched!
    }

    // Check for subclasses if necessary
    if ((isIncludeSubtypes())
        && (reference.getAllSubclasses().contains(invokedOn))) {
      addInvocation(expression);
      return; // Matched!
    }

    // Check for supertypes if necessary
    if ((isIncludeSupertypes())
        && (reference.getAllSupertypes().contains(invokedOn))) {
      addInvocation(expression);
      return; // Matched!
    }

    // Didn't match
  }

  /**
   * Adds invocation to the list of results.
   *
   * @param expression field invocation expression.
   */
  private void addInvocation(final BinFieldInvocationExpression expression) {
    getSupervisor().addInvocation(
        getTarget(),
        getSupervisor().getCurrentLocation(),
        expression.getNameAst(),
        expression);
  }
}
