package com.ajani;







import com.ajani.EngineView.EngineThread;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Proj3Activity extends Activity {
   
	Spinner levels;
	Button begin_button;
	  
	
	private EngineView EView;
	private EngineThread Ethread; 
	private int level;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  
        
        levels = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levels.setAdapter(adapter);
        levels.setOnItemSelectedListener(new LevelSelectListener());
        
        
        begin_button = (Button) findViewById(R.id.button1);
        begin_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	//startActivityForResult(new Intent(v.getContext(), EngineActivity.class), 0);
            //	Toast.makeText(getApplicationContext(), "You want to START!", Toast.LENGTH_SHORT).show();
            
               	setContentView(R.layout.engine);  
               	
               	EView = (EngineView) findViewById(R.id.engine);
            	Ethread = EView.getThread();        			
            	Ethread.setLevel(level);  
            	         	 
            	// give the MarsView a handle to the TextView used for messages 
            	// (which is overlaid on top of drawing SurfaceView -- see layout xml)
            	EView.setTextView((TextView) findViewById(R.id.text));

            	// we were just launched: set up a new game
            	Ethread.setState(EngineThread.STATE_READY);
            //	Ethread.setState(EngineThread.STATE_LOSE);
            }
            	
            	
            
        });

        
    }
    
    public class LevelSelectListener implements  OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> v, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			
			level = position;
			
			//String level_chosen = v..toString();
			//if (level != 0)
				Toast.makeText(getApplicationContext(), "Level " + (level+1) +" selected", Toast.LENGTH_SHORT).show();
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    	
    
    protected void onPause()  {
    	 super.onPause();
    	 finish();
    	}
}