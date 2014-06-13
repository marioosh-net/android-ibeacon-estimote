package net.marioosh.ibeacon;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.connection.BeaconConnection;
import com.estimote.sdk.utils.L;

public class MainActivity extends ActionBarActivity {

	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
	
	/**
	 * specyficzny bekon, major i minor podany
	 */
	private static final Region MY_ESTIMOTE_BEACON = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, 63890, 27793);
	
	protected static final String TAG = "TAG";
	private BeaconManager beaconManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		beaconManager = new BeaconManager(getApplicationContext());
		
		if(!beaconManager.isBluetoothEnabled()) {
			Toast.makeText(getApplicationContext(), "To nie bêdzie dzia³aæ. W³¹czy³eœ Bluetooth'a?", Toast.LENGTH_LONG).show();
		}
		
		if(beaconManager.isBluetoothEnabled()) { 
			beaconManager.setRangingListener(new BeaconManager.RangingListener() {
				@Override
				public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
					Log.i(TAG, "Ranged beacons: " + beacons);
					Log.i(TAG, Utils.computeAccuracy(beacons.get(0))+"");
					
					double odl = Utils.computeAccuracy(beacons.get(0));
					TextView status = (TextView) findViewById(R.id.textView2);
					status.setText(String.format("%1$,.2f", odl));
					
					SeekBar sb = (SeekBar) findViewById(R.id.seekBar1);
					sb.setProgress((int) ((10-odl*2)+1));
					
					if(odl < 0.5) {
						Toast.makeText(getApplicationContext(), "Mam Ciê!", Toast.LENGTH_SHORT).show();						
						Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
						startActivity(intent);
					}
				}
			});
			beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {				
				@Override
				public void onExitedRegion(Region arg0) {
					Toast.makeText(getApplicationContext(), "Poza regionem", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onEnteredRegion(Region arg0, List<Beacon> arg1) {
					Toast.makeText(getApplicationContext(), "W regionie", Toast.LENGTH_SHORT).show();
				}
			});
			
			initSwitches();
		
		}

	}
	
	private void initSwitches() {
		/**
		 * On / Off Ranging / Monitoring
		 */
		Switch rangingSwitch = (Switch) findViewById(R.id.switch1);
		Switch monitoringSwitch = (Switch) findViewById(R.id.switch2);
		
		monitoringSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					try {
						beaconManager.startMonitoring(MY_ESTIMOTE_BEACON);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						beaconManager.stopMonitoring(MY_ESTIMOTE_BEACON);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		rangingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					try {
						beaconManager.startRanging(MY_ESTIMOTE_BEACON);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						beaconManager.stopRanging(MY_ESTIMOTE_BEACON);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * wyjscie
	 * @param view
	 */
	public void end(View view) {
		finish();
	}
	
	@Override
	protected void onStart() {
		if(beaconManager.isBluetoothEnabled()) {
			beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
				@Override
				public void onServiceReady() {
					try {
						beaconManager.startRanging(MY_ESTIMOTE_BEACON);
						beaconManager.startMonitoring(MY_ESTIMOTE_BEACON);
					} catch (RemoteException e) {
						Log.e(TAG, "Cannot start ranging", e);
					}
				}
			});
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		try {
			if(beaconManager.isBluetoothEnabled()) {
				beaconManager.stopRanging(MY_ESTIMOTE_BEACON);
				beaconManager.stopMonitoring(MY_ESTIMOTE_BEACON);
			}
		} catch (RemoteException e) {
			Log.e(TAG, "Cannot stop but it does not matter now", e);
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if(beaconManager.isBluetoothEnabled()) {
			beaconManager.disconnect();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
