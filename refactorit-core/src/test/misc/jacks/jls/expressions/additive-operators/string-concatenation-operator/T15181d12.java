
class T15181d12 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + -1.2345678912345678e-200 == "-1.2345678912345678E-200") ? 1 : 0):
        }
    }
}
