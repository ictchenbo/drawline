package ict.ada.drawline.result;

import ict.ada.drawline.rule.Rule;

import com.google.common.collect.ImmutableList;

/**
 * DependencyInstance is an instance with dependencies.
 *
 * @author Jiyuan Lin
 */
public class DependencyInstance extends Instance {

  protected final Instance[] dependencies;

  public DependencyInstance(DependencyInstance that) {
    this(that.rule, that.offset, that.length, that.dependencies);
  }

  public DependencyInstance(Rule rule, int offset, int length, Instance[] dependencies) {
    super(rule, offset, length);
    this.dependencies = dependencies;
  }

  public ImmutableList<Instance> getDependencies() {
    return ImmutableList.copyOf(dependencies);
  }

  @Override public DependencyInstance clone() {
    return new DependencyInstance(this);
  }
}
