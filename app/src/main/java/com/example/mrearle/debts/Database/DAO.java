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
    // DAO for database
    // Debtor Queries

    // All debtors as Live Data
    @Transaction
    @Query("SELECT * FROM debtor")
    LiveData<List<DebtorLedger>> loadAllDebtors();

    // Normal debtors
    @Query("SELECT * FROM debtor")
    List<DebtorLedger> loadAllNormalDebtors();

    // Insert debtor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDebtor(Debtor debtor);

    // Update debtor
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateDebtor(Debtor debtor);

    // Delete debtor
    @Delete
    void deleteDebtor(Debtor debtor);

    // Get a debtor from his id as Live Data
    @Transaction
    @Query("SELECT * FROM debtor WHERE debtor.id = :id")
    LiveData<DebtorLedger> getDebtorById(int id);

    // Get debtor by Id, but not as live data
    @Transaction
    @Query("SELECT * FROM debtor WHERE debtor.id = :id")
    DebtorLedger getNormalDebtorById(int id);

    // Debt Queries

    // Insert debt
    @Insert
    void insertDebt(Debt debt);

    // Create debt
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateDebt(Debt debt);

    // Delete debt
    @Delete
    void deleteDebt(Debt debt);

    // Select All debts from a debtor, ordered by id, as Live Data
    @Query("SELECT * FROM debt WHERE debtor_id = :id ORDER BY date")
    LiveData<List<Debt>> loadDebtsFromId(int id);

    // Get debt by id as Live Data
    @Query("SELECT * FROM debt WHERE debt_id = :id")
    LiveData<Debt> getDebtById(int id);
}
