
class T1528qns3 {
    
        static String s = "1";
        static final String s1 = s;
        void foo(int j) {
            switch (j) {
                case 0:
                case ((T1528qns3.s1 == "1") ? 1 : 0):
            }
        }
    
}
