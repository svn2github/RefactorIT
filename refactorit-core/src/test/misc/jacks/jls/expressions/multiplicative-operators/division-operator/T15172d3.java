
class T15172d3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-1d / -2d > 0d) ? 1 : 0):
            case ((1d / 2d > 0d) ? 2 : 0):
        }
    }
}
