package paperdroid.application;

import paperdroid.application.DataSyncService.DataSyncBinder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeScreen extends Activity{
	private DataSyncService _service;
	private boolean _bound = false;
	private boolean _firstStart = true;
	private static final String _STATE_FIRSTSTART = "firstStart";
	private TextView _usernameLabel, _passwordLabel, _usernameTextEdit, _passwordTextEdit, _loginFailedMessage;
	private Button _logButton, _switchActivityButton, _offlineButton;
	
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
        
		setContentView(R.layout.welcome_screen);
		_logButton = (Button)findViewById(R.id.welcome_log_button);
	}

	@Override
	public void onStart() {
	    super.onStart();
	    if(!_bound)
	    bindService(new Intent(this, DataSyncService.class), _connection,
	            Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
    public void onResume() {
    	super.onResume();   
    	_switchActivityButton = (Button) findViewById(R.id.welcome_change_activity);
    	_logButton = (Button) findViewById(R.id.welcome_log_button);
    	_offlineButton = (Button) findViewById(R.id.welcome_offline_button);
    	_passwordLabel = (TextView) findViewById(R.id.welcome_password);
    	_passwordTextEdit = (TextView) findViewById(R.id.welcome_password_edit);
    	_usernameLabel = (TextView) findViewById(R.id.welcome_username);
    	_usernameTextEdit = (TextView) findViewById(R.id.welcome_username_edit);
    	if(_firstStart) {
	    	new Thread(new Runnable() {
				public void run() {
					while(!_bound)
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					_service.setSetting("offline", "0");
					_firstStart = false;
		    		_service.initializeClient();
		    		if(_service.isTokenSet())
		    			_switchActivityButton.post(new Runnable() {
		                    public void run() {
		                    	_service.setSetting("offline", "0");
		                    	_switchActivityButton.callOnClick();
		                    }
		                });
		    		else
		    		{
		    			_logButton.post(new Runnable() {
		                    public void run() {
		                    	_logButton.setVisibility(View.VISIBLE);
		                    }
		                });
		    			_offlineButton.post(new Runnable() {
		                    public void run() {
		                    	_offlineButton.setVisibility(View.VISIBLE);
		                    }
		                });
		    			_passwordLabel.post(new Runnable() {
		                    public void run() {
		                    	_passwordLabel.setVisibility(View.VISIBLE);
		                    }
		                });
		    			_passwordTextEdit.post(new Runnable() {
		                    public void run() {
		                    	_passwordTextEdit.setVisibility(View.VISIBLE);
		                    }
		                });
		    			_usernameLabel.post(new Runnable() {
		                    public void run() {
		                    	_usernameLabel.setVisibility(View.VISIBLE);
		                    }
		                });
		    			_usernameTextEdit.post(new Runnable() {
		                    public void run() {
		                    	_usernameTextEdit.setVisibility(View.VISIBLE);
		                    	_usernameTextEdit.setText(_service.getSetting("username"));
		                    }
		                });
		    		}
				}
	    	}).start();
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(_STATE_FIRSTSTART, _firstStart);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        _firstStart = savedInstanceState.getBoolean(_STATE_FIRSTSTART);        
    }
	
	
	public void onChangeActivityClicked(View view) {
		Intent i = new Intent(WelcomeScreen.this, PaperDroidActivity.class);
		WelcomeScreen.this.startActivity(i);		
		this.finish();
	}
	
	public void onLogButtonClicked(View view) {
		_loginFailedMessage = (TextView) findViewById(R.id.login_failed);
		_passwordTextEdit = (TextView) findViewById(R.id.welcome_password_edit);
		_usernameTextEdit = (TextView) findViewById(R.id.welcome_username_edit);
		_service.setSetting("username", _usernameTextEdit.getText().toString());
		_service.setSetting("password", _passwordTextEdit.getText().toString());
		_passwordTextEdit.setText("");
		final ProgressDialog dialog = ProgressDialog.show(WelcomeScreen.this, "", 
                "Your phone is connecting to Instapaper...", true, true);
    	new Thread(new Runnable() {
			public void run() {
				while(!_bound)
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				_firstStart = false;
	    		_service.initializeClient(dialog);
	    		if(_service.isTokenSet())
	    			_switchActivityButton.post(new Runnable() {
	                    public void run() {
	                    	_switchActivityButton.callOnClick();
	                    }
	                });
	    		else
	    			_loginFailedMessage.post(new Runnable() {
	                    public void run() {
	                    	_loginFailedMessage.setVisibility(View.VISIBLE);
	                    }
	                });
			}
    	}).start();	
	}
	
	public void onOfflineButtonClicked(View v){
		_service.setSetting("offline", "1");
		onChangeActivityClicked(v);
	}
}
