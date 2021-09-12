package ict.ada.drawline.result;

import java.util.Comparator;

/**
 * Comparator for Instance.
 * The comparing process is same as comparing tuple : (offset, offset + length, rule).
 *
 * @author Jiyuan Lin
 */
public class InstanceComparator implements Comparator<Instance> {
  /**
   * Singleton InstanceComparator.
   */
  private static InstanceComparator comparator = new InstanceComparator();

  private InstanceComparator() {
  }

  /**
   * Return the singleton InstanceComparator.
   * Since the cost of static constructing InstanceComparator is small, "double-check locking" is not used.
   */
  public static InstanceComparator getInstance() {
    return comparator;
  }

  /**
   * Compare two instance by comparing tuple: (offset, offset + length, rule).
   *
   * @param i1 the first instance
   * @param i2 the second instance
   */
  public int compare(Instance i1, Instance i2) {
    if (i1.getOffset() != i2.getOffset()) {
      return i1.getOffset() - i2.getOffset();
    }
    if (i1.getLength() != i2.getLength()) {
      return i1.getLength() - i2.getLength();
    }
    return i1.rule.compareTo(i2.rule);
  }
}
