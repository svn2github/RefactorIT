
class A extends Exception {

  public void f() {
    try {
      throw new A();
    } catch(Exception e) {
      e.printStackTrace();
    }

  }


}
