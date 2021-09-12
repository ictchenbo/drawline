package ict.ada.drawline.rule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RuleSet maintains a set of rules partitioned by name and sort the underlying rules according to
 * their name's order in the dependency graph.
 * For each name bucket, rules are de-duplicated by their stringForm, not their semantic.
 *
 * @author Jiyuan Lin
 */
public class RuleSet {

  public static final ImmutableList<Rule> EmptyRuleList = ImmutableList.of();

  /**
   * Comparator for (String, Integer) which only compares the value part.
   */
  protected static final Comparator<Map.Entry<String, Integer>> NameOrderComparator =
      new Comparator<Map.Entry<String, Integer>>() {
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
          return o1.getValue() - o2.getValue();
        }
      };

  /**
   * Store rules which are partitioned by name.
   */
  private Map<String, Set<Rule>> rulesByName;
  /**
   * Whether new rules are added since last build.
   */
  private boolean hasNewRule;
  /**
   * Sorted Rules according to their topological order.
   */
  private ImmutableList<Rule> sortedRules;

  /**
   * Primary constructor.
   */
  public RuleSet() {
    this.rulesByName = new HashMap<String, Set<Rule>>();
    this.hasNewRule = false;
    this.sortedRules = EmptyRuleList;
  }

  /**
   * Copy constructor.
   *
   * @param that
   */
  public RuleSet(RuleSet that) {
    this();
    addAll(that.sortedRules());
  }

  /**
   * Add a rule.
   *
   * @param rule
   */
  public void add(Rule rule) {
    String name = rule.getName();
    if (!rulesByName.containsKey(name)) {
      rulesByName.put(name, new HashSet<Rule>());
    }
    hasNewRule = rulesByName.get(name).add(rule) || hasNewRule;
  }

  /**
   * Add rule set
   * @param that
   */
  public void add(RuleSet that) {
    for(String name: that.getRuleNames()){
      this.addAll(that.getRules(name));
    }
  }

  /**
   * Add a collection of rules.
   *
   * @param rules
   */
  public void addAll(Iterable<Rule> rules) {
    for (Rule rule : rules) {
      add(rule);
    }
  }


  /**
   * Whether the set contains a rule name or not.
   *
   * @param name
   */
  public boolean contains(String name) {
    return rulesByName.keySet().contains(name);
  }

  /**
   * Return a set of underlying rules' names.
   */
  public Set<String> getRuleNames() {
    return rulesByName.keySet();
  }

  /**
   * Calculate a name's order in the dependency graph by dfs. Throw exception if loop is found.
   *
   * @param name      name to consider
   * @param nameOrder already known order of names
   * @param edges     edge set of the dependency graph
   */
  private int calcOrder(String name, Map<String, Integer> nameOrder,
      Map<String, Set<String>> edges) {
    if (nameOrder.containsKey(name)) {
      int depth = nameOrder.get(name);
      if (depth == -1) {
        throw new RuntimeException(String.format("Loop found when calculating: %s", name));
      }
      return depth;
    }
    // put -1 deliberately to check loop
    nameOrder.put(name, -1);
    int depth = 0;
    for (String dep : edges.get(name)) {
      depth = Math.max(depth, calcOrder(dep, nameOrder, edges) + 1);
    }
    nameOrder.put(name, depth);
    return depth;
  }

  /**
   * Check whether dependencies of each rule are in this ruleSet and analyse dependencies of MConcept.
   * If not, construct a concept of such dependency and add to it.
   */
  private void checkDependencies() {
    List<Rule> newConcepts = new ArrayList<Rule>();
    for (Set<Rule> ruleSet : rulesByName.values()) {
      for (Rule rule : ruleSet) {
        if (rule instanceof MConcept) {
          ((MConcept) rule).analyseDependencies(this);
        }
        for (String dependency : rule.dependencies) {
          if (!contains(dependency)) {
            newConcepts.add(new Concept(dependency, dependency));
          }
        }
      }
    }
    addAll(newConcepts);
  }

  /**
   * Sort rules according to their topological order.
   * Take rule's name as node, rule's dependency as edge, then this is a DAG graph.
   * A name's order is its depth in the graph. Lower name ranks early than higher name.
   * A rule's order is its name's order.
   */
  public void build() {
    if (!hasNewRule) {
      return;
    }
    checkDependencies();
    Map<String, Set<String>> edges = new HashMap<String, Set<String>>();
    for (String name : rulesByName.keySet()) {
      edges.put(name, new HashSet<String>());
    }
    for (Set<Rule> ruleSet : rulesByName.values()) {
      for (Rule rule : ruleSet) {
        String name = rule.getName();
        for (String dep : rule.getDependencies()) {
          edges.get(name).add(dep);
        }
      }
    }
    Map<String, Integer> nameOrder = new HashMap<String, Integer>();
    for (String name : getRuleNames()) {
      calcOrder(name, nameOrder, edges);
    }
    List<Map.Entry<String, Integer>> nameOrderList = Lists.newArrayList(nameOrder.entrySet());
    Collections.sort(nameOrderList, NameOrderComparator);
    List<Rule> sortedNameRules = new ArrayList<Rule>();
    for (Map.Entry<String, Integer> e : nameOrderList) {
      sortedNameRules.addAll(rulesByName.get(e.getKey()));
    }
    sortedRules = ImmutableList.copyOf(sortedNameRules);
    hasNewRule = false;
  }

  /**
   * Return sorted rules.
   */
  public ImmutableList<Rule> sortedRules() {
    build();
    return sortedRules;
  }

  /**
   * Return a defensive copy of the underlying rules with the specific name.
   *
   * @param name given rule name
   */
  public ImmutableList<Rule> getRules(String name) {
    if (!rulesByName.containsKey(name)) {
      return EmptyRuleList;
    }
    return ImmutableList.copyOf(rulesByName.get(name));
  }

  /**
   * Clone this RuleSet.
   */
  @Override public RuleSet clone() {
    return new RuleSet(this);
  }
}
