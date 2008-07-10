
class Test4 {

  int field = 0;

  void fun() {
    int i = 0;
    while (field < 2) {
      field++;
      /*]*/
      i = newmethod(i);
      /*[*/
    }
  }

  int newmethod(int i) {
    System.out.println("i, field == " + i++ + ", " + field);

    return i;
  }
}
