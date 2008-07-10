
class T1525botm1 {
    
        void foo(byte b) {}
        void foo(int i) throws Exception {}

        void bar() {
            boolean bool = true;
            foo(bool ? (byte) 0 : 0);
        }
    
}
