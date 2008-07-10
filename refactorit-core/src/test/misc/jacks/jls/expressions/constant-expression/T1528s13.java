
class T1528s13 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (true & true) == "true") ? 1 : 0):
            case (("" + (true ^ false) == "true") ? 2 : 0):
            case (("" + (false | true) == "true") ? 3 : 0):
        }
    }
}
