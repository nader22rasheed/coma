package AST;

public class RootNode extends ASTnode {

    public RootNode() {
        super("Root", 0);
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("RootNode");
        for (ASTnode child : children) {
            child.print(indent + 1);
        }
    }
}
