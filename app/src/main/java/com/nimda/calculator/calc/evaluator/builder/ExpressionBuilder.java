package com.nimda.calculator.calc.evaluator.builder;

import com.nimda.calculator.calc.evaluator.lexer.Operator;
import com.nimda.calculator.calc.evaluator.lexer.Parenthesis;
import com.nimda.calculator.calc.evaluator.lexer.PredefinedFunction;
import com.nimda.calculator.calc.evaluator.lexer.Token;

import java.util.Stack;

import static com.nimda.calculator.calc.evaluator.lexer.Operator.NEGATION;
import static com.nimda.calculator.calc.evaluator.lexer.Operator.SUBTRACT;

public class ExpressionBuilder {
    private final Stack<Token> expressionList = new Stack<Token>();

    private boolean notEmpty() {
        return !expressionList.isEmpty();
    }

    public boolean isEmpty() {
        return expressionList.isEmpty();
    }

    private Token peek() {
        return expressionList.peek();
    }

    private void push(Token token) {
        expressionList.push(token);
    }

    private Token pop() {
        return expressionList.pop();
    }

    private boolean topIsNumber() {
        return !expressionList.isEmpty()
                && expressionList.peek() instanceof StringNumberLiteral;
    }

    private boolean topIsOperator() {
        return !expressionList.isEmpty()
                && expressionList.peek() instanceof Operator;
    }

    private boolean topIsPredefinedFunction() {
        return !expressionList.isEmpty()
                && expressionList.peek() instanceof PredefinedFunction;
    }

    public void appendDecimal() {
        if (topIsNumber()) {
            ((StringNumberLiteral) peek()).addDecimalIfNeeded();
        }
    }

    public void appendOperator(Operator operator) {
        if (notEmpty()) {
            if ((operator == SUBTRACT || operator == NEGATION)
                    || (!(peek() instanceof Operator) && peek() != Parenthesis.OPEN)) {
                push(operator);
            }
        }
    }

    public void appendXSquared() {
        if (notEmpty() && !(peek() instanceof Operator)
                && peek() != Parenthesis.OPEN) {
            push(Operator.POWER);
            push(new StringNumberLiteral("2"));
        }
    }

    public void togglePlusMinus() {
        if (topIsNumber()) {
            ((StringNumberLiteral) peek()).togglePlusMinus();
        }
    }

    public void appendParenthesis(Parenthesis parenthesis) {
        if (parenthesis == Parenthesis.CLOSE && notEmpty() && !topIsOperator()
                && peek() != Parenthesis.OPEN) {
            int openCount = 0;
            int closeCount = 0;

            for (Token token : expressionList) {
                if (token == Parenthesis.OPEN) {
                    openCount++;
                } else if (token == Parenthesis.CLOSE) {
                    closeCount++;
                }
            }

            if (openCount > closeCount) {
                push(parenthesis);
            }
        } else if (parenthesis == Parenthesis.OPEN) {
            if (notEmpty() && (peek() == Parenthesis.CLOSE || topIsNumber())) {
                push(Operator.MULTIPLY);
            }

            push(parenthesis);
        }
    }

    public void appendFunction(PredefinedFunction function) {
        if (notEmpty() && (peek() == Parenthesis.CLOSE || topIsNumber())) {
            push(Operator.MULTIPLY);
        }
        push(function);
        push(Parenthesis.OPEN);
    }

    public void appendDigit(int digit) {
        if (topIsNumber()) {
            ((StringNumberLiteral) peek()).appendDigit(digit);
        } else {
            if (notEmpty() && peek() == Parenthesis.CLOSE) {
                push(Operator.MULTIPLY);
            }
            push(new StringNumberLiteral(String.valueOf(digit)));
        }
    }

    public void deleteElement() {
        if (notEmpty()) {
            if (topIsNumber()) {
                ((StringNumberLiteral) peek()).deleteChar();

                if (((StringNumberLiteral) peek()).isEmpty()) {
                    pop();
                }
            } else if (peek() == Parenthesis.OPEN) {
                pop();

                if (topIsPredefinedFunction()) {
                    pop();
                }
            } else {
                pop();
            }
        }
    }

    public void clear() {
        expressionList.clear();
    }

    public String build() {
        int openCount = 0;
        int closeCount = 0;

        for (Token token : expressionList) {
            if (token == Parenthesis.OPEN) {
                openCount++;
            } else if (token == Parenthesis.CLOSE) {
                closeCount++;
            }
        }

        int numCloseRequired = openCount - closeCount;

        if (numCloseRequired > 0) {
            if (peek() != Parenthesis.OPEN && !(peek() instanceof Operator)) {
                for (int i = 0; i < numCloseRequired; i++) {
                    push(Parenthesis.CLOSE);
                }
            }
        } else if (numCloseRequired < 0) {
            // This check probably won't be needed since there's another
            // validation that checks if the parenthesis count allows a close.
            for (int i = numCloseRequired; i < 0; i++) {
                expressionList.insertElementAt(Parenthesis.OPEN, 0);
            }
        }

        return toString();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        for (Token token : expressionList) {
            builder.append(token.toString());
        }
        return builder.toString();
    }

    public void setExpression(String string) {
        clear();
        expressionList.add(new StringNumberLiteral(string));
    }
}
