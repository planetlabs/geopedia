package com.sinergise.common.util.lang;

public interface SGCommand {
	SGCommand NO_OP = new SGCommand() {
		@Override
		public void execute() {}
	};

	public void execute();
}
