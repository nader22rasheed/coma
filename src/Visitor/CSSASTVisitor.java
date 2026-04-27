package Visitor;

import AST.ASTnode;
import AST.CSS.*;
import antlr.HTMLCSSJinjaParser;
import antlr.HTMLCSSJinjaParserBaseVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CSSASTVisitor extends HTMLCSSJinjaParserBaseVisitor<ASTnode> {

    @Override
    public ASTnode visitStylesheetRule(HTMLCSSJinjaParser.StylesheetRuleContext ctx) {
        StylesheetNode sheet = new StylesheetNode(ctx.getStart().getLine());

        for (var child : ctx.children) {
            ASTnode n = null;
            try {
                n = visit(child);
            } catch (Throwable ignored) {
                // ignore non-AST parse fragments
            }
            if (n == null) continue;

            if (n instanceof CharsetNode charset) {
                sheet.setCharset(charset);
            } else if (n instanceof ImportNode imp) {
                sheet.addImport(imp);
            } else if (n instanceof NamespaceNode ns) {
                sheet.addNamespace(ns);
            } else if (n instanceof NeastedStatementNode rule) {
                sheet.addRule(rule);
            } else if (n instanceof ASTnode ast) {
                sheet.addChild(ast);
            }
        }

        return sheet;
    }

    @Override
    public ASTnode visitGoodCharset(HTMLCSSJinjaParser.GoodCharsetContext ctx) {
        return new CharsetNode(ctx.getStart().getLine(), ctx.CSS_String_().getText(), true);
    }

    @Override
    public ASTnode visitBadCharset(HTMLCSSJinjaParser.BadCharsetContext ctx) {
        return new CharsetNode(ctx.getStart().getLine(), ctx.CSS_String_().getText(), false);
    }

    @Override
    public ASTnode visitGoodImport(HTMLCSSJinjaParser.GoodImportContext ctx) {
        String url = ctx.CSS_String_() != null ? ctx.CSS_String_().getText() : (ctx.css_url() != null ? ctx.css_url().getText() : null);
        MediaQueryListNode media = ctx.mediaQueryList() != null ? (MediaQueryListNode) visit(ctx.mediaQueryList()) : null;
        return new ImportNode(ctx.getStart().getLine(), url, media, true);
    }

    @Override
    public ASTnode visitGoodImport2(HTMLCSSJinjaParser.GoodImport2Context ctx) {
        String url = ctx.CSS_String_() != null ? ctx.CSS_String_().getText() : (ctx.css_url() != null ? ctx.css_url().getText() : null);
        return new ImportNode(ctx.getStart().getLine(), url, null, true);
    }

    @Override
    public ASTnode visitBadImport(HTMLCSSJinjaParser.BadImportContext ctx) {
        String url = ctx.CSS_String_() != null ? ctx.CSS_String_().getText() : (ctx.css_url() != null ? ctx.css_url().getText() : null);
        MediaQueryListNode media = ctx.mediaQueryList() != null ? (MediaQueryListNode) visit(ctx.mediaQueryList()) : null;
        return new ImportNode(ctx.getStart().getLine(), url, media, false);
    }

    @Override
    public ASTnode visitBadImport2(HTMLCSSJinjaParser.BadImport2Context ctx) {
        String url = ctx.CSS_String_() != null ? ctx.CSS_String_().getText() : (ctx.css_url() != null ? ctx.css_url().getText() : null);
        return new ImportNode(ctx.getStart().getLine(), url, null, false);
    }

    @Override
    public ASTnode visitGoodNamespace(HTMLCSSJinjaParser.GoodNamespaceContext ctx) {
        String prefix = null;
        String url = null;

        if (ctx.namespacePrefix() != null) {
            prefix = ctx.namespacePrefix().getText();
        }
        if (ctx.CSS_String_() != null) {
            url = ctx.CSS_String_().getText();
        } else if (ctx.css_url() != null) {
            url = ctx.css_url().getText();
        }

        return new NamespaceNode(ctx.getStart().getLine(), prefix, url, true);
    }

    @Override
    public ASTnode visitBadNamespace(HTMLCSSJinjaParser.BadNamespaceContext ctx) {
        String prefix = null;
        String url = null;

        if (ctx.namespacePrefix() != null) {
            prefix = ctx.namespacePrefix().getText();
        }
        if (ctx.CSS_String_() != null) {
            url = ctx.CSS_String_().getText();
        } else if (ctx.css_url() != null) {
            url = ctx.css_url().getText();
        }

        return new NamespaceNode(ctx.getStart().getLine(), prefix, url, false);
    }

    @Override
    public ASTnode visitSelectorGroupRule(HTMLCSSJinjaParser.SelectorGroupRuleContext ctx) {
        SelectorListNode list = new SelectorListNode(ctx.getStart().getLine());
        for (HTMLCSSJinjaParser.SelectorContext selCtx : ctx.selector()) {
            SelectorNode sel = (SelectorNode) visit(selCtx);
            if (sel != null) list.addSelector(sel);
        }
        return list;
    }

    @Override
    public ASTnode visitSelectorRule(HTMLCSSJinjaParser.SelectorRuleContext ctx) {
        SelectorNode selector = new SelectorNode(ctx.getStart().getLine());

        SimpleSelectorSequenceNode firstSeq = (SimpleSelectorSequenceNode) visit(ctx.simpleSelectorSequence(0));
        selector.addSequence(firstSeq, null);

        for (int i = 0; i < ctx.combinator().size(); i++) {
            CombinatorNode comb = (CombinatorNode) visit(ctx.combinator(i));
            SimpleSelectorSequenceNode seq = (SimpleSelectorSequenceNode) visit(ctx.simpleSelectorSequence(i + 1));
            selector.addSequence(seq, comb);
        }

        return selector;
    }

    @Override
    public ASTnode visitSimpleSeq1(HTMLCSSJinjaParser.SimpleSeq1Context ctx) {
        SimpleSelectorSequenceNode seq = new SimpleSelectorSequenceNode(ctx.getStart().getLine());

        if (ctx.typeSelector() != null) {
            ASTnode n = visit(ctx.typeSelector());
            if (n instanceof SimpleSelectorNode s) seq.add(s);
        } else if (ctx.universal() != null) {
            ASTnode n = visit(ctx.universal());
            if (n instanceof SimpleSelectorNode s) seq.add(s);
        }

        for (var child : ctx.children) {
            ASTnode n = null;
            try { n = visit(child); } catch (Throwable ignored) {}
            if (n instanceof SimpleSelectorNode s) {
                seq.add(s);
            }
        }

        return seq;
    }

    @Override
    public ASTnode visitSimpleSeq2(HTMLCSSJinjaParser.SimpleSeq2Context ctx) {
        SimpleSelectorSequenceNode seq = new SimpleSelectorSequenceNode(ctx.getStart().getLine());

        for (var child : ctx.children) {
            if (child instanceof TerminalNode t && t.getSymbol().getType() == HTMLCSSJinjaParser.CSS_Hash) {
                seq.add(new IdSelectorNode(ctx.getStart().getLine(), t.getText().substring(1)));
                continue;
            }

            ASTnode n = null;
            try { n = visit(child); } catch (Throwable ignored) {}
            if (n instanceof SimpleSelectorNode s) {
                seq.add(s);
            }
        }

        return seq;
    }

    @Override
    public ASTnode visitTypeSelector(HTMLCSSJinjaParser.TypeSelectorContext ctx) {
        int line = ctx.getStart().getLine();
        String namespace = null;
        if (ctx.typeNamespacePrefix() != null) {
            String nsText = ctx.typeNamespacePrefix().getText();
            if (nsText.endsWith("|")) nsText = nsText.substring(0, nsText.length() - 1);
            namespace = nsText;
        }
        return new TypeSelectorNode(line, namespace, ctx.elementName().getText());
    }

    @Override
    public ASTnode visitKnownRuleset(HTMLCSSJinjaParser.KnownRulesetContext ctx) {
        SelectorListNode selectors = (SelectorListNode) visit(ctx.selectorGroup());
        DeclarationListNode declarations = ctx.declarationList() != null ? (DeclarationListNode) visit(ctx.declarationList()) : null;
        return new RulesetNode(ctx.getStart().getLine(), selectors, declarations);
    }

    @Override
    public ASTnode visitUnknownRuleset(HTMLCSSJinjaParser.UnknownRulesetContext ctx) {
        DeclarationListNode declarations = ctx.declarationList() != null ? (DeclarationListNode) visit(ctx.declarationList()) : null;
        return new RulesetNode(ctx.getStart().getLine(), null, declarations);
    }

    @Override
    public ASTnode visitMediaRule(HTMLCSSJinjaParser.MediaRuleContext ctx) {
        MediaQueryListNode queries = ctx.mediaQueryList() != null ? (MediaQueryListNode) visit(ctx.mediaQueryList()) : null;
        GroupRuleBodyNode body = ctx.groupRuleBody() != null ? (GroupRuleBodyNode) visit(ctx.groupRuleBody()) : null;
        return new MediaNode(ctx.getStart().getLine(), queries, body);
    }

    @Override
    public ASTnode visitMediaQueryListRule(HTMLCSSJinjaParser.MediaQueryListRuleContext ctx) {
        MediaQueryListNode list = new MediaQueryListNode(ctx.getStart().getLine());
        for (HTMLCSSJinjaParser.MediaQueryContext qCtx : ctx.mediaQuery()) {
            MediaQueryNode q = (MediaQueryNode) visit(qCtx);
            if (q != null) list.addQuery(q);
        }
        return list;
    }

    @Override
    public ASTnode visitMediaTypeQuery(HTMLCSSJinjaParser.MediaTypeQueryContext ctx) {
        return new MediaQueryNode(ctx.getStart().getLine(), ctx.getText());
    }

    @Override
    public ASTnode visitMediaExprQuery(HTMLCSSJinjaParser.MediaExprQueryContext ctx) {
        return new MediaQueryNode(ctx.getStart().getLine(), ctx.getText());
    }

    @Override
    public ASTnode visitMediaExpression(HTMLCSSJinjaParser.MediaExpressionContext ctx) {
        String feature = ctx.mediaFeature().getText();
        String expr = ctx.expr() != null ? ctx.expr().getText() : null;
        return new MediaExpressionNode(ctx.getStart().getLine(), feature, expr);
    }

    @Override
    public ASTnode visitGroupBody(HTMLCSSJinjaParser.GroupBodyContext ctx) {
        GroupRuleBodyNode node = new GroupRuleBodyNode(ctx.getStart().getLine());
        for (var nestedCtx : ctx.nestedStatement()) {
            ASTnode n = visit(nestedCtx);
            if (n instanceof NeastedStatementNode rule) {
                node.addRule(rule);
            }
        }
        return node;
    }

    @Override
    public ASTnode visitNestRuleset(HTMLCSSJinjaParser.NestRulesetContext ctx) {
        return visit(ctx.ruleset());
    }

    @Override
    public ASTnode visitNestMedia(HTMLCSSJinjaParser.NestMediaContext ctx) {
        return visit(ctx.media());
    }

    @Override
    public ASTnode visitNestPage(HTMLCSSJinjaParser.NestPageContext ctx) {
        return visit(ctx.page());
    }

    @Override
    public ASTnode visitNestFontFace(HTMLCSSJinjaParser.NestFontFaceContext ctx) {
        return visit(ctx.fontFaceRule());
    }

    @Override
    public ASTnode visitNestKeyframes(HTMLCSSJinjaParser.NestKeyframesContext ctx) {
        return visit(ctx.keyframesRule());
    }

    @Override
    public ASTnode visitNestSupports(HTMLCSSJinjaParser.NestSupportsContext ctx) {
        return visit(ctx.supportsRule());
    }

    @Override
    public ASTnode visitNestViewport(HTMLCSSJinjaParser.NestViewportContext ctx) {
        return visit(ctx.viewport());
    }

    @Override
    public ASTnode visitNestCounter(HTMLCSSJinjaParser.NestCounterContext ctx) {
        return visit(ctx.counterStyle());
    }

    @Override
    public ASTnode visitNestFontFeature(HTMLCSSJinjaParser.NestFontFeatureContext ctx) {
        return visit(ctx.fontFeatureValuesRule());
    }

    @Override
    public ASTnode visitNestAtRule(HTMLCSSJinjaParser.NestAtRuleContext ctx) {
        return visit(ctx.atRule());
    }

    @Override
    public ASTnode visitFontFaceRuleLabel(HTMLCSSJinjaParser.FontFaceRuleLabelContext ctx) {
        FontFaceNode node = new FontFaceNode(ctx.getStart().getLine());
        for (var declCtx : ctx.fontFaceDeclaration()) {
            FontFaceDeclarationNode decl = (FontFaceDeclarationNode) visit(declCtx);
            if (decl != null) {
                node.declarations.add(decl);
                node.addChild(decl);
            }
        }
        return node;
    }

    @Override
    public ASTnode visitKnownFontFaceDeclaration(HTMLCSSJinjaParser.KnownFontFaceDeclarationContext ctx) {
        String prop = ctx.property_().getText();
        ASTnode val = visit(ctx.expr());
        return new FontFaceDeclarationNode(ctx.getStart().getLine(), prop, val, true);
    }

    @Override
    public ASTnode visitUnknownFontFaceDeclaration(HTMLCSSJinjaParser.UnknownFontFaceDeclarationContext ctx) {
        String prop = ctx.property_().getText();
        ASTnode val = visit(ctx.value());
        return new FontFaceDeclarationNode(ctx.getStart().getLine(), prop, val, false);
    }

    @Override
    public ASTnode visitKeyframesRule(HTMLCSSJinjaParser.KeyframesRuleContext ctx) {
        String name = ctx.css_ident().getText();
        KeyframesNode node = new KeyframesNode(ctx.getStart().getLine(), name);
        for (var blockCtx : ctx.keyframeBlock()) {
            KeyframeBlockNode block = (KeyframeBlockNode) visit(blockCtx);
            if (block != null) node.addBlock(block);
        }
        return node;
    }

    @Override
    public ASTnode visitKeyframeBlock(HTMLCSSJinjaParser.KeyframeBlockContext ctx) {
        DeclarationListNode decls = ctx.declarationList() != null ? (DeclarationListNode) visit(ctx.declarationList()) : null;
        KeyframeBlockNode node = new KeyframeBlockNode(ctx.getStart().getLine(), decls);
        for (var child : ctx.keyframeSelector().children) {
            if (child instanceof TerminalNode t) {
                int type = t.getSymbol().getType();
                if (type == HTMLCSSJinjaParser.CSS_From || type == HTMLCSSJinjaParser.CSS_To || type == HTMLCSSJinjaParser.CSS_Percentage) {
                    node.addKey(t.getText());
                }
            }
        }
        return node;
    }

    @Override
    public ASTnode visitPseudoPage(HTMLCSSJinjaParser.PseudoPageContext ctx) {
        return new PseudoPageNode(ctx.getStart().getLine(), ctx.css_ident().getText());
    }

    @Override
    public ASTnode visitPageRule(HTMLCSSJinjaParser.PageRuleContext ctx) {
        return new PageNode(ctx.getStart().getLine(), ctx.pseudoPage() != null ? (PseudoPageNode) visit(ctx.pseudoPage()) : null);
    }

    @Override
    public ASTnode visitCalcRule(HTMLCSSJinjaParser.CalcRuleContext ctx) {
        CalcSumNode sum = ctx.calcSum() != null ? (CalcSumNode) visit(ctx.calcSum()) : null;
        return new CalcNode(ctx.getStart().getLine(), sum);
    }

    @Override
    public ASTnode visitCalcSumRule(HTMLCSSJinjaParser.CalcSumRuleContext ctx) {
        CalcSumNode node = new CalcSumNode(ctx.getStart().getLine());
        if (!ctx.calcProduct().isEmpty()) {
            node.addProduct((CalcProductNode) visit(ctx.calcProduct(0)), null);
            for (int i = 1; i < ctx.calcProduct().size(); i++) {
                String op = ctx.children.get(i * 2 - 1).getText();
                node.addProduct((CalcProductNode) visit(ctx.calcProduct(i)), op);
            }
        }
        return node;
    }

    @Override
    public ASTnode visitCalcProdRule(HTMLCSSJinjaParser.CalcProdRuleContext ctx) {
        CalcProductNode node = new CalcProductNode(ctx.getStart().getLine());
        if (!ctx.calcValue().isEmpty()) {
            node.addValue((CalcValueNode) visit(ctx.calcValue(0)), null);
            int valueIndex = 1;
            for (var child : ctx.children) {
                String text = child.getText();
                if ("*".equals(text) || "/".equals(text)) {
                    node.addValue((CalcValueNode) visit(ctx.calcValue(valueIndex)), text);
                    valueIndex++;
                }
            }
        }
        return node;
    }

    @Override
    public ASTnode visitCalcValNum(HTMLCSSJinjaParser.CalcValNumContext ctx) {
        return new CalcValueNode(ctx.getStart().getLine(), ctx.getText().trim());
    }

    @Override
    public ASTnode visitCalcValDim(HTMLCSSJinjaParser.CalcValDimContext ctx) {
        return new CalcValueNode(ctx.getStart().getLine(), ctx.getText().trim());
    }

    @Override
    public ASTnode visitCalcValPercent(HTMLCSSJinjaParser.CalcValPercentContext ctx) {
        return new CalcValueNode(ctx.getStart().getLine(), ctx.getText().trim());
    }

    @Override
    public ASTnode visitCalcValParen(HTMLCSSJinjaParser.CalcValParenContext ctx) {
        return new CalcValueNode(ctx.getStart().getLine(), (ASTnode) visit(ctx.calcSum()));
    }

    @Override
    public ASTnode visitKnownTerm(HTMLCSSJinjaParser.KnownTermContext ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm2(HTMLCSSJinjaParser.KnownTerm2Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm3(HTMLCSSJinjaParser.KnownTerm3Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm4(HTMLCSSJinjaParser.KnownTerm4Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm5(HTMLCSSJinjaParser.KnownTerm5Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm6(HTMLCSSJinjaParser.KnownTerm6Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm7(HTMLCSSJinjaParser.KnownTerm7Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm8(HTMLCSSJinjaParser.KnownTerm8Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm9(HTMLCSSJinjaParser.KnownTerm9Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm10(HTMLCSSJinjaParser.KnownTerm10Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitKnownTerm11(HTMLCSSJinjaParser.KnownTerm11Context ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), true);
    }

    @Override
    public ASTnode visitBadTerm(HTMLCSSJinjaParser.BadTermContext ctx) {
        return new TermNode(ctx.getStart().getLine(), ctx.getText(), false);
    }

    @Override
    public ASTnode visitVarRule(HTMLCSSJinjaParser.VarRuleContext ctx) {
        return new VarNode(ctx.getStart().getLine(), ctx.CSS_Variable().getText());
    }

    @Override
    public ASTnode visitAnyIdent(HTMLCSSJinjaParser.AnyIdentContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyNumber(HTMLCSSJinjaParser.AnyNumberContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyPercent(HTMLCSSJinjaParser.AnyPercentContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyDim(HTMLCSSJinjaParser.AnyDimContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyString(HTMLCSSJinjaParser.AnyStringContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyUrl(HTMLCSSJinjaParser.AnyUrlContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyHash(HTMLCSSJinjaParser.AnyHashContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyUnicode(HTMLCSSJinjaParser.AnyUnicodeContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyInclude(HTMLCSSJinjaParser.AnyIncludeContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyDashMatch(HTMLCSSJinjaParser.AnyDashMatchContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitAnyColon(HTMLCSSJinjaParser.AnyColonContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "AnyNode");
    }

    @Override
    public ASTnode visitCssNum(HTMLCSSJinjaParser.CssNumContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "Number");
    }

    @Override
    public ASTnode visitCssPercent(HTMLCSSJinjaParser.CssPercentContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "Percent");
    }

    @Override
    public ASTnode visitCssDim(HTMLCSSJinjaParser.CssDimContext ctx) {
        return new AnyNode(ctx.getStart().getLine(), ctx.getText(), "Dimension");
    }
}
