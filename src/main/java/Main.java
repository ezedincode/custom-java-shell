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
            }else {
                System.out.println(command + ": command not found");
            }
        }

    }
}
