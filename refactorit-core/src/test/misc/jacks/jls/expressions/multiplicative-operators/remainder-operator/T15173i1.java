
class T15173i1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((5 % 3 == 2) ? 1 : 0):
            case ((5 % -3 == 2) ? 2 : 0):
            case ((-5 % 3 == -2) ? 3 : 0):
            case ((-5 % -3 == -2) ? 4 : 0):
        }
    }
}
