package RenameLabel;

public class A {
  {
    staticLoop:
    for(int k = 0; k < 10;) {
      if(k == 8) {
        break staticLoop;
      }
    }
  }
  
  public static void meth1() {
    loop1:
    for(int i=0; i < 10; i++) {
      loop2:
      for(int j = 0; j<10; j++) {
        if(j == i*2) {
          break loop1;
        } else if( j < 5){
          continue loop2;
        } 
      }
  
      if(i < 100) {
        continue;
      }
      
      if(true) {
        continue loop1;
      } else if (false){
        break loop1;
      }
      break;
    }
  
    class LocalClass {
      public void meth() {
        localLoop:
        for(;;) {
          break localLoop;
        }
      }
    }
  }
  
  private class InnerClass {
    public void meth() {
      innerLoop:
      for(;;) {
        break innerLoop;
      }
    }
  }
  
  public void testAnonymous() {
    Object o = new Object() {
      public String toString() {
        anonymousLoop:
        for(;;) {
          break anonymousLoop;
        }
        return null;
      }
    };
  }
}