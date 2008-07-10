
class T15172d5 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((0d / 0d != 0d / 0d) ? 1 : 0):
            case ((-0d / 0d != -0d / 0d) ? 2 : 0):
        }
    }
}
