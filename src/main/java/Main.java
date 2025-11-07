import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        while(true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            if(command.equals("exit 0")) {
                break;
            } else if (command.split(" ")[0].equals("echo")) {
                System.out.println(command.substring(5));
            } else if (command.split(" ")[0].equals("type"))
            {
                if(command.substring(5).equals("echo")
                        || command.substring(5).equals("exit")
                        || command.substring(5).equals("type")){
                System.out.println(command.substring(5) + " is a shell builtin");}
                else {
                    System.out.println(command.substring(5) + ": not found");
                }
            } else {
                System.out.println(command + ": command not found");
            }
        }

    }
}
