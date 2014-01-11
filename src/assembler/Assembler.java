package assembler;

import java.io.File;
import java.io.FileInputStream;
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
    private int _firstExecAddress;
    private int _programLength;
    private int _baseAddress;
    private final Map<String, Operation> _opTable;
    private final Map<String, Integer> _registerTable;
    private final Map<String, Integer> _symbolTable;
    
    public Assembler() {
        _opTable = Utility.getOperaionTable();
        _registerTable = Utility.getRegisterTable();
        
        _symbolTable = new HashMap<>();
        _symbolTable.put(null, 0);
    }
    
    public void assemble(File input, File output) throws IOException, ClassNotFoundException {
        File intermediateFile = new File(".assembler.tmp");

        try {
            intermediateFile.createNewFile();

            processPass1(input, intermediateFile);

            processPass2(intermediateFile, output);
        } finally {
            intermediateFile.delete();
        }
    }
    
    private void processPass1(File input, File output) throws IOException {
        try (Scanner scanner = new Scanner(input);
             FileOutputStream ostream = new FileOutputStream(output);
             ObjectOutputStream objOutputStream = new ObjectOutputStream(ostream);) {
            
            _locctr = _startAddress = 0;
            _firstExecAddress = -1;
            
            while (scanner.hasNext()) {
                try {
                    Statement statement = Statement.parse(scanner.nextLine());

                    if (statement.isComment()) {
                        continue;
                    }
                    
                    statement.setLocation(_locctr);
                    
                    if (statement.label() != null) {
                        if (_symbolTable.containsKey(statement.label())) {
                            throw new DuplicateSymbolException(statement);
                        } else {
                            _symbolTable.put(statement.label(), _locctr);
                        }
                    }
                    
                    switch (statement.operation()) {
                        case "START":
                            _startAddress = Integer.parseInt(statement.operand1());
                        
                            statement.setLocation(_locctr = _startAddress);
                            break;
                        case "END":
                            break;
                        case "WORD":
                            _locctr += 3;
                            
                            break;
                        case "RESW":
                            _locctr += 3 * Integer.parseInt(statement.operand1());
                            
                            break;
                        case "RESB":
                            _locctr += Integer.parseInt(statement.operand1());
                            
                            break;
                        case "BYTE":
                            String s = statement.operand1();

                            switch (s.charAt(0)) {
                                case 'C':
                                    _locctr += (s.length() - 3); // C'EOF' -> EOF -> 3 bytes
                                    break;
                                case 'X':
                                    _locctr += (s.length() - 3) / 2; // X'05' -> 05 -> 2 half bytes
                                    break;
                            }
                            break;
                        case "BASE":
                        case "NOBASE":
                            break;
                        default:
                            if (_opTable.containsKey(statement.operation())) {
                                if (_firstExecAddress < 0) {
                                    _firstExecAddress = _locctr;
                                }
                                
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
                            } else {
                                throw new InvalidOperationCodeException(statement);
                            }
                    }
                    
    //                Uncomment the next line can show the Loc and Source statements
    //                System.out.println(statement);
                    
                    objOutputStream.writeObject(statement);
                } catch (DuplicateSymbolException | InvalidOperationCodeException e) {
                    System.out.println(e.getMessage());
                }
            }
            
            _programLength = _locctr - _startAddress;
        }
    }
    
    private void processPass2(File input, File output) throws IOException, ClassNotFoundException {
        try (FileInputStream istream = new FileInputStream(input);
             ObjectInputStream objInputStream = new ObjectInputStream(istream);
             FileWriter objectProgram = new FileWriter(output)) {
            
            List<Record> mRecords = new ArrayList<>();
            TextRecord textRecord = new TextRecord(_startAddress);
            int lastRecordAddress = _startAddress;
            
            while (istream.available() > 0) {
                Statement statement = (Statement) objInputStream.readObject();
                
                if (statement.isComment()) {
                    continue;
                }
                
                if (statement.compareTo("START") == 0) {
                    objectProgram.write(new HeaderRecord(statement.label(), _startAddress, _programLength).toObjectProgram() + '\n');
                } else if (statement.compareTo("END") == 0) {
                    break;
                } else {
                    String objectCode = assembleInstruction(statement);

                    // If it is format 4 and not immediate value
                    if (statement.isExtended() && _symbolTable.containsKey(statement.operand1())) {
                        mRecords.add(new ModificationRecord(statement.location() + 1, 5));
                    }

//                    Uncomment next line to show the instruction and corresponding object code
//                    System.out.println(statement + "\t\t" + objectCode);
                    
                    if (statement.location() - lastRecordAddress >= 0x1000 || textRecord.add(objectCode) == false) {
                        objectProgram.write(textRecord.toObjectProgram() + '\n');

                        textRecord = new TextRecord(statement.location());
                        textRecord.add(objectCode);
                    }
                    
                    lastRecordAddress = statement.location();
                }
            }
            
            objectProgram.write(textRecord.toObjectProgram() + '\n');
            
            for (Record r : mRecords) {
                objectProgram.write(r.toObjectProgram() + '\n');
            }
            
            objectProgram.write(new EndRecord(_firstExecAddress).toObjectProgram() + '\n');
        }
    }
    
    private String assembleInstruction(Statement statement) {
        String objCode = "";

        if (_opTable.containsKey(statement.operation())) {
            switch (_opTable.get(statement.operation()).format()) {
                case "1":
                    objCode = _opTable.get(statement.operation()).opcode();
                    
                    break;
                case "2":
                    objCode = _opTable.get(statement.operation()).opcode();
                    
                    objCode += Integer.toHexString(_registerTable.get(statement.operand1())).toUpperCase();
                    objCode += Integer.toHexString(_registerTable.get(statement.operand2())).toUpperCase();
                    
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
                        code = (code | n | i) << 12; // for RSUB, NOBASE
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
                        
                        int disp;
                        
                        if (_symbolTable.get(operand) == null) {
                            disp = Integer.parseInt(operand);
                        } else {
                            int targetAddress = _symbolTable.get(operand);
                            
                            disp = targetAddress;
                            
                            if (statement.isExtended() == false) {
                                disp -= statement.location() + 3;
                                
                                if (disp >= -2048 && disp <= 2047) {
                                    code |= p;
                                } else {
                                    code |= b;
                                    
                                    disp = targetAddress - _baseAddress;
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
        } else if (statement.compareTo("BYTE") == 0) {
            String s = statement.operand1();
            char type = s.charAt(0);
            
            s = s.substring(s.indexOf('\'') + 1, s.lastIndexOf('\''));
            
            switch (type) {
                case 'C':
                    for (char ch : s.toCharArray()) {
                        objCode += Integer.toHexString(ch).toUpperCase();
                    }
                
                    break;
                case 'X':
                    objCode = s;
                    
                    break;
            }
        } else if (statement.compareTo("WORD") == 0) {
            objCode = String.format("%06X", statement.operand1());
        } else if (statement.compareTo("BASE") == 0) {
            _baseAddress = _symbolTable.get(statement.operand1());
        } else if (statement.compareTo("NOBASE") == 0) {
            _baseAddress = 0;
        }
        
        return objCode;
    }
    
    public static void main(String[] args) {
        try {
            Assembler asm = new Assembler();
            
            asm.assemble(new File("copy.asm"), new File("copy.o"));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
