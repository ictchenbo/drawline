package ict.ada.drawline.rule;

import ict.ada.drawline.Document;
import ict.ada.drawline.result.Instance;
import ict.ada.drawline.result.InstanceSet;
import ict.ada.drawline.util.StringUtil;

import com.google.common.collect.ImmutableList;

/**
 * Conceptually, a Rule has:
 * 1) identifier, fullName and content
 * 2) getName method. The difference between name and fullName is fullName can have parameters
 *    while name is the representative part of the rule and can be referred by other rules.
 * 2) dependencies which depend on Rule's name
 * 3) match method which accept a document and return instances which are the occurrences matched in the document
 *
 * Note:
 * 1) rule's content is a complete content, that's with restrictions of its parents (if has)
 * 2) temporary rule's fullName equals to the rule's content
 * 3) The string form of a rule is: identifier:fullName:content.
 * 4) Equality of rules is judged by such string form.
 *
 * @author Jiyuan Lin
 */
public abstract class Rule implements Comparable {

  public static final String DELIMITER = ":";
  public static final String[] EmptyDependencies = StringUtil.EmptyStringArray;
  /**
   * Logical parts of a Rule.
   */
  protected final String fullName;
  protected final String content;
  protected String[] dependencies;
  /**
   * Composite string form.
   */
  protected String stringForm;

  /**
   * Primary constructor of a Rule.
   *
   * @param fullName
   * @param content
   * @param dependencies
   */
  protected Rule(String fullName, String content, String[] dependencies) {
    this.fullName = fullName;
    this.content = content;
    this.dependencies = dependencies;
    this.stringForm = getIdentifier() + ":" + fullName + ":" + content;
  }

  /**
   * Constructor without dependencies.
   *
   * @param fullName
   * @param content
   */
  protected Rule(String fullName, String content) {
    this(fullName, content, EmptyDependencies);
  }

  /**
   * Concrete rule's identifier.
   */
  public abstract String getIdentifier();

  /**
   * By default, name is same as fullName.
   */
  public String getName() {
    return fullName;
  }

  /**
   * Return fullName.
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * Return content.
   */
  public String getContent() {
    return content;
  }

  /**
   * Return dependencies.
   */
  public String[] getDependencies() {
    return dependencies;
  }

  /**
   * Whether this rule is a temporary one or not.
   */
  public boolean isTemporary() {
    return fullName.equals(content);
  }

  /**
   * Return
   *
   * @param instanceSet a set of instances used to retrieved dependent instance
   * @param doc         the document to match
   * @return sorted matched instances
   */
  public abstract ImmutableList<Instance> match(InstanceSet instanceSet, Document doc);

  /**
   * String Form of a rule.
   */
  @Override public String toString() {
    return stringForm;
  }

  /**
   * Rule's hashCode is the hashCode of the underlying stringForm.
   */
  @Override public int hashCode() {
    return stringForm.hashCode();
  }

  @Override public boolean equals(Object o) {
    return (o instanceof Rule) && hashCode() == o.hashCode();
  }

  /**
   * Indeed, compare the underlying string form of two rules.
   *
   * @param o the other rule to compare to
   */
  @Override public int compareTo(Object o) {
    return stringForm.compareTo(((Rule) o).stringForm);
  }
}
