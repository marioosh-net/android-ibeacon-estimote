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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

public class MainActivity extends ActionBarActivity {

	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
	
	/**
	 * specyficzny bekon, major i minor podany
	 */
	private static final Region MY_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, 63890, 27793);
	
	protected static final String TAG = "TAG";
	private BeaconManager beaconManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

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
		}
		

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
						beaconManager.startRanging(MY_ESTIMOTE_BEACONS);
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
				beaconManager.stopRanging(MY_ESTIMOTE_BEACONS);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
