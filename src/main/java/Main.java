import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
                case "pwd":
                    pwd();
                    break;
                case "cd":
                    cd(command);
                    break;
                default:
                    execute(command);
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
        String[] validCommands = {"echo", "type", "exit", "pwd", "cd"};

        if (search(validCommands, input[1])) {
            System.out.println(input[1] + " is a shell builtin");
        } else {
            String result = pathFinder(input[1]);
            System.out.println(result);
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

    public static String pathFinder(String input) {
        String paths = System.getenv("PATH");
        if (paths == null) {
            return input + ": not found";

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

        return result.map(path -> path.toAbsolutePath().toString()).orElseGet(() -> input + ": not found");
    }

    //for executing executable file
    public static void execute(String[] args) throws IOException {
        String result = pathFinder(args[0]);
        if (result.contains(": not found")) {
            commandNotFound(args[0]);
            return;
        }
        String[] temp = result.split("/");
        String path = temp[temp.length - 1];
        //combining executable path with the arguments
        //        commands.add(path);
        List<String> commands = new ArrayList<>(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        p.destroy();
    }

    public static void pwd() {
        System.out.println(System.getProperty("user.dir"));
    }

    public static void cd(String[] input) throws IOException {
        if(input[1].equals("~")){
            //for unix
            File homePath = new File(System.getenv("HOME"));
            //window use System.getProperty("user.home")
            if(homePath == null){
                homePath = new File(System.getProperty("user.home"));
            }
            System.setProperty("user.dir", homePath.getCanonicalPath());
            return;
        }


        String path = input[1];
        if (input[1].equals("./")) {
            return;

        }
        if (input[1].startsWith("../")) {
            String[] commands = input[1].split("/");
            boolean containIllegalCommand = false;
            for (String command : commands) {
                containIllegalCommand = !command.equals("..");
                if (containIllegalCommand) {
                    break;

                }

            }
            if (!containIllegalCommand) {
                File currentDir = new File(System.getProperty("user.dir"));
                for (String command : commands) {
                    if (command.equals("..") && currentDir.getParentFile() != null) {
                        currentDir = currentDir.getParentFile();
                    }
                }
                System.setProperty("user.dir", currentDir.getCanonicalPath());
                return;
            } else {
                path = input[1].substring(2);
            }




        }
       String completePath = System.getProperty("user.dir");

        File dir;
        if(path.startsWith("./")){
            path = path.substring(2);
            dir = new File(System.getProperty("user.dir"),path);
        } else if (path.startsWith("/")) {
            dir = new File(path);
            //for window os
            //because window take /path/path as a relative paths
            if(!dir.isAbsolute()){
                dir = new File(System.getProperty("user.dir"),path);
            }
        } else{
            dir = new File(System.getProperty("user.dir"),path);
        }

        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("cd: " + path + ": No such file or directory");
            return;
        }
        //i use getCanonicalPath() instead of getAbsolutePath() to handle paths start with ./
        System.setProperty("user.dir", dir.getCanonicalPath());
    }
}
