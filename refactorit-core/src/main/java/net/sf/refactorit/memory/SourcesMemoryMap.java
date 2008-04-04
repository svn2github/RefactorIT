/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.memory;


import net.sf.refactorit.common.util.ResourceUtil;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 */
public class SourcesMemoryMap {
  // properties where mappings are saved.
  private static Properties mappings;

  /**
   * @return The memory in MB's for number of sources, or 0 if it cannot be determined
   * 
   */
  public static int getRecommendedMemoryFor(int nrOfSources) {
  	try {
	    initMappings();
	    Enumeration enumer = mappings.propertyNames();
	    while (enumer.hasMoreElements()) {
	      String key = (String) enumer.nextElement();
	      StringTokenizer tokens = new StringTokenizer(key, "-");
	      String firstToken = tokens.nextToken();
	      String secondToken = tokens.nextToken();
	      Integer first = new Integer(firstToken);
	
	      Integer second = null;
	      if (secondToken.equals("*")) {
	        second = new Integer(Integer.MAX_VALUE);
	      } else {
	        second = new Integer(secondToken);
	      }
	
	      if ((nrOfSources >= first.intValue()) && (nrOfSources <= second.intValue())) {
	        return Integer.parseInt(mappings.getProperty(key));
	      }
	    }
	
	    return 0;
  	} catch (Exception e) {
  		System.err.println("Failed to determine recommended memory:" + e);
  		return 0;
  	}
  }

  /**
   */
  private static void initMappings() throws Exception {
    if (mappings == null) {
      InputStream in = ResourceUtil
          .getResourceAsStream(SourcesMemoryMap.class, "SourcesToMemoryMap");

      mappings = new Properties();
      mappings.load(in);
    }
  }
}
