import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class SystemCalls {
    private Pair<String, Object>[] memory;

    public SystemCalls(Pair<String, Object>[] memory){
        this.memory = memory;
    }

    public Object takeInput(){
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

    public Object readFromMem(String varName, Process process){
        for (int i = process.getStartBoundary(); i < process.getEndBoundary(); i++) {
            if (memory[i] == null) continue;
            if (memory[i].getKey().equals(varName))
                return memory[i].getValue();
        }
        return null;
    }

    public void writeToMem(String varName, Object value, Process process){
        Pair<String, Object> pair = new Pair<>(varName, value);
        for (int i = process.getStartBoundary(); i <= process.getEndBoundary(); i++) {
            if (memory[i].getKey() != null && memory[i].getValue() != null) continue;
            memory[i] = pair;
            return;
        }
    }

    public void printData(Object data){
        System.out.println(data.toString());
    }

    public String readFromDisk(String fileName) throws IOException {

        Path path = Paths.get(fileName);
        return Files.readString(path);
    }

    public void writeToDisk(String fileName, Object data) throws IOException{
        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName, true));
        pw.append(data.toString());
        pw.append("\n");
        pw.close();

    }
}
