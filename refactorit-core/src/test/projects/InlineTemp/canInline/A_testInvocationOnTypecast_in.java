package test.projects.InlineTemp.canInline;
class A{
  void m(Object o) {
    char firstChar = ((String)o).charAt(0);
    System.out.println(firstChar);
  }
}