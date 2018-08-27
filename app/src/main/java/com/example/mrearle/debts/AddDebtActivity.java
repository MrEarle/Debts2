package com.example.mrearle.debts;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.mrearle.debts.Database.AppDatabase;
import com.example.mrearle.debts.Database.Debt;
import com.example.mrearle.debts.Database.DebtorLedger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddDebtActivity extends AppCompatActivity {

    // Extra for the task ID to be received in the intent
    public static final String EXTRA_DEBTOR_ID = "extraDebtorId";
    public static final String EXTRA_DEBT_ID = "extraDebtId";
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_DEBT_ID = "instanceDebtId";
    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_DEBT_ID = -1;
    // Constant for logging
    private static final String TAG = AddDebtActivity.class.getSimpleName();

    private Integer mDebtorId;
    private int mDebtId = DEFAULT_DEBT_ID;
    // Fields for views
    EditText mAmount;
    EditText mDescription;
    DatePicker mDate;
    Button mButton;


    // Member variable for the Database
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_DEBT_ID)) {
            mDebtId = savedInstanceState.getInt(INSTANCE_DEBT_ID, DEFAULT_DEBT_ID);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mDebtorId = intent.getIntExtra(EXTRA_DEBTOR_ID, -1);
        if (intent.hasExtra(EXTRA_DEBT_ID)) {
            mButton.setText(R.string.update_button);
            if (mDebtId == DEFAULT_DEBT_ID) {
                // populate the UI
                mDebtId = intent.getIntExtra(EXTRA_DEBT_ID, DEFAULT_DEBT_ID);

                Log.d(TAG, "Actively retrieving a specific debt from the DataBase");
                // COMPLETED (3) Extract all this logic outside the Executor and remove the Executor
                // COMPLETED (2) Fix compile issue by wrapping the return type with LiveData
                final LiveData<Debt> debt = mDb.getDao().getDebtById(mDebtId);
                // COMPLETED (4) Observe tasks and move the logic from runOnUiThread to onChanged
                debt.observe(this, new Observer<Debt>() {
                    @Override
                    public void onChanged(@Nullable Debt debtEntry) {
                        // COMPLETED (5) Remove the observer as we do not need it any more
                        debt.removeObserver(this);
                        Log.d(TAG, "Receiving database update from LiveData");
                        populateUI(debtEntry);
                    }
                });
            }
        }

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_DEBT_ID, mDebtorId);
        super.onSaveInstanceState(outState);
    }

    private void populateUI(Debt debt) {
        if (debt == null) {
            return;
        }
        mAmount.setText(debt.amount.toString());
        mDescription.setText(debt.description);
        String[] parts = debt.date.split("/");
        mDate.init(Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]), null);
    }

    private void initViews() {
        mAmount = findViewById(R.id.editTextAmount);
        mDescription = findViewById(R.id.editTextDescription);
        mDate = findViewById(R.id.datePicker);

        mButton = findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onSaveButtonClicked() {
        Integer amount = Integer.parseInt(mAmount.getText().toString());
        String description = mDescription.getText().toString();
        String date = String.format(Locale.US, "%d/%02d/%02d",
                mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth());

        Log.d(TAG, mDebtorId.toString());

        final Debt debt = new Debt(amount, date, description, mDebtorId);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mDebtId == DEFAULT_DEBT_ID) {
                    // insert new task
                    mDb.getDao().insertDebt(debt);
                } else {
                    //update task
                    debt.setId(mDebtId);
                    mDb.getDao().updateDebt(debt);
                }
                finish();
            }
        });
    }

}
