/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.reports;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.cli.StringArrayArguments;
import net.sf.refactorit.cli.actions.Runner;
import net.sf.refactorit.test.TempFileCreator;
import net.sf.refactorit.test.Utils;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;


/**
 * @author RISTO A
 */
public class XMLReportGenerationTest extends TestCase {
  private Project project;

  public XMLReportGenerationTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bingo-example"));

    project.getProjectLoader().build();
  }

  protected void tearDown() {
    project = null;
  }

  public void testNotUsedXMLFile() {
    File tempFile = TempFileCreator.getInstance().createRootFile()
        .getFileOrNull();

    assertFalse(tempFile == null);

    Arguments cl = new StringArrayArguments(
        "-notused -format xml -output " + tempFile.getAbsolutePath());

    new Runner().runAction(project, cl);

    validateXMLFile(tempFile, "xsd/NotUsedReport.xsd");
  }

  public void testMetricsXMLFile() {
    File tempFile = TempFileCreator.getInstance().createRootFile()
        .getFileOrNull();

    assertFalse(tempFile == null);

    Arguments cl = new StringArrayArguments(
        "-metrics -format xml -output " + tempFile.getAbsolutePath());

    new Runner().runAction(project, cl);

    validateXMLFile(tempFile, "xsd/MetricsReport.xsd");
  }
  
  public void testAuditsXMLFile() {
    File tempFile = TempFileCreator.getInstance().createRootFile()
        .getFileOrNull();

    assertFalse(tempFile == null);

    Arguments cl = new StringArrayArguments(
        "-audit -format xml -output " + tempFile.getAbsolutePath());

    new Runner().runAction(project, cl);

    validateXMLFile(tempFile, "xsd/AuditsReport.xsd");
  }

  private void validateXMLFile(File tempFile, String xsdFile)
  throws FactoryConfigurationError {
    try {
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      saxParserFactory.setNamespaceAware(true);
      saxParserFactory.setValidating(true);

      SAXParser saxParser = saxParserFactory.newSAXParser();

      saxParser.setProperty(
          "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
          "http://www.w3.org/2001/XMLSchema");

      saxParser.setProperty(
          "http://java.sun.com/xml/jaxp/properties/schemaSource",
         XMLReportGenerationTest.class.getResource(xsdFile).toExternalForm());

      DefaultHandler handler = new DefaultHandler();

      XMLValidationErrorHandler errorHandler = new XMLValidationErrorHandler();

      XMLReader reader = saxParser.getXMLReader();
      reader.setContentHandler(handler);
      reader.setEntityResolver(handler);
      reader.setErrorHandler(errorHandler);
      reader.setDTDHandler(handler);

      reader.parse(new InputSource(new FileInputStream(tempFile)));

      assertFalse(errorHandler.toString(), errorHandler.hasErrors());
    } catch (SAXException exc) {
      assertFalse(exc.toString(), true);
    } catch (IOException e) {
      assertFalse(e.toString(), true);
    } catch (ParserConfigurationException e) {
      assertFalse(e.toString(), true);
    }
  }
}

class XMLValidationErrorHandler implements ErrorHandler, ErrorListener {
  public static class ValidationError {
    private String type;
    private Exception exception;

    private ValidationError(String type, Exception exception) {
      this.type = type;
      this.exception = exception;
    }

    private static Object createSAXError(SAXParseException exception) {
      return new ValidationError("SAX Error", exception);
    }

    private static Object createSAXFatalError(SAXParseException exception) {
      return new ValidationError("SAX Fatal Error", exception);
    }

    private static Object createSAXWarning(SAXParseException exception) {
      return new ValidationError("SAX Warning", exception);
    }

    private static Object createTransformerError(TransformerException exception) {
      return new ValidationError("Transformer Error", exception);
    }

    private static Object createTransformerFatalError(TransformerException exception) {
      return new ValidationError("Transformer Fatal Error", exception);
    }

    private static Object createTransformerWarning(TransformerException exception) {
      return new ValidationError("Transformer Warning", exception);
    }

    public String toString() {
      return type + ": " + exception.getMessage(); 
    }
  }

  private ArrayList list = new ArrayList();

  public void error(SAXParseException exception) throws SAXException {
    list.add(ValidationError.createSAXError(exception));
  }

  public void fatalError(SAXParseException exception) throws SAXException {
    list.add(ValidationError.createSAXFatalError(exception));
  }

  public void warning(SAXParseException exception) throws SAXException {
    list.add(ValidationError.createSAXWarning(exception));
  }

  public void error(TransformerException exception) throws TransformerException {
    list.add(ValidationError.createTransformerError(exception));
  }

  public void fatalError(TransformerException exception) throws TransformerException {
    list.add(ValidationError.createTransformerFatalError(exception));
  }

  public void warning(TransformerException exception) throws TransformerException {
    list.add(ValidationError.createTransformerWarning(exception));
  }

  public ValidationError[] getAllError() {
    return (ValidationError[]) list.toArray(new ValidationError[list.size()]);
  }

  public boolean hasErrors() {
    return list.size() > 0;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();

    XMLValidationErrorHandler.ValidationError[] errors = getAllError();
    for (int i = 0; i < errors.length; i++) {
      buffer.append("\n\r");
      buffer.append(errors[i].toString());
    }

    return buffer.toString();
  }
}
