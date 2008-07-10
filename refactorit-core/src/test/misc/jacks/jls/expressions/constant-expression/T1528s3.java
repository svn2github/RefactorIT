
class T1528s3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (boolean) true == "true") ? 1 : 0):
            case (("" + (int) 1.5 == "1") ? 2 : 0):
        }
    }
}
