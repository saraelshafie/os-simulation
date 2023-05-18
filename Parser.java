import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {

    String fileName;
    Object [] memory;

    public Parser(String fileName, Object[] memory){

        this.fileName = fileName;
        this.memory = memory;
    }

    public void parse() throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;

        while((line =  br.readLine()) != null){

            String[] arr = line.split(" ");
            String ins = arr[0];

            if(ins.equals("semWait")){

            }





        }
    }


}
