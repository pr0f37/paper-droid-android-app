package instapaper.api;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ApplicationDatabaseHelper extends SQLiteOpenHelper {
	private static final int _DATABASE_VERSION = 7;
	
	private static final String _DATABASE_NAME = "aPaperDB";
	private static final String _BOOKMARKS_TABLE_CREATE =
	            "CREATE TABLE Bookmarks (" +
	            "Id INT, " +
	            "Url TEXT, " +
	            "Title TEXT, " +
	            "Description TEXT, " +
	            "Time INT, " +
	            "Starred INT, " +
	            "PrivateSource TEXT, " +
	            "Hash TEXT, " +
	            "Progress Float, " +
	            "ProgressTimestamp INT," +
	            "Body TEXT, " +
	            "FolderId INT" +	
	            ");";
	private static final String _BOOKMARKS_TABLE_DROP = "DROP TABLE Bookmarks;";
	private static final String _FOLDER_TABLE_CREATE =
            "CREATE TABLE Folders (" +
            "Id INT, " +
            "Title TEXT, " +
            "SyncToMobile INT, " +
            "Position INT " +	
            ");";
	private static final String _FOLDER_TABLE_DROP = "DROP TABLE Folders;";
	private static final String _SETTINGS_TABLE_CREATE =
            "CREATE TABLE Settings (" +
            "Key TEXT, " +
            "Value TEXT " +
            ");";
	private static final String _SETTINGS_TABLE_DROP = "DROP TABLE Settings;";
	public ApplicationDatabaseHelper(Context context) {
        super(context, _DATABASE_NAME, null, _DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(_BOOKMARKS_TABLE_CREATE);
		db.execSQL(_FOLDER_TABLE_CREATE);
		db.execSQL(_SETTINGS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int OldVer, int NewVer) {
		if (OldVer< NewVer) {
			db.execSQL(_BOOKMARKS_TABLE_DROP);
			db.execSQL(_BOOKMARKS_TABLE_CREATE);
			db.execSQL(_FOLDER_TABLE_DROP);
			db.execSQL(_FOLDER_TABLE_CREATE);
			db.execSQL(_SETTINGS_TABLE_DROP);
			db.execSQL(_SETTINGS_TABLE_CREATE);
		}
		
	}

}
