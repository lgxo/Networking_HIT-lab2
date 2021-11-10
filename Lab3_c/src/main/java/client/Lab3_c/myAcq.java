package client.Lab3_c;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class myAcq {
  public static final String acq = "story";

  public static void writeFile(String path, String data){
    try {
      PrintWriter out = new PrintWriter(path, "UTF-8");
      out.println(data);
      out.close();
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }

  }

}
