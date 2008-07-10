
class T15262urs3 {
    
	void foo() {}
	void bar() {
	    int i = 1;
	    i >>>= foo();
	}
    
}
