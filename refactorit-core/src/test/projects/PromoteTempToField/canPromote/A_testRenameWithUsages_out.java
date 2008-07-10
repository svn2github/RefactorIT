package test.projects.PromoteTempToField.canPromote;

class A{
  private int newName = 0;

  void f(){
    
    int x = newName;
    class Local {
      int usage = newName;
    }
  }
}