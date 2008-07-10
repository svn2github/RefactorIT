
class T15171f10 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1e-22f * 1e-22f == 1e-44f) ? 1 : 0):
            case ((1e-22f * -1e-22f == -1e-44f) ? 2 : 0):
        }
    }
}
