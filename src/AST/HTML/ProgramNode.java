package AST.HTML;

public class ProgramNode {
    private String info = "ProgramNode placeholder";

    public ProgramNode() {}

    @Override
    public String toString() {
        return "ProgramNode()";
    }

    // used by Main.saveAST if exists
    public void print(int indent) {
        for (int i=0;i<indent;i++) System.out.print(" ");
        System.out.println("ProgramNode");
    }
}
