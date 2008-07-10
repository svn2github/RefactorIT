
class T513nan2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((int) Double.NaN == 0) ? 1 : 0):
            case (((int) -Double.NaN == 0) ? 2 : 0):
        }
    }
}
