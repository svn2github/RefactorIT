/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.standalone.JBrowserPanel;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.options.CustomOptionsTab;
import net.sf.refactorit.ui.options.DefaultOption;
import net.sf.refactorit.ui.options.DefaultOptions;
import net.sf.refactorit.ui.options.DefaultOptionsTab;
import net.sf.refactorit.ui.options.JOptionsDialog;
import net.sf.refactorit.ui.options.Option;
import net.sf.refactorit.ui.options.Options;
import net.sf.refactorit.ui.options.OptionsTab;
import net.sf.refactorit.ui.options.Separator;

import java.awt.Color;
import java.awt.Font;
import java.util.Properties;

/**
 *
 * OptionsAction - action dispaying RefactorIt options dialog
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.31 $ $Date: 2005/06/03 14:24:31 $
 */
public class OptionsAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.OptionsAction";

  public static final Options VIEW_OPTIONS =
      new DefaultOptions(new OptionsTab[] {
        new DefaultOptionsTab("tree", new Option[] {
            new DefaultOption("tree.font", Font.class),
            new DefaultOption("tree.background", Color.class),
            new DefaultOption("tree.foreground", Color.class),
            new DefaultOption("tree.selection.background", Color.class),
            new DefaultOption("tree.selection.foreground", Color.class),
        }),
        new DefaultOptionsTab("source", new Option[] {
            new DefaultOption("source.font", Font.class),
            new DefaultOption("source.background", Color.class),
            new DefaultOption("source.foreground", Color.class),
            new DefaultOption("source.selection.highlight", Boolean.class),
            new DefaultOption("source.highlight.color", Color.class),
            new DefaultOption(
                "character.encoding", UIResources.CharacterEncoding.class),
        }),
        new DefaultOptionsTab("source.format", new Option[] {
            new DefaultOption(
                FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.class),
            new DefaultOption(
                FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS,
                Boolean.class),
            new DefaultOption(
                FormatSettings.PROP_FORMAT_BLOCK_INDENT, Integer.class),
            new DefaultOption(
                FormatSettings.PROP_FORMAT_CONTINUATION_INDENT, Integer.class),
//            new DefaultOption(
//                FormatSettings.PROP_FORMAT_BRACE_INDENT, Integer.class),
            new DefaultOption(
                FormatSettings.PROP_FORMAT_NEWLINE_BEFORE_BRACE, Boolean.class),
            new DefaultOption(
                FormatSettings.PROP_FORMAT_SPACE_BEFORE_PARENTHESIS,
                Boolean.class),
            new DefaultOption(
                FormatSettings.PROP_SPACE_BEFORE_ASSIGNMENT, Boolean.class),
        }),
//        new DefaultOptionsTab("debug", new Option[]{
//          new DefaultOption("debug.profileOnLoading", Boolean.class),
//          new DefaultOption("debug.checkIntegrityAfterLoad", Boolean.class),
//        }),
        new DefaultOptionsTab("separator", new Option[] {
            new DefaultOption("separator.decimal", Separator.class),
            new DefaultOption("separator.grouping", Separator.class),
        }),
//        new DefaultOptionsTab("performance", new Option[] {
//            new DefaultOption(
//                "performance.incremental.compiling", Boolean.class),
//            new DefaultOption(
//                "performance.rebuild.activation", Boolean.class),
//
//            //IDE events switched off (temporarily?); see Project.java
////            new DefaultOption(
////                "performance.rebuild.use-ide-events", Boolean.class),
//
//            //Switched off (not used)
//            //REBUILD_AT_STARTUP
//        }),
        new DefaultOptionsTab("version.control", new Option[] {
            new DefaultOption("version.control.enabled", Boolean.class),
            //new DefaultOption("version.control.verbose", Boolean.class), // Useless at the moment
            new DefaultOption("version.control.dir.list", String.class)
        }),
        new DefaultOptionsTab("warnings", UIResources.getWarningOption()),

        new DefaultOptionsTab("preview", UIResources.getPreviewOption()),

        new DefaultOptionsTab("misc", new Option[] {
            new DefaultOption("misc.show.fixme.dialog", Boolean.class),
            new DefaultOption("show.disabled.actions", Boolean.class),
            new DefaultOption("rebuild.project.options.change", Boolean.class)
        })
    });

  public String getName() {
    return "RefactorIT Options";
  }

  public char getMnemonic() {
    return 'O';
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(IdeWindowContext context) {
    OptionsAction.runAction(context, null);
    return false;
  }

  /**
   * Runs RefactorIT Options dialog (these options apply to all projects).
   *
   * One of the parameters may be null, but not both.
   * @param browser browser
   */
  public static void runAction(IdeWindowContext context, JBrowserPanel browser) {
    Properties properties = GlobalOptions.getProperties();

    JOptionsDialog dialog = new JOptionsDialog(context, properties,
        OptionsAction.VIEW_OPTIONS, UIResources.resLocalizedStrings,
        "getStart.refactoritOptions", getOptionsDialogName());
    dialog.show();

    Properties prop = dialog.getProperties();

    if (prop == null) {
      return;
    }

    GlobalOptions.setProperties(prop);

    if (browser != null) {
      GlobalOptions.registerOptionChangeListener(browser);
    }

    GlobalOptions.fireOptionsChanged();

    GlobalOptions.save();
  }

  private static String getOptionsDialogName() {
    if (RuntimePlatform.isMacOsX()) {
      return "RefactorIT Preferences";
    }
    return "RefactorIT Options";
  }

  public static void addCustomOptionsTab(CustomOptionsTab tab) {
    VIEW_OPTIONS.addTab(tab);
  }
}
