public class UnicodeCharLiterals {
  String a = "\u005c"; 
  String b = "\u005cn\u005crabc";
  String c = "\u005ct\u005c\u000d\u005c\u000a";
  String d = "\u005c'\\\u005ctabc";
  String e = "int\u005bx\u005d";

  char o = '\u005c'; // legal, \u005c == \
//  char s = '\\u005c'; - not legal
}
