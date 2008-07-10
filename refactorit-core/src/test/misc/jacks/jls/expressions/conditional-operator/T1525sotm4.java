
class T1525sotm4 {
    
        void foo(short s) throws Exception {}
        void foo(int i) {}

        void bar() {
            boolean bool = true;
            foo(bool ? (short) 0 : 32768);
        }
    
}
