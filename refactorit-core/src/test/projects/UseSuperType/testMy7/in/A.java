
class A extends Exception {

  public void f() {
    try {
      throw new A();
    } catch(A e) {
      e.printStackTrace();
    }

  }


}
