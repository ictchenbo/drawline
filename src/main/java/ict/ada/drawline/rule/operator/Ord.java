package ict.ada.drawline.rule.operator;

/**
 * Ord operator which requires all its operands to be occurred in order.
 *
 * @author Jiyuan Lin
 */
public class Ord extends Operator {

  protected static final Ord Instance = new Ord();

  protected Ord() {
  }

  public static Ord getInstance() {
    return Instance;
  }

  @Override public String toString() {
    return "Ord";
  }
}
