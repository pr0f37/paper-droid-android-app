package paperdroid.application;

import instapaper.api.ApiException;
import instapaper.api.ApplicationDatabaseHelper;
import instapaper.api.BookmarkInstaMessage;
import instapaper.api.BookmarkListInstaMessage;
import instapaper.api.BookmarkTextInstaMessage;
import instapaper.api.CredentialsInstaMessage;
import instapaper.api.FolderInstaMessage;
import instapaper.api.FoldersListInstaMessage;
import instapaper.api.InstapaperClient;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DataSyncService extends Service {
	private final IBinder _binder = new DataSyncBinder();
	private static final String _TAG = "DataSyncService";
	private static String _KEY = "XzvZtp1GNhBnZuXM66afogW4vCYHIghWACIO6CasYh6ruQ8YV4";
	private static String _SECRET = "DB3Cv8qvT9s5SQ1IYfvIMUOEfaGwZAca1M7xxInacPfubsecie";
	private static String _OFFLINE_MODE = "I cannot synchronize this page. You are working in offline mode or internet connection is not avaliable. Please check your network state or log in once more to the Instapaper.";
	private InstapaperClient _client;
	private boolean _isAccessTokenSet = false;
	private boolean _isServiceBusy = false;
	private final ApplicationDatabaseHelper _dbh = new ApplicationDatabaseHelper(this);
	private static final int HELLO_ID = 1;
	
	public class DataSyncBinder extends Binder{
		DataSyncService getService() {
			return DataSyncService.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		
		return _binder;
	}


    public synchronized boolean isServiceBusy(){
    	return _isServiceBusy;
    }
    
    public synchronized boolean isTokenSet(){
    	return _isAccessTokenSet;
    }
    public boolean isConnectionAvailable(){
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    	if(networkInfo != null && networkInfo.isConnected())
    		return true;
    	return false;
    }
	
	public synchronized void initializeClient(final Dialog dialog){
		if(isConnectionAvailable()) {
			_client = new InstapaperClient(_KEY, _SECRET);
			try {
				_isServiceBusy = true;
				Log.d(_TAG, "username: " + getSetting("username"));
				Log.d(_TAG, "password: " + getSetting("password"));
				_client.getAccessToken(getSetting("username"), getSetting("password"));
				CredentialsInstaMessage cred =  _client.getCredentials();				
				setSetting("userId", String.valueOf(cred.getUserId()));
				setSetting("subscriptionStatus", String.valueOf(cred.getSubscriptionStatus()));
				_isAccessTokenSet = true;											
				_isServiceBusy = false;
				dialog.dismiss();
			} catch (ApiException e) {
				_isAccessTokenSet = false;
				_isServiceBusy = false;
				dialog.dismiss();
			}	
		}	

	}
	
	public synchronized void initializeClient(){
		if(isConnectionAvailable()) {
			_client = new InstapaperClient(_KEY, _SECRET);
			try {
				_isServiceBusy = true;
				Log.d(_TAG, "username: " + getSetting("username"));
				Log.d(_TAG, "password: " + getSetting("password"));
				_client.getAccessToken(getSetting("username"), getSetting("password"));
				CredentialsInstaMessage cred =  _client.getCredentials();				
				setSetting("userId", String.valueOf(cred.getUserId()));
				setSetting("subscriptionStatus", String.valueOf(cred.getSubscriptionStatus()));
				_isAccessTokenSet = true;											
				_isServiceBusy = false;
			} catch (ApiException e) {
				_isAccessTokenSet = false;
				_isServiceBusy = false;
			}	
		}	

	}

	
	public synchronized void updateBookmarkList(final int limit, final String folder, final String have, boolean fullSynchro) {
		if(isConnectionAvailable() && _isAccessTokenSet == true ){
			try {
				_isServiceBusy = true;
				BookmarkListInstaMessage bookmarkList = _client.getBookmarkList(limit, folder, have);
				updateBookmarkFolder(bookmarkList, folder, fullSynchro);
				_isServiceBusy = false;
			
			} catch (ApiException e) {
				Log.e(_TAG, String.valueOf(e.getCode()));
				Log.e(_TAG, e.getMessage());
				_isServiceBusy = false;

			}
		}
	}
	
	public synchronized void updateFoldersList(int limit, boolean fullSynchro){
		if(isConnectionAvailable() && _isAccessTokenSet == true){
			try {
				_isServiceBusy = true;
				FoldersListInstaMessage folders = _client.getFoldersList();
				updateFoldersTable(folders, limit ,fullSynchro);
				_isServiceBusy = false;
			
			} catch (ApiException e) {
				Log.e(_TAG, String.valueOf(e.getCode()));
				Log.e(_TAG, e.getMessage());
				_isServiceBusy = false;
			}
		}
	}
	
		
	public void notifyUser(CharSequence contentTitle, CharSequence contentText,  CharSequence tickerText){
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher;
		
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, PaperDroidActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, Notification.FLAG_AUTO_CANCEL);
		
		Notification.Builder notificationBuilder = new Notification.Builder(context);
		notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
		notificationBuilder.setSmallIcon(icon);
		notificationBuilder.setTicker(tickerText);
		notificationBuilder.setContentText(contentText);
		notificationBuilder.setContentTitle(contentTitle);
		notificationBuilder.setContentIntent(contentIntent);
		notificationBuilder.setVibrate(null);
		
		notificationManager.notify(HELLO_ID, notificationBuilder.getNotification());
	}
	public void cancelNotification(){
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(HELLO_ID);
	}
	

	public synchronized void updateBookmarkFolder(BookmarkListInstaMessage bookmarks, String folderId, boolean fullSynchro) {
		
		
		SQLiteDatabase db = _dbh.getWritableDatabase();
		int folder;
		if(folderId.equals("unread"))
			folder = -1;
		else if(folderId.equals("starred"))
			folder = -2;
		else if(folderId.equals("archive"))
			folder = -3;
		else 
			folder = Integer.parseInt(folderId);
		if(bookmarks.getBookmarkMessagesList().size() != 0){
			LinkedList<Integer> Ids = new LinkedList<Integer>();
			Iterator<BookmarkInstaMessage> it = bookmarks.getBookmarkMessagesList().iterator();
			db.beginTransaction();
			while(it.hasNext()) {
				BookmarkInstaMessage msg = it.next();
				Ids.add(msg.getBookmarkId());
				Cursor c = db.query("Bookmarks", null, "Id = " + String.valueOf(msg.getBookmarkId()), null, null, null, null);
				c.moveToFirst();
				if(c.getCount() == 0){ // there's no bookmark in local DB so we have to create new one
					ContentValues cv = new ContentValues();
					cv.put("Id", msg.getBookmarkId());
					cv.put("Url", msg.getUrl());
					cv.put("Title", msg.getTitle());
					cv.put("Description", msg.getDescription());
					cv.put("Time", msg.getTime());
					cv.put("Starred", msg.getStarred());
					cv.put("PrivateSource", msg.getPrivateSource());
					cv.put("Progress", msg.getProgress());
					cv.put("ProgressTimestamp", msg.getProgressTimestamp());
					cv.put("FolderId", folder);
					if (fullSynchro){
						try {
							cv.put("Hash",msg.getHash());
							cv.put("Body", _client.getBookmarkText(msg.getBookmarkId()).getMessageBody());
						} catch (ApiException e) {
							cv.remove("Body");
						}
					}
					else
						cv.put("Hash", "0");
					db.insert("Bookmarks", null, cv);
				} else if (!c.getString(c.getColumnIndex("Hash")).equals(msg.getHash())){ 
					
					/* we've found bookmark in local DB - 
					 * we need to check if it has changed 
					 * and has to be synchronized again 
					 * if hash is different, bookmark text 
					 * has to be cleaned to be updated when user selects it*/
					ContentValues ucv = new ContentValues();
					if (fullSynchro){
						try {
							ucv.put("Hash",msg.getHash());
							ucv.put("Body", _client.getBookmarkText(msg.getBookmarkId()).getMessageBody());
						} catch (ApiException e) {
							ucv.remove("Body");
						}
					}
					ucv.put("Progress", 0.0f);
					db.update("Bookmarks", ucv, "Id = " + String.valueOf(msg.getBookmarkId()), null);
					}
				
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.beginTransaction();
			/* bookmarks not present in BookmarkListInstaMessage have to be deleted from localDB */
			String[] col = {"Id"};
			Cursor c = db.query("Bookmarks",col, null, null, null, null, null);
			if(c.getCount()!=0)
				while(c.moveToNext())
					if (!Ids.contains(c.getInt(c.getColumnIndex("Id"))))
						db.delete("Bookmarks", "Id = " + String.valueOf(c.getInt(c.getColumnIndex("Id"))) + " AND FolderId = " + String.valueOf(folder), null);
			
			db.setTransactionSuccessful();
			db.endTransaction();
		} else
		{
			db.delete("Bookmarks", "FolderId = " + String.valueOf(folder), null);
		}
		db.close();
		
	}
	
	public synchronized void updateFoldersTable(FoldersListInstaMessage folders, int limit, boolean fullSynchro){
		SQLiteDatabase db = _dbh.getWritableDatabase();
		LinkedList<Integer> Ids = new LinkedList<Integer>();
		Iterator<FolderInstaMessage> it = folders.getFolders().iterator();
		db.beginTransaction();
		while(it.hasNext()){
			FolderInstaMessage folder = it.next();
			Ids.add(folder.getFolder_id());
			Cursor c = db.query("Folders", null, "Id = " + String.valueOf(folder.getFolder_id()), null, null, null, null);
			c.moveToFirst();
			if(c.getCount() == 0){
				ContentValues cv = new ContentValues();
				cv.put("Id", folder.getFolder_id());
				cv.put("Title", folder.getTitle());
				cv.put("SyncToMobile", folder.getSync_to_mobile());
				cv.put("Position", folder.getPosition());
				db.insert("Folders", null, cv);
			} else {
				ContentValues cv = new ContentValues();
				cv.put("Title", folder.getTitle());
				cv.put("SyncToMobile", folder.getSync_to_mobile());
				cv.put("Position", folder.getPosition());
				db.update("Folders", cv, "Id = " + String.valueOf(folder.getFolder_id()), null);
			}
		}
		
		String[] col = {"Id"};
		Cursor c = db.query("Folders",col, null, null, null, null, null);
		if(c.getCount()!=0)
			while(c.moveToNext())
				if (!Ids.contains(c.getInt(c.getColumnIndex("Id")))){
					db.delete("Bookmarks", "FolderId = " + String.valueOf(c.getInt(c.getColumnIndex("Id"))), null);
					db.delete("Folders", "Id = " + String.valueOf(c.getInt(c.getColumnIndex("Id"))), null);
					
				}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		// updating user folders
		Iterator<FolderInstaMessage> fIterator = folders.getFolders().iterator();
		while(fIterator.hasNext())
			updateBookmarkList(limit, String.valueOf(fIterator.next().getFolder_id()), "", fullSynchro);
	}
	
	public synchronized String getSetting(String key) {
		SQLiteDatabase db = _dbh.getReadableDatabase();
		String[] col = {"Value"}; 
		Cursor c = db.query("Settings", col, "Key = '" + key+"'", null, null, null, null);
		c.moveToFirst();
		db.close();
		if(c.getCount() != 1) 
			return null;
		else
			return c.getString(c.getColumnIndex("Value"));	
	}
	
	public synchronized Boolean setSetting(String key, String value){
		SQLiteDatabase db = _dbh.getWritableDatabase();
		String[] col = {"Key"};
		Cursor c = db.query("Settings", col, "Key = '" + key+"'", null, null, null, null);
		c.moveToFirst();
		if(c.getCount() > 0)
			db.delete("Settings", "Key = '" + key + "'", null);
		ContentValues cv = new ContentValues();
		cv.put("Key", key);
		cv.put("Value", value);
		if(db.insert("Settings", null, cv) == -1)
		{
			db.close();
			return false;
		}
		db.close();
		return true;
	}
	
	public synchronized String getBookmarkText(int id, boolean offline){
		SQLiteDatabase db = _dbh.getWritableDatabase();

		String[] col = {"Body"};
    	String[] arg = {String.valueOf(id)};
		Cursor c = db.query("Bookmarks", col, "Id = ?" , arg, null, null, null);
    	if(c.getCount() != 0 && c.moveToFirst())
    	{
    		
    		if(c.getString(c.getColumnIndex("Body")) != null && !c.getString(c.getColumnIndex("Body")).isEmpty()){
    			db.close();
    			return c.getString(c.getColumnIndex("Body"));
    		}
    		else if(!offline && isConnectionAvailable()) {
    			try {
    				BookmarkTextInstaMessage msg = _client.getBookmarkText(id);
    				ContentValues cv = new ContentValues();
    				cv.put("Body", msg.getMessageBody());
    				db.update("Bookmarks", cv, "Id = ?", arg);
    				db.close();
    				return msg.getMessageBody();
				} catch (ApiException e) {
				}
    		}
    		else
    			return _OFFLINE_MODE;
    	}
    	db.close();
    	return _OFFLINE_MODE;
	}
	
	public synchronized Boolean deleteBookmark(int id){
		if( isConnectionAvailable() && _client.delBookmark(id)) {
			SQLiteDatabase db = _dbh.getWritableDatabase();
	    	String[] arg = {String.valueOf(id)};
			db.delete("Bookmarks", "Id = ?" , arg);
			db.close();
			return true;
    	}
    	return false;
	}
	
	public synchronized Boolean archiveBookmark(int id){

		if( isConnectionAvailable()) {
			SQLiteDatabase db = _dbh.getWritableDatabase();
			try {
				BookmarkInstaMessage msg = _client.archiveBookmark(id);
		    	String[] arg = {String.valueOf(id)};
				ContentValues cv = new ContentValues();
				cv.put("Id", msg.getBookmarkId());
				cv.put("Url", msg.getUrl());
				cv.put("Title", msg.getTitle());
				cv.put("Description", msg.getDescription());
				cv.put("Time", msg.getTime());
				cv.put("Starred", msg.getStarred());
				cv.put("PrivateSource", msg.getPrivateSource());
				cv.put("Progress", msg.getProgress());
				cv.put("ProgressTimestamp", msg.getProgressTimestamp());
				cv.put("FolderId", -3);
				db.update("Bookmarks", cv, "Id = ?", arg);
				db.close();
				return true;
			} catch (ApiException e) {
				db.close();
				return false;
			}		
    	}
    	return false;
	}
	
	public synchronized Boolean unarchiveBookmark(int id){

		if( isConnectionAvailable()) {
			SQLiteDatabase db = _dbh.getWritableDatabase();
			try {
				BookmarkInstaMessage msg = _client.unarchiveBookmark(id);
		    	String[] arg = {String.valueOf(id)};
				ContentValues cv = new ContentValues();
				cv.put("Id", msg.getBookmarkId());
				cv.put("Url", msg.getUrl());
				cv.put("Title", msg.getTitle());
				cv.put("Description", msg.getDescription());
				cv.put("Time", msg.getTime());
				cv.put("Starred", msg.getStarred());
				cv.put("PrivateSource", msg.getPrivateSource());
				cv.put("Progress", msg.getProgress());
				cv.put("ProgressTimestamp", msg.getProgressTimestamp());
				cv.put("FolderId", -1);
				db.update("Bookmarks", cv, "Id = ?", arg);
				db.close();
				return true;
			} catch (ApiException e) {
				db.close();
				return false;
			}		
    	}
    	return false;
	}
	
	public synchronized Boolean starBookmark(int id){

		if( isConnectionAvailable()) {
			SQLiteDatabase db = _dbh.getWritableDatabase();
			try {
				BookmarkInstaMessage msg = _client.starBookmark(id);
		    	String[] arg = {String.valueOf(id)};
				ContentValues cv = new ContentValues();
				cv.put("Id", msg.getBookmarkId());
				cv.put("Url", msg.getUrl());
				cv.put("Title", msg.getTitle());
				cv.put("Description", msg.getDescription());
				cv.put("Time", msg.getTime());
				cv.put("Starred", msg.getStarred());
				cv.put("PrivateSource", msg.getPrivateSource());
				cv.put("Progress", msg.getProgress());
				cv.put("ProgressTimestamp", msg.getProgressTimestamp());
				db.update("Bookmarks", cv, "Id = ?", arg);
				db.close();
				return true;
			} catch (ApiException e) {
				db.close();
				return false;
			}		
    	}
    	return false;
	}
	
	public synchronized Boolean unstarBookmark(int id){

		if( isConnectionAvailable()) {
			SQLiteDatabase db = _dbh.getWritableDatabase();
			try {
				BookmarkInstaMessage msg = _client.unstarBookmark(id);
		    	String[] arg = {String.valueOf(id)};
				ContentValues cv = new ContentValues();
				cv.put("Id", msg.getBookmarkId());
				cv.put("Url", msg.getUrl());
				cv.put("Title", msg.getTitle());
				cv.put("Description", msg.getDescription());
				cv.put("Time", msg.getTime());
				cv.put("Starred", msg.getStarred());
				cv.put("PrivateSource", msg.getPrivateSource());
				cv.put("Progress", msg.getProgress());
				cv.put("ProgressTimestamp", msg.getProgressTimestamp());
				db.update("Bookmarks", cv, "Id = ?", arg);
				db.close();
				return true;
			} catch (ApiException e) {
				db.close();
				return false;
			}		
    	}
    	return false;
	}
}
