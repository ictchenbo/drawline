package ict.ada.drawline.rule;

import ict.ada.drawline.Document;
import ict.ada.drawline.result.Instance;
import ict.ada.drawline.result.InstanceSet;
import ict.ada.drawline.result.MConceptRuleInstance;
import ict.ada.drawline.rule.operator.Dist;
import ict.ada.drawline.rule.operator.Operator;
import ict.ada.drawline.rule.operator.Or;
import ict.ada.drawline.rule.operator.Ord;
import ict.ada.drawline.rule.operator.Sent;
import ict.ada.drawline.util.ArrayUtil;
import ict.ada.drawline.util.StringUtil;
import ict.ada.drawline.util.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MConceptRule use a operator (one of And, Dist, Or, Ord, Sent) and operands (can be any kind of the rules) to do matching.
 *
 * @author Jiyuan Lin
 */
public class MConceptRule extends Rule {

  public static final String IDENTIFIER = "MCONCEPT_RULE";

  protected static final Parameter[] EmtpyParameters = new Parameter[0];

  protected static final Pattern ParameterPattern =
      Pattern.compile("\\s*_([^{}]+)\\{([^{}]+)\\}\\s*");

  private final String name;
  private final Parameter[] parameters;
  private final ImmutableList<Operator> operators;

  /**
   * Variables used in generate instances.
   */
  private final int dist;
  private final boolean hasSent;
  private final boolean hasOrd;
  private final boolean isOr;

  /**
   * @param fullName     original string form of fullName without parameters
   * @param content      original string form of content which has been rewritten
   * @param parameters   parameters to captured
   * @param operators    operators including parents', the last one is of itself
   * @param dependencies dependent rules' fullName
   */
  protected MConceptRule(String fullName, String content, Parameter[] parameters,
      List<Operator> operators, String[] dependencies) {
    super(fullName, content, dependencies);
    this.name = parseFullName(fullName);
    this.parameters = parameters;
    this.operators = ImmutableList.copyOf(Operator.reduceOperators(operators));
    this.dist = getDist(operators);
    this.hasSent = operators.contains(Sent.getInstance());
    this.hasOrd = operators.contains(Ord.getInstance());
    this.isOr = operators.get(operators.size() - 1) instanceof Or;
  }

  @Override public String getName() {
    return name;
  }

  @Override public String getFullName() {
    return fullName;
  }

  /**
   * Construct MConceptRule from its content, operators and dependencies,
   * where its fullName equals its content.
   *
   * @param content
   * @param operators
   * @param dependencies
   */
  protected MConceptRule(String content, List<Operator> operators, String[] dependencies) {
    super(content, content, dependencies);
    this.name = fullName;
    this.parameters = EmtpyParameters;
    this.operators = ImmutableList.copyOf(Operator.reduceOperators(operators));
    this.dist = getDist(operators);
    this.hasSent = operators.contains(Sent.getInstance());
    this.hasOrd = operators.contains(Ord.getInstance());
    this.isOr = operators.get(operators.size() - 1) instanceof Or;
  }

  /**
   * Construct MConceptRule from its operators and dependencies,
   * where fullName and content is constructed from operators and dependencies.
   *
   * @param operators
   */
  protected MConceptRule(List<Operator> operators, String[] dependencies) {
    this(toString(operators, dependencies), operators, dependencies);
  }

  public Parameter[] getParameters() {
    return parameters;
  }

  public ImmutableList<Operator> getOperators() {
    return operators;
  }

  @Override public String getIdentifier() {
    return IDENTIFIER;
  }

  /**
   * Get instances belonging to dependencies from instanceSet.
   *
   * @param instanceSet
   * @return
   */
  private ImmutableList<Instance>[] getDependentInstances(InstanceSet instanceSet) {
    ImmutableList<Instance>[] depInstances = new ImmutableList[dependencies.length];
    for (int i = 0; i < dependencies.length; ++i) {
      depInstances[i] = instanceSet.getInstancesByName(dependencies[i]);
    }
    return depInstances;
  }

  /**
   * Return the minimum dist from dist operators among a list of operators.
   * If no dist operator is found, return Integer.MAX_VALUE.
   *
   * @param operators
   */
  public static int getDist(List<Operator> operators) {
    int dist = Integer.MAX_VALUE;
    for (Operator operator : operators) {
      if (operator instanceof Dist) {
        Dist distOp = (Dist) operator;
        dist = Math.min(dist, distOp.getDist());
      }
    }
    return dist;
  }

  /**
   * Return a single sorted index array from an array of sorted instance list.
   * Duplicates are removed.
   *
   * @param instanceLists an array of sorted instance list
   * @return
   */
  public static Pair<Integer, Integer>[] getSortedInstanceIndex(
      ImmutableList<Instance>[] instanceLists) {
    int[] pos = new int[instanceLists.length];
    Arrays.fill(pos, 0);
    int n = 0;
    for (ImmutableList<Instance> instanceList : instanceLists) {
      n += instanceList.size();
    }
    Pair<Integer, Integer>[] sortedInstanceIndex = new Pair[n];
    for (int i = 0; i < n; ++i) {
      Instance min = null;
      Pair<Integer, Integer> cur = null;
      for (int j = 0; j < pos.length; ++j) {
        if (pos[j] < instanceLists[j].size()) {
          Instance candidate = instanceLists[j].get(pos[j]);
          if (min == null || min.compareTo(candidate) > 0) {
            min = candidate;
            cur = new Pair<Integer, Integer>(j, pos[j]);
          }
        }
      }
      ++pos[cur.getKey()];
      sortedInstanceIndex[i] = cur;
    }
    return sortedInstanceIndex;
  }

  /**
   * @param doc
   * @param candidates
   * @param lastI
   * @param curI
   * @param instances
   * @param result
   */
  private void generate(Document doc, List<Pair<Integer, Instance>> candidates, int lastI, int curI,
      Instance[] instances, List<Instance> result) {
    Instance lastInstance = candidates.get(lastI).getValue();
    if (ArrayUtil.indexOf(instances, null) == -1 || curI == candidates.size() && isOr) {
      int left = lastInstance.getOffset();
      int right = lastInstance.getOffset() + lastInstance.getLength() - 1;
      for (Instance instance : instances) {
        if (instance != null) {
          left = Math.min(left, instance.getOffset());
          right = Math.max(right, instance.getOffset() + instance.getLength() - 1);
        }
      }
      result.add(new MConceptRuleInstance(this, left, right - left + 1, instances.clone()));
      return;
    }
    // candidates are used up
    if (curI == candidates.size()) {
      return;
    }
    Instance curInstance = candidates.get(curI).getValue();
    int off = lastInstance.getOffset();
    int end = curInstance.getOffset() + curInstance.getLength() - 1;
    boolean pass = true;
    // already exists in instances
    if (instances[candidates.get(curI).getKey()] != null) {
      pass = false;
    }
    // intersection
    else if (lastInstance.getOffset() < curInstance.getOffset() + curInstance.getLength()) {
      pass = false;
    }
    // disorder
    else if (hasOrd && candidates.get(curI).getKey() >= candidates.get(lastI).getKey()) {
      pass = false;
    }
    // too far away
    else if (doc.getWordPos(off) - doc.getWordPos(end) - 1 > dist) {
      pass = false;
    }
    if (pass) {
      instances[candidates.get(curI).getKey()] = curInstance;
      generate(doc, candidates, curI, curI + 1, instances, result);
      instances[candidates.get(curI).getKey()] = null;
    }
    generate(doc, candidates, lastI, curI + 1, instances, result);
  }

  /**
   * @param depInstances
   * @param doc
   * @return
   */
  private ImmutableList<Instance> generate(ImmutableList<Instance>[] depInstances, Document doc) {
    List<Instance> result = new ArrayList<Instance>();
    // Sort the depInstances
    // Enumerate each instance, take it as the rightest part of this rule's instance,
    // and check the remaining parts' instances.
    // If the check is passed, a new instance of this rule is generated and added to result.
    Pair<Integer, Integer>[] sortedInstanceIndex = getSortedInstanceIndex(depInstances);
    int[] sentIds = new int[sortedInstanceIndex.length];
    for (int i = 0; i < sortedInstanceIndex.length; ++i) {
      Pair<Integer, Integer> cur = sortedInstanceIndex[i];
      Instance curInstance = depInstances[cur.getKey()].get(cur.getValue());
      sentIds[i] = doc.getSentencePos(curInstance.getOffset());
      // gather all candidates that pass the initial dist and sent check.
      List<Pair<Integer, Instance>> candidates = new ArrayList<Pair<Integer, Instance>>();
      candidates.add(new Pair<Integer, Instance>(cur.getKey(), curInstance));
      for (int j = i - 1; j >= 0; --j) {
        if (hasSent && sentIds[j] != sentIds[i]) {
          break;
        }
        Pair<Integer, Integer> next = sortedInstanceIndex[j];
        Instance nextInstance = depInstances[next.getKey()].get(next.getValue());
        int off = curInstance.getOffset();
        int end = nextInstance.getOffset() + nextInstance.getLength() - 1;
        if (doc.getWordPos(off) - doc.getWordPos(end) - 1 > dist) {
          break;
        }
        cur = next;
        curInstance = nextInstance;
        candidates.add(new Pair<Integer, Instance>(cur.getKey(), curInstance));
      }
      Instance[] instances = new Instance[dependencies.length];
      instances[candidates.get(0).getKey()] = candidates.get(0).getValue();
      generate(doc, candidates, 0, 1, instances, result);
    }
    Collections.sort(result);
    return ImmutableList.copyOf(result);
  }

  @Override public ImmutableList<Instance> match(InstanceSet instanceSet, Document doc) {
    return generate(getDependentInstances(instanceSet), doc);
  }


  /**
   * A parameter consists of fullName and the path along the dependencies.
   * Temporary rules do not have parameters.
   */
  public static class Parameter {
    protected final String name;
    protected final int[] path;

    public Parameter(String name, int[] path) {
      this.name = name;
      this.path = path;
    }

    public String getName() {
      return name;
    }

    public int[] getPath() {
      return path;
    }

    @Override public String toString() {
      return name + ":" + Ints.join("->", path);
    }
  }

  /**
   * ruleName(p1, p2, ...,)
   *
   * @param fullName
   * @return
   */
  public static String parseFullName(String fullName) {
    int p = fullName.indexOf("(");
    String result = (p < 0 ? fullName : fullName.substring(0, p)).trim();
    if (fullName.isEmpty()) {
      throw new IllegalArgumentException(String.format("Name can't be empty: %s", fullName));
    }
    return result;
  }

  public static String[] parseFullNameParameters(String fullName) {
    String trimName = fullName.trim();
    int st = trimName.indexOf("(");
    if (st != -1 && !trimName.endsWith(")")) {
      throw new IllegalArgumentException(
          String.format("Name should have form fullName(p1, p2, ...): %s", fullName));
    }
    if (st == -1) {
      return StringUtil.EmptyStringArray;
    }
    String[] parameterNames = trimName.substring(st + 1, trimName.length() - 1).split(",");
    for (int i = 0; i < parameterNames.length; ++i) {
      parameterNames[i] = parameterNames[i].trim();
      if (parameterNames[i].isEmpty()) {
        throw new IllegalArgumentException(String.format("Parameter can't be empty: %s", fullName));
      }
    }
    return parameterNames;
  }

  /**
   * Return a string in the form of: (op0+op1+...,dep0,dep1,...)
   *
   * @param operators
   * @param dependencies
   */
  public static String toString(List<Operator> operators, String[] dependencies) {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(Operator.toString(Operator.reduceOperators(operators)));
    for (String dependency : dependencies) {
      sb.append(",");
      // MConceptRule
      // TODO what if the content of rule looks like: "(...)"
      if (dependency.startsWith("(") && dependency.endsWith(")")) {
        sb.append(dependency);
      } else {
        sb.append("\"").append(dependency).append("\"");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  public String info() {
    StringBuilder sb = new StringBuilder(super.toString()).append("\n");
    sb.append(String.format("\tParameters: %s\n", ImmutableList.copyOf(parameters)));
    sb.append(String.format("\tOperators: %s\n", operators));
    sb.append(String.format("\tDependencies: %s", ImmutableList.copyOf(dependencies)));
    return sb.toString();
  }

  /**
   * RuleSet to store additional generated temporary rules (MConceptRule and Concept).
   * Where the last one is the outermost MConceptRule.
   *
   * @param fullName MCONCEPT_RULE fullName
   * @param content  MCONCEPT_RULE content
   */
  public static ImmutableList<MConceptRule> newInstance(String fullName, String content) {
    List<MConceptRule> rules = new ArrayList<MConceptRule>();
    fullName = fullName.trim();
    content = content.trim();
    // parse content and construct temporary MConceptRules
    Stack<Operator> operators = new Stack<Operator>();
    Stack<Integer> paths = new Stack<Integer>();
    Map<String, Queue<int[]>> parameterPaths = new TreeMap<String, Queue<int[]>>();
    List<String> dependencies = new ArrayList<String>();
    for (int s = 0, nthParameter = 0; s < content.length(); ++s) {
      char ch = content.charAt(s);
      if (Character.isSpaceChar(ch) || ch == ',') {
        continue;
      }
      if (ch == '(') {
        int e = content.indexOf(",", s);
        if (!operators.isEmpty()) {
          paths.add(nthParameter);
          nthParameter = 0;
        }
        try {
          operators.push(Operator.newInstance(content.substring(s + 1, e).trim()));
        } catch (IllegalArgumentException ex) {
          throw new IllegalArgumentException(String.format("Rule format error: %s", content));
        }
        s = e;
      } else if (ch == '"') {
        int e = content.indexOf('"', s + 1);
        String part = content.substring(s + 1, e);
        Matcher match = ParameterPattern.matcher(part);
        if (match.matches()) {
          String parameter = match.group(1);
          if (!parameterPaths.containsKey(parameter)) {
            parameterPaths.put(parameter, new ArrayDeque<int[]>());
          }
          int[] path = Arrays.copyOf(Ints.toArray(paths), paths.size() + 1);
          path[path.length - 1] = nthParameter;
          parameterPaths.get(parameter).add(path);
          dependencies.add(match.group(2));
        } else {
          dependencies.add(part);
        }
        ++nthParameter;
        s = e;
      } else if (ch == ')' && s != content.length() - 1) { // not the last ')'
        if (operators.size() < 2) {
          throw new IllegalArgumentException(String.format("Rule format error: %s", content));
        }
        // Remove dependencies and build MConceptRule here
        String[] currentDep = new String[nthParameter];
        while (nthParameter-- > 0) {
          currentDep[nthParameter] = dependencies.remove(dependencies.size() - 1);
        }
        MConceptRule rule = new MConceptRule(operators, currentDep);
        rules.add(rule);
        dependencies.add(rule.getFullName());
        nthParameter = paths.pop() + 1;
        operators.pop();
      }
    }
    if (operators.size() != 1) {
      throw new IllegalArgumentException(String.format("Rule format error: %s", content));
    }
    // construct the outermost MConceptRule
    String[] parsedParameterNames = parseFullNameParameters(fullName);
    Parameter[] parameters = new Parameter[parsedParameterNames.length];
    for (int i = 0; i < parsedParameterNames.length; ++i) {
      if (!parameterPaths.containsKey(parsedParameterNames[i]) || parameterPaths
          .get(parsedParameterNames[i]).isEmpty()) {
        throw new IllegalArgumentException(
            String.format("Parameters not match: %s %s", fullName, content));
      }
      parameters[i] = new Parameter(parsedParameterNames[i],
          parameterPaths.get(parsedParameterNames[i]).poll());
    }
    rules.add(new MConceptRule(fullName, content, parameters, operators,
        dependencies.toArray(new String[dependencies.size()])));
    return ImmutableList.copyOf(rules);
  }

  /**
   * Add temporary rules to rulSet and return the outermost rule.
   *
   * @param fullName
   * @param content
   * @param ruleSet
   */
  public static MConceptRule newInstance(String fullName, String content, RuleSet ruleSet) {
    ImmutableList<MConceptRule> rules = newInstance(fullName, content);
    for (int i = 0; i < rules.size() - 1; ++i) {
      ruleSet.add(rules.get(i));
    }
    return rules.get(rules.size() - 1);
  }
}
