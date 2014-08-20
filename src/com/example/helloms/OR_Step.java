package com.example.helloms;

import android.util.Log;

public class OR_Step {
	 String[] orSteps;
	 String doIfFalse;
	 String doIfTrue;
	 
	 int currentIndexIntoStep = -1;
	 OR_Step(String[] in_orSteps, String in_doIfTrue, String  in_doIfFalse) {
		orSteps = in_orSteps;
		doIfTrue = in_doIfTrue;
		doIfFalse = in_doIfFalse;
	 }
	 
	public String getNextStep(boolean responseToPrevious) {
		currentIndexIntoStep+=1;
		Log.i("BETH", "or steps" + orSteps.length);
		Log.i("BETH", "currentIndexIntoStep: " + currentIndexIntoStep);
		if (responseToPrevious == true) {
			Log.i("BETH", "doIfTrue");
			return this.doIfTrue;
		
		}
		else if (currentIndexIntoStep >= orSteps.length) {
			Log.i("BETH", "doIfFalse");
			return this.doIfFalse;
		}
		else {
			Log.i("BETH", "orSteps");
			return this.orSteps[currentIndexIntoStep];
		}
	}
	
	public String getFirstStep() {
		currentIndexIntoStep = 0;
		return this.orSteps[0];
	}

	public void reset() {
		currentIndexIntoStep=-1;
	}
	 
}
