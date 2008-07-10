package test.projects.PromoteTempToField.canPromote;

class A{
  void f(){
    int i = 0;
    
    int x = i;
    class Local {
      int usage = i;
    }
  }
}