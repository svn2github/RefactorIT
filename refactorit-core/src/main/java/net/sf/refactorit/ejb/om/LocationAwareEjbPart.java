/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ejb.om;


import net.sf.refactorit.source.SourceCoordinate;

import org.xml.sax.Locator;

/**
 * @author jura
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class LocationAwareEjbPart {
  int type = -1;
  Object value = null;
  LocationRange locationRange;

  public LocationAwareEjbPart(int type, Locator locator) {
    this.locationRange=new LocationRange();
    this.locationRange.setStartLocator(locator);
    this.type = type;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(locationRange.toString());
    sb.append(value.toString());
    return sb.toString();
  }

  public boolean hasCoordinate(SourceCoordinate coordinate) {
    return locationRange.hasCoordinate(coordinate);
  }

  public void setEndLocator(Locator locator) {
    this.locationRange.setEndLocator(locator);
  }

  public void setStartLocator(Locator locator) {
    this.locationRange.setStartLocator(locator);
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Locator getStartLocator() {
    return this.locationRange.getStartLocator();
  }
  
  public Locator getEndLocator() {
    return this.locationRange.getEndLocator();
  }

  public LocationRange getLocationRange() {
    return locationRange;
  }

}
