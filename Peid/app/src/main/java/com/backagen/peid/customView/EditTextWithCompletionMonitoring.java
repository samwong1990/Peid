package com.backagen.peid.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
* Created by samwong on 14/07/2014.
*/
public class EditTextWithCompletionMonitoring extends EditText {
    private List<FinishedEditingListener> mFinishedEditingListeners = new ArrayList<FinishedEditingListener>();

    /**
     * too many ways to trigger this, I cannot ensure this callback is only called once.
     * I believe excessive calling is more useful than missing an event
     */
    public interface FinishedEditingListener{
        void idempotentFinishedEditingHandler(EditText view);
    }

    public EditTextWithCompletionMonitoring(Context context) {
        super(context);
        setupListeners();
    }

    public EditTextWithCompletionMonitoring(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupListeners();
    }

    public EditTextWithCompletionMonitoring(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupListeners();
    }

    private void setupListeners() {
        setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                onFinishedEditing();
                return false;
            }
        });
        setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                onFinishedEditing();
            }
        });
    }

    private void onFinishedEditing() {
        for(FinishedEditingListener listener : mFinishedEditingListeners){
            listener.idempotentFinishedEditingHandler(this);
        }
    }

    public EditTextWithCompletionMonitoring addFinishedEditingListener(FinishedEditingListener listener){
        mFinishedEditingListeners.add(listener);
        return this;
    }

    public EditTextWithCompletionMonitoring removeFinishedEditingListener(FinishedEditingListener listenerToRemove){
        mFinishedEditingListeners.remove(listenerToRemove);
        return this;
    }


}
