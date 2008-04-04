/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.reports;

import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.query.notused.NotUsedTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

import java.util.Iterator;
import java.util.List;


public class NotUsedXMLExporter extends XMLExporter {

  public NotUsedXMLExporter(String encoding, String dtd, boolean isIndentNeeded) {
    super(encoding, dtd, isIndentNeeded, "not-used");
  }

  protected void processModelInfo(BinTreeTableModel mdl, TransformerHandler hd) throws SAXException {
    NotUsedTreeTableModel model = (NotUsedTreeTableModel)mdl;
    
    
    ExcludeFilterRule[] rules = model.getFilterRules();
    if (rules.length > 0) {
      atts.clear();
      hd.startElement("", "info", "info", atts);
      atts.clear();
      hd.startElement("", "exclude-filter-rules", "exclude-filter-rules", atts);
      
      for (int i = 0; i < rules.length; i++) {
        processExcludeFilterRule(hd, rules[i], i);
      }

      hd.endElement("", "exclude-filter-rules", "exclude-filter-rules");
      hd.endElement("", "info", "info");
    }
    
  }

  /**
   * @param hd
   * @param rules
   * @param i
   * @throws SAXException
   */
  private void processExcludeFilterRule(final TransformerHandler hd, final ExcludeFilterRule rule, final int i)
      throws SAXException {
    atts.clear();
    atts.addAttribute("", "id", "id", "CDATA", rule.getKey());
    atts.addAttribute("", "name", "name", "CDATA", rule.getName());
    hd.startElement("", "rule", "rule", atts);
      processExcludeFilterRuleOptions(hd, rule.getOptions());
    hd.endElement("", "rule", "rule");
  }

  /**
   * @param hd
   * @param map
   * @throws SAXException
   */
  private void processExcludeFilterRuleOptions(final TransformerHandler hd, final MultiValueMap map)
      throws SAXException {

    if(map.size() > 0) {
      for(Iterator it = map.keySet().iterator(); it.hasNext(); ) {
        String optionName = (String)it.next();
        List optionValueList = (List)map.get(optionName);
        for(Iterator it2 = optionValueList.iterator(); it2.hasNext(); ) {
          String optionValue = (String)it2.next();
          atts.clear();
          atts.addAttribute("", "name", "name", "CDATA", optionName);
          atts.addAttribute("", "value", "value", "CDATA", optionValue);
          hd.startElement("", "option", "option", atts);
          hd.endElement("", "option", "option");          
        }
      }
    }
  }

  protected void processNode(BinTreeTableNode node, BinTreeTableModel model, TransformerHandler hd) throws SAXException {
    String type = node.getNameType(node.getBin()).toLowerCase();
    String name = node.getDisplayName();
    atts.clear();
    atts.addAttribute("", "name", "name", "CDATA", name);
    hd.startElement("", type, type, atts);
      processNodes(model, hd, node.getAllChildren());
    hd.endElement("", type, type);
  }

  /**
   * overriden version of document processing
   * @param model
   * @param hd
   * @throws SAXException
   */
  protected void processDocument(BinTreeTableModel model, TransformerHandler hd)
      throws SAXException {
    processModelInfo(model, hd);
    
    if (model.getVisibleColumnsCount() > 0) {
      BinTreeTableNode root = (BinTreeTableNode) model.getRoot();
      List children = root.getChildren();
      for (Iterator it = children.iterator(); it.hasNext();) {
        BinTreeTableNode node = (BinTreeTableNode) it.next();
        if (node.getBin().equals(NotUsedTreeTableModel.WHOLE_TYPES)) {
          processWholeTypes(model, hd, node);
        } else if (node.getBin().equals(NotUsedTreeTableModel.SINGLE_MEMBERS)) {
          processSingleMembers(model, hd, node);
        }
      }
    }
  }

  /**
   * @param model
   * @param hd
   * @param node
   * @throws SAXException
   */
  private void processSingleMembers(final BinTreeTableModel model, final TransformerHandler hd, final BinTreeTableNode node)
      throws SAXException {
    atts.clear();
    hd.startElement("", "single-members", "single-members", atts);
      processNodes(model, hd, node.getAllChildren());
    hd.endElement("", "single-members", "single-members");
  }

  /**
   * @param model
   * @param hd
   * @param node
   * @throws SAXException
   */
  private void processWholeTypes(final BinTreeTableModel model, final TransformerHandler hd, final BinTreeTableNode node)
      throws SAXException {
    atts.clear();
    hd.startElement("", "whole-types", "whole-types", atts);
      processNodes(model, hd, node.getAllChildren());
    hd.endElement("", "whole-types", "whole-types");
  }
  
  
}
