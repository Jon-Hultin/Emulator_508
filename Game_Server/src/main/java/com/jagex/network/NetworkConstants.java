package com.jagex.network;

/**
 * <p>
 * A container class for storing constants pertaining to the <b>Network</b>.
 * </p>
 *
 * <br>
 *
 * <p>
 * This class is non-instantiable.
 * </p>
 *
 * @version 1.0.0
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/17/2022}
 */
public final class NetworkConstants {

    /**
     * The game server's port address.
     */
    public static final int GAME_PORT = 43594;

    /**
     * The default connection timeout.
     */
    public static final int DEFAULT_TIMEOUT = 5;

    /**
     * Non-instantiable constructor.
     */
    private NetworkConstants() { }
}
