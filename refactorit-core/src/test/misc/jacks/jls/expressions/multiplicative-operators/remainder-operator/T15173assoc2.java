
class T15173assoc2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((10.0 % 4.0 % 3.0 == 2.0) ? 1 : 0):
            case ((10.0 % (4.0 % 3.0) == 0.0) ? 2 : 0):
        }
    }
}
