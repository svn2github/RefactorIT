
class T3107v15 {
    void foo(int i) {
	switch (i) {
	    case 0:
	    case (("\b" == "\u0008") ? 1 : 0):
	    case (("\10" == "\u0008") ? 2 : 0):
	}
    }
}
    