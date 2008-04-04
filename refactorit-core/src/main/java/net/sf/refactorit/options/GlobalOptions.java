/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.options;

import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.ui.OptionsChangeListener;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.options.DefaultOption;
import net.sf.refactorit.ui.options.Option;
import net.sf.refactorit.utils.SwingUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;



/*
 *	This class helps separate UI indedependent options  from UI dependent
 *  options, thus making possible to run tests what failed previously
 *  due lack of GUI (it's also useful if refactorit will have commandline interface)
 *
 */
public class GlobalOptions {
  public static final String TREE_FONT = "tree.font";
  public static final String JB_PRODUCTIVITY_COMP_MODE =
      "jbuilder_productivity_tool_compatibility_mode";
  public static final String JB_PRODUCTIVITY_COMP_MODE_INFO_DISPLAYED =
      "jbuilder_productivity_tool_compatibility_mode_info_displayed";
  public static final String REBUILD_AT_STARTUP =
      "rebuild.at.startup";

  public static final String configDir = RuntimePlatform.getConfigDir();
  private static String oldConfigDirToImportFrom = RuntimePlatform.
      getOldConfigDirToImportFrom();

  public static final String configFile = configDir +
      File.separator + "refactorit.cfg";
  public static final String shortcutsFile = configDir +
      File.separator + "shortcuts.cfg";

  private static Properties properties;
  private static Properties defaultProps;

  private static List optionChangeListeners = new LinkedList();

  public static final Option REBUILD_AT_STARTUP_OPT =
      new DefaultOption(GlobalOptions.REBUILD_AT_STARTUP, Boolean.class);

  private static File latestDir;

  public static final String REFACTORIT_NAME = "RefactorIT";

  private GlobalOptions() {}

  public static Properties getProperties() {
    if (properties == null) {
      loadOptions();
    }
    return properties;
  }

  public static void setProperties(Properties properties) {
    GlobalOptions.properties = properties;
  }

  public static String getOption(String name) {
      loadOptions();
    if (properties == null) {
    }
    return properties.getProperty(name);
  }

  public static String getDefaultOption(String name) {
    if (defaultProps == null) {
      loadOptions();
    }
    return defaultProps.getProperty(name);
  }

  public static String getOption(String name, String defaultValue) {
    if (properties == null) {
      loadOptions();
    }

    final String prop = properties.getProperty(name, defaultValue);
    return prop;
  }

  public static boolean getOptionAsBoolean(String name, boolean defaultValue) {
    return "true".equals(getOption(name, "" + defaultValue));
  }

  public static void setOption(String name, String value) {
    if (properties == null) {
      loadOptions();
    }

    if (value == null) {
      properties.remove(name);
    } else {
      properties.setProperty(name, value);
    }
    GlobalOptions.fireOptionChanged(name, value);
  }

  /**
   * Load UI options for current user.
   */
  public static void loadOptions() {
    convertOldOptions();
    moveOldFiles();

    try {
      if (properties == null) {
        // properties init
        properties = new Properties();

        File file = new File(configFile);

        try {
          InputStream in = new FileInputStream(file);
          try {
            properties.load(in);
          } finally {
            in.close();
          }
        } catch (FileNotFoundException e) {
        }
      }

      if (defaultProps == null) {
        defaultProps = new Properties();
        initializeProperties(defaultProps);
      }

      initializeProperties(properties);

    } catch (Throwable t) {
      System.err.println("Ignored exception:");
      t.printStackTrace();
    }
  }

  private static void convertOldOptions() {
    File newConfigFile = new File(configFile);
    if (newConfigFile.exists()) {
      return;
    }

    File oldConfigDir = new File(oldConfigDirToImportFrom);
    if (!oldConfigDir.exists()) {
      File newConfigDir = new File(configDir);
      if (!newConfigDir.exists()) {
        newConfigDir.mkdirs();
      }
      return;
    }

    File tempSafeLocationForOldConfigDir = new File(oldConfigDirToImportFrom
        + "_please_delete_me");
    oldConfigDir.renameTo(tempSafeLocationForOldConfigDir);

    File newConfigDir = new File(configDir);
    if (!newConfigDir.exists()) {
      newConfigDir.mkdirs();
    }
    tempSafeLocationForOldConfigDir.renameTo(newConfigDir);
  }

  /**
   * Move shortcuts.properties into new folder
   *
   * Must be called after convertOldOptions() !!!
   */
  private static void moveOldFiles() {
    String path = System.getProperty("user.home") +
        File.separator + "shortcuts.refactorit";
    File old = new File(path);
    if (!old.exists()) {
      return;
    }

    File file = new File(shortcutsFile);

    old.renameTo(file);
  }

  /**
   * Save UI options for current user.
   */
  public static void save() {
    if (GlobalOptions.getLastDirectory() != null) {
      properties.setProperty("lastpath",
          GlobalOptions.getLastDirectory().getAbsolutePath());
    }

    try {
      OutputStream out = new FileOutputStream(configFile);
      try {
        properties.store(out, null);
      } finally {
        out.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (RuntimePlatform.isMacOsX()) {
      RuntimePlatform.MacOsX.setRefactorITCreatorCode(new File(configFile));
    }
  }

  /** Examples: "ISO-8859-1", "Shift_JIS" */
  public static String getEncoding() {
    // Need to call "changeInto..." to avoid NPEs because sometimes propertis are emptied outside
    // this class
    return changeIntoDefaultIfNotSupported(getOption("character.encoding"));
  }

  public static void setEncoding(String encoding) {
    setOption("character.encoding", encoding);
  }

  private static String changeIntoDefaultIfNotSupported(final String encoding) {
    if (encoding == null || "".equals(encoding) || (!encodingSupported(encoding))) {
      return getSystemDefaultEncoding();
    } else {
      return encoding;
    }
  }

  public static String[] getKnownSupportedEncodings() {
    List result = new ArrayList(Arrays.asList(new String[] {
        "US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"}));

    if (!result.contains(getSystemDefaultEncoding())) {
      result.add(getSystemDefaultEncoding());
    }

    return (String[]) result.toArray(new String[result.size()]);
  }

  public static String getSystemDefaultEncoding() {
    InputStreamReader reader = new InputStreamReader(
        new ByteArrayInputStream(new byte[] {0}));
    return Charset.forName(reader.getEncoding()).name();
  }

  public static boolean encodingSupported(String encoding) {
    try {
      new String(new byte[] {}, encoding);
    } catch (UnsupportedEncodingException e) {
      return false;
    }

    return true;
  }

  public static void initializeProperties(Properties props) {
    String prop;

    // Set GUI dependent options when GUI present.
    if (SwingUtil.isGUI()) {
      initializeGUIProperties(props);
    }

    // FIXME in general this is a wrong place for initialization of style settings
    final FormatSettings.OneTrueBraceStyle oneTrueBraceStyle
        = new FormatSettings.OneTrueBraceStyle();

    prop = props.getProperty(FormatSettings.PROP_FORMAT_TAB_SIZE);
    if (prop == null) {
      prop = Integer.toString(oneTrueBraceStyle.getTabSize());
      props.setProperty(FormatSettings.PROP_FORMAT_TAB_SIZE, prop);
    }

    prop = props.getProperty(
        FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS);
    if (prop == null) {
      prop = oneTrueBraceStyle.isUseSpacesInPlaceOfTabs() ? "true" : "false";
      props.setProperty(FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS,
          prop);
    }

    prop = props.getProperty(FormatSettings.PROP_FORMAT_BLOCK_INDENT);
    if (prop == null) {
      prop = Integer.toString(oneTrueBraceStyle.getBlockIndent());
      props.setProperty(FormatSettings.PROP_FORMAT_BLOCK_INDENT, prop);
    }

    prop = props.getProperty(FormatSettings.PROP_FORMAT_CONTINUATION_INDENT);
    if (prop == null) {
      prop = Integer.toString(oneTrueBraceStyle.getContinuationIndent());
      props.setProperty(FormatSettings.PROP_FORMAT_CONTINUATION_INDENT, prop);
    }

    prop = props.getProperty(FormatSettings.PROP_FORMAT_BRACE_INDENT);
    if (prop == null) {
      prop = Integer.toString(oneTrueBraceStyle.getBraceIndent());
      props.setProperty(FormatSettings.PROP_FORMAT_BRACE_INDENT, prop);
    }

    prop = props.getProperty(FormatSettings.PROP_FORMAT_NEWLINE_BEFORE_BRACE);
    if (prop == null) {
      prop = oneTrueBraceStyle.isNewlineBeforeBrace()
          ? "true" : "false";
      props.setProperty(FormatSettings.PROP_FORMAT_NEWLINE_BEFORE_BRACE, prop);
    }

    prop = props.getProperty(FormatSettings.
        PROP_FORMAT_SPACE_BEFORE_PARENTHESIS);
    if (prop == null) {
      prop = oneTrueBraceStyle.isSpaceBeforeParenthesis()
          ? "true" : "false";
      props.setProperty(FormatSettings.PROP_FORMAT_SPACE_BEFORE_PARENTHESIS,
          prop);
    }

    prop = props.getProperty("source.selection.highlight");
    if (prop == null) {
      prop = "true";
      props.setProperty("source.selection.highlight", prop);
    }

    prop = props.getProperty("source.highlight.color");
    if (prop == null) {
      prop = "#ffff00";
      props.setProperty("source.highlight.color", prop);
    }

    prop = props.getProperty("menu.debug");
    if (prop == null) {
      prop = "false";
      props.setProperty("menu.debug", prop);
    }

    prop = props.getProperty("performance.incremental.compiling");
    if (prop == null || "all".equals(prop)) {
      prop = "true";
      props.setProperty("performance.incremental.compiling", prop);
    }

    prop = props.getProperty(REBUILD_AT_STARTUP);
    if (prop == null) {
      prop = "false";
      props.setProperty(REBUILD_AT_STARTUP, prop);
    }

    prop = props.getProperty("performance.rebuild.activation");
    if (prop == null) {
      prop = "false";
      props.setProperty("performance.rebuild.activation", prop);
    }

    prop = props.getProperty("performance.rebuild.use-ide-events");
    if (prop == null) {
      prop = "true";
      props.setProperty("performance.rebuild.use-ide-events", prop);
    }

    prop = props.getProperty("separator.decimal");
    if (prop == null) {
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      prop = "" + dfs.getDecimalSeparator();
      props.setProperty("separator.decimal", prop);
    }

    prop = props.getProperty("separator.grouping");
    if (prop == null) {
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      prop = "" + dfs.getGroupingSeparator();
      props.setProperty("separator.grouping", prop);
    }

    prop = props.getProperty("version.control.dir.list");
    if (prop == null) {
      prop = "CVS;RCS;rcs;SCCS;.dependency-info";
      props.setProperty("version.control.dir.list", prop);
    }

    prop = props.getProperty("version.control.enabled");
    if (prop == null) {
      prop = "true";
      props.setProperty("version.control.enabled", prop);
    }

    prop = props.getProperty("version.control.verbose");
    if (prop == null) {
      prop = "false";
      props.setProperty("version.control.verbose", prop);
    }

    prop = props.getProperty("character.encoding");
    props.setProperty("character.encoding",
        changeIntoDefaultIfNotSupported(prop));

    prop = props.getProperty("debug.profileOnLoading");
    if (prop == null) {
      prop = "false";
      props.setProperty("debug.profileOnLoading", prop);
    }

    prop = props.getProperty("debug.checkIntegrityAfterLoad");
    if (prop == null) {
      prop = "false";
      props.setProperty("debug.checkIntegrityAfterLoad", prop);
    }

    prop = props.getProperty("module.type.javadoc");
    if (prop == null) {
      prop = "true";
      props.setProperty("module.type.javadoc", prop);
    }

    prop = props.getProperty("misc.show.fixme.dialog");
    if (prop == null) {
      prop = "true";
      props.setProperty("misc.show.fixme.dialog", prop);
    }
    prop = props.getProperty("show.disabled.actions");
    if (prop == null) {
      prop = "true";
      props.setProperty("show.disabled.actions", prop);
    }
    prop = props.getProperty("rebuild.project.options.change");
    if (prop == null) {
      prop = "true";
      props.setProperty("rebuild.project.options.change", prop);
    }

    prop = props.getProperty("misc.registration.server2");
    if (prop == null) {
      prop = "http://activation.refactorit.com/ipay/keychecker/";
      props.setProperty("misc.registration.server2", prop);
    }
  }

  public static void initializeGUIProperties(Properties props) {
    String prop;
    // Tree fonts & colors
    prop = props.getProperty(TREE_FONT);
    if (prop == null) {
      prop = UIResources.getDefaultFont("Tree.font");
      props.setProperty(TREE_FONT, prop);
    }

    prop = props.getProperty("tree.background");
    if (prop == null) {
      prop = UIResources.getDefaultColor("Tree.background");
      props.setProperty("tree.background", prop);
    }

    prop = props.getProperty("tree.foreground");
    if (prop == null) {
      prop = UIResources.getDefaultColor("Tree.foreground");
      props.setProperty("tree.foreground", prop);
    }

    prop = props.getProperty("tree.selection.background");
    if (prop == null) {
      prop = UIResources.getDefaultColor("Tree.selectionBackground");
      props.setProperty("tree.selection.background", prop);
    }

    prop = props.getProperty("tree.selection.foreground");
    if (prop == null) {
      prop = UIResources.getDefaultColor("Tree.selectionForeground");
      props.setProperty("tree.selection.foreground", prop);
    }

    // Source editor fonts & colors
    prop = props.getProperty("source.font");
    if (prop == null) {
      prop = "Monospaced-12";
      props.setProperty("source.font", prop);
    }

    prop = props.getProperty("source.background");
    if (prop == null) {
      prop = UIResources.getDefaultColor("TextArea.background");
      props.setProperty("source.background", prop);
    }

    prop = props.getProperty("source.foreground");
    if (prop == null) {
      prop = UIResources.getDefaultColor("TextArea.foreground");
      props.setProperty("source.foreground", prop);
    }
  }

  public static void fireOptionsChanged() {
    for (int i = 0; i < optionChangeListeners.size(); i++) {
      OptionsChangeListener l
          = (OptionsChangeListener) optionChangeListeners.get(i);
      if (l != null) {
        l.optionsChanged();
      }
    }
  }

  public static void fireOptionChanged(String key, String newValue) {
    for (int i = 0; i < optionChangeListeners.size(); i++) {
      OptionsChangeListener l
          = (OptionsChangeListener) optionChangeListeners.get(i);
      if (l != null) {
        l.optionChanged(key, newValue);
      }
    }
  }

  public static void registerOptionChangeListener(OptionsChangeListener l) {
    if (l != null) {
      CollectionUtil.addNew(optionChangeListeners, l);
    }
  }

  public static void unregisterOptionChangeListener(OptionsChangeListener l) {
    optionChangeListeners.remove(l);
  }

  public static synchronized File getLastDirectory() {
    return latestDir;
  }

  public static synchronized void setLastDirectory( File file ) {
    if ( !file.isDirectory() ) {
      file = file.getParentFile();
    }

    latestDir = file;
  }
}
