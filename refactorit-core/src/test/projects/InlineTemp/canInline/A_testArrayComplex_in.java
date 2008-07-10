package test.projects.InlineTemp.canInline;
class A {
	void f() {
		Object[][] x[][] = new Object[][][][] {{{{"a", "b"}}}};
    System.out.println(x);
	}
}
