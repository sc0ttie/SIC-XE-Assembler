package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Malthael
 */
public class Assembler {
    private int _locctr;
//    private Map<String, Operation> _opTab;
    private Map<String, String> _symTab;
    
    public Assembler() {
        
    }
    
    public boolean assemble(String input, String output) throws FileNotFoundException {
        return assemble(new File(input), new File(output));
    }
    
    public boolean assemble(File input, File output) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(input)) {
            while (scanner.hasNext()) {
                String buf = scanner.nextLine().trim();
                
                for (String s : buf.split("\t")) {
                    if (s.length() > 0) {
                        System.out.print("[" + s.trim() + "]");
                    }
                }
                System.out.println();
            }
        }
        
        return true;
    }
    
    public static void main(String[] args) {
        Assembler asm = new Assembler();
        
        try {
            asm.assemble("copy.asm", "copy.o");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}
