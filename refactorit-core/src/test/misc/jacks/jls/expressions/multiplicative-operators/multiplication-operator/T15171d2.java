
class T15171d2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Double.NaN * 1d != Double.NaN * 1d) ? 1 : 0):
        }
    }
}
