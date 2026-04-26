package AST.JINJA;

import AST.ASTnode;

public class IfExpressionNode extends ASTnode {

    public IfExpressionNode(int line) {
        super("if_expression", line);
    }

    @Override
    public void print(int indent) {

    }
}
