
class T1525botm3 {
    
        void foo(byte b) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            foo(bool ? (byte) 0 : 128);
        }
    
}
