package paperdroid.application;

import paperdroid.application.DataSyncService.DataSyncBinder;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class PaperDroidActivity extends Activity {
	DataSyncService _service;
	public volatile boolean _bound = false;
	private boolean _firstStart = true;
	private int _tabIndex = 0;
	
	private static final String _STATE_FIRSTSTART = "firstStart"; 
	private static final String _STATE_TABINDEX = "tabIndex";
	static final int _DIALOG_AUTH_ID = 0;
	EditText _authUsername, _authPassword;
	Button _authDialogOkButton, _authDialogCancelButton;
	
	
	
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        

    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Binding to the service
        if(!_bound)
        bindService(new Intent(this, DataSyncService.class), _connection,
                Context.BIND_AUTO_CREATE);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	prepareActionBar();
    	    
    }
    
    @Override
    public void onPause() {
    	_tabIndex = getActionBar().getSelectedNavigationIndex();
    	super.onPause();
    }
    
    @Override 
    public void onStop() {
        
    	super.onStop();
    	
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
        inflater.inflate(R.menu.paper_droid_activity_menu, menu);
        if(_service.getSetting("offline").equals("1"))
        	menu.getItem(2).setEnabled(false);
        return true;
    }	
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	super.onOptionsItemSelected(item);
    	switch (item.getItemId()) {
		case R.id.menu_refresh:
			refreshLists(item);
			break;
		case R.id.menu_preferences:
			showPreferences();
			break;
		case R.id.menu_logout:
			logout();
			break;
		default:
			break;
		}
    	return true;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(_STATE_FIRSTSTART, _firstStart);
        savedInstanceState.putInt(_STATE_TABINDEX, getActionBar().getSelectedNavigationIndex());
        getActionBar().removeAllTabs();
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        _firstStart = savedInstanceState.getBoolean(_STATE_FIRSTSTART);
        _tabIndex =  savedInstanceState.getInt(_STATE_TABINDEX);
    }
    
    
    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          */
        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
                
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }
    }

    
    
    public void resetPassword(){
    	_service.setSetting("password", "");
    }


	public void onButtonClicked(View view) {
		resetPassword();
	}
 
	public void refreshLists(final MenuItem item){
		
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

	    Animation rotation = AnimationUtils.loadAnimation(this, R.anim.image_click);
	    rotation.setRepeatCount(Animation.INFINITE);
	    iv.startAnimation(rotation);

	    item.setActionView(iv);
		_tabIndex = getActionBar().getSelectedNavigationIndex();
		new Thread(new Runnable() {
			public void run() {
				if(_bound) {
					Boolean unreadSynch =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("unread_synchro", false) && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("synchronization_full", false);
					Boolean favouritesSynch = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("favourites_synchro", false) && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("synchronization_full", false);
					Boolean archivedSynch = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("archived_synchro", false) && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("synchronization_full", false);
					int limit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("limit", "500"));
		    		_service.notifyUser("aPaper", "Synchronising articles", "Synchronizing articles in progress");
		        	_service.updateBookmarkList(limit, "unread", "", unreadSynch);
		        	_service.updateBookmarkList(limit, "starred", "", favouritesSynch);
		        	_service.updateBookmarkList(limit, "archive", "", archivedSynch);
		        	
		        	// TODO: create user folders synchro preference
		        	//_service.updateFoldersList(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("limit", "500")), false);
		        	switch (_tabIndex) {
		        	case 0:
						final BrowsingFragment browse = (BrowsingFragment)getFragmentManager().findFragmentByTag("browse_fragment");
						browse.getListView().post(new Runnable() {
			        		public void run(){
			        			browse.populateList();    
			        		}
						});
						break;
					case 1:
						final ArchivedListFragment arch = (ArchivedListFragment)getFragmentManager().findFragmentByTag("archive_fragment");
						arch.getListView().post(new Runnable() {
			        		public void run(){
			        			arch.populateList();    
			        		}
						});
						break;
					case 2:
						final FavouriteListFragment fav = (FavouriteListFragment)getFragmentManager().findFragmentByTag("fav_fragment");
			        	fav.getListView().post(new Runnable() {
			        		public void run(){
			        			fav.populateList();        			
			        		}
						});
					default:
						break;
					}  	        	
		        	_service.cancelNotification();
		    		item.getActionView().post(new Runnable(){
						public void run() {
				    		item.getActionView().clearAnimation();
				            item.setActionView(null);
				
						}
		    			
		    		});

		    	}
			}
		}).start();
		    	
	}
	
    public void showPreferences(){
    	Intent i = new Intent(this, UserPreferencesActivity.class);
    	startActivity(i);
    }
    
    public void logout() {
    	_service.setSetting("username", "");
    	Intent i = new Intent(this, WelcomeScreen.class);
    	startActivity(i);
    	finish();
    }
	
	public void prepareActionBar(){
		ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        
        Tab tab = actionBar.newTab()
        		.setText("Unread")
        		.setTabListener(new TabListener<BrowsingFragment>(
                        this, "browse_fragment", BrowsingFragment.class));
        actionBar.addTab(tab);
        tab = actionBar.newTab()
        		.setText("Archive")
        		.setTabListener(new TabListener<ArchivedListFragment>(
                        this, "archive_fragment", ArchivedListFragment.class));
        actionBar.addTab(tab);
        tab = actionBar.newTab()
        		.setText("Favourites")
        		.setTabListener(new TabListener<FavouriteListFragment>(
                        this, "fav_fragment", FavouriteListFragment.class));
        actionBar.addTab(tab);
        actionBar.setSelectedNavigationItem(_tabIndex);
	}    
}