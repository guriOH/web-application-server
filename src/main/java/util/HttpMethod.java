package util;

public enum HttpMethod {
    GET, POST, PUT, DELETE;

    public boolean isPost(){
        return this == POST;
    }


}
