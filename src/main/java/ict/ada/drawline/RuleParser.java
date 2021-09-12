package ict.ada.drawline;

import ict.ada.drawline.rule.Concept;
import ict.ada.drawline.rule.MConcept;
import ict.ada.drawline.rule.MConceptRule;
import ict.ada.drawline.rule.RegexConcept;
import ict.ada.drawline.rule.Rule;
import ict.ada.drawline.rule.RuleSet;
import ict.ada.drawline.util.StringUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * RuleParser mainly parse lines of rules and construct a RuleSet.
 *
 * @author Jiyuan Lin
 */
public class RuleParser {


  /**
   * Check whether a line is a comment or not.
   *
   * @param line given line to check
   */
  public static boolean isComment(String line) {
    for (String tag : Configuration.getCommentTags()) {
      if (line.startsWith(tag)) {
        return true;
      }
    }
    return false;
  }

  /**
   * RulePart stores the three parts of a rule: identifier, fullName, content
   */
  private static class RulePart {
    private String identifier;
    private String fullName;
    private String content;

    public RulePart(String identifier, String fullName, String content) {
      this.identifier = identifier;
      this.fullName = fullName;
      this.content = content;
    }

    public String getIdentifier() {
      return identifier;
    }

    public String getFullName() {
      return fullName;
    }

    public String getContent() {
      return content;
    }

    public String toString() {
      return this.identifier + Rule.DELIMITER + this.fullName + Rule.DELIMITER + this.content;
    }
  }

  /**
   * Given string of the template file, check each line and filter comments.
   * Return split parts of each line.
   *
   * @param template input template file
   */
  public static List<RulePart> preprocess(String template) {
    List<RulePart> ruleParts = new ArrayList<RulePart>();
    for (String line : template.split(Configuration.getLineSeperator())) {
      line = line.trim();
      if (line.isEmpty() || isComment(line)) {
        continue;
      }
      String[] parts = StringUtil.splitKParts(line, Rule.DELIMITER, 3);
      ruleParts.add(new RulePart(parts[0], parts[1], parts[2]));
    }
    return ruleParts;
  }

  /**
   * Parse lines of rules and return a RuleSet. Comments and empty lines are removed.
   *
   * @param template lines of rules
   */
  public static RuleSet parse(String template) {
    RuleSet ruleSet = new RuleSet();
    for (RulePart rp : preprocess(template)) {
      if (rp.getIdentifier().equals(Concept.IDENTIFIER)) {
        ruleSet.add(Concept.newInstance(rp.getFullName(), rp.getContent()));
      } else if (rp.getIdentifier().equals(RegexConcept.IDENTIFIER)) {
        ruleSet.add(RegexConcept.newInstance(rp.getFullName(), rp.getContent()));
      } else if (rp.getIdentifier().equals(MConcept.IDENTIFIER)) {
        ruleSet.add(MConcept.newInstance(rp.getFullName(), rp.getContent()));
      } else if (rp.getIdentifier().equals(MConceptRule.IDENTIFIER)) {
        ruleSet.add(MConceptRule.newInstance(rp.getFullName(), rp.getContent(), ruleSet));
      } else {
        System.err.println("Unknown rule identifier: " + rp.getIdentifier());
      }
    }
    return ruleSet;
  }

  /**
   * An overloaded version of parse function while the input is template file path.
   *
   * @param templateFile template file path
   * @throws IOException
   */
  public static RuleSet parseFile(String templateFile) throws IOException {
    return parse(new String(Files.readAllBytes(Paths.get(templateFile))));
  }

  public static RuleSet parseFile(String templateFile, String charset) throws IOException {
    return parse(new String(Files.readAllBytes(Paths.get(templateFile)), charset));
  }

  public static void main(String[] args) throws IOException{

    RuleSet ruleSet = parseFile("test.cfg");

    String rule1 = " CONCEPT:a:123 xxx \r\r\n\r\n\n aaaa:b:c";

    System.out.println(System.lineSeparator().length());

    System.out.println(preprocess(rule1));

  }
}
