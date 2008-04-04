/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;



import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.MissingBinMember;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.HtmlUtil;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.loader.CanceledException;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.source.MethodNotFoundError;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.RefactorItConstants;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;


/**
 * The progress bar used to show the progress of searching.
 *
 * This class is used to show the searching progress of some kind of elements
 * related with Refactorings processes.
 */
public class JProgressDialog implements ProgressListener {
  public static final long MAX_NON_INTERRUPTABLE_PROCESS_TIME = 80000;

  private static final long MAX_INTERRUPT_WAITING_TIME = 2000;

  final boolean interruptable;

  final RitDialog dialog;

  ProgressThread thread;

  private ProgressDialogAdapter adapter;
  private JPanel contentPane;

  /**
   * Label containing details. <code>null</code> if no details shown.
   */
  private JLabel detailsLabel;

  // a progressbar (0-100%) showing the search progress
  private JProgressBar progressBar;

  private JProgressDialog(
      IdeWindowContext context, ProgressThread thread, boolean interruptable
  ) {
    Assert.must(thread != null);

    this.thread = thread;
    this.interruptable = interruptable;
    
    contentPane = new JPanel(new GridBagLayout());

    JLabel label = new JLabel("Searching ...");
    label.setFont(label.getFont().deriveFont(Font.BOLD, 15));
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setForeground(Color.black);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.insets = new Insets(5, 10, 5, 10);
    constraints.weighty = 1.0;
    constraints.ipadx = 100;
    constraints.ipady = 5;

    contentPane.add(label, constraints);
    // getContentPane().add(new JLabel("Very very very long test"));

    //this.progressLabel = new JLabel();
    //label.setFont( label.getFont().deriveFont(Font.BOLD, 15) );
    //label.setHorizontalAlignment( JLabel.CENTER );
    //label.setForeground( Color.black );
    //layout.setConstraints(this.progressLabel, labelConstraints);
    //getContentPane().add(this.progressLabel);

    dialog = RitDialog.create(context);
    dialog.setTitle("RefactorIT");
    dialog.setContentPane(contentPane);

    adapter = new ProgressDialogAdapter();

    // start the thread after window has opened
    dialog.addWindowListener(adapter);
    dialog.getRootPane().addKeyListener(adapter);
  }

  /*
   * @see net.sf.refactorit.common.util.ProgressListener#showMessage(java.lang.String)
   */
  public void showMessage(final String message) {
    if (SwingUtilities.isEventDispatchThread()) {
      setDetails(message);
    } else {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            setDetails(message);
          }
        });
      } catch (Exception ignored) {}
    }
  }

  /**
   * Sets details shown in this progress dialog.
   *
   * @param detail detail to show or <code>null</code> for no details.
   */
  void setDetails(String detail) {
    // I commented this setResizable out because for some strange reason it sometimes
    // gets:
    //An unexpected exception has been detected in native code outside the VM.
    //Unexpected Signal : EXCEPTION_ACCESS_VIOLATION occurred at PC=0x6d0bbc65
    //Function name=Java_sun_awt_windows_WDesktopProperties_playWindowsSound
    //Library=C:\jdk1.3.1_01\jre\bin\awt.dll
    //
    // If uncommented, two other setResizable( true ) (in init and this method)
    // should be changed to setResizble( false ). This way it will not be
    // possible to resize the dialog.

    // setResizable(true);
    if (detailsLabel != null) {
      // details shown in the dialog at the moment
      if (detail == null) {
        // Remove details label
        contentPane.remove(detailsLabel);
      } else {
        detailsLabel.setText(detail);
      }
    } else {
      // Details not shown in dialog at the moment
      if (detail != null) {
        // Add details label
        detailsLabel = new JLabel(detail);
        detailsLabel.setForeground(Color.black);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 1;
        constraints.insets = new Insets(5, 10, 10, 10);
        constraints.weighty = 1.0;
        constraints.ipady = 5;

        contentPane.add(detailsLabel, constraints);
      }
    }
  }

  /**
   * Gets details about current search to show in progress dialog.
   *
   * @param target search target.
   *
   * @return details or <code>null</code> if no detailed information available.
   */
  public static String getDetails(BinItem target) {
    if (target == null) {
      return null;
    }

    if (target instanceof BinMember) {
      return ((BinMember) target).getQualifiedName();
    }

    if (target instanceof BinLocalVariable) {
      return ((BinLocalVariable) target).getDetails();
    }

    if (target instanceof BinPackage) {
      return ((BinPackage) target).getQualifiedName();
    }

    if (target instanceof BinMemberInvocationExpression) {
      return ((BinMemberInvocationExpression) target).getDetails();
    }

    if (target instanceof BinConstructorInvocationExpression) {
      return ((BinConstructorInvocationExpression) target).getDetails();
    }

    if (target instanceof BinStringConcatenationExpression) {
      // FIXME: check if it is really needed
      return ((BinStringConcatenationExpression) target)
          .getReturnType().getQualifiedName();
    }

    if (target instanceof BinThrowStatement) {
      return "throw "
          + BinFormatter.formatQualified(((BinThrowStatement) target)
          .getExpression().getReturnType());
    }

    if (target instanceof BinMethod.Throws) {
      return "throws " + ((BinMethod.Throws) target)
          .getException().getQualifiedName();
    }

    if (target instanceof BinNewExpression) {
      return "new " + ((BinNewExpression) target)
          .getReturnType().getQualifiedName();
    }

    if (target instanceof BinLabeledStatement) {
      return "label " + ((BinLabeledStatement) target).getLabelIdentifierName();
    }
    
    if (target instanceof BinSelection) {
      String text = ((BinSelection) target).getText();
      if (text.length() > 20) {
        text = text.substring(0, 20) + "..";
      }
      return text;
    }

    if (target instanceof MissingBinMember) {
      MethodNotFoundError error = ((MissingBinMember) target).error;
      return error.getMethodName() + "( )";
    }

    throw new IllegalArgumentException(
        "Don't know how to show details for target of type " + target.getClass());
  }

  public static String[] getDetails(Object[] target) {
    String[] details = new String[target.length];
    for (int i = 0; i < target.length; i++) {
      // FIXME target[i] == null happened once after:
      // 1. WhereUsed for several classes
      // 2. Move of these classes
      // 3. ReRun in WhereUsed panel
      if (target[i] == null) {
        new IllegalArgumentException("Target " + i
            + " is null").printStackTrace();
      } else if (target[i] instanceof BinItem) {
        details[i] = getDetails((BinItem) target[i]);
      } else if (target[i] instanceof Project) {
        details[i] = "Project " + ((Project) target[i]).getName();
      } else {
        throw new IllegalArgumentException(
            "Don't know how to show details for "
            + " target of type " + target[i].getClass());
      }
    }
    return details;
  }

  private static String combineDetails(String[] details) {
    String result;

    final StringBuffer buf = new StringBuffer(80);
    synchronized (buf) {
      Font font = Font.decode(GlobalOptions.getOption("tree.font"));

      for (int i = 0; i < details.length; i++) {
        buf.append("<TR><TD>").append(
            HtmlUtil.styleText(details[i], font)).append("</TD></TR>");
        if (i > 20) {
          buf.append("<TR><TD><B>...</B></TD></TR>");
          break;
        }
      }

      result = "<HTML><BODY><TABLE>" + buf.toString() + "</TABLE></BODY></HTML>";
    }

    return result;
  }

  /**
   * Starts progress dialog with specified task and detail information
   * about the task.
   *
   * @param parent dialog owner.
   * @param task task to run.
   * @param object contains details to show about the task.
   * @param interruptable interruptable
   * @throws SearchingInterruptedException
   * @see #run(Component, Runnable, boolean)
   */
  public static void run(
      final IdeWindowContext context, Runnable task,
      Object object, final boolean interruptable
  ) throws SearchingInterruptedException {
    if (context instanceof NullContext) {
      task.run();
      return;
    }

    final ProgressThread thread = new ProgressThread(task);

    final JProgressDialog dialog =
      new JProgressDialog(context, thread, interruptable);

    thread.setDialog(dialog);
    dialog.setDetails(getDetails(object));
    dialog.dialog.setDisposeOnClose(false);

    try {
//      SwingUtil.invokeInEdtUnderNetBeansAndJB(new Runnable() {
//        public void run() {
          dialog.dialog.show();
//        }
//      });
    } catch (RuntimeException e) {
      AppRegistry.getExceptionLogger().debug(e, JProgressDialog.class);
      try {
        dialog.dialog.dispose();
      } catch (Exception ex) {
        AppRegistry.getExceptionLogger().debug(ex, JProgressDialog.class);
      }
    } finally {
      if (dialog.interrupted()) {
        //if (myAdapter.interrupted ) {
        //System.out.println("ProgressDialog interrupted!!!");
        //FIXME: i18n
        String msg = "Processing was interrupted by user";
        throw new SearchingInterruptedException(msg);
      }
    }

    Throwable exception = thread.getException();
    if (exception != null) {
      AppRegistry.getExceptionLogger().error(exception, JProgressDialog.class);
      JErrorDialog err = new JErrorDialog(context, "Error");
      err.setException(exception);
      err.show();
    }
  }

  private static String getDetails(final Object object) {
    String details;

    if (object instanceof String) {
      details = (String) object;
    } else if ((object instanceof Object[])) {
      details = combineDetails(getDetails((Object[]) object));
    } else if (object instanceof Collection) {
      details = combineDetails(getDetails(((Collection) object).toArray()));
    } else if (object instanceof BinItem) {
      details = getDetails((BinItem) object);
    } else {
      details = "";
    }

    return details;
  }

  /**
   * Starts progress dialog with specified task.
   *
   * @param owner dialog owner.
   * @param task task to run.
   * @param interruptable interruptable
   * @throws SearchingInterruptedException
   * @see #run(Component, Runnable, Object, boolean)
   */
  public static void run(
      IdeWindowContext context, Runnable task, boolean interruptable
  ) throws SearchingInterruptedException {
    run(context, task, null, interruptable);
  }

  /*
   * @see net.sf.refactorit.common.util.ProgressListener#progressHappened(float)
   */
  public void progressHappened(final float percentage) {
    if (SwingUtilities.isEventDispatchThread()) {
      setProgressValue(percentage);
    } else {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            setProgressValue(percentage);
          }
        });
      } catch (Exception ignored) {}
    }
  }

  /**
   * Sets progress value shown in this progress dialog.
   *
   * @param percentage to show.
   */
  void setProgressValue(float percentage) {
    if (progressBar == null) {
      // Add progressBar
      progressBar = new JProgressBar(0, 100);

      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridy = 2;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      //constraints.insets = new Insets(5, 10, 5, 10);
      //constraints.ipadx = 100;

      contentPane.add(progressBar, constraints);

      final int height = progressBar.getPreferredSize().height;
      dialog.setSize(dialog.getWidth() + 1, dialog.getHeight() + height);

      // strangely doesn't properly layout on setSize()
      SwingUtilities.getRoot(dialog.getRootPane()).validate();
    }

    progressBar.setValue((int) percentage);
  }

  boolean interrupted() {
    return adapter.interrupted;
  }

  void dispose() {
    dialog.dispose();
  }

  private static class ProgressThread extends Thread {
    boolean finished;
    boolean stoppedByDialog;

    private Runnable task;
    private Throwable exception;

    private JProgressDialog dialog;

    public ProgressThread(Runnable task) {
      this.task = task;
    }

    public void run() {
      finished = false;
      try {
        CFlowContext.add(ProgressListener.class.getName(), dialog);
        task.run();
      } catch (Exception e) {
        exception = e;
        AppRegistry.getExceptionLogger().debug(e, getClass());
      } catch (CanceledException ex) {
        if (RefactorItConstants.debugInfo) {
          AppRegistry.getLogger(getClass()).debug(
              "CanceledException caught in JProgressDialog[OK]");
        }
      } finally {
        finished = true;

        CFlowContext.remove(ProgressListener.class.getName());
        if (stoppedByDialog) {
          return; // if interrupted
        }
      }

      try {
        dialog.dispose();
//        dialog.dialog.dispatchEvent(new WindowEvent(
//            dialog.dialog,WindowEvent.WINDOW_CLOSING));
      } catch (Exception ie) {
        if (Assert.enabled) {
          ie.printStackTrace();
        }
      }
    }

    public void setDialog(JProgressDialog dialog) {
      this.dialog = dialog;
    }

    public boolean isFinished() {
      return finished;
    }

    public Throwable getException() {
      return exception;
    }
  }

  class ProgressDialogAdapter extends WindowAdapter implements KeyListener {
    private long processTime = 0;
    boolean interrupted = false;

    public void windowOpened(WindowEvent e) {
      thread.start();
    }

    public void windowClosed(WindowEvent e) {
      if (!thread.isFinished() && !interruptable) {
        if (RefactorItConstants.debugInfo) { // debug messages
          //FIXME: under Netbeans, user can close dialog even if we set closeOp to DO_NOTHING_ON_CLOSE
          // thread is still working on ...
          String msg = "Error: not interruptable dialog was closed";
          if (IDEController.runningNetBeans()) {
            msg += " -- netBeans issue";
          } else {
            msg +=
                " -- this one was tracked before under netBeans platform only!!!";
          }
          AppRegistry.getLogger(this.getClass()).debug(msg);
        }
      }
    }

//    public void processInterruptionRequest() {
//      // FIXME: ProgressThread needs refactoring
//    }

    /**
     * Stops thread if thread is interruptable and returns true.
     * Otherwise returns false.
     *
     * @return true if succeeded to interrupt
     */
    private boolean processThreadInterruptionRequest() {
      if (!interruptable) {
        if (processTime == 0) {
          processTime = System.currentTimeMillis();
          return false;
        }

        if (System.currentTimeMillis() - processTime
            < JProgressDialog.MAX_NON_INTERRUPTABLE_PROCESS_TIME) {
          return false;
        }

        if (RefactorItConstants.debugInfo) {
          AppRegistry.getLogger(getClass()).debug(
              "Maximum request processing time exceeded, disposing dialog");
        }

        // allow interruption

//      // FIXME: i18n
//      String message = "RefactorIT is performing changes now";
//      String title = "Can not interrupt processing";
//      System.out.println("Interruption refused");
//      JOptionPane.showMessageDialog(
//          parent, message, title, JOptionPane.ERROR_MESSAGE);
      }

      thread.stoppedByDialog = true;
      thread.interrupt();

      try {
        thread.join(MAX_INTERRUPT_WAITING_TIME);
      } catch (InterruptedException ex) {
        if (RefactorItConstants.debugInfo) {
          AppRegistry.getLogger(getClass()).debug(
              "InterruptException was thrown");
        }
      }

      if (thread.isAlive()) {
        if (RefactorItConstants.debugInfo) {
          AppRegistry.getLogger(getClass()).debug(
              "disposing progress dialog but thread Is still alive!!!");
        }

        /*
                 // bad code: thread.stop() causes monitors to unlock!!
                 try {
          // FIXME: should also set Project.parsingCanceled() here
          thread.stop();
                 } catch (ThreadDeath ex) {
                 } catch(Exception e) {
          DebugInfo.traceException(e);
                 }
         */
      }

      thread.finished = true;
      //System.out.println("calling thread interrupt!!!");
      interrupted = true;

      return true;
    }

    /**
     * Processing dialog closing request and disposing dialog if allowed.
     * @param e an event
     */
    public void windowClosing(WindowEvent e) {
      close();
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//        dialog.dialog.dispose();
        close();
      }
    }

    public void keyTyped(KeyEvent e) {
    }

    private void close() {
      if (thread != null && !thread.isFinished() && thread.isAlive()) {
        if (!processThreadInterruptionRequest()) {
          return;
        }
      }

      try {
        dialog.dispose();
      } catch (Exception ex) {
        AppRegistry.getExceptionLogger().debug(ex, getClass());
      }
    }
  }
}
