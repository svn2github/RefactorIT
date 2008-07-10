package DeclarationSplitterTest;

/**
 * @violations 33
 */
public class Class1 {
	public int /*a comment*/a=1/*a comment*/
		//a comment
	,/*b comment
	b comment
	*/b=new Integer(10).intValue()// b comment
	/**
	* b comment again
	*/
	, 
	c=2/*c comment*/,/*d comment*/d=3,/*d comment*/ // d comment
	/*e comment*/e=4/**e comment*/, // e comment
	f, // f comment
	g=(5*18)/23 /*g comment*/
	, /*h comment */h=6/*h comment*/, // h comment
	i=7 /*i comment*/
	/*i comment*/,/*j comment1*/ 
	/*j comment2*/j=8 /**
					* j comment3
					*/ // j comment4
		/* j comment5*/; /*j comment6*/ /** j comment7*/ // j comment8
}

class Class2 {
	  public static final int VAR_1 = 1, // VAR_1 comment
                          VAR_2 = 2, // VAR_2 comment
                          VAR_3 = 3; // VAR_3 comment
}

class Class3 {
	public static String 
		// this is connection URL
		URL = "www.ee",
		// this is URI
		URI = "/index.html",
		// request parameters
		PARAMS[] = {"abc=1","def=2","ghi=3"};
}

class Class4 {
	public int a[] /*sigle dimension*/, b[][]/*bi-dimensional*/, c[][][]/*three dimensional*/;
}

class Class5 {
    public int[] /*a comment*/a,
                 /*b comment*/b,
                 /*c comment*/c;
}

class Class6 {
	public int x, y= 
        ((2*1303)/144) + // this is very complex mathematical expression
	/*should not consider this comment*/
        144 - (123*12) - // decrease some value
	/*should not consider this comment*/
        1254 - 12; /*the result is ok*/ // yeah, the result
}

class Class7 {
	public /*some comment*/ int 
	/*f comment*/
	f 
	/*f comment*/
	=
	/*f comment*/
	4;
}

class Class8 {
	int x = 1 + // one
			2 + // two
			3 + // three
			4 + // four
			5;  // five
}

class Class9 {
  int x = 1, /*y comment*/ y=2, z, k,j /*j comment*/;
}

class Class10 {
  int 
  /**
   * javadoc for x
   */
  x = 20, // 20 is a very important value
  /*
    multiline for y
  */
  y = 40; // 40 is also very important
}