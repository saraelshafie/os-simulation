import java.util.ArrayList;
import java.util.Scanner;

public class Kernel {
    private Mutex fileAccess;
    private Mutex input;
    private  Mutex printer;

    private Scheduler scheduler;
    private Object [] memory;
    private ArrayList<Object> hardDisk;



    public Object readFromDisk(String filename){

        return null;
    }

    public void writeToDisk(String filename , Object data){

    }

    public void printData(Object data){
        System.out.println(data);
    }

    public Object takeInput(){
        System.out.println("Please enter a value: ");
        Scanner sc = new Scanner(System.in);
        return sc.next();  //check this de habda ;)
    }

    public Object readFromMemory(int pc){
        return memory[pc];
    }

    public void writeToMemory(Object data){
        //mesh aarfen :)))))
    }




}
