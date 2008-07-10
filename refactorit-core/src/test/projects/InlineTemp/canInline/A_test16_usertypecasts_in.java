package test.projects.InlineTemp.canInline;
class Test {
	void m(){
		Object object = "a" + "b";
		String s = (String) object;
	}
}
