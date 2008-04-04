header {
  package net.sf.refactorit.parser;
}

/** Java 1.5 Recognizer
 *
 * Run 'java Main [-showtree] directory-full-of-java-files'
 *
 * [The -showtree option pops up a Swing frame that shows
 *  the AST constructed from the parser.]
 *
 * Run 'java Main <directory full of java files>'
 *
 * Contributing authors:
 *		John Mitchell		johnm@non.net
 *		Terence Parr		parrt@magelang.com
 *		John Lilley		jlilley@empathy.com
 *		Scott Stanchfield	thetick@magelang.com
 *		Markus Mohnen		mohnen@informatik.rwth-aachen.de
 *		Peter Williams		pete.williams@sun.com
 *		Allan Jacobs		Allan.Jacobs@eng.sun.com
 *		Steve Messick		messick@redhills.com
 *		John Pybus		john@pybus.org
 *
 * Version 1.00 December 9, 1997 -- initial release
 * Version 1.01 December 10, 1997
 *		fixed bug in octal def (0..7 not 0..8)
 * Version 1.10 August 1998 (parrt)
 *		added tree construction
 *		fixed definition of WS,comments for mac,pc,unix newlines
 *		added unary plus
 * Version 1.11 (Nov 20, 1998)
 *		Added "shutup" option to turn off last ambig warning.
 *		Fixed inner class def to allow named class defs as statements
 *		synchronized requires compound not simple statement
 *		add [] after builtInType DOT class in primaryExpression
 *		"const" is reserved but not valid..removed from modifiers
 * Version 1.12 (Feb 2, 1999)
 *		Changed LITERAL_xxx to xxx in tree grammar.
 *		Updated java.g to use tokens {...} now for 2.6.0 (new feature).
 *
 * Version 1.13 (Apr 23, 1999)
 *		Didn't have (stat)? for else clause in tree parser.
 *		Didn't gen ASTs for interface extends.  Updated tree parser too.
 *		Updated to 2.6.0.
 * Version 1.14 (Jun 20, 1999)
 *		Allowed final/abstract on local classes.
 *		Removed local interfaces from methods
 *		Put instanceof precedence where it belongs...in relationalExpr
 *			It also had expr not type as arg; fixed it.
 *		Missing ! on SEMI in classBlock
 *		fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
 *		fixed: didn't like Object[].class in parser or tree parser
 * Version 1.15 (Jun 26, 1999)
 *		Screwed up rule with instanceof in it. :(  Fixed.
 *		Tree parser didn't like (expr).something; fixed.
 *		Allowed multiple inheritance in tree grammar. oops.
 * Version 1.16 (August 22, 1999)
 *		Extending an interface built a wacky tree: had extra EXTENDS.
 *		Tree grammar didn't allow multiple superinterfaces.
 *		Tree grammar didn't allow empty var initializer: {}
 * Version 1.17 (October 12, 1999)
 *		ESC lexer rule allowed 399 max not 377 max.
 *		java.tree.g didn't handle the expression of synchronized
 *		statements.
 * Version 1.18 (August 12, 2001)
 *	  	Terence updated to Java 2 Version 1.3 by
 *		observing/combining work of Allan Jacobs and Steve
 *		Messick.  Handles 1.3 src.  Summary:
 *		o  primary didn't include boolean.class kind of thing
 *	  	o  constructor calls parsed explicitly now:
 * 		   see explicitConstructorInvocation
 *		o  add strictfp modifier
 *	  	o  missing objBlock after new expression in tree grammar
 *		o  merged local class definition alternatives, moved after declaration
 *		o  fixed problem with ClassName.super.field
 *	  	o  reordered some alternatives to make things more efficient
 *		o  long and double constants were not differentiated from int/float
 *		o  whitespace rule was inefficient: matched only one char
 *		o  add an examples directory with some nasty 1.3 cases
 *		o  made Main.java use buffered IO and a Reader for Unicode support
 *		o  supports UNICODE?
 *		   Using Unicode charVocabulay makes code file big, but only
 *		   in the bitsets at the end. I need to make ANTLR generate
 *		   unicode bitsets more efficiently.
 * Version 1.19 (April 25, 2002)
 *		Terence added in nice fixes by John Pybus concerning floating
 *		constants and problems with super() calls.  John did a nice
 *		reorg of the primary/postfix expression stuff to read better
 *		and makes f.g.super() parse properly (it was METHOD_CALL not
 *		a SUPER_CTOR_CALL).  Also:
 *
 *		o  "finally" clause was a root...made it a child of "try"
 *		o  Added stuff for asserts too for Java 1.4, but *commented out*
 *		   as it is not backward compatible.
 *
 * Version 1.20 (October 27, 2002)
 *
 *	  Terence ended up reorging John Pybus' stuff to
 *	  remove some nondeterminisms and some syntactic predicates.
 *	  Note that the grammar is stricter now; e.g., this(...) must
 *	be the first statement.
 *
 *	  Trinary ?: operator wasn't working as array name:
 *		  (isBig ? bigDigits : digits)[i];
 *
 *	  Checked parser/tree parser on source for
 *		  Resin-2.0.5, jive-2.1.1, jdk 1.3.1, Lucene, antlr 2.7.2a4,
 *		and the 110k-line jGuru server source.
 *
 * Version 1.21 (October 17, 2003)
 *  Fixed lots of problems including:
 *  Ray Waldin: add typeDefinition to interfaceBlock in java.tree.g
 *  He found a problem/fix with floating point that start with 0
 *  Ray also fixed problem that (int.class) was not recognized.
 *  Thorsten van Ellen noticed that \n are allowed incorrectly in strings.
 *  TJP fixed CHAR_LITERAL analogously.
 *
 * Version 1.21.2 (March, 2003)
 *	  Changes by Matt Quail to support generics (as per JDK1.5/JSR14)
 *	  Notes:
 *	  o We only allow the "extends" keyword and not the "implements"
 *		keyword, since thats what JSR14 seems to imply.
 *	  o Thanks to Monty Zukowski for his help on the antlr-interest
 *		mail list.
 *	  o Thanks to Alan Eliasen for testing the grammar over his
 *		Fink source base
 *
 * Version 1.22 (July, 2004)
 *	  Changes by Michael Studman to support Java 1.5 language extensions
 *	  Notes:
 *	  o Added support for annotations types
 *	  o Finished off Matt Quail's generics enhancements to support bound type arguments
 *	  o Added support for new for statement syntax
 *	  o Added support for static import syntax
 *	  o Added support for enum types
 *	  o Tested against JDK 1.5 source base and source base of jdigraph project
 *	  o Thanks to Matt Quail for doing the hard part by doing most of the generics work
 *
 * Version 1.22.1 (July 28, 2004)
 *	  Bug/omission fixes for Java 1.5 language support
 *	  o Fixed tree structure bug with classOrInterface - thanks to Pieter Vangorpto for
 *		spotting this
 *	  o Fixed bug where incorrect handling of SR and BSR tokens would cause type
 *		parameters to be recognised as type arguments.
 *	  o Enabled type parameters on constructors, annotations on enum constants
 *		and package definitions
 *	  o Fixed problems when parsing if ((char.class.equals(c))) {} - solution by Matt Quail at Cenqua
 *
 * Version 1.22.2 (July 28, 2004)
 *	  Slight refactoring of Java 1.5 language support
 *	  o Refactored for/"foreach" productions so that original literal "for" literal
 *	    is still used but the for sub-clauses vary by token type
 *	  o Fixed bug where type parameter was not included in generic constructor's branch of AST
 *
 * Version 1.22.3 (August 26, 2004)
 *	  Bug fixes as identified by Michael Stahl; clean up of tabs/spaces
 *        and other refactorings
 *	  o Fixed typeParameters omission in identPrimary and newStatement
 *	  o Replaced GT reconcilliation code with simple semantic predicate
 *	  o Adapted enum/assert keyword checking support from Michael Stahl's java15 grammar
 *	  o Refactored typeDefinition production and field productions to reduce duplication
 *
 * This grammar is in the PUBLIC DOMAIN
 */

class JavaRecognizer extends Parser;
options {
	k = 2;							// two token lookahead
	exportVocab=Java;				// Call its vocabulary "Java"
	codeGenMakeSwitchThreshold = 2;	// Some optimizations
	codeGenBitsetTestThreshold = 3;
	defaultErrorHandler = true;
	buildAST = true;
}

tokens {
	BLOCK; MODIFIERS; OBJBLOCK; SLIST; CTOR_DEF; METHOD_DEF; VARIABLE_DEF;
	INSTANCE_INIT; STATIC_INIT; TYPE; CLASS_DEF; INTERFACE_DEF;
	PACKAGE_DEF; ARRAY_DECLARATOR; EXTENDS_CLAUSE; IMPLEMENTS_CLAUSE;
	PARAMETERS; PARAMETER_DEF; LABELED_STAT; TYPECAST; INDEX_OP;
	POST_INC; POST_DEC; METHOD_CALL; EXPR; ARRAY_INIT;
	IMPORT; UNARY_MINUS; UNARY_PLUS; CASE_GROUP; ELIST; FOR_INIT; FOR_CONDITION;
	FOR_ITERATOR; EMPTY_STAT; FINAL="final"; ABSTRACT="abstract";
	STRICTFP="strictfp"; SUPER_CTOR_CALL; CTOR_CALL; VARIABLE_PARAMETER_DEF;
	STATIC_IMPORT; ENUM_DEF; ENUM_CONSTANT_DEF; FOR_EACH_CLAUSE; ANNOTATION_DEF; ANNOTATIONS;
	ANNOTATION; ANNOTATION_MEMBER_VALUE_PAIR; ANNOTATION_FIELD_DEF; ANNOTATION_ARRAY_INIT;
	TYPE_ARGUMENTS; TYPE_ARGUMENT; TYPE_PARAMETERS; TYPE_PARAMETER; WILDCARD_TYPE;
	TYPE_UPPER_BOUNDS; TYPE_LOWER_BOUNDS;
}

{
	/**
	 * Counts the number of LT seen in the typeArguments production.
	 * It is used in semantic predicates to ensure we have seen
	 * enough closing '>' characters; which actually may have been
	 * either GT, SR or BSR tokens.
	 */
	private int ltCounter = 0;

  public void setLineColumn(Token firstToken, ASTImpl ast)
      throws TokenStreamException {
    try {
      ast.setColumn(firstToken.getColumn());
      ast.setLine(firstToken.getLine());
      final TokenExt lastToken = (TokenExt) LT(0);
      ast.setEndColumn(lastToken.getEndColumn());
      ast.setEndLine(lastToken.getEndLine());
    } catch (NullPointerException e) {
      // it's ok
    }
  }

  public void setLineColumnAll(Token firstToken, ASTImpl ast)
      throws TokenStreamException {
    try {
      ast.setColumn(firstToken.getColumn());
      ast.setLine(firstToken.getLine());
      final TokenExt lastToken = (TokenExt) LT(0);
      ast.setEndColumn(lastToken.getEndColumn());
      ast.setEndLine(lastToken.getEndLine());

      for(AST nextAst = ast.getNextSibling(); nextAst != null;
          nextAst = nextAst.getNextSibling()) {
        ASTImpl changeAst = (ASTImpl) nextAst;
        changeAst.setColumn(firstToken.getColumn());
        changeAst.setLine(firstToken.getLine());
        changeAst.setEndColumn(ast.getEndColumn());
        changeAst.setEndLine(ast.getEndLine());
      }
    } catch (NullPointerException e) {
      // it's ok
    }
  }

}

// Compilation Unit: In Java, this is a single file. This is the start
// rule for this parser
compilationUnit
	:	// A compilation unit starts with an optional package definition
		(	(annotations "package")=> packageDefinition
		|	/* nothing */
		)

		// Next we have a series of zero or more import statements
		( importDefinition )*

		// Wrapping things up with any number of class or interface
		// definitions
		( typeDefinition )*

		( EOF )!
	;


// Package statement: optional annotations followed by "package" then the package identifier.
packageDefinition
	options {defaultErrorHandler = true;} // let ANTLR handle errors
	:	annotations p:"package"^ {#p.setType(PACKAGE_DEF);} identifier SEMI!
	;


// Import statement: import followed by a package or class name
importDefinition
	options {defaultErrorHandler = true;}
	{ boolean isStatic = false; }
	:	i:"import"^ {#i.setType(IMPORT);} ( "static"! {#i.setType(STATIC_IMPORT);} )? identifierStar SEMI!
	;

// A type definition is either a class, interface, enum or annotation with possible additional semis.
typeDefinition
	options {defaultErrorHandler = true;}
  { final Token firstToken = LT(1); }
	:	m:modifiers!
		typeDefinitionInternal[#m, firstToken]
	|	SEMI!
	;

// Protected type definitions production for reuse in other productions
protected typeDefinitionInternal[AST mods, Token firstToken]
	:	classDefinition[#mods, firstToken]		// inner class
	|	interfaceDefinition[#mods, firstToken]	// inner interface
	|	enumDefinition[#mods, firstToken]		// inner enum
	|	annotationDefinition[#mods, firstToken]	// inner annotation
	;

// A declaration is the creation of a reference or primitive-type variable
// Create a separate Type/Var tree for each var in the var list.
declaration!
  { final Token firstToken = LT(1); }
	:	m:modifiers t:typeSpec[false] v:variableDefinitions[#m,#t]
		{#declaration = #v;
     setLineColumnAll(firstToken, (ASTImpl) ##);}
	;

// A type specification is a type name with possible brackets afterwards
// (which would make it an array type).
typeSpec[boolean addImagNode]
	:	classTypeSpec[addImagNode]
	|	builtInTypeSpec[addImagNode]
	;

// A class type specification is a class type with either:
// - possible brackets afterwards
//   (which would make it an array type).
// - generic type arguments after
classTypeSpec[boolean addImagNode]
{ final Token firstToken = LT(1); }
	:	classOrInterfaceType[false]

    // Any number of "[]" that occur after a class or built-in type to signify the
    // type is an array
		(options{greedy=true;}: // match as many as possible
			lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);} RBRACK!
		)*

		{
			if ( addImagNode ) {
				#classTypeSpec = #(#[TYPE,"TYPE"], #classTypeSpec);
        setLineColumn(firstToken, (ASTImpl) classTypeSpec_AST);
			}
		}
	;

// A non-built in type name, with possible type parameters
classOrInterfaceType[boolean addImagNode]
	:	IDENT^ (typeArguments)?
		(options{greedy=true;}: // match as many as possible
			DOT^
			IDENT (typeArguments)?
		)*
		{
			if ( addImagNode ) {
				#classOrInterfaceType = #(#[TYPE,"TYPE"], #classOrInterfaceType);
			}
		}
	;

// A specialised form of typeSpec where built in types must be arrays
typeArgumentSpec
	:	classTypeSpec[true]
	|	builtInTypeArraySpec[true]
	;

// A generic type argument is a class type, a possibly bounded wildcard type or a built-in type array
typeArgument
{final Token firstToken = LT(1);}
	:	(	typeArgumentSpec
		|	wildcardType
		)
		{#typeArgument = #(#[TYPE_ARGUMENT,"TYPE_ARGUMENT"], #typeArgument);
		setLineColumn(firstToken, (ASTImpl) ##);}
	;

// Wildcard type indicating all types (with possible constraint)
wildcardType
	:	q:QUESTION^ {#q.setType(WILDCARD_TYPE);}
		(("extends" | "super")=> typeArgumentBounds)?
	;

// Type arguments to a class or interface type
typeArguments
{int currentLtLevel = 0;
 final Token firstToken = LT(1);}
	:
		{currentLtLevel = ltCounter;}
		LT! {ltCounter++;}
		typeArgument
		(options{greedy=true;}: // match as many as possible
			{inputState.guessing != 0 || ltCounter == currentLtLevel + 1}?
			COMMA! typeArgument
		)*

		(	// turn warning off since Antlr generates the right code,
			// plus we have our semantic predicate below
			options{generateAmbigWarnings=false;}:
			typeArgumentsOrParametersEnd
		)?

		// make sure we have gobbled up enough '>' characters
		// if we are at the "top level" of nested typeArgument productions
		{currentLtLevel != 0 || ltCounter == currentLtLevel}?

		{#typeArguments = #(#[TYPE_ARGUMENTS, "TYPE_ARGUMENTS"], #typeArguments);
    setLineColumn(firstToken, (ASTImpl) ##);}
	;

// this gobbles up *some* amount of '>' characters, and counts how many
// it gobbled.
protected typeArgumentsOrParametersEnd
	:	GT! {ltCounter-=1;}
	|	SR! {ltCounter-=2;}
	|	BSR! {ltCounter-=3;}
	;

// Restriction on wildcard types based on super class or derrived class
typeArgumentBounds
	{boolean isUpperBounds = false;}
	:
		( "extends"! {isUpperBounds=true;} | "super"! ) classTypeSpec[false]
		{
			if (isUpperBounds) {
				#typeArgumentBounds
            = #(#[TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS"], #typeArgumentBounds);
			} else {
				#typeArgumentBounds
            = #(#[TYPE_LOWER_BOUNDS,"TYPE_LOWER_BOUNDS"], #typeArgumentBounds);
			}
		}
	;

// A builtin type array specification is a builtin type with brackets afterwards
builtInTypeArraySpec[boolean addImagNode]
{ final Token firstToken = LT(1); }
	:	builtInType
		(options{greedy=true;}: // match as many as possible
			lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);} RBRACK!
		)+

		{
			if ( addImagNode ) {
				#builtInTypeArraySpec = #(#[TYPE,"TYPE"], #builtInTypeArraySpec);
        setLineColumn(firstToken, (ASTImpl) #builtInTypeArraySpec);
			}
		}
	;

// A builtin type specification is a builtin type with possible brackets
// afterwards (which would make it an array type).
builtInTypeSpec[boolean addImagNode]
{ final Token firstToken = LT(1); }
	:	builtInType
    // Any number of "[]" that occur after a class or built-in type to signify the
    // type is an array
		(options{greedy=true;}: // match as many as possible
			lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);} RBRACK!
		)*

		{
			if ( addImagNode ) {
				#builtInTypeSpec = #(#[TYPE,"TYPE"], #builtInTypeSpec);
        setLineColumn(firstToken, (ASTImpl) #builtInTypeSpec);
			}
		}
	;

// A type name. which is either a (possibly qualified and parameterized)
// class name or a primitive (builtin) type
type
	:	classOrInterfaceType[false]
	|	builtInType
	;

// The primitive types.
builtInType
	:	"void"
	|	"boolean"
	|	"byte"
	|	"char"
	|	"short"
	|	"int"
	|	"float"
	|	"long"
	|	"double"
	;

// A (possibly-qualified) java identifier. We start with the first IDENT
// and expand its name by adding dots and following IDENTS
identifier
	:	IDENT ( DOT^ IDENT )*
	;

identifierStar
	:	IDENT
		( DOT^ IDENT )*
		( DOT^ STAR )?
	;

// A list of zero or more modifiers. We could have used (modifier)* in
// place of a call to modifiers, but I thought it was a good idea to keep
// this rule separate so they can easily be collected in a Vector if
// someone so desires
modifiers
	:
		(
			//hush warnings since the semantic check for "@interface" solves the non-determinism
			options{generateAmbigWarnings=false;}:

			modifier
			|
			//Semantic check that we aren't matching @interface as this is not an annotation
			//A nicer way to do this would be nice
			{LA(1)==AT && !LT(2).getText().equals("interface")}? annotation
		)*

		{#modifiers = #([MODIFIERS, "MODIFIERS"], #modifiers);}
	;

// modifiers for Java classes, interfaces, class/instance vars and methods
modifier
	:	"private"
	|	"public"
	|	"protected"
	|	"static"
	|	"transient"
	|	"final"
	|	"abstract"
	|	"native"
//	|	"threadsafe"
	|	"synchronized"
	|	"volatile"
	|	"strictfp"
	;

annotation!
	:	AT! i:identifier ( LPAREN! ( args:annotationArguments )? RPAREN! )?
		{#annotation = #(#[ANNOTATION,"ANNOTATION"], i, args);}
	;

annotations
    :   (annotation)*
		{#annotations = #([ANNOTATIONS, "ANNOTATIONS"], #annotations);}
    ;

annotationArguments
	:	annotationMemberValueInitializer | annotationMemberValuePairs
	;

annotationMemberValuePairs
	:	annotationMemberValuePair ( COMMA! annotationMemberValuePair )*
	;

annotationMemberValuePair!
	:	i:IDENT ASSIGN! v:annotationMemberValueInitializer
		{#annotationMemberValuePair = #(#[ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR"], i, v);}
	;

annotationMemberValueInitializer
	:
		conditionalExpression | annotation | annotationMemberArrayInitializer
	;

// This is an initializer used to set up an annotation member array.
annotationMemberArrayInitializer
	:	lc:LCURLY^ {#lc.setType(ANNOTATION_ARRAY_INIT);}
			(	annotationMemberArrayValueInitializer
				(
					// CONFLICT: does a COMMA after an initializer start a new
					// initializer or start the option ',' at end?
					// ANTLR generates proper code by matching
					// the comma as soon as possible.
					options {
						warnWhenFollowAmbig = false;
					}
				:
					COMMA! annotationMemberArrayValueInitializer
				)*
				(COMMA!)?
			)?
		RCURLY!
	;

// The two things that can initialize an annotation array element are a conditional expression
// and an annotation (nested annotation array initialisers are not valid)
annotationMemberArrayValueInitializer
	:	annotationMemberArrayConditional
	|	annotation
	;

annotationMemberArrayConditional
{final Token firstToken = LT(1); }
	:	(conditionalExpression
		{## = #(#[EXPR,"EXPR"],##);}
		)
{setLineColumn(firstToken, (ASTImpl) ##);}
	;


superClassClause!
	:	( "extends" c:classOrInterfaceType[false] )?
		{#superClassClause = #(#[EXTENDS_CLAUSE,"EXTENDS_CLAUSE"],c);}
	;

// Definition of a Java class
classDefinition![AST modifiers, Token firstToken]
	:	("class" IDENT
		// it _might_ have type paramaters
		(tp:typeParameters)?
		// it _might_ have a superclass...
		sc:superClassClause
		// it might implement some interfaces...
		ic:implementsClause
		// now parse the body of the class
		cb:classBlock
		{#classDefinition = #(#[CLASS_DEF,"CLASS_DEF"],
								modifiers,IDENT,tp,sc,ic,cb);}
	  )
    {setLineColumn(firstToken, (ASTImpl) ##);}
	;

// Definition of a Java Interface
interfaceDefinition![AST modifiers, Token firstToken]
	:	("interface" IDENT
		// it _might_ have type paramaters
		(tp:typeParameters)?
		// it might extend some other interfaces
		ie:interfaceExtends
		// now parse the body of the interface (looks like a class...)
		ib:interfaceBlock
		{#interfaceDefinition = #(#[INTERFACE_DEF,"INTERFACE_DEF"],
									modifiers,IDENT,tp,ie,ib);}
		)
    {setLineColumn(firstToken, (ASTImpl) ##);}
	;

enumDefinition![AST modifiers, Token firstToken]
	:	("enum" IDENT
		// it might implement some interfaces...
		ic:implementsClause
		// now parse the body of the enum
		eb:enumBlock
		{#enumDefinition = #(#[ENUM_DEF,"ENUM_DEF"],
								modifiers,IDENT,ic,eb);}
	  )
    {setLineColumn(firstToken, (ASTImpl) ##);}
	;

annotationDefinition![AST modifiers, Token firstToken]
	:	(AT "interface" IDENT
		// now parse the body of the annotation
		ab:annotationBlock
		{#annotationDefinition = #(#[ANNOTATION_DEF,"ANNOTATION_DEF"],
									modifiers,IDENT,ab);}
		)
    {setLineColumn(firstToken, (ASTImpl) ##);}
	;

typeParameters
{int currentLtLevel = 0;
 final Token firstToken = LT(1); }
	:
		{currentLtLevel = ltCounter;}
		LT! {ltCounter++;}
		typeParameter (COMMA! typeParameter)*
		(typeArgumentsOrParametersEnd)?

		// make sure we have gobbled up enough '>' characters
		// if we are at the "top level" of nested typeArgument productions
		{(currentLtLevel != 0) || ltCounter == currentLtLevel}?

		{#typeParameters = #(#[TYPE_PARAMETERS, "TYPE_PARAMETERS"], #typeParameters);
		setLineColumn(firstToken, (ASTImpl) ##);}
	;

typeParameter
{ final Token firstToken = LT(1); }
	:
		// I'm pretty sure Antlr generates the right thing here:
		(id:IDENT) ( options{generateAmbigWarnings=false;}: typeParameterBounds )?
		{#typeParameter = #(#[TYPE_PARAMETER,"TYPE_PARAMETER"], #typeParameter);
		setLineColumn(firstToken, (ASTImpl) ##);}
	;

typeParameterBounds
	:
		"extends"! classOrInterfaceType[false]
		(BAND! classOrInterfaceType[false])*
		{#typeParameterBounds = #(#[TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS"], #typeParameterBounds);}
	;

// This is the body of a class. You can have classFields and extra semicolons.
classBlock
{ final Token firstToken = LT(1); }
	:	LCURLY!
			( classField | SEMI! )*
		RCURLY!
		{
    AST objBlock = #[OBJBLOCK, "OBJBLOCK"];
    #classBlock = #(objBlock, #classBlock);
    setLineColumn(firstToken, (ASTImpl) objBlock);
    }
	;

// This is the body of an interface. You can have interfaceField and extra semicolons.
interfaceBlock
{ final Token firstToken = LT(1); }
	:	LCURLY!
			( interfaceField | SEMI! )*
		RCURLY!
		{
    AST objBlock = #[OBJBLOCK, "OBJBLOCK"];
    #interfaceBlock = #(objBlock, #interfaceBlock);
    setLineColumn(firstToken, (ASTImpl) objBlock);
    }
	;

// This is the body of an annotation. You can have annotation fields and extra semicolons,
// That's about it (until you see what an annoation field is...)
annotationBlock
{ final Token firstToken = LT(1); }
	:	LCURLY!
		( annotationField | SEMI! )*
		RCURLY!
		{
    AST #objBlock_AST = #[OBJBLOCK, "OBJBLOCK"];
    #annotationBlock = #(objBlock_AST, #annotationBlock);
    setLineColumn(firstToken, (ASTImpl) #objBlock_AST);
    }
	;

// This is the body of an enum. You can have zero or more enum constants
// followed by any number of fields like a regular class
enumBlock
{ final Token firstToken = LT(1); }
	:	LCURLY!
			( enumConstant ( options{greedy=true;}: COMMA! enumConstant )* ( COMMA! )? )?
			( SEMI! ( classField | SEMI! )* )?
		RCURLY!
		{AST #objBlock_AST = #[OBJBLOCK, "OBJBLOCK"];
    #enumBlock = #(objBlock_AST, #enumBlock);
    setLineColumn(firstToken, (ASTImpl) #objBlock_AST);}
	;

// An annotation field
annotationField!
{final Token firstToken = LT(1); }
	:	(mods:modifiers
		(	td:typeDefinitionInternal[#mods, firstToken]
			{#annotationField = #td;}
		|	t:typeSpec[false]		// annotation field
			(	i:IDENT				// the name of the field

				LPAREN! RPAREN!

				rt:declaratorBrackets[#t]

        afd:annotationFieldDefault

				SEMI

				{#annotationField =
					#(#[ANNOTATION_FIELD_DEF,"ANNOTATION_FIELD_DEF"],
						 mods,
						 #(#[TYPE,"TYPE"],rt),
						 i,afd
						 );}
			|	v:variableDefinitions[#mods,#t] SEMI	// variable
				{#annotationField = #v;}
			)
		)
  )
  {setLineColumnAll(firstToken, (ASTImpl) ##);}
	;

annotationFieldDefault
  : ( assign:"default"^ {#assign.setType(ASSIGN);} annotationMemberValueInitializer )?
  ;

//An enum constant may have optional parameters and may have a
//a class body
enumConstant!
{final Token firstToken = LT(1); }
	:	an:annotations
		i:IDENT
		(	LPAREN!
			a:argList
			RPAREN!
		)?
		( b:enumConstantBlock )?
		{#enumConstant = #([ENUM_CONSTANT_DEF, "ENUM_CONSTANT_DEF"], an, i, a, b);
    setLineColumn(firstToken, (ASTImpl) ##);}
	;

//The class-like body of an enum constant
enumConstantBlock
{final Token firstToken = LT(1); }
	:	LCURLY!
		( enumConstantField | SEMI! )*
		RCURLY!
		{AST objBlock = #[OBJBLOCK, "OBJBLOCK"];
    #enumConstantBlock = #(objBlock, #enumConstantBlock);
    setLineColumn(firstToken, (ASTImpl) #objBlock);}
	;

//An enum constant field is just like a class field but without
//the posibility of a constructor definition or a static initializer
enumConstantField!
{final Token firstToken = LT(1); }
	:	(mods:modifiers
		(	td:typeDefinitionInternal[#mods, firstToken]
			{#enumConstantField = #td;}

		|	// A generic method has the typeParameters before the return type.
			// This is not allowed for variable definitions, but this production
			// allows it, a semantic check could be used if you wanted.
			(tp:typeParameters)? t:typeSpec[false]		// method or variable declaration(s)
			(	IDENT									// the name of the method

				// parse the formal parameter declarations.
				LPAREN! param:parameterDeclarationList RPAREN!

				rt:declaratorBrackets[#t]

				// get the list of exceptions that this method is
				// declared to throw
				(tc:throwsClause)?

				( s2:compoundStatement | SEMI )
				{#enumConstantField = #(#[METHOD_DEF,"METHOD_DEF"],
							 mods,
							 tp,
							 #(#[TYPE,"TYPE"],rt),
							 IDENT,
							 param,
							 tc,
							 s2);}
			|	v:variableDefinitions[#mods,#t] SEMI
				{#enumConstantField = #v;}
			)
		)

	// "{ ... }" instance initializer
	|	s4:compoundStatement
		{#enumConstantField = #(#[INSTANCE_INIT,"INSTANCE_INIT"], s4);}
	)
  {setLineColumnAll(firstToken, (ASTImpl) #enumConstantField);}
	;

// An interface can extend several other interfaces...
interfaceExtends
	:	(
		e:"extends"!
		classOrInterfaceType[false] ( COMMA! classOrInterfaceType[false] )*
		)?
		{#interfaceExtends = #(#[EXTENDS_CLAUSE,"EXTENDS_CLAUSE"],
								#interfaceExtends);}
	;

// A class can implement several interfaces...
implementsClause
	:	(
			i:"implements"! classOrInterfaceType[false] ( COMMA! classOrInterfaceType[false] )*
		)?
		{#implementsClause = #(#[IMPLEMENTS_CLAUSE,"IMPLEMENTS_CLAUSE"],
								 #implementsClause);}
	;

// Now the various things that can be defined inside a class
classField!
{final Token firstToken = LT(1); }
	:	// method, constructor, or variable declaration
    (
		mods:modifiers
		(	td:typeDefinitionInternal[#mods, firstToken]
			{#classField = #td;}

		|	(tp:typeParameters)?
			(
				h:ctorHead s:constructorBody // constructor
				{#classField = #(#[CTOR_DEF,"CTOR_DEF"], mods, tp, h, s);}

				|	// A generic method/ctor has the typeParameters before the return type.
					// This is not allowed for variable definitions, but this production
					// allows it, a semantic check could be used if you wanted.
					t:typeSpec[false]		// method or variable declaration(s)
					(	IDENT				// the name of the method

						// parse the formal parameter declarations.
						LPAREN! param:parameterDeclarationList RPAREN!

						rt:declaratorBrackets[#t]

						// get the list of exceptions that this method is
						// declared to throw
						(tc:throwsClause)?

						( s2:compoundStatement | SEMI )
						{#classField = #(#[METHOD_DEF,"METHOD_DEF"],
									 mods,
									 tp,
									 #(#[TYPE,"TYPE"],rt),
									 IDENT,
									 param,
									 tc,
									 s2);}
					|	v:variableDefinitions[#mods,#t] SEMI
						{#classField = #v;}
					)
			)
		)

	// "static { ... }" class initializer
	|	"static" s3:compoundStatement
		{#classField = #(#[STATIC_INIT,"STATIC_INIT"], s3);}

	// "{ ... }" instance initializer
	|	s4:compoundStatement
		{#classField = #(#[INSTANCE_INIT,"INSTANCE_INIT"], s4);}
  )
  {setLineColumnAll(firstToken, (ASTImpl) #classField);}
	;

// Now the various things that can be defined inside a interface
interfaceField!
{final Token firstToken = LT(1); }
	:	// method, constructor, or variable declaration
    (
		mods:modifiers
		(	td:typeDefinitionInternal[#mods, firstToken]
			{#interfaceField = #td;}

		|	(tp:typeParameters)?
			// A generic method has the typeParameters before the return type.
			// This is not allowed for variable definitions, but this production
			// allows it, a semantic check could be used if you want a more strict
			// grammar.
			t:typeSpec[false]		// method or variable declaration(s)
			(	IDENT				// the name of the method

				// parse the formal parameter declarations.
				LPAREN! param:parameterDeclarationList RPAREN!

				rt:declaratorBrackets[#t]

				// get the list of exceptions that this method is
				// declared to throw
				(tc:throwsClause)?

				SEMI

				{#interfaceField = #(#[METHOD_DEF,"METHOD_DEF"],
							 mods,
							 tp,
							 #(#[TYPE,"TYPE"],rt),
							 IDENT,
							 param,
							 tc);}
			|	v:variableDefinitions[#mods,#t] SEMI
				{#interfaceField = #v;}
			)
		)
  )
  {setLineColumnAll(firstToken, (ASTImpl) #interfaceField);}
	;

constructorBody
{final Token firstToken = LT(1);}
	:	(lc:LCURLY^ {#lc.setType(SLIST);}
			( options { greedy=true; } : explicitConstructorInvocation)?
			(statement)*
		RCURLY!)
{setLineColumn(firstToken, (ASTImpl) #constructorBody);}
	;

/** Catch obvious constructor calls, but not the expr.super(...) calls */
explicitConstructorInvocation
{final Token firstToken = LT(1);}
	:	(typeArguments)? // JAVA5: must be checked
  ("this" lp1:LPAREN^ argList RPAREN! SEMI!
		{#lp1.setType(CTOR_CALL);}
	|	"super" lp2:LPAREN^ argList RPAREN! SEMI!
		{#lp2.setType(SUPER_CTOR_CALL);}
  )
{setLineColumn(firstToken, (ASTImpl) #explicitConstructorInvocation);}
	;

variableDefinitions[AST mods, AST t]
	:	variableDeclarator[getASTFactory().dupTree(mods),
							getASTFactory().dupTree(t)]
		(	COMMA!
			variableDeclarator[getASTFactory().dupTree(mods),
							getASTFactory().dupTree(t)]
		)*
	;

/** Declaration of a variable. This can be a class/instance variable,
 *  or a local variable in a method
 *  It can also include possible initialization.
 */
variableDeclarator![AST mods, AST t]
	:	id:IDENT d:declaratorBrackets[t] v:varInitializer
		{#variableDeclarator = #(#[VARIABLE_DEF,"VARIABLE_DEF"], mods, #(#[TYPE,"TYPE"],d), id, v);}
	;

declaratorBrackets[AST typ]
	:	{#declaratorBrackets=typ;}
		(lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);} RBRACK!)*
	;

varInitializer
	:	( ASSIGN^ initializer )?
	;

// This is an initializer used to set up an array.
arrayInitializer
	:	((lc:LCURLY^ {#lc.setType(ARRAY_INIT);}
			(	initializer
				(
					// CONFLICT: does a COMMA after an initializer start a new
					// initializer or start the option ',' at end?
					// ANTLR generates proper code by matching
					// the comma as soon as possible.
					options {
						warnWhenFollowAmbig = false;
					}
				:
					COMMA! initializer
				)*
				(COMMA!)?
			)?
		RCURLY!)
  | (lc2:LCURLY^ {#lc2.setType(ARRAY_INIT);}
    COMMA!
    RCURLY!)
  )
  {if (## != null) {
     final ASTImpl bodyAst = (ASTImpl) ##;
     final TokenExt lastToken = (TokenExt) LT(0);
     bodyAst.setEndLine(lastToken.getEndLine());
     bodyAst.setEndColumn(lastToken.getEndColumn());
   }
  }
	;


// The two "things" that can initialize an array element are an expression
// and another (nested) array initializer.
initializer
	:	expression
	|	arrayInitializer
	;

// This is the header of a method. It includes the name and parameters
// for the method.
// This also watches for a list of exception classes in a "throws" clause.
ctorHead
	:	IDENT // the name of the method

		// parse the formal parameter declarations.
		LPAREN! parameterDeclarationList RPAREN!

		// get the list of exceptions that this method is declared to throw
		(throwsClause)?
	;

// This is a list of exception classes that the method is declared to throw
throwsClause
	:	"throws"^ identifier ( COMMA! identifier )*
	;

// A list of formal parameters
//	 Zero or more parameters
//	 If a parameter is variable length (e.g. String... myArg) it is the right-most parameter
parameterDeclarationList
{final Token firstToken = LT(0);}
	// The semantic check in ( .... )* block is flagged as superfluous, and seems superfluous but
	// is the only way I could make this work. If my understanding is correct this is a known bug
	:	(	( parameterDeclaration )=> parameterDeclaration
			( options {warnWhenFollowAmbig=false;} : ( COMMA! parameterDeclaration ) => COMMA! parameterDeclaration )*
			( COMMA! variableLengthParameterDeclaration )?
		|
			variableLengthParameterDeclaration
		)?
		{#parameterDeclarationList = #(#[PARAMETERS,"PARAMETERS"],
									#parameterDeclarationList);
		  final ASTImpl listAst = (ASTImpl) ##;
      if (listAst != null) {
        listAst.setColumn(((TokenExt) firstToken).getEndColumn());
        listAst.setLine(((TokenExt) firstToken).getEndLine());
        final TokenExt lastToken = (TokenExt) LT(0);
        listAst.setEndColumn(lastToken.getEndColumn());
        listAst.setEndLine(lastToken.getEndLine());
      }
    }
	;

// A formal parameter.
parameterDeclaration!
{final Token firstToken = LT(1);}
	:	pm:parameterModifier t:typeSpec[false] id:IDENT
		pd:declaratorBrackets[#t]
		{AST parameterDef = #[PARAMETER_DEF,"PARAMETER_DEF"];
      #parameterDeclaration = #(#parameterDef, pm, #([TYPE,"TYPE"],pd), id);
    setLineColumn(firstToken, (ASTImpl) #parameterDef);}
	;

variableLengthParameterDeclaration!
	:	pm:parameterModifier t:typeSpec[false] TRIPLE_DOT! id:IDENT
		pd:declaratorBrackets[#t]
		{#variableLengthParameterDeclaration = #(#[VARIABLE_PARAMETER_DEF,"VARIABLE_PARAMETER_DEF"],
												pm, #([TYPE,"TYPE"],pd), id);}
	;

parameterModifier
	//final can appear amongst annotations in any order - greedily consume any preceding
	//annotations to shut nond-eterminism warnings off
	:	(options{greedy=true;} : annotation)* (f:"final")? (annotation)*
		{#parameterModifier = #(#[MODIFIERS,"MODIFIERS"], #parameterModifier);}
	;

// Compound statement. This is used in many contexts:
// Inside a class definition prefixed with "static":
// it is a class initializer
// Inside a class definition without "static":
// it is an instance initializer
// As the body of a method
// As a completely indepdent braced block of code inside a method
// it starts a new scope for variable definitions

compoundStatement
{final Token firstToken = LT(1);}
	:	lc:LCURLY^ {#lc.setType(SLIST);}
			// include the (possibly-empty) list of statements
			(statement)*
		RCURLY!
  {setLineColumn(firstToken, (ASTImpl) ##);}
	;

statement
{final Token firstToken = LT(1); }
	// A list of statements in curly braces -- start a new scope!
	: (	compoundStatement

	// declarations are ambiguous with "ID DOT" relative to expression
	// statements. Must backtrack to be sure. Could use a semantic
	// predicate to test symbol table to see what the type was coming
	// up, but that's pretty hard without a symbol table ;)
	|	(declaration)=> declaration SEMI!

	// An expression statement. This could be a method call,
	// assignment statement, or any other expression evaluated for
	// side-effects.
	|	expression SEMI!

	//TODO: what abour interfaces, enums and annotations
	// class definition
	|	m:modifiers! classDefinition[#m, firstToken]

	// Attach a label to the front of a statement
	|	IDENT c:COLON^ {#c.setType(LABELED_STAT);} statement

	// If-else statement
	|	"if"^ LPAREN! expression RPAREN! statement
		(
			// CONFLICT: the old "dangling-else" problem...
			// ANTLR generates proper code matching
			// as soon as possible. Hush warning.
			options {
				warnWhenFollowAmbig = false;
			}
		:
			"else"! statement
		)?

	// For statement
	|	forStatement

	// While statement
	|	"while"^ LPAREN! expression RPAREN! statement

	// do-while statement
	|	"do"^ statement "while"! LPAREN! expression RPAREN! SEMI!

	// get out of a loop (or switch)
	|	"break"^ (IDENT)? SEMI!

	// do next iteration of a loop
	|	"continue"^ (IDENT)? SEMI!

	// Return an expression
	|	"return"^ (expression)? SEMI!

	// switch/case statement
	|	"switch"^ LPAREN! expression RPAREN! LCURLY!
			( casesGroup )*
		RCURLY!

	// exception try-catch block
	|	tryBlock

	// throw an exception
	|	"throw"^ expression SEMI!

	// synchronize a statement
	|	"synchronized"^ LPAREN! expression RPAREN! compoundStatement

	// asserts (uncomment if you want 1.4 compatibility)
	|	"assert"^ expression ( COLON! expression )? SEMI!

	// empty statement
	|	s:SEMI {#s.setType(EMPTY_STAT);}
  )
  {
    if (## != null) {
      final ASTImpl statementAst = (ASTImpl) ##;
      if (statementAst.getLine() == 0 || statementAst.getType() == LABELED_STAT) {
        statementAst.setLine(firstToken.getLine());
      }
      if (statementAst.getColumn() == 0 || statementAst.getType() == LABELED_STAT) {
        statementAst.setColumn(firstToken.getColumn());
      }
      final TokenExt lastToken = (TokenExt) LT(0);
      statementAst.setEndLine(lastToken.getEndLine());
      statementAst.setEndColumn(lastToken.getEndColumn());
    }
  }
	;

forStatement
	:	f:"for"^
		LPAREN!
			(	(forInit SEMI)=>traditionalForClause
			|	forEachClause
			)
		RPAREN!
		statement					 // statement to loop over
	;

traditionalForClause
	:
		forInit SEMI!	// initializer
		forCond SEMI!	// condition test
		forIter			// updater
	;

forEachClause
	:
		p:parameterDeclaration forEachColon
		{#forEachClause = #(#[FOR_EACH_CLAUSE,"FOR_EACH_CLAUSE"], #forEachClause);}
	;

forEachColon
  :
  COLON^ expression
  ;

casesGroup
	:	(	// CONFLICT: to which case group do the statements bind?
			// ANTLR generates proper code: it groups the
			// many "case"/"default" labels together then
			// follows them with the statements
			options {
				greedy = true;
			}
			:
			aCase
		)+
		caseSList
		{#casesGroup = #([CASE_GROUP, "CASE_GROUP"], #casesGroup);}
	;

aCase
	:	("case"^ expression | "default") COLON!
	;

caseSList
{final Token caseColonToken = LT(0); }
	:	(statement)*
		{#caseSList = #(#[SLIST,"SLIST"],#caseSList);}
{
      final ASTImpl listAst = (ASTImpl) ##;
      if (listAst != null) {
        listAst.setColumn(((TokenExt)caseColonToken).getEndColumn()+1);
        listAst.setLine(((TokenExt)caseColonToken).getEndLine());
        final TokenExt lastToken = (TokenExt) LT(0);
        listAst.setEndColumn(lastToken.getEndColumn());
        listAst.setEndLine(lastToken.getEndLine());
      }
}
	;

// The initializer for a for loop
forInit
		// if it looks like a declaration, it is
	:	((declaration)=> declaration
		// otherwise it could be an expression list...
		|	expressionList
		)?
		{#forInit = #(#[FOR_INIT,"FOR_INIT"],#forInit);}
	;

forCond
	:	(expression)?
		{#forCond = #(#[FOR_CONDITION,"FOR_CONDITION"],#forCond);}
	;

forIter
	:	(expressionList)?
		{#forIter = #(#[FOR_ITERATOR,"FOR_ITERATOR"],#forIter);}
	;

// an exception handler try/catch block
tryBlock
{final Token firstToken = LT(1); }
	:	("try"^ compoundStatement
		(handler)*
		( finallyClause )?
		)
{setLineColumn(firstToken, (ASTImpl) ##);}
	;

finallyClause
{final Token firstToken = LT(1); }
	:	("finally"^ compoundStatement)
{setLineColumn(firstToken, (ASTImpl) ##);}
	;

// an exception handler
handler
{final Token firstToken = LT(1); }
	:	("catch"^ LPAREN! parameterDeclaration RPAREN! compoundStatement)
{setLineColumn(firstToken, (ASTImpl) ##);}
	;


// expressions
// Note that most of these expressions follow the pattern
//   thisLevelExpression :
//	   nextHigherPrecedenceExpression
//		   (OPERATOR nextHigherPrecedenceExpression)*
// which is a standard recursive definition for a parsing an expression.
// The operators in java have the following precedences:
//	lowest  (13)  = *= /= %= += -= <<= >>= >>>= &= ^= |=
//			(12)  ?:
//			(11)  ||
//			(10)  &&
//			( 9)  |
//			( 8)  ^
//			( 7)  &
//			( 6)  == !=
//			( 5)  < <= > >=
//			( 4)  << >>
//			( 3)  +(binary) -(binary)
//			( 2)  * / %
//			( 1)  ++ -- +(unary) -(unary)  ~  !  (type)
//				  []   () (method call)  . (dot -- identifier qualification)
//				  new   ()  (explicit parenthesis)
//
// the last two are not usually on a precedence chart; I put them in
// to point out that new has a higher precedence than '.', so you
// can validy use
//	 new Frame().show()
//
// Note that the above precedence levels map to the rules below...
// Once you have a precedence chart, writing the appropriate rules as below
//   is usually very straightfoward



// the mother of all expressions
expression
{final Token firstToken = LT(1); }
	:	(assignmentExpression
		{#expression = #(#[EXPR,"EXPR"],#expression);}
		)
{setLineColumn(firstToken, (ASTImpl) ##);}
	;


// This is a list of expressions.
expressionList
{final Token firstToken = LT(1); }
	:	(expression (COMMA! expression)*
		{#expressionList = #(#[ELIST,"ELIST"], expressionList);})
{setLineColumn(firstToken, (ASTImpl) ##);}
	;


// assignment expression (level 13)
assignmentExpression
	:	conditionalExpression
		(	(	ASSIGN^
			|	PLUS_ASSIGN^
			|	MINUS_ASSIGN^
			|	STAR_ASSIGN^
			|	DIV_ASSIGN^
			|	MOD_ASSIGN^
			|	SR_ASSIGN^
			|	BSR_ASSIGN^
			|	SL_ASSIGN^
			|	BAND_ASSIGN^
			|	BXOR_ASSIGN^
			|	BOR_ASSIGN^
			)
			assignmentExpression
		)?
	;


// conditional test (level 12)
conditionalExpression
	:	logicalOrExpression
		( QUESTION^ assignmentExpression COLON! conditionalExpression )?
	;


// logical or (||) (level 11)
logicalOrExpression
	:	logicalAndExpression (LOR^ logicalAndExpression)*
	;


// logical and (&&) (level 10)
logicalAndExpression
	:	inclusiveOrExpression (LAND^ inclusiveOrExpression)*
	;


// bitwise or non-short-circuiting or (|) (level 9)
inclusiveOrExpression
	:	exclusiveOrExpression (BOR^ exclusiveOrExpression)*
	;


// exclusive or (^) (level 8)
exclusiveOrExpression
	:	andExpression (BXOR^ andExpression)*
	;


// bitwise or non-short-circuiting and (&) (level 7)
andExpression
	:	equalityExpression (BAND^ equalityExpression)*
	;


// equality/inequality (==/!=) (level 6)
equalityExpression
	:	relationalExpression ((NOT_EQUAL^ | EQUAL^) relationalExpression)*
	;


// boolean relational expressions (level 5)
relationalExpression
	:	shiftExpression
		(	(	(	LT^
				|	GT^
				|	LE^
				|	GE^
				)
				shiftExpression
			)*
		|	"instanceof"^ typeSpec[true]
		)
	;


// bit shift expressions (level 4)
shiftExpression
	:	additiveExpression ((SL^ | SR^ | BSR^) additiveExpression)*
	;


// binary addition/subtraction (level 3)
additiveExpression
	:	multiplicativeExpression ((PLUS^ | MINUS^) multiplicativeExpression)*
	;


// multiplication/division/modulo (level 2)
multiplicativeExpression
	:	unaryExpression ((STAR^ | DIV^ | MOD^ ) unaryExpression)*
	;

unaryExpression
	:	INC^ unaryExpression
	|	DEC^ unaryExpression
	|	MINUS^ {#MINUS.setType(UNARY_MINUS);} unaryExpression
	|	PLUS^ {#PLUS.setType(UNARY_PLUS);} unaryExpression
	|	unaryExpressionNotPlusMinus
	;

unaryExpressionNotPlusMinus
	:	BNOT^ unaryExpression
	|	LNOT^ unaryExpression
	|	(	// subrule allows option to shut off warnings
			options {
				// "(int" ambig with postfixExpr due to lack of sequence
				// info in linear approximate LL(k). It's ok. Shut up.
				generateAmbigWarnings=false;
			}
		:	// If typecast is built in type, must be numeric operand
			// Have to backtrack to see if operator follows
		(LPAREN builtInTypeSpec[true] RPAREN unaryExpression)=>
		lpb:LPAREN^ {#lpb.setType(TYPECAST);} builtInTypeSpec[true] RPAREN!
    {final TokenExt lastToken = (TokenExt) LT(0);
     ASTImpl ast = (ASTImpl) lpb_AST;
     ast.setEndColumn(lastToken.getEndColumn());
     ast.setEndLine(lastToken.getEndLine());
    }
		unaryExpression

		// Have to backtrack to see if operator follows. If no operator
		// follows, it's a typecast. No semantic checking needed to parse.
		// if it _looks_ like a cast, it _is_ a cast; else it's a "(expr)"
	|	(LPAREN classTypeSpec[true] RPAREN unaryExpressionNotPlusMinus)=>
		lp:LPAREN^ {#lp.setType(TYPECAST);} classTypeSpec[true] RPAREN!
    {final TokenExt lastToken = (TokenExt) LT(0);
     ASTImpl ast = (ASTImpl) lp_AST;
     ast.setEndColumn(lastToken.getEndColumn());
     ast.setEndLine(lastToken.getEndLine());
    }
		unaryExpressionNotPlusMinus

	|	postfixExpression
	)
	;

// qualified names, array expressions, method invocation, post inc/dec
postfixExpression
	:
		primaryExpression

		(
			/*
			options {
				// the use of postfixExpression in SUPER_CTOR_CALL adds DOT
				// to the lookahead set, and gives loads of false non-det
				// warnings.
				// shut them off.
				generateAmbigWarnings=false;
			}
		:	*/
			//type arguments are only appropriate for a parameterized method
			//semantic check may be needed here to ensure this
			DOT^ (typeArguments)? IDENT
			(	lp:LPAREN^ {#lp.setType(METHOD_CALL);}
				argList
				RPAREN!
			)?
		|	DOT^ "this"

		|	DOT^ "super"
			(	// (new Outer()).super() (create enclosing instance)
				lp3:LPAREN^ argList RPAREN!
				{#lp3.setType(SUPER_CTOR_CALL);}
			|	DOT^ (typeArguments)? IDENT
				(	lps:LPAREN^ {#lps.setType(METHOD_CALL);}
					argList
					RPAREN!
				)?
			)
		|	DOT^ newExpression
		|	lb:LBRACK^ {#lb.setType(INDEX_OP);} expression RBRACK
		)*

		(	// possibly add on a post-increment or post-decrement.
			// allows INC/DEC on too much, but semantics can check
			in:INC^ {#in.setType(POST_INC);}
	 	|	de:DEC^ {#de.setType(POST_DEC);}
		)?
 	;

// the basic element of an expression
primaryExpression
	:	identPrimary ( options {greedy=true;} : DOT^ "class" )?
	|	constant
	|	"true"
	|	"false"
	|	"null"
	|	newExpression
	|	"this"
	|	"super"
	|	lp:LPAREN^ assignmentExpression RPAREN!
  {final TokenExt lastToken = (TokenExt) LT(0);
   ASTImpl ast = (ASTImpl) #lp;
   ast.setEndColumn(lastToken.getEndColumn());
   ast.setEndLine(lastToken.getEndLine());}

		// look for int.class and int[].class
	|	builtInType
		( lbt:LBRACK^ {#lbt.setType(ARRAY_DECLARATOR);} RBRACK! )*
		DOT^ "class"
	;

/** Match a, a.b.c refs, a.b.c(...) refs, a.b.c[], a.b.c[].class,
 *  and a.b.c.class refs. Also this(...) and super(...). Match
 *  this or super.
 */
identPrimary
	:	(ta1:typeArguments!)?
		IDENT
		// Syntax for method invocation with type arguments is
		// <String>foo("bla")
		(
			options {
				// .ident could match here or in postfixExpression.
				// We do want to match here. Turn off warning.
				greedy=true;
				// This turns the ambiguity warning of the second alternative
				// off. See below. (The "false" predicate makes it non-issue)
				warnWhenFollowAmbig=false;
			}
			// we have a new nondeterminism because of
			// typeArguments... only a syntactic predicate will help...
			// The problem is that this loop here conflicts with
			// DOT typeArguments "super" in postfixExpression (k=2)
			// A proper solution would require a lot of refactoring...
		:	(DOT (typeArguments)? IDENT) =>
				DOT^ (ta2:typeArguments!)? IDENT
		|	{false}?	// FIXME: this is very ugly but it seems to work...
						// this will also produce an ANTLR warning!
				// Unfortunately a syntactic predicate can only select one of
				// multiple alternatives on the same level, not break out of
				// an enclosing loop, which is why this ugly hack (a fake
				// empty alternative with always-false semantic predicate)
				// is necessary.
		)*
		(
			options {
				// ARRAY_DECLARATOR here conflicts with INDEX_OP in
				// postfixExpression on LBRACK RBRACK.
				// We want to match [] here, so greedy. This overcomes
				// limitation of linear approximate lookahead.
				greedy=true;
			}
		:	(	lp:LPAREN^ {#lp.setType(METHOD_CALL);}
				// if the input is valid, only the last IDENT may
				// have preceding typeArguments... rather hacky, this is...
				{if (#ta2 != null) astFactory.addASTChild(currentAST, #ta2);}
				{if (#ta2 == null) astFactory.addASTChild(currentAST, #ta1);}
				argList RPAREN!
			)
		|	( options {greedy=true;} :
				lbc:LBRACK^ {#lbc.setType(ARRAY_DECLARATOR);} RBRACK!
			)+
		)?
	;

/** object instantiation.
 *  Trees are built as illustrated by the following input/tree pairs:
 *
 *  new T()
 *
 *  new
 *   |
 *   T --  ELIST
 *		   |
 *		  arg1 -- arg2 -- .. -- argn
 *
 *  new int[]
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *
 *  new int[] {1,2}
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR -- ARRAY_INIT
 *								  |
 *								EXPR -- EXPR
 *								  |	  |
 *								  1	  2
 *
 *  new int[3]
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *				|
 *			  EXPR
 *				|
 *				3
 *
 *  new int[1][2]
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *			   |
 *		 ARRAY_DECLARATOR -- EXPR
 *			   |			  |
 *			 EXPR			 1
 *			   |
 *			   2
 *
 */
newExpression
{final Token firstToken = LT(1);}
	:	"new"^ (typeArguments)? type
		(	LPAREN! argList RPAREN! (classBlock)?

			//java 1.1
			// Note: This will allow bad constructs like
			//	new int[4][][3] {exp,exp}.
			//	There needs to be a semantic check here...
			// to make sure:
			//   a) [ expr ] and [ ] are not mixed
			//   b) [ expr ] and an init are not used together

		|	newArrayDeclarator (arrayInitializer)?
		)
    {
      final ASTImpl typeAst = (ASTImpl) ##;
      if (typeAst != null) {
        typeAst.setColumn(firstToken.getColumn());
        typeAst.setLine(firstToken.getLine());
        final Token lastToken = LT(1);
        typeAst.setEndColumn(lastToken.getColumn());
        typeAst.setEndLine(lastToken.getLine());
      }
    }
	;

argList
{final TokenExt firstToken = (TokenExt)LT(1);}
	:	(	expressionList
		|	/*nothing*/
			{#argList = #[ELIST,"ELIST"];}
		)
			{
				final ASTImpl typeAst = (ASTImpl) ##;
				typeAst.setColumn( firstToken.getColumn());
				typeAst.setLine( firstToken.getLine() );
				final TokenExt rparenToken = (TokenExt)LT(1); // RPAREN
				typeAst.setEndLine( rparenToken.getEndLine() );
				typeAst.setEndColumn( rparenToken.getEndColumn());
			}
	;

newArrayDeclarator
	:	(
			// CONFLICT:
			// newExpression is a primaryExpression which can be
			// followed by an array index reference. This is ok,
			// as the generated code will stay in this loop as
			// long as it sees an LBRACK (proper behavior)
			options {
				warnWhenFollowAmbig = false;
			}
		:
			lb:LBRACK^ {#lb.setType(ARRAY_DECLARATOR);}
				(expression)?
			RBRACK!
		)+
	;

constant
	:	NUM_INT
	|	CHAR_LITERAL
	|	STRING_LITERAL
	|	NUM_FLOAT
	|	NUM_LONG
	|	NUM_DOUBLE
	;


//----------------------------------------------------------------------------
// The Java scanner
//----------------------------------------------------------------------------
class JavaLexer extends Lexer;

options {
	exportVocab=Java;		// call the vocabulary "Java"
	testLiterals=false;		// don't automatically test for literals
	k=4;					// four characters of lookahead
	charVocabulary='\u0003'..'\uFFFE';
	// without inlining some bitset tests, couldn't do unicode;
	// I need to make ANTLR generate smaller bitsets; see
	// bottom of JavaLexer.java
	codeGenBitsetTestThreshold=20;
}

{
	/** flag for enabling the "assert" keyword */
//	private boolean assertEnabled = false;
	/** flag for enabling the "enum" keyword */
//	private boolean enumEnabled = false;

	/** Enable the "assert" keyword */
	public final void setAssertEnabled(boolean enable) {
    if (enable) {
	    literals.put(new ANTLRHashString("assert", this), new Integer(LITERAL_assert));
    } else {
	    literals.remove(new ANTLRHashString("assert", null));
    }
//    assertEnabled = enable;
  }
	/** Query the "assert" keyword state */
//	public final boolean isAssertEnabled() { return assertEnabled; }
	/** Enable the "enum" keyword */
	public final void setEnumEnabled(boolean enable) {
    if (enable) {
	    literals.put(new ANTLRHashString("enum", this), new Integer(LITERAL_enum));
    } else {
	    literals.remove(new ANTLRHashString("enum", null));
    }
//    enumEnabled = enable;
  }
	/** Query the "enum" keyword state */
//	public final boolean isEnumEnabled() { return enumEnabled; }

  protected char mLA1;
  public static final String CHARS[] = new String[1 << 16];

  static {
    for (int i = 0; i < CHARS.length; i++) {
      CHARS[i] = Character.toString((char) i).intern();
    }
  }

  private static final class LiteralPair {
    final String str;
    final int type;

    public LiteralPair(String str, int type) {
      this.str = str;
      this.type = type;
    }
  }

  private final LiteralPair findLiteral() {
    hashString.setBuffer(text.getBuffer(), text.length());
    final LiteralPair result = (LiteralPair) literals.get(hashString);
    hashString.clean();
    return result;
  }

  protected static HashMap literals;

}

// OPERATORS
// Sander&Anton hacked them
QUESTION		:			{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("?");return;}}	'?';
LPAREN			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("(");return;}}	'(';
RPAREN			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText(")");return;}}	')';
LBRACK			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("[");return;}}	'[';
RBRACK			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("]");return;}}	']';
LCURLY			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("{");return;}}	'{';
RCURLY			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("}");return;}}	'}';
COLON			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText(":");return;}}	':';
COMMA			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText(",");return;}}	',';
//DOT			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText(".");return;}}	'.';
ASSIGN			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("=");return;}}	'=';
EQUAL			:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("==");return;}}	"==";
LNOT			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("!");return;}}	'!';
BNOT			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("~");return;}}	'~';
NOT_EQUAL		:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("!=");return;}}	"!=";
DIV				:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("/");return;}}	'/';
DIV_ASSIGN		:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("/=");return;}}	"/=";
PLUS			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("+");return;}}	'+';
PLUS_ASSIGN		:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("+=");return;}}	"+=";
INC				:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("++");return;}}	"++";
MINUS			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("-");return;}}	'-';
MINUS_ASSIGN	:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("-=");return;}}	"-=";
DEC				:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("--");return;}}	"--";
STAR			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("*");return;}}	'*';
STAR_ASSIGN		:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("*=");return;}}	"*=";
MOD				:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("%");return;}}	'%';
MOD_ASSIGN		:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("%=");return;}}	"%=";
SR				:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText(">>");return;}}	">>";
SR_ASSIGN		:	{if(_createToken) {consume();consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText(">>=");return;}}	">>=";
BSR				:	{if(_createToken) {consume();consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText(">>>");return;}}	">>>";
BSR_ASSIGN		:	{if(_createToken) {consume();consume();consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText(">>>=");return;}}	">>>=";
GE				:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText(">=");return;}}	">=";
GT				:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText(">");return;}}	">";
SL				:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("<<");return;}}	"<<";
SL_ASSIGN		:	{if(_createToken) {consume();consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("<<=");return;}}	"<<=";
LE				:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("<=");return;}}	"<=";
LT				:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("<");return;}}	'<';
BXOR			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("^");return;}}	'^';
BXOR_ASSIGN		:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("^=");return;}}	"^=";
BOR				:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("|");return;}}	'|';
BOR_ASSIGN		:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("|=");return;}}	"|=";
LOR				:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("||");return;}}	"||";
BAND			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("&");return;}}	'&';
BAND_ASSIGN		:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("&=");return;}}	"&=";
LAND			:	{if(_createToken) {consume();consume(); _returnToken = makeToken(_ttype);_returnToken.setText("&&");return;}}	"&&";
SEMI			:		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText(";");return;}}	';';

// JDK 1.5 token for annotations and their declarations
AT        :		{if(_createToken) {consume(); _returnToken = makeToken(_ttype);_returnToken.setText("@");return;}} '@';


// Whitespace -- ignored
WS	:	(	' '
		|	'\t'
		|	'\f'
			// handle newlines
		|	(	options {generateAmbigWarnings=false;}
			:	"\r\n"	// Evil DOS
			|	'\r'	// Macintosh
			|	'\n'	// Unix (the right way)
			)
			{ newline(); }
		)+
		{ _ttype = Token.SKIP; }
	;

// Single-line comments
SL_COMMENT
	:	"//"
		(options {generateAmbigWarnings=false;}: ~('\n'|'\r') | '\0'..'\2' )*
//		('\n'|'\r'('\n')?)
//		{$setType(Token.SKIP); newline();}
	;

// multiple-line comments
ML_COMMENT
	:	"/*"
		(	/*	'\r' '\n' can be matched in one alternative or by matching
				'\r' in one iteration and '\n' in another. I am trying to
				handle any flavor of newline that comes in, but the language
				that allows both "\r\n" and "\r" and "\n" to all be valid
				newline is ambiguous. Consequently, the resulting grammar
				must be ambiguous. I'm shutting this warning off.
			 */
			options {
				generateAmbigWarnings=false;
			}
		:
			{ LA(2)!='/' }? '*'
		|	'\r' '\n'		{newline();}
		|	'\r'			{newline();}
		|	'\n'			{newline();}
		|	~('*'|'\n'|'\r')
		| '\0'..'\2'
		)*
		"*/"
//		{$setType(Token.SKIP);}
	;


// character literals
CHAR_LITERAL
	:	'\'' ( ESC | ~('\''|'\n'|'\r'|'\\') ) '\''
{		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			int len = text.length() - _begin;
			if (len == 1) {
				_token.setText(CHARS[text.charAt(_begin)]);
			} else {
				_token.setText(new String(text.getBuffer(), _begin, len));
			}

/*			if (len < 6) {
				_token.setText(new String(text.getBuffer(), _begin, len).intern());
			} else {
				_token.setText(new String(text.getBuffer(), _begin, len));
			}*/
		}
		_returnToken = _token;
		return;}
	;

// string literals
STRING_LITERAL
	:	'"' (ESC|~('"'|'\\'|'\n'|'\r'))* '"'
{		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			int len = text.length() - _begin;
			if (len == 1) {
				_token.setText(CHARS[text.charAt(_begin)]);
			} else {
				_token.setText(new String(text.getBuffer(), _begin, len));
			}

/*			if (len < 6) {
				_token.setText(new String(text.getBuffer(), _begin, len).intern());
			} else {
				_token.setText(new String(text.getBuffer(), _begin, len));
			}*/
		}
		_returnToken = _token;
		return;}
	;


// escape sequence -- note that this is protected; it can only be called
// from another lexer rule -- it will not ever directly return a token to
// the parser
// There are various ambiguities hushed in this rule. The optional
// '0'...'9' digit matches should be matched here rather than letting
// them go back to STRING_LITERAL to be matched. ANTLR does the
// right thing by matching immediately; hence, it's ok to shut off
// the FOLLOW ambig warnings.
protected
ESC
	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
		|	'0'..'3'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
				(
					options {
						warnWhenFollowAmbig = false;
					}
				:	'0'..'7'
				)?
			)?
		|	'4'..'7'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
			)?
		)
	;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
	:	('0'..'9'|'A'..'F'|'a'..'f')
	;


// a dummy rule to force vocabulary to be all characters (except special
// ones that ANTLR uses internally (0 to 2)
protected
VOCAB
//	:	'\3'..'\377'
	:	'\3'..'\uFFFE'
	;

// an identifier. Note that testLiterals is set to true! This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
	options {testLiterals=true;}
{
    consume();

    while (Character.isJavaIdentifierPart(mLA1)) {
          consume();
    }

    LiteralPair pair = findLiteral();
//		if (_createToken && _token==null && _ttype!=Token.SKIP) {
			if (pair == null) {
				_token = makeToken(_ttype);

				int len = text.length() - _begin;
				if (len == 1) {
					_token.setText(CHARS[text.charAt(_begin)]);
				} else {
					_token.setText(new String(text.getBuffer(), _begin, len).intern());
				}

//				final String content = ASTUtil.intern(_ttype, new String(text.getBuffer(), _begin, text.length()-_begin));
//				_token.setText(content);
			} else {
				_token = makeToken(pair.type);
				_token.setText(pair.str);
			}
//		}
		_returnToken = _token;
		if (true) return;}
	:	('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$')*
	;


// a numeric literal
NUM_INT
	{boolean isDecimal=false; Token t=null;}
	:	('.' {_ttype = DOT;}
			(
				(('0'..'9')+ (EXPONENT)? (f1:FLOAT_SUFFIX {t=f1;})?
				{
				if (t != null && t.getText().toUpperCase().indexOf('F')>=0) {
					_ttype = NUM_FLOAT;
				}
				else {
					_ttype = NUM_DOUBLE; // assume double
				}
				})
				|
				// JDK 1.5 token for variable length arguments
				(".." {_ttype = TRIPLE_DOT;})
			)?

	|	(	'0' {isDecimal = true;} // special case for just '0'
			(	('x'|'X')
				(											// hex
					// the 'e'|'E' and float suffix stuff look
					// like hex digits, hence the (...)+ doesn't
					// know when to stop: ambig. ANTLR resolves
					// it correctly by matching immediately. It
					// is therefor ok to hush warning.
					options {
						warnWhenFollowAmbig=false;
					}
				:	HEX_DIGIT
				)+

			|	//float or double with leading zero
				(('0'..'9')+ ('.'|EXPONENT|FLOAT_SUFFIX)) => ('0'..'9')+

			|	('0'..'7')+									// octal
			)?
		|	('1'..'9') ('0'..'9')*  {isDecimal=true;}		// non-zero decimal
		)
		(	('l'|'L') { _ttype = NUM_LONG; }

		// only check to see if it's a float if looks like decimal so far
		|	{isDecimal}?
			(	'.' ('0'..'9')* (EXPONENT)? (f2:FLOAT_SUFFIX {t=f2;})?
			|	EXPONENT (f3:FLOAT_SUFFIX {t=f3;})?
			|	f4:FLOAT_SUFFIX {t=f4;}
			)
			{
			if (t != null && t.getText().toUpperCase() .indexOf('F') >= 0) {
				_ttype = NUM_FLOAT;
			}
			else {
				_ttype = NUM_DOUBLE; // assume double
			}
			}
		)?)
{        if (_createToken && _token == null && _ttype != Token.SKIP) {
          _token = makeToken(_ttype);

          // Profiling showed that a lot of garbage is allocated here.
          // since most of the numbers used are 0 or 1 or . something
          // we can do a little special case for them
          int len = text.length() - _begin;
          if (len == 1) {
              _token.setText(CHARS[text.charAt(_begin)]);
          } else {
              final String content = new String(text.getBuffer(), _begin, len);
              _token.setText(content);
          }

        }
        _returnToken = _token;
        return;}
	;

// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
	:	('e'|'E') ('+'|'-')? ('0'..'9')+
	;


protected
FLOAT_SUFFIX
	:	'f'|'F'|'d'|'D'
	;

UNICODE_EOF
  : ('\u001a' | "\\u001a") {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE); return;}
  ;
