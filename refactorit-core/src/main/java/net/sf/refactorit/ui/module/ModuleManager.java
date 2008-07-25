/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.classmodel.AbstractLocationAware;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.edit.LineReader;
import net.sf.refactorit.ui.RuntimePlatform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * Module Manager. Is responsible for loading the module classes into VM so they
 * can be created later by newInstance(...) method and registered with
 * registerModule(RefactorItModule ...) method. Also provides functions for
 * retrieving the RefactorItActions supported by various RefactorIT modules.
 * 
 * @author Igor Malinin
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
public class ModuleManager {
  private static final String SERVICE = "META-INF/services/"
      + net.sf.refactorit.ui.module.RefactorItModule.class.getName();

  private static List modules = new ArrayList(10);

  private static boolean loaded;

  private static final ActionsSorter actionsSorter = new ActionsSorter();

  public static List getActions(Object[] bins) {
    if (bins.length == 0) {
      return Collections.EMPTY_LIST;
    }

    List list = getActions(bins[0]);

    if (bins.length == 1) {
      return list;
    }

    for (int i = 1; i < bins.length; i++) {
      list.retainAll(getActions(bins[i]));
    }

    for (Iterator i = list.iterator(); i.hasNext();) {
      RefactorItAction action = (RefactorItAction) i.next();

      if (!action.isMultiTargetsSupported()) {
        i.remove();
      }
    }

    Collections.sort(list, actionsSorter);

    return list;
  }

  public static List getActions(Object o) {
    List act = getActions(o.getClass());

    if (o instanceof AbstractLocationAware) {
      AbstractLocationAware item = (AbstractLocationAware) o;

      if (item.isPreprocessedSource()) {
        for (Iterator i = act.iterator(); i.hasNext();) {
          RefactorItAction action = (RefactorItAction) i.next();

          if (!action.isPreprocessedSourcesSupported(item)) {
            i.remove();
          }
        }
      }
    }

    return act;
  }

  public static List getActions(RunContext ctx) {
    List result = getActionUnion(ctx.getItemClasses());

    if (ctx.isCheckMultiTarget() && ctx.getItemClasses() != null
        && ctx.getItemClasses().length > 1) {
      filterMultiTargetSupported(result);
    }

    if (ctx.getContextType() == RunContext.JSP_CONTEXT) {
      filterJspSupportedActions(result, ctx.getItemClasses());
    }

    return result;
  }

  /**
   * Searches for a list of actions available for specified BinXXX class.
   * Queries all modules whether they support RefactorItActions for specified
   * BinXXX class and returns a list of all RefactorItActions supported by all
   * registered modules.
   * 
   * @param cls
   *          BinXXX class
   * @return List, a list object containing RefactorItAction's.
   */
  public static List getActions(Class cls) {
    List list = new ArrayList(4);
    if (cls == null) {
      return list;
    }

    // get all registered modules
    Iterator it = modules.iterator();
    while (it.hasNext()) {
      // get next module and query it whether it supports actions for
      // specified BinXXX class.
      RefactorItModule module = (RefactorItModule) it.next();
      final RefactorItAction actions[] = module.getActions();

      for (int i = 0; i < actions.length; i++) {
        if (actions[i].isAvailableForType(cls)) {
          list.add(actions[i]);
        }
      }
    }

    Collections.sort(list, actionsSorter);

    return list;
  }

  /**
   * Searches for a list of actions available for specified BinXXX class.
   * Queries all modules whether they support RefactorItActions for specified
   * BinXXX class and returns a list of all RefactorItActions supported by all
   * registered modules.
   * 
   * @param cls
   *          BinXXX class array
   * @param checkMultiTargetsSupport
   *          checkMultiTargetsSupport
   * @return a list containing RefactorItAction's.
   */
  public static List getActions(Class[] cls, boolean checkMultiTargetsSupport) {
    if (cls.length == 0) {
      return Collections.EMPTY_LIST;
    }

    List list = getActions(cls[0]);
    if (cls.length > 1) {
      for (int i = 1; i < cls.length; i++) {
        list.retainAll(getActions(cls[i]));
      }

      if (checkMultiTargetsSupport) {
        filterMultiTargetSupported(list);
      }

      Collections.sort(list, actionsSorter);
    }

    return list;
  }

  private static void filterMultiTargetSupported(final List list) {
    for (Iterator i = list.iterator(); i.hasNext();) {
      RefactorItAction action = (RefactorItAction) i.next();

      if (!action.isMultiTargetsSupported()) {
        i.remove();
      }
    }
  }

  /**
   * Finds actions that are available at least for one of the given classes.
   * 
   * @param cls
   *          classes
   * @return a list of actions
   */
  public static List getActionUnion(Class[] cls) {
    if (cls.length == 0) {
      return Collections.EMPTY_LIST;
    }

    Set actions = new TreeSet(actionsSorter);

    for (int i = 0; i < cls.length; i++) {
      actions.addAll(getActions(cls[i]));
    }

    return new ArrayList(actions);
  }

  public static CompilationUnit[] getCompilationUnits(Object target,
      RefactorItContext context) {
    if (target instanceof Object[]) {
      Object targets[] = (Object[]) target;

      CompilationUnit[][] results = new CompilationUnit[targets.length][];

      for (int i = 0; i < targets.length; ++i) {
        results[i] = getCompilationUnits(targets[i], context);
      }

      Set finalResult = new HashSet();

      for (int i = 0; i < targets.length; ++i) {
        finalResult.addAll(Arrays.asList(results[i]));
      }

      return (CompilationUnit[]) finalResult
          .toArray(new CompilationUnit[finalResult.size()]);
    }

    if (target instanceof CompilationUnit) {
      return new CompilationUnit[] {(CompilationUnit) target};
    }

    if (target instanceof Project) {
      ArrayList compilationUnits = ((Project) target).getCompilationUnits();

      return (CompilationUnit[]) compilationUnits
          .toArray(new CompilationUnit[compilationUnits.size()]);
    }

    if (target instanceof BinPackage) {
      BinPackage aPackage = (BinPackage) target;

      HashSet result = new HashSet();

      BinPackage packageList[] = context.getProject().getAllPackages();

      for (int i = 0; i < packageList.length; ++i) {
        BinPackage pkg = packageList[i];

        if (pkg.getQualifiedName().startsWith(aPackage.getQualifiedName())) {
          for (Iterator t = pkg.getAllTypes(); t.hasNext();) {
            BinCIType type = ((BinTypeRef) t.next()).getBinCIType();

            if (type.isFromCompilationUnit()) {
              result.add(type.getCompilationUnit());
            }
          }
        }
      }

      return (CompilationUnit[]) result
          .toArray(new CompilationUnit[result.size()]);
    }

    if (target instanceof BinCIType) {
      BinCIType type = (BinCIType) target;

      if (type.isFromCompilationUnit()) {
        return new CompilationUnit[] {type.getCompilationUnit()};
      }

      // FIXME should warn somehow probably?
      return new CompilationUnit[0];
    }

    Assert.must(false, "Invalid target: " + target);

    return null;
  }

  private static class ActionsSorter implements Comparator {
    public int compare(Object o1, Object o2) {
      try {
        return ((RefactorItAction) o1).getName().compareTo(
            ((RefactorItAction) o2).getName());
      } catch (Exception e) {
        System.err.println("strange objects: " + o1 + " - " + o2);
        System.err.println("strange objects: " + o1.getClass() + " - "
            + o2.getClass());
        return 0;
      }
    }
  }

  /**
   * Searches RefactorItAction by actionKey if there is one for particular class.
   * 
   * @param cls
   *          target class
   * @param actionKey
   *          action key
   * @return an action
   */
  public static RefactorItAction getAction(Class cls, String actionKey) {
    if (actionKey == null) {
      return null;
    }

    for (Iterator i = getActions(cls).iterator(); i.hasNext();) {
      RefactorItAction act = (RefactorItAction) i.next();

      if (actionKey.equals(act.getKey())) {
        return act;
      }
    }

    return null;
  }

  /**
   * Searches RefactorItAction by actionKey if there is one for particular
   * classes.
   * 
   * @param cls
   *          target classes
   * @param actionKey
   *          action key
   * @return an action
   */
  public static RefactorItAction getAction(Class[] cls, String actionKey) {
    return getAction(cls, actionKey, true);
  }

  public static RefactorItAction getAction(Class[] cls, String actionKey,
      boolean checkMultiTargetSupport) {
    if (actionKey == null) {
      return null;
    }

    List actions = getActions(cls, checkMultiTargetSupport);

    for (Iterator i = actions.iterator(); i.hasNext();) {
      RefactorItAction act = (RefactorItAction) i.next();

      if (actionKey.equals(act.getKey())) {
        return act;
      }
    }

    return null;
  }

  private static void loadModulesDevelTime(String moduleListFile) {
    LineReader lr = null;
    try {
      File f = new File(moduleListFile);
      lr = new LineReader(new FileReader(f));

      do {
        String className = lr.readLine();
        if (className == null) {
          break;
        }

        className = StringUtil.replace(className, "\n", "");
        className = StringUtil.replace(className, "\r", "");

        System.err.println("Starting module: '" + className + "'");

        Class.forName(className);
      } while (true);
    } catch (Exception e) {
      e.printStackTrace();

      throw new RuntimeException(e.getMessage());
    } finally {
      try {
        if (lr != null) {
          lr.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Load modules from ./modules directory or from directory specified by
   * "module.list" system property. This functions loads module classes into VM,
   * so they can be created later by newInstance() method and registered by
   * registerModule(...) function.
   * 
   * @pre (System.getProperty( "module.list" ) != null)
   */
  public static void loadModules() {
    if (loaded) {
      return;
    }

    loaded = true;

    String moduleList = System.getProperty("module.list");

    if (moduleList != null) {
      loadModulesDevelTime(moduleList);
      return;
    }

    List classes = new ArrayList(40);

    try { // Read list of default modules
      InputStream in = ModuleManager.class.getClassLoader()
          .getResourceAsStream(SERVICE);
      if (in != null) {
        try {
          readClassNames(in, classes);
        } finally {
          in.close();
        }
      }
    } catch (IOException e) {
      // FIXME: something has to be done here. Logging for example!
      System.err.println(e.getMessage());
      e.printStackTrace();
    }

    // Sets the modules directory path.
    // Gets the RefactorIT modules path from System.properties, if not
    // found there then sets its own.
    String modulesPath = RuntimePlatform.getModulesPath();
    File dir = null;
    if (modulesPath != null && modulesPath.length() > 0) {
      dir = new File(modulesPath);
      if (!dir.isDirectory()) {
        dir = null;
      }
    }

    // if modules directory wasn't specified by the caller of this
    // function, then set relative path to "modules".
    if (dir == null) {
      dir = new File("modules");
    }

    // Cycles all module ".jar"'s, reads all module definitions from them
    // and adds all module class file names into modules list (classes).
    File[] modulesJars = dir.listFiles();
    int len = (modulesJars == null) ? 0 : modulesJars.length;

    List urls = new ArrayList(len);
    for (int i = 0; i < len; i++) {
      try {
        File file = modulesJars[i];

        if (file.isDirectory() || !file.getName().endsWith(".jar")) {
          continue; // there are dirs, license and html files also
        }
        // will not compile with JDK 1.3
        // URL url = file.toURI().toURL();
        // FIXME: J2SDK suggest way above
        URL url = file.toURL();
        JarFile jar = new JarFile(file);
        try {
          JarEntry entry = jar.getJarEntry(SERVICE);
          if (entry != null) {
            InputStream in = jar.getInputStream(entry);
            try {
              readClassNames(in, classes);
            } finally {
              in.close();
            }
          }
        } finally {
          jar.close();
        }

        urls.add(url);

// log( "Added module " + URLDecoder.decode(url.toString()));
      } catch (MalformedURLException e) {
        log(e.getMessage());
      } catch (IOException e) {
        log(e.getMessage());
      }
    }

    // Loads modules from specified URL's and instantiates the
    // module classes.
    // log( "Loading modules using URLClassLoader..." );

    ClassLoader loader = new URLClassLoader(
        (URL[]) urls.toArray(new URL[urls.size()]),
        ModuleManager.class.getClassLoader()
        );

    log("Instantiating module classes...");

    int loadedModuleCount = 0;
    int failedModuleCount = 0;

    Iterator i = classes.iterator();
    while (i.hasNext()) {
      String name = (String) i.next();
      try {
        Class.forName(name, true, loader);

        loadedModuleCount++;
        // System.out.println( "Loaded module " + name );
      } catch (ClassNotFoundException e) {
        failedModuleCount++;
        log("Failed to load module", e);
      } catch (NoClassDefFoundError e) {
        failedModuleCount++;
        log("Failed to load module", e);
      }
    }

    log("Modules loaded successfully: " + loadedModuleCount);

    if (failedModuleCount > 0) {
      log("Failed to load " + failedModuleCount + " modules");
    }
  }

  /**
   * Reads the modules class names from input stream and puts them into a list.
   * 
   * @param in
   *          from where to read the class names (i.e.
   *          net.sf.refactorit.ui.module.gotomodule.GotoModule, ...)
   * @param classes
   *          to where to put the names of module classes.
   * @throws IOException
   */
  private static void readClassNames(InputStream in, List classes)
      throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in,
        "8859_1"));

    String line = reader.readLine();
    while (line != null) {
      if (line.length() > 0) {
        classes.add(line);
      }

      line = reader.readLine();
    }
  }

  /**
   * Dynamically registers module instance.
   * 
   * @param module
   *          registered module
   */
  public static void registerModule(RefactorItModule module) {
    modules.add(module);
  }

  /**
   * Logs the specified message.
   * 
   * @param message
   *          message.
   */
  private static void log(String message) {
    System.out.println("RefactorIT: " + message);
  }

  /**
   * Logs the specified message with provided stack trace.
   * 
   * @param message
   *          message.
   * @param t
   *          exception whose stack trace to log.
   */
  private static void log(String message, Throwable t) {
    System.out.println("RefactorIT: " + message);
    t.printStackTrace(System.out);
  }

  /**
   * Retain only JSP supported actions
   * 
   * @param actions
   *          list of <code>RefactorItAction</code>s
   * @param binClasses
   *          BinItems on which actions are operating
   */
  public static void filterJspSupportedActions(List actions, Class[] binClasses) {
    // System.err.println("filtring Jsp actions\n"+actions);

    Iterator it = actions.iterator();
    while (it.hasNext()) {
      RefactorItAction action = (RefactorItAction) it.next();
      if (!isAllowedForJsp(action, binClasses)) {
        it.remove();
      }
    }
  }

  /**
   * Is action allowed for Jsp.
   * 
   * @param act
   *          action
   * @param cl
   *          classes
   * @return true if allowed
   */
  public static boolean isAllowedForJsp(RefactorItAction act, Class[] cl) {
    if (act == null) {
      return false;
    }

    for (int i = 0; i < cl.length; ++i) {
      if (!act.isPreprocessedSourcesSupported(cl[i])) {
        return false;
      }
    }

    return true;
  }

  /**
   * @return unmodifiable list of all actions sorted by name
   */
  public static List getAllActions() {
    List result = new ArrayList(50);

    for (Iterator i = modules.iterator(); i.hasNext();) {
      RefactorItModule module = (RefactorItModule) i.next();

      RefactorItAction[] actions = module.getActions();
      if (actions != null) {
        CollectionUtil.addAllNew(result, Arrays.asList(actions));
      }
    }

    Collections.sort(result, actionsSorter);

    return Collections.unmodifiableList(result);
  }
}
