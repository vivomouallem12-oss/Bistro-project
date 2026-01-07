package logic;

import java.io.Serializable;

public enum RequestType implements Serializable {

    // ===== reservation flow =====
    CHECK_AVAILABILITY,
    CREATE_RESERVATION,

    // ===== code lookup =====
    CONFIRMATION_CODE,
    SEND_CODE,

    // ===== cancel / payment =====
    CANCEL_RESERVATION,
    PAY_BILL,

    // ===== terminal =====
    TERMINAL_CHECKIN,

    // ===== history =====
    GET_ACCOUNT_HISTORY
}
