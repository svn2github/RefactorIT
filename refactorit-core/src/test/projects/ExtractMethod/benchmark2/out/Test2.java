
class Test2 {

  int field = 0;

  void fun() {
    int i = 0;
    /*]*/
    i = newmethod(i);
    /*[*/
    System.out.println("i == " + i);
  }

  int newmethod(int i) {
    System.out.println("i, field == " + i++ + ", " + field);

    return i;
  }
}
