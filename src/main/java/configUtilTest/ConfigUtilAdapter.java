package configUtilTest;

public class ConfigUtilAdapter {

    public static boolean getBoolean(String key) {
        return true;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return true;
    }

    public static int getInt(String key) {
        return 0;
    }

    public static int getInt(String key, int defaultValue) {
        return 0;
    }

    public static long getLong(String key) {
        return 0;
    }

    public static long getLong(String key, long defaultValue) {
        return 0;
    }

    public static String getString(String key) {
        return "";
    }

    public static String getString(String key, String defaultValue) {
        return "";
    }

    public static String[] getStringArray(String key) {
        return new String[] {};
    }
}
