package AST.JINJA;

import AST.ASTnode;
import AST.HTML.HtmlContentNode;

public class CodeBlockNode extends ASTnode {
    public HtmlContentNode html;

    public CodeBlockNode(int line) {
        super("CodeBlock", line);
        this.html = html;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println(nodeName + " {");

        for (ASTnode child : children) {
            if (child != null) {
                child.print(indent + 1);
            }
        }

        printIndent(indent);
        System.out.println("}");
    }

}
