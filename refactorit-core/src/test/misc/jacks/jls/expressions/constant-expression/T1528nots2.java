
class T1528nots2 {
    
        final Object o = "1";
        void foo (int j) {
            switch (j) {
                case 0:
                case ((o == "1") ? 1 : 0):
            }
        }
    
}
