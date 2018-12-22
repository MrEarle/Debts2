package com.example.mrearle.debts.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "debt",
        foreignKeys = @ForeignKey(entity = Debtor.class, parentColumns = "id",
                                  childColumns = "debtor_id", onDelete = CASCADE,
                                  onUpdate = CASCADE),
        indices = @Index("debtor_id"))
public class Debt {
    // Debt relational table
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "debt_id")
    public int debtId;
    @ColumnInfo(name = "debtor_id")
    public int debtorId;
    public Integer amount;
    public String date;
    public String description;
    private int checked;

    @Ignore
    public Debt(Integer amount, String date, String description, int debtorId){
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.debtorId = debtorId;
        this.checked = 0;
    }

    public Debt(Integer amount, String date, String description, int debtorId, int debtId, int checked){
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.debtorId = debtorId;
        this.debtId = debtId;
        this.checked = checked;
    }

    public int getId() {
        return debtId;
    }

    public void setId(int debtId) {
        this.debtId = debtId;
    }

    public int getChecked(){
        return checked;
    }

    public void setChecked(int checked){
        this.checked = checked;
    }

    public void check(){
        this.checked = 1;
    }

    public void uncheck(){
        this.checked = 0;
    }

    public boolean isChecked(){
        return this.checked == 1;
    }
}
