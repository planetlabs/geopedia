package java.net;

import java.io.IOException;

public class MalformedURLException extends IOException  {
    private static final long serialVersionUID = -4597998646797318818L;
    public MalformedURLException() {
    }
	public MalformedURLException(String message) {
		super(message);
	}
}
