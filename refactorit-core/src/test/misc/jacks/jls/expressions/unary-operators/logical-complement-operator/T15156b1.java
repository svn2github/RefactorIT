
class T15156b1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((!true == false) ? 1 : 0):
        }
    }
}
