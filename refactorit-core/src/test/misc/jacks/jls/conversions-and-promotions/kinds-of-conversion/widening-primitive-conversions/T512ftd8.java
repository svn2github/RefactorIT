
class T512ftd8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((double)Float.NaN != Double.NaN) ? 1 : 0):
            case (((double)Float.NaN != (double)Float.NaN) ? 2 : 0):
        }
    }
}
