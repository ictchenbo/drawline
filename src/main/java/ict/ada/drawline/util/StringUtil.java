package ict.ada.drawline.util;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import ict.ada.drawline.rule.operator.Dist;

/**
 * @author Jiyuan Lin
 */
public final class StringUtil {

  private StringUtil() {
  }

  public static final String[] EmptyStringArray = new String[0];

  /**
   * Find the first k - 1 delimiter of str, and split it into k parts.
   * TODO Same as String.split(regex, limit)?
   *
   * @param str       input string
   * @param delimiter input delimiter
   * @param k         k parts
   * @return k parts
   */
  public static String[] splitKParts(String str, String delimiter, int k) {
    if (k <= 0) {
      throw new IllegalArgumentException(String.format("k(%d) must >= 1.", k));
    }
    String[] parts = new String[k];
    int from = 0;
    for (int p = 0; p < k - 1; ++p) {
      int to = str.indexOf(delimiter, from);
      if (to == -1) {
        throw new IllegalArgumentException(
            String.format("%s can't split into %d parts by delimiter %s", str, k, delimiter));
      }
      parts[p] = str.substring(from, to);
      from = to + 1;
    }
    parts[k - 1] = str.substring(from);
    return parts;
  }

  /**
   * Judge whether word equals to text[offset, offset + word.length()] or not.
   *
   * @param word
   * @param text
   * @param offset
   * @return
   */
  public static boolean equals(String word, String text, int offset) {
    if (word.isEmpty()) {
      return true;
    }
    if (offset < 0) {
      return false;
    }
    for (int i = 0; i < word.length(); ++i) {
      if (offset + i >= text.length() || word.charAt(i) != text.charAt(offset + i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Judge whether a character is an english punctuation or not.
   *
   * @param ch
   * @return
   */
  public static boolean isPunctuation(char ch) {
    return ch == ',' || ch == '.' || ch == '!' || ch == '?' || ch == ':' || ch == ';';
  }

  /**
   * Judge whether a character is an english letter or not.
   *
   * @param ch
   * @return
   */
  public static boolean isEnglishLetter(char ch) {
    return Character.isLowerCase(ch) || Character.isUpperCase(ch);
  }

  public static void main(String[] args) {
    for (String part : splitKParts("add:bb", ":", 2)) {
      System.out.println(part);
    }
    System.out.println("---");
    for (String part : splitKParts(":3:add:bb", ":", 4)) {
      System.out.println(part);
    }
  }
}
