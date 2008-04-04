/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ResourceUtil;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 *
 *
 * @author Igor Malinin
 */
public class Version {
  private static final Logger log = AppRegistry.getLogger(Version.class);

  private static final String UNKNOWN = "unknown";
  private static final String VERSION = "version";
  private static final String BUILDID = "buildid";

  private static final Properties properties = new Properties();

  static {
    try {
      InputStream in = ResourceUtil
          .getResourceAsStream(Version.class, VERSION);

      if (in == null) {
        log.error("Loading version failed: version resource is missing");
      } else {
        try {
          properties.load(in);
        } finally {
          in.close();
        }
      }
    } catch (IOException e) {
      log.error("Loading version failed", e);
    }
  }

  public static final String getVersion() {
    return properties.getProperty(VERSION, UNKNOWN);
  }

  public static final String getBuildId() {
    return properties.getProperty(BUILDID, UNKNOWN);
  }
}
