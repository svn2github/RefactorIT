
class T15172f14 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1e-10f / 1e-40f == 1.0000054E30f) ? 1 : 0):
            case ((1e-10f / -1e-40f == -1.0000054E30f) ? 2 : 0):
            case ((1e-40f / -1e-10f == -9.999946E-31f) ? 3 : 0):
            case ((-1e-40f / -1e-10f == 9.999946E-31f) ? 4 : 0):
        }
    }
}
