/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.transformations.view;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author  Arseni Grigorjev
 */
public class ProjectViewTransaction {
  
  private final List triads = new ArrayList(40);
  private boolean active = true;

  /** Creates a new instance of ViewTransaction */
  public ProjectViewTransaction() {
  }
  
  public boolean isActive(){
    return this.active;
  }
  
  public void setActive(final boolean active){
    this.active = active;
  }
  
  public List getTriads(){
    return triads;
  }
  
  public void add(Triad triad){
    this.triads.add(triad);
  }
}
