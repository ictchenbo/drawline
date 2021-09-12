package ict.ada.drawline.ds;

import ict.ada.drawline.result.Instance;
import ict.ada.drawline.rule.Concept;

import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Aho Corasick automata
 *
 * @author Jiyuan Lin
 */
public class AhoCorasickAutomata {

  private TrieNode root;
  private Set<Concept> conceptSet;
  private Set<Character> characterSet;
  private boolean hasNewConcept;

  public AhoCorasickAutomata() {
    this.root = null;
    this.conceptSet = new HashSet<Concept>();
    this.characterSet = new HashSet<Character>();
    this.hasNewConcept = false;
  }

  /**
   * Add concept to conceptSet, and set hasNewConcept to true if the concept doesn't exist in the conceptSet.
   *
   * @param concept
   */
  public void add(Concept concept) {
    hasNewConcept = conceptSet.add(concept) || hasNewConcept;
  }

  /**
   * Add a collections of concepts.
   *
   * @param concepts
   */
  public void addAll(Iterable<Concept> concepts) {
    for (Concept concept : concepts) {
      add(concept);
    }
  }

  /**
   * Find first available node which contains ch and return its children node.
   * Also, put the result to nodes which doesn't contain ch along the path.
   *
   * @param node
   * @param ch
   * @return
   */
  protected TrieNode findFirstAvailable(TrieNode node, char ch) {
    if (node.children.containsKey(ch)) {
      return node.children.get(ch);
    }
    if (node == root) {
      return root;
    }
    TrieNode first = findFirstAvailable(node.fail, ch);
    node.children.put(ch, first);
    return first;
  }

  /**
   * Compute all nodes' fail.
   */
  public void build() {
    if (hasNewConcept) {
      root = new TrieNode();
      for (Concept concept : conceptSet) {
        root.add(concept);
        for (char ch : concept.getContent().toCharArray()) {
          characterSet.add(ch);
        }
      }
      root.fail = root;
      Queue<TrieNode> queue = new ArrayDeque<TrieNode>();
      for (TrieNode node : root.children.values()) {
        node.fail = root;
        queue.add(node);
      }
      while (!queue.isEmpty()) {
        TrieNode cur = queue.poll();
        for (Map.Entry<Character, TrieNode> entry : cur.children.entrySet()) {
          Character ch = entry.getKey();
          TrieNode child = entry.getValue();
          child.fail = findFirstAvailable(cur.fail, ch);
          queue.add(child);
        }
      }
      hasNewConcept = false;
    }
  }

  /**
   * Given a doc, find all the occurrences of the concepts.
   *
   * @param doc
   * @return
   */
  public ImmutableList<Instance> match(String doc) {
    build();
    List<Instance> instances = new ArrayList<Instance>();
    TrieNode cur = root;
    for (int i = 0; i < doc.length(); ++i) {
      char ch = doc.charAt(i);
      if (!characterSet.contains(ch)) {
        cur = root;
        continue;
      }
      TrieNode next = findFirstAvailable(cur, ch);
      for (Concept concept : next.getAccumulativeConcepts()) {
        int length = concept.getContent().length();
        int offset = i - length + 1;
        instances.add(new Instance(concept, offset, length));
      }
      cur = next;
    }
    return ImmutableList.copyOf(instances);
  }

  public static void main(String[] args) {
    AhoCorasickAutomata automata = new AhoCorasickAutomata();
    automata.addAll(ImmutableList
        .of(Concept.newInstance("1", "abc"), Concept.newInstance("1", "ab"),
            Concept.newInstance("1", "bc"), Concept.newInstance("2", "abc")));
    System.out.println(automata.match("abcdccdabcbc"));
    automata = new AhoCorasickAutomata();
    automata
        .addAll(ImmutableList.of(Concept.newInstance("1", "abcd"), Concept.newInstance("1", "bc")));
    System.out.println(automata.match("abcdefg"));
  }
}
