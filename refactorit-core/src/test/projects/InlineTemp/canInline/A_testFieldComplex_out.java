package test.projects.InlineTemp.canInline;
class A {
  
	void f() {
    int oneComplex = 0 + 1;
    int twoMixed = 1 + (0 + 1);
    int twoComplex = (0 + 1) + (0 + 1);
  }
}
