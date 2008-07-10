
class T1628u2 {
    void foo(int i) {
        int j;
        switch (i) {
            case 0: j = 0; break;
            case 1: j = 1; break;
            default: j = 2; break;
        }
        j++;
    }
}
    