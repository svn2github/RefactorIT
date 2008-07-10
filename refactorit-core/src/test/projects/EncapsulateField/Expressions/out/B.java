public class B {
  
  private A a = new A();
  
  private void doSomethingWithLocalA() {
    A a = new A();
    int b = a.getTest();
    a.setTest(0);
    a.setTest(a.getTest() + 5);
  }

  private void doSomethingWithMemberA() {
    int b = a.getTest();
    a.setTest(0);
    a.setTest(a.getTest() + 5);
  }
}