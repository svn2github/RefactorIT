
class T15173f3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((3f % -2f > 0f) ? 1 : 0):
            case ((3f % 2f > 0f) ? 2 : 0):
        }
    }
}
