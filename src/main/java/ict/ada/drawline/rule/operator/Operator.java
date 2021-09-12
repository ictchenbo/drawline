package ict.ada.drawline.rule.operator;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import javax.management.ImmutableDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 * Operators in MCONCEPT_RULE.
 *
 * @author Jiyuan Lin
 */
public abstract class Operator {

  /**
   * Reduce Dist_k
   *
   * @param operators
   * @return
   */
  public static List<Operator> reduceOperators(List<Operator> operators) {
    if (operators.isEmpty()) {
      return operators;
    }
    List<Operator> reducedOperators = new ArrayList<Operator>();
    Dist lastDist = null;
    for (Operator operator : operators) {
      if (operator instanceof Sent) {
        reducedOperators.remove(Sent.getInstance());
        reducedOperators.add(Sent.getInstance());
      }
      if (operator instanceof Ord) {
        reducedOperators.remove(Ord.getInstance());
        reducedOperators.add(Ord.getInstance());
      }
      if (operator instanceof Dist) {
        if (lastDist == null) {
          lastDist = new Dist((Dist) operator);
          reducedOperators.add(lastDist);
        } else {
          reducedOperators.remove(lastDist);
          lastDist.setMinDist((Dist) operator);
          reducedOperators.add(lastDist);
        }
      }
    }
    Operator last = operators.get(operators.size() - 1);
    if ((last instanceof And) || (last instanceof Or)) {
      reducedOperators.add(last);
    }
    return reducedOperators;
  }

  /**
   * Using '+' to join a list of operators.
   *
   * @param operators
   * @return
   */
  public static String toString(List<Operator> operators) {
    return Joiner.on("+").join(operators);
  }

  public static Operator newInstance(String operator) {
    if (operator.equals("AND")) {
      return And.getInstance();
    }
    if (operator.equals("OR")) {
      return Or.getInstance();
    }
    if (operator.equals("ORD")) {
      return Ord.getInstance();
    }
    if (operator.equals("SENT")) {
      return Sent.getInstance();
    }
    if (operator.startsWith("DIST_")) {
      return new Dist(Integer.parseInt(operator.substring(5)));
    }
    throw new IllegalArgumentException(String.format("Unknown operator: %s", operator));
  }

  public static void main(String[] args) {
    List<Operator> operators = new ArrayList<Operator>();
    operators.add(And.getInstance());
    operators.add(new Dist(3));
    operators.add(Sent.getInstance());
    operators.add(new Dist(1));
    operators.add(Or.getInstance());
    operators.add(Ord.getInstance());
    operators.add(And.getInstance());
    operators.add(And.getInstance());
    System.out.println(ImmutableList.copyOf(reduceOperators(operators)));
  }
}
