package ict.ada.drawline;

import ict.ada.drawline.util.StringUtil;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Document stores a document's text and preprocess the text to get positions of each word & sentence
 * for future queries. Where:
 *
 * 1. A Word is one of the followings:
 * 1) an English or a number token, with its following spaces or English punctuations
 * 2) a character of other languages, with its following spaces or English punctuations
 *
 * 2. A Sentence is: a substring ended with a sentence delimiter
 *
 * @author Jiyuan Lin
 */
public class Document {

  private String text;
  /**
   * Record end position of each word and sentence.
   */
  private int[] wordEndPos;
  private int[] sentenceEndPos;

  /**
   * Primary constructor, store underlying text and calculate word & sentence offset for later queries.
   *
   * @param text
   */
  public Document(String text) {
    this.text = text;
    this.wordEndPos = calcWordEndPos(text);
    this.sentenceEndPos = calcSentenceEndPos(text);
  }

  /**
   * Return underlying text.
   */
  public String getText() {
    return text;
  }

  /**
   * Calculate end positions of each word in text.
   *
   * @param text
   */
  public static int[] calcWordEndPos(String text) {
    List<Integer> pos = new ArrayList<Integer>();
    char lastCh = 0;
    char ch;
    for (int i = 0; i < text.length(); lastCh = ch, ++i) {
      ch = text.charAt(i);
      if (i == 0 || Character.isSpaceChar(ch) || StringUtil.isPunctuation(ch)) {
        continue;
      }
      if (Character.isDigit(ch)) {
        if (!Character.isDigit(lastCh)) {
          pos.add(i - 1);
        }
      } else if (StringUtil.isEnglishLetter(ch)) {
        if (!StringUtil.isEnglishLetter(lastCh)) {
          pos.add(i - 1);
        }
      } else {
        pos.add(i - 1);
      }
    }
    if (!text.isEmpty()) {
      pos.add(text.length() - 1);
    }
    return Ints.toArray(pos);
  }

  /**
   * Calculate end positions of each sentence in text.
   *
   * @param text
   */
  public static int[] calcSentenceEndPos(String text) {
    List<Integer> pos = new ArrayList<Integer>();
    for (int i = 0; i < text.length(); ) {
      int oldI = i;
      for (String delimiter : Configuration.getSentenceDelimiters()) {
        if (StringUtil.equals(delimiter, text, i)) {
          i += delimiter.length();
          pos.add(i - 1);
          break;
        }
      }
      if (oldI == i) {
        ++i;
      }
    }
    // add a fake sentence delimiter to the end
    if (!pos.isEmpty() && pos.get(pos.size() - 1) != text.length() - 1) {
      pos.add(text.length());
    }
    return Ints.toArray(pos);
  }

  /**
   * Binary searching the wordEndPos to map a given offset to a word position.
   *
   * @param offset
   */
  public int getWordPos(int offset) {
    int pos = Arrays.binarySearch(wordEndPos, offset);
    return pos >= 0 ? pos : -pos - 1;
  }

  /**
   * Binary searching the wordEndPos to map a given offset to a sentence position.
   *
   * @param offset
   */
  public int getSentencePos(int offset) {
    int pos = Arrays.binarySearch(sentenceEndPos, offset);
    return pos >= 0 ? pos : -pos - 1;
  }

  /**
   * Split text into words.
   */
  public List<String> getWords() {
    List<String> words = new ArrayList<String>();
    for (int i = 0, last = 0; i < wordEndPos.length; ++i) {
      words.add(text.substring(last, wordEndPos[i] + 1));
      last = wordEndPos[i] + 1;
    }
    return words;
  }

  /**
   * Split text into sentences.
   */
  public List<String> getSentences() {
    List<String> sentences = new ArrayList<String>();
    for (int i = 0, last = 0, l = text.length(); i < sentenceEndPos.length; ++i) {
      sentences.add(text.substring(last, Math.min(l, sentenceEndPos[i] + 1)));
      last = sentenceEndPos[i] + 1;
    }
    return sentences;
  }

  public static void main(String[] args) {
    Document doc = new Document("abc这ABc是..。额deようこそe., ,   中환영합니다!文。！d");
    System.out.println("['" + Joiner.on("', '").join(doc.getWords()) + "']");
    System.out.println("['" + Joiner.on("', '").join(doc.getSentences()) + "']");
    for (int i = 0; i < doc.getText().length(); ++i) {
      System.out.println(i + " " + doc.getWordPos(i) + " " + doc.getSentencePos(i));
    }
  }
}
