/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.shell;

/*
 * ShellFrame.java
 *
 * Created on November 19, 2001, 12:38 PM
 */

import bsh.Interpreter;
import bsh.util.JConsole;

import javax.swing.JFrame;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/** Frame hosting BeanShell interpreter. */
public class ShellFrame extends JFrame {
  /** BeanShell interpreter. */
  public final Interpreter bsh;

  /**
   * Constructs new BeanShell interpreter frame.
   */
  public ShellFrame() {
    final JConsole console = new JConsole();
    bsh = new Interpreter(console);
    final Thread bshThread = new Thread(bsh);
    bshThread.setDaemon(true);
    bshThread.start();

    Dimension dim = getToolkit().getScreenSize();
    int w = (dim.width / 2);
    int h = (dim.height / 2);
    console.setPreferredSize(new Dimension(w, h));

    getContentPane().add(console);
    pack();

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        // FIXME: signal interpreter that it should close
        //        May be it is not necessary on the other hand.
        //        Interpreter does not seem to have a method for that.
      }
    });
  }

  /**
   * Gets BeanShell interpreter associated with this frame.
   *
   * @return interpreter. Never returns <code>null</code>.
   */
  public Interpreter getInterpreter() {
    return bsh;
  }
}
