/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.testmodule;



import java.io.File;

import javax.swing.KeyStroke;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.NBController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;

import junit.swingui.TestRunner;


/**
 *
 *
 * @author  risto
 */
public class NBTestRunnerModule extends AbstractRefactorItModule {
  public static final String NAME = "[JUnit Tests] ";
  public static final String KEY = "refactorit.action.NBTestRunnerModule";

  public static class TestingEnvironment {
    public Project activeProject = null;
  };

  public static TestingEnvironment testingEnvironment = new TestingEnvironment();

  public NBTestRunnerModule() {}

  public RefactorItAction[] getActions() {return createActions();}

  public String getName() {return NAME;}

  private static class TestRunnerAction extends AbstractRefactorItAction {
    private Class testSuite;
    private String name;

    public boolean isReadonly() {
      return true;
    }

    public TestRunnerAction(Class testSuite, String name) {
      this.testSuite = testSuite;
      this.name = name;
    }

    public String getKey() {return KEY + "." + testSuite.getName();}

    public KeyStroke getKeyStroke() {return null;}

    public String getName() {return NAME + " -- " + name;}

    public boolean isMultiTargetsSupported() {return true;}

    public boolean isAvailableForType(Class type) {
      return true;
    }
    
    public boolean run(RefactorItContext context, Object object) {
      if(IDEController.getInstance() instanceof NBController) {
        testingEnvironment.activeProject = IDEController.getInstance().getActiveProject();
      }

      DialogManager.setInstance(new NullDialogManager());

      try {
        Utils.setTestFileDirectory(new File(NBTestRunnerModule.getTestProjectsDir()));
      } catch (CancelledException e) {
        AppRegistry.getExceptionLogger().error(e, this);
        throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
      }

      // Returns immediately
      TestRunner.main(new String[] {"-noloading", testSuite.getName()});

      return false;
    }

  }

  private static String getTestProjectsDir() throws CancelledException {
    String checkoutFolder = Parameters.getRitCheckoutFolder();

    String result = checkoutFolder + File.separator + "refactoring"
        + File.separator + "test";
    if (!new File(result).exists()) {
      Parameters.forgetLastValue();
      DialogManager.getInstance().showCustomError(
          IDEController.getInstance().createProjectContext(),
          "Can't find RIT checkout folder");
      throw new CancelledException();
    }

    return result;
  }



  private RefactorItAction[] createActions() {
    try {
      return new RefactorItAction[] {
        new TestRunnerAction(Class.forName("net.sf.refactorit.test.netbeans.AllTests"),
            "NB integration"),
        new TestRunnerAction(Class.forName("net.sf.refactorit.test.netbeans.vcs.CvsTest"),
            "CVS"),

        // Has may failures -- it's just confusing right now; should be uncommented once the tests are all running
        //new TestRunnerAction(net.sf.refactorit.test.AllTests.class),
      };
    } catch (NoClassDefFoundError e) {
      // ignore - production system without junit.jar?
      return new RefactorItAction[0];
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
  }

  public static class CancelledException extends Exception {}


  public static class Parameters {
    private static String lastName = null;

    private static final String PSEUDO_ASK_AGAIN = "";

    public static String getCvsRoot() throws CancelledException {
      return getOption("netbeans.cvs.tests.cvsroot",
          "CVS Root of the server to work on");
    }

    public static String getRitCheckoutFolder() throws CancelledException {
      return getOption("refactorit.checkout.dir",
          "RefactorIT checkout dir (must contain refactoring/tests)");
    }

    private static String getOption(String optionName,
        String displayMessage) throws CancelledException {
      lastName = optionName;

      if (GlobalOptions.getOption(optionName) == null
          || GlobalOptions.getOption(optionName).equals(PSEUDO_ASK_AGAIN)) {
        String value = RitDialog.showInputDialog(
            IDEController.getInstance().createProjectContext(), displayMessage);
        if (value == null) {
          throw new CancelledException();
        }
        GlobalOptions.setOption(optionName, value);
        GlobalOptions.save();
      }

      return GlobalOptions.getOption(optionName);
    }

    public static void forgetLastValue() {
      if (lastName != null) {
        GlobalOptions.setOption(lastName, PSEUDO_ASK_AGAIN);
        GlobalOptions.save();
      }
    }
  }
}
