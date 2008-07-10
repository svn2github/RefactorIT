
class T15173d4 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-3d % 2d < 0d) ? 1 : 0):
            case ((-3d % -2d < 0d) ? 2 : 0):
        }
    }
}
