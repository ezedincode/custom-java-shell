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
                    execute(input);
                    break;
            }

        }
    }

    private static void commandNotFound(String command) {
        System.out.println(command + ": command not found");
    }

//    public static void echo(String input) {
//        String [] parts;
//        String[] loopParts;
//        int i=1;
//        input = input.substring(5);
//        String finalPart="";
//        if(input.contains("'")){
//            parts = input.trim().split("'");
//            for (String part : parts) {
//                if(i % 2 != 0) {
//                    if(part.isEmpty()){
//                        continue;
//                    }
//                    else {
//                        boolean space = part.charAt(0) == ' ';
//                        System.out.println(space);
//                        loopParts = part.trim().split("\\s+");
//                        System.out.println(Arrays.toString(loopParts));
//                        if (loopParts.length > 1) {
//                            for (String loopPart : loopParts) {
//                                if (space) {
//                                    finalPart = finalPart.concat(" ");
//                                    space = false;
//                                }
//                                finalPart = finalPart.concat(loopPart).concat(" ");
//                            }
//                            finalPart = finalPart.trim();
//                        }else{
//                            finalPart = finalPart.concat(part);
//                        }
//
//                    }
//                }else{
//                    finalPart =finalPart.concat(part);
//                }
//                i++;
//            }
//        }else {
//            parts = input.trim().split("\\s+");
//            for (String part : parts) {
//                finalPart = finalPart.concat(part).concat(" ");
//            }
//            finalPart = finalPart.trim();
//        }
//
//        System.out.println(finalPart.trim());
//    }
public static void echo(String input) {

    input = input.substring(5); // remove "echo "

    StringBuilder out = new StringBuilder();
    boolean inQuotes = false;
    boolean lastWasSpace = false;

    for (int c = 0; c < input.length(); c++) {
        char ch = input.charAt(c);

        if (ch == '\'') {
            inQuotes = !inQuotes;
            continue; // remove quotes
        }

        if (Character.isWhitespace(ch)) {
            if (inQuotes) {
                // inside quotes → keep spaces exactly
                out.append(ch);
            } else {
                // outside quotes → compress to single space
                if (!lastWasSpace) {
                    out.append(' ');
                }
            }
            lastWasSpace = true;
        } else {
            out.append(ch);
            lastWasSpace = false;
        }
    }

    System.out.println(out.toString().trim());
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
    public static void execute(String args) throws IOException {
        String [] result = args.split(" ");
        String results = pathFinder(result[0]);
        if (results.contains(": not found")) {
            commandNotFound(result[0]);
            return;
        }
        List<String> argResult = parseCommand(args);
//        String[] temp = result.split("/");
//        String path = temp[temp.length - 1];
        //combining executable path with the arguments
        //        commands.add(path);
//        List<String> commands = new ArrayList<>(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(argResult);
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
            File homePath;
            try {
                 homePath = new File(System.getenv("HOME"));
            }catch (Exception e){
                homePath = new File(System.getProperty("user.home"));
            }
            ;
            //window use System.getProperty("user.home")
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
    public static List<String> parseCommand(String line) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '\'') {
                inQuotes = !inQuotes;
                continue;
            }

            if (Character.isWhitespace(ch) && !inQuotes) {
                if (!current.isEmpty()) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(ch);
            }
        }

        if (!current.isEmpty()) {
            args.add(current.toString());
        }

        return args;
    }


}
