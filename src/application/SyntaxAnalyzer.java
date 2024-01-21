package application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyntaxAnalyzer {
	private List<Token> tokenList;
	private int currentIndex;
	private String procedureName;

	public SyntaxAnalyzer(List<Token> tokens) {
		this.tokenList = tokens;
		this.currentIndex = 0;
	}

	public void analyzeSyntax() {
		processModuleDeclaration();
	}

	private Token getCurrentToken() {
		return tokenList.get(currentIndex);
	}

	private void advanceToken() {
		currentIndex++;
	}

	private void processModuleDeclaration() {
		processModuleHeader();
		declareVariables();
		processProcedureDeclaration();
		processCodeBlock();
		analyzeModuleName();
		if (!tokenList.get(currentIndex - 1).getTextValue().equals(currentModuleName)) {
			error("Module name in the header and at the end does not match!");
		}

		if (currentIndex == tokenList.size()) {
			error("Missing '.' at the end of the program in line " + tokenList.get(currentIndex - 1).getLineNumber());
		}
		match(".");
		if (currentIndex < tokenList.size()) {
			error("Code must end after '.', but additional code found in line "
					+ tokenList.get(currentIndex).getLineNumber());
		}
	}

	private static String currentModuleName = "";

	private void processModuleHeader() {
		match("module");
		currentModuleName = tokenList.get(currentIndex).getTextValue();
		analyzeModuleName();
		match(";");
	}

	private void analyzeModuleName() {
		// TODO Auto-generated method stub

	}

	private void declareVariables() {
		processConstDeclaration();
		processVarDeclaration();
	}

	Set<String> FIRST_CONST_DECLARE = new HashSet<>(Arrays.asList("const"));
	Set<String> FOLLOW_CONST_DECLARE = new HashSet<>(Arrays.asList("var", "procedure", "begin"));

	private void processConstDeclaration() {
		if (FIRST_CONST_DECLARE.contains(getCurrentToken().getTextValue())) {
			match("const");
			processConstList();
		} else if (FOLLOW_CONST_DECLARE.contains(getCurrentToken().getTextValue())) {

		} else {
			int line = getCurrentToken().getLineNumber();
			error("Unexpected token " + tokenList.get(currentIndex).getTextValue() + " in const-declaration at line "
					+ line);
		}
	}

	Set<String> FIRST_VAR_DECLARE = new HashSet<>(Arrays.asList("var"));
	Set<String> FOLLOW_VAR_DECLARE = new HashSet<>(Arrays.asList("procedure", "begin"));

	private void processVarDeclaration() {
		if (FIRST_VAR_DECLARE.contains(getCurrentToken().getTextValue())) {
			match("var");
			processVarList();
		} else if (FOLLOW_VAR_DECLARE.contains(getCurrentToken().getTextValue())) {

		} else {
			int line = getCurrentToken().getLineNumber();
			error("Unexpected token in var-declaration at line " + line);
		}
	}

	private void processConstList() {
		while (tokenList.get(currentIndex).getTokenType().equals("name")) {
			analyzeSyntaxName();
			match("=");
			processDataType();
			match(";");
		}
	}

	private void processVarList() {
		while (tokenList.get(currentIndex).getTokenType().equals("name")) {
			processVarItem();
			match(";");
		}
	}

	private void processVarItem() {
		processNameList();
		match(":");
		processDataType();
	}

	private void processDataType() {
		if (tokenList.get(currentIndex).getTextValue().equals("integer")) {
			match("integer");
		} else if (tokenList.get(currentIndex).getTextValue().equals("real")) {
			match("real");
		} else if (tokenList.get(currentIndex).getTextValue().equals("char")) {
			match("char");
		} else {
			Token current = tokenList.get(currentIndex);
			int line = findLine(current);
			error("Data type " + tokenList.get(currentIndex).getTextValue() + " is invalid in line: " + line);
		}
	}

	private void processNameList() {
		analyzeSyntaxName();
		while (tokenList.get(currentIndex).getTextValue().equals(",")) {
			currentIndex++;
			analyzeSyntaxName();
		}
	}

	private void processProcedureDeclaration() {
		String currentProcedureName = "";
		procedureHeading();
		declareVariables();
		processCodeBlock();
		analyzeModuleName();
		if (!tokenList.get(currentIndex - 1).getTextValue().equals(currentProcedureName)) {
			error("Procedure name in procedure header and at the end does not match!");
		}
		match(";");
	}

	private void processCodeBlock() {
		match("begin");
		processStatementList();
		match("end");
	}

	private void processStatementList() {
		processStatement();

		while (tokenList.get(currentIndex).getTextValue().equals(";")) {
			match(";");
			processStatement();
		}
	}

	Set<String> FIRST_STATEMENT = new HashSet<>(Arrays.asList("name", "readint", "readreal", "readchar", "readln",
			"writeint", "writereal", "writechar", "writeln", "if", "while", "loop", "exit", "call"));

	Set<String> FOLLOW_STATEMENT = new HashSet<>(Arrays.asList(";", "elseif", "else", "end", "until"));

	private void processStatement() {
		if (FIRST_STATEMENT.contains(getCurrentToken().getTextValue())
				|| getCurrentToken().getTokenType().equals("name")) {
			if (tokenList.get(currentIndex).getTokenType().equals("name")) {
				processAssignmentStatement();
			} else if (tokenList.get(currentIndex).getTextValue().equals("readint")) {
				match("readint");
				match("(");
				processNameList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("readreal")) {
				match("readreal");
				match("(");
				processNameList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("readchar")) {
				match("readchar");
				match("(");
				processNameList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("readln")) {
				match("readln");
			} else if (tokenList.get(currentIndex).getTextValue().equals("writeint")) {
				match("writeint");
				match("(");
				processNameList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("writereal")) {
				match("writereal");
				match("(");
				processNameList();
				match(")");

			} else if (tokenList.get(currentIndex).getTextValue().equals("writechar")) {
				match("writechar");
				match("(");
				processNameList();
				match(")");
			} else if (tokenList.get(currentIndex).getTextValue().equals("writeln")) {
				match("writeln");

			} else if (tokenList.get(currentIndex).getTextValue().equals("while")) {
				match("while");
				condition();
				match("do");
				processStatementList();

				match("end");
			} else if (tokenList.get(currentIndex).getTextValue().equals("if")) {
				match("if");
				condition();
				match("then");
				processStatementList();
				elseIfPart();
				elsePart();
				match("end");
			} else if (tokenList.get(currentIndex).getTextValue().equals("loop")) {
				match("loop");
				processStatementList();
				match("until");
				condition();
			} else if (tokenList.get(currentIndex).getTextValue().equals("exit")) {
				match("exit");
			} else if (tokenList.get(currentIndex).getTextValue().equals("call")) {
				match("call");
				if (!currentProcedureName.equals(tokenList.get(currentIndex).getTextValue())) {
					int line = getCurrentToken().getLineNumber();
					error("procedure name after call " + getCurrentToken().getTextValue() + " in line  " + line
							+ " not match the procedure name");
				}
				analyzeSyntaxName();
			}

		} else if (FOLLOW_STATEMENT.contains(getCurrentToken().getTextValue())) {

		} else {
			int line = getCurrentToken().getLineNumber();
			error("Unexpected " + getCurrentToken().getTextValue() + " token in statement at line " + line);
		}
	}

	// ... other methods

	private void processAssignmentStatement() {
		analyzeSyntaxName();
		match(":=");
		processExpression();
	}

	private void processExpression() {
		processVarItem();
		processExpressionRecursive();
	}

	Set<String> FIRST_ADD_OPERAND = new HashSet<>(Arrays.asList("+", "-"));

	private void processExpressionRecursive() {
		if (FIRST_ADD_OPERAND.contains(getCurrentToken().getTextValue())) {
			processAdditionOperator();
			processVarItem();
			processExpressionRecursive();
		}
	}

	private void processAdditionOperator() {

	}

	private void term() {
		factor();
		termRecursive();
	}

	Set<String> FIRST_MUL_OPERAND = new HashSet<>(Arrays.asList("*", "/", "mod", "div"));

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
							error("error value " + tokenList.get(currentIndex).getTextValue()
									+ " is not valid in line : " + line);
						}
					}
				} else {
					Token current = tokenList.get(currentIndex);
					int line = findLine(current);
					error("error value " + tokenList.get(currentIndex).getTextValue() + "is not valid in line : "
							+ line);

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
			error("ecpext ( " + expected + " ) but found ( " + tokenList.get(currentIndex).getTextValue()
					+ " ) in line " + line);
		}
	}

	void error(String message) {
		throw new RuntimeException("Error: " + message);
	}

	int findLine(Token t) {
		return tokenList.get(currentIndex).getLineNumber();
	}
}
