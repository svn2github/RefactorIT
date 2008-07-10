package p;
class A{
  void f(){
    final int j = 0;
    class Local {
      void f() {
        int i = j;
      }
    }
  }
}