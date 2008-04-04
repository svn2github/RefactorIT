/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.license;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.parser.ErrorListener;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;


/**
 *
 *
 * @author Igor Malinin
 */
public class AboutTemplates {
  private static final Logger log = Logger.getLogger(AboutTemplates.class);

  // static utils
  private AboutTemplates() {}

  public static String getAboutTemplate() {
    return getTemplate("about");
  }

  public static String getImportTemplate() {
    return getTemplate("import");
  }

  public static String getIDEText() {
    if (IDEController.runningStandalone()) {
      return "stand-alone";
    }

    return "for " + IDEController.getInstance().getIdeName();
  }

  private static final ErrorListener errorListener = new ErrorListener() {
    boolean hadError = false;
    public void onError(final String message, final String fileName,
        final int line, final int column) {
      hadError = true;
    }

    public boolean hadErrors() {
      return hadError;
    }
  };

  private static String getTemplate(String name) {
    try {
      InputStream in = AboutDialog.class
          .getResourceAsStream("resources/" + name + ".html");
      try {
        InputStreamReader reader = new InputStreamReader(in, "ISO8859_1");
        StringWriter writer = new StringWriter(1024);
        char[] buf = new char[1024];
        while (true) {
          int n = reader.read(buf);
          if (n < 0) {
            break;
          }
          writer.write(buf, 0, n);
        }

        return writer.toString();
      } finally {
        in.close();
      }
    } catch (Exception e) {
      log.error("Error loading template", e);
    }

    return "";
  }
}
