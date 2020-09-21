package com.severell.core.view;

import com.severell.core.exceptions.ViewException;

import java.io.Writer;

public interface View {

    /**
     * Render a template. This will use the underlying ViewManager and the
     * chosen ViewDriver.
     * @param template Path to template file
     * @param object Data to pass to template. Relative to templates directory
     * @param writer Writer to write the output. I.E. {@link java.io.PrintWriter} from Response
     * @throws ViewException
     */
    void render(String template, Object object, Writer writer) throws ViewException;

    /**
     * Render a template. This will use the underlying ViewManager and the
     * chosen ViewDriver.
     * @param template Path to template file. Relative to baseDir
     * @param object Data to pass to template. Relative to templates directory
     * @param baseDir Path to directory
     * @param writer Writer to write the output. I.E. {@link java.io.PrintWriter} from Response
     * @throws ViewException
     */
    void render(String template, Object object, String baseDir, Writer writer) throws ViewException;

}
