package org.addhen.whereami;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Whereami extends Activity {
    /** Called when the activity is first created. */
	
	private EditText vCode;
	private Button saveBtn;
	private Button startService;
	private Button stopService;
	private SharedPreferences.Editor editor;
	private boolean isServiceRunning = false;
	
	public static final String VERIFICATION_CODE = "VERIFICATION_CODE";
	
	public void updateBtns() {
    	if( isServiceRunning ) {
			startService.setEnabled(false);
			stopService.setEnabled(true);
		} else {
			startService.setEnabled(true);
			stopService.setEnabled(false);
		}
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        vCode = (EditText)findViewById(R.id.vcode);
        
        saveBtn = (Button)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener(){
			
			public void onClick(View v) {
				String vcode = vCode.getText().toString();
            	
				if( !vcode.trim().equals("") ){
                	editor.putString(VERIFICATION_CODE, vcode);
                	editor.commit();            		
            		vCode.setText(vcode);
            	}
				
			}
		});

        startService = (Button) findViewById(R.id.start_service);
        startService.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
				
				Intent mIntent = new Intent(Whereami.this,LocationService.class);
				
        		mIntent.putExtra("dest", "foobar");
				
        		startService(mIntent);
        		
        		isServiceRunning = true;
        		updateBtns();
        	}
        });
        
        stopService = (Button) findViewById(R.id.stop_service);
        stopService.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		stopService(new Intent(Whereami.this,
                        LocationService.class));
        		isServiceRunning = false;
        		updateBtns();
        	}
        });
        
        SharedPreferences vCodePref = getSharedPreferences( VERIFICATION_CODE, 0);
        String vCodeStr = vCodePref.getString(VERIFICATION_CODE, null); 
        editor = vCodePref.edit();
        
      //Prepopulate the vcode field with the current code
		vCode.setText(vCodeStr);
		this.updateBtns();
	}
    
    public static void sendLocationInfo( String location ) {
    	Log.i("Location", "Timer stopped!!! "+location );
    }
    
   
}