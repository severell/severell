package com.severell.core.http;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class Responsable {

    private ByteArrayOutputStream stream;
    private PrintWriter write;
    private int statusCode;
    private String contentType;

    public Responsable(Object obj, int statusCode) {
        stream = new ByteArrayOutputStream();
        this.statusCode = statusCode;
    }

    public Responsable(int statusCode) {
        this.statusCode = statusCode;
        stream = new ByteArrayOutputStream();
    }

    public PrintWriter getWriter() {
        if(write == null) {
            this.write = new PrintWriter(stream);
        }
        return write;
    }

    public ByteArrayOutputStream getStream() {
        return this.stream;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
