
class T15172l1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((5L / 3L == 1L) ? 1 : 0):
            case ((5L / -3L == -1L) ? 2 : 0):
            case ((-5L / 3L == -1L) ? 3 : 0):
            case ((-5L / -3L == 1L) ? 4 : 0):
        }
    }
}
