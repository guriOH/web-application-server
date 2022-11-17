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
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    public static final String WEBAPP = "./webapp";

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

            Map<String, String> cookies = HttpRequestUtils.parseCookies(
                    HttpRequestUtils.parseHeader(
                        HttpRequestUtils.getSpecificLines(contents,"Cookie:")
                    ).getValue()
            );

            byte[] bytes = new byte[0];

            if(requestUrl.contains(".html")){
                bytes = Files.readAllBytes(Paths.get(WEBAPP,requestUrl));
                HttpResponseUtils.response200Header(dos, bytes.length);
            }else{
                if(requestUrl.equals("/user/create")){
                    String body = getRequestBody(rd, HttpRequestUtils.getSpecificLines(contents,"Content-Length:"));
                    Map<String, String> paramMap = HttpRequestUtils.parseValues(body, "&");
                    // User create
                    User user = makeUser(paramMap);
                    // Insert new user
                    DataBase.addUser(user);
                    HttpResponseUtils.response302Header(dos,"/index.html");
                }else if(requestUrl.equals("/user/login")){
                    String body = getRequestBody(rd, HttpRequestUtils.getSpecificLines(contents,"Content-Length:"));
                    Map<String, String> paramMap = HttpRequestUtils.parseValues(body, "&");

                    User user = DataBase.findUserById(paramMap.get("userId"));
                    if(Objects.isNull(user)){
                        HttpResponseUtils.responseLogin(dos, "false","/user/login_failed.html");
                    }else{
                        HttpResponseUtils.responseLogin(dos,"true","/index.html");
                    }
                }
            }
            HttpResponseUtils.responseBody(dos, bytes);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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
