
class T1525botm2 {
    
        void foo(byte b) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            int i = 0;
            foo(bool ? (byte) 0 : i);
        }
    
}
