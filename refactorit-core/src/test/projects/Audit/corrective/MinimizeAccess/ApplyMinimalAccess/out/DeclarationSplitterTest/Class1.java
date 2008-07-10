package DeclarationSplitterTest;

/**
 * @violations 33
 */
public class Class1 {
	private int a = 1; /*a comment*/
                     /*a comment*/
                     //a comment
  private int b = new Integer(10).intValue(); /*b comment
                                               b comment
                                               */
                                              // b comment
                                              /**
                                               * b comment again
                                               */
  private int c = 2; /*c comment*/
  private int d = 3; /*d comment*/
                     /*d comment*/
                     // d comment
  private int e = 4; /*e comment*/
                     /**e comment*/
                     // e comment
  private int f; // f comment
  private int g = (5*18)/23; /*g comment*/
  private int h = 6; /*h comment */
                     /*h comment*/
                     // h comment
  private int i = 7; /*i comment*/
                     /*i comment*/
  private int j = 8; /*j comment1*/
                     /*j comment2*/
                     /**
                      * j comment3
                      */
                     // j comment4
                     /* j comment5*/
                     /*j comment6*/
                     /** j comment7*/
                     // j comment8   
}

class Class2 {
	  private static final int VAR_1 = 1; // VAR_1 comment
    private static final int VAR_2 = 2; // VAR_2 comment
    private static final int VAR_3 = 3; // VAR_3 comment 
}

class Class3 {
	private static String URL = "www.ee"; // this is connection URL
  private static String URI = "/index.html"; // this is URI
  private static String[] PARAMS = {"abc=1","def=2","ghi=3"}; // request parameters
}

class Class4 {
	private int[] a; /*sigle dimension*/
  private int[][] b; /*bi-dimensional*/
  private int[][][] c; /*three dimensional*/
}

class Class5 {
    private int[] a; /*a comment*/
    private int[] b; /*b comment*/
    private int[] c; /*c comment*/
}

class Class6 {
	private int x;
  private int y = ((2*1303)/144) + // this is very complex mathematical expression
	/*should not consider this comment*/
        144 - (123*12) - // decrease some value
	/*should not consider this comment*/
        1254 - 12; /*the result is ok*/
                   // yeah, the result  
}

class Class7 {
	private /*some comment*/ int 
	/*f comment*/
	f 
	/*f comment*/
	=
	/*f comment*/
	4;
}

class Class8 {
	private int x = 1 + // one
			2 + // two
			3 + // three
			4 + // four
			5;  // five
}

class Class9 {
  private int x = 1;
  private int y = 2; /*y comment*/
  private int z;
  private int k;
  private int j; /*j comment*/
}

class Class10 {
  private int x = 20; /**
                        * javadoc for x
                        */
                       // 20 is a very important value
  private int y = 40; /*
                       multiline for y
                       */
                      // 40 is also very important 
}