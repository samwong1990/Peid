package com.backagen.peid;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.backagen.peid.customView.EditTextWithCompletionMonitoring;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BillDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class BillDetailsFragment extends Fragment implements EditTextWithCompletionMonitoring.FinishedEditingListener {

    public static final int EMPTY_FIELDS_COUNT_GUARANTEE = 1;
    private EditTextWithCompletionMonitoring mBillAmountInput;
    private Button mNextButton;
    private ViewGroup mContactDetailsContainer;
    private List<EditTextWithCompletionMonitoring> mContactDetails;
    private List<EditText> mLivePublishChangesWatchlist;
    private List<EditTextWithCompletionMonitoring> mCheckErrorOnFinishedWatchlist;
    private List<OnBillDetailsFilledInListener> mListeners = Lists.newArrayList();;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BillDetails.
     */
    public static BillDetailsFragment newInstance() {
        BillDetailsFragment fragment = new BillDetailsFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public BillDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_bill_details, container, false);
        mBillAmountInput = (EditTextWithCompletionMonitoring) rootView.findViewById(R.id.bill_amount_input);
        mNextButton = (Button) rootView.findViewById(R.id.bill_details_next_btn);
        mContactDetailsContainer = (ViewGroup) rootView.findViewById(R.id.contact_details_container);

        // Prep trackers
        mContactDetails = new ArrayList<EditTextWithCompletionMonitoring>();
        mLivePublishChangesWatchlist = new ArrayList<EditText>(){
            @Override
            public boolean add(EditText object) {
                object.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(isInputValid(false, false)){
                            BillDetailsFragment.this.callOnBillDetailsUpdated();
                        }
                    }
                });
                return super.add(object);
            }
        };
        mCheckErrorOnFinishedWatchlist = new ArrayList<EditTextWithCompletionMonitoring>(){
            @Override
            public boolean add(EditTextWithCompletionMonitoring object) {
                object.addFinishedEditingListener(BillDetailsFragment.this);
                return super.add(object);
            }
        };

        // Setup validator
        InputFilter[] filters = ObjectArrays.concat(mBillAmountInput.getFilters(),
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source,
                                               int start,
                                               int end,
                                               Spanned dest,
                                               int dstart,
                                               int dend) {
                        String result = dest.subSequence(0, dstart)
                                        + source.toString()
                                        + dest.subSequence(dend, dest.length());

                        Matcher matcher = Pattern.compile("(0|[1-9]+[0-9]*)?(\\.[0-9]{0,2})?").matcher(result);
                        if (!matcher.matches()) return dest.subSequence(dstart, dend);
                        return null;
                    }
                });
        mBillAmountInput.setFilters(filters);

        // Setup buttons
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInputValid(true, true)){
                    callOnBillDetailsFilledIn();
                }
            }
        });

        // Keep track of changes
        for(int i=0; i<mContactDetailsContainer.getChildCount(); i++){
            final EditTextWithCompletionMonitoring editText = (EditTextWithCompletionMonitoring) mContactDetailsContainer.getChildAt(i);
            mLivePublishChangesWatchlist.add(editText);
            mContactDetails.add(editText);
        }
        mLivePublishChangesWatchlist.add(mBillAmountInput);

        // Add error checking
        mCheckErrorOnFinishedWatchlist.add(mBillAmountInput);
        for(EditTextWithCompletionMonitoring editText : mContactDetails){
            mCheckErrorOnFinishedWatchlist.add(editText);
        }
        return rootView;
    }

    private void callOnBillDetailsUpdated() {
        Iterable<ImmutableContactDetails> contactDetails = getImmutableContactDetails();
        final BigDecimal billAmount = new BigDecimal(mBillAmountInput.getText().toString());
        for(OnBillDetailsFilledInListener listener : mListeners){
            listener.onBillDetailsUpdated(billAmount, Lists.newArrayList(contactDetails));
        }
    }

    private void callOnBillDetailsFilledIn(){
        Iterable<ImmutableContactDetails> contactDetails = getImmutableContactDetails();
        final BigDecimal billAmount = new BigDecimal(mBillAmountInput.getText().toString());
        for(OnBillDetailsFilledInListener listener : mListeners){
            listener.onBillDetailsFilledIn(billAmount, Lists.newArrayList(contactDetails));
        }
    }

    private Iterable<ImmutableContactDetails> getImmutableContactDetails() {
        Iterable<EditTextWithCompletionMonitoring> filledInOnes = Iterables.filter(mContactDetails, new Predicate<EditTextWithCompletionMonitoring>() {
            @Override
            public boolean apply(@Nullable EditTextWithCompletionMonitoring input) {
                return input != null && input.getText().length() > 0;
            }
        });

        return Iterables.transform(
                filledInOnes,
                new Function<EditTextWithCompletionMonitoring, ImmutableContactDetails>() {
                    @Nullable
                    @Override
                    public ImmutableContactDetails apply(@Nullable EditTextWithCompletionMonitoring input) {
                        if (input != null) {
                            String inputString = input.getText().toString();
                            if (isEmail(inputString)) {
                                return new ImmutableContactDetails(inputString, null);
                            } else if (isMobile(inputString)) {
                                return new ImmutableContactDetails(null, inputString);
                            }
                        }
                        throw new IllegalArgumentException("Attempted to work on invalid data.");
                    }
                }
        );
    }

    @Override
    public void idempotentFinishedEditingHandler(EditText view) {
        // Incremental Validate
        if(isInputValid(true, false)){
            callOnBillDetailsUpdated();
        }
        // See if there is at least EMPTY_FIELDS_COUNT_GUARANTEE empty row for another contact details, if not, make 'em
        if(getCurrentEmptyFieldsCount() <= EMPTY_FIELDS_COUNT_GUARANTEE){
            addNewContactInput();
        }
    }

    private void addNewContactInput() {
        final EditTextWithCompletionMonitoring newField = new EditTextWithCompletionMonitoring(getActivity());
        mLivePublishChangesWatchlist.add(newField);
        mCheckErrorOnFinishedWatchlist.add(newField);
        newField.setInputType(InputType.TYPE_CLASS_TEXT);
        newField.setHint(getString(R.string.contact_details_hint));
        mContactDetails.add(newField);
        mContactDetailsContainer.addView(newField);
    }

    private int getCurrentEmptyFieldsCount() {
        return Collections2.filter(mContactDetails, new Predicate<EditTextWithCompletionMonitoring>() {
            @Override
            public boolean apply(@Nullable EditTextWithCompletionMonitoring input) {
                return (input != null ? input.getText().length() : 0) == 0;
            }
        }).size();
    }

    private boolean isInputValid(boolean showNonEmptyFieldErrors, boolean showEmptyFieldErrors) {
        int emptyFieldErrors = 0;
        int nonEmptyFieldErrors = 0;

        // validate bill amount
        if(mBillAmountInput.getText().length() == 0){
            emptyFieldErrors++;
            if(showEmptyFieldErrors) mBillAmountInput.setError(getString(R.string.bill_amount_empty_error_message));
        }

        Matcher matcher = Constants.MONEY_REGEX_PATTERN.matcher(mBillAmountInput.getText());
        if (!matcher.matches()){
            nonEmptyFieldErrors++;
            if(showNonEmptyFieldErrors) mBillAmountInput.setError("Please enter a valid amount");
        }

        // validate all email/phone
        if(mContactDetails.size() == 0){
            emptyFieldErrors++;
        }else if(
                // should have at least one email
                Lists.newArrayList(Iterables.filter(mContactDetails, new Predicate<EditTextWithCompletionMonitoring>() {
                    @Override
                    public boolean apply(@Nullable EditTextWithCompletionMonitoring input) {
                        return input.getText().length() > 0;
                    }
                })).size() < 1){
            emptyFieldErrors++;
            if(showEmptyFieldErrors) mContactDetails.get(0).setError(getString(R.string.no_party_supplied_error_message));
        }
        for(EditTextWithCompletionMonitoring editText : mContactDetails){
            final String text = editText.getText().toString();
            if(TextUtils.isEmpty(text) || isEmail(text) || isMobile(text)){
                continue;
            }else{
                nonEmptyFieldErrors++;
                if(showNonEmptyFieldErrors) editText.setError("Please enter a valid email or mobile phone number");
            }
        }
        return nonEmptyFieldErrors == 0 && emptyFieldErrors == 0;
    }

    private boolean isMobile(String text) {
        final Pattern regex = Pattern.compile("07\\d{9}");
        Matcher matcher = regex.matcher(text);
        return matcher.matches();
    }

    private boolean isEmail(String text) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListeners.add((OnBillDetailsFilledInListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnBillDetailsFilledInListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListeners.clear();
    }

//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
    public interface OnBillDetailsFilledInListener {
        public void onBillDetailsUpdated(BigDecimal billAmount, List<ImmutableContactDetails> party);
        public void onBillDetailsFilledIn(BigDecimal billAmount, List<ImmutableContactDetails> party);
    }

}
