package com.sinergise.geopedia.app;

import java.sql.SQLException;

import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.db.entities.MetaData;

public class UserAccessControl {
	
	public static int verifyAccessRights (Table table, User user, int requiredAccessLevel) throws GeopediaException, SQLException{
		return verifyAccessRights(GeopediaEntity.TABLE,table.getId(), table.public_perms, user, requiredAccessLevel);
	}
	
	public static int verifyAccessRights (Theme theme, User user, int requiredAccessLevel) throws GeopediaException, SQLException{
		return verifyAccessRights(GeopediaEntity.THEME,theme.getId(),theme.public_perms, user, requiredAccessLevel);
	}
	
	
	private static int verifyAccessRights(GeopediaEntity entityType, int entityId, int entityPublicPerms,  User user, int requiredAccessLevel) throws GeopediaException, SQLException {
		if (user==null || User.NO_USER.equals(user)) {  // not logged in (public access)
			if ((entityType == GeopediaEntity.TABLE && requiredAccessLevel < Permissions.TABLE_EDITDATA) ||
				(entityType == GeopediaEntity.THEME && requiredAccessLevel < Permissions.THEME_EDIT)) {
				if ( requiredAccessLevel != Permissions.ADMINPERMS && entityPublicPerms>=requiredAccessLevel)
					return entityPublicPerms;
			}
			throw new GeopediaException(GeopediaException.Type.PERMISSION_DENIED);
		}
		if ( requiredAccessLevel != Permissions.ADMINPERMS && entityPublicPerms>=requiredAccessLevel)
			return entityPublicPerms;

		
		if (user.isAdmin)
			return Permissions.ADMINPERMS;

		Integer perm = user.getPermissions(entityType, entityId);
		if (perm !=null && perm >= 0) {
			if (perm >= requiredAccessLevel)
				return perm;
		}
	
		throw new GeopediaException(GeopediaException.Type.PERMISSION_DENIED);		
	}
	
	
	public static boolean hasAccess(Session sess, GeopediaEntity entity, int id, long metaTs, int requiredLevel) throws GeopediaException, SQLException {
		if (sess==null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		User user = sess.getUser();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		MetaData meta = instance.getMetaData();

		if (user.isAdmin)
			return true;
			
		Integer perm = user.getPermissions(entity, id);
		if (perm!=null) {
			return perm >= requiredLevel;
		}
		
		// perm not set for user and/or his groups
		if (requiredLevel >= Permissions.ADMINPERMS) return false;
		
		if (entity == GeopediaEntity.THEME) {
			Theme t = meta.getThemeByIdMeta(id, metaTs);
			return t.public_perms >= requiredLevel;
		} else
		if (entity == GeopediaEntity.TABLE) {
			Table t = meta.getTableByIdMeta(id, metaTs);
			return t.public_perms >= requiredLevel;
		} else
		if (entity == GeopediaEntity.CATEGORY) {
			return true;
		}
		
		return false;
	}
	
	
	public static int checkAccess(Session sess, GeopediaEntity entity, int id, long metaTs, int requiredLevel) throws SQLException, GeopediaException
	{
		if (sess==null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		MetaData meta = instance.getMetaData();
		User user = sess.getUser();
		if (user != null && !user.equals(User.NO_USER)) {
			if (user.isAdmin)
				return Permissions.ADMINPERMS;
			
			Integer perm = user.getPermissions(entity, id);
			if (perm!=null) {
				if (perm >= requiredLevel)
					return perm;
				throw new GeopediaException(GeopediaException.Type.PERMISSION_DENIED);
			}
		}
		
		// perm not set for user and/or his groups
		if (requiredLevel < Permissions.ADMINPERMS) {
		if (entity == GeopediaEntity.THEME) {
			Theme t = meta.getThemeByIdMeta(id, metaTs);
			if (t.public_perms >= requiredLevel)
				return user == null ? -1 : t.public_perms;
		} else
		if (entity == GeopediaEntity.TABLE) {
			Table t = meta.getTableByIdMeta(id, metaTs);
			if (t.public_perms >= requiredLevel)
				return user == null ? -1 : t.public_perms;
		} else
		if (entity == GeopediaEntity.CATEGORY) {
			return user == null ? -1 : Permissions.CAT_EDITDATA;
		} else
		if (entity == GeopediaEntity.GROUP) {
			return user == null ? -1 : Permissions.GROUP_VIEW;
		}
		}
		if (user != null && !user.equals(User.NO_USER)) {
			throw new GeopediaException(GeopediaException.Type.PERMISSION_DENIED);
		} else		
			throw new GeopediaException(GeopediaException.Type.NOT_LOGGED_IN);
	}
}
