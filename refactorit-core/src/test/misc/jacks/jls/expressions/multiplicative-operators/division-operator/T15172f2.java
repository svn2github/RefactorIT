
class T15172f2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.NaN / 1f != Float.NaN / 1f) ? 1 : 0):
            case ((1f / Float.NaN != 1f / Float.NaN) ? 2 : 0):
        }
    }
}
