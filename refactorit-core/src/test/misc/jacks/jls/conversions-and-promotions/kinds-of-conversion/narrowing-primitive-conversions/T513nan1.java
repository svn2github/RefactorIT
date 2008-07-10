
class T513nan1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((int) Float.NaN == 0) ? 1 : 0):
            case (((int) -Float.NaN == 0) ? 2 : 0):
        }
    }
}
