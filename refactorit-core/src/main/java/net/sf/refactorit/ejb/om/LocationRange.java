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
import org.xml.sax.helpers.LocatorImpl;

/**
 * @author jura
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class LocationRange {
  private Locator startLocator,endLocator;
  
  public String toString() {
    StringBuffer sb=new StringBuffer();
    sb.append('[');
    sb.append(locatorToString(startLocator));
    sb.append('-');
    sb.append(locatorToString(endLocator));
    sb.append(']');
    return sb.toString();
  }
  private String locatorToString(Locator locator) {
    StringBuffer sb=new StringBuffer();
    sb.append(locator.getLineNumber());
    sb.append(':');
    sb.append(locator.getColumnNumber());
    return sb.toString();
  }
  public Locator getEndLocator() {
    return endLocator;
  }
  public void setEndLocator(Locator endLocator) {
    this.endLocator = new LocatorImpl(endLocator);
  }
  public Locator getStartLocator() {
    return startLocator;
  }
  public void setStartLocator(Locator startLocator) {
    this.startLocator = new LocatorImpl(startLocator);
  }
  
  public boolean hasCoordinate(SourceCoordinate coordinate) {
    return isBetweenStartAndEndLines(coordinate)
            || (isOnStartLine(coordinate) && isAfterStartCol(coordinate))
            || (isOnEndLine(coordinate) && isBeforeEndCol(coordinate));
  }

  private boolean isBeforeEndCol(SourceCoordinate coordinate) {
    return coordinate.getColumn()<=endLocator.getColumnNumber();
  }

  private boolean isOnEndLine(SourceCoordinate coordinate) {
    return coordinate.getLine() == endLocator.getLineNumber();
  }

  private boolean isAfterStartCol(SourceCoordinate coordinate) {
    return coordinate.getColumn() >= startLocator.getColumnNumber();
  }

  private boolean isOnStartLine(SourceCoordinate coordinate) {
    return coordinate.getLine() == startLocator.getLineNumber();
  }

  private boolean isBetweenStartAndEndLines(SourceCoordinate coordinate) {
    return (coordinate.getLine() > startLocator.getLineNumber() && coordinate
            .getLine() < endLocator.getLineNumber());
  }
  
}
