/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit.rules.service;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.Referable;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.SinglePointVisitor;


/**
 *
 * @author  Arseni Grigorjev
 */
public class ServiceBinItemReferenceRule  extends AuditRule {
  public static final String NAME = "service_references";
  private BinCIType currentType;
  private SinglePointVisitor singlePoint = new SinglePointVisitor() {
    public void onEnter(Object item){
      if (item instanceof Referable){
        boolean oldCacheEnabled = BinItemReference.cacheEnabled;
        BinItemReference.cacheEnabled = false;
        BinItemReference ref = BinItemReference.create(item);
        Object restoredItem = ref.restore(getProject());
        if (restoredItem == null){
          addViolation(new NullItemViolation(currentType, ref, item));
        } else if (!item.equals(restoredItem)){
          addViolation(new WrongItemViolation(currentType, ref, item));
        }
        BinItemReference.cacheEnabled = oldCacheEnabled;
      }
    }
    
    public void onLeave(Object item){}
  };
  
  public void visit(BinCIType type){
    currentType = type;
    singlePoint.visit(type);
  }
  
}

class FailedToRestoreViolation extends SimpleViolation {
  FailedToRestoreViolation (BinCIType type, BinItemReference ref,
      Object item, String message){
    super(type.getTypeRef(), getAstFrom(item, type), message + ": ref="+ ref
        + ", item: " + ClassUtil.getShortClassName(item), null);
  }

  public static ASTImpl getAstFrom(Object item, BinCIType type) {
    ASTImpl result = null;
    if (item instanceof BinSourceConstruct){
      result = ((BinSourceConstruct) item).getCompoundAst();
    } else if (item instanceof BinMember){
      result = ((BinMember) item).getNameAstOrNull();
    }
    
    if (result == null){
      return type.getNameAstOrNull();
    }
    return result;
  }
}

class NullItemViolation extends FailedToRestoreViolation {
  NullItemViolation(BinCIType type, BinItemReference ref, Object item) {
    super(type, ref, item, "Restored null item");
  }
}

class WrongItemViolation extends FailedToRestoreViolation {
  WrongItemViolation(BinCIType type, BinItemReference ref, Object item) {
    super(type, ref, item, "Restored wrong item");
  }
}
