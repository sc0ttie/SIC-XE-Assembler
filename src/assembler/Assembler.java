package assembler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Assembler {
    private int _locctr;
    private int _startAddress;
    private int _programLength;
    private int _baseAddress;
    private final Map<String, Operation> _opTable;
    private List<Map<String, Literal>> _literalPools;
    
    public Assembler(File validOp) throws FileNotFoundException {
        _opTable = new HashMap<>();
        
        try (Scanner scanner = new Scanner(validOp)) {
            while (scanner.hasNext()) {
                String[] buf = scanner.nextLine().split(" ");
                
                _opTable.put(buf[0], new Operation(buf[0], buf[1], buf[2]));
            }
        }
        
        _literalPools = new ArrayList<>();
    }
    
    public boolean assemble(File input, File output) throws IOException, ClassNotFoundException, ExpectedDirectiveNotFoundException {
        File intermediateFile = new File(".assembler.tmp");

        intermediateFile.createNewFile();

        Map<String, Integer> symbolTable = passOne(input, intermediateFile);

        passTwo(symbolTable, intermediateFile, output);

        intermediateFile.delete();
        
        return true;
    }
    
    private Map<String, Integer> passOne(File input, File output) throws IOException, ExpectedDirectiveNotFoundException {
        try (Scanner scanner = new Scanner(input);
             FileOutputStream ostream = new FileOutputStream(output);
             ObjectOutputStream objOutputStream = new ObjectOutputStream(ostream);) {
            
            Map<String, Integer> symTab = new HashMap<>();
            
            // put REGISTERs into the symbol table
            symTab.put(null, 0);
            symTab.put("A", 0);
            symTab.put("X", 1);
            symTab.put("L", 2);
            symTab.put("B", 3);
            symTab.put("S", 4);
            symTab.put("T", 5);
            symTab.put("F", 6);
            symTab.put("SW", 9);
            
            Map<String, Literal> literalPool = new HashMap<>();
            Statement statement = Statement.parse(scanner.nextLine());
            objOutputStream.writeObject(statement);
            
            if (statement.compareTo("START") == 0) {
                _startAddress = Integer.parseInt(statement.operand1());
            } else {
                throw new ExpectedDirectiveNotFoundException("The directive START not found.");
            }
            
            _locctr = _startAddress;
            
            while (scanner.hasNext()) {
                statement = Statement.parse(scanner.nextLine());
                
                statement.setLocation(_locctr);
//                Uncomment the next line can show the Loc and Source statements
//                System.out.println(statement);
            
                try {
                    if (statement.compareTo("END") != 0) {
                        if (statement.isComment()) {
                            continue;
                        }
                        
                        if (statement.label() != null) {
                            if (symTab.containsKey(statement.label())) {
                                throw new DuplicateSymbolException(statement);
                            } else {
                                symTab.put(statement.label(), _locctr);
                            }
                        }

                        if (_opTable.containsKey(statement.operation())) {
                            switch (_opTable.get(statement.operation()).format()) {
                                case "1":
                                    _locctr += 1;
                                    break;
                                case "2":
                                    _locctr += 2;
                                    break;
                                case "3/4":
                                    _locctr += 3 + (statement.isExtended() ? 1 : 0);
                                    break;
                            }
                        } else if (statement.compareTo("WORD") == 0) {
                            _locctr += 3;
                        } else if (statement.compareTo("RESW") == 0) {
                            _locctr += 3 * Integer.parseInt(statement.operand1());
                        } else if (statement.compareTo("RESB") == 0) {
                            _locctr += Integer.parseInt(statement.operand1());
                        } else if (statement.compareTo("BYTE") == 0) {
                            String s = statement.operand1();

                            switch (s.charAt(0)) {
                                case 'C':
                                    _locctr += (s.length() - 3); // C'EOF' -> EOF -> 3 bytes
                                    break;
                                case 'X':
                                    _locctr += (s.length() - 3) / 2; // X'05' -> 05 -> 2 half bytes
                                    break;
                            }
                        } else if (statement.compareTo("BASE") == 0) {
                            // pass one do nothing to directive 'BASE'
                        } else if (statement.compareTo("EQU") == 0) {
                            // EQU
                        } else if (statement.compareTo("LTORG") == 0) {
                            // LTORG
                        } else {
                            throw new InvalidOperationCodeException(statement);
                        }

                        if (statement.hasLiteral()) {
                            Literal literal = Literal.parse(statement.operand1());
//System.out.println(literal.name());
                            literalPool.put(literal.name(), literal);
                        }
                    }
                } catch (DuplicateSymbolException | InvalidOperationCodeException e) {
                    System.out.println(e.getMessage());
                }
                
                objOutputStream.writeObject(statement);
                
                if (statement.compareTo("LTORG") == 0 || statement.compareTo("END") == 0) {
                    for (Literal literal : literalPool.values()) {
                        statement = literal.toStatement();
                        
                        literal.updateAddress(_locctr);
                        statement.setLocation(_locctr);
                        _locctr += literal.length();
                        
                        objOutputStream.writeObject(statement);
                    }
                    
                    _literalPools.add(literalPool);
                    literalPool = new HashMap<>();
                }
            }
            
            _programLength = _locctr - _startAddress;
            
            return symTab;
        }
    }
    
    private void passTwo(Map<String, Integer> symTab, File input, File output) throws IOException, ClassNotFoundException, ExpectedDirectiveNotFoundException {
        try (FileInputStream istream = new FileInputStream(input);
             ObjectInputStream objInputStream = new ObjectInputStream(istream);
             FileWriter objectProgram = new FileWriter(output)) {
            Statement statement = (Statement) objInputStream.readObject();
            
            if (statement.compareTo("START") == 0) {
                objectProgram.write(new HeaderRecord(statement.label(), _startAddress, _programLength).toObjectProgram() + '\n');
            } else {
                throw new ExpectedDirectiveNotFoundException("The directive START not found.");
            }
            
            List<Record> mRecords = new ArrayList<>();
            TextRecord textRecord = new TextRecord(_startAddress);
            Map<String, Literal> literalPool = _literalPools.remove(0);
            
            while (istream.available() > 0) {
                statement = (Statement) objInputStream.readObject();
                
                if (statement.isComment() == false) {
                    String objectCode = assembleInstruction(statement, symTab, literalPool);

                    // If it is format 4 and not immediate value
                    if (statement.isExtended() && symTab.containsKey(statement.operand1())) {
                        mRecords.add(new ModificationRecord(statement.location() + 1, 5));
                    }

                    if (statement.compareTo("LTORG") == 0) {
                        literalPool = _literalPools.remove(0);
                    }

//                    Uncomment next line to show the instruction and corresponding object code
//                    System.out.println(statement + "\t\t" + objectCode);

                    if (textRecord.add(objectCode) == false || statement.compareTo("RESB") == 0) {
                        objectProgram.write(textRecord.toObjectProgram() + '\n');

                        textRecord = new TextRecord(statement.location());
                        textRecord.add(objectCode);
                    }
                }
            }
            
            objectProgram.write(textRecord.toObjectProgram() + '\n');
            
            for (Record r : mRecords) {
                objectProgram.write(r.toObjectProgram() + '\n');
            }
            
            objectProgram.write(new EndRecord(_startAddress).toObjectProgram() + '\n');
        }
        
    }
    
    private String assembleInstruction(Statement statement, Map<String, Integer> symTab, Map<String, Literal> litPool) {
        String objCode = "";

        if (_opTable.containsKey(statement.operation())) {
            switch (_opTable.get(statement.operation()).format()) {
                case "1":
                    objCode = _opTable.get(statement.operation()).opcode();
                    
                    break;
                case "2":
                    objCode = _opTable.get(statement.operation()).opcode();
                    
                    objCode += Integer.toHexString(symTab.get(statement.operand1())).toUpperCase();
                    objCode += Integer.toHexString(symTab.get(statement.operand2())).toUpperCase();
                    
                    break;
                case "3/4":
                    final int n = 1 << 5;
                    final int i = 1 << 4;
                    final int x = 1 << 3;
                    final int b = 1 << 2;
                    final int p = 1 << 1;
                    final int e = 1;
                    
                    int code = Integer.parseInt(_opTable.get(statement.operation()).opcode(), 16) << 4;
                    String operand = statement.operand1();
                    
                    if (operand == null) {
                        code = (code | n | i) << 12; // for RSUB
                    } else {
                        switch (operand.charAt(0)) {
                            case '#': // immediate addressing
                                code |= i;

                                operand = operand.substring(1);
                                break;
                            case '@': // indirect addressing
                                code |= n;

                                operand = operand.substring(1);
                                break;
                            default: // simple/direct addressing
                                code |= n | i;

                                if (statement.operand2() != null) {
                                    code |= x;
                                }
                        }
                        
                        int disp, target;
                        
                        if (symTab.get(operand) == null && litPool.get(operand) == null) {
                            disp = Integer.parseInt(operand);
                        } else {
                            if (symTab.get(operand) != null) {
                                disp = target = symTab.get(operand);
                            } else {
                                disp = target = litPool.get(operand).address();
                            }
                            
                            if (statement.isExtended() == false) {
                                disp -= statement.location() + 3;
                                
                                if (disp >= -2048 && disp <= 2047) {
                                    code |= p;
                                } else {
                                    code |= b;
                                    
                                    disp = target - _baseAddress;
                                }
                            }
                        }
                        
                        if (statement.isExtended()) {
                            code |= e;
                            
                            code = (code << 20) | (disp & 0xFFFFF);
                        } else {
                            code = (code << 12) | (disp & 0xFFF);
                        }
                    }
                    
                    objCode = String.format(statement.isExtended() ? "%08X" : "%06X", code);
                    
                    break;
            }
        } else if (statement.compareTo("BYTE") == 0 || statement.hasLiteral()) {
            String s, type;
            
            if (statement.compareTo("BYTE") == 0) {
                s = statement.operand1();
            } else { // literal
                s = statement.operation();
            }
            
            type = s.substring(0, s.indexOf('\''));
            s = s.substring(s.indexOf('\'') + 1, s.lastIndexOf('\''));
            
            switch (type) {
                case "C":
                case "=C":
                    for (char ch : s.toCharArray()) {
                        objCode += Integer.toHexString(ch).toUpperCase();
                    }
                
                    break;
                case "X":
                case "=X":
                    objCode = s;
                    
                    break;
            }
        } else if (statement.compareTo("WORD") == 0) {
            // convert constant to object code
        } else if (statement.compareTo("BASE") == 0) {
            _baseAddress = symTab.get(statement.operand1());
        }
        
        return objCode;
    }
    
    public static void main(String[] args) {
        try {
            Assembler asm = new Assembler(new File("ValidOperations.txt"));
            
            asm.assemble(new File("copy.asm"), new File("copy.o"));
        } catch (ExpectedDirectiveNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
