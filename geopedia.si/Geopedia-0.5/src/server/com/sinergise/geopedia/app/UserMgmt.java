package com.sinergise.geopedia.app;



import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.entities.User;
/*
 * 
 * Use UserAccessControl
 */
@Deprecated
public class UserMgmt implements Permissions
{
	

	public static int requireUserId(Session sess) throws UpdateException
    {
		if (sess == null)
			throw new UpdateException(UpdateException.T_NO_SESSION);
		
		User user = sess.getUser();

		if (user == null)
			throw new UpdateException(UpdateException.T_NOT_LOGGED_IN);
		
		return user.getId();
    }

	public static boolean isValidTablePublicPerms(int public_perms)
    {
	    return Permissions.Util.isValidPublicPerm(GeopediaEntity.TABLE, public_perms);
    }
	public static boolean isValidThemePublicPerms(int public_perms)
    {
	    return Permissions.Util.isValidPublicPerm(GeopediaEntity.THEME, public_perms);
    }
	
	public static void flushUserPrivileges(Session session)
	{
		
		if (session == null)
			return;		
		User user =session.getUser();
		if (user == null)
			return;
		
		user.flushPrivileges();
	}

	public static void requireAdmin(Session sess) throws UpdateException
    {
		if (sess == null)
			throw new UpdateException(UpdateException.T_NO_SESSION);
		
		User user = (User) sess.getUser();
		if (user == null)
			throw new UpdateException(UpdateException.T_NOT_LOGGED_IN);
		
		if (!user.isAdmin)
			throw new UpdateException(UpdateException.T_NO_PERMISSION,"ADMIN privileges requires");
    }
/*
	public static boolean isValidSid(String sid)
    {
		//return SessionManager.getBySid(sid) != null;
		return false;
    }
	*/
	public static boolean isLoggedIn(Session sess)
	{
		if (sess == null)
			return false;
		
		User user =sess.getUser();
		if (user == null)
			return false;
		
		return true;
	}
	
	public static User getUserData(Session sess)
	{
		if (sess == null)
			return null;
		
		User user = (User) sess.getUser();
		return user;
	}
	
	public static boolean isAdmin(Session sess)
    {
		if (sess == null)
			return false;
		
		User user = (User) sess.getUser();
		if (user == null)
			return false;
		
		return user.isAdmin;
    }
	
	

 
	
      
    
    private static boolean isValidAuthenticationKey(String users_authentication_key) {
        return users_authentication_key!=null && users_authentication_key.trim().length()>8;
    }
}
