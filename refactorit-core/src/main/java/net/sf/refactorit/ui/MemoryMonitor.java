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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MemoryMonitor extends JPanel implements ActionListener {
  JLabel used = new JLabel("000");
  JLabel heap = new JLabel("000");

  Runtime runtime = Runtime.getRuntime();

  Thread thread;

  private Updater updater = new Updater();

  private JButton runGcButton;

  public MemoryMonitor() {
    super(new FlowLayout(FlowLayout.CENTER, 0, 5));
    add(new JLabel(" "));
    add(used);
    add(new JLabel("M / "));
    add(heap);
    add(new JLabel("M "));

    final Icon runGcIcon = ResourceUtil.getIcon(MemoryMonitor.class, "GC.gif");
    runGcButton = new JButton("", runGcIcon);

    runGcButton.setToolTipText("Run garbage collector");
    runGcButton.setMargin(new Insets(0, 0, 0, 0));
    add(runGcButton);
    runGcButton.addActionListener(this);
    updater.start();

//    runGcButton.addFocusListener(new FocusListener() {
//      public void focusGained(FocusEvent e) {
//        runGcButton.transferFocus();
//      }
//
//      public void focusLost(FocusEvent e) {}
//    });
  }

  /**
   * Invoked when an action occurs.
   */
  public void actionPerformed(ActionEvent e) {
    runtime.runFinalization();
    runtime.gc();
    updater.update();
  }

  class Updater implements Runnable {
    public void start() {
      thread = new Thread(this);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.setName("MemoryMonitor");
      thread.setDaemon(true);
      thread.start();
    }

    public synchronized void stop() {
      thread = null;
      this.notify();
    }

    public void run() {
      final Thread me = Thread.currentThread();

      while (thread == me) {
        try {
          update();
          Thread.sleep(5000);
        } catch (InterruptedException e) {}
      }
    }

    /**
     * Updates current memory display.
     */
    public void update() {
      synchronized (this) {
        if (isShowing()) {
          int totalMemory = (int) (runtime.totalMemory() / 1024 / 1024);
          int usedMemory = totalMemory
              - (int) (runtime.freeMemory() / 1024 / 1024);
          used.setText(Integer.toString(usedMemory));
          heap.setText(Integer.toString(totalMemory));
        }
      }
    }
  }
}
