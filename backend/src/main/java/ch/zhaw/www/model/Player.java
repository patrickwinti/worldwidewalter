package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;

/**
 * Model class with basic
 * information of the player
 */
@Data
public class Player {
    @NotNull
    private String id;
    private String name;
    private boolean connected = true;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Instant disconnectedSince;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Marks the player as connected and clears any pending disconnect grace timer.
     */
    public void markConnected() {
        this.connected = true;
        this.disconnectedSince = null;
    }

    /**
     * Marks the player as disconnected. The grace timer is anchored at the first drop, so a
     * repeated disconnect event does not reset how long the player has already been gone.
     *
     * @param now the moment the disconnect was observed
     */
    public void markDisconnected(Instant now) {
        this.connected = false;
        if (this.disconnectedSince == null) {
            this.disconnectedSince = now;
        }
    }

    /**
     * Whether the player should still be counted as present. A connected player is always
     * present; a disconnected player is treated as present until the grace period elapses, so
     * that brief connection hiccups do not drop them from the game.
     *
     * @param now   the current time
     * @param grace how long a disconnected player is still treated as present
     * @return true if connected, or disconnected no longer than the grace period ago
     */
    public boolean isPresent(Instant now, Duration grace) {
        if (connected || disconnectedSince == null) {
            return true;
        }
        return Duration.between(disconnectedSince, now).compareTo(grace) <= 0;
    }
}
