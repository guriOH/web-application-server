package webserver.controller;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;
import util.HttpSession;

import java.util.Collection;
import java.util.Map;

public class ListUserController implements Controller{
    @Override
    public void service(HttpRequest request, HttpResponse response) {
//        if(!isLogin(request.getHeader("Cookie"))){
//            response.sendRedirect("/user/login.html");
//            return;
//        }
        if(!isLogin(request.getSession())){
            response.sendRedirect("/user/login.html");
            return;
        }

        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        for(User user : users){
            sb.append("<tr>");
            sb.append("<td>"+user.getUserId()+"</td>");
            sb.append("<td>"+user.getName()+"</td>");
            sb.append("<td>"+user.getEmail()+"</td>");
            sb.append("</tr>");
        }
        response.forwardBody(sb.toString());
    }

//    private boolean isLogin(String cookieValue) {
//        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieValue);
//        String value = cookies.get("logined");
//        if (value == null){
//            return false;
//        }
//        return Boolean.parseBoolean(value);
//    }
    private boolean isLogin(HttpSession session) {
        Object user = session.getAttributes("user");
        if (user == null) {
            return false;
        }
        return true;
    }
}