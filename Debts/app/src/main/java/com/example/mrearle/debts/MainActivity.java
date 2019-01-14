package com.example.mrearle.debts;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.mrearle.debts.Database.AppDatabase;
import com.example.mrearle.debts.Database.Debtor;
import com.example.mrearle.debts.Database.DebtorLedger;

import java.util.List;
import java.util.Locale;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity implements MainAdapter.ItemClickListener,
        MainAdapter.LongItemClickListener {

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    private MainAdapter mAdapter;
    private TextView mGrandTotalView;
//    private SearchView mSearch;

    private static final int POSITIVE_COLOR = Color.rgb(0,94,0);
    private static final int NEGATIVE_COLOR = Color.RED;

    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Debts");
        }

        // Set the RecyclerView to its corresponding view
        RecyclerView mRecyclerView = findViewById(R.id.debtors_recycle_view);
        mGrandTotalView = findViewById(R.id.tv_grandTotal);
//        mSearch = findViewById(R.id.debtor_search);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new MainAdapter(this, this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            // Todo: Add checked
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                final List<DebtorLedger> debtors = mAdapter.getDebtors();
                Context context = MainActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final Debtor debtor = debtors.get(position).debtor;
                builder.setTitle(String.format(Locale.US, "Do you really want to delete %s", debtor.name));
                // Here is where you'll implement swipe to delete

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.getDao().deleteDebtor(debtor);
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
                Intent addTaskIntent = new Intent(MainActivity.this, AddDebtorActivity.class);
                startActivity(addTaskIntent);
            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());
        retrieveDebtors();



        Snackbar.make(findViewById(R.id.coordinator_main), "Tap to edit, swipe to delete", Snackbar.LENGTH_LONG).show();
    }

    private void retrieveDebtors() {
        Log.d(TAG, "Actively retrieving the debtors from the DataBase");
        LiveData<List<DebtorLedger>> debtors = mDb.getDao().loadAllDebtors();
        debtors.observe(this, new Observer<List<DebtorLedger>>() {
            @Override
            public void onChanged(@Nullable List<DebtorLedger> debtorEntries) {
                Log.d(TAG, "Receiving database update from LiveData");
                mAdapter.setDebtors(debtorEntries);
                // Todo: Make more efficient
                int grandTotal = 0;
                for(DebtorLedger debtor: debtorEntries){
                    grandTotal += debtor.getTotal();
                }
                updateGrandTotal(grandTotal);
            }
        });
    }

    private void updateGrandTotal(int total) {
        mGrandTotalView.setText(String.format(Locale.US, "Total: $%d", total));

        if(total >= 0){
            mGrandTotalView.setTextColor(POSITIVE_COLOR);
        } else {
            mGrandTotalView.setTextColor(NEGATIVE_COLOR);
        }
    }

    @Override
    public void onItemClickListener(int itemId, String name) {
        Intent intent = new Intent(MainActivity.this, DebtorActivity.class);
        Log.d(TAG, name);
        intent.putExtra(DebtorActivity.EXTRA_DEBTOR_ID, itemId);
        intent.putExtra(DebtorActivity.EXTRA_DEBTOR_NAME, name);
        startActivity(intent);
    }

    @Override
    public void onLongItemClickListener(int itemId) {
        // Launch AddDebtor adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddDebtorActivity.class);
        intent.putExtra(AddDebtorActivity.EXTRA_DEBTOR_ID, itemId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem mSearch = menu.findItem(R.id.action_search);

        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search");

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return true;
            }
        });

        MenuItem mSortAlpha = menu.findItem(R.id.action_sort_alphabetical);
        mSortAlpha.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "Preparing to sort");
                int currSort = mAdapter.currentSort;
                if(currSort == MainAdapter.ALPHA_ASC || currSort == MainAdapter.ALPHA_DESC){
                    mAdapter.sort(-currSort);
                } else {
                    mAdapter.sort(MainAdapter.ALPHA_ASC);
                }
                return false;
            }
        });

        MenuItem mSortTotal = menu.findItem(R.id.action_sort_amount);
        mSortTotal.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "Preparing to sort");
                int currSort = mAdapter.currentSort;
                if(currSort == MainAdapter.TOTAL_ASC || currSort == MainAdapter.TOTAL_DESC){
                    mAdapter.sort(-currSort);
                } else {
                    mAdapter.sort(MainAdapter.TOTAL_ASC);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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

        return super.onOptionsItemSelected(item);
    }
}
