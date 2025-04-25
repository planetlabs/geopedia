package com.sinergise.common.util.format;

public interface SGDateTimeConstants {
	String[] ampms();
	
	String[] dateFormats();
	
	String[] eraNames();
	
	String[] eras();
	
	String firstDayOfTheWeek();
	
	String[] months();
	
	String[] narrowMonths();
	
	String[] narrowWeekdays();
	
	String[] quarters();
	
	String[] shortMonths();
	
	String[] shortQuarters();
	
	String[] shortWeekdays();
	
	String[] standaloneMonths();
	
	String[] standaloneNarrowMonths();
	
	String[] standaloneNarrowWeekdays();
	
	String[] standaloneShortMonths();
	
	String[] standaloneShortWeekdays();
	
	String[] standaloneWeekdays();
	
	String[] timeFormats();
	
	String[] weekdays();
	
	String[] weekendRange();

	public static final SGDateTimeConstants DEFAULTS_EN = new SGDateTimeConstants() {
	
		@Override
		public String[] ampms() {
			return new String[]{"AM", "PM"};
		}
	
		@Override
		public String[] dateFormats() {
			return new String[]{"EEEE, y MMMM dd", "y MMMM d", "y MMM d", "yyyy-MM-dd"};
		}
	
		@Override
		public String[] eraNames() {
		    return new String[] {
		        "Before Christ",
		        "Anno Domini"
		    };
		}
	
		@Override
		public String[] eras() {
		    return new String[] {
		        "BC",
		        "AD"
		    };
		}
	
		@Override
		public String firstDayOfTheWeek() {
			return weekdays()[1];
		}
	
		@Override
		public String[] months() {
		    return new String[] {
		        "January",
		        "February",
		        "March",
		        "April",
		        "May",
		        "June",
		        "July",
		        "August",
		        "September",
		        "October",
		        "November",
		        "December"
		    };
		}
	
		@Override
		public String[] narrowMonths() {
		    return new String[] {
		        "J",
		        "F",
		        "M",
		        "A",
		        "M",
		        "J",
		        "J",
		        "A",
		        "S",
		        "O",
		        "N",
		        "D"
		    };
		}
	
		@Override
		public String[] narrowWeekdays() {
		    return new String[] {
		    	"S",
		        "S",
		        "M",
		        "T",
		        "W",
		        "T",
		        "F",
		        "S"
		    };
		}
	
		@Override
		public String[] quarters() {
		    return new String[] {
		        "1st quarter",
		        "2nd quarter",
		        "3rd quarter",
		        "4th quarter"
		    };
		}
	
		@Override
		public String[] shortMonths() {
		    return new String[] {
		        "Jan",
		        "Feb",
		        "Mar",
		        "Apr",
		        "May",
		        "Jun",
		        "Jul",
		        "Aug",
		        "Sep",
		        "Oct",
		        "Nov",
		        "Dec"
		    };
		}
	
		@Override
		public String[] shortQuarters() {
		    return new String[] {
		        "Q1",
		        "Q2",
		        "Q3",
		        "Q4"
		    };
		}
	
		@Override
		public String[] shortWeekdays() {
		    return new String[] {
		        "Sun",
		        "Mon",
		        "Tue",
		        "Wed",
		        "Thu",
		        "Fri",
		        "Sat"
		    };
		}
	
		@Override
		public String[] standaloneMonths() {
			return months();
		}
	
		@Override
		public String[] standaloneNarrowMonths() {
		    return narrowMonths();
		}
	
		@Override
		public String[] standaloneNarrowWeekdays() {
			return narrowWeekdays();
		}
	
		@Override
		public String[] standaloneShortMonths() {
			return shortMonths();
		}
	
		@Override
		public String[] standaloneShortWeekdays() {
			return shortWeekdays();
		}
	
		@Override
		public String[] standaloneWeekdays() {
			return weekdays();
		}
	
		@Override
		public String[] timeFormats() {
			return new String[]{"HH:mm:ss zzzz", "HH:mm:ss z", "HH:mm:ss", "HH:mm"};
		}
	
		@Override
		public String[] weekdays() {
		    return new String[] {
		        "Sunday",
		        "Monday",
		        "Tuesday",
		        "Wednesday",
		        "Thursday",
		        "Friday",
		        "Saturday"
		    };
		}
	
		@Override
		public String[] weekendRange() {
			return new String[] {weekdays()[6],weekdays()[0]};
		}
		
	};
}
