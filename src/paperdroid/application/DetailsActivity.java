package paperdroid.application;

import instapaper.api.ApplicationDatabaseHelper;
import paperdroid.application.DataSyncService.DataSyncBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;

public class DetailsActivity extends Activity implements OnLongClickListener{
	DataSyncService _service;
	public  boolean _bound = false;
	private boolean _firstStart = true;
	private boolean _fullScreen = false;
	private int _folderId;
	private int _starred;
	private static String _STATE_ID = "id";
	private static final String _STATE_FIRSTSTART = "firstStart";
	private static final String _STATE_FULSCREEN = "fullscreen";
	private int _id = 0;
	private WebView _wv;
	
	private ServiceConnection _connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			DataSyncBinder binder = (DataSyncBinder) service;
			_service = binder.getService();
			_bound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			_service = null;
			_bound = false;
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      	_id = getIntent().getExtras().getInt("id");
      	getBookmarkProperties();
      	_wv = new WebView(this);
      	_wv.setOnLongClickListener(this);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if(!_bound)
        bindService(new Intent(this, DataSyncService.class), _connection,
                Context.BIND_AUTO_CREATE);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	getActionBar().setDisplayShowTitleEnabled(false);
    	if(_fullScreen)
            getActionBar().hide();
    	_wv.setOnLongClickListener(this);
    	if (_firstStart == true){
    		
	    	new Thread(new Runnable() {
				public void run() {
					while(!_bound)
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					final boolean offline = _service.getSetting("offline").equals("1");
					final String bookmarkBody = _service.getBookmarkText(_id, offline);
					_wv.post(new Runnable() {
						@Override
						public void run() {
							_wv.loadData(bookmarkBody, "text/html", "UTF-8");	
						}
					});
				}
	    	}).start();
	    	
	        setContentView(_wv);
    	}
    	
    }
    
    @Override 
    public void onDestroy() {
    	super.onDestroy();
    	if (_bound) {
    		unbindService(_connection);
    		_bound = false;
    	}
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_activity_menu, menu);
        if(_service.getSetting("offline").equals("1")){
        	menu.getItem(0).setEnabled(false);
        	menu.getItem(1).setEnabled(false);
        	menu.getItem(2).setEnabled(false);
        }
        if(_starred == 1)
        {
        	menu.getItem(0).setChecked(true);
        	menu.getItem(0).setIcon(R.drawable.menu_star);
        }
        if(_folderId == -3)
        {
        	menu.getItem(1).setChecked(true);
        	menu.getItem(1).setIcon(R.drawable.menu_archive);
        }	
        return true;
    }	
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	super.onOptionsItemSelected(item);
    	switch (item.getItemId()) {
    	case R.id.fullscreen_details_menu:
    		getActionBar().hide();
    		_fullScreen = true;
    		break;
    	case R.id.delete_details_menu:
    		onDeleteClicked();
    		break;
    	case R.id.archive_details_menu:
    		onArchiveClicked(item);
    		break;
    	case R.id.star_details_menu:
    		onStarClicked(item);
    		break;
		default:
			break;
		}   	
    	return true;
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        _firstStart = savedInstanceState.getBoolean(_STATE_FIRSTSTART);
        _id = savedInstanceState.getInt(_STATE_ID);
        _fullScreen = savedInstanceState.getBoolean(_STATE_FULSCREEN);
        
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(_STATE_FIRSTSTART, _firstStart);
        savedInstanceState.putInt(_STATE_ID, _id);
        savedInstanceState.putBoolean(_STATE_FULSCREEN, _fullScreen);
        super.onSaveInstanceState(savedInstanceState);
    }

	@Override
	public boolean onLongClick(View v) {
		if(_fullScreen){
			getActionBar().show();
			_fullScreen = false;
			return true;
		}
		return false;
	}
	
	public void getBookmarkProperties(){
		ApplicationDatabaseHelper dbh = new ApplicationDatabaseHelper(this);
		SQLiteDatabase db = dbh.getReadableDatabase();
		String[] col = {"Starred", "FolderId"};
		Cursor c = db.query("Bookmarks", col, "Id = " + String.valueOf(_id), null, null, null, null);
		if(c.moveToFirst())
		{
			_starred = c.getInt(c.getColumnIndex("Starred"));
			_folderId = c.getInt(c.getColumnIndex("FolderId"));
		}
		db.close();
	}

	public void onDeleteClicked(){
		new Thread(new Runnable() {
			public void run() {
				_service.deleteBookmark(_id);
				runOnUiThread(new Runnable() {
	        		public void run(){
	        			finish();
	        		}
				});
			}
		}).start();
		
		
	}
	public void onArchiveClicked(final MenuItem item){
		new Thread(new Runnable() {
			public void run() {
				if(!item.isChecked() && _service.archiveBookmark(_id)){
					runOnUiThread(new Runnable() {
		        		public void run(){
		        			item.setIcon(R.drawable.menu_archive);
							item.setChecked(true);
		        		}
					});
					_folderId = -3;
					
				}else if (item.isChecked() && _service.unarchiveBookmark(_id)){
					runOnUiThread(new Runnable() {
		        		public void run(){
							item.setIcon(R.drawable.menu_unarchive);
							item.setChecked(false);
		        		}
					});
					_folderId = -1;
				}
			}
		}).start();
		
	}
	public void onStarClicked(final MenuItem item){
		new Thread(new Runnable() {
			public void run() {
				if(!item.isChecked() && _service.starBookmark(_id)){
					runOnUiThread(new Runnable() {
		        		public void run(){
		        			item.setIcon(R.drawable.menu_star);
		        			item.setChecked(true);    
		        		}
					});
					_starred = 1;
					
				}else if (item.isChecked() && _service.unstarBookmark(_id)){
					runOnUiThread(new Runnable() {
		        		public void run(){
		        			item.setIcon(R.drawable.menu_unstar);	
							item.setChecked(false);
		        		}
					});
					_starred = 0;
				}
			}
		}).start();
	}
}
