package test.projects.InlineTemp.canInline;
class A{
  void m(){
    Object[] x = getArray();
    System.out.println(x);
  }
  
  Object[] getArray() {
    return null;
  }
}