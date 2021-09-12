package ict.ada.drawline.service;

import ict.ada.drawline.Document;
import ict.ada.drawline.Drawline;
import ict.ada.drawline.RuleParser;
import ict.ada.drawline.result.Instance;
import ict.ada.drawline.result.MConceptRuleInstance;
import ict.ada.drawline.result.ParameterValue;
import ict.ada.drawline.rule.Rule;
import ict.ada.drawline.rule.RuleSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleService {

    private static final String[] basic_rules = {"rules/basic_cn.cfg"};

    /**
     * 生成规则集
     * @param rule_file 可能为空
     * @return
     * @throws IOException
     */
    public static RuleSet mkRuleSet(String rule_file) throws IOException {
        RuleSet ruleSet = new RuleSet();
        if(rule_file!=null) {
            ruleSet = RuleParser.parseFile(rule_file);
        }
        // Or construct RuleSet manually:
        // ruleSet.add(Concept.newInstance("NAME", "姜周"));

        // 叠加基础规则
        for (String basic : basic_rules) {
            RuleSet ruleSetBasic = RuleParser.parseFile(basic);
            ruleSet.add(ruleSetBasic);
        }

        return ruleSet;
    }

    public static Map<String, Map<String, List<String>>> extract(String text, String customRule) throws IOException {
        Document doc = new Document(text);

        //规则集合
        RuleSet ruleSet = mkRuleSet(customRule);
        //构造Drawline实例
        Drawline drawline = new Drawline(ruleSet);

        Map<String, Map<String, List<String>>> person_map = new HashMap<>();

        //获取匹配结果
        for (Instance instance : drawline.match(doc)) {
            Rule rule = instance.getRule();
            String predicate = rule.getName();

            if (instance instanceof MConceptRuleInstance) {
                MConceptRuleInstance mConceptRuleInstance = (MConceptRuleInstance) instance;
                ParameterValue[] values = mConceptRuleInstance.getParameterValues();

                String subject = values[0].getInstance().getText(doc.getText());
                String object = values[1].getInstance().getText(doc.getText());

                if (values.length > 2) {
                    predicate = predicate + ":" + values[2].getInstance().getText(doc.getText());
                }

                if (person_map.containsKey(subject)) {
                    Map<String, List<String>> map = person_map.get(subject);
                    if (map.containsKey(predicate)) {
                        map.get(predicate).add(object);
                    } else {
                        map.put(predicate, asList(object));
                    }
                } else {
                    Map<String, List<String>> map = new HashMap<>();
                    map.put(predicate, asList(object));
                    person_map.put(subject, map);
                }

//        System.out.println(subject+"-["+predicate+"]->" + object);
            } else {
                // 抽取获得单一实体、概念或其他
                // String value = instance.getText(doc.getText());
                // System.out.println(predicate+": "+value);
            }
        }

        return person_map;
    }

    /**
     * 生成可变列表 注意：不不能用Arrays.asList() 因为返回的是不可变List
     * @param value
     * @return
     */
    private static List<String> asList(String value){
        List<String> list = new ArrayList<>();
        list.add(value);
        return list;
    }
}
