package com.vibal.utilities.models;

import androidx.annotation.NonNull;
import androidx.room.Embedded;

import com.vibal.utilities.util.DiffDbUsable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class EntryBase<E extends EntryInfo> implements DiffDbUsable<EntryBase<?>>, Cloneable {

    @NonNull
    @Embedded
    private final E entryInfo;

    @NonNull
    private static EntryBase<EntryInfo> getInstance(EntryInfo entryInfo, List<Participant> fromParticipants,
                                                    List<Participant> toParticipants) {
        return new EntryLocal(entryInfo, fromParticipants, toParticipants);
    }

    @NonNull
    public static EntryBase<EntryInfo> getInstance(EntryInfo entryInfo, Participant participant1,
                                                   Participant participant2) {
        ArrayList<Participant> fromParticipants = new ArrayList<>();
        ArrayList<Participant> toParticipants = new ArrayList<>();
        if (participant1.isFrom())
            fromParticipants.add(participant1);
        else
            toParticipants.add(participant1);
        if (participant2.isFrom())
            fromParticipants.add(participant2);
        else
            toParticipants.add(participant2);
        return getInstance(entryInfo, fromParticipants, toParticipants);
    }

    @NonNull
    public static EntryBase<EntryInfo> getInstance(EntryInfo entryInfo) {
        return new EntryLocal(entryInfo);
    }

    public EntryBase(@NonNull E entryInfo, @NonNull List<Participant> fromParticipants,
                     @NonNull List<Participant> toParticipants) {
        this.entryInfo = entryInfo;
        checkParticipantsEmpty(fromParticipants, toParticipants);
    }

    private void checkParticipantsEmpty(@NonNull List<Participant> fromParticipants,
                                        @NonNull List<Participant> toParticipants) {
        if (fromParticipants.isEmpty())
            fromParticipants.add(Participant.newFrom(Participant.getSelfName()));
        if (toParticipants.isEmpty())
            toParticipants.add(Participant.newTo(Participant.getSelfName()));
    }

    @NonNull
    public static String formatParticipants(@NonNull Collection<Participant> participants) {
        if (participants.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();
        for (Participant p : participants)
            builder = builder.append(p.toString())
                    .append(',');

        return builder.deleteCharAt(builder.length() - 1).toString();

    }

    @NonNull
    public E getEntryInfo() {
        return entryInfo;
    }

    @NonNull
    public abstract List<Participant> getFromParticipants();

    @NonNull
    public abstract List<Participant> getToParticipants();

    public double getParticipantBalance(Participant participant) {
        double balance = 0;
        double sumParticipantsAmounts = 0;
        for (Participant p : getToParticipants()) {
            if (p.isSameParticipant(participant))
                balance += p.getAmount();
            sumParticipantsAmounts += p.getAmount();
        }
        balance /= Math.abs(sumParticipantsAmounts);
        double balanceFrom = 0;
        sumParticipantsAmounts = 0;
        for (Participant p : getFromParticipants()) {
            if (p.isSameParticipant(participant))
                balanceFrom += p.getAmount();
            sumParticipantsAmounts += p.getAmount();
        }
        return (balance + balanceFrom / Math.abs(sumParticipantsAmounts)) * getEntryInfo().getAmount();
    }

    // Implement DiffDbUssable

    @Override
    public boolean areItemsTheSame(@NonNull EntryBase<?> newItem) {
        return getEntryInfo().areItemsTheSame(newItem.getEntryInfo());
    }

    @Override
    public boolean areContentsTheSame(@NonNull EntryBase<?> newItem) {
        return getEntryInfo().areContentsTheSame(newItem.getEntryInfo()) &&
                Objects.equals(getFromParticipants(), newItem.getFromParticipants()) &&
                Objects.equals(getToParticipants(), newItem.getToParticipants());
    }

    // Implement Cloneable

    @NonNull
    public EntryBase<?> cloneContents(long entryId, long cashBoxId) {
        List<Participant> cloneFromParticipants = new ArrayList<>();
        for (Participant p : getFromParticipants())
            cloneFromParticipants.add(p.cloneContents(entryId));
        List<Participant> cloneToParticipants = new ArrayList<>();
        for (Participant p : getToParticipants())
            cloneToParticipants.add(p.cloneContents(entryId));

        // Hack for clone in abstract using reflection
        try {
            EntryInfo entryInfo = getEntryInfo().cloneContents(entryId, cashBoxId);
            return getClass()
                    .getDeclaredConstructor(entryInfo.getClass(), List.class, List.class)
                    .newInstance(entryInfo, cloneFromParticipants, cloneToParticipants);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while cloning", e);
        }
    }

    /**
     * Clones the object without conserving the id and the cashBoxId
     *
     * @return the new object, product of the cloning
     */
    @NonNull
    public EntryBase<?> cloneContents() {
        return cloneContents(CashBoxInfo.NO_ID, CashBoxInfo.NO_ID);
    }

    /**
     * Clones the object conserving the id
     *
     * @return the new object, product of the cloning
     */
    @NonNull
    @Override
    public EntryBase<?> clone() {
        List<Participant> cloneFromParticipants = new ArrayList<>();
        for (Participant p : getFromParticipants())
            cloneFromParticipants.add(p.clone());
        List<Participant> cloneToParticipants = new ArrayList<>();
        for (Participant p : getToParticipants())
            cloneToParticipants.add(p.clone());

        // Hack for clone in abstract using reflection
        try {
            EntryInfo entryInfo = getEntryInfo().clone();
            return getClass()
                    .getDeclaredConstructor(entryInfo.getClass(), List.class, List.class)
                    .newInstance(entryInfo, cloneFromParticipants, cloneToParticipants);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while cloning", e);
        }
    }

@NonNull
public EntryBase<?> getEntryWithCashBoxId(long cashBoxId) {
    if (this.entryInfo.getCashBoxId() == cashBoxId)
        return this;

    List<Participant> cloneFromParticipants, cloneToParticipants;
    EntryInfo entryInfo = getEntryInfo().getEntryWithCashBoxId(cashBoxId);
    if (this.entryInfo.getCashBoxId() != CashBoxInfo.NO_ID) {    // get a clone with this cashbox id
        cloneFromParticipants = new ArrayList<>();
        for (Participant p : getFromParticipants())
            cloneFromParticipants.add(p.getParticipantWithEntryId(entryInfo.getId()));
        cloneToParticipants = new ArrayList<>();
        for (Participant p : getToParticipants())
            cloneToParticipants.add(p.getParticipantWithEntryId(entryInfo.getId()));
    } else {
        cloneFromParticipants = getFromParticipants();
        cloneToParticipants = getToParticipants();
    }

    try {
        return getClass()
                .getDeclaredConstructor(entryInfo.getClass(), List.class, List.class)
                .newInstance(entryInfo, cloneFromParticipants, cloneToParticipants);
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
        e.printStackTrace();
        throw new RuntimeException("Error while cloning", e);
    }
}

}
