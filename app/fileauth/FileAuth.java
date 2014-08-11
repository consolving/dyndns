/**
 * FileAuth 31.07.2012
 *
 * @author Philipp Haussleiter
 *
 */
package fileauth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.typesafe.config.ConfigFactory;

import fileauth.utils.MD5Crypt;
import fileauth.utils.UnixCrypt;
import play.Logger;
import play.cache.Cache;

/**
 * Basic Wrapper for all FileAuth Functions.
 * 
 * @author Philipp Haußleiter
 */
public class FileAuth {

	/* Cache Key for User/PasswordHash Map */
	public final static String AUTH_FILE_USERS_CACHE_KEY = "AUTH_FILE_USERS";
	/* Cache Key for Group/Users Map */
	public final static String AUTH_FILE_GROUPS_CACHE_KEY = "AUTH_FILE_GROUPS";
	private final static int CACHE_TIMEOUT = 60 * 5;

	private final static boolean IS_ENABLED = isEnabled();
	private final static String AUTH_FILE_USERS_DELIMETER = getConfig(
			"authfile.users.delimeter", ":");
	private final static String AUTH_FILE_GROUPS_DELIMETER = getConfig(
			"authfile.groups.delimeter", " ");
	private final static String AUTH_FILE_USERS_PATH = ConfigFactory.load()
			.getString("authfile.users.path");
	private final static String AUTH_FILE_GROUPS_PATH = ConfigFactory.load()
			.getString("authfile.groups.path");

	/**
	 * Returns a Map of all Users (user/password hash).
	 * 
	 * @return the Map.
	 */
	public static Map<String, String> getUsers() {
		if (!IS_ENABLED) {
			return new HashMap<String, String>();
		}
		@SuppressWarnings("unchecked")
		Map<String, String> users = (HashMap<String, String>) Cache
				.get(AUTH_FILE_USERS_CACHE_KEY);
		if (users == null) {
			users = scanUsers();
		}
		return users;
	}

	/**
	 * Returns a Map of all Groups (groups/usernames).
	 * 
	 * @return the Map.
	 */
	public static Map<String, Set<String>> getGroups() {
		if (!IS_ENABLED) {
			return new HashMap<String, Set<String>>();
		}
		@SuppressWarnings("unchecked")
		Map<String, Set<String>> groups = (HashMap<String, Set<String>>) Cache
				.get(AUTH_FILE_GROUPS_CACHE_KEY);
		if (groups == null) {
			groups = scanGroups();
		}
		return groups;
	}

	/**
	 * Checks if a group contains a given username.
	 * 
	 * @param group
	 *            the Group to check.
	 * @param user
	 *            the user to check.
	 * @return true if user is in group, otherwise false.
	 */
	public static boolean contains(String group, String user) {
		if (!IS_ENABLED) {
			return true;
		}
		if (group == null || user == null) {
			return false;
		}
		Map<String, Set<String>> groups = getGroups();
		Set<String> groupUsers = groups.get(group);
		if (groupUsers == null) {
			return false;
		}
		return groupUsers.contains(user);
	}

	/**
	 * Validates an user with a given password agains the user/password hash
	 * mapping.
	 * 
	 * @param user
	 *            the given user.
	 * @param password
	 *            the given password (clear text).
	 * @return true if validation okay, otherwise false.
	 */
	public static boolean validate(String user, String password) {
		if (!IS_ENABLED) {
			return true;
		}
		if (user == null || password == null) {
			return false;
		}
		Map<String, String> users = getUsers();
		String encryptedPass = users.get(user);
		if (encryptedPass == null) {
			Logger.warn("encryptedPass is NULL for user " + user);
			return false;
		}
		if (encryptedPass.startsWith("$")
				&& MD5Crypt.verifyPassword(password, encryptedPass)) {
			return true;
		}
		if (encryptedPass.length() == 13
				&& UnixCrypt.matches(encryptedPass, password)) {
			return true;
		}
		Logger.warn("could not validate user " + user);
		return false;
	}

	/**
	 * Rescans the users file.
	 * 
	 * @return the updated Map of users.
	 */
	public static Map<String, String> scanUsers() {
		String fileName = AUTH_FILE_USERS_PATH;
		StringBuilder sb = new StringBuilder("@" + System.currentTimeMillis()
				+ " Scanning Users in " + fileName + "... ");
		Map<String, String> users = new HashMap<String, String>();
		File file = new File(fileName);
		if (file == null || !file.exists() || !file.isFile()) {
			Logger.warn(fileName + " is not a valid Auth-File!");
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			String parts[];
			while ((line = br.readLine()) != null) {
				parts = line.split(AUTH_FILE_USERS_DELIMETER);
				/*
				 * Matches user:hash user:hash:uid: ...
				 */
				if (parts.length > 1) {
					users.put(parts[0].trim(), parts[1].trim());
				}
			}
			br.close();
			Cache.set(AUTH_FILE_USERS_CACHE_KEY, users, CACHE_TIMEOUT);
		} catch (FileNotFoundException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
		} catch (IOException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
		}
		sb.append(" found " + users.size() + " mappings");
		Logger.info(sb.toString());
		return users;
	}

	/**
	 * Rescans the groups file.
	 * 
	 * @return the updated Map of groups.
	 */
	public static Map<String, Set<String>> scanGroups() {
		String fileName = AUTH_FILE_GROUPS_PATH;
		StringBuilder sb = new StringBuilder("@" + System.currentTimeMillis()
				+ " Scanning Groups in " + fileName + "... ");
		Map<String, Set<String>> groups = new HashMap<String, Set<String>>();
		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			Logger.warn(fileName + " is not a valid Auth-File!");
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			String parts[];
			String userParts[];
			Set<String> users;
			while ((line = br.readLine()) != null) {
				parts = line.split(AUTH_FILE_USERS_DELIMETER);
				if (parts.length > 1) {
					if (parts.length > 2) {
						userParts = parts[parts.length - 1]
								.split(AUTH_FILE_GROUPS_DELIMETER);
					} else {
						userParts = parts[1].split(AUTH_FILE_GROUPS_DELIMETER);
					}
					if (userParts.length > 0) {
						users = new HashSet<String>();
						for (String user : userParts) {
							users.add(user.trim());
						}
						groups.put(parts[0].trim(), users);
					}
				}
			}
			br.close();
			Cache.set(AUTH_FILE_GROUPS_CACHE_KEY, groups, CACHE_TIMEOUT);
		} catch (FileNotFoundException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
		} catch (IOException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
		}
		sb.append(" found " + groups.size() + " mappings");
		Logger.info(sb.toString());
		return groups;
	}

	private static boolean isEnabled() {
		if (!ConfigFactory.load().hasPath("authfile.users.path")
				|| !ConfigFactory.load().hasPath("authfile.groups.path")) {
			Logger.info("FileAuth not enabled. authfile.users.path or authfile.groups.path not set!");
			return false;
		}
		return true;
	}

	private static String getConfig(String key, String defaultValue) {
		if (ConfigFactory.load().hasPath(key)) {
			return ConfigFactory.load().getString(key);
		} else {
			Logger.info("Config " + key + " not found, using default: '"
					+ defaultValue + "'");
			return defaultValue;
		}
	}
}
