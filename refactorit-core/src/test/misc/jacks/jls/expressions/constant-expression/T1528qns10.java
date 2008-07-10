
class T1528qns10 {
    
        final String s = "1";
        void foo(int j) {
            final T1528qns10 t = new T1528qns10();
            switch (j) {
                case 0:
                case ((t.s == "1") ? 1 : 0):
            }
        }
    
}
