
class T15173d9 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1d % 0d != 1d % 0d) ? 1 : 0):
            case ((-1d % 0d != -1d % 0d) ? 2 : 0):
            case ((1d % -0d != 1d % -0d) ? 3 : 0):
            case ((-1d % -0d != -1d % -0d) ? 4 : 0):
        }
    }
}
