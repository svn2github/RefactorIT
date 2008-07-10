
class T15173f12 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1e30f % 1e-30f == 8.166816e-31f) ? 1 : 0):
            case ((-1e30f % -1e-30f == -8.166816e-31f) ? 2 : 0):
        }
    }
}
