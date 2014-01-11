package assembler;

class DuplicateSymbolException extends Exception {
    public DuplicateSymbolException(Statement statement) {
        super("Duplicate symbol found: " + statement.label());
    }
}
