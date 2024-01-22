package application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyntaxAnalyzer {
	List<Token> myCustomTokenList;
	int myCustomTokenIndex;

	String myCustomModule = "";

	private void validateModuleName() {
		String moduleName = myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue();

		if (!moduleName.matches("[a-zA-Z][a-zA-Z0-9]*")) {
			Token current = myCustomTokenList.get(myCustomTokenIndex - 1);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: Invalid module name '" + moduleName + "' in line " + line);
		}

		System.out.println("Placeholder: Module Name Validation");
	}

	private void moduleHeading() {
		String moduleKeyword = "module";
		String expected = moduleKeyword;

		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: expected ( " + expected + " ) but found ( "
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
		}

		String moduleName;

		String name = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
		char chars[] = name.toCharArray();
		int i = 0;

		if (Character.isLetter(chars[i])) {
			i++;

			for (i = 1; i < chars.length; i++) {
				if (Character.isLetterOrDigit(chars[i])) {

				} else {
					Token current = myCustomTokenList.get(myCustomTokenIndex);
					int line = current.getLineNumber();
					String errorMessage;

					if (i == 1 && myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals(moduleKeyword)) {
						errorMessage = "Error: " + moduleKeyword + " name is not allowed, in line: " + line;
					} else {
						errorMessage = "Error: variable or constant or procedure name ("
								+ myCustomTokenList.get(myCustomTokenIndex).getTextValue()
								+ ") is starting with digit or unknown symbol which is not allowed, in line: " + line;
					}

					throw new RuntimeException(errorMessage);
				}
			}

		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			String errorMessage = "Error: name not allowed in line " + line;
			throw new RuntimeException(errorMessage);
		}

		moduleName = myCustomTokenList.get(myCustomTokenIndex).getTextValue();

		expected = ";";
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: expected ( " + expected + " ) but found ( "
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
		}
	}

	Set<String> firstConstant = new HashSet<>(Arrays.asList("const"));
	Set<String> followConstant = new HashSet<>(Arrays.asList("var", "procedure", "begin"));

	public SyntaxAnalyzer(List<Token> tokens) {
		this.myCustomTokenList = tokens;
		this.myCustomTokenIndex = 0;
	}

	public void parse() {
		checkForPeriod();

		moduleHeading();
		constDecl();
		varDecl();
		System.out.println("Placeholder: Procedure Declaration");
		block();
		validateModuleName();

		checkForPeriod();

		if (!myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals(myCustomModule)) {
			throw new RuntimeException("Error: module name does not match!");
		}

		validateEndOfProgram();
		validateNoAdditionalCode();
	}

	private void checkForPeriod() {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(".")) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: expected . but found ( "
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
		}
	}

	private void validateEndOfProgram() {
		if (myCustomTokenIndex == myCustomTokenList.size()) {
			throw new RuntimeException("Error: " + "missing . at the end of the program in line "
					+ myCustomTokenList.get(myCustomTokenIndex - 1).getLineNumber());
		}

		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(".")) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: " + "expected . but found ( "
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
		}
	}

	private void validateNoAdditionalCode() {
		if (myCustomTokenIndex < myCustomTokenList.size()) {
			throw new RuntimeException("Error: " + "code must end after . but found additional code in line "
					+ myCustomTokenList.get(myCustomTokenIndex).getLineNumber());
		}
	}

	private Token getCurrentToken() {
		return myCustomTokenList.get(myCustomTokenIndex);
	}

	private void constDecl() {
		String expectedToken = "const";

		if (firstConstant.contains(getCurrentToken().getTextValue())) {
			if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expectedToken)) {
				myCustomTokenIndex++;
				constList();
			} else {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException("Error: " + "Expected token ( " + expectedToken + " ) but found ( "
						+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
			}
		} else if (followConstant.contains(getCurrentToken().getTextValue())) {
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException("Error: " + "Unexpected token "
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " in const-decl at line " + line);
		}
	}

	Set<String> firstVariable = new HashSet<>(Arrays.asList("var"));
	Set<String> followVariable = new HashSet<>(Arrays.asList("procedure", "begin"));

	private void varDecl() {
		String expected = "var";

		if (firstVariable.contains(getCurrentToken().getTextValue())) {
			if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
				myCustomTokenIndex++;
			} else {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException("Error: " + "Expected token ( " + expected + " ) but found ( "
						+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
			}

			varList();
		} else if (followVariable.contains(getCurrentToken().getTextValue())) {
			System.out.println("Following var declaration, but no action taken.");

			while (!firstVariable.contains(getCurrentToken().getTextValue())
					&& !followVariable.contains(getCurrentToken().getTextValue())) {
				myCustomTokenIndex++;
			}
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException("Error: " + "Unexpected token in var-decl at line " + line);
		}
	}

	private void constList() {
		while (myCustomTokenList.get(myCustomTokenIndex).getTokenType().equals("name")) {
			String expectedName = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
			myCustomTokenIndex++;

			if (!myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("=")) {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException("Error: expected ( = ) but found ( "
						+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
			}
			myCustomTokenIndex++;

			value();

			if (!myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(";")) {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException("Error: expected ( ; ) but found ( "
						+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
			}
			myCustomTokenIndex++;
		}
	}

	private void varList() {
		while (myCustomTokenList.get(myCustomTokenIndex).getTokenType().equals("name")) {
			String expected = ";";

			if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
				myCustomTokenIndex++;
			} else {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException("Error: " + "expecting ( " + expected + " ) but found ( "
						+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
			}

			varItem(expected);
		}
	}

	private void varItem(String expected) {
		nameList();

		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: " + "expected ( " + expected + " ) but found ( " + current.getTextValue()
					+ " ) in line " + line);
		}

		dataType();
	}

	void dataType(String expected) {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: " + "expected data type '" + expected + "' but found '"
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + "' in line " + line);
		}
	}

	void dataType() {
		String dataTypeToken = myCustomTokenList.get(myCustomTokenIndex).getTextValue();

		if ("integer".equals(dataTypeToken) || "real".equals(dataTypeToken) || "char".equals(dataTypeToken)) {
			dataType(dataTypeToken);
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: " + "data type " + dataTypeToken + " is invalid in line: " + line);
		}
	}

	private void nameList() {
		String itemName = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
		char itemChars[] = itemName.toCharArray();
		int itemIndex = 0;

		if (Character.isLetter(itemChars[itemIndex])) {
			itemIndex++;

			for (; itemIndex < itemChars.length; itemIndex++) {
				if (Character.isLetterOrDigit(itemChars[itemIndex])) {

				} else {
					Token current = myCustomTokenList.get(myCustomTokenIndex);
					int line = current.getLineNumber();
					String errorMessage;

					if (itemIndex == 1
							&& myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals("module")) {
						errorMessage = "Error: module name is not allowed, in line: " + line;
					} else {
						errorMessage = "Error: variable or constant or procedure name ("
								+ myCustomTokenList.get(myCustomTokenIndex).getTextValue()
								+ ") is starting with digit or unknown symbol which is not allowed, in line: " + line;
					}

					throw new RuntimeException(errorMessage);
				}
			}

		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			String errorMessage = "Error: name not allowed in line " + line;
			throw new RuntimeException(errorMessage);
		}

		myCustomTokenIndex++;

		while (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(",")) {
			myCustomTokenIndex++;
			itemName = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
			itemChars = itemName.toCharArray();
			itemIndex = 0;

			if (Character.isLetter(itemChars[itemIndex])) {
				itemIndex++;

				for (; itemIndex < itemChars.length; itemIndex++) {
					if (Character.isLetterOrDigit(itemChars[itemIndex])) {

					} else {
						Token current = myCustomTokenList.get(myCustomTokenIndex);
						int line = current.getLineNumber();
						String errorMessage;

						if (itemIndex == 1
								&& myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals("module")) {
							errorMessage = "Error: module name is not allowed, in line: " + line;
						} else {
							errorMessage = "Error: variable or constant or procedure name ("
									+ myCustomTokenList.get(myCustomTokenIndex).getTextValue()
									+ ") is starting with digit or unknown symbol which is not allowed, in line: "
									+ line;
						}

						throw new RuntimeException(errorMessage);
					}
				}

			} else {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				String errorMessage = "Error: name not allowed in line " + line;
				throw new RuntimeException(errorMessage);
			}

			myCustomTokenIndex++;
		}
	}

	private void procedureDecl(String expected) {
		String procedureNameFromHeading = extractProcedureName();

		if (!myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals(procedureNameFromHeading)) {
			throw new RuntimeException("Error: Procedure name in procedure beginning and ending does not match!");
		}

		checkNameValidity(procedureNameFromHeading, "procedure");

		if (!myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: Expected ( " + expected + " ) but found ( " + current.getTextValue()
					+ " ) in line " + line);
		}

		myCustomTokenIndex++;
	}

	private static String myCustomProcedure = "";

	private String extractProcedureName() {
		procedureHeading();
		return myCustomProcedure;
	}

	private void checkNameValidity(String name, String type) {
		char chars[] = name.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			if (i == 0 && Character.isDigit(chars[i])) {
				throwNameError(name, type, "starting with a digit");
			} else if (!Character.isLetterOrDigit(chars[i])) {
				throwNameError(name, type, "starting with an unknown symbol");
			}
		}
	}

	private void throwNameError(String name, String type, String reason) {
		Token current = myCustomTokenList.get(myCustomTokenIndex);
		int line = current.getLineNumber();
		throw new RuntimeException("Error: " + type + " name (" + name + ") is " + reason + ", in line: " + line);
	}

	private void procedureDecl() {
		procedureDecl(";");
	}

	private void block() {
		String expected = "begin";

		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: expected ( " + expected + " ) but found ( " + current.getTextValue()
					+ " ) in line " + line);
		}

		stamentList();

		expected = "end";
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: expected ( " + expected + " ) but found ( " + current.getTextValue()
					+ " ) in line " + line);
		}
	}

	private void stamentList() {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(";")) {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: unexpected ';' in line " + line);
		}

		statement();

		while (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(";")) {
			myCustomTokenIndex++;
			statement();
		}
	}

	Set<String> firstStatement = new HashSet<>(Arrays.asList("readint", "readreal", "while", "loop", "readln", "name",
			"writeint", "writereal", "writechar", "writeln", "if", "exit", "readchar", "call"));
	Set<String> followStatement = new HashSet<>(Arrays.asList("until", "elseif", "else", "end", ";"));

	private void statement() {
		if (firstStatement.contains(getCurrentToken().getTextValue())
				|| getCurrentToken().getTokenType().equals("name")) {

			if (getCurrentToken().getTokenType().equals("name")) {
				if (!getCurrentToken().getTextValue().equals(myCustomProcedure)) {
					int line = getCurrentToken().getLineNumber();
					throw new RuntimeException(
							"Error: " + "procedure name after call " + getCurrentToken().getTextValue() + " in line  "
									+ line + " does not match the expected procedure name");
				}

				String name = getCurrentToken().getTextValue();
				char chars[] = name.toCharArray();
				int i = 0;

				if (Character.isLetter(chars[i])) {
					i++;

					for (i = 1; i < chars.length; i++) {
						if (Character.isLetterOrDigit(chars[i])) {

						} else {
							int line = getCurrentToken().getLineNumber();
							String errorMessage;

							if (i == 1
									&& myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals("module")) {
								errorMessage = "Error: module name is not allowed, in line: " + line;
							} else {
								errorMessage = "Error: variable or constant or procedure name ("
										+ getCurrentToken().getTextValue()
										+ ") is starting with digit or unknown symbol which is not allowed, in line: "
										+ line;
							}

							throw new RuntimeException(errorMessage);
						}
					}

				} else {
					int line = getCurrentToken().getLineNumber();
					String errorMessage = "Error: name not allowed in line " + line;
					throw new RuntimeException(errorMessage);
				}

				myCustomTokenIndex++;

			} else if (getCurrentToken().getTextValue().equals("readint")) {
				myCustomTokenIndex++;
				expectAndAdvance("(");
				nameList();
				expectAndAdvance(")");
			} else if (getCurrentToken().getTextValue().equals("readreal")) {
				myCustomTokenIndex++;
				expectAndAdvance("(");
				nameList();
				expectAndAdvance(")");
			} else if (getCurrentToken().getTextValue().equals("readchar")) {
				myCustomTokenIndex++;
				expectAndAdvance("(");
				nameList();
				expectAndAdvance(")");
			} else if (getCurrentToken().getTextValue().equals("readln")) {
				myCustomTokenIndex++;
			} else if (getCurrentToken().getTextValue().equals("writeint")) {
				myCustomTokenIndex++;
				expectAndAdvance("(");
				writeList("");
				expectAndAdvance(")");
			} else if (getCurrentToken().getTextValue().equals("writereal")) {
				myCustomTokenIndex++;
				expectAndAdvance("(");
				writeList("");
				expectAndAdvance(")");
			} else if (getCurrentToken().getTextValue().equals("writechar")) {
				myCustomTokenIndex++;
				expectAndAdvance("(");
				writeList("");
				expectAndAdvance(")");
			} else if (getCurrentToken().getTextValue().equals("writeln")) {
				myCustomTokenIndex++;
			} else if (getCurrentToken().getTextValue().equals("while")) {
				myCustomTokenIndex++;
				condition();
				expectAndAdvance("do");
				stamentList();
				expectAndAdvance("end");
			} else if (getCurrentToken().getTextValue().equals("if")) {
				myCustomTokenIndex++;
				condition();
				expectAndAdvance("then");
				stamentList();
				elseIfPart();
				elsePart();
				expectAndAdvance("end");
			} else if (getCurrentToken().getTextValue().equals("loop")) {
				myCustomTokenIndex++;
				stamentList();
				expectAndAdvance("until");
				condition();
			} else if (getCurrentToken().getTextValue().equals("exit")) {
				myCustomTokenIndex++;
			} else if (getCurrentToken().getTextValue().equals("call")) {
				myCustomTokenIndex++;
				expectProcedureName();

				String name = getCurrentToken().getTextValue();
				char chars[] = name.toCharArray();
				int i = 0;

				if (Character.isLetter(chars[i])) {
					i++;

					for (i = 1; i < chars.length; i++) {
						if (Character.isLetterOrDigit(chars[i])) {

						} else {
							int line = getCurrentToken().getLineNumber();
							String errorMessage;

							if (i == 1
									&& myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals("module")) {
								errorMessage = "Error: module name is not allowed, in line: " + line;
							} else {
								errorMessage = "Error: variable or constant or procedure name ("
										+ getCurrentToken().getTextValue()
										+ ") is starting with digit or unknown symbol which is not allowed, in line: "
										+ line;
							}

							throw new RuntimeException(errorMessage);
						}
					}

				} else {
					int line = getCurrentToken().getLineNumber();
					String errorMessage = "Error: name not allowed in line " + line;
					throw new RuntimeException(errorMessage);
				}

				myCustomTokenIndex++;

			}

		} else if (followStatement.contains(getCurrentToken().getTextValue())) {

		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException("Error: " + "Unexpected " + getCurrentToken().getTextValue()
					+ " token in statement at line " + line);
		}
	}

	private void expectAndAdvance(String expected) {
		if (!myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			handleSyntaxError("Expected '" + expected + "'");
		}
		myCustomTokenIndex++;
	}

	private void expectProcedureName() {
		if (!myCustomProcedure.equals(myCustomTokenList.get(myCustomTokenIndex).getTextValue())) {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException("Error: " + "procedure name after call " + getCurrentToken().getTextValue()
					+ " in line  " + line + " does not match the expected procedure name");
		}
	}

	private void handleSyntaxError(String message) {
		int line = getCurrentToken().getLineNumber();
		throw new RuntimeException("Syntax Error at line " + line + ": " + message);
	}

	private void elseIfPart() {
		while (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("elseif")) {
			if (!myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("elseif")) {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException(
						"Error: expected 'elseif' but found '" + current.getTextValue() + "' in line " + line);
			}
			myCustomTokenIndex++;

			condition();

			if (!myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("then")) {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException(
						"Error: expected 'then' but found '" + current.getTextValue() + "' in line " + line);
			}
			myCustomTokenIndex++;

			stamentList();
		}
	}

	Set<String> firstElse = new HashSet<>(Arrays.asList("else"));
	Set<String> followElse = new HashSet<>(Arrays.asList("end"));

	private void elsePart() {
		String expected = "else";

		if (firstElse.contains(getCurrentToken().getTextValue())) {
			if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
				myCustomTokenIndex++;
				stamentList();
			} else {
				handleMismatch(expected);
			}
		} else if (followElse.contains(getCurrentToken().getTextValue())) {
		} else {
			handleUnexpectedToken();
		}
	}

	private void handleMismatch(String expected) {
		Token current = myCustomTokenList.get(myCustomTokenIndex);
		int line = current.getLineNumber();
		throw new RuntimeException("Error: " + "Expected token ( " + expected + " ) but found ( "
				+ current.getTextValue() + " ) in line " + line);
	}

	private void handleUnexpectedToken() {
		int line = getCurrentToken().getLineNumber();
		throw new RuntimeException("Error: " + "Unexpected token in else-part at line " + line);
	}

	private void writeList(String expected) {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: " + "expected ( " + expected + " ) but found ( "
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
		}

		writeItem();
		while (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(",")) {
			writeList("");
			writeItem();
		}
	}

	private void writeItem() {
		if (isType("name")) {
			String itemName = parseItem();
			// Process the parsed item as needed
		} else if (isType("integer") || isType("real")) {
			parseValue();
			// Process the parsed value as needed
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException(
					"Error: " + "invalid write item " + getCurrentToken().getTextValue() + " in line " + line);
		}
	}

	boolean isType(String type) {
		return myCustomTokenList.get(myCustomTokenIndex).getTokenType().equals(type);
	}

	String parseItem() {
		String name = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
		char chars[] = name.toCharArray();
		int i = 0;

		if (Character.isLetter(chars[i])) {
			i++;

			for (i = 1; i < chars.length; i++) {
				if (Character.isLetterOrDigit(chars[i])) {

				} else {
					Token current = myCustomTokenList.get(myCustomTokenIndex);
					int line = current.getLineNumber();
					String errorMessage;

					if (i == 1 && myCustomTokenList.get(myCustomTokenIndex - 1).getTokenType().equals("module")) {
						errorMessage = "Error: module name is not allowed, in line: " + line;
					} else {
						errorMessage = "Error: variable or constant or procedure name ("
								+ myCustomTokenList.get(myCustomTokenIndex).getTextValue()
								+ ") is starting with digit or unknown symbol which is not allowed, in line: " + line;
					}

					throw new RuntimeException(errorMessage);
				}
			}

		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			String errorMessage = "Error: name not allowed in line " + line;
			throw new RuntimeException(errorMessage);
		}

		myCustomTokenIndex++;
		return name;
	}

	private void condition() {
		nameValue();
		relationalOper();
		nameValue();
	}

	private void nameValue() {
		if (getCurrentToken().getTokenType().equals("identifier")) {
			String identifier = getCurrentToken().getTextValue();
			char chars[] = identifier.toCharArray();
			int i = 0;

			if (Character.isLetter(chars[i])) {
				i++;

				for (i = 1; i < chars.length; i++) {
					if (Character.isLetterOrDigit(chars[i])) {

					} else {
						Token current = getCurrentToken();
						int line = current.getLineNumber();
						String errorMessage;

						if (i == 1 && myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals("module")) {
							errorMessage = "Error: module name is not allowed, in line: " + line;
						} else {
							errorMessage = "Error: variable or constant or procedure name (" + identifier
									+ ") is starting with digit or unknown symbol which is not allowed, in line: "
									+ line;
						}

						throw new RuntimeException(errorMessage);
					}
				}

			} else {
				Token current = getCurrentToken();
				int line = current.getLineNumber();
				String errorMessage = "Error: name not allowed in line " + line;
				throw new RuntimeException(errorMessage);
			}

		} else if (getCurrentToken().getTokenType().equals("integer")
				|| getCurrentToken().getTokenType().equals("real")) {
			parseValue();
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException(
					"Error: " + "invalid name or value " + getCurrentToken().getTextValue() + "  in line " + line);
		}
	}

	private void parseValue() {
		Token currentToken = getCurrentToken();
		String valueType = currentToken.getTokenType();
		String valueText = currentToken.getTextValue();

		if ("integer".equals(valueType)) {
			try {
				int integerValue = Integer.parseInt(valueText);
			} catch (NumberFormatException e) {
				int line = currentToken.getLineNumber();
				throw new RuntimeException("Error: Invalid integer format in line " + line);
			}
		} else if ("real".equals(valueType)) {
			try {
				double realValue = Double.parseDouble(valueText);
			} catch (NumberFormatException e) {
				int line = currentToken.getLineNumber();
				throw new RuntimeException("Error: Invalid real number format in line " + line);
			}
		} else {
			int line = currentToken.getLineNumber();
			throw new RuntimeException("Error: Unexpected value type '" + valueType + "' in line " + line);
		}

		myCustomTokenIndex++;
	}

	private void relationalOper(String expected) {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException("Error: " + "expecting ( " + expected + " ) but found ( "
					+ getCurrentToken().getTextValue() + " ) in line " + line);
		}
	}

	private void relationalOper() {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("=")) {
			relationalOper("=");
		} else if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("|=")) {
			relationalOper("|=");
		} else if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("<")) {
			relationalOper("<");
		} else if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("<=")) {
			relationalOper("<=");
		} else if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(">")) {
			relationalOper(">");
		} else if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(">=")) {
			relationalOper(">=");
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException(
					"Error: " + "operation (" + getCurrentToken().getTextValue() + ") at line " + line + " is invalid");
		}
	}

	private void statement(String expected) {
		String name = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
		char chars[] = name.toCharArray();
		int i = 0;

		if (Character.isLetter(chars[i])) {
			i++;

			for (i = 1; i < chars.length; i++) {
				if (Character.isLetterOrDigit(chars[i])) {

				} else {
					Token current = myCustomTokenList.get(myCustomTokenIndex);
					int line = current.getLineNumber();
					String errorMessage;

					if (i == 1 && myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals("module")) {
						errorMessage = "Error: module name is not allowed, in line: " + line;
					} else {
						errorMessage = "Error: variable or constant or procedure name ("
								+ myCustomTokenList.get(myCustomTokenIndex).getTextValue()
								+ ") is starting with digit or unknown symbol which is not allowed, in line: " + line;
					}

					throw new RuntimeException(errorMessage);
				}
			}

		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			String errorMessage = "Error: name not allowed in line " + line;
			throw new RuntimeException(errorMessage);
		}

		myCustomTokenIndex++;

		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: " + "expected ( " + expected + " ) but found ( "
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
		}

		expression();
	}

	private void expression() {
		term();
		expressionRecursive();
	}

	Set<String> firstAdd = new HashSet<>(Arrays.asList("+", "-"));

	private void expressionRecursive() {

		if (firstAdd.contains(getCurrentToken().getTextValue())) {
			addOper();
			term();
			expressionRecursive();
		}

	}

	private void term() {
		factor();
		termRecursive();
	}

	Set<String> firstMultiplication = new HashSet<>(Arrays.asList("*", "mod", "/", "div"));

	private void termRecursive() {
		if (firstMultiplication.contains(getCurrentToken().getTextValue())) {
			mulOper();
			factor();
			termRecursive();
		}

	}

	private void addOper(String expected) {
		if (getCurrentToken().getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = getCurrentToken();
			int line = current.getLineNumber();
			throw new RuntimeException("Error: " + "expected ( " + expected + " ) but found ( " + current.getTextValue()
					+ " ) in line " + line);
		}
	}

	private void addOper() {
		if (getCurrentToken().getTextValue().equals("+") || getCurrentToken().getTextValue().equals("-")) {
			addOper(getCurrentToken().getTextValue());
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException(
					"Error: " + "invalid add operation " + getCurrentToken().getTextValue() + "  in line " + line);
		}
	}

	private void mulOper(String expected) {
		if (getCurrentToken().getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException("Error: " + "unexpected operator ( " + getCurrentToken().getTextValue()
					+ " ), expected ( " + expected + " ) in line " + line);
		}
	}

	private void mulOper() {
		if (getCurrentToken().getTextValue().equals("*")) {
			mulOper("*");
		} else if (getCurrentToken().getTextValue().equals("/")) {
			mulOper("/");
		} else if (getCurrentToken().getTextValue().equals("mod")) {
			mulOper("mod");
		} else if (getCurrentToken().getTextValue().equals("div")) {
			mulOper("div");
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException("Error: " + "invalid multiplication operation "
					+ getCurrentToken().getTextValue() + "  in line " + line);
		}
	}

	private void factor(String expected) {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException("Error: " + "expected ( " + expected + " ) but found ( "
					+ getCurrentToken().getTextValue() + " ) in line " + line);
		}
	}

	private void factor() {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals("(")) {
			factor("(");
			expression();
			factor(")");
		} else if (myCustomTokenList.get(myCustomTokenIndex).getTokenType().equals("name")) {
			String name = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
			char[] chars = name.toCharArray();
			int i = 0;

			if (Character.isLetter(chars[i])) {
				i++;

				for (; i < chars.length; i++) {
					if (Character.isLetterOrDigit(chars[i])) {
						// Continue processing the name
					} else {
						int line = getCurrentToken().getLineNumber();
						String errorMessage;

						if (i == 1 && myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals("module")) {
							errorMessage = "Error: module name is not allowed, in line: " + line;
						} else {
							errorMessage = "Error: variable or constant or procedure name ("
									+ myCustomTokenList.get(myCustomTokenIndex).getTextValue()
									+ ") is starting with digit or unknown symbol which is not allowed, in line: "
									+ line;
						}

						throw new RuntimeException(errorMessage);
					}
				}

			} else {
				int line = getCurrentToken().getLineNumber();
				String errorMessage = "Error: name not allowed in line " + line;
				throw new RuntimeException(errorMessage);
			}

			myCustomTokenIndex++;
		} else if (myCustomTokenList.get(myCustomTokenIndex).getTokenType().equals("intger")
				|| myCustomTokenList.get(myCustomTokenIndex).getTokenType().equals("real")) {
			value();
		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException(
					"Error: unvalid factor " + getCurrentToken().getTextValue() + " in line " + line);
		}
	}

	private void procedureHeading(String expected) {
		if (myCustomTokenList.get(myCustomTokenIndex).getTextValue().equals(expected)) {
			myCustomTokenIndex++;
		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			throw new RuntimeException("Error: " + "expected ( " + expected + " ) but found ( "
					+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " ) in line " + line);
		}
	}

	private void procedureHeading() {
		String name = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
		char chars[] = name.toCharArray();
		int i = 0;

		if (Character.isLetter(chars[i])) {
			i++;

			for (i = 1; i < chars.length; i++) {
				if (Character.isLetterOrDigit(chars[i])) {

				} else {
					Token current = myCustomTokenList.get(myCustomTokenIndex);
					int line = current.getLineNumber();
					String errorMessage;

					if (i == 1 && myCustomTokenList.get(myCustomTokenIndex - 1).getTextValue().equals("procedure")) {
						errorMessage = "Error: procedure name is not allowed, in line: " + line;
					} else {
						errorMessage = "Error: variable or constant name ("
								+ myCustomTokenList.get(myCustomTokenIndex).getTextValue()
								+ ") is starting with digit or unknown symbol which is not allowed, in line: " + line;
					}

					throw new RuntimeException(errorMessage);
				}
			}

		} else {
			Token current = myCustomTokenList.get(myCustomTokenIndex);
			int line = current.getLineNumber();
			String errorMessage = "Error: name not allowed in line " + line;
			throw new RuntimeException(errorMessage);
		}

		myCustomProcedure = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
		myCustomTokenIndex++;

		procedureHeading(";");
	}

	void value() {
		if (myCustomTokenList.get(myCustomTokenIndex).getTokenType().equals("intger")) {
			String s = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
			char arr[] = s.toCharArray();
			int i = 0;

			if (Character.isDigit(arr[i])) {
				for (i = 1; i < arr.length; i++) {
					if (Character.isDigit(arr[i])) {

					} else {
						Token current = myCustomTokenList.get(myCustomTokenIndex);
						int line = current.getLineNumber();
						throw new RuntimeException(
								"Error: " + "integer value " + s + " is not digits in line: " + line);
					}
				}
			} else {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException("Error: " + "integer value " + s + " is not digits in line: " + line);
			}

			myCustomTokenIndex++;

		} else if (myCustomTokenList.get(myCustomTokenIndex).getTokenType().equals("real")) {
			String s = myCustomTokenList.get(myCustomTokenIndex).getTextValue();
			char arr[] = s.toCharArray();
			int i = 0;
			boolean flag = false;

			if (Character.isDigit(arr[0])) {
				for (i = 1; i < arr.length; i++) {
					if (Character.isDigit(arr[i]) || arr[i] == '.') {
						if (flag && arr[i] == '.') {
							Token current = myCustomTokenList.get(myCustomTokenIndex);
							int line = current.getLineNumber();
							throw new RuntimeException("Error: " + "two . in this real value "
									+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " in line: " + line);
						}
						if (arr[i] == '.') {
							if (i < arr.length - 1 && Character.isDigit(arr[i + 1])) {
								flag = true;
							} else {
								Token current = myCustomTokenList.get(myCustomTokenIndex);
								int line = current.getLineNumber();
								throw new RuntimeException("Error: " + "error value "
										+ myCustomTokenList.get(myCustomTokenIndex).getTextValue()
										+ " is not valid in line: " + line);
							}
						}
					} else {
						Token current = myCustomTokenList.get(myCustomTokenIndex);
						int line = current.getLineNumber();
						throw new RuntimeException(
								"Error: " + "error value " + myCustomTokenList.get(myCustomTokenIndex).getTextValue()
										+ " is not valid in line: " + line);
					}
				}
			} else {
				Token current = myCustomTokenList.get(myCustomTokenIndex);
				int line = current.getLineNumber();
				throw new RuntimeException("Error: " + "error value "
						+ myCustomTokenList.get(myCustomTokenIndex).getTextValue() + " is not valid in line: " + line);
			}

			myCustomTokenIndex++;

		} else {
			int line = getCurrentToken().getLineNumber();
			throw new RuntimeException(
					"Error: " + "invalid value " + getCurrentToken().getTextValue() + " in line " + line);
		}
	}

}
