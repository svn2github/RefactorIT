
class T15173f9 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1f % 0f != 1f % 0f) ? 1 : 0):
            case ((-1f % 0f != -1f % 0f) ? 2 : 0):
            case ((1f % -0f != 1f % -0f) ? 3 : 0):
            case ((-1f % -0f != -1f % -0f) ? 4 : 0):
        }
    }
}
