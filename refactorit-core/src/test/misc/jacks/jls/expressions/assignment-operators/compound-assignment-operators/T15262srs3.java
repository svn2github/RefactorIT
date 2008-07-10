
class T15262srs3 {
    
	void foo() {}
	void bar() {
	    int i = 1;
	    i >>= foo();
	}
    
}
