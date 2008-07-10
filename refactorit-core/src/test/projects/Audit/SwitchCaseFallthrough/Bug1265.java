package SwitchCaseFallthrough;

public class Bug1265 {
  
   /**
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   */
  public void test1(){
    
    int x = 0;
    
    switch(x) {
      case 1:
        System.out.println("A");
      case 2:
        int a = 0;
      case 3:
        // Test
        int b = 0;
      default:
        System.out.println("B");
    }
  }
}
