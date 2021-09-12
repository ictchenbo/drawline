package ict.ada.drawline.rule;

import ict.ada.drawline.Document;
import ict.ada.drawline.result.Instance;
import ict.ada.drawline.result.InstanceSet;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegexConcept supports Regex.
 * <p/>
 * REGEX:AGE:\d{1,3}
 *
 * @author Jiyuan Lin
 */
public class RegexConcept extends Rule {

  public static final String IDENTIFIER = "REGEX";

  private Pattern pattern;

  protected RegexConcept(String name, String content) {
    super(name, content);
    this.pattern = Pattern.compile(content);
  }

  @Override public String getIdentifier() {
    return IDENTIFIER;
  }

  @Override public ImmutableList<Instance> match(InstanceSet instanceSet, Document doc) {
    Matcher matcher = pattern.matcher(doc.getText());
    List<Instance> instances = new ArrayList<Instance>();
    while (matcher.find()) {
      MatchResult res = matcher.toMatchResult();
      instances.add(new Instance(this, res.start(), res.end() - res.start()));
    }
    return ImmutableList.copyOf(instances);
  }

  /**
   * Overloaded version for convenience.
   *
   * @param doc
   * @return
   */
  public ImmutableList<Instance> match(Document doc) {
    return match(null, doc);
  }

  public static RegexConcept newInstance(String name, String content) {
    return new RegexConcept(name, content);
  }

  public static void main(String[] args) {
    RegexConcept rule = RegexConcept.newInstance("test", "\\d{1,3}");
    Document doc =  new Document("hello this is a 1 and that 2333 23");
    for (Instance inst : rule.match(doc)) {
      System.out.println(inst.toString(doc.getText()));
    }
  }
}
