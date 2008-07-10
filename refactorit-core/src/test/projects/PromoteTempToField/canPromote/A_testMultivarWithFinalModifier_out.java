package test.projects.PromoteTempToField.canPromote;
//initialize in method
class A{
  int i;

  void f(){
    final int x = 0;
    i = 1;
    final int j = 2;
  }
}