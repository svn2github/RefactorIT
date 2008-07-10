
class T8851aesia5_super {
    private T8851aesia5_super(Object o) {}
    private T8851aesia5_super(String s) {}
    private T8851aesia5_super(boolean state) {}
    private T8851aesia5_super(int i) {}
    private T8851aesia5_super(long l) {}
    private T8851aesia5_super(float f) {}
    private T8851aesia5_super(double d) {}
    private T8851aesia5_super(String s, char c) {}
    private T8851aesia5_super(double d, short s) {}
    private T8851aesia5_super(byte b, float f) {}

    static class T8851aesia5_o extends T8851aesia5_super {
        T8851aesia5_o() { super( new Object() ); }
    }
    static class T8851aesia5_s extends T8851aesia5_super {
        T8851aesia5_s() { super("hello"); }
    }
    class T8851aesia5_b extends T8851aesia5_super {
        T8851aesia5_b() { super(true); }
    }
    class T8851aesia5_i extends T8851aesia5_super {
        T8851aesia5_i() { super(1); }
    }
    class T8851aesia5_f extends T8851aesia5_super {
        T8851aesia5_f() { super(0.0F); }
    }
    class T8851aesia5_d extends T8851aesia5_super {
        T8851aesia5_d() { super(0.0D); }
    }
    Object l_obj = new T8851aesia5_super(0L) {
        void foo() {}
    };
    Object sb_obj = new T8851aesia5_super("hello", 'b') {
        void foo() {}
    };
    Object bf_obj = new T8851aesia5_super((byte) 2, 1.0F) {
        void foo() {}
    };
    
}
