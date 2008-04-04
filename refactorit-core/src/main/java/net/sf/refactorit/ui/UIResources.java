/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.refactorings.createconstructor.CreateConstructor;
import net.sf.refactorit.refactorings.createmissing.CreateMissingMethodRefactoring;
import net.sf.refactorit.refactorings.delegate.AddDelegatesRefactoring;
import net.sf.refactorit.refactorings.delegate.OverrideMethodsRefactoring;
import net.sf.refactorit.refactorings.encapsulatefield.EncapsulateFields;
import net.sf.refactorit.refactorings.extract.ExtractMethod;
import net.sf.refactorit.refactorings.extractsuper.ExtractSuper;
import net.sf.refactorit.refactorings.factorymethod.FactoryMethod;
import net.sf.refactorit.refactorings.inlinemethod.InlineMethod;
import net.sf.refactorit.refactorings.inlinevariable.InlineVariable;
import net.sf.refactorit.refactorings.introducetemp.IntroduceTemp;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccess;
import net.sf.refactorit.refactorings.movemember.MoveMember;
import net.sf.refactorit.refactorings.movetype.MoveType;
import net.sf.refactorit.refactorings.promotetemptofield.PromoteTempToField;
import net.sf.refactorit.refactorings.pullpush.PullPush;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.refactorings.rename.RenameLocal;
import net.sf.refactorit.refactorings.rename.RenameMethod;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.refactorings.usesupertype.UseSuperTypeRefactoring;
import net.sf.refactorit.ui.options.DefaultOption;
import net.sf.refactorit.ui.options.Option;
import net.sf.refactorit.ui.options.PreviewOption;
import net.sf.refactorit.ui.options.WarningOption;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;


/**
 *
 * class for global ui related stuff, must be in refactorit.ui package!
 *
 * @version $Revision: 1.7 $ $Date: 2005/12/09 12:03:17 $
 *
 * FIXME Make this class free of side effects to UI thread
 */
public class UIResources {

  public static boolean startupDialogEnabled = true;

  public static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(UIResources.class);

  public static String getDefaultColor(String key) {
    Color c = UIManager.getColor(key);
    if (c == null) {
      return "#000000";
    }

    return "#" + Integer.toHexString(c.getRGB()).substring(2);
  }

  public static String getDefaultFont(String key) {
    //if ( RefactorItActions.getNBHome() != null ) {
    /*
     * We do not need to obtain
     * tree font settings from NetBeans directly
     * because NB takes tree fonts from UIManager
     * on every startup also
     */
    //} else if ( System.getProperty("jbuilder.home") != null ) {
    /*
     * The same situation with JBuilder. There is no settings
     * for its own trees also.
     */
    //}

    Font f = null;
    try {
      if (!(DialogManager.getInstance() instanceof NullDialogManager)) {
        f = UIManager.getFont(key);
      }
    } catch (RuntimeException e) {
      System.err.println("Couldn't get default font, could be no GUI!");
    } catch (Throwable e) {
      System.err.println("Couldn't get default font, could be no GUI!");
    }

    if (f == null) {
      return "Monospaced-12";
    }

    StringBuffer buf = new StringBuffer(f.getName());

    if (f.isBold()) {
      buf.append("-bold");
    }

    if (f.isItalic()) {
      buf.append("-italic");
    }

    buf.append('-').append(f.getSize());

    return buf.toString();
  }

  public static Option[] getWarningOption() {
    ResourceBundle bundle
        = ResourceUtil.getBundle(UIResources.class, "Warnings");

    ArrayList list = new ArrayList();
    Enumeration enumer = bundle.getKeys();
    while (enumer.hasMoreElements()) {
      String key = (String) enumer.nextElement();
      list.add(new WarningOption(key, bundle.getString(key), Boolean.class));
    }

    Collections.sort(list);

    return (WarningOption[]) list.toArray(new WarningOption[list.size()]);
  }

  public static Option[] getPreviewOption() {
    String[][] refactorings = { {InlineVariable.key, "Inline Temp"}
        , {InlineMethod.key, "Inline Method"}
        , {EncapsulateFields.key, "Encapsulate Field"}
        , {FactoryMethod.key, "Factory Method"}
        , {MinimizeAccess.key, "Minimize Access"}
        , {ChangeMethodSignatureRefactoring.key, "Change Method Signature"}
        , {MoveMember.key, "Move member"}
        , {UseSuperTypeRefactoring.key, "Use Supertype Where Possible"}
        , {OverrideMethodsRefactoring.key, "Override methods"}
        , {PromoteTempToField.key, "Promote Temp To Field"}
        , {AddDelegatesRefactoring.key, "Add Delegate Method"}
        , {CreateMissingMethodRefactoring.key, "Create Missing Method"}
        , {IntroduceTemp.key, "Introduce Explaining Variable"}
        , {ExtractMethod.key, "Extract Method"}
        , {InlineMethod.key, "Inline Method"}
        , {PullPush.key, "Pull Up / Push Down"}
        , {CreateConstructor.key, "Create Constructor"}
        , {MoveType.key, "Move Type"}
        , {ExtractSuper.key, "Extract Super"}
        //, {RenameMember.key, "Rename refactoring"}
        , {RenameField.key, "Rename Field"}
        , {RenameLocal.key, "Rename Local"}
        , {RenameMethod.key, "Rename Method"}
        , {RenameType.key, "Rename Type"}
        , {RenamePackage.key, "Rename Package"}
    };

    DefaultOption[] defaultOptions = new PreviewOption[refactorings.length];

    sortPreviewOptionKeys(refactorings);

    for (int i = 0; i < refactorings.length; i++) {
      defaultOptions[i] = new PreviewOption("preview." + refactorings[i][0],
          refactorings[i][1], Boolean.class);
    }

    return defaultOptions;
  }

  private static void sortPreviewOptionKeys(String[][] unsorted) {
    Arrays.sort(unsorted, new Comparator() {
      public int compare(Object obj1, Object obj2) {
        String[] str1 = (String[]) obj1;
        String[] str2 = (String[]) obj2;
        return str1[1].compareTo(str2[1]);
      }
    });
  }

  public static String getExitCommandCaption() {
    if (RuntimePlatform.isMacOsX()) {
      return "Quit RefactorIT";
    }

    return "Exit";
  }

  public static void disableStartupDialogIfNotYetShown() {
    startupDialogEnabled = false;
  }

  public static class CharacterEncoding {
    private String name;

    public CharacterEncoding(String name) {
      this.name = name;
    }

    public String toString() {
      return this.name;
    }

    public boolean equals(Object o) {
      if (o instanceof CharacterEncoding) {
        return this.name.equals(((CharacterEncoding) o).name);
      }

      return false;
    }

    public int hashCode() {
      return this.name.hashCode();
    }
  }


  public static void fireHidePopups() {
    for (int i = 0; i < hidePopupsListeners.size(); i++) {
      ((Runnable) hidePopupsListeners.get(i)).run();
    }
  }

  public static void addHidePopupsListener(Runnable r) {
    hidePopupsListeners.add(r);
  }

  private static List hidePopupsListeners = new ArrayList();

  public static ImageIcon getRefreshIcon() {
    return ResourceUtil.getIcon(UIResources.class, "Refresh.gif");
  }

  public static DecimalFormat createDecimalFormat() {
    DecimalFormat result = new DecimalFormat();
    result.setMinimumFractionDigits(1);
    result.setMaximumFractionDigits(3);
    result.setDecimalSeparatorAlwaysShown(true);

    result.setDecimalFormatSymbols(createDecimalFormatSymbols());

    return result;
  }

  public static DecimalFormatSymbols createDecimalFormatSymbols() {
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    String decSep = GlobalOptions.getOption("separator.decimal");
    if (decSep != null && decSep.length() > 0) {
      if (!decSep.equals("none")) {
        dfs.setDecimalSeparator(decSep.charAt(0));
      }
    }
    String groupSep = GlobalOptions.getOption("separator.grouping");
    if (groupSep != null && groupSep.length() > 0) {
      if (!groupSep.equals("none")) {
        dfs.setGroupingSeparator(groupSep.charAt(0));
      }
    }

    return dfs;
  }
}
