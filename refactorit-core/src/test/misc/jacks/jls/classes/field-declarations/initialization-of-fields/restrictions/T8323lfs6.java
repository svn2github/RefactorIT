
class T8323lfs6 {
    
        static int i = new Object(){ int bar() { return j; } }.bar();
        // not in declaring class
        static int j = 1;
    
}
