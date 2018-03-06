import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.codingame.gameengine.runner.GameRunner;

public class Main {
    public static void main(String[] args) {
        try {

            File file = new File("test.properties");
            FileInputStream fileInput = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();

            GameRunner gameRunner = new GameRunner(properties);
            gameRunner.addAgent(Player1.class);
            gameRunner.addAgent(Player2.class);
            gameRunner.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
