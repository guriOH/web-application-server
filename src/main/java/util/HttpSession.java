package util;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {

    private String id;

    private Map<String, Object> values = new HashMap<String, Object>();

    public HttpSession(String id) {
        this.id = id;
    }

    public void setAttributes(String name, Object value) {
        values.put(name, value);
    }

    public Object getAttributes(String name){
        return values.get(name);
    }

    public void removeAttributes(String name){
        values.remove(name);
    }

    public void invalidate(){
        HttpSessions.remove(id);
    }

    public String getId() {
        return id;
    }
}
