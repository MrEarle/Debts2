package com.example.mrearle.debts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mrearle.debts.Database.Debt;
import com.example.mrearle.debts.Database.Debtor;
import com.example.mrearle.debts.Database.DebtorLedger;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class DebtorAdapter extends RecyclerView.Adapter<DebtorAdapter.DebtorAdapterViewHolder> {
    // Constant for date format

    // Member variable to handle item clicks
    final private DebtorAdapter.ItemClickListener mItemClickListener;
    // Class variables for the List that holds task data and the Context
    private DebtorLedger mDebtor;
    private List<Debt> mDebts;
    private Context mContext;
    // Colors
    private static final int POSITIVE_COLOR = Color.rgb(0,94,0);
    private static final int NEGATIVE_COLOR = Color.RED;

    /**
     * Constructor for the TaskAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public DebtorAdapter(Context context, DebtorAdapter.ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new MainAdapterViewHolder that holds the view for each task
     */
    @Override
    public DebtorAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.debt_layout, parent, false);

        return new DebtorAdapter.DebtorAdapterViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(DebtorAdapterViewHolder holder, int position) {
        // Determine the values of the wanted data
        Debt debt = mDebts.get(position);
        Integer amount = debt.amount;
        String description = debt.description;
        String date = debt.date;
        Log.d(TAG, "Modifying some stuff");
        //Set value
        holder.debtAmountView.setText(String.format(Locale.US, "$ %d", amount));
        holder.debtDescriptionView.setText(description);
        holder.debtDateView.setText(date);

        if (amount >= 0){
            holder.debtAmountView.setTextColor(POSITIVE_COLOR);
        } else holder.debtAmountView.setTextColor(NEGATIVE_COLOR);

        Log.d(TAG, String.format("%s checked is %d", debt.description, debt.getChecked()));

        if (debt.isChecked()) {
            holder.debtAmountView.setPaintFlags(holder.debtAmountView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.debtAmountView.setPaintFlags(holder.debtAmountView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mDebts == null) {
            return 0;
        }
        return mDebts.size();
    }

    public List<Debt> getDebts() {
        return mDebts;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setDebts(List<Debt> debts) {
        mDebts = debts;
        notifyDataSetChanged();
    }

    public void setDebtor(DebtorLedger debtor) {
        mDebtor = debtor;
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId, String name);
    }

    public interface LongItemClickListener {
        void onLongItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    class DebtorAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the task description and priority TextViews
        TextView debtAmountView;
        TextView debtDescriptionView;
        TextView debtDateView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public DebtorAdapterViewHolder(View itemView) {
            super(itemView);

            debtAmountView = itemView.findViewById(R.id.tv_amount);
            debtDescriptionView = itemView.findViewById(R.id.tv_description);
            debtDateView = itemView.findViewById(R.id.tv_date);
            Log.d(TAG, "Setting DebtorAdapterViewHolder views");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mDebts.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId, mDebtor.getName());
        }
    }
}
