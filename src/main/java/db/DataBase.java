package db;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import model.User;
import util.HttpSession;

public class DataBase {
    private static Map<String, User> users = Maps.newHashMap();
    private static Map<String, HttpSession> sessions = Maps.newHashMap();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static void addSession(HttpSession session) {
        sessions.put(session.getId(), session);
    }

    public static HttpSession findSessionById(String id){ return sessions.get(id); }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
