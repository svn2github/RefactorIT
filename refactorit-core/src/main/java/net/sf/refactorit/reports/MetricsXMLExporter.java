/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.reports;

import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.metrics.MetricsModel;
import net.sf.refactorit.metrics.MetricsNode;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;


public final class MetricsXMLExporter extends XMLExporter {
  
  /**
   * @param encoding
   * @param dtd
   * @param isIndentNeeded
   * @param actionName (example - "metric", "audit", etc)
   */
  public MetricsXMLExporter(String encoding, String dtd, boolean isIndentNeeded) {
    super(encoding, dtd, isIndentNeeded, "metric");
  }  
  
  protected void processModelInfo(BinTreeTableModel mdl, TransformerHandler hd) throws SAXException {
    MetricsModel model = (MetricsModel)mdl;
    atts.clear();
    hd.startElement("", "info", "info", atts);
    for (int i = 1; i < model.getColumnCount(); i++) {
      if (model.isShowing(i)) {
        String name = model.getColumnName(i);
        String key = model.getKey(i);
        // FIXME: description shall be taken from localizedstring (the same way as it is done in GUI)
        String description = model.getKey(i);
        String min = Double.toString(model.getState().getMin(key));
        String max = Double.toString(model.getState().getMax(key));
        atts.clear();
        atts.addAttribute("", "name", "name", "CDATA", name);
        atts.addAttribute("", "description", "description", "CDATA", description);
        atts.addAttribute("", "lower-preffered-limit", "lower-preffered-limit", "CDATA", min);
        atts.addAttribute("", "upper-preffered-limit", "upper-preffered-limit", "CDATA", max);
        hd.startElement("", actionName, actionName, atts);
        hd.endElement("", actionName, actionName);
      }
    }
    hd.endElement("", "info", "info");
  }


  protected void processNode(BinTreeTableNode treeTableNode, BinTreeTableModel model,
      TransformerHandler hd) throws SAXException {
    MetricsNode node = (MetricsNode) treeTableNode;

    String type = node.getNameType(node.getBin()).toLowerCase();
    String name = node.getNameForTextOutput();
    atts.clear();
    atts.addAttribute("", "name", "name", "CDATA", name);
    hd.startElement("", type, type, atts);

    // metrics
    for (int i = 1; i < model.getColumnCount(); i++) {
      if (model.isShowing(i)) {
        Object value = model.getValueAt(node, i);
        if (value == null) {
          continue;
        } else {
          atts.clear();
          atts.addAttribute("", "name", "name", "CDATA", model.getColumnName(i));
          atts.addAttribute("", "value", "value", "CDATA", StringUtil
              .removeHtml(value.toString().toLowerCase()));
          hd.startElement("", actionName, actionName, atts);
          hd.endElement("", actionName, actionName);
        }
      }
    }

    processChildren(node, model, hd);
    
    hd.endElement("", type, type);
  }

}
