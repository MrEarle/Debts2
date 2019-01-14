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
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mrearle.debts.Database.AppDatabase;
import com.example.mrearle.debts.Database.Debtor;
import com.example.mrearle.debts.Database.DebtorLedger;

public class AddDebtorActivity extends AppCompatActivity {

    // Extra for the task ID to be received in the intent
    public static final String EXTRA_DEBTOR_ID = "extraDebtorId";
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_DEBTOR_ID = "instanceDebtorId";
    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_DEBTOR_ID = -1;
    // Constant for logging
    private static final String TAG = AddDebtorActivity.class.getSimpleName();
    // Fields for views
    EditText mEditText;
    Button mButton;


    private int mDebtorId = DEFAULT_DEBTOR_ID;

    // Member variable for the Database
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debtor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Debts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_DEBTOR_ID)) {
            mDebtorId = savedInstanceState.getInt(INSTANCE_DEBTOR_ID, DEFAULT_DEBTOR_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_DEBTOR_ID)) {
            mButton.setText(R.string.update_button);
            if (mDebtorId == DEFAULT_DEBTOR_ID) {
                // populate the UI
                mDebtorId = intent.getIntExtra(EXTRA_DEBTOR_ID, DEFAULT_DEBTOR_ID);

                Log.d(TAG, "Actively retrieving a specific debtor from the DataBase");
                // COMPLETED (3) Extract all this logic outside the Executor and remove the Executor
                // COMPLETED (2) Fix compile issue by wrapping the return type with LiveData
                final LiveData<DebtorLedger> debtor = mDb.getDao().getDebtorById(mDebtorId);
                // COMPLETED (4) Observe tasks and move the logic from runOnUiThread to onChanged
                debtor.observe(this, new Observer<DebtorLedger>() {
                    @Override
                    public void onChanged(@Nullable DebtorLedger debtorLedger) {
                        // COMPLETED (5) Remove the observer as we do not need it any more
                        debtor.removeObserver(this);
                        Log.d(TAG, "Receiving database update from LiveData");
                        populateUI(debtorLedger);
                    }
                });
            }
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_DEBTOR_ID, mDebtorId);
        super.onSaveInstanceState(outState);
    }

    /**
     * initViews is called from onCreate to init the member variable views
     */
    private void initViews() {
        mEditText = findViewById(R.id.editTextName);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSaveButtonClicked();
                }
                return false;
            }
        });

        mEditText.requestFocus();

        mButton = findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }

    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param debtorLedger the taskEntry to populate the UI
     */
    private void populateUI(DebtorLedger debtorLedger) {
        if (debtorLedger == null) {
            return;
        }

        mEditText.setText(debtorLedger.getName());
    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onSaveButtonClicked() {
        String name = mEditText.getText().toString();

        final Debtor debtor = new Debtor(name);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mDebtorId == DEFAULT_DEBTOR_ID) {
                    // insert new task
                    mDb.getDao().insertDebtor(debtor);
                } else {
                    //update task
                    debtor.setId(mDebtorId);
                    mDb.getDao().updateDebtor(debtor);
                }
                finish();
            }
        });
    }
}
