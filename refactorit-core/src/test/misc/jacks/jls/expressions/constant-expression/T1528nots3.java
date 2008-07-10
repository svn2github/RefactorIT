
class T1528nots3 {
    
        final Object o = "1";
        void foo (int j) {
            switch (j) {
                case 0:
                case (((String) o == "1") ? 1 : 0):
            }
        }
    
}
