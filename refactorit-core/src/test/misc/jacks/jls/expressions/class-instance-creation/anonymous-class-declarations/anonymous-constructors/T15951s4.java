
class T15951s4 {
    private T15951s4(int i, byte b) {}
    T15951s4(byte b, int i) {}
}
class Other4 {
    byte b;
    Object o = new T15951s4(b, b) {};
}
    