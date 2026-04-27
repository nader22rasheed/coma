package AST.JINJA;

import AST.ASTnode;
import AST.HTML.HtmlContentNode;

public class CodeBlockNode extends ASTnode {
    private HtmlContentNode html;

    public CodeBlockNode(int line) {
        super("CodeBlock", line);
    }

    public void setHtml(HtmlContentNode html) {
        this.html = html;
    }

    public HtmlContentNode getHtml() {
        return html;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println(nodeName + " {");
        if (html != null) html.print(indent + 1);
        for (ASTnode child : children) {
            if (child != null) child.print(indent + 1);
        }
        printIndent(indent);
        System.out.println("}");
    }
}
