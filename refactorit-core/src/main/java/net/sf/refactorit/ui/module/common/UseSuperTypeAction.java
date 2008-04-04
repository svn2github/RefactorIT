/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.usesupertype.UseSuperTypeRefactoring;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.common.usesupertype.AbstractUseSupertypeInputDialog;
import net.sf.refactorit.ui.module.common.usesupertype.SingleUseSupertypeDialog;
import net.sf.refactorit.ui.module.common.usesupertype.UseSupertypeInputDialog;

import java.util.List;


/**
 *
 *
 * @author Tonis Vaga
 */
public class UseSuperTypeAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.useSuperType";
  public static final String NAME = "Use Supertype Where Possible";

  public UseSuperTypeAction() {
  }

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'U';
  }

  public String getKey() {
    return KEY;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean isReadonly() {
    return false;
  }

  public boolean isAvailableForType(Class type) {
    return UseSuperTypeRefactoring.isTargetItemClassSupported(type);
  }

  public Refactoring createRefactoring(RefactorItContext context, Object object) {
    object = RefactorItActionUtils.unwrapTarget(object);
    BinMember member=null;
    if ( object instanceof BinMember ) {
      member = (BinMember) object; 
    }

    Refactoring refactoring = new UseSuperTypeRefactoring(member, context);

    return refactoring;
  }

  public boolean readUserInput(Refactoring refactoring) {
    final UseSuperTypeRefactoring useSuperRefact = ((UseSuperTypeRefactoring) refactoring);

    AbstractUseSupertypeInputDialog dialog;

    boolean isSingleUsage = useSuperRefact.isTargetSingleUsage();
  
    if ( isSingleUsage ) {
      BinTypeRef []possibleSupertypes=useSuperRefact.getPossibleSupertypes();
     
      // this must be checked in check preconditions
      assert possibleSupertypes.length > 0;

      dialog = new SingleUseSupertypeDialog(refactoring.getContext(),
          useSuperRefact.getTargetItemDisplayName(), possibleSupertypes);
      
    } else  {
      dialog = new UseSupertypeInputDialog(
          refactoring.getContext(), getName(), useSuperRefact.getSupertype());
      
    }


    dialog.show();

    if (!dialog.isOkPressed()) {
      return false;
    }
    BinTypeRef selectedSuper = dialog.getSelectedSupertype();
    
    if ( selectedSuper == null ) {
      return false;
    }
    useSuperRefact.setSupertype(selectedSuper);

    
    if ( !isSingleUsage ) {
      List subtypes = ((UseSupertypeInputDialog)dialog).getSelectedSubtypes();
      
      if (subtypes == null) {
        return false;
      }
      useSuperRefact.setSubtypes(subtypes);
      
    }
  
    return true;
  }
}
