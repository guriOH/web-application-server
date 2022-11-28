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
//        InputStreamReader reader = new InputStreamReader(in);
//        BufferedReader rd = new BufferedReader(reader);
//
//        String requestString = this.readContents(rd);
//
//        this.readMethod(requestString);
//        this.readPath(requestString);
//        this.readHeaders(requestString);
//        this.readParams(requestString);

    }

    public HttpMethod getMethod(){
        return requestLine.getMethod();
    }

    public String getPath(){
        return requestLine.getPath();
    }


    private void readHeaders(String requestString) {
        headers = new HashMap<String,String>();
        String[] token = requestString.split("\n");
        for (String line : token) {
            if(line.contains(":")){
                String[] data = line.split(":");

                headers.put(data[0].trim(),data[1].trim());
            }
        }
    }


    private String readContents(BufferedReader rd) throws IOException {
        String line = rd.readLine();
        String requestString = "";
        while(!"".equals(line)){
            requestString = requestString + line + "\n";
            line = rd.readLine();

            if(line == null) break;
        }

        requestString = requestString+ rd.readLine();

        return requestString;
    }

    public String getHeader(String element) {
        return headers.get(element);
    }

    public String getParameter(String userId) {
        return params.get(userId);
    }
}
