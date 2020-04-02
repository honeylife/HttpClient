package gov.pianzong.httpclientproject.network;


import org.apache.http.HttpEntity;

public class RequestURL {
    private String encode;

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public HttpEntity getEntiry() {
        return entiry;
    }

    public void setEntiry(HttpEntity entiry) {
        this.entiry = entiry;
    }

    private HttpEntity entiry;
    public HttpEntity postEntiry(){
        return getEntiry();
    }

}
