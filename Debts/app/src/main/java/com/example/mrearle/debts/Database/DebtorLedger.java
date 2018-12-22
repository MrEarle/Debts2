package com.example.mrearle.debts.Database;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;
import android.arch.persistence.room.Transaction;

import java.util.List;

public class DebtorLedger {
    // Debtor Ledger relational table

    @Embedded
    public Debtor debtor;

    @Relation(parentColumn = "id", entityColumn = "debtor_id", entity = Debt.class)
    public List<Debt> debts;

    public int getTotal() {
        Integer total = 0;
        for (Debt debt : debts) {
            if (!debt.isChecked()) {
                total += debt.amount;
            }
        }
        return total;
    }

    public String getName() {
        return debtor.name;
    }

    public int getId() {
        return debtor.id;
    }

    public void setId(int id) {
        debtor.id = id;
    }
}
