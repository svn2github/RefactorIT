
class T15171f12 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1e30f * 1e-40f == 9.999946e-11f) ? 1 : 0):
            case ((1e30f * -1e-40f == -9.999946e-11f) ? 2 : 0):
            case ((1e-40f * -1e30f == -9.999946e-11f) ? 3 : 0):
            case ((-1e-40f * -1e30f == 9.999946e-11f) ? 4 : 0):
        }
    }
}
