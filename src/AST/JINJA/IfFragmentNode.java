package AST.JINJA;

import AST.ASTnode;

public class IfFragmentNode extends ASTnode {
    public EqVarNode condition;

    public IfFragmentNode(EqVarNode condition, int line) {
        super("IfFragment", line);
        this.condition = condition;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("IfFragment");
        condition.print(indent + 1);
    }
}
