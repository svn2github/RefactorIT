
class T3107v14 {
    void foo(int i) {
	switch (i) {
	    case 0:
	    case (('\t' == 9) ? 1 : 0):
	    case (('\t' == '\011') ? 2 : 0):
	}
    }
}
    