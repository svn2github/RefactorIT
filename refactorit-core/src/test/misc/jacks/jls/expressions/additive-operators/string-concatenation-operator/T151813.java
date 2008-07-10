
class T151813 {
    void foo() {
        String n1 = null + (String)null;
        String n2 = (String)null + null;
        String n3 = (String)null + (String)null;
    }
}
