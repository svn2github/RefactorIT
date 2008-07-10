package p;
class A{
  void f(){
    final int j = 0;
    class Local {
      private int i = j;

      void f() {
      }
    }
  }
}