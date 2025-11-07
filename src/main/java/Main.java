import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {

        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] command = input.split(" ");
            switch (command[0]) {
                case "exit":
                    System.exit(Integer.parseInt(command[1]));
                    break;
                case "echo":
                    echo(input);
                    break;
                case "type":
                    type(command);
                    break;
                default:
                    commandNotFound(command[0]);
                    break;
            }

        }
    }

    private static void commandNotFound(String command) {
        System.out.println(command + ": command not found");
    }

    public static void echo(String input) {
        System.out.println(input.substring(5));
    }

    public static void type(String[] input) {
        String[] validCommands = {"echo", "type", "exit"};

        if (search(validCommands, input[1])) {
            System.out.println(input[1] + " is a shell builtin");
        } else {
            pathFinder(input[1]);
        }
    }

    public static boolean search(String[] input, String command) {
        for (String s : input) {
            if (s.equals(command)) {
                return true;
            }
        }
        return false;
    }

    public static void pathFinder(String input) {
        String paths = System.getenv("PATH");
        if (paths == null) {
            System.out.println(input + ": not found");
            return;
        }

        String[] pathArray = paths.split(File.pathSeparator);

        // Search through all PATH directories
        Optional<Path> result = Arrays.stream(pathArray)
                .map(Paths::get)
                .filter(Files::isDirectory)
                .flatMap(dir -> {
                    try {
                        return Files.list(dir); // List files in directory
                    } catch (IOException e) {
                        return Stream.empty();
                    }
                })
                .filter(Files::isRegularFile)
                .filter(Files::isExecutable)
                .filter(path -> path.getFileName().toString().equals(input))
                .findFirst();

        if (result.isPresent()) {
            System.out.println(input + " is " + result.get().toAbsolutePath());
        } else {
            System.out.println(input + ": not found");
        }
    }
}
