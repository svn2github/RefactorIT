package test.projects.InlineTemp.canInline;
class A {
  private int ONE = 1;
  
	void f() {
    int one = A.ONE;
  }
}
