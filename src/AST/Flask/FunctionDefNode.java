package AST.Flask;

import AST.ASTnode;

public class FunctionDefNode extends ASTnode {
    private String functionName;
    private boolean isAsync;

    public FunctionDefNode(String functionName, boolean isAsync, int lineNumber) {
        super("FunctionDef", lineNumber);
        this.functionName = functionName;
        this.isAsync = isAsync;
    }

    @Override
    public void print(int indent) {
        String spaces = " ".repeat(indent * 2);
        System.out.println(spaces + nodeName + " - Name: " + functionName + ", Async: " + isAsync + " (Line: " + lineNumber + ")");
        for (ASTnode child : children) {  // parameters, return type, body
            child.print(indent + 1);
        }
    }
}