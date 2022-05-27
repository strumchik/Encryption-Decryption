package encryptdecrypt;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

class algorithmFactory {
    public static Algorithm makeAlgorithm(String algType, String[] args) {
        switch (algType) {
            case "shift":
                return new shiftAlgorithm(args);
            case "unicode":
                return new unicodeAlgorithm(args);
            default:
                return new shiftAlgorithm(args);
        }
    }
}

abstract class Algorithm {
    int key;
    String data;
    String mode;
    String inPath;
    String outPath;
    String resultData;

    public Algorithm(String[] args) {
        List<String> argsList = Arrays.asList(args);
        this.mode = argsList.contains("-mode") ? argsList.get(argsList.indexOf("-mode") + 1) : "enc";
        this.key = argsList.contains("-key") ? Integer.parseInt(argsList.get(argsList.indexOf("-key") + 1)) : 0;
        this.inPath = argsList.contains("-in") ? argsList.get(argsList.indexOf("-in") + 1) : "";
        this.outPath = argsList.contains("-out") ? argsList.get(argsList.indexOf("-out") + 1) : "";
        this.data = argsList.contains("-data") ? argsList.get(argsList.indexOf("-data") + 1) : "";
        if ("".equals(this.data)) {
            try {
                this.data = new String(Files.readAllBytes(Paths.get(inPath)));
            } catch (IOException e) {
                System.out.println("ERROR: " + e);
            }
        }
    }

    public abstract String encode();

    public abstract String decode();

    public void printToOutput() {
        if ("".equals(outPath)) {
            System.out.println(resultData);
        } else {
            File file = new File(outPath);
            try (FileWriter writer = new FileWriter(file)) {
                writer.append(resultData);
            } catch (IOException e) {
                System.out.println("ERROR: " + e);
            }
        }
    }

    public void run() {
        if ("dec".equals(mode)) {
            resultData = decode();
        } else {
            resultData = encode();
        }
    }
}

class shiftAlgorithm extends Algorithm {
    public shiftAlgorithm(String[] args) {
        super(args);
    }

    @Override
    public String encode() {
        StringBuilder encryptedMessage = new StringBuilder();
        char a = 'a';
        char z = 'z';
        char aUp = 'A';
        char zUp = 'Z';
        int alphabetSize = 26;
        while (key < 0) {
            key += alphabetSize;
        }
        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) >= a && data.charAt(i) <= z) {
                encryptedMessage.append((char) (((data.charAt(i) - a + key) % alphabetSize) + a));
            } else if (data.charAt(i) >= aUp && data.charAt(i) <= zUp) {
                encryptedMessage.append((char) (((data.charAt(i) - aUp + key) % alphabetSize) + aUp));
            } else
                encryptedMessage.append(data.charAt(i));
        }
        return encryptedMessage.toString();
    }

    @Override
    public String decode() {
        key = -key;
        return encode();
    }
}

class unicodeAlgorithm extends Algorithm {
    public unicodeAlgorithm(String[] args) {
        super(args);
    }

    @Override
    public String encode() {
        StringBuilder encryptedMessage = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            int code = data.charAt(i) + key;
            encryptedMessage.append((char) code);
        }
        return encryptedMessage.toString();
    }

    @Override
    public String decode() {
        key = -key;
        return encode();
    }
}

public class Main {
    public static void main(String[] args) {
        int i = Arrays.asList(args).indexOf("-alg");
        String algType = i < 0 ? "" : args[i + 1];
        final Algorithm algorithm = algorithmFactory.makeAlgorithm(algType, args);
        algorithm.run();
        algorithm.printToOutput();
    }
}