import ict.ada.drawline.service.SimpleService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * Drawline demo
 *
 * @author Chenbo
 */
public class DrawlineDemo {

  public static void main(String[] args) throws Exception {
    String text = new String(Files.readAllBytes(Paths.get("test.txt")));

    Map<String, Map<String, List<String>>> person_map = SimpleService.extract(text, "test.cfg");

    System.out.println(person_map);
  }

}
