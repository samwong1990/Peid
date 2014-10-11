package com.backagen.peid;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.*;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfirmationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfirmationFragment extends Fragment {

    public interface OnInstructionSentListener{

        void onInstructionSent(
                String nameOfOriginator, String userToGetMoney,
                List<ImmutableContactDetails> contacts,
                String amountToPayPerPerson);
    }
    private static final String BILL_AMOUNT = "ConfirmationFragment.BILL_AMOUNT";

    private static final String PARTY = "ConfirmationFragment.PARTY";
    private static final String USER_NAME = "ConfirmationFragment.USER_NAME";
    private static final String USER_EMAIL = "ConfirmationFragment.USER_EMAIL";
    public static final double COST_OF_TIME_FACTOR = 0.02;
    private static final String TAG = ConfirmationFragment.class.getSimpleName();

    private OnInstructionSentListener mListener;
    private TextView mBillAmountTextView;
    private TextView mCostOfTimeTextView;
    private TextView mSharePerPersonTextView;
    private TextView mPartySizeTextView;
    private TextView mRawShareTextView;
    private LinearLayout mPeopleToBillLinearLayout;
    private Button mFinishButton;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConfirmationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfirmationFragment newInstance(String userName, String userEmail, BigDecimal billAmount, ArrayList<ImmutableContactDetails> party) {
        checkNotNull(party, "party cannot be null");
        checkArgument(billAmount.compareTo(BigDecimal.ZERO) == 1, "billAmount should be greater than zero");

        Bundle args = new Bundle();
        args.putSerializable(BILL_AMOUNT, billAmount);
        args.putSerializable(PARTY, party);
        args.putString(USER_NAME, userName);
        args.putString(USER_EMAIL, userEmail);

        final ConfirmationFragment confirmationFragment = new ConfirmationFragment();
        confirmationFragment.setArguments(args);

        return confirmationFragment;
    }

    public ConfirmationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnInstructionSentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnInstructionSentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_confirmation, container, false);
        mBillAmountTextView = (TextView) root.findViewById(R.id.confirm_bill_amount_textview);
        mCostOfTimeTextView = (TextView) root.findViewById(R.id.confirm_cost_of_time_per_person_textview);
        mSharePerPersonTextView = (TextView) root.findViewById(R.id.confirm_share_per_person_textview);
        mPartySizeTextView = (TextView) root.findViewById(R.id.confirm_party_size_textview);
        mRawShareTextView = (TextView) root.findViewById(R.id.confirm_raw_share_textview);
        mPeopleToBillLinearLayout = (LinearLayout) root.findViewById(R.id.people_who_will_receive_bill_listview);
        mFinishButton = (Button) root.findViewById(R.id.finish_btn);

        // populate views
        final ImmutableList<ImmutableContactDetails> party =
                ImmutableList.copyOf(
                        (ArrayList<ImmutableContactDetails>) getArguments().getSerializable(PARTY)
                );



        final BigDecimal serializedBillAmount = (BigDecimal) getArguments().getSerializable(BILL_AMOUNT);
        updateViews(party, serializedBillAmount);
        return root;
    }

    public void updateViews(final ImmutableList<ImmutableContactDetails> party, BigDecimal serializedBillAmount) {
        List<String> contactDetailsStrings = Lists.transform(party, new Function<ImmutableContactDetails, String>() {
            @Nullable
            @Override
            public String apply(@Nullable ImmutableContactDetails input) {
                return Objects.firstNonNull(input.getEmail(), input.getPhoneNumber());
            }
        });

        mPeopleToBillLinearLayout.removeAllViews();
        for(String contact : contactDetailsStrings){
            TextView view = (TextView) getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
            view.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
            view.setText(contact);
            mPeopleToBillLinearLayout.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        final BigDecimal billAmount = serializedBillAmount.setScale(2, BigDecimal.ROUND_UP);
        final BigDecimal partySize = new BigDecimal(party.size() + 1);
        final BigDecimal rawShare = billAmount.divide(partySize, BigDecimal.ROUND_UP).setScale(2, BigDecimal.ROUND_UP);
        final BigDecimal costOfTime = rawShare .multiply(new BigDecimal(COST_OF_TIME_FACTOR)).setScale(2, BigDecimal.ROUND_UP);
        final BigDecimal sharePerPerson = rawShare.add(costOfTime);


        mBillAmountTextView.setText(billAmount.toString());
        mPartySizeTextView.setText(partySize.toString());
        mRawShareTextView.setText(rawShare.toString());
        mCostOfTimeTextView.setText(costOfTime.toString());
        mSharePerPersonTextView.setText(sharePerPerson.toString());

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Please wait...", Toast.LENGTH_SHORT).show();
                mFinishButton.setEnabled(false);
                try {
                    askParseToSendEmails       (getArguments().getString(USER_NAME), getArguments().getString(USER_EMAIL), party, sharePerPerson.toString());
                    mListener.onInstructionSent(getArguments().getString(USER_NAME), getArguments().getString(USER_EMAIL), party, sharePerPerson.toString());
                } catch (ParseException e) {
                    Log.e(TAG, String.valueOf(e));
                    Toast.makeText(getActivity(), "Oops, encountered an error. Are you connected to the internet?", Toast.LENGTH_LONG).show();
                    mFinishButton.setEnabled(true);
                }
            }
        });
    }

    private void askParseToSendEmails(String nameOfOriginator, String originatorEmail, List<ImmutableContactDetails> contacts, String amountToPayPerPerson) throws ParseException {
        Iterable<ImmutableContactDetails> withEmail = Iterables.filter(contacts, new Predicate<ImmutableContactDetails>() {
            @Override
            public boolean apply(@Nullable ImmutableContactDetails input) {
                return input.getEmail() != null;
            }
        });
        final ArrayList<String> emailsToSendBillTo = Lists.newArrayList(Iterables.transform(withEmail, new Function<ImmutableContactDetails, String>() {
            @Nullable
            @Override
            public String apply(@Nullable ImmutableContactDetails input) {
                return input.getEmail();
            }
        }));
        Map<String, Object> params = Maps.newHashMap();
        params.put("originatorEmail", originatorEmail);
        params.put("nameOfOriginator", nameOfOriginator);
        params.put("emailsToSendBillTo", emailsToSendBillTo);
        params.put("amountToPayPerPerson", amountToPayPerPerson);

        ParseCloud.callFunction("sendBills", params);
    }
}
