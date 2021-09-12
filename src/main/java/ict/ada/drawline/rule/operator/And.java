package ict.ada.drawline.rule.operator;

/**
 * And operator which requires all its operands to be occurred in the text.
 *
 * @author Jiyuan Lin
 */
public class And extends Operator {

  protected static final And Instance = new And();

  protected And() {
  }

  public static And getInstance() {
    return Instance;
  }

  @Override public String toString() {
    return "And";
  }
}
