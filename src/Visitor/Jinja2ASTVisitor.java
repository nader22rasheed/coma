package Visitor;

import AST.ASTnode;
import AST.JINJA.*;
import Symbols.SymbolTableManager;
import Symbols.Symbol;

public class Jinja2ASTVisitor {
    private final SymbolTableManager symbols;

    public Jinja2ASTVisitor() {
        this.symbols = new SymbolTableManager();
        this.symbols.enterScope("global");
    }

    public Jinja2ASTVisitor(SymbolTableManager symbols) {
        this.symbols = symbols;
    }

    public void visit(ASTnode node) {
        if (node == null) return;
        switch (node.getNodeName()) {
            case "Assignment": visitAssignment((AssignmentNode) node); break;
            case "BoolEq": visitBoolEq((BoolEqNode) node); break;
            case "BoolLiteral": visitBoolLiteral((EqBoolLiteralNode) node); break;
            case "EqBoolPar": visitEqBoolPar((EqBoolParNode) node); break;
            case "EqDbl": visitEqDbl((EqDblNode) node); break;
            case "EqInt": visitEqInt((EqIntNode) node); break;
            case "EqMul": visitEqMul((EqMulNode) node); break;
            case "EqPar": visitEqPar((EqParNode) node); break;
            case "EqStr": visitEqStr((EqStrNode) node); break;
            case "EqVar": visitEqVar((EqVarNode) node); break;
            case "IfFragment": visitIfFragment((IfFragmentNode) node); break;
            case "IfStatement": visitIfStatement((IfStatementNode) node); break;
            case "ElifFragment": visitElifFragment((ElifFragmentNode) node); break;
            case "ElifStatement": visitElifStatement((ElifStatementNode) node); break;
            case "ElseFragment": visitElseFragment((ElseFragmentNode) node); break;
            case "ElseStatement": visitElseStatement((ElseStatementNode) node); break;
            case "EndIfFragment": visitEndIfFragment((EndIfFragmentNode) node); break;
            case "EndWhileFragment": visitEndWhileFragment((EndWhileFragmentNode) node); break;
            case "JinjaExpression": visitJinjaExpression((JinjaExpressionNode) node); break;
            case "StmtAssign": visitStmtAssign((StmtAssignNode) node); break;
            case "StmtExpr": visitStmtExpr((StmtExprNode) node); break;
            case "StmtIf": visitStmtIf((StmtIfNode) node); break;
            case "StmtWhile": visitStmtWhile((StmtWhileNode) node); break;
            case "WhileFragment": visitWhileFragment((WhileFragmentNode) node); break;
            case "WhileStatement": visitWhileStatement((WhileStatementNode) node); break;
            case "RelationExpr": visitRelationExpr((RelationExprNode) node); break;
            case "EqAdd": visitEqAdd((EqAddNode) node); break;
            case "CodeBlock": visitCodeBlock((CodeBlockNode) node); break;
            default: System.out.println("Unknown node type: " + node.getNodeName());
        }
    }

    public void visitAssignment(AssignmentNode node) {
        symbols.insert(node.variable, "variable", "int", node.getLineNumber(), node.getLineNumber());
        visit(node.value);
    }

    public void visitBoolEq(BoolEqNode node) { visit(node.left); visit(node.right); }
    public void visitBoolLiteral(EqBoolLiteralNode node) {}
    public void visitEqBoolPar(EqBoolParNode node) { visit(node.inside); }
    public void visitEqDbl(EqDblNode node) {}
    public void visitEqInt(EqIntNode node) {}
    public void visitEqMul(EqMulNode node) { visit(node.left); visit(node.right); }
    public void visitEqPar(EqParNode node) { visit(node.inside); }
    public void visitEqStr(EqStrNode node) {}
    public void visitEqVar(EqVarNode node) { if (symbols.lookup(node.name) == null) System.out.println("Error: Variable '" + node.name + "' not defined (line " + node.getLineNumber() + ")"); }
    public void visitIfFragment(IfFragmentNode node) { visit(node.condition); }
    public void visitIfStatement(IfStatementNode node) { visit(node.ifFragment); symbols.enterScope("if"); visit(node.code); if (node.elifChain != null) visit(node.elifChain); if (node.elseStmt != null) visit(node.elseStmt); symbols.exitScope(); visit(node.endifFragment); }
    public void visitElifFragment(ElifFragmentNode node) { visit(node.condition); }
    public void visitElifStatement(ElifStatementNode node) { visit(node.fragment); visit(node.code); if (node.nextElif != null) visit(node.nextElif); if (node.elseStmt != null) visit(node.elseStmt); }
    public void visitElseFragment(ElseFragmentNode node) {}
    public void visitElseStatement(ElseStatementNode node) { visit(node.fragment); visit(node.code); }
    public void visitEndIfFragment(EndIfFragmentNode node) {}
    public void visitEndWhileFragment(EndWhileFragmentNode node) {}
    public void visitJinjaExpression(JinjaExpressionNode node) { visit(node.inner); }
    public void visitStmtAssign(StmtAssignNode node) { visit(node.assign); }
    public void visitStmtExpr(StmtExprNode node) { visit(node.expr); }
    public void visitStmtIf(StmtIfNode node) { visit(node.ifStmt); }
    public void visitStmtWhile(StmtWhileNode node) { visit(node.whileStmt); }
    public void visitWhileFragment(WhileFragmentNode node) { visit(node.condition); }
    public void visitWhileStatement(WhileStatementNode node) { symbols.enterScope("while"); visit(node.whileFragment); for (ASTnode stmt : node.body) visit(stmt); symbols.exitScope(); visit(node.endWhile); }
    public void visitRelationExpr(RelationExprNode node) { visit(node.left); visit(node.right); }
    public void visitEqAdd(EqAddNode node) { visit(node.left); visit(node.right); }
    public void visitCodeBlock(CodeBlockNode node) { symbols.enterScope("block"); if (node.getHtml() != null) visit(node.getHtml()); symbols.exitScope(); }
    public void printSymbolTable() { symbols.printAll(); }
}
