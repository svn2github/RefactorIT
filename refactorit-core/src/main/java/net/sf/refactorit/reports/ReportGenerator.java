/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.reports;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.metrics.MetricsAction;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.utils.FileUtil;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;


public abstract class ReportGenerator {
    
  protected abstract String getToolTipText();
  protected abstract XMLExporter createExporter();
  protected abstract Source getXslForHtmlReport();
  protected abstract Source getXslForCsvReport();
  protected abstract Source getXslForPlainTextReport();
  
  public JButton getReportButton(
      final BinTreeTableModel[] model, final IdeWindowContext context, Object target
  ) {
    JButton report = createReportButton();
    report.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        boolean stop = true;
        boolean error = false;
        do {
          stop = true;

          try {
            onReportButtonClick(model, context);

          } catch (FileNotFoundException e) {
            String message = "Could not save the report. Save under a different name?";
            String title = "Report error";
            int response = RitDialog.showConfirmDialog(context, message, title, JOptionPane.OK_CANCEL_OPTION);
            if (response == JFileChooser.APPROVE_OPTION) {
              stop = false;
            }
          } catch (SAXException e) {
            Logger log = AppRegistry.getLogger(ReportGenerator.class);
            log.error(getClass().getName() + " failed to generate report.", e);
            error = true;
          } catch (TransformerConfigurationException e) {
            Logger log = AppRegistry.getLogger(ReportGenerator.class);
            log.error(getClass().getName() + " failed to generate report.", e);
            error = true;
          }
        } while (!stop);
        
        if(error) {
          String message = "Error occured while report generation. \nPlease send the refactorit.log file to support@refactorit.com";
          String title = "Error while creating the report";
          RitDialog.showMessageDialog(context, message, title, JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    return report;
  }

  public void onReportButtonClick(BinTreeTableModel[] model,
      IdeWindowContext context) throws FileNotFoundException, SAXException,
      SAXException, TransformerConfigurationException {

    JFileChooser chooser = createReportFormatChooser();
    int res = RitDialog.showFileDialog(context, chooser);
    if (res != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File file = chooser.getSelectedFile();

    GenericFileFilter filter = (GenericFileFilter) chooser.getFileFilter();
    if (!filter.isExtensionAccepted(file)) {
      file = new File(file.getAbsolutePath()
          + filter.getFirstAvailableExtension());
    }

    PrintWriter pw = null;
    FileOutputStream stream = new FileOutputStream(file);
    generate(model, file, stream);
  }
  /**
   * @param model
   * @param file
   * @param pw 
   * @throws FileNotFoundException 
   * @throws SAXException 
   * @throws TransformerConfigurationException 
   */
  public void generate(BinTreeTableModel[] model, File file, OutputStream stream) throws 
      TransformerConfigurationException, SAXException {
    String fileName = file.getName();
      if (FileUtil.isXmlFile(fileName)) {
        generateXml(model, stream);
      } else if (FileUtil.isHtmlFile(fileName)) {
        generateHtml(model, stream);
      } else if (FileUtil.isCsvFile(fileName)) {
        generateCsv(model, stream);
      } else if (FileUtil.isPlainTextFile(fileName)) {
        generatePlainText(model, stream);
      }
    }
  
  
  public void generateXml(BinTreeTableModel[] model, OutputStream stream)
      throws TransformerConfigurationException, SAXException {
    Source source = null;
    generate(model, stream, source);
  }

  public void generateHtml(BinTreeTableModel[] model, OutputStream stream)
      throws TransformerConfigurationException, SAXException {
    Source source = getXslForHtmlReport();
    generate(model, stream, source);
  }

  public void generateCsv(BinTreeTableModel[] model, OutputStream stream)
      throws TransformerConfigurationException, SAXException {
    Source source = getXslForCsvReport();
    generate(model, stream, source);
  }

  public void generatePlainText(BinTreeTableModel[] model, OutputStream stream)
      throws TransformerConfigurationException, SAXException {
    Source source = getXslForPlainTextReport();
    generate(model, stream, source);
  }
  
  public void generateForUserDefinedXSL(BinTreeTableModel[] model,
      OutputStream stream, String xslFile)
      throws TransformerConfigurationException, SAXException {
    File file = new File(xslFile);
    Source source = new StreamSource(file);
    generate(model, stream, source);
  }
  
  
  
  private void generate(BinTreeTableModel[] model, OutputStream stream, Source source)
      throws TransformerConfigurationException, SAXException {
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(stream);
      XMLExporter converter = createExporter();
      converter.process(model[0], pw, source);
    } finally {
      if (pw != null) {
        pw.close();
      }
    }
  }

  private JButton createReportButton() {
    JButton report = new JButton(ResourceUtil.getIcon(MetricsAction.class, "report.gif"));
    report.setMargin(new Insets(0, 0, 0, 0));
    report.setToolTipText(getToolTipText());
    return report;
  }

 
  private JFileChooser createReportFormatChooser() {
    final FileNameSavingFileChooser chooser = new FileNameSavingFileChooser(); 
    
    chooser.addChoosableFileFilter(new GenericFileFilter("XML files", 
        new String[] {FileUtil.XML_FILE_EXT}));
   
    chooser.addChoosableFileFilter(new GenericFileFilter("CSV files", 
        new String[] {FileUtil.CSV_FILE_EXT}));
    
    chooser.addChoosableFileFilter(new GenericFileFilter("Plain text files", 
        new String[] {FileUtil.TEXT_FILE_EXT}));
    
    chooser.addChoosableFileFilter(new GenericFileFilter("HTML files", 
        new String[] { FileUtil.HTML_FILE_EXT, FileUtil.HTM_FILE_EXT }));
    
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    
    return chooser;
  }
  
  
//  
//  public JButton getAuditReportButton(
//      final AuditTreeTableModel model, final IdeWindowContext context, Object target
//  ) {
//    
//    String toolTipText = "tool tip text";
//    
//    JButton report = createReportButton();
//
//    report.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        ReportGenerator.onCopy2HtmlClick(model, context);
//      }
//    });
//
//    return report;
//  }
//  
//  
//  
//  
//  
//  
//
//  public static void onCopy2HtmlClick(
//      AuditTreeTableModel model, IdeWindowContext context) {
//    JFileChooser chooser = new JFileChooser();
//    chooser.setFileFilter(new FileFilter() {
//      public boolean accept(File f) {
//        if (f.isDirectory()) {
//          return true;
//        }
//        
//        String fileExt = FileUtil.getExtension(f);
//        if (fileExt.equals("xml")) {
//          return true;
//        }
//        
//        return false;
//      }
//
//      public String getDescription() {
//        return "XML files";
//      }
//    });
//    
//    chooser.setFileFilter(new FileFilter() {
//      public boolean accept(File f) {
//        if (f.isDirectory()) {
//          return true;
//        }
//        
//        String fileExt = FileUtil.getExtension(f);
//        if (fileExt.equals("htm") ||
//            fileExt.equals("html")) {
//          return true;
//        }
//        
//        return false;
//      }
//
//      public String getDescription() {
//        return "HTML files (default)";
//      }
//    });
//    
//    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
//    int res = RitDialog.showFileDialog(context, chooser);
//    if (res != JFileChooser.APPROVE_OPTION) {
//      return;
//    }
//
//    File file = chooser.getSelectedFile();
//    String fileExt = FileUtil.getExtension(file);
//    if (!(fileExt.equals("htm") || fileExt.equals("html") || fileExt.equals("xml"))) {
//      file = new File(file.getAbsolutePath() + ".html");
//    }
//      
//    PrintWriter pw = null;
//    try {
//      pw = new PrintWriter(new FileOutputStream(file));
//      
//      if(fileExt.equals("xml")) {
//        XMLExporter converter = new AuditsXMLExporter(GlobalOptions.getEncoding(), null, true);
//        converter.process(model, pw);
//      } else {
//      //  pw.print(TableLayout.getClipboardText(new HtmlTableFormat(), model));
//      }
//    } catch (IOException ignore) {
//    } catch (TransformerConfigurationException e) {
//      e.printStackTrace();
//    } catch (SAXException e) {
//      e.printStackTrace();
//    } finally {
//      if (pw != null) {
//        pw.close();
//      }
//    }
//  }
}
