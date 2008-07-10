package issue596;

/**
 * @violations 0
 */
public class B extends A {
  
  private class Inner2 {
    void usesBar() {
      bar();
    }
  }
}