package Server;




import java.io.IOException;


import java.sql.PreparedStatement;
//__________________

import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

import logic.*;
import logic.EmailService;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;


public class EchoServer extends AbstractServer {

    String emailForCode = null;
    private final Map<ConnectionToClient, String> clientIPs = new ConcurrentHashMap<>();

    private static final ExecutorService EMAIL_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("EMAIL_EXECUTOR");
                return t;
            });

    public EchoServer(int port) {
        super(port);
    }

    // =====================================================
    // MESSAGE HANDLER
    // =====================================================
    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {

        MySQLConnectionPool pool = null;
        Connection conn = null;

        try {
            pool = MySQLConnectionPool.getInstance();
            conn = pool.getConnection();

            if (msg instanceof SubscriberLoginRequest loginReq) {
                handleSubscriberLogin(conn, loginReq, client);
                return;
            }

            if (msg instanceof Request req) {
                switch (req.getStatus()) {

                    case "CHECK_AVAILABILITY" -> handleCheckAvailability(conn, req, client);
                    case "CREATE_RESERVATION" -> handleCreateReservation(conn, req, client);

                    case "CHECK_ORDER_FOR_PAYMENT" -> handleCheckOrderForPayment(conn, req, client);
                    case "PAY_ORDER", "PAY_BILL" -> handlePayOrder(conn, req, client);

                    case "CANCEL_ORDER" -> handleCancelOrder(conn, req, client);

                    case "TERMINAL_CHECKIN" -> handleTerminalCheckIn(conn, req, client);

                    case "ACCOUNT_VISITS" -> sendVisitHistory(conn, (int) req.getData(), client);
                    case "ACCOUNT_RESERVATIONS" -> sendReservationHistory(conn, (int) req.getData(), client);


                    case "LOST_CONFIRMATION_CODE" -> handleLostConfirmationCode(conn, req, client);

                    // ✅ WAITING LIST (subscriber-only)
                    case "JOIN_WAITING_LIST" -> handleJoinWaitingList(conn, req, client);
                    case "LEAVE_WAITING_LIST" -> handleLeaveWaitingList(conn, req, client);

                    // DONT TOUCH!!!!!!!!!!
                    case "FREE_TABLES" -> handleFreeTables(conn, req, client);
                    case "INSERT_NEW_CUSTOMER" -> handleInsertCustomer(conn, req, client);
                    case "INSERT_NEW_ORDER" -> handleInsertOrder(conn, req, client);
                    case "CONFIRMATION_CODE" -> handleConfirmationCode(conn, req, client);
                    case "SEND_CODE" -> handleSendCode(conn, req, client);
                    case "UPDATE_SUBSCRIBER_EMAIL" -> handleUpdateEmail(conn, req, client);
                    case "UPDATE_SUBSCRIBER_PHONE" -> handleUpdatePhone(conn, req, client);
                    case "UPDATE_TABLE" -> handleUpdateTable(conn, req, client);
                    case "GET_TABLES" -> handleGetTables(conn, client);
                    case "ADD_TABLE" -> handleAddTable(conn, req, client);
                    case "DELETE_TABLE" -> handleDeleteTable(conn, req, client);
                    case "GET_ALL_RESERVATIONS" -> handleGetAllReservations(conn, client);
                    case "REGISTER_SUBSCRIBER" -> handleRegisterSubscriber(conn, req, client);
                    case "GET_MANAGEMENT_INFO" -> handleGetManagementInfo(conn, client);

                    case "GET_REPORTS_DATA" -> handleGetReportsData(conn, client);
   
                    case "SET_OPEN_HOURS" -> handleSetOpeningHours(conn, req, client);



                    default -> System.out.println("[SERVER] Unknown request: " + req.getStatus());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("SERVER_ERROR");
            } catch (Exception ignored) {}
        } finally {
            if (pool != null && conn != null) pool.releaseConnection(conn);
        }
    }

    // =====================================================
    // ✅ LOST CONFIRMATION CODE
    // =====================================================
    private void handleLostConfirmationCode(Connection conn, Request req, ConnectionToClient client) throws Exception {
        int subId = (int) req.getData();

        String sql =
                "SELECT order_date, order_time, confirmation_code, customer_email " +
                        "FROM `order` " +
                        "WHERE subscriber_id = ? AND order_status = 'BOOKED' " +
                        "ORDER BY order_date, order_time";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subId);
            ResultSet rs = ps.executeQuery();

            String email = null;
            List<String> codes = new ArrayList<>();

            while (rs.next()) {
                String date = rs.getString("order_date");
                String time = rs.getString("order_time");
                String code = rs.getString("confirmation_code");
                email = rs.getString("customer_email");
                codes.add("• " + date + " at " + time + " — Code: " + code);
            }

            if (codes.isEmpty() || email == null) {
                client.sendToClient(new Response("LOST_CODE_FAILED", null));
                return;
            }

            String finalEmail = email;
            List<String> finalCodes = List.copyOf(codes);

            EMAIL_EXECUTOR.submit(() -> {
                try { EmailService.LostCodeEmail(finalEmail, finalCodes); } catch (Exception ignored) {}
            });

            client.sendToClient(new Response("LOST_CODE_SENT", null));
        }
    }

    // =====================================================
    // TIME VALIDATION
    // =====================================================
    private String validateReservationWindow(LocalDate date, LocalTime time) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime requested = LocalDateTime.of(date, time);

        if (requested.isBefore(now.plusHours(1)))
            return "Reservation must be at least 1 hour from now.";

        if (requested.isAfter(now.plusMonths(1)))
            return "Reservation cannot be more than 1 month from now.";

        return null;
    }

    private LocalTime parseTimeFlexible(String s) {
        try {
            return LocalTime.parse(s);
        } catch (Exception e) {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"));
        }
    }

    private int requiredPlacesForGuests(int g) {
        if (g <= 2) return 2;
        if (g <= 4) return 4;
        if (g <= 6) return 6;
        return 10;
    }

    // =====================================================
    // AVAILABILITY
    // =====================================================
    private void handleCheckAvailability(Connection conn, Request req, ConnectionToClient client) throws Exception {
        Order o = (Order) req.getData();

        LocalDate d = LocalDate.parse(o.getOrder_date());
        LocalTime t = parseTimeFlexible(o.getOrder_time());

        String err = validateReservationWindow(d, t);
        if (err != null) {
            client.sendToClient(new Response("INVALID_RESERVATION_TIME", err));
            return;
        }

        int need = requiredPlacesForGuests(o.getNumber_of_guests());
        int total = countExactFitTables(conn, need);
        int used = countOverlapping(conn, d, t, need);

        if (used < total)
            client.sendToClient(new Response("AVAILABLE", null));
        else
            client.sendToClient(new Response("NOT_AVAILABLE", suggestTimes(conn, d, t, need)));
    }

    private int countExactFitTables(Connection conn, int p) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM tables WHERE places=?")) {
            ps.setInt(1, p);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    // ✅ IMPORTANT CHANGE: WAITING and WAITING_CALLED do NOT count as occupying schedule
    private int countOverlapping(Connection conn, LocalDate d, LocalTime t, int p) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM `order` " +
                        "WHERE order_date=? AND order_status IN ('BOOKED','SEATED','PAYED','BILL_SENT') " +
                        "AND (CASE WHEN number_of_guests<=2 THEN 2 WHEN number_of_guests<=4 THEN 4 " +
                        "WHEN number_of_guests<=6 THEN 6 ELSE 10 END)=? " +
                        "AND TIME(?) < ADDTIME(order_time,'02:00:00') " +
                        "AND order_time < ADDTIME(TIME(?),'02:00:00')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(d));
            ps.setInt(2, p);
            ps.setTime(3, Time.valueOf(t));
            ps.setTime(4, Time.valueOf(t));
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    // ✅ NEW: is there ANY 2-hour window later today?
    private boolean existsAny2hWindowToday(Connection conn, int places, LocalTime from) throws SQLException {

        LocalTime OPEN = LocalTime.of(17, 0);
        LocalTime CLOSE = LocalTime.of(21, 30);

        LocalTime start = from;
        if (start.isBefore(OPEN)) start = OPEN;

        // last possible start time must allow +2h
        LocalTime lastStart = CLOSE.minusHours(2);
        if (start.isAfter(lastStart)) return false;

        for (LocalTime t = start; !t.isAfter(lastStart); t = t.plusMinutes(15)) {
            int total = countExactFitTables(conn, places);
            int used = countOverlapping(conn, LocalDate.now(), t, places);
            if (used < total) return true;
        }
        return false;
    }
    
    
    // =====================================================
    // Subscriber edit phone number and email
    // =====================================================
    
    private void handleUpdateEmail(Connection conn, Request req, ConnectionToClient client)
            throws Exception {

        Map<?, ?> data = (Map<?, ?>) req.getData();

        int subscriberId = (int) data.get("subscriberId");
        String newEmail = (String) data.get("email");

        if (!newEmail.contains("@")) {
            client.sendToClient(new Response("UPDATE_EMAIL_FAILED", null));
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE subscriber SET subscriber_email=? WHERE subscriber_id=?")) {

            ps.setString(1, newEmail);
            ps.setInt(2, subscriberId);

            if (ps.executeUpdate() > 0)
                client.sendToClient(new Response("UPDATE_EMAIL_SUCCESS", newEmail));
            else
                client.sendToClient(new Response("UPDATE_EMAIL_FAILED", null));
        }
    }



    
    private void handleUpdatePhone(Connection conn, Request req, ConnectionToClient client)
            throws Exception {

        // ✅ EXPECT MAP (matches client)
        Map<?, ?> data = (Map<?, ?>) req.getData();

        int subscriberId = (int) data.get("subscriberId");
        String newPhone = (String) data.get("phone");

        if (!newPhone.matches("\\d{9,10}")) {
            client.sendToClient(new Response("UPDATE_PHONE_FAILED", null));
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE subscriber SET subscriber_phone=? WHERE subscriber_id=?")) {

            ps.setString(1, newPhone);
            ps.setInt(2, subscriberId);

            if (ps.executeUpdate() > 0)
                client.sendToClient(new Response("UPDATE_PHONE_SUCCESS", newPhone));
            else
                client.sendToClient(new Response("UPDATE_PHONE_FAILED", null));
        }
    }





    // =====================================================
    // CREATE RESERVATION
    // =====================================================
    private void handleCreateReservation(Connection conn, Request req, ConnectionToClient client) throws Exception {
        Order o = (Order) req.getData();

        LocalDate d = LocalDate.parse(o.getOrder_date());
        LocalTime t = parseTimeFlexible(o.getOrder_time());

        String err = validateReservationWindow(d, t);
        if (err != null) {
            client.sendToClient(new Response("INVALID_RESERVATION_TIME", err));
            return;
        }

        int code = generateCode(conn);

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO `order` (order_date,order_time,number_of_guests,confirmation_code," +
                        "subscriber_id,customer_phone,customer_email,date_of_placing_order,order_status,status_datetime) " +
                        "VALUES (?,?,?,?,?,?,?,?,'BOOKED',NOW())")) {

            ps.setDate(1, Date.valueOf(d));
            ps.setTime(2, Time.valueOf(t));
            ps.setInt(3, o.getNumber_of_guests());
            ps.setInt(4, code);
            ps.setInt(5, o.getSubscriber_id());
            ps.setString(6, o.getCustomer_phone());
            ps.setString(7, o.getCustomer_email());
            ps.setString(8, LocalDateTime.now().toString());
            ps.executeUpdate();
        }

        client.sendToClient(new Response("RESERVATION_CREATED", Map.of("confirmationCode", code)));

        String email = o.getCustomer_email();
        EMAIL_EXECUTOR.submit(() -> {
            try { EmailService.sendConfirmationEmail(email, code); } catch (Exception ignored) {}
        });
    }

    private int generateCode(Connection conn) throws SQLException {
        Random r = new Random();
        while (true) {
            int c = 100000 + r.nextInt(900000);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM `order` WHERE confirmation_code=?")) {
                ps.setInt(1, c);
                if (!ps.executeQuery().next()) return c;
            }
        }
    }

    // =====================================================
    // PAYMENT
    // =====================================================
    private void handleCheckOrderForPayment(Connection conn, Request req, ConnectionToClient client) throws Exception {
        int code = (int) req.getData();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT order_status,number_of_guests FROM `order` WHERE confirmation_code=?")) {
            ps.setInt(1, code);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                client.sendToClient(new Response("ORDER_NOT_FOUND", null));
                return;
            }

            String status = rs.getString(1);
            int guests = rs.getInt(2);

            if (!status.equals("SEATED") && !status.equals("BILL_SENT")) {
                client.sendToClient(new Response("ORDER_NOT_PAYABLE", status));
                return;
            }

            client.sendToClient(new Response(
                    "ORDER_PAYABLE",
                    Map.of("confirmationCode", code, "price", guests * 50.0)
            ));
        }
    }

    @SuppressWarnings("unused")
	private void handlePayOrder(Connection conn, Request req, ConnectionToClient client) throws Exception {
        int code = (int) req.getData();

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE `order` SET order_status='PAYED', status_datetime=NOW() " +
                        "WHERE confirmation_code=? AND order_status IN ('SEATED','BILL_SENT')")) {

            ps.setInt(1, code);
            int updated = ps.executeUpdate();
            client.sendToClient(new Response(updated > 0 ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED", null));
        }
    }

    // =====================================================
    // OTHER
    // =====================================================
    private void handleCancelOrder(Connection conn, Request req, ConnectionToClient client) throws Exception {
        int code = (int) req.getData();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE `order` SET order_status='CANCELLED_BY_USER' " +
                        "WHERE confirmation_code=? AND order_status='BOOKED'")) {
            ps.setInt(1, code);
            client.sendToClient(new Response(ps.executeUpdate() > 0 ? "CANCEL_SUCCESS" : "CANCEL_FAILED", null));
        }
    }

    // =====================================================
    // ✅ TERMINAL CHECK-IN (UPDATED: supports WAITING_CALLED)
    // =====================================================
    private void handleTerminalCheckIn(Connection conn, Request req, ConnectionToClient client) throws Exception {

        int code = (int) req.getData();

        String orderSql =
                "SELECT order_date, order_time, number_of_guests, order_status, table_num, status_datetime " +
                        "FROM `order` WHERE confirmation_code=?";

        try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
            ps.setInt(1, code);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                client.sendToClient(new Response("CHECKIN_CODE_NOT_FOUND", null));
                return;
            }

            LocalDate orderDate = rs.getDate("order_date").toLocalDate();
            LocalTime orderTime = rs.getTime("order_time").toLocalTime();
            int guests = rs.getInt("number_of_guests");
            String status = rs.getString("order_status");
            Integer tableNum = (Integer) rs.getObject("table_num");
            Timestamp stTs = rs.getTimestamp("status_datetime");

            if (!orderDate.equals(LocalDate.now())) {
                client.sendToClient(new Response("CHECKIN_WRONG_DAY", null));
                return;
            }

            // ✅ Waiting-called flow: must arrive within 15 minutes from status_datetime
            if ("WAITING_CALLED".equals(status)) {

                if (tableNum == null) {
                    client.sendToClient(new Response("CHECKIN_NOT_ALLOWED", "No table assigned."));
                    return;
                }

                if (stTs == null) {
                    client.sendToClient(new Response("CHECKIN_NOT_ALLOWED", "Missing call time."));
                    return;
                }

                LocalDateTime calledAt = stTs.toLocalDateTime();
                if (LocalDateTime.now().isAfter(calledAt.plusMinutes(15))) {
                    client.sendToClient(new Response("CHECKIN_TOO_LATE", null));
                    return;
                }

                try (PreparedStatement ups = conn.prepareStatement(
                        "UPDATE `order` SET order_status='SEATED', order_time=CURTIME(), status_datetime=NOW() " +
                                "WHERE confirmation_code=? AND order_status='WAITING_CALLED'")) {

                    ups.setInt(1, code);
                    int updated = ups.executeUpdate();

                    if (updated <= 0) {
                        client.sendToClient(new Response("CHECKIN_NOT_ALLOWED", "Could not update order."));
                        return;
                    }
                }

                client.sendToClient(new Response(
                        "CHECKIN_SUCCESS",
                        Map.of("table_num", tableNum, "confirmation_code", code)
                ));
                return;
            }

            // ✅ Regular booked flow (your original)
            if (!"BOOKED".equals(status)) {
                client.sendToClient(new Response("CHECKIN_NOT_ALLOWED", status));
                return;
            }

            LocalTime now = LocalTime.now();
            if (now.isBefore(orderTime.minusMinutes(15))) {
                client.sendToClient(new Response("CHECKIN_TOO_EARLY", null));
                return;
            }
            if (now.isAfter(orderTime.plusMinutes(15))) {
                client.sendToClient(new Response("CHECKIN_TOO_LATE", null));
                return;
            }

            int neededPlaces = requiredPlacesForGuests(guests);

            String tableSql =
                    "SELECT t.table_num " +
                            "FROM tables t " +
                            "WHERE t.places=? " +
                            "AND NOT EXISTS ( " +
                            "   SELECT 1 FROM `order` o " +
                            "   WHERE o.table_num = t.table_num " +
                            "   AND o.table_num IS NOT NULL " +
                            "   AND o.order_date = CURDATE() " +
                            "   AND o.order_status = 'SEATED' " +
                            "   AND TIME(NOW()) < ADDTIME(o.order_time,'02:00:00') " +
                            ") " +
                            "LIMIT 1";

            Integer newTable = null;
            try (PreparedStatement tps = conn.prepareStatement(tableSql)) {
                tps.setInt(1, neededPlaces);
                ResultSet trs = tps.executeQuery();
                if (trs.next()) newTable = trs.getInt(1);
            }

            if (newTable == null) {
                client.sendToClient(new Response("NO_TABLE_AVAILABLE", null));
                return;
            }

            try (PreparedStatement ups = conn.prepareStatement(
                    "UPDATE `order` SET order_status='SEATED', table_num=?, status_datetime=NOW() " +
                            "WHERE confirmation_code=? AND order_status='BOOKED'")) {

                ups.setInt(1, newTable);
                ups.setInt(2, code);
                int updated = ups.executeUpdate();

                if (updated <= 0) {
                    client.sendToClient(new Response("CHECKIN_NOT_ALLOWED", "Could not update order."));
                    return;
                }
            }

            client.sendToClient(new Response(
                    "CHECKIN_SUCCESS",
                    Map.of("table_num", newTable, "confirmation_code", code)
            ));
        }
    }

    // =====================================================
    // ✅ WAITING LIST (QUEUE + 15 MIN RULE)
    // =====================================================

    // Request data expected: Map { "subscriberId": int, "guests": int }
    private void handleJoinWaitingList(Connection conn, Request req, ConnectionToClient client) throws Exception {

        Object obj = req.getData();
        if (!(obj instanceof Map<?, ?> data)) {
            client.sendToClient(new Response("WAITING_JOIN_FAILED", "Invalid data"));
            return;
        }

        int subscriberId;
        int guests;
        try {
            subscriberId = (int) data.get("subscriberId");
            guests = (int) data.get("guests");
        } catch (Exception e) {
            client.sendToClient(new Response("WAITING_JOIN_FAILED", "Invalid fields"));
            return;
        }

        if (guests <= 0) {
            client.sendToClient(new Response("WAITING_JOIN_FAILED", "Invalid guests"));
            return;
        }

        // prevent duplicate waiting for same subscriber today
        try (PreparedStatement chk = conn.prepareStatement(
                "SELECT confirmation_code FROM `order` " +
                        "WHERE subscriber_id=? AND order_date=CURDATE() AND order_status IN ('WAITING','WAITING_CALLED','SEATED') " +
                        "ORDER BY status_datetime DESC LIMIT 1")) {
            chk.setInt(1, subscriberId);
            ResultSet crs = chk.executeQuery();
            if (crs.next()) {
                int existingCode = crs.getInt(1);
                client.sendToClient(new Response("WAITING_ALREADY_EXISTS", Map.of("confirmationCode", existingCode)));
                return;
            }
        }

        // get subscriber email/phone
        String email;
        String phone;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT subscriber_email, subscriber_phone FROM subscriber WHERE subscriber_id=?")) {
            ps.setInt(1, subscriberId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                client.sendToClient(new Response("WAITING_JOIN_FAILED", "Subscriber not found"));
                return;
            }
            email = rs.getString(1);
            phone = rs.getString(2);
        }

        LocalDate d = LocalDate.now();
        LocalTime now = LocalTime.now();
        int needPlaces = requiredPlacesForGuests(guests);

        // ✅ If NO 2-hour window exists today -> reject (requirement)
        if (!existsAny2hWindowToday(conn, needPlaces, now)) {
            client.sendToClient(new Response(
                    "WAITING_NOT_ALLOWED",
                    "No table is available for any 2-hour window today. Please come another day."
            ));
            return;
        }

        // ✅ If a PHYSICAL table is free RIGHT NOW -> SEATED immediately (your rule)
        Integer tableNow = findFreePhysicalTableNow(conn, needPlaces);

        int code = generateCode(conn);

        if (tableNow != null) {
            // seat immediately
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO `order` (order_date,order_time,number_of_guests,confirmation_code,subscriber_id," +
                            "customer_phone,customer_email,date_of_placing_order,table_num,order_status,status_datetime) " +
                            "VALUES (CURDATE(), CURTIME(), ?, ?, ?, ?, ?, ?, ?, 'SEATED', NOW())")) {

                ins.setInt(1, guests);
                ins.setInt(2, code);
                ins.setInt(3, subscriberId);
                ins.setString(4, phone);
                ins.setString(5, email);
                ins.setString(6, LocalDateTime.now().toString());
                ins.setInt(7, tableNow);
                ins.executeUpdate();
            }

            // email includes table number
            int finalCode = code;
            int finalTable = tableNow;
            String finalEmail = email;

            EMAIL_EXECUTOR.submit(() -> {
                try {
                    List<String> lines = List.of(
                            "✅ Your table is ready NOW!",
                            "Confirmation Code: " + finalCode,
                            "Table Number: " + finalTable,
                            "Please come to the hostess/terminal now."
                    );
                    EmailService.LostCodeEmail(finalEmail, lines);
                } catch (Exception ignored) {}
            });

            client.sendToClient(new Response(
                    "WAITING_SEATED",
                    Map.of("confirmationCode", code, "table_num", tableNow)
            ));
            return;
        }

        // ✅ Otherwise: join WAITING queue
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO `order` (order_date,order_time,number_of_guests,confirmation_code,subscriber_id," +
                        "customer_phone,customer_email,date_of_placing_order,table_num,order_status,status_datetime) " +
                        "VALUES (CURDATE(), CURTIME(), ?, ?, ?, ?, ?, ?, NULL, 'WAITING', NOW())")) {

            ins.setInt(1, guests);
            ins.setInt(2, code);
            ins.setInt(3, subscriberId);
            ins.setString(4, phone);
            ins.setString(5, email);
            ins.setString(6, LocalDateTime.now().toString());
            ins.executeUpdate();
        }

        // email with confirmation code
        String finalEmail = email;
        int finalCode = code;
        EMAIL_EXECUTOR.submit(() -> {
            try { EmailService.sendConfirmationEmail(finalEmail, finalCode); } catch (Exception ignored) {}
        });

        client.sendToClient(new Response("WAITING_JOINED", Map.of("confirmationCode", code)));
    }

    // Request data: int confirmationCode
    private void handleLeaveWaitingList(Connection conn, Request req, ConnectionToClient client) throws Exception {

        int code = (int) req.getData();

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE `order` SET order_status='CANCELLED_BY_USER', status_datetime=NOW() " +
                        "WHERE confirmation_code=? AND order_status IN ('WAITING','WAITING_CALLED')")) {

            ps.setInt(1, code);
            int updated = ps.executeUpdate();

            if (updated > 0) client.sendToClient(new Response("WAITING_LEFT", null));
            else client.sendToClient(new Response("WAITING_NOT_FOUND", null));
        }
    }

    // =====================================================
    // LOGIN + ACCOUNT
    // =====================================================
    private void handleSubscriberLogin(Connection conn, SubscriberLoginRequest req, ConnectionToClient client) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM subscriber WHERE subscriber_id=? AND BINARY subscriber_name=?")) {
            ps.setInt(1, Integer.parseInt(req.getSubscriberId()));
            ps.setString(2, req.getSubscriberName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                client.sendToClient(new Subscriber(
                        rs.getInt("subscriber_id"),
                        rs.getString("subscriber_name"),
                        rs.getString("subscriber_email"),
                        rs.getString("subscriber_phone")));
            } else client.sendToClient("LOGIN_FAIL");
        }
    }

    @SuppressWarnings("unused")
	private void sendAccountHistory(Connection conn, int id, ConnectionToClient client) throws Exception {
        List<Order> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM `order` WHERE subscriber_id=? ORDER BY order_date DESC")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("order_number"),
                        rs.getString("order_date"),
                        rs.getString("order_time"),
                        rs.getInt("number_of_guests"),
                        rs.getInt("confirmation_code"),
                        rs.getInt("subscriber_id"),
                        rs.getString("customer_phone"),
                        rs.getString("customer_email"),
                        rs.getString("date_of_placing_order"),
                        rs.getInt("table_num"),
                        rs.getString("order_status"),
                        rs.getString("status_datetime")));
            }
        }
        client.sendToClient(new Response("ACCOUNT_HISTORY", list));
    }
    private void sendVisitHistory(Connection conn, int subscriberId, ConnectionToClient client) throws Exception {

        List<Order> list = new ArrayList<>();

        String sql =
            "SELECT * FROM `order` " +
            "WHERE subscriber_id=? " +
            "AND order_status IN ('SEATED','BILL_SENT','PAYED','COMPLETED') " +
            "ORDER BY order_date DESC, order_time DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subscriberId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("order_number"),
                    rs.getString("order_date"),
                    rs.getString("order_time"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("confirmation_code"),
                    rs.getInt("subscriber_id"),
                    rs.getString("customer_phone"),
                    rs.getString("customer_email"),
                    rs.getString("date_of_placing_order"),
                    rs.getInt("table_num"),
                    rs.getString("order_status"),
                    rs.getString("status_datetime")
                ));
            }
        }

        client.sendToClient(new Response("ACCOUNT_VISITS", list));
    }
    private void sendReservationHistory(Connection conn, int subscriberId, ConnectionToClient client) throws Exception {

        List<Order> list = new ArrayList<>();

        String sql =
            "SELECT * FROM `order` " +
            "WHERE subscriber_id=? " +
            "AND order_status NOT IN ('CANCELLED_BY_USER','CANCELLED_NO_SHOW') " +
            "ORDER BY order_date DESC, order_time DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subscriberId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("order_number"),
                    rs.getString("order_date"),
                    rs.getString("order_time"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("confirmation_code"),
                    rs.getInt("subscriber_id"),
                    rs.getString("customer_phone"),
                    rs.getString("customer_email"),
                    rs.getString("date_of_placing_order"),
                    rs.getInt("table_num"),
                    rs.getString("order_status"),
                    rs.getString("status_datetime")
                ));
            }
        }

        client.sendToClient(new Response("ACCOUNT_RESERVATIONS", list));
    }



    // =====================================================
    // DONT TOUCH (your existing code below unchanged)
    // =====================================================
    private void handleFreeTables(Connection conn, Request req, ConnectionToClient client) throws Exception {
        Order o = (Order) req.getData();

        int guests = o.getNumber_of_guests();
        String date = o.getOrder_date();
        String time = o.getOrder_time();

        List<Tables> freeTables = new ArrayList<>();

        int tablePlace = 0;
        if (guests == 2) tablePlace = 2;
        else if (guests >= 3 && guests <= 4) tablePlace = 4;
        else if (guests >= 5 && guests <= 6) tablePlace = 6;
        else tablePlace = 10;

        String sql = "SELECT * FROM tables t WHERE t.places = ? AND t.table_num NOT IN ( " +
                "SELECT o.table_num FROM `order` o " +
                "WHERE o.order_date = ? AND o.order_status IN ('BOOKED', 'SEATED') " +
                "AND (TIME(?) < ADDTIME(o.order_time, '02:00:00') AND o.order_time < ADDTIME(TIME(?), '02:00:00'))" +
                ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tablePlace);
            ps.setString(2, date);
            ps.setString(3, time);
            ps.setString(4, time);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    freeTables.add(new Tables(rs.getInt("table_num"), rs.getInt("places")));
                }
            }

            if (!freeTables.isEmpty()) {
                client.sendToClient(new Response("FREE_TABLES_FOUND", freeTables));
                System.out.println("[FREE TABLES FOUND] " + freeTables);
            } else {
                LocalTime toTime = LocalTime.parse(time);
                LocalTime requested = LocalTime.of(toTime.getHour(), toTime.getMinute());

                List<LocalTime> candidates = List.of(
                        requested.minusMinutes(30),
                        requested.minusMinutes(60),
                        requested.plusMinutes(30),
                        requested.plusMinutes(60),
                        requested.plusMinutes(90),
                        requested.plusMinutes(120)
                );

                List<LocalTime> suggestions = new ArrayList<>();

                for (LocalTime candidate : candidates) {
                    String open = "17:00", close = "21:30";
                    LocalTime openTime = LocalTime.parse(open);
                    LocalTime closeTime = LocalTime.parse(close);

                    if (candidate.isBefore(openTime) || candidate.isAfter(closeTime)) continue;

                    try (PreparedStatement ps2 = conn.prepareStatement(sql)) {
                        ps2.setInt(1, tablePlace);
                        ps2.setDate(2, Date.valueOf(date));
                        ps2.setTime(3, Time.valueOf(candidate));
                        ps2.setTime(4, Time.valueOf(candidate));

                        ResultSet rs = ps2.executeQuery();
                        if (rs.next()) suggestions.add(candidate);
                    }
                    if (suggestions.size() == 3) break;
                }

                client.sendToClient(new Response("FREE_TABLES_NOT_FOUND", suggestions));
            }
        }
    }

    private void handleInsertCustomer(Connection conn, Request req, ConnectionToClient client) throws Exception {
        Customer c = (Customer) req.getData();
        String sql = "INSERT INTO customer (customer_name,customer_email,customer_phone) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCustomer_name());
            ps.setString(2, c.getCustomer_email());
            ps.setString(3, c.getCustomer_phone());
            int updated = ps.executeUpdate();

            emailForCode = c.getCustomer_email();

            if (updated > 0) {
                sendToAllClients("Customer inserted successfully!");
                System.out.println("[DB] Customer inserted: " + c);
            } else {
                sendToAllClients("Customer not inserted!");
                System.out.println("[DB] Customer not inserted: " + c.getCustomer_name());
            }
        }
    }

    private void handleInsertOrder(Connection conn, Request req, ConnectionToClient client) throws Exception {
        Order o = (Order) req.getData();
        String sql = "INSERT INTO `order` (order_number,order_date,order_time,number_of_guests,confirmation_code,subscriber_id,customer_phone,customer_email,date_of_placing_order,table_num,order_status,status_datetime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, o.getOrder_number());
            ps.setString(2, o.getOrder_date());
            ps.setString(3, o.getOrder_time());
            ps.setInt(4, o.getNumber_of_guests());
            ps.setInt(5, o.getConfirmation_code());
            ps.setInt(6, o.getSubscriber_id());
            ps.setString(7, o.getCustomer_phone());
            ps.setString(8, o.getCustomer_email());
            ps.setString(9, o.getDate_of_placing_order());
            ps.setInt(10, o.getTable_num());
            ps.setString(11, o.getOrder_status());
            ps.setString(12, o.getStatus_datetime());

            int updated = ps.executeUpdate();

            if (updated > 0) {
                EmailService.sendConfirmationEmail(o.getCustomer_email(), o.getConfirmation_code());
                client.sendToClient("Order inserted successfully!");
                System.out.println("[DB] Order inserted: " + o);
            } else {
                client.sendToClient("Order not inserted!");
                System.out.println("[DB] Order not inserted: " + o.getOrder_number());
            }
        }
    }

    private void handleConfirmationCode(Connection conn, Request req, ConnectionToClient client) throws Exception {

        int code = (int) req.getData();

        String orderSql = "SELECT * FROM `order` WHERE confirmation_code = ? AND order_status IN ('BOOKED', 'SEATED')";

        try (PreparedStatement ops = conn.prepareStatement(orderSql)) {

            ops.setInt(1, code);

            try (ResultSet ors = ops.executeQuery()) {

                if (!ors.next()) {
                    client.sendToClient(new Response("CODE_NOT_FOUND", null));
                    return;
                }

                Order o = new Order(
                        ors.getInt("order_number"),
                        ors.getString("order_date"),
                        ors.getString("order_time"),
                        ors.getInt("number_of_guests"),
                        ors.getInt("confirmation_code"),
                        ors.getInt("subscriber_id"),
                        ors.getString("customer_phone"),
                        ors.getString("customer_email"),
                        ors.getString("date_of_placing_order"),
                        ors.getInt("table_num"),
                        ors.getString("order_status"),
                        ors.getString("status_datetime")
                );

                String custSql = "SELECT * FROM customer WHERE customer_email = ?";
                try (PreparedStatement cps = conn.prepareStatement(custSql)) {
                    cps.setString(1, ors.getString("customer_email"));
                    try (ResultSet crs = cps.executeQuery()) {
                        Customer c = null;
                        if (crs.next()) {
                            c = new Customer(
                                    crs.getString("customer_name"),
                                    crs.getString("customer_email"),
                                    crs.getString("customer_phone"));
                        }
                        OrderCustomer oc = new OrderCustomer(o, c);
                        Response response = new Response("CODE_FOUND", oc);
                        client.sendToClient(response);
                    }
                }
            }
        }
    }
//for me
    private void handleSetOpeningHours(Connection conn,
            Request req,
            ConnectionToClient client) throws SQLException {

OpenHours hours = (OpenHours) req.getData();

System.out.println("=== OPEN HOURS RECEIVED ===");
System.out.println("day=" + hours.getDay());
System.out.println("open=" + hours.getOpen());
System.out.println("close=" + hours.getClose());

String sql =
"REPLACE INTO openhours (day, open, close) VALUES (?, ?, ?)";

try (PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setInt(1, hours.getDay());
ps.setString(2, hours.getOpen());
ps.setString(3, hours.getClose());
ps.executeUpdate();
}

try {
    client.sendToClient(new Response("OPENING_HOURS_UPDATED", null));
} catch (IOException e) {
    e.printStackTrace();
}

}
    //Update table
    private void handleUpdateTable(Connection conn,
            Request req,
            ConnectionToClient client) throws SQLException {

Tables t = (Tables) req.getData();

String sql = "UPDATE tables SET places=? WHERE table_num=?";

try (PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setInt(1, t.getPlaces());
ps.setInt(2, t.getTable_num());
ps.executeUpdate();
}

try {
client.sendToClient(new Response("TABLE_UPDATED", null));
} catch (IOException e) {
e.printStackTrace();
}
}
    private void handleGetTables(Connection conn,
            ConnectionToClient client) throws SQLException {

List<Tables> list = new ArrayList<>();

String sql = "SELECT table_num, places FROM tables";

try (PreparedStatement ps = conn.prepareStatement(sql);
ResultSet rs = ps.executeQuery()) {

while (rs.next()) {
list.add(new Tables(
rs.getInt("table_num"),
rs.getInt("places")
));
}
}

try {
client.sendToClient(new Response("TABLES_LIST", list));
} catch (IOException e) {
e.printStackTrace();
}
}
    private void handleAddTable(Connection conn,
            Request req,
            ConnectionToClient client) throws SQLException {

        int capacity = (int) req.getData();

       
        int nextTableNum = 1;
        String maxSql = "SELECT MAX(table_num) FROM tables";

        try (PreparedStatement ps = conn.prepareStatement(maxSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                nextTableNum = rs.getInt(1) + 1;
            }
        }

        
        String insertSql = "INSERT INTO tables (table_num, places) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, nextTableNum);
            ps.setInt(2, capacity);
            ps.executeUpdate();
        }

        
        handleGetTables(conn, client);
    }

    private void handleDeleteTable(Connection conn,
            Request req,
            ConnectionToClient client) throws SQLException {

int tableNum = (int) req.getData();

String sql = "DELETE FROM tables WHERE table_num=?";

try (PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setInt(1, tableNum);
ps.executeUpdate();
}

handleGetTables(conn, client); // חשוב!
}
    
    private void handleGetAllReservations(Connection conn,
            ConnectionToClient client) throws SQLException {

        List<Order> list = new ArrayList<>();

        String sql = """
            SELECT order_number, order_date, order_time,
                   number_of_guests, order_status
            FROM `order`
            ORDER BY order_date DESC, order_time DESC
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("order_number"),
                    rs.getString("order_date"),
                    rs.getString("order_time"),
                    rs.getInt("number_of_guests"),
                    0,
                    0,
                    null,
                    null,
                    null,
                    0,
                    rs.getString("order_status"),
                    null
                ));
            }
        }

        try {
            client.sendToClient(
                new Response("ALL_RESERVATIONS_LIST", list)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void handleRegisterSubscriber(
            Connection conn,
            Request req,
            ConnectionToClient client) throws SQLException {

        Subscriber sub = (Subscriber) req.getData();

        String sql =
            "INSERT INTO subscriber (subscriber_name, subscriber_email, subscriber_phone) " +
            "VALUES (?, ?, ?)";

        try (PreparedStatement ps =
                 conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, sub.getUsername());
            ps.setString(2, sub.getEmail());
            ps.setString(3, sub.getPhone());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);   // ← זה ה־ID שה־DB יצר
            }

            client.sendToClient(
                new Response("SUBSCRIBER_CREATED", id)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleGetManagementInfo(
            Connection conn,
            ConnectionToClient client) throws SQLException, IOException {

        int todayReservations = 0;
        int monthlyReservations = 0;
        int canceledReservations = 0;
        int subscribers = 0;
        int currentCustomers = 0;

        // 1️⃣ הזמנות היום
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM `order` WHERE order_date = CURDATE()")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) todayReservations = rs.getInt(1);
        }

        // 2️⃣ הזמנות החודש
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM `order` WHERE MONTH(order_date)=MONTH(CURDATE()) AND YEAR(order_date)=YEAR(CURDATE())")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) monthlyReservations = rs.getInt(1);
        }

        // 3️⃣ הזמנות שבוטלו
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM `order` WHERE order_status LIKE 'CANCELLED%'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) canceledReservations = rs.getInt(1);
        }

        // 4️⃣ מנויים
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM subscriber")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) subscribers = rs.getInt(1);
        }

        // 5️⃣ לקוחות שנמצאים במסעדה (SEATED)
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM `order` WHERE order_status='SEATED'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) currentCustomers = rs.getInt(1);
        }

        Map<String, Integer> data = new HashMap<>();
        data.put("today", todayReservations);
        data.put("month", monthlyReservations);
        data.put("canceled", canceledReservations);
        data.put("subscribers", subscribers);
        data.put("inside", currentCustomers);

        client.sendToClient(new Response("MANAGEMENT_INFO", data));
    }
    private void handleGetReportsData(
            Connection conn,
            ConnectionToClient client) throws SQLException, IOException {

        Map<String, Map<String, Integer>> data = new HashMap<>();

        Map<String, Integer> reservations = new LinkedHashMap<>();
        Map<String, Integer> waiting = new LinkedHashMap<>();
        Map<String, Integer> arrival = new LinkedHashMap<>();

        // ===== Reservations per week =====
        String sql = """
            SELECT WEEK(order_date) AS week,
                   COUNT(*) AS total
            FROM `order`
            WHERE MONTH(order_date) = MONTH(CURDATE())
              AND YEAR(order_date) = YEAR(CURDATE())
            GROUP BY WEEK(order_date)
            ORDER BY WEEK(order_date)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reservations.put(
                    "Week " + rs.getInt("week"),
                    rs.getInt("total")
                );
            }
        }

        // דמה (אפשר להחליף ב-SQL אמיתי בהמשך)
        arrival.put("Week 1", 5);
        arrival.put("Week 2", 8);
        arrival.put("Week 3", 4);
        arrival.put("Week 4", 6);

        waiting.put("Week 1", 10);
        waiting.put("Week 2", 14);
        waiting.put("Week 3", 9);
        waiting.put("Week 4", 12);

        data.put("arrival", arrival);
        data.put("reservations", reservations);
        data.put("waiting", waiting);

        client.sendToClient(new Response("REPORTS_DATA", data));
    }

    private void handleSendCode(Connection conn, Request req, ConnectionToClient client) throws SQLException {
        String email = (String) req.getData();

        String sql = "SELECT order_date, order_time, confirmation_code FROM `order` WHERE customer_email = ? AND order_status = 'BOOKED' ORDER BY order_date, order_time";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            List<String> codes = new ArrayList<>();
            while (rs.next()) {
                String date = rs.getString("order_date");
                String time = rs.getString("order_time");
                String code = rs.getString("confirmation_code");
                codes.add("• " + date + " at " + time + " — Code: " + code);
            }

            if (codes.isEmpty()) return;

            EmailService.LostCodeEmail(email, codes);
        }
    }

    private List<String> suggestTimes(Connection conn, LocalDate d, LocalTime t, int p) throws SQLException {
        List<String> out = new ArrayList<>();

        LocalTime OPEN = LocalTime.of(17, 0);
        LocalTime CLOSE = LocalTime.of(21, 30);

        int[] around = {-90, -60, -30, 30, 60, 90, 120};

        for (int mins : around) {
            LocalTime c = t.plusMinutes(mins);

            if (c.isBefore(OPEN) || c.isAfter(CLOSE)) continue;

            if (validateReservationWindow(d, c) == null &&
                    countOverlapping(conn, d, c, p) < countExactFitTables(conn, p)) {

                out.add(c.toString());
                if (out.size() == 3) return out;
            }
        }

        for (LocalTime c = OPEN; !c.isAfter(CLOSE); c = c.plusMinutes(30)) {

            if (validateReservationWindow(d, c) != null) continue;

            if (countOverlapping(conn, d, c, p) < countExactFitTables(conn, p)) {
                String s = c.toString();
                if (!out.contains(s)) out.add(s);
                if (out.size() == 3) break;
            }
        }

        return out;
    }
    

    // =====================================================
    // SERVER LIFECYCLE
    // =====================================================
    @Override
    protected void serverStarted() {
        System.out.println("[Server] Listening on port " + getPort());

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                runTriggers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.MINUTES);
    }

    //=====================================================
    // SERVER TRIGGERS
    //=====================================================
    private void runTriggers() throws SQLException {

        try (Connection conn = MySQLConnectionPool.getInstance().getConnection()) {

            // BOOKED → NO SHOW
            conn.prepareStatement(
                    "UPDATE `order` " +
                            "SET order_status='CANCELLED_NO_SHOW', status_datetime=NOW() " +
                            "WHERE order_status='BOOKED' " +
                            "AND TIMESTAMP(order_date, order_time) <= NOW() - INTERVAL 15 MINUTE"
            ).executeUpdate();

            // ✅ WAITING_CALLED → NO SHOW (15 minutes after being called)
            conn.prepareStatement(
                    "UPDATE `order` " +
                            "SET order_status='CANCELLED_NO_SHOW', status_datetime=NOW(), table_num=NULL " +
                            "WHERE order_status='WAITING_CALLED' " +
                            "AND status_datetime <= NOW() - INTERVAL 15 MINUTE"
            ).executeUpdate();

            // PAYED → COMPLETED
            conn.prepareStatement(
                    "UPDATE `order` " +
                            "SET order_status='COMPLETED' " +
                            "WHERE order_status='CONFIRMED' " +
                            "AND status_datetime <= NOW() - INTERVAL 2 HOUR"
            ).executeUpdate();

            // ✅ WAITING → WAITING_CALLED (queue, oldest first)
            callNextWaitingIfPossible(conn);

            // BILL (print only)
            String billSql =
                    "SELECT order_number " +
                            "FROM `order` " +
                            "WHERE order_status='SEATED' " +
                            "AND status_datetime <= NOW() - INTERVAL 2 HOUR";

            try (PreparedStatement ps = conn.prepareStatement(billSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int orderNumber = rs.getInt("order_number");
                    System.out.println("[BILL SENT] Order #" + orderNumber);
                }
            }
        }
    }

    // ✅ oldest waiting first, but now we move to WAITING_CALLED (not seated)
    private void callNextWaitingIfPossible(Connection conn) throws SQLException {

        String waitingSql =
                "SELECT confirmation_code, number_of_guests, customer_email " +
                        "FROM `order` " +
                        "WHERE order_status='WAITING' AND order_date=CURDATE() " +
                        "ORDER BY status_datetime ASC";

        try (PreparedStatement ps = conn.prepareStatement(waitingSql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int code = rs.getInt(1);
                int guests = rs.getInt(2);
                String email = rs.getString(3);

                int places = requiredPlacesForGuests(guests);

                Integer table = findFreePhysicalTableNow(conn, places);
                if (table == null) return; // no table right now

                // move WAITING -> WAITING_CALLED and assign table
                try (PreparedStatement ups = conn.prepareStatement(
                        "UPDATE `order` SET order_status='WAITING_CALLED', table_num=?, status_datetime=NOW() " +
                                "WHERE confirmation_code=? AND order_status='WAITING'")) {
                    ups.setInt(1, table);
                    ups.setInt(2, code);
                    int updated = ups.executeUpdate();

                    if (updated > 0) {
                        if (email != null && !email.isBlank()) {
                            int finalTable = table;
                            EMAIL_EXECUTOR.submit(() -> {
                                try {
                                    List<String> lines = List.of(
                                            "📣 Your table is ready!",
                                            "Confirmation Code: " + code,
                                            "Table Number: " + finalTable,
                                            "⚠ Please arrive within 15 minutes (Terminal Check-In)."
                                    );
                                    EmailService.LostCodeEmail(email, lines);
                                } catch (Exception ignored) {}
                            });
                        }

                        // stop after calling ONE (so queue is fair)
                        return;
                    }
                }
            }
        }
    }

    private Integer findFreePhysicalTableNow(Connection conn, int places) throws SQLException {

        String tableSql =
                "SELECT t.table_num " +
                        "FROM tables t " +
                        "WHERE t.places=? " +
                        "AND NOT EXISTS ( " +
                        "   SELECT 1 FROM `order` o " +
                        "   WHERE o.table_num = t.table_num " +
                        "   AND o.table_num IS NOT NULL " +
                        "   AND o.order_date = CURDATE() " +
                        "   AND o.order_status = 'SEATED' " +
                        "   AND TIME(NOW()) < ADDTIME(o.order_time,'02:00:00') " +
                        ") " +
                        "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(tableSql)) {
            ps.setInt(1, places);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return null;
        }
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        clientIPs.put(client, client.getInetAddress().getHostAddress());
        System.out.println("[Client] Connected: " + client.getInetAddress());
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        clientIPs.remove(client);
        System.out.println("[Client] Disconnected: " + client.getInetAddress());
    }
}
