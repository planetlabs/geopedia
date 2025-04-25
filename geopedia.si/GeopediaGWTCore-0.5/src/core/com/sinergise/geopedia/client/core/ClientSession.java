package com.sinergise.geopedia.client.core;

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.core.common.HasSession;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.entities.User.UserUpdater;
import com.sinergise.geopedia.core.exceptions.GeopediaException;

public class ClientSession
{	
	public static String additionalTimestamp = "";
	private static User user = User.NO_USER;
	protected static String session;

	public static String extractSessionId() {
		return extractSessionId(session);
	}
	
	
	private static String extractSessionId(String sessionString) {
		if (sessionString==null) return null;
		int idx = sessionString.indexOf("_");
		if (idx==-1) return null;
		return sessionString.substring(0,idx);
	}
	
	private static Long extractSessionSeq(String sessionString) {
		if (sessionString==null) return null;
		int idx = sessionString.indexOf("_");
		if (idx==-1) return null;
		try {
			return Long.parseLong(sessionString.substring(idx+1));
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	public static void updateSession(String sessionString) {
		String newSessionId = extractSessionId(sessionString);
		if (newSessionId == null) return;
		Long newSessionSeq = extractSessionSeq(sessionString);
		if (newSessionSeq==null) return;
		
		String oldSessionId = extractSessionId(session);
		Long oldSessionSeq = extractSessionSeq(session);
		
		if (oldSessionId==null || !oldSessionId.equals(newSessionId)) {
			session = sessionString;
			fireSessionChangedEvent();
		} else if (oldSessionSeq==null || oldSessionSeq<newSessionSeq) {
			session = sessionString;
			fireSessionChangedEvent();
		}
		
	}
    
    public static void generateAdditionalTimestamp() {
    	int ts = Random.nextInt(99999999);
    	ts+=100000000;
    	additionalTimestamp="@"+ts;    	
    }

	public static void start(final AsyncCallback<Void> cb)
    {
        RemoteServices.getSessionServiceInstance().createSession(new AsyncCallback<HasSession<User>>() {
            public void onSuccess(HasSession<User> result)
            {
               if (result!=null) {
            	   updateSession(result.getSession());
            	   setUser(result.getExtraData());            	            	   
               }
               if (cb!=null) {
            	   cb.onSuccess(null);
               }
            }

            public void onFailure(Throwable caught)
            {
                if (cb != null) {
                    cb.onFailure(caught);
        		}                
            }
        });
    }



	public static void login(final String user, String pass, final AsyncCallback<String> cb)
	{
		RemoteServices.getSessionServiceInstance().login(user, pass, createLoginCallback(cb));
	}
	
		
	public static void setUser(User user1) {
		user=user1;
		user.setUserUpdater(new UserUpdater() {
			
			@Override
			public void updateUser(User userToUpdate, final SGAsyncCallback<User> callback) {
				RemoteServices.getSessionServiceInstance().getUserUpdate(userToUpdate.getId(), 
						new AsyncCallback<User>() {

							@Override
							public void onFailure(Throwable caught) {
								if (callback!=null) {
									callback.onFailure(caught);
								}
							}

							@Override
							public void onSuccess(User result) {
								user.setPermissions(result.getPermissions());
								if (callback!=null) {
									callback.onSuccess(result);
								}
								
							}
				});
				
			}
		});
		fireUserLoggedInEvent(user);
	}
	
	
	private static AsyncCallback<HasSession<User>> createLoginCallback(final AsyncCallback<String> cb) {
		return new AsyncCallback<HasSession<User>>() {
			public void onSuccess(HasSession<User> result)
			{
				User usr = result.getExtraData();
				updateSession(result.getSession());
				setUser(usr);
				if (cb != null) {
					cb.onSuccess(usr.getUsername());
				}
				
			}

			public void onFailure(Throwable caught)
			{
				if (cb != null) {
					cb.onFailure(caught);
				}

				if (!user.equals(User.NO_USER)) {
					user=User.NO_USER;
					fireUserLoggedOutEvent(user);
				}
			}
		};
	}
    
	public static boolean canPerformOperation(int opID) {
		try {
			if (user.isAdmin())
				return true;
			if (!isLoggedIn())
				return false;
			return user.hasGlobalPermission(opID);
		} catch (GeopediaException ex) {
			return false;
		}
	}
   
    

    
    @Deprecated
    public static boolean checkThemePrivileges(Theme theme, int which)
    {
		if (!ClientSession.isLoggedIn()) {
			return which<=Permissions.THEME_VIEW;
		} else if (theme.user_perms>=0) {
			return (theme.user_perms >= which);
		}
	    return (theme.public_perms >= which);
    }
    
    @Deprecated
	public static boolean checkTablePrivileges(Table table, int perm) {
		if (ClientSession.isLoggedIn() && table.user_perms >= 0) {
			return table.user_perms >= perm;
		}
		return table.public_perms >= perm;
	}
    
	public static void logout(final AsyncCallback<Void> cb)
	{
		RemoteServices.getSessionServiceInstance().logout(new AsyncCallback<HasSession<Void>>() {
			public void onSuccess(HasSession<Void> result)
			{
				updateSession(result.getSession());
				User oldUser = user;
				user = User.NO_USER;				
				if (cb!=null) cb.onSuccess(null);
				fireUserLoggedOutEvent(oldUser);
			}

			public void onFailure(Throwable caught)
			{
				User oldUser = user;
				user = User.NO_USER;	
				if (cb!=null) cb.onFailure(caught);
				fireUserLoggedOutEvent(oldUser);
			}
		});
	}

	public static boolean isLoggedIn()
	{
		return !user.equals(User.NO_USER);
	}


	public static void initPinger()
	{
		new Timer() {
			public void run()
			{
				RemoteServices.getSessionServiceInstance().ping(new AsyncCallback<HasSession<Void>>() {
					public void onFailure(Throwable caught)
					{
					}

					public void onSuccess(HasSession<Void> result) {
						updateSession(result.getSession());
					}
				});
			}
		}.scheduleRepeating(60000);
	}

	public static boolean logoutAllowed()
    {
		return true;
    }
	

	private static void fireUserLoggedOutEvent(User user) {
		ClientGlobals.eventBus.fireEvent(ClientSessionEvent.createOnLogoutEvent(user));
	}
	private static void fireUserLoggedInEvent(User user) {
		ClientGlobals.eventBus.fireEvent(ClientSessionEvent.createOnLoginEvent(user));
	}
	

	private static void fireSessionChangedEvent() {
		ClientGlobals.eventBus.fireEvent(ClientSessionEvent.createSessionChangedEvent(ClientSession.user, ClientSession.session));
	}
	
	public static void notifyAlreadyLoggedIn() {
		if (!user.equals(User.NO_USER))
			fireUserLoggedInEvent(user);
	}

	public static boolean hasTablePermission(Table table, int permission) {
		if (!table.hasValidId()) // new table
			return true;
		return user.hasTablePermission(table, permission);
	}

	public static boolean hasThemePermissions(Theme theme, int permission) {
		if (!theme.hasValidId()) // new theme
			return true;
		return user.hasThemePermission(theme, permission);
	}

	public static User getUser() {
		return user;
	}

	public static String getSessionValue() {
		return session;
	}	
}
