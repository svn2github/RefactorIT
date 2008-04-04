/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 *
 * @author vadim
 */
public final class InheritanceInterfaceNode extends BinTreeTableNode {

//  public InheritanceInterfaceNode(BinCIType type, BinCIType interf) {
  public InheritanceInterfaceNode(BinCIType type) {
    super(type);
//    super((interf == null) ? type : interf);

//    System.out.println(""); //innnnn
//    System.out.println("type:" + type); //innnnn
//    System.out.println("interf:" + interf); //innnnn

//    if (interf != null) {
    init(type);
//    }
  }

//  private void init(BinCIType type, BinCIType interf) {
  private void init(BinCIType type) {
    BinTypeRef[] interfaces = type.getTypeRef().getInterfaces();

    for (int i = 0; i < interfaces.length; i++) {
      addChild(new InheritanceInterfaceNode(interfaces[i].getBinCIType()));
    }

//    List subTypesOfInterf = interf.getTypeRef().getDirectSubclasses();
//
//    if (subTypesOfInterf.contains(type.getTypeRef())) {
//      addChild(new InheritanceInterfaceNode(type, null));
//    }
//    else {
//      List allSubTypesOfInterf = interf.getTypeRef().getAllSubclasses();
//      for (int i = 0, max = allSubTypesOfInterf.size(); i < max; i++) {
//        BinCIType subType = ((BinTypeRef)allSubTypesOfInterf.get(i)).getBinCIType();
//
//        if ((subType instanceof BinInterface) &&
//            (subType.getTypeRef().getAllSubclasses().contains(type.getTypeRef()))) {
//          addChild(new InheritanceInterfaceNode(type, subType));
//        }
//      }
//    }
  }
}
