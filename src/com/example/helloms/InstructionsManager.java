package com.example.helloms;

import android.util.Log;

public class InstructionsManager {

	OR_Step[] or_steps;
	int onWhichOrIndex = 0;
	boolean done = false;
	String lastStep = "";
	InstructionsManager(OR_Step[] inOR_Steps) {
		or_steps = inOR_Steps;
	}
	
	
	public String firstStep() {
		return getCurrentStep().getNextStep(false);
	}
	
	public String nextStep(boolean responseToPrevious) {
		if (done == true) {
			return lastStep;
		}
		Log.i("DEMO", "orIndex " + onWhichOrIndex);
		String nextStep = getCurrentStep().getNextStep(responseToPrevious);
		if (nextStep.contains("GOTO_STEP:") ) {
			int nextStepNum = Integer.parseInt(nextStep.replace("GOTO_STEP:", "").trim());
			Log.i("BETH", "next step num " + nextStepNum);
			onWhichOrIndex = (nextStepNum -1);
			nextStep = getCurrentStep().getFirstStep();
			return nextStep;
		}
		else  {
			if (onWhichOrIndex > 0) {
				done = true;
				lastStep = nextStep;
			}
			return lastStep;
		}
	}
	
	public OR_Step getCurrentStep() {
		return or_steps[onWhichOrIndex];
	}
	
	public void reset() {
		onWhichOrIndex = 0;
		for (OR_Step i : or_steps) {
			i.reset();
		}
	}
	
}
