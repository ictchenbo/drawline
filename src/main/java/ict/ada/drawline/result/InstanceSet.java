package ict.ada.drawline.result;

import ict.ada.drawline.rule.Rule;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * InstanceSet maintains a set of sorted instances partitioned by same Rule and same Rule name.
 * There is no duplicates in each bucket.
 * Once a rule's instances are added into this set, they are immutable. Any future addition will cause an exception.
 *
 * @author Jiyuan Lin
 */
public class InstanceSet {

  public static final ImmutableList<Instance> EmptyInstanceList = ImmutableList.of();
  /**
   * A predicate used to filter instances generated not from temporary rule.
   */
  public static final Predicate<Instance> NotTemporaryInstancePredicate =
      new Predicate<Instance>() {
        @Override public boolean apply(Instance instance) {
          return !instance.rule.isTemporary();
        }
      };

  /**
   * Buckets used to store sorted instances of same Rule and same Rule name.
   */
  private Map<Rule, ImmutableList<Instance>> instancesByRule;
  private Map<String, SortedSet<Instance>> instancesByName;

  /**
   * Default constructor of InstanceSet which initializes the buckets.
   */
  public InstanceSet() {
    this.instancesByRule = new HashMap<Rule, ImmutableList<Instance>>();
    this.instancesByName = new HashMap<String, SortedSet<Instance>>();
  }

  /**
   * Return underlying sorted and immutable instances of the given rule.
   *
   * @param rule given rule
   */
  public ImmutableList<Instance> getInstancesByRule(Rule rule) {
    if (!instancesByRule.containsKey(rule)) {
      return EmptyInstanceList;
    }
    return instancesByRule.get(rule);
  }

  /**
   * Return a list of sorted instances of same rule name.
   *
   * @param name given rule name
   */
  public ImmutableList<Instance> getInstancesByName(String name) {
    if (!instancesByName.containsKey(name)) {
      return EmptyInstanceList;
    }
    return ImmutableList.copyOf(instancesByName.get(name));
  }

  /**
   * Add a list of sorted, de-duplicated instances generated from same rule.
   * If this rule has been added before, an exception will thrown.
   *
   * @param rule            the rule these instances belong to
   * @param sortedInstances assumed to be sorted, without duplicates and come from same rule
   */
  public void addSortedInstancesOfSameRule(Rule rule, Iterable<Instance> sortedInstances) {
    if (instancesByRule.containsKey(rule)) {
      throw new RuntimeException(String.format("Rule %s has been added in InstanceSet!", rule));
    }
    // defensive copy
    ImmutableList<Instance> sortedInstancesList = ImmutableList.copyOf(sortedInstances);
    instancesByRule.put(rule, sortedInstancesList);
    String name = rule.getName();
    if (!instancesByName.containsKey(name)) {
      instancesByName.put(name, new TreeSet<Instance>());
    }
    instancesByName.get(name).addAll(sortedInstancesList);
  }

  /**
   * Add a list of instances.
   *
   * @param instances a list of instances, not require to be sorted or generated from same rule
   */
  public void addInstances(Iterable<Instance> instances) {
    Map<Rule, List<Instance>> instancesByRule = new HashMap<Rule, List<Instance>>();
    for (Instance instance : instances) {
      Rule rule = instance.getRule();
      if (!instancesByRule.containsKey(rule)) {
        instancesByRule.put(rule, new ArrayList<Instance>());
      }
      instancesByRule.get(rule).add(instance);
    }
    for (Map.Entry<Rule, List<Instance>> e : instancesByRule.entrySet()) {
      Collections.sort(e.getValue());
      addSortedInstancesOfSameRule(e.getKey(), e.getValue());
    }
  }

  /**
   * Return all instances with or without these generated from temporary rules.
   */
  public ImmutableList<Instance> getAllInstances(boolean withTemporary) {
    Iterable<Instance> instances =
        Iterables.mergeSorted(instancesByName.values(), InstanceComparator.getInstance());
    if (!withTemporary) {
      instances = Iterables.filter(instances, NotTemporaryInstancePredicate);
    }
    return ImmutableList.copyOf(instances);
  }

  /**
   * Return all instances excluding these generated from temporary rules.
   */
  public ImmutableList<Instance> getAllInstances() {
    return getAllInstances(false);
  }
}
