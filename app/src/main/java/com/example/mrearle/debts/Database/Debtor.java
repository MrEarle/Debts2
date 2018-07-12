package com.example.mrearle.debts.Database;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Relation;

import java.util.List;

@Entity(tableName = "debtor", indices = {@Index(value = {"name"}, unique = true)})
public class Debtor {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;

    @Ignore
    public Debtor(String name) {
        this.name = name;
    }

    public Debtor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

}

