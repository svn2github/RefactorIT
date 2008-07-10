
class T15173f14 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((5.0f % 3.0f == 2.0f) ? 1 : 0):
            case ((5.0f % -3.0f == 2.0f) ? 2 : 0):
            case ((-5.0f % 3.0f == -2.0f) ? 3 : 0):
            case ((-5.0f % -3.0f == -2.0f) ? 4 : 0):
        }
    }
}
