package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;
import webserver.controller.Controller;
import webserver.controller.CreateUserController;
import webserver.controller.ListUserController;
import webserver.controller.LoginController;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String WEBAPP = "./webapp";
    private static final String USER_LOGIN_FAILED_HTML = "/user/login_failed.html";
    private static final String INDEX_HTML = "/index.html";

    private static Map<String, Controller> controllerMap = null;

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {

        this.connection = connectionSocket;

        controllerMap = new HashMap<String, Controller>();
        controllerMap.put("/user/create", new CreateUserController());
        controllerMap.put("/user/list", new ListUserController());
        controllerMap.put("/user/login", new LoginController());
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);


            Controller controller = RequestMapping.getController(request.getPath());

            if(controller==null){
                String path = getDefaultPath(request.getPath());
                response.forward(path);
            }else{
                controller.service(request,response);
            }
//            InputStreamReader reader = new InputStreamReader(in);
//            BufferedReader rd = new BufferedReader(reader);
//            DataOutputStream dos = new DataOutputStream(out);
//
//            String contents = readContents(rd);
//            // 이다음 읽을 bytes는 본문데이터
//            // 공백라인까지 읽음
//            String requestUrl = HttpRequestUtils.parseUrl(contents);

//            Map<String, String> cookies = getCookies(contents);

//
//            if(path.contains(".html")){
//                bytes = Files.readAllBytes(Paths.get(WEBAPP,path));
//                HttpResponseUtils.response200Header(dos,"text/html", bytes.length);
//            }else if(path.contains(".css")){
//                bytes = Files.readAllBytes(Paths.get(WEBAPP,path));
//                HttpResponseUtils.response200Header(dos,"text/css", bytes.length);
//            }else{
//                if(path.equals("/user/create")){
//                    createUser(request,response);
//                    User user = new User(
//                            request.getParameter("userId"),
//                            request.getParameter("password"),
//                            request.getParameter("name"),
//                            request.getParameter("email")
//                    );
//                    log.debug("user: {}", user);
//
//                    DataBase.addUser(user);
//                    response.sendRedirect("/index.html");
//                    String body = getRequestBody(rd, HttpRequestUtils.getSpecificLines(contents,"Content-Length:"));
//                    Map<String, String> paramMap = HttpRequestUtils.parseValues(body, "&");
//                    // User create
//                    User user = makeUser(paramMap);
//                    // Insert new user
//                    DataBase.addUser(user);
//                    HttpResponseUtils.response302Header(dos,INDEX_HTML);
//                }else if(path.equals("/user/login")){
//                    login(request, response);
//                    String body = getRequestBody(rd, HttpRequestUtils.getSpecificLines(contents,"Content-Length:"));
//                    Map<String, String> paramMap = HttpRequestUtils.parseValues(body, "&");
//
//                    User user = DataBase.findUserById(request.getParameter("userId"));
//                    if(user!=null){
//                        if(user.login(request.getParameter("password"))){
//                            response.addHeader("Set-Cookie","logined=true");
//                            response.sendRedirect("/index.html");
//                        }else{
//                            response.sendRedirect("/user/login_failed.html");
//                        }
//                    }else{
//                        response.sendRedirect("/user/login_failed.html");
//                    }
//                    if(Objects.isNull(user)){
//                        HttpResponseUtils.responseLogin(dos, false, USER_LOGIN_FAILED_HTML);
//                    }else{
//                        HttpResponseUtils.responseLogin(dos,true, INDEX_HTML);
//                    }
//                }else if(path.equals("/user/list")){
//                    listUser(request,response);
//                    if(!isLogin(request.getHeader("Cookie"))){
//                        response.sendRedirect("/user/login.html");
//                        return;
//                    }
//
//                    Collection<User> users = DataBase.findAll();
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("<table border='1'>");
//                    for(User user : users){
//                        sb.append("<tr>");
//                        sb.append("<td>"+user.getUserId()+"</td>");
//                        sb.append("<td>"+user.getName()+"</td>");
//                        sb.append("<td>"+user.getEmail()+"</td>");
//                        sb.append("</tr>");
//                    }
//                    response.forwardBody(sb.toString());

//                    if(cookies.get("logined").equals("true")){
//                        StringBuilder sb = new StringBuilder("<ul>");
//                        for(User user : DataBase.findAll()){
//                            sb.append("<li>");
//                            sb.append(user.getName());
//                            sb.append("</li>");
//                        }
//                        sb.append("</ul>");
//
//                        bytes = HttpTemplate.getTemplate("사용자목록",sb.toString()).getBytes();
//                        HttpResponseUtils.response200Header(dos,"text/html", bytes.length);
//                    }
//                }else{
//                    response.forward(path);
//                }
//            }
//            HttpResponseUtils.responseBody(dos, bytes);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void listUser(HttpRequest request, HttpResponse response) {
    }

    private void login(HttpRequest request, HttpResponse response) {
    }

    private void createUser(HttpRequest request, HttpResponse response) {
    }

    private boolean isLogin(String cookieValue) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieValue);
        String value = cookies.get("logined");
        if (value == null){
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private String getDefaultPath(String path) {
        if(path.equals("/")){
            return "/index.html";
        }
        return path;
    }

    private Map<String, String> getCookies(String contents) {
        return HttpRequestUtils.parseCookies(
                HttpRequestUtils.parseHeader(
                        HttpRequestUtils.getSpecificLines(contents, "Cookie:")
                ).getValue()
        );
    }

    private String getRequestBody(BufferedReader rd, String line) throws IOException {
        return IOUtils.readData(rd,
                Integer.parseInt(HttpRequestUtils.parseHeader(line).getValue()));
    }

    private User makeUser(Map<String, String> paramMap) {
        return new User(
                paramMap.get("userId"),
                paramMap.get("password"),
                paramMap.get("name"),
                paramMap.get("email")
        );
    }

    private String readContents(BufferedReader rd) throws IOException {
        String line = rd.readLine();
        String res = "";
        while(!"".equals(line)){
            res = res + line + "\n";
            line = rd.readLine();

            if(line == null) break;
        }

        return res;
    }
}
