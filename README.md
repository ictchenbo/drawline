# Intro
`drawline` is an rule based information extractioner which supports 4 types of rules:
* **Concept**, do an exact text matching
* **RegexConcept**, match using regex
* **MConcept**, concatenates rules to match
* **MConceptRule**, use an operator (one of And, Dist, Or, Ord, Sent) and operands (can be any kind of the rules or words) to do matching

# Template File Grammar
Each line (except comment or blank lines) in template file is a rule which contains 3 parts:
```
identifier:fullName:content
```
where:
* **identifier** specifies the type of a rule, it can be `CONCEPT`, `REGEX`, `MCONCEPT`, `MCONCEPT_RULE`
* **fullName** specifies the name and parameters (if has) of a rule
* **content** specifies the rule content

## Example:
```
CONCEPT:NAME:张三
CONCEPT:NAME:李四
REGEX:AGE_DIGIT:\d{1,3}
MCONCEPT:AGE:AG_DIGIT岁
CONCEPT:SOCIALRELATION:学生
MCONCEPT_RULE:NAME_NAME_RELATION(person, person, social):(ORD, (SENT, (DIST_2, "_person{NAME}", "的", "_social{SOCIALRELATION}", "_person{NAME}")))
MCONCEPT_RULE:NAME_AGE(person, age):(DIST_2, "_person{NAME}", "_age{AGE}")
```

# Usage
For more detail, please refer to src/java/DrawlineDemo.java.
```
RuleSet ruleSet = RuleParser.parseFile("...");
// Or construct RuleSet manually:
// ruleSet.add(Concept.newInstance("NAME", "张三")); ...
Drawline drawline = new Drawline(ruleSet);
Document doc = new Document("张三的学生李四今年40岁");
for (Instance instance : drawline.match(doc)) {
  // play with instance's getXXX method, such as
  // offset, length, text, parameterValues (if MConceptRuleInstance)
}
```
Note, offset and length in instance is in character metric. To convert to byte offset, use `ict.ada.drawline.util.ByteCalculator`.


## Core Classes
* `Rule` is an abstract class defining members and methods a rule should have. It has 4 concrete sub-classes: `Concept`, `RegexConcept`, `MConcept`, `MConceptRule`.
* `RuleSet` accepts a list of rules then builds a `dependency graph` of them.
* `Drawline` accepts a `RuleSet` and calls each rule to match a document. The rules are called in a topological order of the `dependency graph`.
* `Instance` stores occurrence's **character** offset and length in the document.
* `MConceptRuleInstance` is a special kind of `Instance` which also stores parameters. This instance is generated from `MConceptRule`.

# Implementation Details
* `Aho-Corasick Automata` is used to find occurrences of all the Concepts in one traversal.
* To make the match method of MConcept & MConceptRule effective, they are implemented as a **generating** procedure, not a try and match procedure. Specifically, inside a rule's match method, it fetches enough instances of its dependencies from `InstanceSet` and then do a combination to generate new instances of this rule.
* `InstanceSet` stores sorted, immutable instance sets for each rule. Additionally, it stores a instance set by each rule's name for future queries.
