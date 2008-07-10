
class Test2 {

  int field = 0;

  void fun() {
    int i = 0;
    /*]*/
    System.out.println("i, field == " + i++ + ", " + field);
    /*[*/
    System.out.println("i == " + i);
  }
}
