package application;

public class Token {

	private String textValue;
	private int lineNumber;
	private String tokenType;

	public Token() {
		super();
	}

	public Token(String textValue, int lineNumber, String tokenType) {
		super();
		this.textValue = textValue;
		this.lineNumber = lineNumber;
		this.tokenType = tokenType;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	@Override
	public String toString() {
		return "Token [textValue=" + textValue + ", lineNumber=" + lineNumber + ", tokenType=" + tokenType + "]";
	}

}
