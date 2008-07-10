
class T3101v5 {
    void foo(int i) {
	switch (i) {
	    case 0:
	    case ((0 == 00) ? 1 : 0):
	    case ((0 == 0x0) ? 2 : 0):
	    case ((00 == 0x0) ? 3 : 0):
	}
    }
}
    