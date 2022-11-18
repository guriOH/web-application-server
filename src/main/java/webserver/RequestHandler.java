package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.HttpResponseUtils;
import util.HttpTemplate;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String WEBAPP = "./webapp";
    private static final String USER_LOGIN_FAILED_HTML = "/user/login_failed.html";
    private static final String INDEX_HTML = "/index.html";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader rd = new BufferedReader(reader);
            DataOutputStream dos = new DataOutputStream(out);

            String contents = readContents(rd);
            String requestUrl = HttpRequestUtils.parseUrl(contents);

            Map<String, String> cookies = getCookies(contents);

            byte[] bytes = new byte[0];

            if(requestUrl.contains(".html")){
                bytes = Files.readAllBytes(Paths.get(WEBAPP,requestUrl));
                HttpResponseUtils.response200Header(dos,"text/html", bytes.length);
            }else if(requestUrl.contains(".css")){
                bytes = Files.readAllBytes(Paths.get(WEBAPP,requestUrl));
                HttpResponseUtils.response200Header(dos,"text/css", bytes.length);
            }else{
                if(requestUrl.equals("/user/create")){
                    String body = getRequestBody(rd, HttpRequestUtils.getSpecificLines(contents,"Content-Length:"));
                    Map<String, String> paramMap = HttpRequestUtils.parseValues(body, "&");
                    // User create
                    User user = makeUser(paramMap);
                    // Insert new user
                    DataBase.addUser(user);
                    HttpResponseUtils.response302Header(dos,INDEX_HTML);
                }else if(requestUrl.equals("/user/login")){
                    String body = getRequestBody(rd, HttpRequestUtils.getSpecificLines(contents,"Content-Length:"));
                    Map<String, String> paramMap = HttpRequestUtils.parseValues(body, "&");

                    User user = DataBase.findUserById(paramMap.get("userId"));
                    if(Objects.isNull(user)){
                        HttpResponseUtils.responseLogin(dos, false, USER_LOGIN_FAILED_HTML);
                    }else{
                        HttpResponseUtils.responseLogin(dos,true, INDEX_HTML);
                    }
                }else if(requestUrl.equals("/user/list")){
                    if(cookies.get("logined").equals("true")){
                        StringBuilder sb = new StringBuilder("<ul>");
                        for(User user : DataBase.findAll()){
                            sb.append("<li>");
                            sb.append(user.getName());
                            sb.append("</li>");
                        }
                        sb.append("</ul>");

                        bytes = HttpTemplate.getTemplate("사용자목록",sb.toString()).getBytes();
                        HttpResponseUtils.response200Header(dos,"text/html", bytes.length);
                    }
                }
            }
            HttpResponseUtils.responseBody(dos, bytes);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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
