package net.marioosh.ibeacon;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.connection.BeaconConnection;
import com.estimote.sdk.connection.BeaconConnection.BeaconCharacteristics;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class BeaconSettingsActivity extends Activity {

	protected static final String BC = "BeaconConnection";
	private Beacon beacon;
	private BeaconConnection bc;
	
	private TextView textView;
	private EditText powerEdit;	
	private RelativeLayout relativeLayout1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beacon_settings);
		
		textView = (TextView) findViewById(R.id.textView1);
		powerEdit = (EditText) findViewById(R.id.editText1);
		relativeLayout1 = (RelativeLayout) findViewById(R.id.relativeLayout1);
	
		beacon = getIntent().getParcelableExtra(MainActivity.THIS_BEACON);
		
		reconnect();
		
		Log.i(BC, "Connection...");
		bc.authenticate();
		if(bc.isConnected()) {
			Log.i(BC, "Connected...");
		} else {
			Log.i(BC, "Not Connected...");
		}
		bc.close();
		
		
	}
	
	public void reconnect(View view) {
		reconnect();
	}
	
	public void reconnect() {
		bc =  new BeaconConnection(getApplicationContext(), beacon, new BeaconConnection.ConnectionCallback() {
			@Override
			public void onDisconnected() {
		        runOnUiThread(new Runnable() {
		            @Override public void run() {
		            	textView.setText("Status: Disconnected.");
		            	Log.i(BC, "Disconnected");
		            	relativeLayout1.setVisibility(RelativeLayout.INVISIBLE);
		            }
		        });
			}
			
			@Override
			public void onAuthenticationError() {
		        runOnUiThread(new Runnable() {
		            @Override public void run() {				
						textView.setText("Status: Authentication Error.");
						Log.i(BC, "Authentication Error");
						relativeLayout1.setVisibility(RelativeLayout.INVISIBLE);
		            }
		        });
			}
			
			@Override
			public void onAuthenticated(final BeaconCharacteristics chars) {
		        runOnUiThread(new Runnable() {
		            @Override public void run() {				
						textView.setText("Status: Authenticated to beacon.");
						Log.i(BC, "Authenticated to beacon: " + chars);
						
						relativeLayout1.setVisibility(RelativeLayout.VISIBLE);
						
						String t = chars.getBroadcastingPower().toString();
						powerEdit.setText(t);
						
		            }
		        });
			}
		});
		
	}
	
	public void update(View view) {
		if(bc.isConnected()) {
			Integer p = Integer.parseInt(String.valueOf(powerEdit.getText()));
			bc.writeBroadcastingPower(p, new BeaconConnection.WriteCallback() {
				
				@Override
				public void onSuccess() {
					Toast.makeText(getApplicationContext(), "Updated :)!", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onError() {
					Toast.makeText(getApplicationContext(), "Some error. Update failed!", Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			Toast.makeText(this, "Not Connected. Update failed!", Toast.LENGTH_SHORT).show();
		}
	}
	
	 @Override
	  protected void onResume() {
	    super.onResume();
	    if (!bc.isConnected()) {
	      textView.setText("Status: Connecting...");
	      bc.authenticate();
	    }
	  }

	  @Override
	  protected void onDestroy() {
	    bc.close();
	    super.onDestroy();
	  }
	  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.beacon_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
