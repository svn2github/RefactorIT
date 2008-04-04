/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.rename;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.common.util.PhraseSplitter;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SemanticRenameIndexer extends AbstractIndexer {

  private final class SemanticFinder extends SinglePointVisitor {
    private boolean running = true;
    private boolean suits = false;

    private final BinTypeRef typeRef;
    private List checkedMembers = new ArrayList();

    private SemanticFinder(BinTypeRef ref) {
      super();
      this.typeRef = ref;
    }

    public boolean shouldVisitContentsOf(BinItem x) {
      return running;
    }

    public void onEnter(Object o) {
      // refactor checks
      if(o instanceof BinParameter) {
        BinTypeRef exprRef = ((BinParameter) o).getTypeRef();
        if(exprRef.isDerivedFrom(typeRef) || typeRef.isDerivedFrom(exprRef)) {
          suits = true;
          running = false;
          return;
        }
      } else if(o instanceof BinExpression) {
        BinTypeRef exprRef = ((BinExpression) o).getReturnType();

        if (exprRef != null
            && (exprRef.isDerivedFrom(typeRef) || typeRef
                .isDerivedFrom(exprRef))) {
          suits = true;
          running = false;
          return;
        }

        if (o instanceof BinMemberInvocationExpression) {
          BinMemberInvocationExpression invocation = (BinMemberInvocationExpression) o;

          BinMember m = invocation.getMember();

          // to prevent dead looping
          if(!checkedMembers.contains(m)) {
            checkedMembers.add(m);
            m.accept(this);
            checkedMembers.remove(m);
          }
        }
      } else if(o instanceof BinVariableDeclaration) {
        BinVariableDeclaration d = ((BinVariableDeclaration)o);
        BinVariable[] vars = d.getVariables();
        for(int i = 0; i < vars.length; i++) {
          if(typeRef.isDerivedFrom(vars[i].getTypeRef()) ||
              vars[i].getTypeRef().isDerivedFrom(typeRef)) {
            suits = true;
            running = false;
            return;
          }
        }
      }
    }

    public void onLeave(Object o) {}

    public boolean suits() {
      return suits;
    }
  }


  private List list = new ArrayList();
  private BinCIType type;
  private String[] typeName;
  private final BinTypeRef ref;

  public SemanticRenameIndexer(BinCIType type) {
    super();
    this.type = type;
    this.ref = type.getTypeRef();
    this.typeName = new PhraseSplitter(type.getName()).getAllWords();
  }

  public BinMember[] getCandidates() {
    return (BinMember[])list.toArray(new BinMember[list.size()]);
  }

  public void visit(BinMethod method) {
    // filtering by name
    int[][] indexes = StringUtil.indexesOfSubPhrase(new PhraseSplitter(method
        .getName()).getAllWords(), typeName);

    // filtering by semantic coupling
    if(indexes.length > 0) {
      if(areSemanticallyCoupled(method, ref)) {
        list.add(method);
      }
    }
    super.visit(method);
  }

  public void visit(BinCIType type) {
    // filtering by name
    int[][] indexes = StringUtil.indexesOfSubPhrase(new PhraseSplitter(type
        .getName()).getAllWords(), typeName);

    // filtering by semantic coupling
    if(indexes.length > 0) {
      if(areSemanticallyCoupled(type, ref)) {
        list.add(type);
      }
    }
    super.visit(type);
  }

  public void visit(BinLocalVariable var) {
    int[][] indexes = StringUtil.indexesOfSubPhrase(new PhraseSplitter(
        var.getName()).getAllWords(), typeName);

    if(indexes.length > 0) {
      if(areSemanticallyCoupled(var, ref)) {
        list.add(var);
      }
    }
    super.visit(var);
  }

  public void visit(BinField field) {
    // filtering by name
    PhraseSplitter splitter = new PhraseSplitter(field.getName());
    int[][] indexes = StringUtil.indexesOfSubPhrase(splitter.getAllWords(), typeName);

    if(indexes.length > 0) {

      boolean suits = areSemanticallyCoupled(field, ref);

      if(suits) {
        list.add(field);
      }
    }

    super.visit(field);
  }



  // =====================================================

  private boolean areSemanticallyCoupled(BinLocalVariable var, BinTypeRef ref) {
    BinTypeRef parameterRef = var.getTypeRef();
    if(parameterRef.isDerivedFrom(ref) || ref.isDerivedFrom(parameterRef)) {
      return true;
    }

    SemanticFinder visitor = new SemanticFinder(ref);
    if(var instanceof BinParameter) {
      ((BinParameter)var).getMethod().accept(visitor);
    } else {
      var.accept(visitor);
    }
    if (visitor.suits()) {
      return true;
    }

    // checking if field is referencing this class
    List invocations = Finder.getInvocations(var,
        new BinVariableSearchFilter(false, true, false, false, false));
    for (Iterator it = invocations.iterator(); it.hasNext();) {
      InvocationData data = (InvocationData) it.next();
      BinItemVisitable item = data.getInConstruct().getParent();
      item.accept(visitor);
      if (visitor.suits()) {
        return true;
      }
    }
    return false;
  }

  private boolean areSemanticallyCoupled(final BinMethod method, final BinTypeRef ref) {
    final boolean[] suit_arr = { false };

    BinTypeRef methodReturnRef = method.getReturnType();
    if (methodReturnRef != null
        && (methodReturnRef.isDerivedFrom(ref)
            || ref.isDerivedFrom(methodReturnRef))) {
      return true;
    }

    SemanticFinder visitor = new SemanticFinder(ref);
    method.accept(visitor);
    return visitor.suits();
  }


  private boolean areSemanticallyCoupled(final BinField field,
      final BinTypeRef ref) {
    boolean suits = false;
    {
      // checking if this field belong to the class (property)
      BinCIType parentType = field.getParentType();
      while (parentType != null) {
        if (ref.isDerivedFrom(parentType.getTypeRef())
            || parentType.getTypeRef().isDerivedFrom(ref)) {
          return true;
        }
        parentType = parentType.getParentType();
      }
    }
    SemanticFinder visitor = new SemanticFinder(ref);
    field.accept(visitor);
    if (visitor.suits()) {
      return true;
    }

    // checking if field is referencing this class
    {
      List invocations = Finder.getInvocations(field,
          new BinVariableSearchFilter(false, true, false, false, false));
      for (Iterator it = invocations.iterator(); it.hasNext();) {
        InvocationData data = (InvocationData) it.next();
        BinItemVisitable item = data.getInConstruct().getParent();
        item.accept(visitor);
        if (visitor.suits()) {
          return true;
        }
      }
    }
    return suits;
  }


  private boolean areSemanticallyCoupled(BinCIType type, BinTypeRef ref) {
    if(type.getTypeRef() == ref) {
      return false;
    }
    // TODO Auto-generated method stub
    return true;
  }


}
