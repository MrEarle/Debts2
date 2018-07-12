package com.example.mrearle.debts.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;
import android.content.DialogInterface;

import java.util.List;

@Dao
public interface DAO {
    // Debtor Queries
    @Transaction
    @Query("SELECT * FROM debtor")
    LiveData<List<DebtorLedger>> loadAllDebtors();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDebtor(Debtor debtor);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateDebtor(Debtor debtor);

    @Delete
    void deleteDebtor(Debtor debtor);

    @Transaction
    @Query("SELECT * FROM debtor WHERE debtor.id = :id")
    LiveData<DebtorLedger> getDebtorById(int id);

    @Transaction
    @Query("SELECT * FROM debtor WHERE debtor.id = :id")
    DebtorLedger getNormalDebtorById(int id);

    // Debt Queries
    @Insert
    void insertDebt(Debt debt);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateDebt(Debt debt);

    @Delete
    void deleteDebt(Debt debt);

    @Query("SELECT * FROM debt WHERE debtor_id = :id ORDER BY date")
    LiveData<List<Debt>> loadDebtsFromId(int id);

    @Query("SELECT * FROM debt WHERE debt_id = :id")
    LiveData<Debt> getDebtById(int id);
}
