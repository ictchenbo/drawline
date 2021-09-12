package ict.ada.drawline.rule;

import com.google.common.collect.ImmutableList;
import ict.ada.drawline.Document;
import ict.ada.drawline.result.Instance;
import ict.ada.drawline.result.InstanceSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Concept is the simplest form of Rule.
 * Example:
 * CONCEPT:NAME:Jobs
 *
 * @author Jiyuan Lin
 */
public class Concept extends Rule {

  public static final String IDENTIFIER = "CONCEPT";

  protected Concept(String name, String content) {
    super(name, content);
  }

  @Override public String getIdentifier() {
    return IDENTIFIER;
  }

  /**
   * Find all the appearance in doc.
   * This method should NOT be called. Automata should be used to do multiple CONCEPT matching in doc.
   *
   * @deprecated no need to use this
   * @param doc
   * @return
   */
  @Deprecated public ImmutableList<Instance> generate(String doc) {
    List<Instance> result = new ArrayList<Instance>();
    for (int i = 0, length = content.length();
         i < length && (i = doc.indexOf(content, i)) != -1; ++i) {
      result.add(new Instance(this, i, i + length));
    }
    return ImmutableList.copyOf(result);
  }

  @Override public ImmutableList<Instance> match(InstanceSet instanceSet, Document doc) {
    return generate(doc.getText());
  }

  public static Concept newInstance(String name, String content) {
    return new Concept(name, content);
  }
}
