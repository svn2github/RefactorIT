/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

/**
 * @author risto
 */
public class Attempter {
  private Timing timing = new ForeverLoop();
  
  public Attempter() {
  }
  
  public Attempter(long timeout, long interval) {
    setTiming(timeout, interval);
  }
  
  public boolean attempt(Task task) {
    timing.start();
    
    if(task.attempt()) {
      return true;
    }
    
    while( ! timing.timedOut()) {
      timing.interval();
      
      if(task.attempt()) {
        return true;
      }
    };
    
    return false;
  }

  public void setTiming(Timing newTimeout) {
    timing = newTimeout;
  }
  
  public void setTiming(long timeout, long interval) {
    setTiming(new DefaultTiming(timeout, interval));
  }
  
  public static class DefaultTiming implements Timing {
    private final long timeout;
    private final long interval;
    
    private long start;

    private DefaultTiming(long timeout, long interval) {
      this.timeout = timeout;
      this.interval = interval;
    }

    public void start() {
      start = System.currentTimeMillis();
    }

    public boolean timedOut() {
      return System.currentTimeMillis() - start >= timeout; 
    }

    public void interval() {
      try {
        Thread.sleep(interval);
      } catch(InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static class ForeverLoop implements Timing {
    public void start() {
    }

    public void interval() {
    }
    
    public boolean timedOut() {
      return false;
    }
  }

  public interface Timing {
    void start();
    
    void interval();
    
    boolean timedOut();
  }
  
  public interface Task {
    boolean attempt();
  }
}
