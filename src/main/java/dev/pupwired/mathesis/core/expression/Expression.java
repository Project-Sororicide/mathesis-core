package dev.pupwired.mathesis.core.expression;

import java.util.List;
import java.util.Locale;

/**
 * A tiny arithmetic expression compiled once and evaluated many times, for formulas written in config.
 *
 * <p>Supports {@code + - * / % ^}, parentheses, unary minus, decimal and exponent numbers, variables you
 * declare when compiling, and the functions {@code min max abs sqrt pow clamp floor ceil round ln}.
 * Names are case-insensitive. {@code ^} is right-associative and binds tighter than unary minus
 * ({@code -2^2} is -4). Division by zero follows IEEE rules (infinity or NaN); callers decide what to do.
 *
 * <p>Variables and functions are resolved at compile time, so typos fail when the config loads, not mid-fight.
 */
public final class Expression {

    private interface Node {
        double eval(double[] vars);
    }

    private final Node root;
    private final int variableCount;

    private Expression(Node root, int variableCount) {
        this.root = root;
        this.variableCount = variableCount;
    }

    /**
     * @param variables names usable in the expression; {@link #evaluate} takes values in this same order
     * @throws IllegalArgumentException with a position and reason if the source is invalid
     */
    public static Expression compile(String source, List<String> variables) {
        return new Parser(source, variables.stream().map(v -> v.toLowerCase(Locale.ROOT)).toList()).parse();
    }

    public double evaluate(double... values) {
        if (values.length != variableCount)
            throw new IllegalArgumentException("Expected " + variableCount + " variable value(s), got " + values.length);
        return root.eval(values);
    }

    private static final class Parser {
        private final String text;
        private final List<String> variables;
        private int pos;

        Parser(String text, List<String> variables) {
            this.text = text;
            this.variables = variables;
        }

        Expression parse() {
            Node node = parseExpression();
            skipWhitespace();
            if (pos < text.length()) throw error("Unexpected '" + text.charAt(pos) + "'");
            return new Expression(node, variables.size());
        }

        private Node parseExpression() {
            Node left = parseTerm();
            while (true) {
                skipWhitespace();
                final Node l = left;
                if (match('+')) {
                    final Node r = parseTerm();
                    left = v -> l.eval(v) + r.eval(v);
                } else if (match('-')) {
                    final Node r = parseTerm();
                    left = v -> l.eval(v) - r.eval(v);
                } else {
                    return left;
                }
            }
        }

        private Node parseTerm() {
            Node left = parseUnary();
            while (true) {
                skipWhitespace();
                final Node l = left;
                if (match('*')) {
                    final Node r = parseUnary();
                    left = v -> l.eval(v) * r.eval(v);
                } else if (match('/')) {
                    final Node r = parseUnary();
                    left = v -> l.eval(v) / r.eval(v);
                } else if (match('%')) {
                    final Node r = parseUnary();
                    left = v -> l.eval(v) % r.eval(v);
                } else {
                    return left;
                }
            }
        }

        private Node parseUnary() {
            skipWhitespace();
            if (match('-')) {
                final Node n = parseUnary();
                return v -> -n.eval(v);
            }
            if (match('+')) return parseUnary();
            return parsePower();
        }

        private Node parsePower() {
            final Node base = parsePrimary();
            skipWhitespace();
            if (match('^')) {
                final Node exponent = parseUnary(); // right-associative, allows 2^-1
                return v -> Math.pow(base.eval(v), exponent.eval(v));
            }
            return base;
        }

        private Node parsePrimary() {
            skipWhitespace();
            if (pos >= text.length()) throw error("Unexpected end of expression");
            char c = text.charAt(pos);

            if (c == '(') {
                pos++;
                Node inner = parseExpression();
                skipWhitespace();
                if (!match(')')) throw error("Missing ')'");
                return inner;
            }
            if (Character.isDigit(c) || c == '.') return parseNumber();
            if (Character.isLetter(c) || c == '_') return parseNameOrCall();
            throw error("Unexpected '" + c + "'");
        }

        private Node parseNumber() {
            int start = pos;
            while (pos < text.length() && (Character.isDigit(text.charAt(pos)) || text.charAt(pos) == '.')) pos++;
            if (pos < text.length() && (text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) {
                int p = pos + 1;
                if (p < text.length() && (text.charAt(p) == '+' || text.charAt(p) == '-')) p++;
                if (p < text.length() && Character.isDigit(text.charAt(p))) {
                    while (p < text.length() && Character.isDigit(text.charAt(p))) p++;
                    pos = p;
                }
            }
            final double value;
            try {
                value = Double.parseDouble(text.substring(start, pos));
            } catch (NumberFormatException e) {
                pos = start;
                throw error("Invalid number '" + text.substring(start, Math.min(text.length(), start + 8)) + "'");
            }
            return v -> value;
        }

        private Node parseNameOrCall() {
            int start = pos;
            while (pos < text.length() && (Character.isLetterOrDigit(text.charAt(pos)) || text.charAt(pos) == '_')) pos++;
            String name = text.substring(start, pos).toLowerCase(Locale.ROOT);
            skipWhitespace();

            if (match('(')) return parseCall(name, start);

            int index = variables.indexOf(name);
            if (index < 0) {
                pos = start;
                throw error("Unknown variable '" + name + "' (available: " + String.join(", ", variables) + ")");
            }
            return v -> v[index];
        }

        private Node parseCall(String name, int nameStart) {
            java.util.ArrayList<Node> args = new java.util.ArrayList<>();
            skipWhitespace();
            if (!match(')')) {
                while (true) {
                    args.add(parseExpression());
                    skipWhitespace();
                    if (match(',')) continue;
                    if (match(')')) break;
                    throw error("Expected ',' or ')' in call to " + name);
                }
            }
            int expected = switch (name) {
                case "min", "max", "pow" -> 2;
                case "clamp" -> 3;
                case "abs", "sqrt", "floor", "ceil", "round", "ln" -> 1;
                default -> {
                    pos = nameStart;
                    throw error("Unknown function '" + name + "'");
                }
            };
            if (args.size() != expected) {
                pos = nameStart;
                throw error("Function " + name + " takes " + expected + " argument(s), got " + args.size());
            }
            Node a = args.get(0);
            Node b = expected >= 2 ? args.get(1) : null;
            Node c = expected == 3 ? args.get(2) : null;
            return switch (name) {
                case "min" -> v -> Math.min(a.eval(v), b.eval(v));
                case "max" -> v -> Math.max(a.eval(v), b.eval(v));
                case "pow" -> v -> Math.pow(a.eval(v), b.eval(v));
                case "clamp" -> v -> Math.max(b.eval(v), Math.min(c.eval(v), a.eval(v)));
                case "abs" -> v -> Math.abs(a.eval(v));
                case "sqrt" -> v -> Math.sqrt(a.eval(v));
                case "floor" -> v -> Math.floor(a.eval(v));
                case "ceil" -> v -> Math.ceil(a.eval(v));
                case "round" -> v -> (double) Math.round(a.eval(v));
                case "ln" -> v -> Math.log(a.eval(v));
                default -> throw new IllegalStateException(name);
            };
        }

        private boolean match(char expected) {
            if (pos < text.length() && text.charAt(pos) == expected) {
                pos++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException("Invalid expression '" + text + "' at position " + pos + ": " + message);
        }
    }
}
