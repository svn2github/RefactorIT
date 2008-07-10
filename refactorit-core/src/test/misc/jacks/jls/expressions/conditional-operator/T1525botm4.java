
class T1525botm4 {
    
        void foo(byte b) throws Exception {}
        void foo(int i) {}

        void bar() {
            foo(true ? (byte) 0 : 128);
        }
    
}
