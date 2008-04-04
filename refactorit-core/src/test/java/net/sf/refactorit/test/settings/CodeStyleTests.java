/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.settings;

import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;


public class CodeStyleTests extends RefactoringTestCase {
  
  public CodeStyleTests(String testName) {
    super(testName);
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(CodeStyleTests.class);
    return suite;
  }
  
  public void test_loadDefaultCodeStyle() {
    //try {
    //String prefix = "defaultCodeStyle";
    //String suffix = ".xml";
    
    //File file = File.createTempFile(prefix, suffix);
    //file.deleteOnExit();
    //BufferedWriter out = new BufferedWriter(new FileWriter(file));
    //out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><nodddde><a></a></nodddde>");
    //out.close();

    //BufferedReader in = new BufferedReader(new FileReader(file));
    //System.out.println(in.readLine());
    
    
    //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //DocumentBuilder builder = factory.newDocumentBuilder();
    //Document document = builder.parse(file);
    //NodeList list = document.getElementsByTagName("nodddde");
    //for(int i = 0; i < list.getLength(); i++ ) {
      //Node o = list.item(i);
      //System.out.println("Node: " + o.getNodeName());
      //System.out.println("Node: "+ ((Element)o).getTagName());
      //Node o2 = o.getFirstChild();
      //if(o2 != null) System.out.println(o2.getNodeName());
    //}

    //Codestyle style = new Codestyle(file);
    //String = style.export(CodeStyle.XML_FORMAT);
    //} catch(IOException e) {
    //  assertTrue("Error occured while working with file: "
    //      + e.getStackTrace(), false);
    //} catch (ParserConfigurationException e) {
    //  e.printStackTrace();
    //} catch (SAXException e) {
    //  e.printStackTrace();
    //}
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.test.refactorings.RefactoringTestCase#getTemplate()
   */
  public String getTemplate() {
    // TODO Auto-generated method stub
    return null;
  }
}
