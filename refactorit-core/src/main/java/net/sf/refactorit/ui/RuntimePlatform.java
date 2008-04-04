/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.standalone.JRefactorItFrame;
import net.sf.refactorit.ui.license.AboutDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.OptionsAction;
import net.sf.refactorit.utils.SwingUtil;

import org.apache.log4j.Logger;

import javax.swing.JScrollPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import java.awt.Component;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Uses reflection to make this class compile (and be valid) on all platforms.
 *
 * @author Risto
 * @author Tõnis
 */
public class RuntimePlatform {
  private static final Logger log=AppRegistry.getLogger(RuntimePlatform.class);
  
  public static int[] JRE_1_4 = new int[] {1, 4};

  // output to terminal console
  public static PrintStream console;

  public static boolean isWindows() {
    String os = System.getProperty("os.name").toLowerCase();
    return os != null && os.startsWith("windows");
  }
  
  public static boolean isWindowsXp() {
    String os = System.getProperty("os.name").toLowerCase();
    return os != null && os.indexOf("windows xp") >= 0;
  }

  public static boolean isMacOsX() {
    return System.getProperty("mrj.version") != null;
  }

  static {
    if (IDEController.runningNetBeans()) {
      console = System.out;
    } else {
      console = System.err;
    }
  }
  /**
   * @pre System property "refactorit.modules" is set
   * @return String representing modules path
   */
  public static String getModulesPath() {
    String result = System.getProperty("refactorit.modules");
    if (result == null) {
      String msg =
          "Couldn't get modules path: refactorit.modules property not set";
      log.error(msg);
      result = "";
    }
    return result;
  }

  public static String getLibPath() {
    String result = System.getProperty("refactorit.modules.lib");
    if (result == null) {
      String msg =
          "Couldn't get lib path: refactorit.modules.lib property not set";
      log.error(msg);
      result = "";
    }
    return result;
  }

  public static String getConfigDir() {
    if (RuntimePlatform.isMacOsX()) {
      return getMacConfigDir();
    } else {
      return getWinAndUnixConfigDir();
    }
  }

  private static String getMacConfigDir() {
    try {
      Class mrjFileUtils = Class.forName("com.apple.mrj.MRJFileUtils");
      Object kPreferencesFolderType = mrjFileUtils.getField(
          "kPreferencesFolderType").get(null);

      Class mrjOsType = Class.forName("com.apple.mrj.MRJOSType");
      Method findFolder = mrjFileUtils.getMethod("findFolder",
          new Class[] {mrjOsType});
      String prefFolder = findFolder.invoke(null,
          new Object[] {kPreferencesFolderType}).toString();

      // Note: under Mac settings folders are not hidden
      return prefFolder + File.separator + "RefactorIT";
    } catch (Exception e) {
      System.err.println("REFACTORIT ERROR -- PLEASE REPORT");
      e.printStackTrace();
      return getWinAndUnixConfigDir();
    }
  }

  private static String getWinAndUnixConfigDir() {
    return System.getProperty("user.home") + File.separator + ".refactorit";
  }

  public static JScrollPane createDefaultScrollPane(Component component) {
    JScrollPane result = new JScrollPane(component);
    result.setHorizontalScrollBarPolicy(
        isMacOsX() ? JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS :
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    result.setVerticalScrollBarPolicy(
        isMacOsX() ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS :
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    return result;
  }

  public static boolean runningStandalone() {
    String result = System.getProperty("refactorit.platform");
    if (result != null && result.equals("standalone")) {
      return true;
    }
    return false;
  }

//  public static boolean runningJDev() {
//    String result=System.getProperty("refactorit.platform");
//    if ( result!=null && result.equals("jdev") ) {
//      return true;
//    }
//    return false;
//  }

  public static double getJavaSpecificationVersion() {
    try {
      return Double.parseDouble(
          System.getProperty("java.specification.version"));
    } catch (Exception e) {
      return 1.0;
    }
  }

  public static String getJavaVersion() {
    return System.getProperty("java.version");
  }

  public static boolean javaVersionAtLeast(int[] version) throws StringUtil.
      CheckedNumberFormatException {
    return StringUtil.systemPropertyVersionAtLeast(version, "java.version");
  }

  private static LookAndFeel lookAndFeel;

  /**
   * HACK: Under Mac L&F BinTreeTable fails to collapse its nodes when user clicks on the "arrow",
   * that's why under Mac we need to have default L&F for BinTreeTable.
   */
  public static void changeLookAndFeelIfNeeded() {
    if (RuntimePlatform.isMacOsX()) {
      lookAndFeel = UIManager.getLookAndFeel();

      try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception e) {
        System.err.println("RefactorIT EXCEPTION -- PLEASE REPORT");
        e.printStackTrace();
      }
    }
  }

  public static void restoreLookAndFeelIfNeeded() {
    if (RuntimePlatform.isMacOsX()) {
      try {
        UIManager.setLookAndFeel(lookAndFeel);
      } catch (Exception e) {
        System.err.println("RefactorIT EXECPTION -- PLEASE REPORT");
        e.printStackTrace();
      }
    }
  }

  public static String getOldConfigDirToImportFrom() {
    return System.getProperty("user.home") + File.separator + ".refactorit";
  }

  public static class MacOsX {
    static Object[] handlerArguments;

    public static void setRefactorITCreatorCode(File file) {
      try {
        Class mrjOsType = Class.forName("com.apple.mrj.MRJOSType");
        Class mrjFileUtils = Class.forName("com.apple.mrj.MRJFileUtils");
        Method setFileCreator =
            mrjFileUtils.getMethod("setFileCreator", new Class[] {File.class,
            mrjOsType});
        setFileCreator.invoke(null, new Object[] {file,
            getRefactorITCreatorCode(mrjOsType)});
      } catch (Exception e) {
        System.err.println("REFACTORIT EXCEPTION -- PLEASE REPORT");
        e.printStackTrace();
      }
    }

    public static void addAppleEventHandlers(final JRefactorItFrame frame) {
      try {
        registerMrjHandler("com.apple.mrj.MRJAboutHandler",
            "registerAboutHandler", new Runnable() {
          public void run() {
            IdeWindowContext context = IDEController
                .getInstance().createProjectContext();
            AboutDialog dialog = new AboutDialog(context);
            dialog.show();
          }
        });

        registerMrjHandler("com.apple.mrj.MRJPrefsHandler",
            "registerPrefsHandler", new Runnable() {
          public void run() {
            OptionsAction.runAction(
                IDEController.getInstance().createProjectContext(),
                frame.getBrowser());
          }
        });

        registerMrjHandler("com.apple.mrj.MRJQuitHandler",
            "registerQuitHandler", new Runnable() {
          public void run() {
            frame.exitWithSaveConfirm();

            // If we are here then the user chose 'cancel', and this exception
            // cancels quit for the MRJ
            throw new IllegalStateException();
          }
        });

        registerMrjHandler(
            "com.apple.mrj.MRJOpenDocumentHandler",
            "registerOpenDocumentHandler",
            new Runnable() {
          public void run() {
            UIResources.disableStartupDialogIfNotYetShown();
            frame.openNewProject((File) handlerArguments[0]);
          }
        });
      } catch (Exception e) {
        System.err.println(
            "RefactorIT Exception when installing Mac OS X support -- PLEASE REPORT");
        e.printStackTrace();
      }
    }

    private static Object getRefactorITCreatorCode(Class mrjOsType) throws
        NoSuchMethodException, InstantiationException,
        IllegalAccessException, InvocationTargetException {
      Constructor resultConstructor = mrjOsType
          .getConstructor(new Class[] {String.class});

      return resultConstructor.newInstance(new Object[] {"RBPR"});
    }

    private static void registerMrjHandler(final String handlerClassName,
        final String handlerRegisterMethodName,
        final Runnable runnable) throws ClassNotFoundException,
        IllegalAccessException,
        InvocationTargetException, NoSuchMethodException {
      final Class handlerClass = Class.forName(handlerClassName);
      Object handlerInstance = Proxy.newProxyInstance(
          handlerClass.getClassLoader(),
          new Class[] {handlerClass}
          ,
          new InvocationHandler() {
        public Object invoke(Object proxy, Method method, final Object[] args) {

          if (method.getDeclaringClass().equals(handlerClass)) {
            try {
              SwingUtil.invokeAndWaitFromAnyThread(new Runnable() {
                public void run() {
                  handlerArguments = args;
                  runnable.run();
                }
              });
            } catch (InterruptedException e) {
              System.err.println("REFACTORIT EXCEPTION -- PLEASE REPORT");
              e.printStackTrace();
            } catch (InvocationTargetException e) {
              if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
              }

              System.err.println("REFACTORIT EXCEPTION -- PLEASE REPORT");
              e.printStackTrace();
            }
          }

          return null;
        }
      });

      Method handlerRegisterer = Class
          .forName("com.apple.mrj.MRJApplicationUtils")
          .getMethod(handlerRegisterMethodName, new Class[] {handlerClass});
      handlerRegisterer.invoke(null, new Object[] {handlerInstance});
    }
  }
  
  public static long getFileDatePrecision() {
    if(isWindows()) {
      // A bit bigger than needed to be sure
      // (actually needed: 10 or 19 or 22/23 ?)
      return 30; 
    } else {
      return 1000;
    }
  }
}
