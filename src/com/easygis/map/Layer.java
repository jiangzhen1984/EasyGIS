package com.easygis.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract class Layer extends View {

	public Layer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public Layer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Layer(Context context) {
		super(context);
	}


	
}
