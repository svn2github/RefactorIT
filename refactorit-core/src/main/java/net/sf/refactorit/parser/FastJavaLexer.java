/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.RuntimePlatform;

import rantlr.CharScanner;
import rantlr.CharStreamException;
import rantlr.InputBuffer;
import rantlr.MismatchedCharException;
import rantlr.Token;
import rantlr.collections.impl.BitSet;

import java.io.UnsupportedEncodingException;


public final class FastJavaLexer extends JavaLexer {

  public static final int JVM_AUTOMATIC = -1;
  public static final int JVM_13 = 0;
  public static final int JVM_14 = 1;
  public static final int JVM_50 = 2;

  private static int jvmMode = JVM_14;//JVM_AUTOMATIC;
  private static int actualJvmMode = jvmMode;

  private static int tokensCreated = 0;
  private static TokenImpl[] tokens = new TokenImpl[1024];

//  public FastJavaLexer(InputStream in) {
//    super(new ByteBuffer(in));
//    init();
//  }

  public FastJavaLexer(byte[] in) {
    super(new FastByteBuffer(in));
    init();
  }

  public FastJavaLexer(String s) {
    super(new FastByteBuffer(s));
    init();
  }

  private void init() {
//    setTabSize(1);
    initJvmSupport();
    tokensCreated = 0;

//    _returnToken = null;
//    index = 0;
//    line = 1;
//    column = 1;
//
//    this.filename = filename;
    try {
      mLA1 = inputState.input.LA(1);
    } catch (CharStreamException e) {
      // never happens...
    }
  }

  private final void initJvmSupport() {
    switch (jvmMode) {
      case JVM_AUTOMATIC:
        double javaSpecificationVersion
            = RuntimePlatform.getJavaSpecificationVersion();
        if (javaSpecificationVersion >= 1.5) { // JAVA5: ?
          setJVM50();
        } else if (javaSpecificationVersion >= 1.4) {
          setJVM14();
        } else {
          setJVM13();
        }
        break;

      case JVM_13:
        setJVM13();
        break;

      case JVM_14:
        setJVM14();
        break;

      case JVM_50:
      default:
        setJVM50();
        break;
    }
  }

  private void setJVM13() {
    actualJvmMode = JVM_13;
    setAssertEnabled(false);
    setEnumEnabled(false);
  }

  private void setJVM14() {
    actualJvmMode = JVM_14;
    setAssertEnabled(true);
    setEnumEnabled(false);
  }

  private void setJVM50() {
    actualJvmMode = JVM_50;
    setAssertEnabled(true);
    setEnumEnabled(true);
  }

  public static final void setJvmMode(final int newMode) {
    FastJavaLexer.jvmMode = newMode;
    new FastJavaLexer(new byte[0]); // init actual jvm mode
  }

  public static final int getJvmMode() {
    return jvmMode;
  }

  public static final int getActualJvmMode() {
    return actualJvmMode;
  }

  /**
   * We don't need that smart tab system - we count in physical characters!
   */
  public void tab() {
    inputState.column++;
  }

  public void consume() throws CharStreamException {
    if (inputState.guessing == 0) {
      text.append(mLA1);
      inputState.column++;
    }
    inputState.input.consume();
    mLA1 = inputState.input.LA(1);
  }

  public void rewind(int pos) {
    inputState.input.rewind(pos);
    try {
      mLA1 = inputState.input.LA(1);
    } catch (CharStreamException e) {
      // never happens...
    }
  }

  public char LA(int i) throws CharStreamException {
    return inputState.input.LA(i);
  }

  public void append(char c) {
    text.append(c);
  }

  public void append(String s) {
    text.append(s);
  }

  public void match(char c) throws MismatchedCharException, CharStreamException {
    if (mLA1 != c) {
      throw new MismatchedCharException(mLA1, c, false, this);
    }
    consume();
  }

  public void match(BitSet b) throws MismatchedCharException,
      CharStreamException {
    if (!b.member(mLA1)) {
      throw new MismatchedCharException(mLA1, b, false, this);
    } else {
      consume();
    }
  }

  public void match(String s) throws MismatchedCharException,
      CharStreamException {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      if (mLA1 != s.charAt(i)) {
        throw new MismatchedCharException(mLA1, s.charAt(i), false, this);
      }
      consume();
    }
  }

  public void matchNot(char c) throws MismatchedCharException,
      CharStreamException {
    if (mLA1 == c) {
      throw new MismatchedCharException(mLA1, c, true, this);
    }
    consume();
  }

  public void matchRange(char c1, char c2) throws MismatchedCharException,
      CharStreamException {
    if (mLA1 < c1 || mLA1 > c2)
      throw new MismatchedCharException(mLA1, c1, c2, false, this);
    consume();
  }

  public boolean isLiteral(String text) {
    hashString.setString(text);
    return literals.get(hashString) != null;
  }

  public static void clear() {
    final int max = tokens.length;
    for (int i = 0; i < max; i++) {
      if (tokens[i] == null) {
        break;
      }
      tokens[i].clean();
    }
    tokensCreated = 0;
  }

  protected Token makeToken(final int t) {
//System.err.println("token: " + t + "[" + inputState.tokenStartLine
//        + " : " + inputState.tokenStartColumn + "]");
    TokenImpl token;
    try {
      token = tokens[tokensCreated];
    } catch (ArrayIndexOutOfBoundsException e) {
      final TokenImpl[] newTokens = new TokenImpl[tokens.length<<1];
      System.arraycopy(tokens, 0, newTokens, 0, tokens.length);
      tokens = newTokens;
      token = null;
    }

    if (token == null) {
      token = new TokenImpl(t,
          inputState.tokenStartLine, inputState.tokenStartColumn,
          inputState.line, inputState.column);
      tokens[tokensCreated] = token;
    } else {
      token.reinit(t,
          inputState.tokenStartLine, inputState.tokenStartColumn,
          inputState.line, inputState.column);
    }

    tokensCreated++;
    return token;
  }

}


final class FastByteBuffer extends InputBuffer {
  private static final int INITIAL_BUFFER_SIZE = 128 * 1024;
  private static final int BUFFER_EXTRA_SPACE = 128;
  private static final int INCREASE_EXTRA_AMOUNT = 128 * 1024;

  private static char[] myBuffer = new char[INITIAL_BUFFER_SIZE];

  private int index = 0;

  public FastByteBuffer(final byte[] bb) {
    final String s;
    try {
      s = new String(bb, GlobalOptions.getEncoding());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported encoding: " + e.getMessage());
    }
    init(s);
  }

  public FastByteBuffer(final String s) {
    init(s);
  }

  private final void init(String s) {
    final int length = s.length();
    if (myBuffer.length - BUFFER_EXTRA_SPACE < length) {
      // increase buffer
      myBuffer = null; // drop old buffer and show it explicitly to GC
      myBuffer = new char[length + INCREASE_EXTRA_AMOUNT + BUFFER_EXTRA_SPACE];
      //System.err.println("Buffer is "+myBuffer.length+" char but string is"+length+" characters");
    }
    s.getChars(0, length, myBuffer, 0);

    myBuffer[length] = CharScanner.EOF_CHAR;
    myBuffer[length + 1] = CharScanner.EOF_CHAR;
    myBuffer[length + 2] = CharScanner.EOF_CHAR;
    myBuffer[length + 3] = CharScanner.EOF_CHAR;

    // dos file ending
    if (length > 0 && myBuffer[length - 1] == 0x1A) {
      myBuffer[length - 1] = CharScanner.EOF_CHAR;
    }
  }

  public final char LA(int i) throws CharStreamException {
    return myBuffer[this.index + i - 1];
  }

  /**
   * This method updates the state of the input buffer so that the text matched
   * since the most recent mark() is no longer held by the buffer.
   */
  public final void commit() {
    nMarkers--;
  }

  public final void consume() {
    ++index;
  }

  /** not used */
  public final void fill(int amount) throws CharStreamException {
  }

  /** not used */
  public final String getLAChars() {
    return "";
  }

  /** not used */
  public final String getMarkedChars() {
    return "";
  }

  public final boolean isMarked() {
    return nMarkers != 0;
  }

  public final int mark() {
    nMarkers++;
    return this.index;
  }

  public final void reset() {
    nMarkers = 0;
    this.index = 0;
  }

  public final void rewind(int mark) {
    this.index = mark;
    nMarkers--;
  }
}
