
class T1528p3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((boolean) true) ? 1 : 0):
            case (((int) 1L == (float) 1.0) ? 2 : 0):
            case (((char) 0x61 == 'a') ? 3 : 0):
            case (((char) 97.0D == 'a') ? 4 : 0):
            case (((double) 'a' == 97.0D) ? 5 : 0):
            case (((int) 97L == (int) 'a') ? 6 : 0):
        }
    }
}
