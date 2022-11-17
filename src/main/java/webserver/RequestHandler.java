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
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

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
            System.out.println(contents);
            String requestUrl = HttpRequestUtils.getUrl(contents);

            Map<String, String> cookies = getCookies(contents);
            byte[] bytes = new byte[0];
            Map<String, String> paramMap = null;
            if(requestUrl.equals("/user/create")){
                String body = getRequestBody(rd, contents);
                paramMap = HttpRequestUtils.parseValues(body, "&");

                // User create
                User user = makeUser(paramMap);

                // Insert new user
                DataBase.addUser(user);

                // redirect to /index.html
                bytes = Files.readAllBytes(Paths.get("./webapp","/index.html"));

                // HTTP redirect
                //301,302,303, 307 차이
                // redirect, rewrite 차이
                response302Header(dos, bytes.length,"/index.html");
            }else if(requestUrl.equals("/user/login")){
                String body = getRequestBody(rd, contents);
                paramMap = HttpRequestUtils.parseValues(body, "&");

                User user = DataBase.findUserById(paramMap.get("userId"));
                if(Objects.isNull(user)){
                    responseLogin(dos, bytes.length,"false","/user/login_failed.html");
                }else{
                    responseLogin(dos, bytes.length,"true","/index.html");
                }
            }else {
                bytes = Files.readAllBytes(Paths.get("./webapp",requestUrl));
                response200Header(dos, bytes.length);
            }

            responseBody(dos, bytes);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean runLogin() {
        return false;
    }

    private String getRequestBody(BufferedReader rd, String contents) throws IOException {
        String body=null;
        for (String line : contents.split("\n")) {
            if(line.contains("Content-Length:")){
                body = IOUtils.readData(rd,
                        Integer.parseInt(HttpRequestUtils.parseHeader(line).getValue()));
            }

        }
        return body;
    }

    private Map<String, String> getCookies( String contents) throws IOException {
        Map<String, String> cookies = new HashMap<String, String>();
        for (String line : contents.split("\n")) {
            if(line.contains("Cookie:")){
                String[] tokens = line.split(":")[1].split(";");
                for (String token : tokens){
                    String[] split= token.split("=");
                    cookies.put(split[0].trim(),split[1].trim());
                }
            }

        }
        return cookies;
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

    private void responseLogin(DataOutputStream dos, int lengthOfBodyContent, String result, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Location: "+url+"\r\n");
            dos.writeBytes("Set-Cookie: logined="+result+";"+"Path=/\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Location: "+redirectUrl);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
