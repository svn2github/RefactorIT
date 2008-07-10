
class T15154f1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-Float.NaN != -Float.NaN) ? 1 : 0):
        }
    }
}
