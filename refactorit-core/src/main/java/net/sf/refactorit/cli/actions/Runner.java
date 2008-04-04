/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli.actions;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.reports.ReportGenerator;
import net.sf.refactorit.reports.ReportGeneratorFactory;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;



/**
 * @author RISTO A
 */
public class Runner {
//  public void runAction(Arguments args, Project p) {
//    runAction(p, args.getModelBuilder(), args);
//  }

//  public void runAction(Project p, ModelBuilder modelBuilder,
//      TableFormat f) throws IOException {
//    //runAction(p, modelBuilder, f, "");
//  }

  public void runAction(Project p, Arguments args) {
    ModelBuilder action = args.getModelBuilder();
    BinTreeTableModel[] model = new BinTreeTableModel[] {action.populateModel(p)};
    try {
      if (args.isAuditAction()) {
        ReportGenerator gen = ReportGeneratorFactory.getAuditsReportGenerator();
        printOutput(gen, model, args);
      } else if (args.isMetricsAction()) {
        ReportGenerator gen = ReportGeneratorFactory
            .getMetricsReportGenerator();
        printOutput(gen, model, args);
      } else if (args.isNotUsedAction()) {
        ReportGenerator gen = ReportGeneratorFactory
            .getNotUsedReportGenerator();
        printOutput(gen, model, args);
      }

    } catch (TransformerConfigurationException e) {
      RuntimePlatform.console.print("Error occured! Unable to generate report: \n"
          + e);
    } catch (SAXException e) {
      RuntimePlatform.console.print("Error occured! Unable to generate report: \n"
          + e);
    }
  }

  private void printOutput(ReportGenerator gen, BinTreeTableModel[] model,
      Arguments args) throws TransformerConfigurationException, SAXException {
    String outputFile = args.getOutputFile();
    
    final OutputStream stream;
    if(outputFile.equals("")) {
      stream = RuntimePlatform.console;
    } else {
      try {
        stream = new FileOutputStream(new File(outputFile));
      } catch (IOException e) {
        RuntimePlatform.console.print("ERROR: Unable to write to file: "
            + outputFile);
        return;
      }
    }    
    
    if (args.isXmlFormat()) {
      gen.generateXml(model, stream);
    } else if (args.isHtmlFormat()) {
      gen.generateHtml(model, stream);
    } else if (args.isCommaSeparatedFormat()) {
      gen.generateCsv(model, stream);
    } else if (args.isTextFormat()) {
      gen.generatePlainText(model, stream);
    } else if (args.isUserDefinedFormat()) {
      gen.generateForUserDefinedXSL(model, stream, args.getFormat());
    }
  }

  public boolean supportsChosenFormat(Arguments choise) {
    return choise.getFormatType() != null;
  }

  // Statics

//  public static void printOutput(final String result, final String outputFile) {
//    if (outputFile.equals("")) {
//      RuntimePlatform.console.print(result);
//    } else {
//      try {
//        FileUtil.writeStringToFile(new File(outputFile), result);
//      } catch (RuntimeException e) {
//        RuntimePlatform.console.print("ERROR: Unable to write to file: "
//            + outputFile);
//      }
//    }
//  }
}
