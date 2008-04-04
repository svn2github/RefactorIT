/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.standalone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

  
public class IdeVersion {
  public String name = null;
  public String version = null;
  
  public boolean valid = false;
  
  public IdeVersion(final String curretVersionFromNbCoreBundle, final String buildNumber) {
    Pattern p = Pattern.compile("([^0-9]*) ([0-9\\._]*[ 0-9a-zA-Z]*)");
    Matcher m = p.matcher(curretVersionFromNbCoreBundle);
    
    if(m.find()) {
      name = m.group(1);
      version = m.group(2).trim();
 
      // A hack for NB dev versions
      if(name.indexOf(" Dev") >= 0) {
        name = name.substring(0, name.indexOf("Dev")).trim();
        version = "Dev build " + buildNumber;
      }
      
      valid = true;
    }
  }
  
  public boolean isValid() {
    return valid;
  }
  
  public String getName() {
    return name;
  }
  
  public String getVersion() {
    return version;
  }
}
