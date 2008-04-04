/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.help;



import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.InitializationException;

import javax.help.DefaultHelpBroker;
import javax.help.HelpSet;

import java.awt.Image;
import java.net.URL;
import java.util.ResourceBundle;


public class JavaHelpTopicDisplayer implements TopicDisplayer {
  private final CustomIconHelpBroker helpBroker;

  public JavaHelpTopicDisplayer() throws InitializationException {
    ClassLoader classLoader = HelpViewer.class.getClassLoader();

    // Get the HelpSet file name from resources (properties file).
    ResourceBundle helpResources = ResourceUtil.getBundle(HelpViewer.class,
        "help");
    String helpSetName = helpResources.getString("helpSetName");

    // Retrieve the HelpSet file as URL from refactorit module classpath.
    // If Help Set is not found, throw exception.
    HelpSet helpSet = null;
    URL helpSetURL = HelpSet.findHelpSet(classLoader, helpSetName);
    if (helpSetURL == null) {
      String ourMsg = "Couldn't initialize the Help Viewer. Help Set "
          + helpSetName +
          " not found. Using classLoader: " + classLoader.toString();
      throw new InitializationException(ourMsg, null);
    }

    // Creates a new Help Set object. To do it, the constructor parses
    // the xml file that was given as URL.
    try {
      helpSet = new HelpSet(classLoader, helpSetURL);
    } catch (Exception e) {
      String ourMsg =
          "Couldn't initialize the Help Viewer." +
          " Problems parsing the Help Set." +
          " Help Set URL --> " + helpSetURL;
      throw new InitializationException(ourMsg, e);
    }

    // Create the HelpBroker object for this HelpViewer object.
    // The helpBroker object is used by others functions to display
    // help on HelpViewer.
    helpBroker = new CustomIconHelpBroker(helpSet);
  }

  public void displayTopic(IdeWindowContext context, String topicId) {
    if (context instanceof AWTContext) {
      // FIXME:
      // apparently returns null sometimes:
      //
      // From version 1.2.1
      // Andrew.Rothstein@blackrock.com
      //
      // I downloaded and installed the freeware Windows version this morning.
      // Created a project with 435 sources. Downloaded the evaluation license.
      // Restarted. Went to load the project file I had created with the
      // freeware version and it crashed with the following exception:
      //
      //java.lang.NullPointerException
      //        at net.sf.refactorit.ui.d.a.b(a.java:36)
      //        at net.sf.refactorit.ui.d.a.a(a.java:1)
      //        at net.sf.refactorit.l.ce.a(ce.java:13)
      //        at net.sf.refactorit.l.ce.<init>(ce.java:181)
      //        at net.sf.refactorit.l.bf.actionPerformed(bf.java:0)
      //        at javax.swing.AbstractButton.fireActionPerformed(Unknown Source)
      //        at javax.swing.AbstractButton$ForwardActionEvents.actionPerformed(Unknown Source)
      //        at javax.swing.DefaultButtonModel.fireActionPerformed(Unknown Source)
      //        at javax.swing.DefaultButtonModel.setPressed(Unknown Source)
      //        at javax.swing.AbstractButton.doClick(Unknown Source)

      helpBroker.setActivationWindow(((AWTContext) context).getWindow());
      helpBroker.setCurrentID(topicId);
      helpBroker.setDisplayed(true);

      helpBroker.setIconForOpenedHelpFrame(
          ResourceUtil.getImage(UIResources.class, "RefactorIt.gif"));
    } else {
      AppRegistry.getLogger(getClass()).error(
          "AWTContext was expected in help displayer displayTopic(): "
          + context == null ? null : context.getClass());
    }
  }

  private static class CustomIconHelpBroker extends DefaultHelpBroker {
    public CustomIconHelpBroker(HelpSet helpSet) {
      setHelpSet(helpSet);
    }

    /**
     * Sets an icon of a current help frame (it could be visible or not).
     * Will give a NPE if no window has been opened yet by the DefaultHelpBroker at all.
     */
    public void setIconForOpenedHelpFrame(Image icon) {
      if (frame != null) {
        frame.setIconImage(icon);
      }
    }
  }
}
