
class Test {

  void test() {
    final char c = 'a';
    if (/*]*/newmethod(c)/*[*/) {
      test();
    }
  }

  boolean newmethod(final char c) {
    return ((c >= 'a') && (c <= 'z'))
        || ((c >= 'A') && (c <= 'Z'))
        || ((c >= '0') && (c <= '9'))
        || (c == '_') || (c == '-')
        || ((c == '\u00E4') || (c == '\u00C4'))
        || ((c == '\u00E5') || (c == '\u00C5'))
        || ((c == '\u00F6') || (c == '\u00D6'))
        || ((c == '\u00FC') || (c == '\u00DC'));
  }
}
