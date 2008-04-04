/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.gotomodule.actions;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.ejb.RitEjbModule;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.dialog.RitMenuItem;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



/**
 * @author RISTO A
 */
public abstract class GoToModuleAction extends AbstractRefactorItAction {

  private IdeWindowContext context;

  private class Action implements Runnable {
    private Object object;

    public Action(final Object object) {
      this.object = object;
    }

      public void run() {
        runImpl(object);
      }
  }


  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean run(final RefactorItContext context, Object object) {
    // Catch incorrect parameters
    {
      Assert.must(context != null,
          "Attempt to pass NULL context into GoToAction.run()");
      Assert.must(object != null,
          "Attempt to pass NULL object into GoToAction.run()");
    }

    this.context = context;

    final Object target = unwrapTarget(object);
    if (!((target instanceof BinMember) 
        || (target instanceof BinLabeledStatement))) {
      return false;
    }


    List list = new ArrayList();

    String text = "Go to ";
    if(target instanceof BinMember) {
      text += ((BinMember)target).getNameWithAllOwners();
    } else if(target instanceof BinLabeledStatement) {
      text += ((BinLabeledStatement)target).getLabelIdentifierName();
    }
    text += " (declaration)";
    
    RitMenuItem item = new RitMenuItem(text, new Action(target));
    list.add(item);

    if(target instanceof BinMethod) {
      list.addAll(prepareChoicesFor((BinMethod)target));
    } else if(target instanceof BinVariable) {
      list.addAll(prepareChoicesFor((BinVariable)target));
    } else if(target instanceof BinCIType) {
      list.addAll(prepareChoicesFor((BinCIType)target));
    }

    RitMenuItem[] items = (RitMenuItem[])list.toArray(new RitMenuItem[list.size()]);
    RitDialog.getDialogFactory().showPopupMenu(context, items, false);

    // we never change anything
    return false;
  }

  public boolean isReadonly() {
    return true;
  }

  public Object unwrapTarget(Object target) {
    return RefactorItActionUtils.unwrapTargetIfNotConstructor(target);
  }

  protected void runImpl(Object target) {
    if(this.context == null) {
      return;
    }
    BinTreeTableNode node = new BinTreeTableNode(target);

    SourceHolder source = node.getSource();
    if (source != null && node.queryLineNumber() > 0) {
      context.show(source, node.queryLineNumber(),
          GlobalOptions.getOption("source.selection.highlight").equals("true")
          );
    } else {
      DialogManager.getInstance().showNonSourcePathItemInfo(
          context, getName(), target);
    }
  }


  private List prepareChoicesFor(BinCIType type) {
    List list = new ArrayList();
    BinTypeRef ref = type.getTypeRef().getSuperclass();
    if(ref != null && (ref.getBinCIType().isFromCompilationUnit())) {
      list.add(new RitMenuItem("Go to "
          + ref.getBinCIType().getNameWithAllOwners()
          + " (super type)", new Action(ref.getBinCIType())));
    }

    BinTypeRef[] ints = type.getTypeRef().getInterfaces();
    if (ints.length > 0) {
      list.add(new RitMenuItem("Go to "
          + ints[0].getBinCIType().getNameWithAllOwners()
          + " (super interface)", new Action(ints[0].getBinCIType())));
    }

    String fullyQualifiedName = type.getPackage().getQualifiedName() + "."
    + type.getNameWithAllOwners();
    boolean isInEjbdecls = RitEjbModule.isInEjbDeclarations(fullyQualifiedName);
    if(isInEjbdecls) {
      Map relatedEjbParts = RitEjbModule
              .getRelatedEjbParts(fullyQualifiedName);
      for (Iterator i = relatedEjbParts.entrySet().iterator(); i.hasNext();) {
        Map.Entry ejbPart = (Entry) i.next();
        list.add(new RitMenuItem("Go to "+ejbPart.getKey(),new Action(ejbPart.getValue())));
      }
    }
    return list;
  }

  private List prepareChoicesFor(BinVariable variable) {
    List list = new ArrayList();
    final BinType type = variable.getTypeRef().getBinType();
    if (type.getTypeRef().isReferenceType()) {
      BinTypeRef typeRef =  type.getTypeRef().getNonArrayType();
      if(typeRef.getCompilationUnit() != null) {
        list.add(new RitMenuItem("Go to type " + typeRef.getQualifiedName(), new Action(typeRef)));
      }
    }
    return list;
  }

  private List prepareChoicesFor(final BinMethod method) {
    List list = new ArrayList();

    List overrides = method.findAllOverrides();
    for(Iterator it = overrides.iterator(); it.hasNext(); ) {
      final BinMethod overridenMethod = (BinMethod)it.next();
      if( (overridenMethod.getOwner().getBinCIType().isClass()
            || overridenMethod.getOwner().getBinCIType().isEnum())) {
        String text = "Go to "
            + overridenMethod.getOwner().getBinCIType().getNameWithAllOwners() + "."
            + overridenMethod.getName() + " (super method)";
        RitMenuItem item = new RitMenuItem(text, new Action(overridenMethod));
        list.add(item);
      }
    }

    List hierachy = method.findAllOverridesOverriddenInHierarchy();
    hierachy.removeAll(overrides);
    for(Iterator it = hierachy.iterator(); it.hasNext(); ) {
      final BinMethod overridenMethod = (BinMethod)it.next();
      if( (overridenMethod.getOwner().getBinCIType().isClass()
          || overridenMethod.getOwner().getBinCIType().isEnum())) {
        String text = "Go to "
          + overridenMethod.getOwner().getBinCIType().getNameWithAllOwners() + "."
          + overridenMethod.getName() + " (sub method)";
        RitMenuItem item = new RitMenuItem(text, new Action(overridenMethod));
        list.add(item);
      }
    }

    return list;
  }
}
