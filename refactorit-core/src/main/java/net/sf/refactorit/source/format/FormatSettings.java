/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;


import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.options.GlobalOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class FormatSettings {

  // Properties
  public static final String PROP_FORMAT_TAB_SIZE = "source.tab-size";
  public static final String PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS
      = "source.format.useSpacesInPlaceOfTabs";
  public static final String PROP_SPACE_BEFORE_ASSIGNMENT =
      "source.format.spaceBeforeAssignment";

  public static final String PROP_FORMAT_BLOCK_INDENT
      = "source.format.blockIndent";
  public static final String PROP_FORMAT_CONTINUATION_INDENT
      = "source.format.continuationIndent";
  /** how to shift the opening brace if it is on a new line */
  public static final String PROP_FORMAT_BRACE_INDENT
      = "source.format.braceIndent";

  public static final String PROP_FORMAT_NEWLINE_BEFORE_BRACE
      = "source.format.newlineBeforeBrace";
  public static final String PROP_FORMAT_SPACE_BEFORE_PARENTHESIS
      = "source.format.spaceBeforeParenthesis";
  public static final String PROP_FORMAT_SPACES_NEAR_BRACES
      = "source.format.spacesNearBraces";

  public static final String PROP_FORMAT_LATEST_USED_STYLE
      = "source.format.latestUsedStyle";

  public interface Style {
    String getName();

    String getKey();

    int getTabSize();

    boolean isUseSpacesInPlaceOfTabs();

    int getBlockIndent();

    int getContinuationIndent();

    int getBraceIndent();

    boolean isNewlineBeforeBrace();

    boolean isSpaceBeforeParenthesis();

    boolean isSpaceBeforeAssignment();

    boolean isUseSpacesNearBraces();

    boolean isSpaceAroudBinaryOperator();
  }

  public static boolean isSpaceAroudBinaryOperator() {
    return true;
  }

  /** Sun JDK is written with it */
  public static class OneTrueBraceStyle implements Style {
    public static final String KEY = "OneTrueBraceStyle";

    public String getName() {
      return "One True Brace Style (1TBS)";
    }

    public String getKey() {
      return KEY;
    }

    public int getTabSize() {
      return 4;
    }

    public boolean isUseSpacesInPlaceOfTabs() {
      return true;
    }

    public int getBlockIndent() {
      return 4;
    }

    public int getContinuationIndent() {
      return 4;
    }

    public int getBraceIndent() {
      return 0;
    }

    public boolean isNewlineBeforeBrace() {
      return false;
    }

    public boolean isSpaceBeforeParenthesis() {
      return false;
    }

    public boolean isSpaceBeforeAssignment() {
      return true;
    }

    public boolean isUseSpacesNearBraces() {
      return false;
    }

    public boolean isSpaceAroudBinaryOperator() {
      return true;
    }

  }


  public static class AllmanStyle extends OneTrueBraceStyle {
    public static final String KEY = "AllmanStyle";

    public String getName() {
      return "Allman Style";
    }

    public String getKey() {
      return KEY;
    }

    public boolean isNewlineBeforeBrace() {
      return true;
    }
  }


  public static class WhitesmithsStyle implements Style {
    public static final String KEY = "WhitesmithsStyle";

    public String getName() {
      return "Whitesmiths Style";
    }

    public String getKey() {
      return KEY;
    }

    public int getTabSize() {
      return 8;
    }

    public boolean isUseSpacesInPlaceOfTabs() {
      return true;
    }

    public int getBlockIndent() {
      return 8;
    }

    public int getContinuationIndent() {
      return 4;
    }

    public int getBraceIndent() {
      return 8;
    }

    public boolean isNewlineBeforeBrace() {
      return true;
    }

    public boolean isSpaceBeforeParenthesis() {
      return false;
    }

    public boolean isSpaceBeforeAssignment() {
      return true;
    }

    public boolean isUseSpacesNearBraces() {
      return true;
    }

    public boolean isSpaceAroudBinaryOperator() {
      return true;
    }

  }


  public static class GNUStyle implements Style {
    public static final String KEY = "GNUStyle";

    public String getName() {
      return "GNU Style";
    }

    public String getKey() {
      return KEY;
    }

    public int getTabSize() {
      return 4;
    }

    public boolean isUseSpacesInPlaceOfTabs() {
      return true;
    }

    public int getBlockIndent() {
      return 4;
    }

    public int getContinuationIndent() {
      return 4;
    }

    public int getBraceIndent() {
      return 2;
    }

    public boolean isNewlineBeforeBrace() {
      return true;
    }

    public boolean isSpaceBeforeParenthesis() {
      return false;
    }

    public boolean isSpaceBeforeAssignment() {
      return true;
    }

    public boolean isUseSpacesNearBraces() {
      return true;
    }

    public boolean isSpaceAroudBinaryOperator() {
      return true;
    }
  }


  public static class AqrisStyle implements Style {
    public static final String KEY = "AqrisStyle";

    public String getName() {
      return "Aqris Style";
    }

    public String getKey() {
      return KEY;
    }

    public int getTabSize() {
      return 2;
    }

    public boolean isUseSpacesInPlaceOfTabs() {
      return true;
    }

    public int getBlockIndent() {
      return 2;
    }

    public int getContinuationIndent() {
      return 4;
    }

    public int getBraceIndent() {
      return 0;
    }

    public boolean isNewlineBeforeBrace() {
      return false;
    }

    public boolean isSpaceBeforeParenthesis() {
      return false;
    }

    public boolean isSpaceBeforeAssignment() {
      return true;
    }

    public boolean isUseSpacesNearBraces() {
      return true;
    }

    public boolean isSpaceAroudBinaryOperator() {
      return true;
    }
  }


  /** For new source generation, detected and replaced to real one on write */
  public static final String LINEBREAK = "\r\n";

  private static Style latestStyleUsed = null;

  private static Style getStyle() {
    if (latestStyleUsed == null) {
      String key = GlobalOptions.getOption(PROP_FORMAT_LATEST_USED_STYLE);
      if (key == null) {
        key = OneTrueBraceStyle.KEY;
        GlobalOptions.setOption(PROP_FORMAT_LATEST_USED_STYLE, key);
      }
      latestStyleUsed = getStyle(key);
    }

    return latestStyleUsed;
  }

  public static Style getStyle(String key) {
    Style style = (Style) getStyles().get(key);
    if (style == null) {
      style = new OneTrueBraceStyle();
    }

    return style;
  }

  private static Map getStyles() {
    Map styles = new HashMap();
    styles.put(OneTrueBraceStyle.KEY, new OneTrueBraceStyle());
    styles.put(AllmanStyle.KEY, new AllmanStyle());
    styles.put(AqrisStyle.KEY, new AqrisStyle());
    styles.put(WhitesmithsStyle.KEY, new WhitesmithsStyle());
    styles.put(GNUStyle.KEY, new GNUStyle());
    return styles;
  }

  public static String[] getStyleKeys() {
    final Set keys = getStyles().keySet();
    return (String[]) keys.toArray(new String[keys.size()]);
  }

  public static String[] getStyleNames() {
    final List styleNames = new ArrayList();
    final Iterator it = getStyles().values().iterator();
    while (it.hasNext()) {
      styleNames.add(((Style) it.next()).getName());
    }

    return (String[]) styleNames.toArray(new String[styleNames.size()]);
  }

  public static void applyStyle(Style style) {
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.toString(style.getTabSize()));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS, style.isUseSpacesInPlaceOfTabs() ? "true" : "false");
    GlobalOptions.setOption(FormatSettings.PROP_SPACE_BEFORE_ASSIGNMENT, style.isSpaceBeforeAssignment() ? "true" : "false");

    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BLOCK_INDENT, Integer.toString(style.getBlockIndent()));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_CONTINUATION_INDENT, Integer.toString(style.getContinuationIndent()));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BRACE_INDENT, Integer.toString(style.getBraceIndent()));

    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_NEWLINE_BEFORE_BRACE, style.isNewlineBeforeBrace() ? "true" : "false");
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_SPACE_BEFORE_PARENTHESIS, style.isSpaceBeforeParenthesis() ? "true" : "false");

    clearCache();
  }

  public static final void clearCache() {
    latestStyleUsed = null;
    for (int i = 0; i < indents.length; i++) {
      indents[i] = null;
    }
  }

  public static final int getTabSize() {
    int tab;
    String option = GlobalOptions.getOption(PROP_FORMAT_TAB_SIZE);
    if (option == null) {
      tab = getStyle().getTabSize();
      GlobalOptions.setOption(PROP_FORMAT_TAB_SIZE, Integer.toString(tab));
    } else {
      try {
        tab = Integer.parseInt(option);
      } catch (NumberFormatException e) {
        tab = getStyle().getTabSize();
      }
    }

    return tab;
  }

  public static final boolean isSpaceBeforeAssignment() {
    boolean result;
    String option = GlobalOptions.getOption(PROP_SPACE_BEFORE_ASSIGNMENT);
    if (option == null) {
      result = getStyle().isSpaceBeforeAssignment();
      GlobalOptions.setOption(PROP_SPACE_BEFORE_ASSIGNMENT, (result ? "true" : "false"));
    } else {
      try {
        result = Boolean.valueOf(option).booleanValue();
      } catch (NumberFormatException e) {
        result = getStyle().isSpaceBeforeAssignment();
      }
    }

    return result;
  }

  /** A convenience method */
  public static final String getSpaceBeforeAssignment() {
    return isSpaceBeforeAssignment() ? " " : "";
  }

  public static boolean isUseSpacesInPlaceOfTabs() {
    boolean result;
    String option = GlobalOptions.getOption(PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS);
    if (option == null) {
      result = getStyle().isUseSpacesInPlaceOfTabs();
      GlobalOptions.setOption(PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS, (result ? "true" : "false"));
    } else {
      try {
        result = Boolean.valueOf(option).booleanValue();
      } catch (NumberFormatException e) {
        result = getStyle().isUseSpacesInPlaceOfTabs();
      }
    }

    return result;
  }

  public static int getBlockIndent() {
    int tab;
    String option = GlobalOptions.getOption(PROP_FORMAT_BLOCK_INDENT);
    if (option == null) {
      tab = getStyle().getBlockIndent();
      GlobalOptions.setOption(PROP_FORMAT_BLOCK_INDENT, Integer.toString(tab));
    } else {
      try {
        tab = Integer.parseInt(option);
      } catch (NumberFormatException e) {
        tab = getStyle().getBlockIndent();
      }
    }

    return tab;
  }

  public static int getContinuationIndent() {
    int tab;
    String option = GlobalOptions.getOption(PROP_FORMAT_CONTINUATION_INDENT);
    if (option == null) {
      tab = getStyle().getContinuationIndent();
      GlobalOptions.setOption(PROP_FORMAT_CONTINUATION_INDENT, Integer.toString(tab));
    } else {
      try {
        tab = Integer.parseInt(option);
      } catch (NumberFormatException e) {
        tab = getStyle().getContinuationIndent();
      }
    }

    return tab;
  }

  public static int getBraceIndent() {
    int tab;
    String option = GlobalOptions.getOption(PROP_FORMAT_BRACE_INDENT);
    if (option == null) {
      tab = getStyle().getBraceIndent();
      GlobalOptions.setOption(PROP_FORMAT_BRACE_INDENT, Integer.toString(tab));
    } else {
      try {
        tab = Integer.parseInt(option);
      } catch (NumberFormatException e) {
        tab = getStyle().getBraceIndent();
      }
    }

    return tab;
  }

  private static final String[] indents = new String[24];

  public static final String getIndentString(final int indent) {
    final boolean useTabs = !isUseSpacesInPlaceOfTabs();
    final int tabSize = getTabSize();

    String result;
    if (indent >= indents.length || indents[indent] == null) {
      final StringBuffer buf = new StringBuffer(indent);
      for (int i = 0; i < indent; ) {
        if (useTabs && indent - i >= tabSize) {
          buf.append('\t');
          i += tabSize;
        } else {
          buf.append(' ');
          i++;
        }
      }

      result = buf.toString();
      if (indent < indents.length) {
        indents[indent] = result;
      }
    } else {
      result = indents[indent];
    }

    return result;
  }

  public static boolean isNewlineBeforeBrace() {
    boolean result;
    String option = GlobalOptions.getOption(PROP_FORMAT_NEWLINE_BEFORE_BRACE);
    if (option == null) {
      result = getStyle().isNewlineBeforeBrace();
      GlobalOptions.setOption(PROP_FORMAT_NEWLINE_BEFORE_BRACE, (result ? "true" : "false"));
    } else {
      try {
        result = Boolean.valueOf(option).booleanValue();
      } catch (NumberFormatException e) {
        result = getStyle().isNewlineBeforeBrace();
      }
    }

    return result;
  }

  public static boolean isSpaceBeforeParenthesis() {
    boolean result;
    String option = GlobalOptions.getOption(PROP_FORMAT_SPACES_NEAR_BRACES);
    if (option == null) {
      result = getStyle().isSpaceBeforeParenthesis();
      GlobalOptions.setOption(PROP_FORMAT_SPACES_NEAR_BRACES, (result ? "true" : "false"));
    } else {
      try {
        result = Boolean.valueOf(option).booleanValue();
      } catch (NumberFormatException e) {
        result = getStyle().isSpaceBeforeParenthesis();
      }
    }

    return result;
  }

  public static boolean isUseSpacesNearBraces() {
    boolean result;
    String option = GlobalOptions.getOption(PROP_FORMAT_SPACE_BEFORE_PARENTHESIS);
    if (option == null) {
      result = getStyle().isSpaceBeforeParenthesis();
      GlobalOptions.setOption(PROP_FORMAT_SPACE_BEFORE_PARENTHESIS, (result ? "true" : "false"));
    } else {
      try {
        result = Boolean.valueOf(option).booleanValue();
      } catch (NumberFormatException e) {
        result = getStyle().isSpaceBeforeParenthesis();
      }
    }

    return result;
  }

  public static String getIndentStringForChildrenOf(LocationAware parent) {
    if (parent == null) {
      return "";
    }
    return FormatSettings.getIndentString(
        parent.getIndent() + FormatSettings.getBlockIndent());
  }

  // for NetBeans
//  void method() {
//    DataObject data = null; //findDataObject(src.getSource());
//    if (data != null) {
//      EditorCookie ecookie = (EditorCookie) data.getCookie(EditorCookie.class);
//      if (ecookie != null) {
//        JEditorPane openPanes[] = ecookie.getOpenedPanes();
//        if (openPanes != null && openPanes.length > 0) {
//          Class kitClass = Utilities.getKitClass(openPanes[0]);
//          boolean highlightRow = SettingsUtil.getBoolean(kitClass,
//              ExtSettingsNames.HIGHLIGHT_CARET_ROW,
//              ExtSettingsDefaults.defaultHighlightCaretRow);
//        }
//      }
//    }
//
//  }

}
