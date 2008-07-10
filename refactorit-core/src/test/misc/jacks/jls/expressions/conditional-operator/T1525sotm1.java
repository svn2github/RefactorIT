
class T1525sotm1 {
    
        void foo(short s) {}
        void foo(byte b) throws Exception {}
        void foo(int i) throws Exception {}

        void bar() {
            boolean bool = true;
            foo(bool ? (byte) 0 : (short) 0);
        }
    
}
