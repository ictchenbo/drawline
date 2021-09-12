package ict.ada.drawline.result;

import ict.ada.drawline.rule.Rule;

import com.google.common.collect.ImmutableList;

/**
 * Instance records the offset and length generated from a rule.
 * Instance is immutable.
 *
 * @author Jiyuan Lin
 */
public class Instance implements Comparable {
  /**
   * Rule which generates this instance.
   */
  protected final Rule rule;
  /**
   * Character offset and length of this instance.
   */
  protected final int offset;
  protected final int length;

  /**
   * Copy constructor.
   *
   * @param that Instance to be copied from
   */
  public Instance(Instance that) {
    this(that.rule, that.offset, that.length);
  }

  /**
   * Primary constructor.
   *
   * @param rule
   * @param offset
   * @param length
   */
  public Instance(Rule rule, int offset, int length) {
    this.rule = rule;
    this.offset = offset;
    this.length = length;
  }

  /**
   * Return rule which generates this instance.
   */
  public Rule getRule() {
    return rule;
  }

  /**
   * Return character offset.
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Return character length.
   */
  public int getLength() {
    return length;
  }

  /**
   * Return the string form of this instance, which is a tuple of (offset, length, rule).
   */
  @Override public String toString() {
    return String.format("(%d,%d,%s)", offset, length, rule);
  }

  /**
   * Return matched text from the target document in addition to the default toString method.
   *
   * @param doc target document text
   */
  public String toString(String doc) {
    return String.format("%s%s", doc.substring(offset, offset + length), toString());
  }

  /**
   * Return matched text.
   *
   * @param doc target document text
   */
  public String getText(String doc) {
    return doc.substring(offset, offset + length);
  }


  /**
   * Compare two instance by comparing tuple: (offset, offset + length, rule).
   *
   * @param o the other instance to compare to
   */
  public int compareTo(Object o) {
    return InstanceComparator.getInstance().compare(this, (Instance) o);
  }

  /**
   * Whether the given object is equal to this instance.
   *
   * @param o
   */
  @Override public boolean equals(Object o) {
    return (o instanceof Instance) && compareTo(o) == 0;
  }

  /**
   * Return this instance's hashCode which is equal to the hasCode of tuple(offset, length, rule).
   */
  @Override public int hashCode() {
    return ImmutableList.of(offset, length, rule.hashCode()).hashCode();
  }

  /**
   * Clone this instance.
   */
  @Override public Instance clone() {
    return new Instance(this);
  }
}
