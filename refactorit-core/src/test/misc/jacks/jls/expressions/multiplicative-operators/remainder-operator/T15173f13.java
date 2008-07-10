
class T15173f13 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1e30f % 1e-40f == 7.121e-42f) ? 1 : 0):
            case ((1e30f % -1e-40f == 7.121e-42f) ? 2 : 0):
            case ((-1e-40f % 1e30f == -1e-40f) ? 3 : 0):
            case ((-1e-40f % -1e30f == -1e-40f) ? 4 : 0):
        }
    }
}
