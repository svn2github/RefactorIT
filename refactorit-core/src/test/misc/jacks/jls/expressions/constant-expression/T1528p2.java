
class T1528p2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1L == 1L) ? 1 : 0):
            case ((1f == 1f) ? 2 : 0):
            case ((1.0 == 1.0) ? 3 : 0):
            case ((true) ? 4 : 0):
        }
    }
}
