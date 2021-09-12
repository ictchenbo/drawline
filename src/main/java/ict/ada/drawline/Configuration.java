package ict.ada.drawline;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Global configurations for drawline.
 *
 * @author Jiyuan Lin
 */
public final class Configuration {

  //TODO BUG， `.` in english text may be abbreviation, for example e.g. Mr. Ms. in which case it's not sentence delimiter
  private static Set<String> SentenceDelimiters = ImmutableSet.of(".", "!", "?", "。", "！", "？", "\n", "\r");
  private static Set<String> CommentTags = ImmutableSet.of("//", "#", "%");
  private static String LineSeperator = System.lineSeparator();

  private Configuration() {
  }

  public static void setSentenceDelimiters(Iterable<String> delimiters) {
    SentenceDelimiters = ImmutableSet.copyOf(delimiters);
  }

  public static Set<String> getSentenceDelimiters() {
    return SentenceDelimiters;
  }

  public static void setCommentTags(Iterable<String> tags) {
    CommentTags = ImmutableSet.copyOf(tags);
  }

  public static Set<String> getCommentTags() {
    return CommentTags;
  }

  public static void setLineSeperator(String lineSeperator) {
    LineSeperator = lineSeperator;
  }

  public static String getLineSeperator() {
    return LineSeperator;
  }
}
