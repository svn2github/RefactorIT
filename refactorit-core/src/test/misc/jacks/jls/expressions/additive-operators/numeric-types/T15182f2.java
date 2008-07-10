
class T15182f2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.NaN + 1f != Float.NaN + 1f) ? 1 : 0):
        }
    }
}
