package test.projects.InlineTemp.canInline;
class A{
  void m(){
    System.out.println(getArray());
  }
  
  Object[] getArray() {
    return null;
  }
}