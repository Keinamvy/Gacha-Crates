package com.gmail.cparse2021.gachacrates.cache;

import com.gmail.cparse2021.gachacrates.struct.crate.CrateSession;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class SessionManager {
    private final HashMap<UUID, CrateSession> crateSessions = new HashMap<>();

    public void clearSession(UUID playerUuid) {
        crateSessions.remove(playerUuid);
    }

    @Nullable
    public CrateSession getCrateSession(UUID playerUuid) {
        return crateSessions.get(playerUuid);
    }

    public void registerSession(CrateSession crateSession) {
        crateSessions.put(crateSession.getPlayerUuid(), crateSession);
    }
}