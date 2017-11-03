import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import parser.*;

public class ThfAstGen {

    /**
     * parse ANTLRInputStream containing thf and return ast
     * @param inputStream ANTLRInputStream object
     * @param rule start parsing at this rule
     * @return ast
     * @throws ParseException if there is no such rule
     */
    public static ParseContext parse(ANTLRInputStream inputStream, String rule, String name) throws ParseException {

        tptp_v7_0_0_0Lexer lexer = new tptp_v7_0_0_0Lexer(inputStream);
        lexer.removeErrorListeners(); // only for production
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();


        tptp_v7_0_0_0Parser parser = new tptp_v7_0_0_0Parser(tokens);
        parser.removeErrorListeners(); // only for production
        ParseContext parseContext = new ParseContext();

        DefaultTreeListener treeListener = new DefaultTreeListener();

        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        parser.setBuildParseTree(true);
        parser.setTokenStream(tokens);

        // parsing starting from a rule requires invoking that rulename as parser method
        ParserRuleContext parserRuleContext = null;
        try {
            Class<?> parserClass = parser.getClass();
            Method method = parserClass.getMethod(rule, (Class<?>[]) null);
            parserRuleContext = (ParserRuleContext) method.invoke(parser, (Object[]) null);

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage());
        }        // the above or the below

        // create ast
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(treeListener, parserRuleContext);

        // create and return test.src.main.java.ParseContext
        parseContext.parserRuleContext = parserRuleContext;
        parseContext.name = name;
        parseContext.error = treeListener.error;
        return parseContext;
    }

    /**
     * parse String containing thf and return ast
     * @param inputString String object
     * @param rule start parsing at this rule
     * @return ast
     * @throws ParseException if there is no such rule
     */
    public static ParseContext parse(String inputString, String rule, String name) throws ParseException {
         return ThfAstGen.parse(new ANTLRInputStream(inputString), rule, name);
    }

}
