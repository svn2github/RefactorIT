
class T513nan4 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((long) Double.NaN == 0L) ? 1 : 0):
            case (((long) -Double.NaN == 0L) ? 2 : 0):
        }
    }
}
