package com.ajani;










import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class EngineView extends  SurfaceView implements SurfaceHolder.Callback {

	
	public class EngineThread extends Thread{ //thread for handling engine
		
		
		private SurfaceHolder engineSurfaceHolder;
		private Handler engineHandler;
		private Context engineContext;
	
	

		/**images used */
		private Drawable AImage;
		private Bitmap Background;
		
	//	private Drawable virus_img[];
		private Drawable virus_img;
		private PaintDrawable antibody = new PaintDrawable(Color.CYAN);
		
		private ArrayList<Rect> pathogens;
		private ArrayList<Rect> shots;
		private int virus_count;
		private int shot_count;
		private int killcount;
		/** Pixel width of avatar image. */
		private int A_width;
		private int A_height;
		
		private int V_width;
		private int V_height;
		
		private int level ;
		private int scale_virus;
		private int scale_virus_speed;
		
		
		
		Paint mTextPaint;
		Paint Alert;
		
		public   double cell_position ; //cell can only move on one axis, x-axis.
		
		public static final int STATE_LOSE = 1;
		public static final int STATE_PAUSE = 2;
		public static final int STATE_READY = 3;
		public static final int STATE_RUNNING = 4;
		public static final int STATE_WIN = 5;
		
		public static final int cell_maxspeed = 50;
		public static final int cell_factor = 10;
		public static final int pathogen_speed = 3;
		
		public static final int shot_width = 20;
		public static final int shot_height = 70;
		public static final int shot_speed = 2;
		public static final int shot_max = 3;	
		
		
		public static final float max_x =380;
		public static final float max_y = 800;
		public static final float min_x = 0;
		public static final float min_y =0 ;
		
		public static final int spawnrate = 50; 
		
		public static final float max_virus_x = 350;
		public static final float min_virus_x = 0;
		public static final float max_virus_y = 750;
		public static final float min_virus_y =0;
		
		public static final int virus_limit = 3;
		public int total_v;
		public static final float ATTACK_RANGE = 30;
		private int gameMode;
		private boolean canRun= false;
		private boolean avatar_hit =false;
		
		public static final float start_x = 50;
		public static final float start_y =675;
		
		
		SensorManager SM;
		double accelX, accelY, accelZ;
		double accelMagnitude;
		boolean haveAccel = false;		
		
		
		
		class SensorListener implements SensorEventListener {
			
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					float[] values = event.values;
					// Movement
					
					accelX = values[0];
					accelY = values[1];
					accelZ = values[2];
					accelMagnitude = Math.sqrt(accelX*accelX + accelY*accelY + accelZ*accelZ);
					haveAccel = true;
		
				}

			}

			@Override
			public void onAccuracyChanged(Sensor arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

		}
		public EngineThread(SurfaceHolder surfaceHolder, Context context, Handler handler ) {
			
			
			engineSurfaceHolder = surfaceHolder;
			engineHandler = handler;
			engineContext= context;	
			
			Resources res = context.getResources();
			// cache handles to our key sprites & other drawables
			AImage = context.getResources().getDrawable(R.drawable.avatar);
			virus_img = context.getResources().getDrawable(R.drawable.v4);			
			Background = BitmapFactory.decodeResource(res,R.drawable.chocolateland);
		
			cell_position = start_x;
			
			pathogens = new ArrayList<Rect>();
			shots = new ArrayList<Rect>();
			
			
			
			virus_count=pathogens.size();
			shot_count = shots.size();
			killcount = 0;
			total_v =0;
			
			scale_virus= 0;
			scale_virus_speed=0;
			
			//drawing and hit detection variables
			A_width = AImage.getIntrinsicWidth();
			A_height =AImage.getIntrinsicHeight();
			
			V_height = virus_img.getIntrinsicHeight();
			V_width = virus_img.getIntrinsicWidth();
			
			mTextPaint = new Paint();
			mTextPaint.setAntiAlias(true);
			mTextPaint.setARGB(255, 255, 0, 0);
			mTextPaint.setTextSize(25);
			
			Alert = new Paint();
			Alert.setAntiAlias(true);
			Alert.setARGB(255, 255, 0, 0);
			Alert.setTextAlign(Paint.Align.CENTER);
			Alert.setTextSize(48);
			
			SM = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
			SM.registerListener(new SensorListener(),
					SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SM.SENSOR_DELAY_GAME);		
			
		}

		
		//override 
		public void run() {		
			//if(canRun){
				while(canRun){
				Canvas c = null;
				try {
					c = engineSurfaceHolder.lockCanvas(null);
					synchronized (engineSurfaceHolder) {
						if (gameMode == STATE_RUNNING)						
						  	update(); 
						
						doDraw(c);	
						
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						engineSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
		
		public void pause(){
			synchronized (engineSurfaceHolder){
				if(gameMode ==STATE_RUNNING){
					setState(STATE_PAUSE);
				}
			}
			
		}
		
		public void doDraw (Canvas canvas){
			
			int left = (int) (cell_position );
			int top = (int) (start_y - A_height /2);
			
			
			canvas.drawBitmap(Background, 0, 0, null);
			AImage.setBounds(left, top,left+A_width, top+ A_height);
			boolean opaque =virus_img.isVisible();
			AImage.draw(canvas);
		
			//String s = String.format("(%+5.3f, %+5.3f, %+5.3f)", accelX, accelY, accelZ);
		//	canvas.drawText(s, 0, 400, mTextPaint);
		//	canvas.drawText(""+pathogens.size(), 0, 200, mTextPaint);
			//canvas.drawText(""+avatar_hit, 0, 200, mTextPaint);
			canvas.drawText("LEVEL: "+(level+1), 0, 50, mTextPaint);
			canvas.drawText("KILLS: "+(killcount), 385, 50, mTextPaint);
			
			for(int i = 0; i<pathogens.size(); i++){
				//virus_img.setAlpha(255);
				virus_img.setBounds(pathogens.get(i));
				//Rect r = pathogens.get(i);
			//	canvas.drawText(""+r, 0, 200, mTextPaint);
				virus_img.draw(canvas);
				
			}
			for(int i = 0; i<shots.size(); i++){
				//virus_img.setAlpha(255);
				antibody.setBounds(shots.get(i));
				//Rect r = pathogens.get(i);
			//	canvas.drawText(""+r, 0, 200, mTextPaint);
				antibody.draw(canvas);
				
			}
			
			
		}		
		
		void doTouchDown(float x, float y, int count) {
			synchronized (engineSurfaceHolder) {

				// here's where a touch starts the game...
				if(gameMode == STATE_RUNNING)
					antibody();
				
				if (gameMode != STATE_RUNNING)
					setState( STATE_RUNNING );
					
					//cell_position = x;
				
					//shoot function here, every time shot fired
			}
		}

		public void doTouchMove(float x_touch, float y_touch, float dx_touch,
				float dy_touch, int pointerCount) {
			// TODO Auto-generated method stub
			synchronized (engineSurfaceHolder) {			
				
			}
			
		}

		public void doTouchUp(float x_touch, float y_touch, int pointerCount) {
			// TODO Auto-generated method stub
			synchronized (engineSurfaceHolder) {
				
				
			}
			
		}
		
		//set thread methods
		
		public void setRunning(boolean b) {
			canRun = b;
		}
			
		
		public void setState(int mode) {
				synchronized (engineSurfaceHolder) {
				setState(mode, null);
			}
		}
		
		public void setState(int mode, CharSequence message) {
			/*
			 * This method optionally can cause a text message to be displayed
			 * to the user when the mode changes. Since the View that actually
			 * renders that text is part of the main View hierarchy and not
			 * owned by this thread, we can't touch the state of that View.
			 * Instead we use a Message + Handler to relay commands to the main
			 * thread, which updates the user-text View.
			 */
			synchronized (engineSurfaceHolder) {
				gameMode = mode;
				String kills = "\nYou killed "+killcount+ " viruses";
				String total = "\n out of "+virus_count+" total viruses";
				if (gameMode == STATE_RUNNING) {
					Message msg = engineHandler.obtainMessage();
					Bundle b = new Bundle();
					b.putString("text", "");
					b.putInt("viz", View.INVISIBLE);
					msg.setData(b);
					engineHandler.sendMessage(msg);
				} else {
					
					Resources res = engineContext.getResources();
					CharSequence str = "";
					if (gameMode == STATE_READY)
						str = res.getText(R.string.mode_ready);
					else if (gameMode == STATE_PAUSE)
						str = res.getText(R.string.mode_pause);
					else if (gameMode ==  STATE_LOSE){
							str = res.getText(R.string.mode_lose) +kills + total;
									
							
					}
					else if (gameMode == STATE_WIN)
						str = res.getString(R.string.mode_win_prefix)
						+ " "
						+ res.getString(R.string.mode_win_suffix);

					if (message != null) {
						str = message + "\n" + str;
					}

					Message msg = engineHandler.obtainMessage();
					Bundle b = new Bundle();
					b.putString("text", str.toString());
					b.putInt("viz", View.VISIBLE);
					msg.setData(b);
					engineHandler.sendMessage(msg);

				}
			}
		}  // setState

		
		//updating game elements
		void update(){
			
			growth(virus_limit+scale_virus);
			moveCell();
			infection();
			cure();
			kill();
			cellularAssault();			
			necrosis();
			levelup();
		}
		
		void necrosis(){
			
			if(avatar_hit)
				setState(STATE_LOSE);
			
		}
		
		void growth(int limit){//adds a virus to the list, if possible
			
			int spawner = (int) ( Math.random() *100) ;
			
			if(spawner == spawnrate)
				if(virus_count < limit)			
				{
					Rect new_v =new Rect(); 
					int xpos = (int)( Math.random() * max_virus_x); //(int) ( Math.random() * max_virus_x) ;
					int ypos = 0; //starts viruses at the top of the screen


					int left = (int) xpos - ( V_width /2);
					int top = (int) ypos - ( V_height /2);				

					new_v.set(left, top,left+V_width,top+V_height);			

					pathogens.add(new_v); //picks and adds a random image to the list
					total_v++;				

				}
			virus_count = pathogens.size();	
		}

		
		
		void infection(){ //updates viruses on screen.
			
			for(int i = 0;i<pathogens.size();i++){
				Rect r = pathogens.get(i);
				
				r.set(r.left, r.top+pathogen_speed+scale_virus_speed, r.right, r.bottom+pathogen_speed+scale_virus_speed);
								
				if(r.top > max_virus_y) //checks to see if the virus is off the screen
					pathogens.remove(i);
				else
					pathogens.set(i,r);				
			}
						
		}
		
		
	 void moveCell(){ //accelerometer is the only way to move
		 if(haveAccel){
			 double speed = cell_factor * accelX;
			 
			 if(speed > cell_maxspeed)
				 speed =cell_maxspeed;
			 else if(speed < -cell_maxspeed)
				 speed =  -cell_maxspeed;
			 
				 cell_position += speed;
			 
			 if(cell_position > max_x )
				 cell_position = max_x;
			 else if (cell_position < min_x)
				 cell_position = min_x;			 
		 }		 
		 
	}
	 
	 void antibody(){ //generated when a shot is fired. 
		 
		 
			
			
				if(shot_count < shot_max)			
				{
					Rect new_v =new Rect(); 
					int xpos = (int) cell_position; //(int) ( Math.random() * max_virus_x) ;
					int ypos = (int) start_y; //starts viruses at the top of the screen


					int left = (int) ( xpos - (shot_width /2));
					int top = (int) ypos - ( shot_height /2);				

					new_v.set(left, top,left+shot_width,top+shot_height);			

					shots.add(new_v); //picks and adds a random image to the list
					//total_v++;				

				}
			shot_count = shots.size();	 
	 }
		
	 void cure(){ //updates viruses on screen.
			
			for(int i = 0;i<shots.size();i++){
				Rect r = shots.get(i);
				
				r.set(r.left, r.top-shot_speed, r.right, r.bottom-shot_speed);			
				
				if(r.top < min_virus_y) //checks to see if the virus is off the screen
					shots.remove(i);
				else
					shots.set(i,r);				
			}
						
		}
	 
	 
	 
	 
	 void cellularAssault(){ //checks to see if the virus has hit the wbc
		 boolean check = false; 
		 for(int i =0; i<pathogens.size();i++)
		 {
		 
			 check = collision(AImage.getBounds(),pathogens.get(i));
			 
			 if(check){
				 avatar_hit = true;
				// pathogens.remove(i);
				 return; //once we have a hit in the list, the'res no need to check the rest.
				 		//remove that virus from the list.
			 }
			 else avatar_hit = false;
		 }		 
		 
	 }	 
	 
	 void kill(){ //hit detection for shots  & viruses
		 boolean check;
		 Rect bullet;
		 Rect virus;
		 for (int i =0; i<shots.size(); i++){
			 for(int j=0; j<pathogens.size(); j++){
				 bullet = shots.get(i);
				 virus = pathogens.get(j);
				 check = collision(bullet, virus);
				 
				 if(check){
					 pathogens.remove(j);
					killcount++; 
				 }
			 }
			 
		 }		 
	 }
	 void levelup(){//enhances difficulty
		 
		 if((killcount !=0) && (killcount % 20) ==  0){ //0 % 20 = zero, instant level up!
			 level++;
		 	scale_virus++;
		 	scale_virus_speed +=3;
		 	
		 }
			 
	 }
	 
	 

	 boolean collision(Rect r, Rect cell){ //checks to see if a virus has hit the wbcell , individually


		 if(Rect.intersects(r,cell)){
			 return true;

		 }
		 else return false;
	 }
			
	 void setLevel (int L){
		 
		 level =L;
		 
		 
	 }
	 
	 
	 
	}//end of enginethread definition
	
	private Context mContext;
	private TextView mStatusText;
	private EngineThread thread = null;

	private float x_previous_touch, y_previous_touch;
	
	public EngineView(Context context, AttributeSet attrs) { //constructor
		super(context, attrs);
		// TODO Auto-generated constructor stub
	
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		thread = new EngineThread(holder, context, new Handler() {
			@Override
			public void handleMessage(Message m) {
				mStatusText.setVisibility(m.getData().getInt("viz"));
				mStatusText.setText(m.getData().getString("text"));
			}
		});

		setFocusable(true); // make sure we get key events
	
	
		
		
		
	}
	
	public EngineThread getThread() {
		return thread;
	}
	
	
	public void setTextView(TextView textView) {
		mStatusText = textView;
	}
	
	
	public boolean onTouchEvent(MotionEvent e) {
		
		if(thread.avatar_hit){
			surfaceDestroyed(getHolder());
			//super.setVisibility(INVISIBLE);
			//super.onDetachedFromWindow();
			//super.findViewById(R.layout.main);
			//super.
			//thread.setRunning(false);
		}
		float x_touch = e.getX();
		float y_touch = e.getY();

		int action = e.getAction();

		switch (action & MotionEvent.ACTION_MASK)
		{

		case MotionEvent.ACTION_MOVE: 

			float dx_touch = x_touch - x_previous_touch;
			float dy_touch = y_touch - y_previous_touch;

			thread.doTouchMove(x_touch, y_touch, dx_touch, dy_touch, e.getPointerCount());
			break;

		case MotionEvent.ACTION_DOWN:
			//if the thread has been terminated, exit	
			if(thread.getState() == Thread.State.TERMINATED){
				
				super.findViewById(R.layout.main);
				
				
			}
			thread.doTouchDown(x_touch, y_touch, e.getPointerCount());
			break;

		case MotionEvent.ACTION_UP:
			thread.doTouchUp(x_touch, y_touch, e.getPointerCount());
			break;	
			

		}

		x_previous_touch = x_touch;
		y_previous_touch = y_touch;

		return true;
	}

	
	
	
	
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		thread.setRunning(true);
		thread.start();
	}


	
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		boolean retry = true;
		thread.setRunning(false);
		
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {  }
			
		}
	}
		
	

	
	
}
