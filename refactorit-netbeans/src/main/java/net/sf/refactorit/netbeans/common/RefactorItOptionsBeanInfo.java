/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import net.sf.refactorit.options.GlobalOptions;

import org.apache.log4j.Logger;


/**
 *
 * @author  Igor Malinin
 */
public class RefactorItOptionsBeanInfo extends SimpleBeanInfo {
  private static final Logger log = Logger.getLogger(RefactorItOptionsBeanInfo.class);

  public BeanDescriptor getBeanDescriptor() {
    BeanDescriptor desc = new BeanDescriptor(RefactorItOptions.class);
    desc.setDisplayName(GlobalOptions.REFACTORIT_NAME);
    return desc;
  }

  private static PropertyDescriptor[] desc;

  // initialization of the array of descriptors
  static {
    try {
      desc = new PropertyDescriptor[] {
          new PropertyDescriptor(RefactorItOptions.PROP_COMPILE,
          RefactorItOptions.class),
          new PropertyDescriptor(RefactorItOptions.PROP_CLASSPATH,
          RefactorItOptions.class),
          new PropertyDescriptor(RefactorItOptions.PROP_SOURCEPATH,
          RefactorItOptions.class),
          new PropertyDescriptor(RefactorItOptions.PROP_JAVADOCPATH,
          RefactorItOptions.class),
          new PropertyDescriptor(RefactorItOptions.PROP_AUTODETECT_PATHS,
          RefactorItOptions.class),
          new PropertyDescriptor(RefactorItOptions.
          PROP_NEW_PROJECT_MESSAGE_DISPLAYED, RefactorItOptions.class),
          //new PropertyDescriptor( RefactorItOptions.PROP_IS_GLOBAL_HACK, RefactorItOptions.class ),
          new PropertyDescriptor(RefactorItOptions.NEW_PROPERTIES,
          RefactorItOptions.class),
          new PropertyDescriptor(RefactorItOptions.HACK, RefactorItOptions.class),
          new PropertyDescriptor(RefactorItOptions.PROP_CACHEPATH,
          RefactorItOptions.class)
      };

      desc[0].setDisplayName("Compile Before Refactoring");
      desc[0].setShortDescription("Compile Before Refactoring");

      desc[1].setDisplayName("Parser Class Path");
      desc[1].setShortDescription("Parser Class Path");

      desc[2].setDisplayName("Parser Source Path");
      desc[2].setShortDescription("Parser Source Path");

      desc[3].setDisplayName("Javadoc Path");
      desc[3].setShortDescription("Javadoc Path");

      desc[4].setDisplayName("Autodetect Class and Source Paths");
      desc[4].setShortDescription("Autodetect Class and Source Paths");

      desc[5].setDisplayName("User was notified to review path settings");
      desc[5].setShortDescription("User was notified to review path settings");
      desc[5].setHidden(true);

      desc[6].setHidden(true);
      desc[7].setHidden(true);
      desc[8].setHidden(true);
    } catch (IntrospectionException e) {
      log.warn("IntrospectionException", e);
      throw new RuntimeException(e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return desc;
  }

  public Image getIcon(int type) {
    String path = net.sf.refactorit.ui.UIResources.class.getName();
    // there are rumours that class.getPackage() sometimes doesn't work, so..
    path = path.substring(0, path.lastIndexOf('.')).replace('.', '/');
    path += "/images/RefactorIt.gif";
    Image image = org.openide.util.Utilities.loadImage(path);
    if (image == null) {
      image = org.openide.util.Utilities.loadImage('/' + path);
    }
    return image;
  }
}
