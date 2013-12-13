package assembler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Malthael
 */
public class Assembler {
    private int _locctr;
    private int _startAddr;
    private String _progLen;
    private String _base;
    private final Map<String, Operation> _opTab;
    
    public Assembler(File validOp) throws FileNotFoundException {
        _opTab = new HashMap<>();
        
        try (Scanner scanner = new Scanner(validOp)) {
            while (scanner.hasNext()) {
                String[] buf = scanner.nextLine().split(" ");
                
                _opTab.put(buf[0], new Operation(buf[0], buf[1], buf[2]));
            }
        }
    }
    
    public boolean assemble(File input, File output) throws IOException, DuplicateSymbolException, InvalidOperationCodeException, ClassNotFoundException {
        File intermediateFile = new File("passone.tmp");

        intermediateFile.createNewFile();

        Map opTab = passOne(input, intermediateFile);

        passTwo(intermediateFile, opTab);

        intermediateFile.delete();
        
        return true;
    }
    
    private Map passOne(File input, File output) throws IOException, DuplicateSymbolException, InvalidOperationCodeException {
        try (Scanner scanner = new Scanner(input);
             FileOutputStream ostream = new FileOutputStream(output);
             ObjectOutputStream objOutputStream = new ObjectOutputStream(ostream);) {
            
            Map<String, String> symTab = new HashMap<>();
            Statement statement = Statement.parse(scanner.nextLine());
            
            if (statement.operation().compareTo("START") == 0) {
                _startAddr = Integer.parseInt(statement.operand1());
            } else {
                _startAddr = 0;
            }
            
            _locctr = _startAddr;
            
            while (scanner.hasNext()) {
                statement = Statement.parse(scanner.nextLine());
                
                statement.setLoc(_locctr);
//                Uncomment the next line can show the Loc and Source statements
//                System.out.println(Integer.toHexString(_locctr).toUpperCase() + "\t" + statement);
            
                if (statement.operation().compareTo("END") != 0) {
                    if (statement.isComment() == false) {
                        if (statement.label() != null) {
                            if (symTab.containsKey(statement.label())) {
                                throw new DuplicateSymbolException(statement);
                            } else {
                                symTab.put(statement.label(), Integer.toHexString(_locctr).toUpperCase());
                            }
                        }
                        
                        if (_opTab.containsKey(statement.operation())) {
                            int format = _opTab.get(statement.operation()).format();
                            
                            switch (format) {
                                case 1:
                                case 2:
                                    _locctr += format;
                                    break;
                                case 3:
                                    _locctr += 3 + (statement.isExtended() ? 1 : 0);
                                    break;
                            }
                        } else if (statement.operation().compareTo("WORD") == 0) {
                            _locctr += 3;
                        } else if (statement.operation().compareTo("RESW") == 0) {
                            _locctr += 3 * Integer.parseInt(statement.operand1());
                        } else if (statement.operation().compareTo("RESB") == 0) {
                            _locctr += Integer.parseInt(statement.operand1());
                        } else if (statement.operation().compareTo("BYTE") == 0) {
                            String s = statement.operand1();
                            
                            switch (s.charAt(0)) {
                                case 'C':
                                    _locctr += (s.length() - 3); // C'EOF' -> EOF -> 3 bytes
                                    break;
                                case 'X':
                                    _locctr += (s.length() - 3) / 2; // X'05' -> 05 -> 2 half bytes
                                    break;
                            }
                        } else if (statement.operation().compareTo("BASE") == 0) {
                            _base = statement.operand1();
                        } else {
                            throw new InvalidOperationCodeException(statement);
                        }
                    }
                }
                
                objOutputStream.writeObject(statement);
            }
            
            _progLen = Integer.toHexString(_locctr - _startAddr).toUpperCase();
            
            return symTab;
        }
    }
    
    private void passTwo(File input, Map symTab) throws IOException, ClassNotFoundException {
        try (FileInputStream istream = new FileInputStream(input);
             ObjectInputStream objInputStream = new ObjectInputStream(istream)) {
            
            while (istream.available() > 0) {
                Statement statement = (Statement) objInputStream.readObject();
                
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            Assembler asm = new Assembler(new File("ValidOperations.txt"));
            
            asm.assemble(new File("copy.asm"), new File("copy.o"));
        } catch (DuplicateSymbolException | InvalidOperationCodeException e) {
            System.out.println(e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
