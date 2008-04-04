/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhraseSplitter {
  private String[] words = new String[] {};
  private String[] significantWords = new String[]{};

  public PhraseSplitter(String str) {
    List wordList = extractWords(str);
    List significantWordList = excludeMeaninglessWords(wordList);
    
    words = (String[])wordList.toArray(new String[wordList.size()]);
    significantWords = (String[])significantWordList.toArray(new String[significantWordList.size()]);
  }

  /**
   * Takes words list and returns only meaningful words
   * @param wordList
   * @return
   */
  private List excludeMeaninglessWords(final List wordList) {
    List significantWordsList = new ArrayList();
    for(Iterator it = wordList.iterator(); it.hasNext(); ) {
      String word = (String)it.next();
      if(!WordUtils.isMeaningless(word.toCharArray())) {
        significantWordsList.add(word);
      }
    }

    return significantWordsList;
  }

  /**
   * extracts word from a phrase. Any not digit/letter symbol is used
   * as a delimiter. Also, SuchCamelStyle is accepted. 
   * @param str
   * @return
   */
  private List extractWords(final String str) {
    List wordList = new ArrayList();  
    
    if(str == null || str.length() == 0) {
      return wordList;
    }
    
    final int WORD_START = 0;
    final int WORD_LOWERCASE_CHARACTER = 1;
    final int WORD_UPPERCASE_CHARACTER = 2;
    final int WORD_WITH_ALL_UPPERCASE_CHARACTERS = 3;
    final int WORD_END = 7;
    final int WORD_WITH_ALL_DIGITS = 6;
    final int SENTENCE_END = 8;
    final int WORD_WITHOUT_MEANING = 9;
    
    int state = WORD_START;

    boolean running = true; 
    
    int index = 0;
    int offset = 0; 
    
    while(running) {
      switch(state) {
        case WORD_START:
          offset = index;
          if(Character.isLetter(str.charAt(index))) {
            if(Character.isUpperCase(str.charAt(index))) {
              state = WORD_UPPERCASE_CHARACTER;
              index++;
            } else {
              state = WORD_LOWERCASE_CHARACTER;
              index++;
            }
          } else if (Character.isDigit(str.charAt(index))) {
            state = WORD_WITH_ALL_DIGITS;
            index++;
          } else {
            state = WORD_WITHOUT_MEANING;
            index++;
          }
          break;
          
        case WORD_WITHOUT_MEANING:
          state = WORD_END;
          break;
          
        case WORD_WITH_ALL_DIGITS:
          if(Character.isDigit(str.charAt(index))) {
            index++;
          } else {
            state = WORD_END;
          }
          break;
        
        case WORD_UPPERCASE_CHARACTER:
          if(Character.isLetter(str.charAt(index))) {
            if(Character.isUpperCase(str.charAt(index))) {
              state = WORD_WITH_ALL_UPPERCASE_CHARACTERS;
              index++;
            } else {
              state = WORD_LOWERCASE_CHARACTER;
              index++;
            }
          } else {
            state = WORD_END;
          }
          break;
        case WORD_LOWERCASE_CHARACTER:
          if(Character.isLetter(str.charAt(index)) && Character.isLowerCase(str.charAt(index))) {
            index++;
          } else {
            state = WORD_END;
          }
          break;
          
        case WORD_WITH_ALL_UPPERCASE_CHARACTERS:
          if(Character.isLetter(str.charAt(index))) {
            if(Character.isUpperCase(str.charAt(index))) {
              index++;
            } else {
              state = WORD_END;
              index--;
            }
          } else {
            state = WORD_END; 
          } 
          break;

        case SENTENCE_END:
          running = false; 
        case WORD_END:
          //char[] src = new char[index - offset];
          String src = str.substring(offset, index);
          //System.arraycopy(str, offset, src, 0, index - offset); 
          wordList.add(src);                   
          state = WORD_START;
          break;
      }
      
      if(index >= str.length()) {
        state = SENTENCE_END; 
      }
    }

    return wordList;
  }

  /** 
   * @return only meaningful words
   */
  public String[] getSignificantWords() {
    return significantWords;
  }
  
  /**
   * @return all detected words
   */
  public String[] getAllWords() {
    return words;
  }

}
