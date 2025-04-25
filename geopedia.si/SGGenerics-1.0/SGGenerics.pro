#
# This ProGuard configuration file illustrates how to process ProGuard itself.
# Configuration files for typical applications will be very similar.
# Usage:
#     java -jar proguard.jar @proguard.pro
#

# Disregard warnings about missing classes, in case we don't have
# the Ant or J2ME libraries.

-ignorewarnings
-verbose

# Allow methods with the same signature, except for the return type,
# to get the same obfuscation name.

#-overloadaggressively


# Put all obfuscated classes into the nameless root package.

#-defaultpackage 'c'

-libraryjars ../SineRepo/Web/gwt/2.4.0/lib/gwt-servlet.jar
-libraryjars ../SineRepo/Util/logback/0.9.28/lib/logback-classic-0.9.28.jar
-libraryjars ../SineRepo/Util/logback/0.9.28/lib/logback-core-0.9.28.jar


-keeppackagenames
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-dontoptimize
-dontpreverify



-keep public class * {
    public <fields>;
    public <methods>;
}

-keep public abstract class * {
	public protected <fields>;
    public protected <methods>;
}

-keep public class com.sinergise.generics.core.EntityType {
	public protected private <fields>;
    public protected private <methods>;
}

-keep public class * extends ch.qos.logback.core.AppenderBase {
	public protected private <fields>;
    public protected private <methods>;
}


-keep public enum com.sinergise.generics.core.services.GenericsService$** {
    **[] $VALUES;
    public *;
}

-keep public class  com.sinergise.generics.server.SessionRemoteServiceServlet {
	protected final com.sinergise.generics.server.GenericsSessionManager sessionManager;
}

-keep public class com.sinergise.generics.gwt.widgets.table.GenercsTable {
	protected java.util.List attributeList;
	protected com.sinergise.generics.gwt.widgets.table.PagingTable tableWidget;
	protected com.sinergise.generics.gwt.widgets.table.TableDataProvider dataProvider;
	
	protected com.google.gwt.user.client.ui.Widget createAndBindCellWidget(int, int);
}

-keep public class com.sinergise.generics.gwt.components.GenericsTableComponent {
	protected <methods>;
	
	protected final com.sinergise.generics.core.services.GenericsServiceAsync genericsService;
	protected com.sinergise.generics.gwt.widgets.table.GenercsTable genTable;
}

-keep public class com.sinergise.generics.gwt.widgetprocessors.TableValueBinderWidgetProcessor {
	protected void notifyStartProcessing();
}

-keep public class com.sinergise.generics.gwt.widgetprocessors.RemoteTableDataProvider {
	protected com.sinergise.generics.core.filter.DataFilter modifyFilter(com.sinergise.generics.core.filter.DataFilter);
	
	protected final com.sinergise.generics.core.services.GenericsServiceAsync genericsService;
	protected java.lang.String datasourceId;
}

-keep public class com.sinergise.generics.java.OracleConnectionProvider {
	protected javax.sql.DataSource ds;
	protected static final java.util.Map providers;
}

-keep public class com.sinergise.generics.server.GenericsServiceImpl {
	public protected <methods>;
	public protected <fields>;
	
#	protected java.util.Map entityMetadataMap;
#	protected java.util.Map widgetMetadataMap;
#	protected java.util.Map widgetMetadataCacheMap;

}

-keep public class com.sinergise.generics.core.services.GenericsServiceAsync {
	public <methods>;
}

-keep public class com.sinergise.generics.server.GenericsServerSession {
	protected javax.servlet.http.HttpServletRequest httpRequest;
}

-keep public class com.sinergise.generics.server.GenericsSessionManager {
	protected com.sinergise.generics.server.GenericsServerSession createSession (javax.servlet.http.HttpServletRequest);
}


