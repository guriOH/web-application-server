package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private DataOutputStream dos = null;
    private Map<String, String> headers = new HashMap<String, String>();

    public HttpResponse(OutputStream outputStream) {
        dos = new DataOutputStream(outputStream);
    }

    public void forward(String url) {
        try {
//            dos.writeBytes("HTTP/1.1 302 OK \r\n");
//            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
//            dos.writeBytes("Location: "+s+"\r\n");
            byte[] body = Files.readAllBytes(new File("./wepapp"+url).toPath());
            if(url.endsWith(".css")){
                headers.put("Content-Type","text/css");
            }else if(url.endsWith(".js")){
                headers.put("Content-Type","application/javascript");
            }else{
                headers.put("Content-Type","text/html;charset=UTF-8");
            }
            headers.put("Content-Length",body.length+"");
            response200Header(body.length);
            responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }


    public void sendRedirect(String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            processHeaders();
            dos.writeBytes("Location: "+redirectUrl+" \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void response200Header(int lengthOfBodyContent) {
        try{
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try{
            dos.write(body,0,body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private void processHeaders() {
        try{
            Set<String> keys = headers.keySet();
            for (String key : keys){
                dos.writeBytes(key+": "+headers.get(key)+ " \r\n");
            }
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    public void addHeader(String key, String value) {
        headers.put(key,value);
    }

    public void forwardBody(String forwardBody) {
        try{
            byte[] body = forwardBody.getBytes();
            headers.put("Content-Type","text/html;charset=UTF-8");
            headers.put("Content-Length",body.length+"");
            response200Header(body.length);
            responseBody(body);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
