package ict.ada.drawline.result;

import com.google.common.collect.ImmutableList;
import ict.ada.drawline.rule.MConceptRule;

/**
 * MConceptRuleInstance
 *
 * @author Jiyuan Lin
 */
public class MConceptRuleInstance extends DependencyInstance {

  protected final ParameterValue[] parameterValues;

  public MConceptRuleInstance(MConceptRuleInstance that) {
    this((MConceptRule) that.rule, that.offset, that.length, that.dependencies);
  }

  public MConceptRuleInstance(MConceptRule rule, int offset, int length, Instance[] dependencies) {
    super(rule, offset, length, dependencies);
    this.parameterValues = calcParameterValues(rule.getParameters());
  }

  /**
   * Calculate the value of parameters.
   *
   * @param parameters
   * @return
   */
  protected ParameterValue[] calcParameterValues(MConceptRule.Parameter[] parameters) {
    ParameterValue[] parameterValues = new ParameterValue[parameters.length];
    for (int i = 0; i < parameters.length; ++i) {
      Instance cur = this;
      for (int x : parameters[i].getPath()) {
        cur = ((DependencyInstance) cur).dependencies[x];
      }
      parameterValues[i] = new ParameterValue(parameters[i].getName(), cur);
    }
    return parameterValues;
  }

  public ParameterValue[] getParameterValues() {
    return parameterValues;
  }

  @Override public MConceptRuleInstance clone() {
    return new MConceptRuleInstance(this);
  }

  @Override public String toString() {
    // A lazy way to use the pretty print of ImmutableList by passing data to it
    return super.toString() + ImmutableList.copyOf(getParameterValues());
  }
}

