/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ChainableRuntimeException;

import rantlr.ASTFactory;
import rantlr.ASTPair;
import rantlr.MismatchedTokenException;
import rantlr.RecognitionException;
import rantlr.Token;
import rantlr.TokenStream;
import rantlr.TokenStreamException;
import rantlr.TokenStreamRecognitionException;


public class OptimizedJavaRecognizer extends rantlr.LLkParser implements
    JavaTokenTypes {

// number of tokens for RIT sources:
//  100 - 308, 200 - 156, 300 - 108, 400 - 95, 500 - 55,
//  500 -- 60.57%
//  600 - 61, 700 - 49, 800 - 34, 900 - 30, 1000 - 30,
//  1000 -- 77.68%
//  1100 - 22, 1200 - 19, 1300 - 16, 1400 - 19, 1500 - 12,
//  1500 -- 85.06%
//  1600 - 12, 1700 - 13, 1800 - 14, 1900 - 11, 2000 - 9,
//  2000 -- 90.01%
//  2100 - 9, 2200 - 7, 2300 - 13, 2400 - 6, 2500 - 6,
//  2500 -- 93.45%
//  2600 - 5, 2700 - 4, 2800 - 4, 2900 - 2, 3000 - 5,
//  3000 -- 95.13%
//  3100 - 2, 3200 - 4, 3300 - 5, 3400 - 2, 3500 - 4,
//  3500 -- 96.56%
//  3600 - 2, 3700 - 1, 3800 - 2, 3900 - 1, 4000 - 2,
//  4000 -- 97.23%
//  4100 - 2, 4300 - 3,
//  4500 -- 97.65%
//  4600 - 3, 4700 - 1, 4800 - 4, 4900 - 1, 5000 - 1,
//  5000 -- 98.48%
//  5500 - 1,
//  5500 -- 98.57%
//  5900 - 1,
//  6000 -- 98.65%
//  6100 - 1, 6300 - 1, 6500 - 1,
//  6500 -- 98.90%
//  6600 - 1, 7000 - 1,
//  7000 -- 99.07%
//  7100 - 1, 7500 - 1,
//  7500 -- 99.24%
//  8000 - 1,
//  8000 -- 99.32%
//  8800 - 1,
//  9000 -- 99.41%
//  9200 - 1,
//  9500 -- 99.49%
//  10300 - 1,
//  10500 -- 99.58%
//  10800 - 1,
//  11000 -- 99.66%
//  13800 - 1,
//  14000 -- 99.75%
//  22200 - 1,
//  22500 -- 99.83%
//  29700 - 1,
//  30000 -- 99.91%
//  37100 - 1,
//  37500 -- 100.0%

  private static final int INITIAL_TOKEN_BUFFER_SIZE = 1024;

  private static ErrorListener errorListener;

  private static ASTPair[] createdPairs = new ASTPair[192];
  private static int createdPairsFilled = 0;

  private static Token[] allTokens = null;

  private int tIndex = 0;
  private int finalSize = 0;
  private final TokenStream myLexer;

  protected int mLA1;

  private static final Token EOF_TOKEN
      = new TokenImpl(EOF, Integer.MAX_VALUE, Integer.MAX_VALUE);

  OptimizedJavaRecognizer(final TokenStream lexer, final int k) {
    super(lexer, k);
    myLexer = lexer;
    initialize();
  }

  OptimizedJavaRecognizer(final Object no, final int k) {
    this(null, 2);
    throw new RuntimeException(
        "not implemented: use OptimizedJavaRecognizer(TokenStream lexer, int k)");
  }


  public static final void releaseMemory() {
    allTokens = null;
  }

  private static final void staticInit() {
    allTokens = new Token[INITIAL_TOKEN_BUFFER_SIZE];
  }

  private final void initialize() {
    if (allTokens == null) {
      staticInit();
    }

    Token t = null;
    try {
      while (true) {
        t = myLexer.nextToken();
        ensureTokens(finalSize);
        allTokens[finalSize++] = t;
        if (t.getType() == EOF) {
          break;
        }
      }
    } catch (TokenStreamException e) {
      int line = 0;
      int column = 0;
      String message = e.getMessage(); // earlier was just constant "Token error"
      if (message == null || message.length() == 0) {
        message = "Token error";
      }
      if (e instanceof TokenStreamRecognitionException) {
        final RecognitionException r = ((TokenStreamRecognitionException) e).
            recog;
        line = r.getLine();
        column = r.getColumn();
      }
      if (t != null && line == 0) {
        line = t.getLine();
        column = t.getColumn();
      }

      throw new ChainableRuntimeException(
          message + " in " + ((CommentStoringFilter) myLexer).getFilename()
          + " " + ((line > 0) ? (line + ":" + column) : ""), e);
    } catch (RuntimeException e) {
      String message = "Crashed parsing "
          + ((CommentStoringFilter) myLexer).getFilename()
          + ", last token read: " + t;
      AppRegistry.getExceptionLogger().error(e, message, this);
      throw new ChainableRuntimeException(message, e);
    }

    ensureTokens(finalSize);
    allTokens[finalSize] = EOF_TOKEN; // we avoid NPE on lookahead
    mLA1 = LA(1);
  }

  private final void ensureTokens(final int finalSize) {
    if (finalSize >= allTokens.length) {
      final Token[] temp = new Token[allTokens.length << 1];
      System.arraycopy(allTokens, 0, temp, 0, finalSize);
      allTokens = temp;
    }
  }

  public final void consume() {
    tIndex++;
    try {
      mLA1 = allTokens[tIndex].getType();
    } catch (NullPointerException e) {
      mLA1 = EOF; // die
    } catch (ArrayIndexOutOfBoundsException e) {
      mLA1 = EOF;
    }
  }

  public final int LA(final int i) {
    try {
      return allTokens[tIndex - 1 + i].getType();
    } catch (NullPointerException e) {
      return EOF; // let's die here
    } catch (ArrayIndexOutOfBoundsException e) {
      return EOF;
    }
  }

  public final Token LT(final int i) {
    try {
      final Token token = allTokens[tIndex - 1 + i];
      if (token == null) {
        return EOF_TOKEN;
      }
      return token;
    } catch (NullPointerException e) {
      return EOF_TOKEN; // end of show
    } catch (ArrayIndexOutOfBoundsException e) {
      return EOF_TOKEN; // end of show
    }
  }

  public final int mark() {
    return tIndex;
  }

  public final void rewind(final int pos) {
    tIndex = pos;
    mLA1 = LA(1);
  }

  static final class MyMismatchedException extends MismatchedTokenException {
    final String mess;

    MyMismatchedException(final String mess) {
      super();
      this.mess = mess;
    }

    public final String getMessage() {
      return mess;
    }
  }


  public final void match(final int t) throws MismatchedTokenException,
      TokenStreamException {
    if (mLA1 != t) {
      if (inputState.guessing == 0) {
        final String mess = "Unexpected token in " + getFilename()
            + " '" + LT(1).getText() + "'";
        final MyMismatchedException e = new MyMismatchedException(mess);
        e.fileName = getFilename();
        e.line = LT(1).getLine();
        e.column = LT(1).getColumn();
        throw e;
      } else {
        throw EMPTY_MISMATCHED_EXCEPTION;
      }
    } else {
      // mark token as consumed -- fetch next token deferred until LA/LT
      consume();
    }
  }

  private static final MismatchedTokenException EMPTY_MISMATCHED_EXCEPTION
      = new MyMismatchedException("empty");

  static final ASTPair getNextASTPair() {
    if (createdPairsFilled == 0) {
      return new ASTPair();
    }
    return createdPairs[--createdPairsFilled];
  }

  static final void releaseASTPair(final ASTPair aPair) {
    aPair.root = null;
    aPair.child = null;
    try {
      createdPairs[createdPairsFilled] = aPair;
    } catch (ArrayIndexOutOfBoundsException e) {
      final ASTPair[] tmpPairs = new ASTPair[createdPairsFilled * 2];
      System.arraycopy(createdPairs, 0, tmpPairs, 0, createdPairsFilled);
      createdPairs = tmpPairs;

      createdPairs[createdPairsFilled] = aPair;
    }
    createdPairsFilled++;
  }

  ///----- Added error handling ---------------

  public final void reportError(final String s) {
    errorListener.onError(s, getFilename(), 1, 1);
  }

  public final void reportError(final RecognitionException e) {
    errorListener.onError(e.getMessage(), e.getFilename(), e.getLine(),
        e.getColumn());
  }

  public static void setErrorListener(final ErrorListener e) {
    errorListener = e;
  }

  ///------------------------------------------

  public final void setASTFactory(ASTFactory f) {
    super.setASTFactory(net.sf.refactorit.parser.ASTImplFactory.getInstance());
  }

  public final ASTFactory getASTFactory() {
    return super.getASTFactory();
  }
}
