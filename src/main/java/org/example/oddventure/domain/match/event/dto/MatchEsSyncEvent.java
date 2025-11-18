package org.example.oddventure.domain.match.event.dto;

import java.io.Serializable;

public record MatchEsSyncEvent(

        SyncType syncType,
        Long matchId
) implements Serializable {
    public enum SyncType {

        CREATE,
        UPDATE,
        DELETE
    }
}