/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.reports;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.ui.audit.AuditTreeTableModel;
import net.sf.refactorit.ui.audit.AuditTreeTableNode;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.sax.TransformerHandler;

import java.util.Iterator;
import java.util.List;


public class AuditsXMLExporter extends XMLExporter {

  public AuditsXMLExporter(String encoding, String dtd, boolean isIndentNeeded) {
    super(encoding, dtd, isIndentNeeded, "audit");
  }

  protected void processModelInfo(BinTreeTableModel mdl, TransformerHandler hd)
      throws SAXException {
    AuditTreeTableModel model = (AuditTreeTableModel)mdl;
    AuditRule[] rules = model.getAuditRules();

    atts.clear();
    hd.startElement("", "info", "info", atts);
    for (int i = 0; i < rules.length; i++) {
      processAuditRule(rules[i], hd);
    }
    hd.endElement("", "info", "info");
  }

  private void processAuditRule(AuditRule rule, TransformerHandler hd)
      throws SAXException {
    atts.clear();
    Element element = rule.getConfiguration();
    if(element != null) {
      element.normalize();
      processDomAttributes(element.getAttributes(), true);
    }
    processAuditRuleAttributes(rule, hd);

    hd.startElement("", "audit", "audit", atts);
    if(element != null) {
      processDomChildren(element.getChildNodes(), hd);
    }
    hd.endElement("", "audit", "audit");
  }

  private void processAuditRuleAttributes(AuditRule rule, TransformerHandler hd)
      throws SAXException {
    atts.addAttribute("", "id", "id", "CDATA", rule.getKey());
    atts.addAttribute("", "name", "name", "CDATA", rule.getAuditName());
    atts.addAttribute("", "priority", "priority", "CDATA", rule.getPriority().getName());
    atts.addAttribute("", "category", "category", "CDATA", rule.getCategoryName());
  }


  private void processDomElement(Node element, String elementName,
      TransformerHandler hd) throws SAXException {
    atts.clear();
    hd.startElement("", elementName, elementName, processDomAttributes(element.getAttributes(), false));
      processDomChildren(element.getChildNodes(), hd);
    hd.endElement("", elementName, elementName);
  }

  /**
   * @param map
   */
  private AttributesImpl processDomAttributes(final NamedNodeMap map, boolean skipRuleAttributes) {

    if (map == null) {
      return atts;
    }
    for (int i = 0; i < map.getLength(); i++) {
      Node node = map.item(i);

      String name = node.getNodeName();

      if(skipRuleAttributes) {
//      skipping attributes, what are defined in processAuditRuleAttributes()
        if(name.equals("id") || name.equals("name") || name.equals("priority") || name.equals("category")) {
          continue;
        }
      }

      String uri = node.getNamespaceURI();
      if (uri == null) {
        uri = "";
      }

      String localName = node.getLocalName();
      if (localName == null) {
        localName = name;
      }

      String type = atts.getType(node.getNodeType());
      if (type == null) {
        type = "CDATA";
      }

      String value = node.getNodeValue();
      if (value == null) {
        value = "";
      }

      atts.addAttribute(uri, localName, name, type, value);
    }

    return atts;
  }

  private void processDomChildren(NodeList childNodes, TransformerHandler hd)
      throws SAXException {
    for (int i = 0, max = childNodes.getLength(); i < max; i++) {
      Node node = (Node) childNodes.item(i);
      if(node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      processDomElement(node, node.getNodeName(), hd);
    }
  }

  protected void processNode(BinTreeTableNode treeNode,
      BinTreeTableModel model, TransformerHandler hd) throws SAXException {

    if (treeNode instanceof AuditTreeTableNode) {
      AuditTreeTableNode node = (AuditTreeTableNode) treeNode;
      RuleViolation violation = node.getRuleViolation();

      if (!violation.isSkipped()) {
        atts.clear();
        atts.addAttribute("", "audit", "audit", "CDATA", violation.getAuditRule()
            .getKey());
        atts.addAttribute("", "message", "message", "CDATA", violation.getMessage());
        atts.addAttribute("", "line", "line", "CDATA", new Integer(violation
            .getLine()).toString());
        atts.addAttribute("", "density", "density", "CDATA", new Float(violation
            .getDensity()).toString());
        hd.startElement("", "violation", "violation", atts);
          processCorrectiveActions(violation.getCorrectiveActions(), hd);
        hd.endElement("", "violation", "violation");
      }

    } else {
      String type = treeNode.getNameType(treeNode.getBin()).toLowerCase();
      String name = treeNode.getDisplayName();
      atts.clear();
      atts.addAttribute("", "name", "name", "CDATA", name);
      hd.startElement("", type, type, atts);

      List children = treeNode.getAllChildren();
      if (children.size() > 0 && children.get(0) instanceof AuditTreeTableNode) {
        //reached violations
        processChildren(treeNode, model, hd, false);
      } else {
        processChildren(treeNode, model, hd);
      }
      hd.endElement("", type, type);
    }
  }

  private void processCorrectiveActions(List correctiveActions, TransformerHandler hd) throws SAXException {
    for(Iterator it = correctiveActions.iterator(); it.hasNext();) {
      CorrectiveAction action = (CorrectiveAction)it.next();
      atts.clear();
      atts.addAttribute("", "id", "id", "CDATA", action.getKey());
      atts.addAttribute("", "name", "name", "CDATA", action.getName());
      hd.startElement("", "corrective-action", "corrective-action", atts);
      hd.endElement("", "corrective-action", "corrective-action");
    }
  }


}
