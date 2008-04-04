/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.options.CustomOptionsTab;
import net.sf.refactorit.ui.options.DefaultOption;
import net.sf.refactorit.ui.options.Option;
import net.sf.refactorit.ui.options.Shortcut;

import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * Base class for the Standalone/JB/NB/JDEV shortcuts support.
 * Keeps shortcuts properties.
 *
 * @author Vladislav Vislogubov
 */
public abstract class Shortcuts implements CustomOptionsTab {
  public static final String delimiter = " ";

  private static Properties originalProperty;
  private static Properties cloneProperty;
  private static Map actionsByStroke = new TreeMap(new Comparator() {
    public int compare(Object o1, Object o2) {
      KeyStroke k1 = (KeyStroke) o1;
      KeyStroke k2 = (KeyStroke) o2;

      int s1 = k1.getModifiers();
      int s2 = k2.getModifiers();

      if (s1 != s2) {
        return s1 - s2;
      }

      s1 = k1.getKeyCode();
      s2 = k2.getKeyCode();

      return s1 - s2;
    }
  });

  private static Map shortcutsByKey = new TreeMap();

  private String ideKey;

  private ArrayList options = new ArrayList();

  // neccessary for the control of changes
  private LinkedList changes = new LinkedList();

  /**
   * @param ideKey Some identifier of IDE. Can be ".jbuilder", ".netbeans",
   * ".jdev", ".standalone".
   *
   * This param is necessary for the finding of value from the properties,
   * as this property is one for all IDEs.
   */
  public Shortcuts(String ideKey) {
    this.ideKey = ideKey;

    if (originalProperty == null) {
      // we have one properties for all IDEs.
      // so if it is opened lets skip the init.

      originalProperty = new Properties();
      cloneProperty = new Properties();

      loadProperties();
    }

    cloneProperty = (Properties) originalProperty.clone();
  }

  public String getName() {
    // Do not change this name. It is a key for the bundle.
    // Real name will be obtained from LocalizedStrings.properties.
    return "shortcuts";
  }

  public Option getVisibleOption(int index) {
    return (Option) options.get(index);
  }

  public int getVisibleOptionsCount() {
    return options.size();
  }

  public void setDefault() {
    //clear all

    Iterator it = actionsByStroke.keySet().iterator();
    while (it.hasNext()) {
      KeyStroke ks = (KeyStroke) it.next();
      unregister(ks);
    }

    Properties backup = (Properties) originalProperty.clone();
    originalProperty = new Properties();

    changes.clear();
    options.clear();
    actionsByStroke.clear();
    shortcutsByKey.clear();

    registerAll();

    it = actionsByStroke.keySet().iterator();
    while (it.hasNext()) {
      KeyStroke ks = (KeyStroke) it.next();
      changes.add(ks);
    }
    originalProperty = backup;
  }

  public void save() {
    originalProperty = (Properties) cloneProperty.clone();
    saveProperties();
    changes.clear();
  }

  /**
   * this method is called  when user press cancel button or
   * close JOptionDialog
   */
  public void cancel() {
    cloneProperty = (Properties) originalProperty.clone();

    Iterator iter = changes.iterator();
    while (iter.hasNext()) {
      unregister((KeyStroke) iter.next());
    }

    if (changes.size() != 0) {
      changes.clear();
      options.clear();
      actionsByStroke.clear();
      shortcutsByKey.clear();

      registerAll();
    }
  }

  public Object getValue(String key) {
    Object obj = shortcutsByKey.get(key);
    if (obj != null) {
      return obj;
    }

    String text = cloneProperty.getProperty(key + ideKey);
    Shortcut sc = new Shortcut(getKeyStrokeFromText(text), text);

    shortcutsByKey.put(key, sc);

    return sc;
  }

  public void setValue(String key, Object value) {
    Shortcut sc = (Shortcut) value;

    KeyStroke ksOld = ((Shortcut) getValue(key)).getStroke();
    if (ksOld != null) {
      unregister(ksOld);
      changes.remove(ksOld);
      actionsByStroke.remove(ksOld);
    }

    KeyStroke ksNew = sc.getStroke();
    if (ksNew != null) {
      RefactorItAction act = getAction(key);
      register(act, key, ksNew);

      if (!changes.contains(ksNew)) {
        changes.add(ksNew);

      }
      actionsByStroke.put(ksNew, act);
    }

    shortcutsByKey.put(key, sc);
    cloneProperty.setProperty(key + ideKey, sc.getStrokeText());
  }

  /**
   * Checks whether this stroke is busy
   */
  public abstract boolean isBusy(KeyStroke stroke);

  /**
   * Binds this stroke.
   *
   * @param action can be null if actionKey is some other action then
   * in ModuleManager. Example: RefactorIT Options shortcut.
   */
  public abstract void register(ShortcutAction action, String actionKey,
      KeyStroke stroke);

  public abstract void unregister(KeyStroke stroke);

  private KeyStroke getKeyStroke(String key, ShortcutAction action, String name,
      KeyStroke defaultStroke) {
    options.add(new DefaultOption(key, name, Shortcut.class));

    String st = originalProperty.getProperty(key + ideKey);
    if (st == null) {
      if (defaultStroke == null || isBusy(defaultStroke)) {
        originalProperty.setProperty(key + ideKey, "");
        cloneProperty.setProperty(key + ideKey, "");
        shortcutsByKey.put(key, new Shortcut(null, "", ""));
        return null;
      }

      String text = getTextFromKeyStroke(defaultStroke.getKeyCode(),
          defaultStroke.getModifiers());

      originalProperty.setProperty(key + ideKey, text);
      cloneProperty.setProperty(key + ideKey, text);

      actionsByStroke.put(defaultStroke, action);
      shortcutsByKey.put(key, new Shortcut(defaultStroke, text));
      return defaultStroke;
    }

    KeyStroke ks = getKeyStrokeFromText(st);
    if (ks != null) {
      actionsByStroke.put(ks, action);

    }
    shortcutsByKey.put(key, new Shortcut(ks, st));

    return ks;
  }

  public static void setHack() {
    try {
      originalProperty.setProperty("refactorit.action.SuperAction.standalone",
          "650 73 Ctrl+Alt+I");
    } catch (Exception e) {
      // ignore
    }
  }

  public static boolean isHack() {
    try {
      return originalProperty.getProperty(
          "refactorit.action.SuperAction.standalone", "").length() > 0;
    } catch (Exception e) {
      return false;
    }
  }

  private void loadProperties() {
    try {
      File file = new File(GlobalOptions.shortcutsFile);
      file.createNewFile();

      InputStream in = new FileInputStream(file);
      try {
        originalProperty.load(in);
      } finally {
        in.close();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void saveProperties() {
    try {
      OutputStream out = new FileOutputStream(GlobalOptions.shortcutsFile);
      try {
        originalProperty.store(out, null);
      } finally {
        out.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isValid(String key, Object value) {
    if (value == null) {
      return false;
    }

    return!isBusy(((Shortcut) value).getStroke());
  }

  public static String getTextFromKeyStroke(int keyCode, int modifiers) {
    String text = "" + modifiers;
    if (text.length() == 0) {
      text = "" + keyCode;
    } else {
      text += delimiter + keyCode;
    }

    String text2 = KeyEvent.getKeyModifiersText(modifiers);
    if (text2.length() == 0) {
      text2 = KeyEvent.getKeyText(keyCode);
    } else {
      text2 += "+" + KeyEvent.getKeyText(keyCode);
    }

    return text + delimiter + text2;
  }

  public static KeyStroke getKeyStrokeFromText(String text) {
    if (text == null || text.length() == 0) {
      return null;
    }

    StringTokenizer tokenizer = new StringTokenizer(text, delimiter);

    String first = tokenizer.nextToken();
    if (tokenizer.countTokens() > 1) {
      String second = tokenizer.nextToken();

      return KeyStroke.getKeyStroke(Integer.parseInt(second),
          Integer.parseInt(first));
    }

    return KeyStroke.getKeyStroke(Integer.parseInt(first), 0);
  }

  protected static RefactorItAction getAction(final String actionKey) {
    if (actionKey == null) {
      return null;
    }

    Iterator it = ModuleManager.getAllActions().iterator();
    while (it.hasNext()) {
      RefactorItAction action = (RefactorItAction) it.next();
      if (actionKey.equals(action.getKey())) {
        return action;
      }
    }

    return null;
  }

  public static ShortcutAction getAction(KeyStroke stroke) {
    if (stroke == null) {
      return null;
    }

    return (ShortcutAction) actionsByStroke.get(stroke);
  }

  public static KeyStroke getKeyStrokeByAction(String actionKey) {
    Shortcut sc = (Shortcut) shortcutsByKey.get(actionKey);
    if (sc == null) {
      return null;
    }

    return sc.getStroke();
  }

  protected void registerAll() {

    ActionRepository rep = IDEController.getInstance().getActionRepository();
    Iterator it = rep.getShortcutActions().iterator();

    while (it.hasNext()) {
      ShortcutAction acts = (ShortcutAction) it.next();
      String key = acts.getKey();
      KeyStroke stroke = getKeyStroke(key, acts, acts.getName(),
          acts.getKeyStroke()
          );

      if (stroke != null) {
        register(acts, key, stroke);
      }
    }

    // get all registered modules
//        Iterator it = ModuleManager.getModules().iterator();
//        while ( it.hasNext() ) {
//            RefactorItModule module = (RefactorItModule) it.next();
//            RefactorItAction[] acts = module.getActions();
//            for( int i = 0; i < acts.length; i++ ) {
//                String key = acts[i].getKey();
//                KeyStroke stroke = getKeyStroke( key, acts[i], acts[i].getName(),
//                acts[i].getKeyStroke()
//                );
//
//                if ( stroke != null ) {
//                    register( acts[i], key, stroke );
//                }
//            }
//        }
    sortVisibleOptions();
  }

  protected void sortVisibleOptions() {
    Comparator comp = new Comparator() {
      public int compare(Object obj1, Object obj2) {
        DefaultOption opt1 = (DefaultOption) obj1;
        DefaultOption opt2 = (DefaultOption) obj2;
        return opt1.getValue().compareTo(opt2.getValue());

      }
    };
    java.util.Collections.sort(options, comp);
  }

  /*public void reload() {
      cloneProperty = (Properties)originalProperty.clone();

      Iterator iter = changes.iterator();
      while( iter.hasNext() ) {
          unregister( (KeyStroke)iter.next() );
      }

      changes.clear();
      options.clear();
      actionsByStroke.clear();
      shortcutsByKey.clear();

      registerAll();
       }*/
}
