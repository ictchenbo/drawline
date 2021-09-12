package ict.ada.drawline.rule;

import ict.ada.drawline.Document;
import ict.ada.drawline.result.Instance;
import ict.ada.drawline.result.InstanceSet;
import ict.ada.drawline.util.StringUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MConcept supports concatenation of other rules.
 * Example:
 * CONCEPT:AGE:20
 * CONCEPT:NAME:Jobs
 * MCONCEPT:NAMEwithAGE:AGE years' old NAME
 *
 * In fact, MCONCEPT is a special case of MCONCEPT_RULE. But due to historic reason, it's remained.
 *
 * @author Jiyuan Lin
 */
public class MConcept extends Rule {

  public static final String IDENTIFIER = "MCONCEPT";

  protected String[] neighborTexts;
  protected boolean isAnalysed;

  /**
   * Construct MConcept without analysing its content.
   *
   * @param name
   * @param content
   */
  protected MConcept(String name, String content) {
    super(name, content);
    this.neighborTexts = null;
    this.isAnalysed = false;
  }

  /**
   * Construct MConcept by specifying the dependencies and neighborTexts.
   *
   * @param name
   * @param content
   * @param dependencies
   * @param neighborTexts
   */
  protected MConcept(String name, String content, String[] dependencies, String[] neighborTexts) {
    super(name, content, dependencies);
    this.neighborTexts = neighborTexts;
    this.isAnalysed = true;
  }

  /**
   * Construct MConcept by specifying the dependencies and neighborTexts.
   *
   * @param name
   * @param content
   * @param dependencies
   * @param neighborTexts
   */
  protected MConcept(String name, String content, List<String> dependencies,
      List<String> neighborTexts) {
    this(name, content, dependencies.toArray(new String[dependencies.size()]),
        neighborTexts.toArray(new String[neighborTexts.size()]));
  }

  @Override public String getIdentifier() {
    return IDENTIFIER;
  }

  /**
   * A helper Predicate to test an instance's neighbor text is satisfied or not.
   */
  private static class TextPredicate implements Predicate<Instance> {
    private String doc;
    private String left;
    private String right;

    public TextPredicate(String doc) {
      this.doc = doc;
    }

    public void setLeft(String left) {
      this.left = left;
    }

    public void setRight(String right) {
      this.right = right;
    }

    public boolean apply(Instance instance) {
      return StringUtil.equals(left, doc, instance.getOffset() - left.length()) && StringUtil
          .equals(right, doc, instance.getOffset() + instance.getLength());
    }
  }

  /**
   * Get instances belonging to dependencies from instanceSet after filtering by neighbor text.
   *
   * @param instanceSet
   * @param doc
   * @return
   */
  private ImmutableList<Instance>[] getDependentInstances(InstanceSet instanceSet, String doc) {
    ImmutableList<Instance>[] depInstances = new ImmutableList[dependencies.length];
    TextPredicate pred = new TextPredicate(doc);
    for (int i = 0; i < dependencies.length; ++i) {
      pred.setLeft(neighborTexts[i]);
      pred.setRight(neighborTexts[i + 1]);
      depInstances[i] = ImmutableList
          .copyOf(Iterables.filter(instanceSet.getInstancesByName(dependencies[i]), pred));
    }
    return depInstances;
  }

  /**
   * Do a combination.
   *
   * @param depInstances
   * @return
   */
  private ImmutableList<Instance> generate(ImmutableList<Instance>[] depInstances) {
    List<Instance> result = new ArrayList<Instance>();
    final int n = depInstances.length;
    int[] pos = new int[n];
    for (Instance begin : depInstances[0]) {
      // Current interval's left & right point
      int left = begin.getOffset() - neighborTexts[0].length();
      int right = begin.getOffset() + begin.getLength() + neighborTexts[1].length() - 1;
      int i = 1;
      while (0 < i && i < n && pos[i] < depInstances[i].size()) {
        Instance next = depInstances[i].get(pos[i]);
        if (next.getOffset() > right + 1) {
          if (--i > 0) {
            right -= depInstances[i].get(pos[i]).getLength() + neighborTexts[i + 1].length();
            ++pos[i];
          }
        } else if (next.getOffset() == right + 1) {
          right += next.getLength() + neighborTexts[i + 1].length();
          ++i;
        } else {
          ++pos[i];
        }
      }
      if (i == n) {
        result.add(new Instance(this, left, right - left + 1));
      }
    }
    return ImmutableSet.copyOf(result).asList();
  }

  /**
   * Gather all the instances of this rule's dependencies, do a combination to generate
   * this rule's instances which are in doc.
   *
   * @param instanceSet instance set to use
   * @param doc         document to match
   * @return
   */
  @Override public ImmutableList<Instance> match(InstanceSet instanceSet, Document doc) {
    if (!isAnalysed) {
      throw new RuntimeException(String.format("Dependencies of %s is not analysed yet.", this));
    }
    return generate(getDependentInstances(instanceSet, doc.getText()));
  }

  /**
   * Split the content into dependent rules and neighbor texts using a lefter and longer RuleName first strategy.
   * After split, content = neighborText[0] + dependencies[0] + neighborText[1] + ... + neighborText[m]
   * where m = dependencies.size().
   * The algorithm is to enumerate all the intervals which is O(n^2), and check whether in ruleNames or not.
   *
   * @param ruleNames
   * @return
   */
  public MConcept analyseDependencies(Set<String> ruleNames) {
    List<String> dependencies = new ArrayList<String>();
    List<String> neighborTexts = new ArrayList<String>();
    int n = content.length();
    // right point of last interval (a fake one)
    int last = -1;
    for (int i = 0, j; i < n; ) {
      StringBuilder tryName = new StringBuilder(content.substring(i, n));
      for (j = n; j > i && !ruleNames.contains(tryName.toString()); --j) {
        tryName.deleteCharAt(tryName.length() - 1);
      }
      if (j == i) {
        ++i;
      } else {
        neighborTexts.add(content.substring(last + 1, i));
        dependencies.add(tryName.toString());
        i = j;
        last = j - 1;
      }
    }
    neighborTexts.add(content.substring(last + 1));
    if (dependencies.isEmpty()) {
      throw new IllegalArgumentException(String.format("Not dependency is found in: %s", this));
    }
    this.dependencies = dependencies.toArray(new String[dependencies.size()]);
    this.neighborTexts = neighborTexts.toArray(new String[neighborTexts.size()]);
    this.isAnalysed = true;
    return this;
  }

  /**
   * An overloaded method where the input is a RuleSet.
   *
   * @param ruleSet
   */
  public MConcept analyseDependencies(RuleSet ruleSet) {
    return analyseDependencies(ruleSet.getRuleNames());
  }

  /**
   * New an instance without analysing its content.
   *
   * @param fullName
   * @param content
   */
  public static MConcept newInstance(String fullName, String content) {
    return new MConcept(fullName, content);
  }

  /**
   * New an instance and analyse the dependencies using the ruleNames.
   *
   * @param fullName  MCONCEPT fullName
   * @param content   MCONCEPT content
   * @param ruleNames Exist rule names
   */
  public static MConcept newInstance(String fullName, String content, Set<String> ruleNames) {
    return new MConcept(fullName, content).analyseDependencies(ruleNames);
  }

  /**
   * New an instance and analyse the dependencies using the RuleSet's ruleNames.
   *
   * @param fullName
   * @param content
   * @param ruleSet
   */
  public static MConcept newInstance(String fullName, String content, RuleSet ruleSet) {
    return newInstance(fullName, content, ruleSet.getRuleNames());
  }

  /**
   * Info of dependencies and neighborText, it looks like:
   * [neighborText[0] + dependencies[0] + neighborText[1] + ... + neighborText[m]]
   */
  public String info() {
    StringBuilder internalString = new StringBuilder();
    internalString.append("[");
    for (int i = 0, m = dependencies.length; i < m; ++i) {
      internalString.append(neighborTexts[i]);
      internalString.append("+");
      internalString.append(dependencies[i]);
      internalString.append("+");
    }
    internalString.append(neighborTexts[neighborTexts.length - 1]);
    internalString.append("]");
    return internalString.toString();
  }

  public static void main(String[] args) {
    Set<String> ruleNames =
        new HashSet<String>(Arrays.asList("ABBBC", "BBCC", "BCC", "CC", "AA", "A"));
    System.out.println(newInstance("abc", "a", ruleNames).info());
    System.out.println(newInstance("abc", "", ruleNames).info());
    System.out.println(newInstance("abc", "AAABBBCCCC", ruleNames).info());
  }
}
