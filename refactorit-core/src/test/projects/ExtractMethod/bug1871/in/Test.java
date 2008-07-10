public class Test {
  void method() {
    int var = 0;
    int x = 0;
    /*]*/++var;
    var = x;/*[*/
    int k = var;
  }
}
