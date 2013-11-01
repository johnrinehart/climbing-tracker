package com.herokuapp.climbingtracker;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.content.Intent;
import android.database.SQLException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.*;
import com.facebook.model.GraphUser;

public class MainActivity extends FragmentActivity implements OnItemSelectedListener, OnCheckedChangeListener{

	private static final int SPLASH = 0;
	private static final int SELECTION = 1;
	private static final int SETTINGS = 2;
	private static final int FRAGMENT_COUNT = SETTINGS +1;
	
	private MenuItem settings;

	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

	private boolean isResumed = false;

	int _grau = 0;
	int _quedas = 0;
	int _parede = 0;
	boolean _guiada = false;

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
		isResumed = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
		isResumed = false;
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		// Only make changes if the activity is visible
		if (isResumed) {
			FragmentManager manager = getSupportFragmentManager();
			// Get the number of entries in the back stack
			int backStackSize = manager.getBackStackEntryCount();
			// Clear the back stack
			for (int i = 0; i < backStackSize; i++) {
				manager.popBackStack();
			}
			if (state.isOpened()) {
				// If the session state is open:
				// Show the authenticated fragment
				showFragment(SELECTION, false);
			} else if (state.isClosed()) {
				// If the session state is closed:
				// Show the login fragment
				showFragment(SPLASH, false);
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.equals(settings)) {
	        showFragment(SETTINGS, true);
	        return true;
	    }
	    return false;
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Session session = Session.getActiveSession();

		if (session != null && session.isOpened()) {
			// if the session is already open,
			// try to show the selection fragment
			showFragment(SELECTION, false);
		} else {
			// otherwise present the splash screen
			// and ask the person to login.
			showFragment(SPLASH, false);
		}
	}

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = 
			new Session.StatusCallback() {
		@Override
		public void call(Session session, 
				SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		uiHelper.onSaveInstanceState(outState);
		outState.putInt("grau", _grau);
		outState.putInt("paredes", _parede);
		outState.putInt("quedas", _quedas);
		outState.putBoolean("isGuiada", _guiada);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		boolean guiada = savedInstanceState.getBoolean("isGuiada");
		int quedas = savedInstanceState.getInt("quedas");
		int parede = savedInstanceState.getInt("paredes");
		int grau = savedInstanceState.getInt("grau");

		Spinner spnParedes = (Spinner) findViewById(R.id.spnParedes);
		spnParedes.setSelection(parede);
		Spinner spnQuedas = (Spinner) findViewById(R.id.spnQuedas);
		spnQuedas.setSelection(quedas);
		Spinner spnGraus = (Spinner) findViewById(R.id.spnGraus);
		spnGraus.setSelection(grau);
		ToggleButton isGuiada = (ToggleButton) findViewById(R.id.isGuiada);
		isGuiada.setActivated(guiada);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    // only add the menu when the selection fragment is showing
	    if (fragments[SELECTION].isVisible()) {
	        if (menu.size() == 0) {
	            settings = menu.add(R.string.settings);
	        }
	        return true;
	    } else {
	        menu.clear();
	        settings = null;
	    }
	    return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		FragmentManager fm = getSupportFragmentManager();
		fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
		fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
		fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
		fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);

		FragmentTransaction transaction = fm.beginTransaction();
		for(int i = 0; i < fragments.length; i++) {
			transaction.hide(fragments[i]);
		}
		transaction.commit();

		setupUI(findViewById(R.id.parent));

		Spinner spnParedes = (Spinner) findViewById(R.id.spnParedes);
		spnParedes.setOnItemSelectedListener(this);
		Spinner spnQuedas = (Spinner) findViewById(R.id.spnQuedas);
		spnQuedas.setOnItemSelectedListener(this);
		Spinner spnGraus = (Spinner) findViewById(R.id.spnGraus);
		spnGraus.setOnItemSelectedListener(this);
		ToggleButton isGuiada = (ToggleButton) findViewById(R.id.isGuiada);
		isGuiada.setOnCheckedChangeListener(this);

					// start Facebook Login
					Session.openActiveSession(this, true, new Session.StatusCallback() {
		
						// callback when session changes state
						@Override
						public void call(Session session, SessionState state, Exception exception) {
							if (session.isOpened()) {
		
								// make request to the /me API
								Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
		
									// callback after Graph API response with user object
									@Override
									public void onCompleted(GraphUser user, Response response) {
										if (user != null) {
											TextView welcome = (TextView) findViewById(R.id.welcome);
											welcome.setText(user.getName());
										}
									}
								});
							}
						}
					});
	}

	public void setupUI(View view) {

		//Set up touch listener for non-text box views to hide keyboard.
		if(!(view instanceof ImageView)) {

			view.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					hideImageView();
					return false;
				}

			});
		}

		//If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {

			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

				View innerView = ((ViewGroup) view).getChildAt(i);

				setupUI(innerView);
			}
		}
	}

	public void hideImageView() {
		ImageView img = (ImageView) findViewById(R.id.imgMapa);
		img.setImageResource(0);
	}

	private void showFragment(int fragmentIndex, boolean addToBackStack) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			if (i == fragmentIndex) {
				transaction.show(fragments[i]);
			} else {
				transaction.hide(fragments[i]);
			}
		}
		if (addToBackStack) {
			transaction.addToBackStack(null);
		}
		transaction.commit();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	public void onisGuiadaClicked(View view) {
		_guiada = true;

		//Acha o Spinner pelo ID
		Spinner paredes = (Spinner) findViewById(R.id.spnParedes);

		String[] TipoDeParede;
		ArrayAdapter<String> adapter;
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			TipoDeParede = getResources().getStringArray(R.array.guiadas); 
		} else {
			TipoDeParede = getResources().getStringArray(R.array.tops);
		}

		//Inicializa o adapter com o array que foi selecionado
		adapter = new ArrayAdapter<String>(
				this,
				android.R.layout.simple_list_item_1,
				TipoDeParede);

		paredes.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	public void PublicarClicked(View view) {

	}

	public void btnMapaClicked(View view) {
		ImageView img = (ImageView) findViewById(R.id.imgMapa);
		img.setImageResource(R.drawable.cp_map);
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Spinner spinner = (Spinner) parent;
		switch(spinner.getId()){
		case R.id.spnGraus:
			_grau = pos;
			DataBaseHelper myDbHelper = new DataBaseHelper(this); //new DataBaseHelper();
			//myDbHelper = new DataBaseHelper(this);

			try {

				myDbHelper.createDataBase();

			} catch (IOException ioe) {

				throw new Error("Unable to create database");

			}

			try {

				myDbHelper.openDataBase();
				
				ArrayList<String> walls = myDbHelper.getWallArray(spinner.getSelectedItem().toString());
				Spinner paredes = (Spinner) findViewById(R.id.spnParedes);
				SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, walls);
				paredes.setAdapter(adapter);

			}catch(SQLException sqle){

				throw sqle;

			}
			break;
		case R.id.spnQuedas:
			_quedas = pos;
			break;
		case R.id.spnParedes:
			_parede = pos;
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		_guiada = arg1;
	}

}
