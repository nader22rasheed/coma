package AST.HTML;

import AST.ASTnode;

public class ProgramNode extends ASTnode {
    public ProgramNode(int lineNumber) {
        super("Program", lineNumber);
    }

    @Override
    public void print(int indent) {
        String spaces = " ".repeat(indent * 2);
        System.out.println(spaces + nodeName + " (Line: " + lineNumber + ")");
        for (ASTnode child : children) {
            if (child != null) child.print(indent + 1);
        }
    }
}
