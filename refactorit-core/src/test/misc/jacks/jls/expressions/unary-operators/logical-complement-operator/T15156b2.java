
class T15156b2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((!false == true) ? 1 : 0):
        }
    }
}
