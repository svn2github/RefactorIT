/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli;

import net.sf.refactorit.cli.actions.AuditModelBuilder;
import net.sf.refactorit.cli.actions.MetricsModelBuilder;
import net.sf.refactorit.cli.actions.ModelBuilder;
import net.sf.refactorit.cli.actions.NotUsedModelBuilder;
import net.sf.refactorit.ui.treetable.writer.CommaSeparatedTableFormat;
import net.sf.refactorit.ui.treetable.writer.HtmlTableFormat;
import net.sf.refactorit.ui.treetable.writer.PlainTextTableFormat;
import net.sf.refactorit.ui.treetable.writer.TableFormat;


/**
 * @author Risto
 */
public abstract class Arguments implements SupportedArguments {
  public static final String HTML = "html";
  public static final String COMMA_SEPARATED = "comma-separated";
  public static final String TEXT = "text";
  public static final String XML = "xml";
  public static final String XSL_EXT = ".xsl";

  public static final String WARNING = "WARNING: ";
  public static final String PARAMETER_IS_EMPTY = " parameter is empty";
  public static final String PARAMETER_IS_MISSING = " parameter is missing";

  // Data sources

  public abstract String getSourcepath();

  public abstract String getClasspath();

  public abstract String getFormat();

  public abstract String getOutputFile();

  public abstract String getProfile();

  public abstract boolean isNotUsedAction();

  public abstract boolean isMetricsAction();

  public abstract boolean isAuditAction();

  // Calculators -- rely on other methods

  public boolean isUserDefinedFormat() {
    return getFormat().endsWith(XSL_EXT);
  }

  public boolean isHtmlFormat() {
    return getFormat().equalsIgnoreCase(HTML);
  }

  public boolean isXmlFormat() {
    return getFormat().equalsIgnoreCase(XML);
  }

  public boolean isCommaSeparatedFormat() {
    return getFormat().equalsIgnoreCase(COMMA_SEPARATED);
  }

  public boolean isTextFormat() {
    return getFormat().equalsIgnoreCase(TEXT) ||
        getFormat().equalsIgnoreCase("");
  }

  protected abstract boolean hasParameter(int param);

  // A bit more "involved" calculation

  public String getWarning() {
    if (getSourcepath().equals("")) {
      return getWarningForMissingParam(SOURCEPATH);
    } else if (getClasspath().equals("")) {
      return getWarningForMissingParam(CLASSPATH);
    } else {
      return null;
    }
  }

  private String getWarningForMissingParam(final int param) {
    String paramNameMsg = WARNING + ARGS.get(param);

    if (hasParameter(param)) {
      return paramNameMsg + PARAMETER_IS_EMPTY;
    } else {
      return paramNameMsg + PARAMETER_IS_MISSING;
    }
  }


  public String getFormatType() {
    if(isTextFormat()) {
      return TEXT;
    } else if(isXmlFormat()) {
      return XML;
    } else if(isCommaSeparatedFormat()) {
      return COMMA_SEPARATED;
    } else if(isHtmlFormat()) {
      return HTML;
    } else if(isUserDefinedFormat()) {
      return XSL_EXT;
    } else {
      return null;
    }
  }
  /**
   * @deprecated. remove after not used.
   */
  public TableFormat getFormatInstance() {
    if (isTextFormat()) {
      return new PlainTextTableFormat();
    } else if (isHtmlFormat()) {
      return new HtmlTableFormat();
    } else if (isCommaSeparatedFormat()) {
      return new CommaSeparatedTableFormat();
    } else {
      return null;
    }
  }

  public ModelBuilder getModelBuilder() {
    if (isNotUsedAction()) {
      return new NotUsedModelBuilder();
    } else if (isMetricsAction()) {
      return new MetricsModelBuilder(getProfile());
    } else if (isAuditAction()) {
      return new AuditModelBuilder(getProfile());
    } else {
      return null;
    }
  }

  public String getUniqueID(){
    return Integer.toHexString(hashCode());
  }
}
