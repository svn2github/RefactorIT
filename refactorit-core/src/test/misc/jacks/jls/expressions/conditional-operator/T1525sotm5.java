
class T1525sotm5 {
    
        void foo(short s) throws Exception {}
        void foo(int i) {}

        void bar() {
            foo(true ? (short) 0 : 32768);
        }
    
}
