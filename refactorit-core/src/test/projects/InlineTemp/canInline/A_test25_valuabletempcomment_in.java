package test.projects.InlineTemp.canInline;

class A {
	void foo() {
		final int value= 42;int x=0;//valuable temp comment
		// some valuable important comment which will be erased
		System.out.println(value);
	}
}