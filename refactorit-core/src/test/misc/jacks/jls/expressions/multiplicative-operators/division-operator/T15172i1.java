
class T15172i1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((5 / 3 == 1) ? 1 : 0):
            case ((5 / -3 == -1) ? 2 : 0):
            case ((-5 / 3 == -1) ? 3 : 0):
            case ((-5 / -3 == 1) ? 4 : 0):
        }
    }
}
