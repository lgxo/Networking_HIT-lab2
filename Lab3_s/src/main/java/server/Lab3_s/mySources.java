package server.Lab3_s;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class mySources {

  public static final String story = "Once there were two mice." +
    "They were friends.One mouse lived in the country;the other mouse lived in the city." +
    "After many years the Country mouse saw the City mouse;he said," +
    "\"Do come and see me at my house in the country.\"So the City mouse went.The City mouse said," +
    "\"This food is not good,and your house is not good.Why do you live in a hole in the field?" +
    "You should come and live in the city.You would live in a nice house made of stone." +
    "You would have nice food to eat.You must come and see me at my house in the city.\"";

  public static String readFile(String path){
    StringBuilder strBuilder = new StringBuilder();
    try {
      Scanner in = new Scanner(Paths.get(path), "UTF-8");
      while (in.hasNextLine()){
        strBuilder.append(in.nextLine() + "\n");
      }
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return strBuilder.toString();
  }

}
