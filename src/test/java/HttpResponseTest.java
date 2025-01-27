import org.junit.Test;
import util.HttpRequest;
import util.HttpResponse;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class HttpResponseTest {

    private String testDirectory = "./src/test/resources/";

    @Test
    public void responseForward() throws Exception {
       HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt"));
       response.forward("/index.html");
    }

    @Test
    public void responseRedirect() throws Exception {
        HttpResponse response = new HttpResponse(createOutputStream("Http_Redirect.txt"));
        response.sendRedirect("/index.html");
    }

    @Test
    public void responseCookies() throws Exception {
        HttpResponse response = new HttpResponse(createOutputStream("Http_Cookies.txt"));
        response.addHeader("Set-Cookie", "logined=true");
        response.sendRedirect("/index.html");
    }


    private OutputStream createOutputStream(String path) throws Exception {
        return new FileOutputStream(new File(testDirectory, path));
    }
}
