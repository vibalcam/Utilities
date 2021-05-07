package com.vibal.utilities.models;

import androidx.annotation.NonNull;
import androidx.room.Ignore;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class EntryLocal extends EntryBase<EntryInfo> {
    @NonNull
    @Relation(parentColumn = "id", entityColumn = "entryId",
            entity = EntryInfo.ParticipantFromView.class)
    private final List<Participant> fromParticipants;
    @NonNull
    @Relation(parentColumn = "id", entityColumn = "entryId",
            entity = EntryInfo.ParticipantToView.class)
    private final List<Participant> toParticipants;

    /**
     * Constructor for Room and for cloning in EntryBase using reflection (do not change)
     */
    public EntryLocal(@NonNull EntryInfo entryInfo, @NonNull List<Participant> fromParticipants,
                      @NonNull List<Participant> toParticipants) {
        super(entryInfo, fromParticipants, toParticipants);
        this.fromParticipants = fromParticipants;
        this.toParticipants = toParticipants;
    }

    @Ignore
    public EntryLocal(@NonNull EntryInfo entry) {
        this(entry, new ArrayList<>(), new ArrayList<>());
    }

    @NonNull
    @Override
    public List<Participant> getFromParticipants() {
        return fromParticipants;
    }

    @NonNull
    @Override
    public List<Participant> getToParticipants() {
        return toParticipants;
    }
}
