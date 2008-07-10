package test.projects.InlineTemp.canInline;
class A{
  class Inner {
    public Inner(String s) {}
  }
  
	void m(int i){
    Object o = new test.projects.InlineTemp.canInline.A$Inner("");
	}
}