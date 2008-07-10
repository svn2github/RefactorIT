public class B extends A {
  
  private class Inner2 {
    void usesBar() {
      bar();
    }
  }
}