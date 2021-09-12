package ict.ada.drawline.rule.operator;

/**
 * Or operator which requires at least one of its operands to be occurred in the text.
 * <p/>
 * TODO:
 * More operands override less operands?
 *
 * @author Jiyuan Lin
 */
public class Or extends Operator {

  protected static final Or Instance = new Or();

  protected Or() {
  }

  public static Or getInstance() {
    return Instance;
  }

  @Override public String toString() {
    return "Or";
  }
}
