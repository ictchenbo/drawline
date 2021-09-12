package ict.ada.drawline.ds;

import ict.ada.drawline.rule.Concept;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Jiyuan Lin
 */
public class TrieNode {

  protected Map<Character, TrieNode> children;
  protected TrieNode fail;
  private Set<Concept> concepts;
  private Set<Concept> accumulativeConcepts;

  public TrieNode() {
    // Use TreeMap instead of HashMap to save memory
    this.children = new TreeMap<Character, TrieNode>();
    this.fail = null;
    this.concepts = new TreeSet<Concept>();
  }

  public boolean add(Concept concept) {
    return add(concept, 0);
  }

  protected boolean add(Concept concept, int cur) {
    if (cur == concept.getContent().length()) {
      return concepts.add(concept);
    }
    char next = concept.getContent().charAt(cur);
    if (!children.containsKey(next)) {
      children.put(next, new TrieNode());
    }
    return children.get(next).add(concept, cur + 1);
  }

  public Set<Concept> getConcepts() {
    return concepts;
  }

  public Set<Concept> getAccumulativeConcepts() {
    if (fail == null) {
      throw new RuntimeException("Fail is not calculated yet!");
    }
    if (accumulativeConcepts == null) {
      accumulativeConcepts = new TreeSet<Concept>();
      accumulativeConcepts.addAll(fail.getAccumulativeConcepts());
      accumulativeConcepts.addAll(concepts);
    }
    return accumulativeConcepts;
  }
}
