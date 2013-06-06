package com.kirit.android.mintercept;

import com.jwetherell.heart_rate_monitor.HeartRateMonitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

@SuppressLint("NewApi")
public class frags extends Activity implements HeartRateMonitor.HeartBeatListener {

	@SuppressLint("NewApi")
	@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	    //    HeartRateMonitor hrm = (HeartRateMonitor) getFragmentManager().findFragmentById(R.id.hr);  
	    }

	@Override
	public void heartBeat(Integer hb) {
		// TODO Auto-generated method stub
		MIntercept instanceFragment = (MIntercept)getFragmentManager().findFragmentById(R.id.mintercept);
	//	instanceFragment.level.getGame().setLevel((hb%10));
		if(hb >= 75)
		{
			instanceFragment.level.getGame().setHrlevel((hb));
		}
		else
		{
			instanceFragment.level.getGame().setHrlevel((3));
		}
	}
}
