import java.io.*;
import java.util.*;

public class Parser {

    String fileName;
    Object[] memory;

    public Parser() {

    }

    public Parser(String fileName, Object[] memory) {

        this.fileName = fileName;
        this.memory = memory;
    }

    public void parse(String fileName) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;

        while ((line = br.readLine()) != null) {
            System.out.print("[");
            System.out.print(line);
            System.out.print("]");
            System.out.println();
        }
    }



    public static void main(String[] args) throws IOException {
//        execute("semWait printer");
//        PrintWriter pw = new PrintWriter(new FileOutputStream("toBeWritten.txt", true));
//        pw.append("hello");
//        pw.close();
    }


}
