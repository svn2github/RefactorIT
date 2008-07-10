
class T513nan5 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((float) Double.NaN != (float) Double.NaN) ? 1 : 0):
        }
    }
}
