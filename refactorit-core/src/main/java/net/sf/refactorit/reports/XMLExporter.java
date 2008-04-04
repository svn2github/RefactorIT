/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.reports;

import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;



public abstract class XMLExporter {
  private String encoding;
  private String dtd;
  private String isIndentNeeded;  
  private String rootName = "report";
  
  protected AttributesImpl atts = new AttributesImpl();
  protected String actionName;
  
  public XMLExporter(String encoding, String dtd, boolean isIndentNeeded, String actionName) {
    this.encoding = encoding;
    this.dtd = dtd;
    this.isIndentNeeded(isIndentNeeded);
    this.actionName = actionName;
  }
  
  private void isIndentNeeded(boolean isIndentNeeded) {
    this.isIndentNeeded = (isIndentNeeded == true ? "yes" : "no");
  }
  
  /**
   * Process specified model into a printWriter as an xml file. Outputs model information + model itself 
   * @param model
   * @param printer
   * @throws TransformerConfigurationException
   * @throws SAXException
   */
  public final void process(BinTreeTableModel model, PrintWriter printer, final Source source)
      throws TransformerConfigurationException, SAXException {
    TransformerHandler hd = startDocument(printer, source);
    processDocument(model, hd);
    endDocument(hd);
  }
  
  /**
   * default implementation of document processing
   * @param model
   * @param hd
   * @throws SAXException
   */
  protected void processDocument(BinTreeTableModel model, TransformerHandler hd) throws SAXException {
    processModelInfo(model, hd);
    BinTreeTableNode root = (BinTreeTableNode)model.getRoot();
    if(model.getVisibleColumnsCount() > 0) {
      processNodes(model, hd, root.getChildren());
    }
  }
  
  protected abstract void processModelInfo(final BinTreeTableModel model,
      TransformerHandler hd) throws SAXException;
  
  protected abstract void processNode(final BinTreeTableNode node, final BinTreeTableModel model,
      final TransformerHandler hd) throws SAXException;

  /**
   * @param printer
   * @return
   * @throws SAXException
   * @throws TransformerConfigurationException
   * @throws TransformerFactoryConfigurationError
   */
  private TransformerHandler startDocument(final PrintWriter printer, final Source source)
      throws SAXException, TransformerConfigurationException,
      TransformerFactoryConfigurationError {

    StreamResult streamResult = new StreamResult(printer);
    SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
        .newInstance();
    TransformerHandler hd = (source == null)
      ? tf.newTransformerHandler()
      : tf.newTransformerHandler(source);

    hd.setResult(streamResult);

    Transformer transformer = hd.getTransformer();
    transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
    if(dtd != null) {
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtd);
    }
    transformer.setOutputProperty(OutputKeys.INDENT, isIndentNeeded);
    
    hd.startDocument();

    String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
    atts.clear();
    atts.addAttribute("", "date", "date", "CDATA", currentDateTime);
    hd.startElement("", rootName, rootName, atts);
    
    return hd;
  }
  
  private void endDocument(final TransformerHandler hd) throws SAXException {
    hd.endElement("", rootName, rootName);
    hd.endDocument();
  }
  
  protected void processNodes(final BinTreeTableModel model,
      final TransformerHandler hd, final List nodes)
      throws SAXException {

    for (Iterator it = nodes.iterator(); it.hasNext();) {
      BinTreeTableNode node = (BinTreeTableNode) it.next();
      processNode(node, model, hd);
    }
  }

  protected final void processChildren(final BinTreeTableNode node,
      final BinTreeTableModel model, final TransformerHandler hd,
      boolean addChildrenTags) throws SAXException {
    List children = node.getAllChildren();
    if (children.size() > 0) 
    {
      atts.clear();
      if (addChildrenTags) 
      {
        hd.startElement("", "children", "children", atts);
        processNodes(model, hd, children);
        hd.endElement("", "children", "children");
      } 
      else 
      {
        processNodes(model, hd, children);
      }
    }
  }
  
  protected final void processChildren(final BinTreeTableNode node,
      final BinTreeTableModel model, final TransformerHandler hd)
      throws SAXException {
    processChildren(node, model, hd, true);
  }

}
