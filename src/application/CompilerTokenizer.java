package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompilerTokenizer {
    private static int tokenCounter = 0;
    static ArrayList<Token> tokensList = new ArrayList<>();

    public static void tokenizeFile(String fileName) throws IOException {
        String line;
        int lineNumber = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String[] words = line.split("\\s+|(?<=[^a-zA-Z0-9\\.])|(?=[^a-zA-Z0-9\\.])");

            for (String word : words) {
                String token = word.trim();
                if (!token.isEmpty()) {
                    processToken(token, lineNumber);
                }
            }
        }

        reader.close();
    }

    private static void processToken(String token, int lineNumber) {
        if (!isSymbol(token) && !isKeyword(token)) {
            handleNonSymbolNonKeyword(token, lineNumber);
        } else {
            handleSymbolOrKeyword(token, lineNumber);
        }
    }

    private static void handleNonSymbolNonKeyword(String token, int lineNumber) {
        if (token.contains(".")) {
            processRealNumber(token, lineNumber);
        } else {
            processIdentifierOrInteger(token, lineNumber);
        }
    }

    private static void processRealNumber(String token, int lineNumber) {
        char[] chars = token.toCharArray();
        if (Character.isDigit(chars[0])) {
            Token t = new Token(token, lineNumber, "real");
            tokensList.add(t);
        } else {
            String identifier = token.substring(0, token.length() - 1);
            Token t1 = new Token(identifier, lineNumber, "identifier");
            Token t2 = new Token(".", lineNumber, "symbol");
            tokensList.add(t1);
            tokensList.add(t2);
        }
    }

    private static void processIdentifierOrInteger(String token, int lineNumber) {
        if (isNumericValue(token)) {
            Token t = new Token(token, lineNumber, "integer");
            tokensList.add(t);
        } else {
            Token t = new Token(token, lineNumber, findType(token, lineNumber));
            tokensList.add(t);
            tokenCounter++;
        }
    }

    private static void handleSymbolOrKeyword(String token, int lineNumber) {
        if (token.equals(":") || token.equals("<") || token.equals(">") || token.equals("|")) {
            handleSpecialSymbols(token, lineNumber);
        } else {
            Token t = new Token(token, lineNumber, findType(token, lineNumber));
            tokensList.add(t);
            tokenCounter++;
        }
    }

    private static void handleSpecialSymbols(String token, int lineNumber) {
        if (token.equals(":") || token.equals("<") || token.equals(">")) {
            handleDoubleSymbols(token, lineNumber);
        } else {
            Token t = new Token(token, lineNumber, findType(token, lineNumber));
            tokensList.add(t);
            tokenCounter++;
        }
    }

    private static void handleDoubleSymbols(String token, int lineNumber) {
        if (token.equals(":") || token.equals("<") || token.equals(">")) {
            String nextToken = getNextToken();
            if (!nextToken.isEmpty()) {
                String combinedToken = token + nextToken;
                Token t = new Token(combinedToken, lineNumber, findType(combinedToken, lineNumber));
                tokensList.add(t);
                tokenCounter++;
            } else {
                Token t = new Token(token, lineNumber, findType(token, lineNumber));
                tokensList.add(t);
                tokenCounter++;
            }
        }
    }

    private static String getNextToken() {
        if (tokenCounter < tokensList.size() - 1) {
            tokenCounter++;
            return tokensList.get(tokenCounter).getTokenType();
        }
        return "";
    }

    static String[] SYMBOLS = { "<", "<=", ">", ">=", ".", ";", "*", ":", "(", ")", ":=", "+", "-", "/", "=", ",", "=",
			"|=" };
    private static boolean isSymbol(String word) {
        for (String symbol : SYMBOLS) {
            if (word.equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    static String[] RESERVED_WORDS = { "begin", "end", "const", "var", "real", "char", "mod", "div", "readint",
			"readchar", "readln", "writeint", "writereal", "writechar", "writeln", "then", "readreal", "end", "if",
			"elseif", "else", "module", "while", "integer", "do", "procedure", "end", "loop", "until", "exit", "call" };

    private static boolean isKeyword(String word) {
        for (String keyword : RESERVED_WORDS) {
            if (word.equals(RESERVED_WORDS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIdentifier(String word) {
        Pattern pattern = Pattern.compile("^[a-zA-Z]+[a-zA-Z0-9]*$");
        Matcher matcher = pattern.matcher(word);
        return matcher.matches();
    }

    private static boolean isNumericValue(String word) {
        try {
            Double.parseDouble(word);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String findType(String text, int line) {
        if (isKeyword(text)) {
            return "keyword";
        } else if (isSymbol(text)) {
            return "symbol";
        } else if (isNumericValue(text)) {
            return "integer";
        } else if (isIdentifier(text)) {
            return "identifier";
        } else {
            reportError("unknown token: " + text + " in line " + line);
            return "unknown";
        }
    }

    private static void reportError(String message) {
        throw new RuntimeException("Error: " + message);
    }
}
