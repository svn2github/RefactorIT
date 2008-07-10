
class T15262ls3 {
    
	void foo() {}
	void bar() {
	    int i = 1;
	    i <<= foo();
	}
    
}
