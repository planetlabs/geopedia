package com.sinergise.geopedia.core.entities;

import java.io.Serializable;

import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.exceptions.GeopediaException;

public class User implements Serializable {
	private static final long serialVersionUID = 1533143820157358801L;
	
	
	public interface UserUpdater {
		public void updateUser(User user, SGAsyncCallback<User> callback);
	}
	
	public void setUserUpdater(UserUpdater userUpdater) {
		this.userUpdater=userUpdater;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public static final User NO_USER = new User(Integer.MIN_VALUE, "NO_USER", null, null, null, false, new UserPermissions());
	
	private int id;
	public String login;
	public String email;
	public String fullName;
	public String org;
	public boolean isAdmin;
	
	private long lastPermTime;
	private UserPermissions permissions;
	private transient UserUpdater userUpdater = null;
	private transient boolean updateInProgress=false;
	@Deprecated
	protected User() {		
	}
	
	public User(int id, String login, String email, String fullName, String org, boolean isAdmin, UserPermissions permissions)
	{
		this.id = id;
		this.login = login;
		this.email = email;
		this.fullName = fullName;
		this.org = org;
		this.isAdmin = isAdmin;
		this.permissions = permissions;
		this.lastPermTime = System.currentTimeMillis();
	}

	
	public void updateUser(final SGAsyncCallback<User> callback){
		if (userUpdater==null || updateInProgress) {
			if (callback!=null)
				callback.onSuccess(this);
			return;
		} 
		if (userUpdater!=null) {
			updateInProgress=true;
			userUpdater.updateUser(this, new SGAsyncCallback<User>() {

				@Override
				public void onFailure(Throwable caught) {
					if(callback!=null) {
						callback.onFailure(caught);
					}
					updateInProgress=false;
				}

				@Override
				public void onSuccess(User result) {					
					email = result.email;
					login= result.login;
					fullName = result.fullName;
					org = result.org;
					isAdmin = result.isAdmin;
					permissions = result.permissions;
					lastPermTime = System.currentTimeMillis();
					if(callback!=null) {
						callback.onSuccess(User.this);
					}
					updateInProgress=false;
				}
				
			});
		}
	}
	public synchronized Integer getPermissions(GeopediaEntity entity, int entityId) throws GeopediaException
	{
		return internalGetPermissions().getPermission(entity, entityId);
	}

	public UserPermissions getPermissions() {
		return permissions;
	}
	private UserPermissions internalGetPermissions() throws GeopediaException {
		if (permissions == null || System.currentTimeMillis() > (lastPermTime + 600000L)) {
			updateUser(null);
		}
		return permissions;
	}
	
	
	public synchronized void flushPrivileges()
    {
		lastPermTime = 0;
    }
	

	public int getId() {
		return id;
	}


	public String getUsername() {
		return login;
	}
	
	 public boolean hasTablePermission(Table table, int permission) {
	    	if (isAdmin) 
	    		return true;
	    	if (permission < Permissions.TABLE_EDITDATA && table.public_perms>=permission) 
	    		return true;
	    	if (!isLoggedIn())
	    		return false;
	    	if (table.public_perms>=permission) 
	    		return true;
	    	return permissions.hasPermission(GeopediaEntity.TABLE, table.getId(), permission);
	    }

	    public boolean hasThemePermission(Theme theme, int permission) {
	    	if (isAdmin) 
	    		return true;
	    	if (permission < Permissions.THEME_EDIT && theme.public_perms>=permission) 
	    		return true;
	    	if (!isLoggedIn())
	    		return false;
	    	if (theme.public_perms>=permission) 
	    		return true;
	    	return permissions.hasPermission(GeopediaEntity.THEME, theme.getId(), permission);
	    }
	    	
	    public boolean isLoggedIn() {
	    	if (NO_USER.equals(this))
	    		return false;
	    	return true;
	    }
	    
	    public String toString() {
	    	return login;
	    }


		public boolean isAdmin() {
			return isAdmin;
		}


		public boolean hasGlobalPermission(int opID) throws GeopediaException {
			Integer permission = getPermissions(GeopediaEntity.GENERAL, opID);
			if (permission==null) return false;
			return permission >= Permissions.LOWEST;
		}

		public void setPermissions(UserPermissions permissions) {
			this.permissions = permissions;
		}
		
		public void forcePrivilegesReload() {
			lastPermTime=0;
		}
}
