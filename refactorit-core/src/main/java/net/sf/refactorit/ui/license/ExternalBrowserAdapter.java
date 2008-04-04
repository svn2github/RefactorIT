/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.license;


import net.sf.refactorit.ui.ExternalWebBrowser;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.Toolkit;


class ExternalBrowserAdapter implements HyperlinkListener {
  public void hyperlinkUpdate(HyperlinkEvent e) {
    // Check if user has clicked on some link
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        ExternalWebBrowser.newWindow(e.getURL());
      } catch (Exception ex) {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }
}
