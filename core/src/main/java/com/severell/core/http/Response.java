package com.severell.core.http;

import com.severell.core.exceptions.ViewException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public interface Response {

    Responsable renderTemplateLiteral(String templateLiteral, String templateName, HashMap<String, Object> here) throws IOException;

    Responsable render(String template, HashMap<String, Object> data) throws IOException, ViewException;

    Responsable render(String template, HashMap<String, Object> data, String baseDir) throws ViewException, IOException;

    Responsable json(Object object) throws IOException;

    Responsable json(Object object, int statusCode) throws IOException;

    Responsable download(File file, String mimeType) throws IOException;

    Responsable download(File file, String mimeType, String name) throws IOException;


    void close() throws IOException;

    void redirect(String s);

    void headers(HashMap<String, String> headers);

    void header(String headerName, String headerValue);

    void share(String key, Object obj);
}
