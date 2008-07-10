package test.projects.InlineTemp.canInline;
class A {
	void f() {
		Object[][] x[][] = new Object[][][][] {{{{"a", "b"}}}}, y[][] = new Object[][][][] {{{{"c", "d"}}}};
    System.out.println(x);
	}
}
