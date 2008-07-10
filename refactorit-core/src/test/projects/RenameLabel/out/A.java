package RenameLabel;

public class A {
  {
    staticNew:
    for(int k = 0; k < 10;) {
      if(k == 8) {
        break staticNew;
      }
    }
  }
  
  public static void meth1() {
    loop1New:
    for(int i=0; i < 10; i++) {
      loop2:
      for(int j = 0; j<10; j++) {
        if(j == i*2) {
          break loop1New;
        } else if( j < 5){
          continue loop2;
        } 
      }
  
      if(i < 100) {
        continue;
      }
      
      if(true) {
        continue loop1New;
      } else if (false){
        break loop1New;
      }
      break;
    }
  
    class LocalClass {
      public void meth() {
        localNew:
        for(;;) {
          break localNew;
        }
      }
    }
  }
  
  private class InnerClass {
    public void meth() {
      innerNew:
      for(;;) {
        break innerNew;
      }
    }
  }
  
  public void testAnonymous() {
    Object o = new Object() {
      public String toString() {
        anonymousNew:
        for(;;) {
          break anonymousNew;
        }
        return null;
      }
    };
  }
}