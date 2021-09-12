package ict.ada.drawline;

import ict.ada.drawline.ds.AhoCorasickAutomata;
import ict.ada.drawline.result.Instance;
import ict.ada.drawline.result.InstanceSet;
import ict.ada.drawline.rule.Concept;
import ict.ada.drawline.rule.MConcept;
import ict.ada.drawline.rule.MConceptRule;
import ict.ada.drawline.rule.RegexConcept;
import ict.ada.drawline.rule.Rule;
import ict.ada.drawline.rule.RuleSet;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;

/**
 * Drawline is a rule based information extractioner. There are 4 kinds of rules:
 * 1) Concept: do an exact text matching
 * 2) RegexConcept: matching using regex
 * 3) MConcept: concatenates rules to match
 * 4) MConceptRule: use an operator (one of And, Dist, Or, Ord, Sent) and operands (can be any kind of the rules or words) to do matching
 *
 * To construct Drawline, feed a ruleSet (no matter from RuleParser or added manually) to it.
 * Then it can be used to match a document.
 *
 * @author Jiyuan Lin
 */
public class Drawline {
  /**
   * Some predicates to identify a Rule's concrete type, where
   * Concept & RegexConcept are low level rules while MConcept & MConceptRule are high level rules.
   */
  protected static final Predicate<Rule> ConceptPredicate =
      Predicates.or(Predicates.instanceOf(Concept.class));
  protected static final Predicate<Rule> RegexConceptPredicate =
      Predicates.or(Predicates.instanceOf(RegexConcept.class));
  protected static final Predicate<Rule> HighLevelRulePredicate = Predicates
      .or(Predicates.instanceOf(MConcept.class), Predicates.instanceOf(MConceptRule.class));

  /**
   * RuleSet to maintain a collections of rules.
   */
  protected RuleSet ruleSet;
  /**
   * Aho-Corasick Automata to find occurrences of all the Concepts in one traversal.
   */
  protected AhoCorasickAutomata automata;

  /**
   * Primary constructor, construct Drawline given a RuleSet.
   *
   * @param ruleSet given ruleSet to feed
   */
  public Drawline(RuleSet ruleSet) {
    this.ruleSet = ruleSet.clone();
    this.automata = new AhoCorasickAutomata();
    this.automata.addAll(Iterables.filter(ruleSet.sortedRules(), Concept.class));
  }

  /**
   * Find all the occurrences in a document which satisfy any rule in the ruleSet, and return the sorted instances.
   *
   * @param doc given document to match
   */
  public List<Instance> match(Document doc) {
    return match(doc, InstanceSet.EmptyInstanceList);
  }

  /**
   * Match a document with additional instances generated in other ways (NER, etc.).
   *
   * @param doc       given document to match
   * @param instances additional instances
   */
  public ImmutableList<Instance> match(Document doc, List<Instance> instances) {
    InstanceSet instanceSet = new InstanceSet();
    // low level match using regex and automata
    instanceSet.addInstances(Iterables.concat(instances, automata.match(doc.getText())));
    for (RegexConcept rule : Iterables.filter(ruleSet.sortedRules(), RegexConcept.class)) {
      instanceSet.addSortedInstancesOfSameRule(rule, rule.match(instanceSet, doc));
    }
    // high level match
    for (Rule rule : Iterables.filter(ruleSet.sortedRules(), HighLevelRulePredicate)) {
      instanceSet.addSortedInstancesOfSameRule(rule, rule.match(instanceSet, doc));
    }
    return instanceSet.getAllInstances();
  }
}
