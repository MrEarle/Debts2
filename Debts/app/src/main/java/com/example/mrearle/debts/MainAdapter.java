package com.example.mrearle.debts;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mrearle.debts.Database.Debt;
import com.example.mrearle.debts.Database.DebtorLedger;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainAdapterViewHolder> {

    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    final private LongItemClickListener mLongItemClickListener;
    // Class variables for the List that holds task data and the Context
    private List<DebtorLedger> mDebtors;
    private Context mContext;
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    // Colors
    private static final int POSITIVE_COLOR = Color.rgb(0,94,0);
    private static final int NEGATIVE_COLOR = Color.RED;

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
        DebtorLedger debtor = mDebtors.get(position);
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
        if (mDebtors == null) {
            return 0;
        }
        return mDebtors.size();
    }

    public List<DebtorLedger> getDebtors() {
        return mDebtors;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setDebtors(List<DebtorLedger> debtors) {
        mDebtors = debtors;
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
            int elementId = mDebtors.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId, mDebtors.get(getAdapterPosition()).getName());
        }

        @Override
        public boolean onLongClick(View view) {
            int elementId = mDebtors.get(getAdapterPosition()).getId();
            mLongItemClickListener.onLongItemClickListener(elementId);
            return false;
        }
    }

}
