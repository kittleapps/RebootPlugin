/*
 * Copyright (c) 2010 Sony Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.KittleApps.plugins.liveview.RebootPlugin;

import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;
import com.sonyericsson.extras.liveview.plugins.PluginUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootToolsException;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class RebootPluginService extends AbstractPluginService {
    
    private int MSG_TYPE_TIMER = 1;
    private int MSG_TYPE_ROTATOR = 2;
    
    // Our handler.
    private Handler mHandler = null;
    
    // Rotating
    private int mRotationDegrees = 2;
    private Bitmap mRotateBitmap = null;
    private int mDegrees = 0;
    
    // Workers
    private Timer mTimer = new Timer();
    private Rotator mRotator = new Rotator();
    
    // Worker state
    private int mCurrentWorker = 0;
    public int mode = 0;
    public String[] Modes = {
    		"Power Off",
    		"Reboot System",
    		"Reboot Recovery",
    		"Reboot Bootloader"
    };
    
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		// Create handler.
		if(mHandler == null) {
		    mHandler = new Handler();
		}
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopWork();
	}
	
    /**
     * Plugin is sandbox.
     */
    protected boolean isSandboxPlugin() {
        return true;
    }
	
	/**
	 * Must be implemented. Starts plugin work, if any.
	 */
	protected void startWork() {
	    if(!workerRunning()) {
	        mHandler.postDelayed(new Runnable() {
                public void run() {
                	try {
                        mLiveViewAdapter.clearDisplay(mPluginId);
                    } catch(Exception e) {
                        Log.e(PluginConstants.LOG_TAG, "Failed to clear display.");
                    }
                	PluginUtils.sendTextBitmap(mLiveViewAdapter, mPluginId, Modes[mode]+".", 128, 12);
                }
            }, 1000);
        }
	}
	public void ScreenUpdate(){
		stopWork();
		startWork();
	}
	/**
	 * Must be implemented. Stops plugin work, if any.
	 */
	protected void stopWork() {
		stopUpdates();
	}
	
	/**
	 * Must be implemented.
	 * 
	 * PluginService has done connection and registering to the LiveView Service. 
	 * 
	 * If needed, do additional actions here, e.g. 
	 * starting any worker that is needed.
	 */
	protected void onServiceConnectedExtended(ComponentName className, IBinder service) {
		
	}
	
	/**
	 * Must be implemented.
	 * 
	 * PluginService has done disconnection from LiveView and service has been stopped. 
	 * 
	 * Do any additional actions here.
	 */
	protected void onServiceDisconnectedExtended(ComponentName className) {
		
	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has checked if plugin has been enabled/disabled.
	 * 
	 * The shared preferences has been changed. Take actions needed. 
	 */	
	protected void onSharedPreferenceChangedExtended(SharedPreferences prefs, String key) {
		
	}

	protected void startPlugin() {
		Log.d(PluginConstants.LOG_TAG, "startPlugin");
		startWork();
	}
			
	protected void stopPlugin() {
		Log.d(PluginConstants.LOG_TAG, "stopPlugin");
		stopWork();
	}
	
	protected void button(String buttonType, boolean doublepress, boolean longpress) {
	    Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType + ", doublepress " + doublepress + ", longpress " + longpress);
		
		if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_UP)) {
			mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_DOWN)) {
            mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_RIGHT)) {
			if (mode == 0) {
				mode = 1;
				ScreenUpdate();
			}

			else if (mode == 1){
				mode = 2;
				ScreenUpdate();
			}
			else if (mode == 2){
				mode = 3;
				ScreenUpdate();
			}
			else if (mode == 3){
				mode = 0;
				ScreenUpdate();
			}
			mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_LEFT)) {
			if (mode == 0){
				mode = 3;
				ScreenUpdate();
			}
			else if (mode == 1){
				mode = 0;
				ScreenUpdate();
			}
			else if (mode == 2) {
				mode = 1;
				ScreenUpdate();
			}
			else if (mode == 3) {
				mode = 2;
				ScreenUpdate();
			}
			mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
		} else if(buttonType.equalsIgnoreCase(PluginConstants.BUTTON_SELECT)) {
			mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
		    toggleAction(Modes[mode]);
		    mLiveViewAdapter.vibrateControl(mPluginId, 50, 50);
		}
	}
	public void toggleAction(String Value){
		if (Value.equalsIgnoreCase("Reboot System")){
			try {
				RebootSystem();
			} catch (RootToolsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (Value.equalsIgnoreCase("Reboot Recovery")){
			try {
				RebootRecovery();
			} catch (RootToolsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (Value.equalsIgnoreCase("Reboot Bootloader")){
			try {
				RebootBootloader();
			} catch (RootToolsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (Value.equalsIgnoreCase("Power Off")){
			try {
				PowerOff();
			} catch (RootToolsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@SuppressWarnings("deprecation")
	protected static void RebootSystem() throws RootToolsException{
		if (RootTools.isAccessGiven()) {
    		try {
				String[] commands = new String[] {
		        		"id",
	                    "reboot" };
				RootTools.sendShell(commands, 10000, 1);
    		} catch (IOException e) {
    		    // something went wrong, deal with it here
    		} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	@SuppressWarnings("deprecation")
	protected static void RebootBootloader() throws RootToolsException{
		if (RootTools.isAccessGiven()) {
    		try {
				String[] commands = new String[] {
		        		"id",
	                    "reboot bootloader" };
				RootTools.sendShell(commands, 10000, 1);
    		} catch (IOException e) {
    		    // something went wrong, deal with it here
    		} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	@SuppressWarnings("deprecation")
	protected static void RebootRecovery() throws RootToolsException{
		if (RootTools.isAccessGiven()) {
    		try {
				String[] commands = new String[] {
		        		"id",
	                    "reboot recovery" };
				RootTools.sendShell(commands, 10000, 1);
    		} catch (IOException e) {
    		    // something went wrong, deal with it here
    		} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	@SuppressWarnings("deprecation")
	protected static void PowerOff() throws RootToolsException{
		if (RootTools.isAccessGiven()) {
    		try {
    		    String[] commands = new String[] {
		        		"id",
	                    "poweroff" };
				RootTools.sendShell(commands, 10000, 1);
    		} catch (IOException e) {
    		    // something went wrong, deal with it here
    		} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

    }
	protected static void SU() throws RootToolsException, TimeoutException{
		if (RootTools.isAccessGiven()) {
    		}
    }
	protected void displayCaps(int displayWidthPx, int displayHeigthPx) {
        Log.d(PluginConstants.LOG_TAG, "displayCaps - width " + displayWidthPx + ", height " + displayHeigthPx);
    }

	protected void onUnregistered() throws RemoteException {
		Log.d(PluginConstants.LOG_TAG, "onUnregistered");
		stopWork();
	}

	protected void openInPhone(String openInPhoneAction) {
		Log.d(PluginConstants.LOG_TAG, "openInPhone: " + openInPhoneAction);
	}
	
    protected void screenMode(int mode) {
        Log.d(PluginConstants.LOG_TAG, "screenMode: screen is now " + ((mode == 0) ? "OFF" : "ON"));
        
        if(mode == PluginConstants.LIVE_SCREEN_MODE_ON) {
            startUpdates();
        } else {
            stopUpdates();
        }
    }
    
    private void stopUpdates() {
        saveState();
        mHandler.removeMessages(MSG_TYPE_ROTATOR);
        mHandler.removeMessages(MSG_TYPE_TIMER);
    }
    
    private void startUpdates() {
        if(mCurrentWorker == MSG_TYPE_ROTATOR) {
            rotate();
        }
        else if(mCurrentWorker == MSG_TYPE_TIMER) {
            scheduleTimer();
        }
    }
    
    /**
     * The runnable used for posting to handler
     */
    private class Timer implements Runnable {
        
        @Override
        public void run() {
            // Prepare and send
            Date currentDate = new Date(System.currentTimeMillis());
            Format timeFormatter = new SimpleDateFormat("HH:mm:ss");
            PluginUtils.sendTextBitmap(mLiveViewAdapter, mPluginId, timeFormatter.format(currentDate));
            Log.d(PluginConstants.LOG_TAG, "Image sent to LiveView.");
            
            // Schedule new timer event.
            scheduleTimer();
        }
        
    }
    
    /**
     * The runnable used for posting to handler
     */
    private class Rotator implements Runnable {
        
        @Override
        public void run() {
            try
            {
                PluginUtils.rotateAndSend(mLiveViewAdapter, mPluginId, mRotateBitmap, updateDegrees());
            } catch(Exception re) {
                Log.e(PluginConstants.LOG_TAG, "Failed to send image to LiveView.", re);
            }
            
            rotate();
        }
        
    }
    
    /**
     * Rotate.
     * 
     * @param degrees
     */
    private void rotate(int degrees) {
        stopWork();
        PluginUtils.rotateAndSend(mLiveViewAdapter, mPluginId, mRotateBitmap, degrees);
        mDegrees = degrees;
    }
    
    /**
     * Schedules a rotation. 
     */
    private void rotate() {
        Message msg = Message.obtain(mHandler, mRotator);
        msg.what = MSG_TYPE_ROTATOR;
        mHandler.sendMessageDelayed(msg, 200);
        
    }
    
    /**
     * Schedules a timer. 
     */
    private void scheduleTimer() {
        Message msg = Message.obtain(mHandler, mTimer);
        msg.what = MSG_TYPE_TIMER;
        mHandler.sendMessageDelayed(msg, 1000);
        Log.d(PluginConstants.LOG_TAG, "Timer scheduled.");
    }
    
    /**
     * Start/stop timer.
     */
    private void toggleTimer() {
        if(workerRunning()) {
            stopWork();
        } else {
            stopWork();
            try {
                if(mLiveViewAdapter != null) {
                    mLiveViewAdapter.clearDisplay(mPluginId);
                }
            } catch(Exception e) {
                // NOP
            }
            scheduleTimer();
        }
    }
    
    /**
     * Start/stop rotator.
     */
    private void toggleRotate(boolean right) {
        if(right) {
            mRotationDegrees = 5;
            stopWork();
            rotate();
        } else {
            mRotationDegrees = -5;
            stopWork();
            rotate();
        }
    }
    
    /**
     * Updates current heading
     * 
     * @return
     */
    private int updateDegrees() {
        if((mDegrees > 360) || (mDegrees < -360)) {
            mDegrees = 0;
        }
        
        mDegrees += mRotationDegrees;
        
        return mDegrees;
    }
    
    private void saveState() {
        int state = 0;
        
        if(workerRunning()) {
           if(rotatorsOnQueue()) {
               state = MSG_TYPE_ROTATOR;
           }
           else {
               state = MSG_TYPE_TIMER;
           }
        }
        
        mCurrentWorker = state;
    }
    
    private boolean timersOnQueue() {
        return mHandler.hasMessages(MSG_TYPE_TIMER);
    }
    
    private boolean rotatorsOnQueue() {
        return mHandler.hasMessages(MSG_TYPE_ROTATOR);
    }
    
    private boolean workerRunning() {
        return (rotatorsOnQueue() || timersOnQueue());
    }
    
}