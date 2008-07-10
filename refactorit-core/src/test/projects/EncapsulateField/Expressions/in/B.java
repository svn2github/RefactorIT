public class B {
  
  private A a = new A();
  
  private void doSomethingWithLocalA() {
    A a = new A();
    int b = a.test;
    a.test = 0;
    a.test += 5;
  }

  private void doSomethingWithMemberA() {
    int b = a.test;
    a.test = 0;
    a.test += 5;
  }
}