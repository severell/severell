package com.severell.core.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateTest {

    @Test
    public void testTemplate() {
        Template template = Template.fromPath("/templates/test.mustache");
        assertEquals("<html>test</html>", template.getTemplateLiteral());
    }
}
