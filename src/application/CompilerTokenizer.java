package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompilerTokenizer {

	static int tokenCounter = 0;
	static ArrayList<Token> customTokenList = new ArrayList<>();

	public static void processFile(String fileName) throws IOException {
		String line = "";
		int lineNumber = 0;
		BufferedReader fileReader = new BufferedReader(new FileReader(fileName));

		while ((line = fileReader.readLine()) != null) {
			lineNumber++;
			String[] tokens = line.split("\\s+|(?<=[^a-zA-Z0-9\\.])|(?=[^a-zA-Z0-9\\.])");

			for (int i = 0; i < tokens.length; i++) {
				String currentToken = tokens[i].trim();

				if (!currentToken.isEmpty()) {
					if (!currentToken.equals(":") && !currentToken.equals("<") && !currentToken.equals(">")
							&& !currentToken.equals("|")) {
						if (currentToken.contains(".")) {
							char[] tokenChars = currentToken.toCharArray();

							if (Character.isDigit(currentToken.charAt(0))) {
								Token realToken = new Token(currentToken, lineNumber, "Real");
								customTokenList.add(realToken);
								continue;
							} else {
								currentToken = currentToken.substring(0, currentToken.length() - 1);
								Token nameToken = new Token(currentToken, lineNumber, "Name");
								Token dotToken = new Token(".", lineNumber, "Symbol");
								customTokenList.add(nameToken);
								customTokenList.add(dotToken);
								continue;
							}
						}

						Token newToken = new Token(currentToken, lineNumber,
								determineTokenType(currentToken, lineNumber));
						customTokenList.add(newToken);
						tokenCounter++;
					} else {
						if (currentToken.equals(":")) {
							if ((i + 1) < tokens.length && tokens[i + 1].equals("=")) {
								i++;
								String combinedToken = currentToken + tokens[i];
								Token combinedTokenObj = new Token(combinedToken, lineNumber,
										determineTokenType(combinedToken, lineNumber));
								customTokenList.add(combinedTokenObj);
								tokenCounter++;
							} else {
								Token singleToken = new Token(currentToken, lineNumber,
										determineTokenType(currentToken, lineNumber));
								customTokenList.add(singleToken);
								tokenCounter++;
							}
						} else if (currentToken.equals("<") || currentToken.equals(">") || currentToken.equals("|")) {
							if ((i + 1) < tokens.length && tokens[i + 1].equals("=")) {
								i++;
								String combinedToken = currentToken + tokens[i];
								Token combinedTokenObj = new Token(combinedToken, lineNumber,
										determineTokenType(combinedToken, lineNumber));
								customTokenList.add(combinedTokenObj);
								tokenCounter++;
							} else {
								Token singleToken = new Token(currentToken, lineNumber,
										determineTokenType(currentToken, lineNumber));
								customTokenList.add(singleToken);
								tokenCounter++;
							}
						}
					}
				}
			}
		}

		fileReader.close();
	}

	private static String determineTokenType(String tokenText, int line) {
		String[] KEYWORDS = { "writeln", "then", "end", "const", "var", "integer", "while", "do", "end", "real", "char",
				"procedure", "mod", "div", "readint", "readreal", "readchar", "readln", "writeint", "writereal",
				"writechar", "if", "elseif", "else", "module", "begin", "end", "loop", "until", "exit", "call" };
		for (String reservedWord : KEYWORDS) {
			if (tokenText.equals(reservedWord)) {
				return "ReservedWordType";
			}
		}

		String[] symbols = { "<", "<=", ">", ">=", "*", ":", "(", ")", ":=", "+", "-", "/", ".", ";", "=", ",", "=",
				"|=" };
		for (String symbol : symbols) {
			if (tokenText.equals(symbol)) {
				return "SymbolType";
			}
		}

		Pattern namePattern = Pattern.compile("^[a-zA-Z]+[a-zA-Z0-9]*$");
		Matcher nameMatcher = namePattern.matcher(tokenText);
		if (nameMatcher.matches()) {
			return "IdentifierType";
		}

		try {
			Double.parseDouble(tokenText);
			return "IntegerType";
		} catch (NumberFormatException e) {
			throw new RuntimeException("Error: Unknown token encountered - " + tokenText + " at line " + line);
		}
	}

}
