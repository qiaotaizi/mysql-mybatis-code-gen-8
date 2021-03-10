package com.jaiz.web.gen.utils;

public class NameUtil {

    /**
     * 下划线命名转小驼峰
     * @param dashName
     * @return
     */
    public static String dashName2Camel(String dashName){
        String lowerColumnName = dashName.toLowerCase();
        String[] propertyNameParts = lowerColumnName.split("_");
        if (propertyNameParts.length == 1) {
            return propertyNameParts[0];
        }
        StringBuilder sb = new StringBuilder(propertyNameParts[0]);
        for (int i = 1; i < propertyNameParts.length; i++) {
            String word = propertyNameParts[i];
            char firstCharUpper = Character.toUpperCase(word.charAt(0));
            sb.append(firstCharUpper);
            if (word.length() > 1) {
                sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * 下划线命名转大驼峰
     * @param dashName
     * @return
     */
    public static String dashName2BigCamel(String dashName){
        String camel=dashName2Camel(dashName);
        return Character.toUpperCase(camel.charAt(0))+camel.substring(1);
    }

}
