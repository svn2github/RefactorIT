/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ejb;

/**
 * @author jura
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RitEjbConstants {
  public final static int EJB_CLASS = 0;
  public final static int EJB_NAME = 1;
  public final static int REMOTE = 2;
  public final static int HOME = 3;
  public final static int LOCAL_HOME = 4;
  public final static int LOCAL = 5;
  public final static int SERVICE_ENDPOINT = 6;
  public final static int PRIM_KEY_CLASS = 7;
  public final static String[] TAGS = {"ejb-class", "ejb-name", "remote",
      "home", "local-home", "local", "service-endpoint", "prim-key-class"};
  public final static String[] CONV_SUFFIXES = {"Bean", "Bean", "Remote", "RemoteHome",
      "LocalHome", "Local", "", "PK"};
  public final static String[] NAMES_IN_UI = {"EJB class", "Bean name",
      "Remote interface", "Home Interface", "Local home interface",
      "Local interface", "Web-Service endpoint interface","Primary key class"};
}
