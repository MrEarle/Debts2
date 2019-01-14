package com.example.mrearle.debts;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.mrearle.debts.Database.Debt;
import com.example.mrearle.debts.Database.DebtorLedger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainAdapterViewHolder>
                            implements Filterable {


    private static final String TAG = MainAdapter.class.getSimpleName();
    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";
    // Colors
    private static final int POSITIVE_COLOR = Color.rgb(0,94,0);
    private static final int NEGATIVE_COLOR = Color.RED;
    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    final private LongItemClickListener mLongItemClickListener;
    // Class variables for the List that holds task data and the Context
    private List<DebtorLedger> mDebtors;
    private List<DebtorLedger> mFilteredDebtors;
    private Context mContext;
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    public static final int ALPHA_ASC = 1;
    public static final int ALPHA_DESC = -1;
    public static final int TOTAL_ASC = 2;
    public static final int TOTAL_DESC = -2;
    public int currentSort = ALPHA_ASC;

    /**
     * Constructor for the TaskAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public MainAdapter(Context context, ItemClickListener listener, LongItemClickListener longListener) {
        mContext = context;
        mItemClickListener = listener;
        mLongItemClickListener = longListener;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new MainAdapterViewHolder that holds the view for each task
     */
    @Override
    public MainAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.debtor_layout, parent, false);

        return new MainAdapterViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(MainAdapterViewHolder holder, int position) {
        // Determine the values of the wanted data
        DebtorLedger debtor = mFilteredDebtors.get(position);
        String name = debtor.getName();
        Integer total = debtor.getTotal();

        //Set values
        holder.debtorNameView.setText(name);
        holder.debtorTotalView.setText(String.format(Locale.US, "Total: $%d", total));

        if (total >= 0){
            holder.debtorTotalView.setTextColor(POSITIVE_COLOR);
        } else holder.debtorTotalView.setTextColor(NEGATIVE_COLOR);
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mFilteredDebtors == null) {
            return 0;
        }
        return mFilteredDebtors.size();
    }

    public List<DebtorLedger> getDebtors() {
        return mFilteredDebtors;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setDebtors(List<DebtorLedger> debtors) {
        mDebtors = debtors;
        mFilteredDebtors = debtors;
        notifyDataSetChanged();
    }

    public void filter(String query) {
        getFilter().filter(query);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Pattern pattern = Pattern.compile(constraint.toString().toLowerCase());
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0){
                    results.values = mDebtors;
                    results.count = mDebtors.size();
                } else{
                    ArrayList<DebtorLedger> filteredDebtors = new ArrayList<>();
                    for(DebtorLedger debtor : mDebtors) {
                        String name = debtor.getName().toLowerCase();
                        Matcher matcher = pattern.matcher(name);
                        if(matcher.find()){
                            filteredDebtors.add(debtor);
                        }
                    }
                    results.values = filteredDebtors;
                    results.count = filteredDebtors.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredDebtors = (List<DebtorLedger>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void sort(final int order) {
        Log.d(TAG, "Starting Sorting with code" + Integer.toString(order));
        if(order == currentSort){
            return;
        } else if(order == ALPHA_ASC || order == ALPHA_DESC){
            Collections.sort(mDebtors, new Comparator<DebtorLedger>() {
                @Override
                public int compare(DebtorLedger o1, DebtorLedger o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName()) * Integer.signum(order);
                }
            });
        } else if(order == TOTAL_ASC || order == TOTAL_DESC){
            Collections.sort(mDebtors, new Comparator<DebtorLedger>() {
                @Override
                public int compare(DebtorLedger o1, DebtorLedger o2) {
                    int result = 0;
                    if(o1.getTotal() > o2.getTotal()){
                        result = 1;
                    }
                    else if(o1.getTotal() < o2.getTotal()) {
                        result = -1;
                    }
                    return result * Integer.signum(order);
                }
            });
        }
        Log.d(TAG, "Done Sorting");
        currentSort = order;
        mFilteredDebtors = mDebtors;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId, String name);
    }

    public interface LongItemClickListener {
        void onLongItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    class MainAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // Class variables for the task description and priority TextViews
        TextView debtorNameView;
        TextView debtorTotalView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public MainAdapterViewHolder(View itemView) {
            super(itemView);

            debtorNameView = itemView.findViewById(R.id.tv_name);
            debtorTotalView = itemView.findViewById(R.id.tv_total);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mFilteredDebtors.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId, mDebtors.get(getAdapterPosition()).getName());
        }

        @Override
        public boolean onLongClick(View view) {
            int elementId = mFilteredDebtors.get(getAdapterPosition()).getId();
            mLongItemClickListener.onLongItemClickListener(elementId);
            return false;
        }
    }

}
