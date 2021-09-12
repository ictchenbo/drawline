package ict.ada.drawline.result;

/**
 *
 * @author Jiyuan Lin
 */
public class ParameterValue {
  final String name;
  final Instance instance;

  public ParameterValue(String name, Instance instance) {
    this.name = name;
    this.instance = instance;
  }

  public Instance getInstance() {
    return instance;
  }

  public String getName() {
    return name;
  }

  @Override public String toString() {
    return String.format("%s=%s", name, instance);
  }
}
