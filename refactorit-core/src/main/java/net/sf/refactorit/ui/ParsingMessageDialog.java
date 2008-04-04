/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.Version;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.loader.CanceledException;
import net.sf.refactorit.loader.ProjectLoader;
import net.sf.refactorit.memory.SourcesMemoryMap;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.ParsingInterruptedException;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


/**
 * Shows modal message dialog while parsing project.
 * Runs parsing process in different thread.
 *
 * @author  Vladislav Vislogubov
 */
public class ParsingMessageDialog {
  public static final long MAX_NON_INTERRUPTABLE_PROCESS_TIME = 8000;

  private static final long MAX_INTERRUPT_WAITING_TIME = 4000;
  private static final Dimension PREFERRED_SIZE = new Dimension(250, 80);

  final JLabel l = new JLabel("Processing RefactorIT ...");
  final JProgressBar p = new JProgressBar(0, 100);

  IdeWindowContext context;

  Exception exception;

  DialogTask task;

  RitDialog dialog;

  boolean interrupted;
  boolean interruptable;
  Thread thread;

  public ParsingMessageDialog(IdeWindowContext context) {
    final ProgressListener listener = new ParsingProgressListener();

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());

    l.setFont(l.getFont().deriveFont(Font.PLAIN, 10));
    l.setHorizontalAlignment(JLabel.CENTER);
    l.setVerticalAlignment(JLabel.CENTER);
    l.setForeground(Color.black);

    contentPane.add(l, BorderLayout.CENTER);
    contentPane.add(p, BorderLayout.SOUTH);

    contentPane.setPreferredSize(PREFERRED_SIZE);

    dialog = RitDialog.create(context);
    dialog.setContentPane(contentPane);
    dialog.setTitle("RefactorIT message");
    dialog.setDisposeOnClose(false);

    this.context = dialog.getContext();

    class MyWindowAdapter extends WindowAdapter implements KeyListener {
      private boolean finished;
      private long processTime;

      void setFinished(boolean finished) {
        synchronized (this) {
          this.finished = finished;
        }
      }

      private boolean isFinished() {
        synchronized (this) {
          return this.finished;
        }
      }

      WindowListener me() {
        return this;
      }

      public void windowOpened(final WindowEvent evt) {
        setFinished(false);

        // thread
        thread = new Thread() {
          public void run() {
            try {
              ErrorsTab.remove();

              try {
                task.process(listener);
              } catch (CanceledException ex) {
                if (RefactorItConstants.debugInfo) {
                  AppRegistry.getLogger(this.getClass()).debug("CanceledException caught in ParsingMessageDialog[OK]");
                }
              }

              //setFinished(true);
            } catch (Exception e) {
              AppRegistry.getExceptionLogger().debug(e,this.getClass());
              exception = e;
            } catch (ThreadDeath t) {
            } finally {
              setFinished(true);
              if (!interrupted) {
                disposeDialog();
              }
            }
          }

          private void disposeDialog() {
            // HACK: I don't know why, but my testing shows we seem to need these lines
            // under S1S 5 030310. This relates to help window and the exception
            // java.lang.IllegalStateException: "Can't dispose InputContext while it's active".
            // I have not seen this with other NB versions that I checked.
            //
            // (To reproduce: comment out these lines & redeploy to S1S 5 030310; when in the IDE:
            // run WhereUsed, open RefactorIT help from the result panel, then
            // run WhereUsed again multiple times from the source window while the help window is
            // still open in the background. After just a couple of times of running WhereUsed,
            // the exception will come and help system under NB will be disabled.)
            //
            // Ask me for more info. Also, if you find more info/a better solution, please let me
            // know.)
            //
            // [RISTO]

            // XXX: refactor/test/remove
            Window w = evt.getWindow();
            w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
            //dialog.dispatchEvent(new ActionEvent(this,ActionEvent.ACTION_FIRST,""));

//            {
//              // Dispatch all current AWT events
//              net.sf.refactorit.common.util.SwingUtil.
//                  invokeAndWaitFromAnyThread_noCheckedExceptions(
//                  new Runnable() {
//                public void run() { /*nothing*/}
//              }
//              );
//            }
//
//            SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(
//                new Runnable() {
//              public void run() {
//                dialog.dispose();
//                dialog.removeWindowListener(me());
//              }
//            }
//            );
          }
        };

        thread.start();
      }

      public void windowClosing(WindowEvent evt) {
        if (interrupted) {
          // paranoia
          if (RefactorItConstants.debugInfo) {
            AppRegistry.getLogger(this.getClass()).debug("Entering WindowClosing second time!!");
          }
          return;
        }

        if (!isFinished()) {
          if (!interruptable) {
            if (processTime == 0) {
              processTime = System.currentTimeMillis();
              return;
            }

            if (System.currentTimeMillis() - processTime
                < ParsingMessageDialog.MAX_NON_INTERRUPTABLE_PROCESS_TIME) {
              return;
            }

            if (RefactorItConstants.debugInfo) {
              AppRegistry.getLogger(this.getClass()).debug("Maximum request processing time exceeded, disposing dialog");
              interruptable = true;
            }

            // allow interruption
          }
          if (interruptable) {
            interruptTask();
            //task.cancel();
          } else {
            return;
          }

        }
        dialog.dispose();
      }

      public void keyPressed(KeyEvent e) {
      }

      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//          dialog.dispose();
          windowClosing(null);
        }
      }

      public void keyTyped(KeyEvent e) {
      }
    }


    MyWindowAdapter myWindowAdapter = new MyWindowAdapter();
    dialog.addWindowListener(myWindowAdapter);
    dialog.getRootPane().addKeyListener(myWindowAdapter);
  }

  void interruptTask() {
    interrupted = true; // must set before interrupting thread
    // marking task as canceled
    task.cancel();
    //thread=null;

    try {
      thread.interrupt();
    } catch (Exception ex) {
      AppRegistry.getExceptionLogger().debug(ex,this.getClass());
    }

    try {
      thread.join(MAX_INTERRUPT_WAITING_TIME);
    } catch (InterruptedException ex1) {
      AppRegistry.getExceptionLogger().debug(ex1,this.getClass());
    }

    if (thread.isAlive()) {
      if (RefactorItConstants.debugInfo) {
        AppRegistry.getLogger(this.getClass()).debug("thread alive after "
        + MAX_INTERRUPT_WAITING_TIME / 1000 + " sek waiting");
      }
//      // caused VM crashing, better if it works in background
//        try {
//          thread.stop();
//        } catch (Exception ex2) {
//          DebugInfo.traceException(ex2);
//        }
    }
  }

  public void show() {
    try {
      show(false);
    } catch (ParsingInterruptedException ex) {
      // should never get here
    }
  }

  /**
   *
   * @param interruptable true if dialog can be closed before parsing is done.
   * @throws ParsingInterruptedException gets thrown when interruptable is true and dialog was
   * closed before parsing thread finished.
   */
  public void show(boolean interruptable) throws ParsingInterruptedException {
    interrupted = false;

    this.interruptable = interruptable;

    SwingUtil.invokeInEdtUnderNetBeansAndJB(new Runnable() {
      public void run() {
        dialog.show();
      }
    });

    if (interrupted) {
      // FIXME: i18n
      String msg = "Parsing was interrupted by user";
      throw new ParsingInterruptedException(msg);
    }
  }

  public void setDialogTask(DialogTask task) {
    this.task = task;
  }

  public Exception getException() {
    return exception;
  }

  public interface DialogTask {
    void process(ProgressListener listener) throws Exception;

    void cancel();
  }


  public static class RebuildProjectTask implements DialogTask {
    private Thread myThread = null;
    private Project project;
    private boolean clean;
    private boolean forceFullBuild;

    public RebuildProjectTask(Project project) {
      this(project, false, false);
    }

    public RebuildProjectTask(Project project, boolean clean,
        boolean forceFullBuild) {
      this.project = project;
      this.clean = clean;
      this.forceFullBuild = forceFullBuild;
    }

    public void process(ProgressListener listener) throws Exception {
      myThread = Thread.currentThread();
      try {
        if (clean) {
          project.clean();
        } else {
          project.getProjectLoader().build(listener, forceFullBuild);
        }
      } catch (SourceParsingException e) {
        if (e.justInformsThatUserFriendlyErrorsExist()) {
          // Silent ignore, because these errors
          // will show up in the ErrorsTab anyway
        } else {
          throw e;
        }
      }
    }

    public void cancel() {
      Assert.must(myThread != null);
      try {
        myThread.interrupt();
      } catch (Exception ex) {
        AppRegistry.getExceptionLogger().debug(ex,this.getClass());
      }

      //project.cancelParsing();
    }
  }

  public class ParsingProgressListener implements ProgressListener {
    long lastUpdate;

    public void showMessage(final String message) {
      l.setText(message);
    }

    public void progressHappened(final float persentage) {
      final long current = System.currentTimeMillis();
      if (current - this.lastUpdate < 500) {
        return;
      }
      this.lastUpdate = current;

      try {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            p.setValue((int) persentage);
          }
        });
      } catch (RuntimeException ignore) {}
    }

    public void beforeRebuild(Project project) {
      checkMemory(project);
    }

    private void checkMemory(final Project project) {
    	// notifies the user about the JVM memory. If application has less memory than recommended then
    	// a dialog is popped up.
    	long max = Math.round(Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0);

      //    int builtNrOfSources = project.getCompilationUnits().size();
      // This code appears to show number of sources more correctly when running
      // rebuild for the first time:
      int builtNrOfSources = project.getProjectLoader().getRebuildLogic()
          .getSourceListToRebuild().size();
    	try {
    		// System.out.println("Built nr of sources:"+builtNrOfSources);
    		int recommendedInMBs = SourcesMemoryMap.getRecommendedMemoryFor(
    				builtNrOfSources);
      	// Runtime.maxMemory() tends to return slightly less
      	// than configured with -Xmx, thus we multiply it with a small constant
        if (1.01 * max  < recommendedInMBs) {
          ProjectLoader.setLowMemoryMode(true);

    			String ideSpecificWarning = IDEController.getInstance().getLowMemoryWarning(recommendedInMBs);
    			String message = "<html><body>Currently available memory: " + max + " MB.<BR>"
					+ "Recommended maximum memory configuration for current project: " + recommendedInMBs + " MB.<BR>"
					+ ideSpecificWarning
					+ "<BR><BR>Find more help on this topic by clicking on the following link:<BR>"
          + "<a href=\"\">How to increase Memory</a></body></html>";

    			String val = GlobalOptions.getOption("memory.low.warning.dialog.show");
    			String version = Version.getVersion();

    			if (val == null || !val.equals(version)) {
    				// Window parentWindow = DialogManager.getDialogParent();
    				JMemoryInfo dialog = new JMemoryInfo(context, message);
    				dialog.show();
    				if (dialog.dontShowAgain.isSelected()) {
    					GlobalOptions.setOption("memory.low.warning.dialog.show", version);
    				}
    				// JOptionPane.showMessageDialog(ParsingMessageDialog.this, message);
    			}
    		}
    	} catch (Exception e) {
    		RuntimePlatform.console.println(e.getMessage());
    		e.printStackTrace(RuntimePlatform.console);
    	}

    }
  }
}
