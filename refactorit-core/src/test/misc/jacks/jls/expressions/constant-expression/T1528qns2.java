
class T1528qns2 {
    
        static String s = "1";
        void foo(int j) {
            switch (j) {
                case 0:
                case ((T1528qns2.s == "1") ? 1 : 0):
            }
        }
    
}
