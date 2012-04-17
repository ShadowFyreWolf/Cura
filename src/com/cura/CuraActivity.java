/*
 Copyright© 2010, 2011 Ahmad Balaa, Oday Maleh

 This file is part of Cura.

	Cura is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cura is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cura.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cura;

/*
 * Description: This is the activity that the user gets dropped to after they have logged in successfully and this is where
 * all of Cura's facilities are shown (Terminal, SysLog, SysMonitor, etc...). The options provided in this activity are
 * Server Info and Logout. 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cura.Connection.CommunicationInterface;
import com.cura.Connection.ConnectionService;
import com.cura.Terminal.TerminalActivity;
import com.cura.nmap.NmapActivity;
import com.cura.syslog.SysLogActivity;
import com.cura.sysmonitor.SysMonitorActivity;

public class CuraActivity extends Activity implements OnClickListener,
		OnTouchListener {

	private final int LOGOUT = 1;
	private final int SERVER_INFO = 2;

	TableRow terminalRow, sysMonitorRow, sysLogRow, nessusRow, nmapRow,
			mapsRow;
	// menu buttons

	User userTemp;
	// user object

	private CommunicationInterface conn;
	private String page = "";
	// to fetch the GET request of the server's location

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// TODO Auto-generated method stub
			conn = CommunicationInterface.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			conn = null;
			Toast.makeText(CuraActivity.this, "Service Disconnected",
					Toast.LENGTH_LONG).show();
		}
	};

	public synchronized String getUname() {
		String resultUNAME = "";
		// produces the result of "uname -a"
		try {
			resultUNAME = conn.executeCommand("uname -a");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultUNAME;
	}

	public synchronized String getUptime() {
		String resultUPTIME = "";
		// produces the output of "uptime"
		try {
			resultUPTIME = conn.executeCommand("uptime");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultUPTIME;
	}

	public void doBindService() {
		Intent i = new Intent(this, ConnectionService.class);
		bindService(i, connection, Context.BIND_AUTO_CREATE);
		// function needed for binding to the Connection service
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Bundle extras = getIntent().getExtras();
		doBindService();
		if (extras != null) {
			userTemp = extras.getParcelable("user");
			// gets the username on entry
		}
		this.setTitle(userTemp.getUsername() + "'s Control Box");
		// welcoming the user in this activity's title.

		// SETTING CLICK-LISTENERS FOR ALL OF THE BUTTONS
		terminalRow = (TableRow) findViewById(R.id.TerminalRow);
		terminalRow.setOnClickListener(this);
		terminalRow.setOnTouchListener(this);

		sysMonitorRow = (TableRow) findViewById(R.id.SysMonitorRow);
		sysMonitorRow.setOnClickListener(this);
		sysMonitorRow.setOnTouchListener(this);

		sysLogRow = (TableRow) findViewById(R.id.SysLogRow);
		sysLogRow.setOnClickListener(this);
		sysLogRow.setOnTouchListener(this);

		nmapRow = (TableRow) findViewById(R.id.NMapRow);
		nmapRow.setOnClickListener(this);
		nmapRow.setOnTouchListener(this);

		BufferedReader in = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			if (userTemp.getDomain().compareTo("wu.ourproject.org") == 0
					&& userTemp.getUsername().compareTo("root") == 0) {
				request.setURI(new URI(
						"http://api.hostip.info/get_html.php?ip=12.215.42.19&position=true"));
			} else {
				request.setURI(new URI(
						"http://api.hostip.info/get_html.php?ip="
								+ userTemp.getDomain() + "&position=true"));
			}
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			page = sb.toString();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.TerminalRow:
			// when the row entitled "Terminal" is clicked, take the user to the
			// terminal activity.
			// terminalRow.setBackgroundColor(R.drawable.moduleselectedhighlight);
			Intent terminalIntent = new Intent(this, TerminalActivity.class);
			terminalIntent.putExtra("user", userTemp);

			// send userTemp (the current user)'s name along with the intent so
			// that the name can be displayed there as well
			startActivity(terminalIntent);
			break;
		case R.id.SysMonitorRow:
			// when the row entitled "SysMonitor" is clicked, take the user the
			// SysMonitor activity.
			Intent sysMonitorIntent = new Intent(this, SysMonitorActivity.class);
			startActivity(sysMonitorIntent);
			break;
		case R.id.SysLogRow:
			// when the row entitled "SysLog" is clicked, take the user to
			// the SysLog activity
			Intent sysLogIntent = new Intent(this, SysLogActivity.class);
			startActivity(sysLogIntent);
			break;
		case R.id.NMapRow:
			// when the row entitled "Nmap" is clicked, take the user
			// to the Nmap activity
			// if ((userTemp.getUsername()).compareTo("root") == 0) {
			// if the user is root, allow them to access this activity
			// if they are not, don't allow them
			Intent nmapIntent = new Intent(this, NmapActivity.class);
			nmapIntent.putExtra("user", userTemp);
			startActivity(nmapIntent);
			// } else {
			// Toast.makeText(
			// CuraActivity.this,
			// "Error! You are not allowed to access the Nmap module if you do not have root privileges over this server.",
			// Toast.LENGTH_LONG).show();
			// }
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, SERVER_INFO, 0, R.string.GetServerInfoOptionMenu).setIcon(
				android.R.drawable.ic_menu_info_details);
		// creates the options menu that includes "Server Info" and "Logout"
		menu.add(0, LOGOUT, 0, R.string.logout).setIcon(
				R.drawable.ic_lock_power_off);
		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case LOGOUT:
			// Confirm logout for user
			new AlertDialog.Builder(this)
					.setTitle("Logout Confirmation")
					.setMessage(R.string.logoutConfirmationDialog)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									try {
										// close the connection
										conn.close();
										// unbind the service
										unbindService(connection);
										Log.d("Connection", "connection closed");
									} catch (RemoteException e) {
										Log.d("Connection", e.toString());
									}
									Intent closeAllActivities = new Intent(
											CuraActivity.this,
											LoginScreenActivity.class);
									// return the user to the login screen
									// activity
									// just close everything
									closeAllActivities
											.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									CuraActivity.this
											.startActivity(closeAllActivities);
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								// if No is chosen, don't do anything and
								// dismiss the dialog
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			break;
		case SERVER_INFO:
			String finalResultForDialog = "";
			// if "Server info" is selected, produce the output of Uptime and
			// Uname, concatenate them into a paragraph and display it for the
			// user
			finalResultForDialog = getUname() + "\n"
					+ getString(R.string.uptimeText) + getUptime() + "\n"
					+ getString(R.string.userLocation) + "\n" + page;
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.ServerInfoDialog);
			final TextView infoArea = new TextView(this);
			infoArea.setText(finalResultForDialog);
			alert.setView(infoArea);
			alert.setNegativeButton("Ok",
					new DialogInterface.OnClickListener() {
						// UPON CLICKING "CANCEL" IN THE DIALOG BOX (ALERT)
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});
			alert.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// unbindService(connection);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unbindService(connection);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			// if the back button is pressed when the user is in this (Cura
			// Activity)
			new AlertDialog.Builder(this).setTitle("Logout Confirmation")
					// confirm logout
					.setMessage(R.string.logoutConfirmationDialog)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									try {
										// close connection
										conn.close();
										// unbind from the service
										unbindService(connection);
										Log.d("Connection", "connection closed");
									} catch (RemoteException e) {
										Log.d("Connection", e.toString());
									}
									Intent closeAllActivities = new Intent(
											CuraActivity.this,
											LoginScreenActivity.class);
									// just close everything
									closeAllActivities
											.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									CuraActivity.this
											.startActivity(closeAllActivities);
								}
							}).setNegativeButton("No",
					// if No is selected, dismiss the dialog
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			v.setBackgroundResource(R.drawable.moduleselectedhighlight);
			break;
		case MotionEvent.ACTION_UP:
			v.setBackgroundResource(0);
			break;
		}

		return false;
	}

}