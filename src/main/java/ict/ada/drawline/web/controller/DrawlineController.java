package ict.ada.drawline.web.controller;

import com.google.gson.*;
import ict.ada.drawline.Document;
import ict.ada.drawline.Drawline;
import ict.ada.drawline.RuleParser;
import ict.ada.drawline.result.Instance;
import ict.ada.drawline.result.MConceptRuleInstance;
import ict.ada.drawline.result.ParameterValue;
import ict.ada.drawline.rule.Rule;
import ict.ada.drawline.rule.RuleSet;
import ict.ada.drawline.service.SimpleService;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class DrawlineController {
    /**
     * 首页测试
     *
     * @param body
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String index(@RequestBody Map<String, Object> body) throws Exception {
        String ruleId = val(body, "rule", "test.cfg");
        String text = val(body, "text", "");
        JsonObject root = new JsonObject();

        if(text.isEmpty()){
            root.addProperty("code", 40000);
            return root.toString();
        }

        Map<String, Map<String, List<String>>> person_map = SimpleService.extract(text, "test.cfg");

        Gson gson = new GsonBuilder().create();
        JsonElement data = gson.toJsonTree(person_map);

        root.addProperty("code", 20000);
        root.add("data", data);
        return root.toString();
    }

    private String val(Map<String, Object> map, String key, String defValue){
        Object value = map.get(key);
        if(value == null){
            return defValue;
        } else {
            return String.valueOf(value);
        }
    }
}