package test.projects.InlineTemp.canInline;
class A {
	void f() {
		Object[][] y[][] = new Object[][][][] {{{{"c", "d"}}}};
    System.out.println(new Object[][][][] {{{{"a", "b"}}}});
	}
}
