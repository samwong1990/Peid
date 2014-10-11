package com.backagen.peid.testFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.backagen.peid.BillDetailsFragment;
import com.backagen.peid.ConfirmationFragment;
import com.backagen.peid.ImmutableContactDetails;
import com.backagen.peid.R;

import java.math.BigDecimal;
import java.util.List;

/**
 * This is for test purpose and is not exported.
 * You can change this in AndroidManifest.xml
 */
public class BlankActivity extends Activity implements ConfirmationFragment.OnInstructionSentListener, BillDetailsFragment.OnBillDetailsFilledInListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.blank, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInstructionSent(String nameOfOriginator, String userToGetMoney, List<ImmutableContactDetails> contacts, String amountToPayPerPerson) {

    }

    @Override
    public void onBillDetailsUpdated(BigDecimal billAmount, List<ImmutableContactDetails> party) {

    }

    @Override
    public void onBillDetailsFilledIn(BigDecimal billAmount, List<ImmutableContactDetails> party) {

    }
}
