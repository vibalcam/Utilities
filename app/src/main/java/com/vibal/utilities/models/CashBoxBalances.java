package com.vibal.utilities.models;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import com.vibal.utilities.util.Util;

import java.util.ArrayList;
import java.util.List;

// imp make not abstract and entry is the parent class
public abstract class CashBoxBalances {
    /**
     * IMPORTANT: Balances already sorted from greater to lowest
     */
    @NonNull
    @CheckResult
    public static List<Transaction> getBalanceTransactions(@NonNull List<Entry> balances) {
        List<Transaction> transactions = new ArrayList<>();
        List<Entry> sorted = new ArrayList<>(balances);
//        sorted.sort((entry1, entry2) -> -Double.compare(entry1.amount, entry2.amount)); // minus to sort from greater to lowest

        // IMPORTANT: Max is positive (money lent) and min is negative (debt)
        Entry max, min;
        while (sorted.size() > 1) {
            max = sorted.get(0);
            min = sorted.get(sorted.size() - 1);

            if (max.amount >= -min.amount) {
                // Change balanced entries
                sorted.remove(sorted.size() - 1);
                if (max.amount == -min.amount)
                    sorted.remove(0);
                else
                    sorted.set(0, new Entry(max.fromName, max.amount + min.amount));

                // Add transaction
                transactions.add(new Transaction(min.fromName, max.fromName, min.amount));
            } else { // max.cash < min.cash
                // Change balanced entries
                sorted.remove(0);
                sorted.set(sorted.size() - 1, new Entry(min.fromName, min.amount + max.amount));

                // Add transaction
                transactions.add(new Transaction(min.fromName, max.fromName, max.amount));
            }
        }

        return transactions;
    }

    public static class Entry {
        @ColumnInfo(name = "name")
        @NonNull
        private final String fromName;
        private final double amount;

        public Entry(@NonNull String fromName, double amount) {
            this.fromName = fromName;
            this.amount = Util.roundTwoDecimals(amount);
        }

        @NonNull
        public String getFromName() {
            return fromName;
        }

        public String printFromName() {
            return Participant.printName(getFromName());
        }

        public double getAmount() {
            return amount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Double.compare(entry.amount, amount) == 0 &&
                    fromName.equals(entry.fromName);
        }
    }

    public static class Transaction extends Entry {
        @NonNull
        private final String toName;

        private Transaction(String fromName, @NonNull String toName, double cash) {
            super(fromName, Math.abs(cash));
            this.toName = toName;
        }

        @NonNull
        public String getToName() {
            return toName;
        }

        public String printToName() {
            return Participant.printName(getToName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Transaction that = (Transaction) o;
            return toName.equals(that.toName);
        }
    }
}
