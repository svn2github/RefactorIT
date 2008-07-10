
class T1410is4 {
    void foo(int n) {
        int q = (n+7)/8;
        switch (n%8) {
            case 0:         do {    foo();          // Great C hack, Tom,
            case 7:                 foo();          // but it's not valid here.
            case 6:                 foo();
            case 5:                 foo();
            case 4:                 foo();
            case 3:                 foo();
            case 2:                 foo();
            case 1:                 foo();
                            } while (--q >= 0);
        }
    }
}
    