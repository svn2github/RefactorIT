
class T1615na10 {
    
	static final boolean x;
	static { x = true; }
	void foo() {
	    if (false ? x = true : true);
	}
    
}
