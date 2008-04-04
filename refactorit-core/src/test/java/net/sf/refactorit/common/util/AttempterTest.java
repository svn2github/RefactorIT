/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;


import junit.framework.TestCase;

/**
 * @author risto
 */
public class AttempterTest extends TestCase {
  private int invocationCount;
  private Attempter a;
  
  public void setUp() {
    invocationCount = 0;
    a = new Attempter();
  }
  
  public void testSucceessOnFirstAttempt() {
    boolean result = a.attempt(new Attempter.Task() {
      public boolean attempt() {
        invocationCount++;
        return true;
      }
    } );
    
    assertEquals(1, invocationCount);
    assertTrue(result);
  }
  
  public void testSucceessOnSecondAttempt() {
    boolean result = a.attempt(new Attempter.Task() {
      public boolean attempt() {
        invocationCount++;
        return invocationCount == 2;
      }
    } );
    
    assertEquals(2, invocationCount);
    assertTrue(result);
  }
  
  public void testTimeout() {
    final boolean timerTimedOut[] = new boolean[] {false};
    
    a.setTiming(new Attempter.Timing() {
      public void start() {}

      public void interval() {}
      
      public boolean timedOut() { return timerTimedOut[0]; }
    } );
    
    boolean result = a.attempt(new Attempter.Task() {
      public boolean attempt() {
        invocationCount++;
        if(invocationCount == 5) {
          timerTimedOut[0] = true; 
        }
        
        return false;
      }
    } );
    
    assertEquals(5, invocationCount);
    assertFalse(result);
  }
  
  public void testImmediateTimeout() {
    a.setTiming(new Attempter.Timing() {
      public void start() {}
      
      public void interval() {}
      
      public boolean timedOut() { return true; }
    } );
    
    boolean result = a.attempt(new Attempter.Task() {
      public boolean attempt() {
        invocationCount++;
        return false;
      }
    } );
    
    assertEquals(1, invocationCount);
    assertFalse(result);
  }
  
  public void testTimerStart() {
    a.setTiming(new Attempter.ForeverLoop() {
      public void start() { invocationCount++; }
    } );
    
    a.attempt(new Attempter.Task() {
      public boolean attempt() {return true;}
    } );
    
    assertEquals(1, invocationCount);
  }
  
  public void testInterval_successOnFirstAttempt() {
    final StringBuffer log = new StringBuffer();
    
    a.setTiming(new Attempter.ForeverLoop() {
      public void interval() {log.append("interval ");}
    } );
    
    a.attempt(new Attempter.Task() {
      public boolean attempt() {
        invocationCount++;
        log.append("attempt#" + invocationCount + " ");
        return invocationCount == 1;
      }
    } );
    
    assertEquals("attempt#1 ", log.toString());
  }
  
  public void testInterval_successOnSecondAttempt() {
    final StringBuffer log = new StringBuffer();
    
    a.setTiming(new Attempter.ForeverLoop() {
      public void interval() {log.append("interval ");}
    } );
    
    a.attempt(new Attempter.Task() {
      public boolean attempt() {
        invocationCount++;
        log.append("attempt#" + invocationCount + " ");
        return invocationCount == 2;
      }
    } );
    
    assertEquals("attempt#1 interval attempt#2 ", log.toString());
  }
  
  public void testInterval_timeout() {
    final StringBuffer log = new StringBuffer();
    
    a.setTiming(new Attempter.Timing() {
      public void interval() {log.append("interval ");}

      public void start() {}

      public boolean timedOut() {
        return invocationCount == 2;
      }
    } );
    
    a.attempt(new Attempter.Task() {
      public boolean attempt() {
        invocationCount++;
        log.append("attempt#" + invocationCount + " ");
        return false;
      }
    } );
    
    assertEquals("attempt#1 interval attempt#2 ", log.toString());
  }
  
  public void testRegularUsage() {
    a.setTiming(0, 0);
    
    a.attempt(new Attempter.Task() {
      public boolean attempt() {
        invocationCount++;
        return false;
      }
    } );
    
    assertEquals(1, invocationCount);
  }
}
