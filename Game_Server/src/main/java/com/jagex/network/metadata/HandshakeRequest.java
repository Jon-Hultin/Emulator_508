package com.jagex.network.metadata;

import com.jagex.network.message.messages.HandshakeMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *     Meta-data descriptor constants for operation codes receive from the client.
 * </p>
 *
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/17/2022}
 * @version 1.0.0
 */
public enum HandshakeRequest {

    /**
     * Login request operation.
     */
    LOGIN_REQUEST(14),

    /**
     * JS5 Cache update request operation.
     */
    JS5_UPDATE_REQUEST(15),

    /**
     * JAGGRAB request operation.
     */
    JAGGRAB_REQUEST(17);

    /**
     * An undisclosed map of {@link HandshakeRequest} key by their operation code.
     */
    private static final Map<Integer, HandshakeRequest> requests = new HashMap<>();

    /**
     * Populates {@code requests} on class load.
     */
    static {
        for (HandshakeRequest request : values()) {
            requests.put(request.opcode, request);
        }
    }

    /**
     * The operation code pertaining to the client request.
     */
    private final int opcode;

    /**
     * Constructs a new {@link HandshakeRequest}.
     *
     * @param opcode The operation code pertaining to the client request.
     */
    HandshakeRequest(int opcode) {
        this.opcode = opcode;
    }

    /**
     * Gets the operation code of this {@link HandshakeRequest}.
     *
     * @return {@code opcode}.
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * Returns a {@link HandshakeRequest} by its {@code opcode} value.
     *
     * @param opcode
     * @return {@link HandshakeRequest}
     */
    public static HandshakeRequest ofOpcode(int opcode) {
        return requests.get(opcode);
    }

}
