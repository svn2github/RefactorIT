
class T513nan3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((long) Float.NaN == 0L) ? 1 : 0):
            case (((long) -Float.NaN == 0L) ? 2 : 0):
        }
    }
}
