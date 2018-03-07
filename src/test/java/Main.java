import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.codingame.gameengine.runner.GameRunner;


public class Main {

    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, IOException {
        File file = new File("test.properties");
        FileInputStream fileInput = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fileInput);
        fileInput.close();

        // Extract the player properties from the list
        List<String> players = new ArrayList<String>();
        for (String s : properties.stringPropertyNames()) {
            if (s.startsWith("player")) {
                players.add(properties.getProperty(s));
                properties.remove(s);
            }
        }

        GameRunner gameRunner = new GameRunner(properties);

        for (String s : players) {
            String[] params = s.split(",");
            String type = params[0];
            String declaration = params[1];
            switch (type) {
                case "java":
                    gameRunner.addAgent(Class.forName(declaration));
                    break;
                default:
                    gameRunner.addAgent(declaration);
                    break;
            }
        }

        gameRunner.start();
    }
}
