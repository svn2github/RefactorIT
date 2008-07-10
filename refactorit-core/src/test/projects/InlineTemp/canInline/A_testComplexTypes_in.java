package test.projects.InlineTemp.canInline;
class A{
  class Inner {
    public Inner(String s) {}
  }
  
	void m(int i){
		test.projects.InlineTemp.canInline.A$Inner instance = new test.projects.InlineTemp.canInline.A$Inner("");
    Object o = instance;
	}
}