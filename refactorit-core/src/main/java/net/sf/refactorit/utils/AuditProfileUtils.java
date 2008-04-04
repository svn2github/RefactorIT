/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import org.w3c.dom.Element;

import java.util.Arrays;


/**
 *
 * @author Arseni Grigorjev
 */
public class AuditProfileUtils {
  
  private AuditProfileUtils() {
  }

  
  public static boolean getBooleanAttributeOption(final Element configuration, 
      final String option, boolean defaultValue){
    if (configuration == null){
      return defaultValue;
    }

    String value = configuration.getAttribute(option);
    return Boolean.valueOf(value).booleanValue();
  }
  
  
  
  /**
   * @return boolean value for specified option from profile
   */
  public static boolean getBooleanOption(final Element configuration, 
      final String optionPrefix, final String option, boolean defaultValue){
    if (configuration == null){
      return defaultValue;
    }

    Element skip = (Element) configuration.getElementsByTagName(optionPrefix)
        .item(0);

    if (skip == null) {
      return defaultValue;
    }

    String value = skip.getAttribute(option);
    return Boolean.valueOf(value).booleanValue();
  }

  /**
   * @return int value for specified option from profile
   */
  public static int getIntOption(final Element configuration,
      final String optionPrefix, final String option, int defaultValue){
    if (configuration == null){
      return defaultValue;
    }
        
    int len = defaultValue;
    Element optionsElement = (Element) configuration.getElementsByTagName(
        optionPrefix).item(0);
    if (optionsElement == null) {
      return len;
    }

    String value = optionsElement.getAttribute(option);
    try {
      len = Integer.parseInt(value);
    } catch (NumberFormatException e){}

    return len;
  }
    
  public static String[] getStringOptionsList(final Element configuration,
      final String optionPrefix, final String option){
    String[] optionsList = new String[0];
    
    Element optionsElement = null;
    try{
      optionsElement = (Element) configuration
          .getElementsByTagName(optionPrefix).item(0);
    } catch (NullPointerException e){
      return optionsList;
    }

    String[] skiped = null;
    try{
      skiped = optionsElement.getAttribute(option).split(";");
    } catch (NullPointerException e){}
    
    int badValues = 0;
    if (skiped != null){
      for (int i = 0; i < skiped.length; i++){
        if (skiped[i].trim().length() == 0){
          skiped[i] = null;
          badValues++;
        }
      }
      
      optionsList = new String[skiped.length - badValues];
      for (int i = 0, j = 0; i < skiped.length; i++){
        if (skiped[i] != null){
          optionsList[j++] = skiped[i];
        }
      }
    }
    
    Arrays.sort(optionsList);
    
    return optionsList;
  }
}
