package ict.ada.drawline.rule.operator;

/**
 * Sent operator which requires all its operands in same sentence.
 * Definition of sentence, please refer to comments of ict.ada.drawline.Document class.
 *
 * @author Jiyuan Lin
 */
public class Sent extends Operator {
  protected static final Sent Instance = new Sent();

  protected Sent() {
  }

  public static Sent getInstance() {
    return Instance;
  }

  @Override public String toString() {
    return "Sent";
  }
}
