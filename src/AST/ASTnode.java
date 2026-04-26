package AST;

import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;
import java.io.IOException;


public abstract class ASTnode {
    protected String nodeName;
    protected int lineNumber;
    protected List<ASTnode> children;

    public ASTnode(String nodeName) {
        this(nodeName, 0);
    }

    public ASTnode(String nodeName, int lineNumber) {
        this.nodeName = nodeName != null ? nodeName : "";
        this.lineNumber = lineNumber;
        this.children = new ArrayList<>();
    }


    public void addChild(ASTnode child) {
        if (child != null) this.children.add(child);
    }

    public List<ASTnode> getChildren() {
        return this.children;
    }


    public String getNodeName() {
        return this.nodeName;
    }

    public String getName() {
        return this.nodeName;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setNodeName(String name) {
        this.nodeName = name;
    }

    public void saveToFile(String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(filename)) {
            save(pw, 0);
        }
    }


    protected void save(PrintWriter pw, int indent) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) sb.append("  ");
        sb.append(this.toString());
        pw.println(sb.toString());

        for (ASTnode c : children) {
            if (c != null) c.save(pw, indent + 1);
        }
    }

    public abstract void print(int indent);

    @Override
    public String toString() {
        return nodeName + " (Line: " + lineNumber + ")";
    }

    protected void printIndent(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
    }
}
