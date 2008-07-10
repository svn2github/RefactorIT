
class T151815 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1 + 2 + " fiddlers" == "3 fiddlers") ? 1 : 0):
            case (("fiddlers " + 1 + 2 == "fiddlers 12") ? 2 : 0):
        }
    }
}
