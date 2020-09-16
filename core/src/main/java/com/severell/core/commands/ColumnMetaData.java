package com.severell.core.commands;

public class ColumnMetaData {

    private String columnName;
    private String setterName;
    private String variableName;
    private int dataType;
    private ColumnType type;
    private boolean isPrimaryKey;

    public String getColumnName() {
        return columnName;
    }

    public String getVariableName() {
        return variableName;
    }
    public String getSetterName() {
        return setterName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
        this.setterName = convertCase(columnName, true);
        this.variableName = convertCase(columnName, false);
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
        this.type = ColumnType.valueOf(dataType);
    }

    public ColumnType getType() {
        return this.type;
    }

    private String convertCase(String in, boolean upperFirstLetter) {
        String temp = null;
        if(in != null) {
            String[] parts = in.split("_");
            temp = joinParts(parts);
            char[] chars = temp.toCharArray();
            if(upperFirstLetter) {
                chars[0] = Character.toUpperCase(chars[0]);
            } else {
                chars[0] = Character.toLowerCase(chars[0]);
            }
            temp = new String(chars);
        }
        return temp;
    }

    private String joinParts(String[] parts) {
        String temp = parts[0];
        for(int i = 1; i< parts.length; i++) {
            char[] chars = parts[i].toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            temp += new String(chars);
        }

        return temp;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }
}
