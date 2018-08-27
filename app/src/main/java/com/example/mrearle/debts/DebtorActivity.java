package com.example.mrearle.debts;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.mrearle.debts.Database.AppDatabase;
import com.example.mrearle.debts.Database.Debt;
import com.example.mrearle.debts.Database.Debtor;
import com.example.mrearle.debts.Database.DebtorLedger;

import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class DebtorActivity extends AppCompatActivity implements DebtorAdapter.ItemClickListener{

    // Constant for logging
    private static final String TAG = DebtorActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private DebtorAdapter mAdapter;

    private Integer mDebtorId;
    private String mShareString;

    private AppDatabase mDb;

    public static final String EXTRA_DEBTOR_ID = "extra_debtor_id";
    public static final String EXTRA_DEBTOR_NAME = "extra_debtor_name";
    private static final int POSITIVE_COLOR = Color.rgb(0,94,0);
    private static final int NEGATIVE_COLOR = Color.RED;

    private TextView mDebtorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debtor);

        initViews();

        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.rv_debtor);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new DebtorAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                List<Debt> debts = mAdapter.getDebts();
                Context context = DebtorActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final Debt debt = debts.get(position);
                builder.setTitle("Do you really want to delete this debt?");
                // Here is where you'll implement swipe to delete

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.getDao().deleteDebt(debt);
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.notifyItemChanged(position);
                        dialog.cancel();
                    }
                });

                builder.show();
                // Here is where you'll implement swipe to delete

            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(DebtorActivity.this, AddDebtActivity.class);
                addTaskIntent.putExtra(AddDebtActivity.EXTRA_DEBTOR_ID, mDebtorId);
                startActivity(addTaskIntent);
            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_DEBTOR_ID)) {
            mDebtorId = intent.getIntExtra(EXTRA_DEBTOR_ID, -1);
            String name = intent.getStringExtra(EXTRA_DEBTOR_NAME);
            Log.d(TAG, name);
            getSupportActionBar().setTitle("Debts: " + name);

            final LiveData<DebtorLedger> debtor = mDb.getDao().getDebtorById(mDebtorId);
            // COMPLETED (4) Observe tasks and move the logic from runOnUiThread to onChanged
            debtor.observe(this, new Observer<DebtorLedger>() {
                @Override
                public void onChanged(@Nullable DebtorLedger debtorLedger) {
                    // COMPLETED (5) Remove the observer as we do not need it any more
//                debtor.removeObserver(this);
                    Log.d(TAG, "Receiving database update from LiveData");
                    generateDebtorString(debtorLedger);
                    populateUI(debtorLedger);
                    mAdapter.setDebtor(debtorLedger);
                }
            });
        }
//        generateDebtorString();
        retrieveDebts();

        Snackbar.make(findViewById(R.id.coordinator_debtor), "Tap to edit, swipe to delete", Snackbar.LENGTH_LONG).show();
    }

    public void initViews() {
        mDebtorView = findViewById(R.id.tv_debtor_total);
    }

    public void populateUI(DebtorLedger debtorLedger) {
        int total = debtorLedger.getTotal();
        mDebtorView.setText(String.format(Locale.US, "Total: $ %d", total));
        if (total >= 0){
            mDebtorView.setTextColor(POSITIVE_COLOR);
        } else mDebtorView.setTextColor(NEGATIVE_COLOR);
    }

    private void retrieveDebts() {
        Log.d(TAG, "Actively retrieving the debts from the DataBase");
        LiveData<List<Debt>> debts = mDb.getDao().loadDebtsFromId(mDebtorId);
        debts.observe(this, new Observer<List<Debt>>() {
            @Override
            public void onChanged(@Nullable List<Debt> debtEntries) {
                Log.d(TAG, "Receiving database update from LiveData");
                mAdapter.setDebts(debtEntries);
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId, String name) {
        Log.d(TAG, "Starting new debt activity (DebtorId: " + mDebtorId.toString());
        Intent intent = new Intent(DebtorActivity.this, AddDebtActivity.class);
        intent.putExtra(AddDebtActivity.EXTRA_DEBTOR_ID, mDebtorId);
        intent.putExtra(AddDebtActivity.EXTRA_DEBT_ID, itemId);
        intent.putExtra(DebtorActivity.EXTRA_DEBTOR_NAME, name);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_debtor, menu);
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//        menuItem.setIntent(createShareDeudaIntent());
        return true;
    }

    private Intent createShareDeudaIntent(){
        return ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(getShareString())
                .getIntent();

    }

    private void generateDebtorString(DebtorLedger debtorLedger) {
        if (debtorLedger == null){
            return;
        }
        Log.d(TAG, "Generating Share String");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(Locale.US, "Nombre: %s\nTotal: $%d\n\n Desglose:\n",
                debtorLedger.getName(), debtorLedger.getTotal()));
        for(Debt debt: debtorLedger.debts){
            stringBuilder.append(String.format(Locale.US, "(%s) $%d -> %s\n", debt.date,
                    debt.amount, debt.description));
        }
        mShareString = stringBuilder.toString();
    }

    private void generateDebtorString() {
        LiveData<DebtorLedger> debtor = mDb.getDao().getDebtorById(mDebtorId);
        debtor.observe(this, new Observer<DebtorLedger>() {
            @Override
            public void onChanged(@Nullable DebtorLedger debtEntry) {
                Log.d(TAG, "Receiving database update from LiveData");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(String.format(Locale.US, "Nombre: %s\nTotal: $%d\n\n Desglose:\n",
                                        debtEntry.getName(), debtEntry.getTotal()));
                for(Debt debt: debtEntry.debts){
                    stringBuilder.append(String.format(Locale.US, "(%s) $%d -> %s\n", debt.date,
                            debt.amount, debt.description));
                }
                mShareString = stringBuilder.toString();
            }
        });
    }

    private String getShareString() {
        String ret = mShareString;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("enable_bank_switch", false)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n\n");
            stringBuilder.append("Nombre: " + sharedPreferences.getString("name", "") + '\n');
            stringBuilder.append("Banco: " + sharedPreferences.getString("bank", "") + '\n');
            stringBuilder.append("Rut: " + sharedPreferences.getString("rut", "") + '\n');
            stringBuilder.append("Numero de Cuenta: " + sharedPreferences.getString("account_number", "") + '\n');
            stringBuilder.append("Tipo de Cuenta: " + sharedPreferences.getString("account_type", "") + '\n');
            stringBuilder.append("Email: " + sharedPreferences.getString("email", "") + '\n');
            ret += stringBuilder.toString();
        }
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_share){
            startActivity(createShareDeudaIntent());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
