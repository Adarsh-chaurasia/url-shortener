package com.urlShortner.urlShortnerProject.Exception;

public class UrlNotFoundException extends  RuntimeException{

    public UrlNotFoundException(String shortCode) {
        super("Short URL not found: " + shortCode);
    }


}
