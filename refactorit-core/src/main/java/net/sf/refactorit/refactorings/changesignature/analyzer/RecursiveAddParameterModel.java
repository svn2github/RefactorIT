/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature.analyzer;


import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.util.List;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Aqris AS</p>
 * @author Kirill Buhhalko
 * @version
 */

public class RecursiveAddParameterModel extends BinTreeTableModel {

  private MultiValueMap map = new MultiValueMap();
  private MethodsInvocationsMap finder;
  private BinMethod method;
  private List overrides;

  public RecursiveAddParameterModel(ChangeMethodSignatureRefactoring ref,
      MethodsInvocationsMap finder) {
    super(new MethodsCallsNode("Call Tree", null, MethodsCallsNode.SYNTETIC, null));
    this.finder = finder;
    this.method = ref.getChange().getMethod();

    overrides = ref.getChange().getOverridesOverridenHierarchy();
    ((MethodsCallsNode)this.getRoot()).setShowCheckBox(false);
    buildTree();

  }

  private void buildTree() {
    ParentTreeTableNode rootNode = (MethodsCallsNode) getRoot();

    rootNode.addChild(constructTreeFor(method));

    for (int i = 0; i < overrides.size(); i++) {
      if (overrides.get(i) != method) {
        rootNode.addChild(constructTreeFor((BinMethod)
            overrides.get(i)));
      }
    }
  }

  private MethodsCallsNode constructTreeFor(BinMethod method) {

    MethodsCallsNode root = new MethodsCallsNode(method, finder,
        MethodsCallsNode.OWNER, map);

    MethodsCallsNode parentNode = new MethodsCallsNode("is called by", finder,
        MethodsCallsNode.PARENT,
        map);
    MethodsCallsNode childNode = new MethodsCallsNode("calls", finder,
        MethodsCallsNode.CHILD, map);

    root.addChild(parentNode);
    root.addChild(childNode);
    root.setShowCheckBox(false);
    root.setSelected(true);

    return root;
  }

  public String getColumnName(final int column) {
    return "";
  }

  public Object getChild(final Object node, final int num) {
    return super.getChild(node, num);
  }

  public int getColumnCount() {
    return 1;
  }

  public boolean isLeaf() {
    return false;
  }

}
