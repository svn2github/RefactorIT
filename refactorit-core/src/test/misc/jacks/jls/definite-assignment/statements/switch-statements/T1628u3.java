
class T1628u3 {
    void foo(int i) {
        int j;
        switch (i) {
            case 0: j = 0; break;
            case 1: break;
            default: j = 2; break;
        }
        j++;
    }
}
    