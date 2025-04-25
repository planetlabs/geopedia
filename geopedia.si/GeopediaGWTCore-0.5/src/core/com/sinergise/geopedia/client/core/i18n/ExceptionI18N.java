package com.sinergise.geopedia.client.core.i18n;

import com.sinergise.geopedia.core.exceptions.EnumeratedException;
import com.sinergise.geopedia.core.exceptions.FeatureDataException;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.exceptions.ImportExportException;
import com.sinergise.geopedia.core.exceptions.TableDataException;


public class ExceptionI18N {

	
	private static class ImportExportExceptionI18N {
		public static String getLocalizedMessage(ImportExportException ex) {
			switch (ex.getType()) {
			case UNKNOWN_OR_CORRUPTED_FILE:
				return ExceptionMessages.INSTANCE.GeopediaException_UnknownOrCorruptedFile();
				///TODO: implement all cases
			default:
				return ex.getType().name();
			}
		}
	}
	
	private static class TableDataExceptionI18N {
		public static String getLocalizedMessage(TableDataException ex) {
			switch (ex.getType()) {
			case FIELD_HAS_REPTEXT_REFERENCE:
			case ILLEGAL_DESCRIPTION:
			case STYLE_ERROR:
			case FIELD_HAS_STYLE_REFERENCE:
			case REPTEXT_ERROR:
			case TABLE_IS_REFERENCED:
				///TODO: implement all cases
			default:
				return ex.getType().name();
			}
		}
	}
	
	private static class FeatureDataExceptionI18N {
		public static String getgetLocalizedMessage(FeatureDataException ex) {
			switch (ex.getType()) {
			case ILLEGAL_FIELD_VALUE:
				///TODO: implement all cases
			default:
				return ex.getType().name();			
			}
		}
	}
	
	
	private static String handleWrappedExceptions(EnumeratedException ex) {
		if (ex instanceof ImportExportException) {
			return ImportExportExceptionI18N.getLocalizedMessage((ImportExportException) ex);
		} else if (ex instanceof TableDataException) {
			return TableDataExceptionI18N.getLocalizedMessage((TableDataException)ex);
		} else if (ex instanceof FeatureDataException) {
			return FeatureDataExceptionI18N.getgetLocalizedMessage((FeatureDataException)ex);
		}
		return null;
	}
	private static class GeopediaExceptionI18N {
		public static String getLocalizedMessage(GeopediaException ex) {
			switch (ex.getType()) {
				case NO_SESSION:
					return ExceptionMessages.INSTANCE.GeopediaException_IllegalSession();
				case NOT_LOGGED_IN:
					return ExceptionMessages.INSTANCE.GeopediaException_NotLogged();
				case PERMISSION_DENIED:
					return ExceptionMessages.INSTANCE.GeopediaException_NoRights();
				case DATABASE_ERROR:
					return ExceptionMessages.INSTANCE.GeopediaException_DatabaseError();
				case INVALID_USER_STATE:
					return ExceptionMessages.INSTANCE.GeopediaException_UserDoesntExist();
				case INVALID_USER:
					return ExceptionMessages.INSTANCE.GeopediaException_InvalidUser();
				case INVALID_WIDGET_ID:
					return ExceptionMessages.INSTANCE.GeopediaException_InvalidWidgetID();
				case WRAPPED_EXCEPTION:
					return handleWrappedExceptions(ex.getWrappedException());
				default:
					return null;
			}
		}	
	}
	
	public static String getLocalizedMessage(Throwable th) {
		if (th instanceof GeopediaException)
			return getLocalizedMessage((GeopediaException)th);
		else {
			return th.getLocalizedMessage();
		}
	}
	
	
	private static String getLocalizedMessage(GeopediaException ex) {
		String message = GeopediaExceptionI18N.getLocalizedMessage(ex);
		if (message==null) {
			message = "Not localized exception type="+ex.getType();
			if (ex.getType()==GeopediaException.Type.WRAPPED_EXCEPTION) {
				EnumeratedException wrappedException = ex.getWrappedException();
				if (wrappedException!=null) {
					message+=" Wrapped exception type="+wrappedException.getType();
				}
			}
		}
		return message;
	}
}
