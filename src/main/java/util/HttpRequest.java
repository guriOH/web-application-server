package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> params = new HashMap<String, String>();

    private RequestLine requestLine;

    public HttpRequest(InputStream in) throws IOException {
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line= br.readLine();
            if(line==null){
                return;
            }
            
            requestLine = new RequestLine(line);

            line = br.readLine();

            while(!line.equals("")){
                log.debug("header :  {}",line);
                String[] tokens = line.split(":");
                headers.put(tokens[0].trim(), tokens[1].trim());

                line = br.readLine();
            }

            if("POST".equals(getMethod())){
                String body=IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }else{
                params = requestLine.getParams();
            }

        }catch (IOException e)
        {
            log.error(e.getMessage());
        }


    }

    public HttpMethod getMethod(){
        return requestLine.getMethod();
    }

    public String getPath(){
        return requestLine.getPath();
    }

    public HttpCookie getCookie(){
        return new HttpCookie(getHeader("Cookie"));
    }
    public String getHeader(String element) {
        return headers.get(element);
    }

    public String getParameter(String userId) {
        return params.get(userId);
    }


    public HttpSession getSession(){
        return HttpSessions.getSession(getCookie().getCookie("JSESSIONID"));
    }
}
