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
import net.sf.refactorit.classmodel.BinParameter;
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

public class RecursiveDeleteParameterModel extends BinTreeTableModel {

  private MultiValueMap map = new MultiValueMap();
  private MethodsInvocationsMap finder;
  private BinMethod method;
  private List overrides;

  public RecursiveDeleteParameterModel(ChangeMethodSignatureRefactoring ref,
      MethodsInvocationsMap finder, BinParameter binParameter) {
    super(new MethodsParDeleteCallsNode("Call Tree", null, MethodsParDeleteCallsNode.SYNTETIC, null, binParameter));
    this.finder = finder;
    this.method = ref.getChange().getMethod();

    overrides = ref.getChange().getOverridesOverridenHierarchy();
    ((MethodsParDeleteCallsNode)this.getRoot()).setShowCheckBox(false);

    buildTree(binParameter);
  }

  private void buildTree(BinParameter binParameter) {
    ParentTreeTableNode rootNode = (MethodsParDeleteCallsNode) getRoot();

    rootNode.addChild(constructTreeFor(method, binParameter));

    for (int i = 0; i < overrides.size(); i++) {
      if (overrides.get(i) != method) {
        rootNode.addChild(constructTreeFor((BinMethod)
            overrides.get(i), binParameter));
      }
    }
  }

  private MethodsParDeleteCallsNode constructTreeFor(BinMethod method,
      BinParameter binParameter) {

    MethodsParDeleteCallsNode root = new MethodsParDeleteCallsNode(method, finder,
        MethodsParDeleteCallsNode.OWNER, map, binParameter);

    MethodsParDeleteCallsNode parentNode = new MethodsParDeleteCallsNode("is called by", finder,
        MethodsParDeleteCallsNode.PARENT, map, binParameter);
    MethodsParDeleteCallsNode childNode = new MethodsParDeleteCallsNode("calls", finder,
        MethodsParDeleteCallsNode.CHILD, map, binParameter);

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
