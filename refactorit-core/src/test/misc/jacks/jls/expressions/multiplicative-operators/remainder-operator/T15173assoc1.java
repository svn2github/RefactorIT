
class T15173assoc1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((10 % 4 % 3 == 2) ? 1 : 0):
            case ((10 % (4 % 3) == 0) ? 2 : 0):
        }
    }
}
