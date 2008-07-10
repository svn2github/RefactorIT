package test.projects.InlineTemp.canInline;
class A {
  private int ONE_COMPLEX = 0 + 1;
  
	void f() {
    int oneComplex = ONE_COMPLEX;
    int twoMixed = 1 + ONE_COMPLEX;
    int twoComplex = ONE_COMPLEX + ONE_COMPLEX;
  }
}
