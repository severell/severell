package com.severell.core.view;

import com.severell.core.exceptions.ViewException;

import java.io.Writer;

public interface View {

    void render(String template, Object object, Writer writer) throws ViewException;

    void render(String template, Object object, String baseDir, Writer writer) throws ViewException;

}
