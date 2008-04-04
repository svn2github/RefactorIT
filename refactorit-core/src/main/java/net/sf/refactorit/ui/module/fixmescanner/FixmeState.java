/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.JWordDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class helps to preserve module states after reload
 *
 * @author Vladislav Vislogubov
 */
public class FixmeState {
  public List words = new ArrayList();
  public boolean sortedByTimestamp = false;

  public static final List DEFAULT_WORDS = new ArrayList();

  static {
    try {
      DEFAULT_WORDS.add(new JWordDialog.Word("BUG", true)); // reported bug
      DEFAULT_WORDS.add(new JWordDialog.Word("CAUTION", true));
      DEFAULT_WORDS.add(new JWordDialog.Word("FIXME", true));
      DEFAULT_WORDS.add(new JWordDialog.Word("I18N", true)); // "Requires internationalization"
      DEFAULT_WORDS.add(new JWordDialog.Word("PENDING", true)); // "Waiting for a decision"
      DEFAULT_WORDS.add(new JWordDialog.Word("TODO", true));
      DEFAULT_WORDS.add(new JWordDialog.Word("HACK", true)); // A hack
      DEFAULT_WORDS.add(new JWordDialog.Word("XXX", true)); // "Crazy, ugly" or under development
      DEFAULT_WORDS.add(new JWordDialog.Word("@todo", true));
    } catch (Exception e) {
      throw new ChainableRuntimeException(e);
    }
  }

  private static final char TYPE_STRING = 's';
  private static final char TYPE_REGEXP = 'r';
  private static final char NOT_SELECTED_TYPE_STRING = 'S';
  private static final char NOT_SELECTED_TYPE_REGEXP = 'R';

  public FixmeState() {}

  /**
   * @return false when some regular expressions had a bad syntax and
   *   nothing was loaded because of that.
   */
  public boolean loadWords() {
    try {
      this.words = stringListToWordList(StringUtil.deserializeStringList(
          GlobalOptions.getOption("fixmescanner.words", StringUtil.serializeStringList(wordListToStringList(DEFAULT_WORDS)))));
      return true;
    } catch (JWordDialog.Word.BadFormatException e) {

      return false;
    }
  }

  public void saveWords() {
    GlobalOptions.setOption("fixmescanner.words", StringUtil.serializeStringList(wordListToStringList(this.words)));
    GlobalOptions.save();
  }

  public void restoreDefaultWords() {
    this.words = DEFAULT_WORDS;
  }

  private List stringListToWordList(List strings) throws JWordDialog.Word.
      BadFormatException {
    List result = new ArrayList(strings.size());

    for (Iterator i = strings.iterator(); i.hasNext(); ) {
      result.add(deserializeWord(i.next().toString()));
    }

    return result;
  }

  private List wordListToStringList(List words) {
    List result = new ArrayList(words.size());

    for (Iterator i = words.iterator(); i.hasNext(); ) {
      result.add(serializeWord(i.next()));
    }

    return result;
  }

  private String serializeWord(Object o) {
    if (o instanceof JWordDialog.Word) {
      JWordDialog.Word word = (JWordDialog.Word) o;
      char type;
      if (word.isRegularExpression) {
        type = (word.isSelected) ? TYPE_REGEXP : NOT_SELECTED_TYPE_REGEXP;
      } else {
        type = (word.isSelected) ? TYPE_STRING : NOT_SELECTED_TYPE_STRING;
      }
      return type + word.word;
    } else {
      return TYPE_STRING + o.toString();
    }
  }

  private JWordDialog.Word deserializeWord(String s) throws JWordDialog.Word.
      BadFormatException {
    char type = s.charAt(0);
    String contents = s.substring(1);

    JWordDialog.Word result;

    switch (type) {
      case TYPE_STRING:
        result = new JWordDialog.Word(contents, false);
        break;
      case TYPE_REGEXP:
        result = new JWordDialog.Word(contents, true);
        break;
      case NOT_SELECTED_TYPE_REGEXP:
        result = new JWordDialog.Word(contents, true);
        result.isSelected = false;
        break;
      case NOT_SELECTED_TYPE_STRING:
        result = new JWordDialog.Word(contents, false);
        result.isSelected = false;
        break;
      default:
        result = new JWordDialog.Word(contents, true);
    }

    return result;
  }
}
