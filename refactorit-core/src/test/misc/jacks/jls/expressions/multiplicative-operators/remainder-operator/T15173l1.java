
class T15173l1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((5L % 3L == 2L) ? 1 : 0):
            case ((5L % -3L == 2L) ? 2 : 0):
            case ((-5L % 3L == -2L) ? 3 : 0):
            case ((-5L % -3L == -2L) ? 4 : 0):
        }
    }
}
