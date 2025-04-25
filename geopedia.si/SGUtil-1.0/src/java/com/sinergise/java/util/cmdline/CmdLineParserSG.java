package com.sinergise.java.util.cmdline;

import java.awt.Color;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.java.util.UtilJava;

public class CmdLineParserSG {
	public static class TimeSpecOptionHandler extends OptionHandler<TimeSpec> {
		public TimeSpecOptionHandler(final CmdLineParser parser, final OptionDef option, final Setter<? super TimeSpec> setter) {
			super(parser, option, setter);
		}
		
		@Override
		public String getDefaultMetaVariable() {
			return "YYYY-MM-DDThh:mm:ss";
		}
		
		@Override
		public int parseArguments(final Parameters p) throws CmdLineException {
			String prm = p.getParameter(0).toUpperCase();
			setter.addValue(new TimeSpec(prm));
			return 1;
		}
	}
	public static class ColorOptionHandler extends OptionHandler<Color> {
		
		public ColorOptionHandler(final CmdLineParser parser, final OptionDef option, final Setter<? super Color> setter) {
			super(parser, option, setter);
		}
		
		@Override
		public String getDefaultMetaVariable() {
			return "AARRGGBB";
		}
		
		@Override
		public int parseArguments(final Parameters p) throws CmdLineException {
			String prm = p.getParameter(0).toUpperCase();
			if (prm.startsWith("#")) {
				prm = "0x" + prm.substring(1);
			} else if (!prm.startsWith("0X")) {
				prm = "0x" + prm;
			}
			final long lng = Long.decode(prm).longValue();
			setter.addValue(new Color((int)lng, true));
			return 1;
		}
		
	}
	
	static {
		CmdLineParser.registerHandler(Color.class, ColorOptionHandler.class);
		CmdLineParser.registerHandler(TimeSpec.class, TimeSpecOptionHandler.class);
	}
	
	public static <T> T parseArgs(final T target, String[] args, final String cmdString) {
		UtilJava.initStaticUtils();
		final CmdLineParser cmp = new CmdLineParser(target);
		try {
			if (args == null) {
				args = new String[0];
			}
			cmp.parseArgument(args);
			return target;
		} catch(final CmdLineException e) {
			System.out.println(e.getLocalizedMessage());
		}
		System.out.println();
		printUsage(cmdString, cmp);
		return null;
	}
	
	private static void printUsage(final String cmdString, final CmdLineParser cmp) {
		System.out.print("Usage: " + cmdString + " ");
		cmp.printSingleLineUsage(System.out);
		System.out.println("\n");
		cmp.printUsage(System.out);
	}
}
