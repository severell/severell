package com.severell.core.error;

class FileSnippet {

    String fileData;
    int lineStart;
    int lineNum;
    String fileName;

    public FileSnippet(JavaFile javaFile, int lineNumber) {
        this.lineNum = lineNumber;
        this.lineStart = lineNumber - 4;
        this.fileData = javaFile.getLines(lineNumber - 5, lineNumber + 6);
    }

}
