
class T15154d1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-Double.NaN != -Double.NaN) ? 1 : 0):
        }
    }
}
