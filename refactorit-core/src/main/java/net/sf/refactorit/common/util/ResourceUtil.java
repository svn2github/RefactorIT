/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.util.Enumeration;
import java.util.Properties;
import javax.swing.ImageIcon;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


/**
 * A set of static functions to provide unified way of getting bundles,
 * images, icons, resources etc.
 *
 * @author Anton Safonov
 */
public final class ResourceUtil {

  /**
   * E.g. if baseclass is <code>a.b.c</code>, then it will seek for:<br>
   * /a/b/resources/&lt;name&gt;_&lt;i18n&l10n_stuff&gt;.properties<br>
   * ...<br>
   * /a/b/resources/&lt;name&gt;.properties
   *
   * @param baseclass determines the package where to seek for bundle.
   * @param name of the bundle.
   *
   * @return {@link ResourceBundle}
   */
  public static final ResourceBundle getBundle(Class baseclass, String name) {
// The following commented code somehow doesn't work, so made a hack
//    return ResourceBundle.getBundle(baseclass.getPackage().getName()
//      + ".resources." + name);

    // FIXME: add locale support later
    try {
      return new PropertyResourceBundle(baseclass.getResourceAsStream(
          "resources/" + name + ".properties"));
    } catch (Exception e) {
      throw new java.util.MissingResourceException(
          "Can't find bundle for base name " + baseclass.getName() + ": " + e,
          baseclass.getName(), "");
    }
  }

  /**
   * E.g. if baseclass is <code>a.b.c</code>, then it will seek for:<br>
   * /a/b/resources/LocalizedStrings_&lt;i18n&l10n_stuff&gt;.properties<br>
   * ...<br>
   * /a/b/resources/LocalizedStrings.properties
   *
   * @param baseclass determines the package where to seek for bundle.
   *
   * @return {@link ResourceBundle}
   */
  public static final ResourceBundle getBundle(Class baseclass) {
    return ResourceUtil.getBundle(baseclass, "LocalizedStrings");
  }

  /**
   * E.g. if baseclass is <code>a.b.c</code>, then it will seek for:<br>
   * /a/b/images/&lt;name&gt
   *
   * @param baseclass determines the package where to seek for icon.
   * @param name of the icon
   *
   * @return {@link ImageIcon}
   */
  public static final ImageIcon getIcon(Class baseclass, String name) {
    try {
      return new ImageIcon(baseclass.getResource("images/" + name));
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * E.g. if baseclass is <code>a.b.c</code>, then it will seek for:<br>
   * /a/b/images/&lt;name&gt
   *
   * @param baseclass determines the package where to seek for image.
   * @param name of the image.
   *
   * @return {@link Image}
   */
  public static final Image getImage(Class baseclass, String name) {
    return Toolkit.getDefaultToolkit().createImage(
        baseclass.getResource("images/" + name));
  }

  /**
   * E.g. if baseclass is <code>a.b.c</code>, then it will seek for:<br>
   * /a/b/resources/&lt;name&gt;<br>
   * and return an {@link URL}
   *
   * @param baseclass determines the package where to seek for resource.
   * @param name of the resource.
   *
   * @return {@link URL}
   */
  public static final URL getResource(Class baseclass, String name) {
    return baseclass.getResource("resources/" + name);
  }

  /**
   * E.g. if baseclass is <code>a.b.c</code>, then it will seek for:<br>
   * /a/b/resources/&lt;name&gt;.properties<br>
   * and return it as stream
   *
   * @param baseclass determines the package where to seek for resource.
   * @param name of the resource.
   *
   * @return {@link InputStream}
   */
  public static final InputStream getResourceAsStream(Class baseclass,
      String name) {
    return baseclass.getResourceAsStream("resources/" + name + ".properties");
  }
  
  
  /**
   * E.g. if baseclass is <code>a.b.c</code>, then it will seek for:<br>
   * /a/b/xsl/&lt;name&gt;.xsl<br>
   * and return it as stream
   *
   * @param baseclass determines the package where to seek for resource.
   * @param name of the resource.
   *
   * @return {@link Source}
   */
  public static final InputStream getXSLResourceAsStream(Class baseclass,
      String name) {
    return baseclass.getResourceAsStream("xsl/" + name + ".xsl");
  }

  /**
   * Converts <code>ResourceBundle</code> to <code>Properties</code> object.
   *
   * @param bundle bundle to be converted.
   *
   * @return <code>Properties</code> object containing same properties as
   *          the bundle.
   *
   * @see ResourceBundle
   * @see Properties
   */
  public static Properties convertBundleToProperties(ResourceBundle bundle) {
    Properties props = new Properties();
    if (bundle != null) {
      for (Enumeration e = bundle.getKeys(); e.hasMoreElements(); ) {
        String key = (String) e.nextElement();
        props.put(key, bundle.getObject(key));
      }
    }

    return props;
  }
}
