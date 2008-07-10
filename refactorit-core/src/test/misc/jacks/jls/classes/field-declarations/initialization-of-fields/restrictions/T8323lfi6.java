
class T8323lfi6 {
    
        int i = new Object(){ int bar() { return j; } }.bar();
        // not in declaring class
        int j = 1;
    
}
