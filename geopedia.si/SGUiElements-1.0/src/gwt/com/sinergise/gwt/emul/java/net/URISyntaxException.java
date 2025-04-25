package java.net;

public class URISyntaxException extends Exception {
    private static final long serialVersionUID = 2348647550605375510L;

    private String input;
	private int    index;
	
	public URISyntaxException(String input, String reason, int index) {
		super(reason);
		assert reason != null : "Reason should not be null";
		assert input != null : "Input should not be null";
		this.input = input;
		this.index = index;
	}
	
	public URISyntaxException(String input, String reason) {
		this(input, reason, -1);
	}
	
	public String getInput() {
		return input;
	}
	
	public String getReason() {
		return super.getMessage();
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public String getMessage() {
		StringBuilder bld = new StringBuilder(super.getMessage());
		if (index >= 0) {
			bld.append(" at index ");
			bld.append(index);
		}
		bld.append(": ");
		bld.append(input);
		return bld.toString();
	}
}
