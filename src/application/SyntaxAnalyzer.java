package application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyntaxAnalyzer {
	private List<Token> tokenList;
	private int currentIndex;
	private static final Set<String> FIRST_CONST_DECLERE = new HashSet<>(Arrays.asList("const"));
	private static final Set<String> FOLLOW_CONST_DECLERE = new HashSet<>(Arrays.asList("var", "procedure", "begin"));

	private static final Set<String> FIRST_VAR_DECL = new HashSet<>(Arrays.asList("var"));
	private static final Set<String> FOLLOW_VAR_DECL = new HashSet<>(Arrays.asList("procedure", "begin"));

	private static final Set<String> FIRST_STATEMENT = new HashSet<>(
			Arrays.asList("name", "readint", "readreal", "readchar", "readln", "writeint", "writereal", "writechar",
					"writeln", "if", "while", "loop", "exit", "call"));

	private static final Set<String> FOLLOW_STATEMENT = new HashSet<>(
			Arrays.asList(";", "elseif", "else", "end", "until"));

	private static final Set<String> FIRST_ADD_OPERAND = new HashSet<>(Arrays.asList("+", "-"));
	private static final Set<String> FIRST_MUL_OPERAND = new HashSet<>(Arrays.asList("*", "/", "mod", "div"));

	private static final Set<String> FIRST_ELSE = new HashSet<>(Arrays.asList("else"));
	private static final Set<String> FOLLOW_ELSE = new HashSet<>(Arrays.asList("end"));
	private static String moduleName = "";
	private static String procedureName = "";

	public SyntaxAnalyzer(List<Token> tokenList) {
		this.tokenList = tokenList;
		this.currentIndex = 0;
	}

	public void analyzeSyntax() {
		moduleDeclaration();

	}

	private Token getCurrentToken() {
		return tokenList.get(currentIndex);
	}

	private void advanceToken() {
		currentIndex++;
	}

	private void moduleDeclaration() {
		moduleHeader();
		declareVariables();
		procedureDeclaration();
		codeBlock();
		analyzeSyntaxName();
		if (!tokenList.get(currentIndex - 1).getTextValue().equals(moduleName)) {
			error("module name in heading and in the ending is not mathced !");
		}

		if (currentIndex == tokenList.size()) {
			error("missing . at end of programm in line " + tokenList.get(currentIndex - 1).getLineNumber());
		}
		match(".");
		if (currentIndex < tokenList.size()) {
			error("code must be ended after . but found addtional code in line "
					+ tokenList.get(currentIndex).getLineNumber());
		}

	}

	private void moduleHeader() {
		match("module");
		moduleName = tokenList.get(currentIndex).getTextValue();
		analyzeSyntaxName();
		match(";");

	}

	private void declareVariables() {
		constDecl();
		varDecl();
	}

	private void constDecl() {
		if (FIRST_CONST_DECLERE.contains(getCurrentToken().getTextValue())) {
			match("const");
			constList();
		} else if (FOLLOW_CONST_DECLERE.contains(getCurrentToken().getTextValue())) {

		} else {
			int line = getCurrentToken().getLineNumber();
			error("Unexpected token " + tokenList.get(currentIndex).getTextValue() + " in const-decl at line " + line);
		}
	}

	private void varDecl() {
		if (FIRST_VAR_DECL.contains(getCurrentToken().getTextValue())) {
			match("var");
			varList();
		} else if (FOLLOW_VAR_DECL.contains(getCurrentToken().getTextValue())) {

		} else {

			int line = getCurrentToken().getLineNumber();
			error("Unexpected token in var-decl at line " + line);
		}
	}

	private void constList() {
		while (tokenList.get(currentIndex).getTokenType().equals("name")) {
			analyzeSyntaxName();
			match("=");
			value();
			match(";");
		}
	}

	private void varList() {
		while (tokenList.get(currentIndex).getTokenType().equals("name")) {
			varItem();
			match(";");

		}
	}

	private void varItem() {
		nameList();
		match(":");
		dataType();
	}

	void dataType() {
		if (tokenList.get(currentIndex).getTextValue().equals("integer")) {
			match("integer");
		} else if (tokenList.get(currentIndex).getTextValue().equals("real")) {
			match("real");
		} else if (tokenList.get(currentIndex).getTextValue().equals("char")) {
			match("char");
		} else {
			Token current = tokenList.get(currentIndex);
			int line = findLine(current);
			error("data type " + tokenList.get(currentIndex).getTextValue() + " is unvalid in line: " + line);

		}
	}

	private void nameList() {
		analyzeSyntaxName();
		while (tokenList.get(currentIndex).getTextValue().equals(",")) {
			currentIndex++;
			analyzeSyntaxName();
		}

	}

	private void procedureDeclaration() {
		procedureHeading();
		declareVariables();
		codeBlock();
		analyzeSyntaxName();
		if (!tokenList.get(currentIndex - 1).getTextValue().equals(procedureName)) {
			error("procedure name in procedure begging and in the procedure ending is not mathced !");
		}
		match(";");

	}

	private void codeBlock() {
		match("begin");
		statementList();
		match("end");
	}

	private void statementList() {
		statement();

		while (tokenList.get(currentIndex).getTextValue().equals(";")) {

			match(";");
			statement();
		}
	}

	private void statement() {
		if (FIRST_STATEMENT.contains(getCurrentToken().getTextValue()) || getCurrentToken().getTokenType().equals("name")) {

			if (tokenList.get(currentIndex).getTokenType().equals("name")) {

				assStmt();
			} else if (tokenList.get(currentIndex).getTextValue().equals("readint")) {
				match("readint");
				match("(");
				nameList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("readreal")) {
				match("readreal");
				match("(");
				nameList();
				match(")");

			} else if (tokenList.get(currentIndex).getTextValue().equals("readchar")) {
				match("readchar");
				match("(");
				nameList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("readln")) {
				match("readln");
			} else if (tokenList.get(currentIndex).getTextValue().equals("writeint")) {
				match("writeint");
				match("(");
				writeList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("writereal")) {
				match("writereal");
				match("(");
				writeList();
				match(")");

			} else if (tokenList.get(currentIndex).getTextValue().equals("writechar")) {
				match("writechar");
				match("(");
				writeList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("writeln")) {
				match("writeln");

			} else if (tokenList.get(currentIndex).getTextValue().equals("while")) {
				match("while");
				condition();
				match("do");
				statementList();

				match("end");
			} else if (tokenList.get(currentIndex).getTextValue().equals("if")) {
				match("if");
				condition();
				match("then");
				statementList();
				elseIfPart();
				elsePart();
				match("end");
			} else if (tokenList.get(currentIndex).getTextValue().equals("loop")) {
				match("loop");
				statementList();
				match("until");
				condition();
			} else if (tokenList.get(currentIndex).getTextValue().equals("exit")) {
				match("exit");
			} else if (tokenList.get(currentIndex).getTextValue().equals("call")) {
				match("call");
				if(!procedureName.equals(tokenList.get(currentIndex).getTextValue())) {
					int line = getCurrentToken().getLineNumber();
					error("procedure name after call " + getCurrentToken().getTextValue() + " in line  " + line+" not match the procedure name");
				}
				analyzeSyntaxName();
			}

		} else if (FOLLOW_STATEMENT.contains(getCurrentToken().getTextValue())) {

		} else {
			int line = getCurrentToken().getLineNumber();
			error("Unexpected " + getCurrentToken().getTextValue() + " token in statement at line " + line);
		}
	}

	private void elseIfPart() {
		while (tokenList.get(currentIndex).getTextValue().equals("elseif")) {
			match("elseif");
			condition();
			match("then");
			statementList();
		}
	}

	private void elsePart() {
		if (FIRST_ELSE.contains(getCurrentToken().getTextValue())) {
			match("else");
			statementList();
		} else if (FOLLOW_ELSE.contains(getCurrentToken().getTextValue())) {

		} else {
			int line = getCurrentToken().getLineNumber();
			error("Unexpected token in else-part at line " + line);
		}
	}

	private void writeList() {
		writeItem();
		while (tokenList.get(currentIndex).getTextValue().equals(",")) {
			match(",");
			writeItem();
		}
	}

	private void writeItem() {
		if (tokenList.get(currentIndex).getTokenType().equals("name")) {
			analyzeSyntaxName();
		} else if (tokenList.get(currentIndex).getTokenType().equals("intger")
				|| tokenList.get(currentIndex).getTokenType().equals("real")) {
			value();
		} else {
			int line = getCurrentToken().getLineNumber();
			error("unvalid write item " + getCurrentToken().getTextValue() + "  in line " + line);
		}
	}

	private void condition() {
		nameValue();
		realtionalOper();
		nameValue();
	}

	private void nameValue() {
		if (tokenList.get(currentIndex).getTokenType().equals("name")) {
			analyzeSyntaxName();
		} else if (tokenList.get(currentIndex).getTokenType().equals("intger")
				|| tokenList.get(currentIndex).getTokenType().equals("real")) {
			value();
		} else {
			int line = getCurrentToken().getLineNumber();
			error("unvalid name or value " + getCurrentToken().getTextValue() + "  in line " + line);
		}
	}

	private void realtionalOper() {
		if (tokenList.get(currentIndex).getTextValue().equals("=")) {
			match("=");
		} else if (tokenList.get(currentIndex).getTextValue().equals("|=")) {
			match("|=");
		} else if (tokenList.get(currentIndex).getTextValue().equals("<")) {
			match("<");
		} else if (tokenList.get(currentIndex).getTextValue().equals("<=")) {
			match("<=");
		} else if (tokenList.get(currentIndex).getTextValue().equals(">")) {
			match(">");
		} else if (tokenList.get(currentIndex).getTextValue().equals(">=")) {
			match(">=");
		} else {
			int line = getCurrentToken().getLineNumber();
			error("openaration (" + getCurrentToken().getTextValue() + ")  at line " + line + " is unvalid");
		}
	}

	private void assStmt() {
		analyzeSyntaxName();
		match(":=");
		exp();
	}

	private void exp() {
		term();
		expRecursive();
	}

	private void expRecursive() {

		if (FIRST_ADD_OPERAND.contains(getCurrentToken().getTextValue())) {
			addOper();
			term();
			expRecursive();
		}

	}

	private void term() {
		factor();
		termRecursive();
	}

	private void termRecursive() {
		if (FIRST_MUL_OPERAND.contains(getCurrentToken().getTextValue())) {
			mulOper();
			factor();
			termRecursive();
		}

	}

	private void addOper() {
		if (getCurrentToken().getTextValue().equals("+")) {
			match("+");
		} else if (getCurrentToken().getTextValue().equals("-")) {
			match("-");
		} else {
			int line = getCurrentToken().getLineNumber();
			error("unvalid add operation " + getCurrentToken().getTextValue() + "  in line " + line);

		}
	}

	private void mulOper() {
		if (getCurrentToken().getTextValue().equals("*")) {
			match("*");
		} else if (getCurrentToken().getTextValue().equals("/")) {
			match("/");
		} else if (getCurrentToken().getTextValue().equals("mod")) {
			match("mod");
		} else if (getCurrentToken().getTextValue().equals("div")) {
			match("div");
		} else {
			int line = getCurrentToken().getLineNumber();
			error("unvalid mul operation " + getCurrentToken().getTextValue() + "  in line " + line);
		}
	}

	private void factor() {
		if (tokenList.get(currentIndex).getTextValue().equals("(")) {
			match("(");
			exp();
			match(")");
		} else if (tokenList.get(currentIndex).getTokenType().equals("name")) {
			analyzeSyntaxName();
		} else if (tokenList.get(currentIndex).getTokenType().equals("intger")
				|| tokenList.get(currentIndex).getTokenType().equals("real")) {
			value();
		} else {
			int line = getCurrentToken().getLineNumber();
			error("unvalid factor " + getCurrentToken().getTextValue() + "  in line " + line);

		}
	}

	private void procedureHeading() {
		match("procedure");
		procedureName = tokenList.get(currentIndex).getTextValue();
		analyzeSyntaxName();
		match(";");
	}

	void analyzeSyntaxName() {

		String name = tokenList.get(currentIndex).getTextValue();

		char chars[] = name.toCharArray();
		int i = 0;
		if (isLetter(chars[i])) {
			i++;
			for (i = 1; i < chars.length; i++) {
				if (isLetter(chars[i]) || isDigit(chars[i])) {

				} else {
					Token current = tokenList.get(currentIndex);
					int line = findLine(current);
					error("name not allowed in line " + line);
				}
			}

		} else {
			Token current = tokenList.get(currentIndex);
			int line = findLine(current);
			if (tokenList.get(currentIndex - 1).equals("module")) {
				error("module name is not allowed , in line: " + line);
			} else {
				error("variable or constant or procedure name (" + tokenList.get(currentIndex).getTextValue()
						+ ") is starting with digit ot unknown simbol wich is  not allowed, in line:  " + line);
			}
		}

		currentIndex++;

	}

	boolean isLetter(char c) {
		return Character.isLetter(c);
	}

	boolean isDigit(char c) {
		return Character.isDigit(c);
	}

	void value() {
		if (tokenList.get(currentIndex).getTokenType().equals("intger")) {
			intger();
		} else if (tokenList.get(currentIndex).getTokenType().equals("real")) {

			real();
		} else {
			int line = getCurrentToken().getLineNumber();
			error("unvalid value  " + getCurrentToken().getTextValue() + "  in line " + line);
		}

	}

	void intger() {
		String s = tokenList.get(currentIndex).getTextValue();
		char arr[] = s.toCharArray();
		int i = 0;
		if (isDigit(arr[i])) {
			for (i = 1; i < arr.length; i++) {
				if (isDigit(arr[i])) {

				} else {
					Token current = tokenList.get(currentIndex);
					int line = findLine(current);
					error("intger value " + s + " is not digits in line: " + line);
				}
			}
		} else {
			Token current = tokenList.get(currentIndex);
			int line = findLine(current);
			error("intger value " + s + " is not digits in line: " + line);
		}
		currentIndex++;

	}

	void real() {
		String s = tokenList.get(currentIndex).getTextValue();
		char arr[] = s.toCharArray();
		int i = 0;
		boolean flag = false;
		if (isDigit(arr[0])) {
			for (i = 1; i < arr.length; i++) {
				if (isDigit(arr[i]) || arr[i] == '.') {
					if (flag == true && arr[i] == '.') {
						Token current = tokenList.get(currentIndex);
						int line = findLine(current);
						error("tow . in this real value " + tokenList.get(currentIndex).getTextValue() + " in line : "
								+ line);
					}
					if (arr[i] == '.') {
						if (i < arr.length - 1 && isDigit(arr[i + 1])) {
							flag = true;
						} else {
							Token current = tokenList.get(currentIndex);
							int line = findLine(current);
							error("error value " + tokenList.get(currentIndex).getTextValue() + " is not valid in line : "
									+ line);
						}
					}
				} else {
					Token current = tokenList.get(currentIndex);
					int line = findLine(current);
					error("error value " + tokenList.get(currentIndex).getTextValue() + "is not valid in line : " + line);

				}
			}
		} else {
			Token current = tokenList.get(currentIndex);
			int line = findLine(current);
			error("error value " + tokenList.get(currentIndex).getTextValue() + "is not valid in line : " + line);
		}
		currentIndex++;
	}

	private void match(String expected) {
		if (tokenList.get(currentIndex).getTextValue().equals(expected)) {
			currentIndex++;
		} else {
			Token current = tokenList.get(currentIndex);
			int line = findLine(current);
			error("ecpext ( " + expected + " ) but found ( " + tokenList.get(currentIndex).getTextValue() + " ) in line "
					+ line);
		}
	}

	void error(String message) {
		throw new RuntimeException("Error: " + message);
	}

	int findLine(Token t) {
		return tokenList.get(currentIndex).getLineNumber();
	}

}
