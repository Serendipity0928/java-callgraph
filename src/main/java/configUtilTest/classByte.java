package configUtilTest;

public class classByte {

    private static final String key = "this_is_a_special_key";
    private static String defaultStr = "sdasdas";

    public boolean getMCC4Boolean() {
        return ConfigUtilAdapter.getBoolean(key);
    }

    public boolean getMCC4BooleanV2(String key) {
        return ConfigUtilAdapter.getBoolean(key, true);
    }

    public boolean getMCC4BooleanV3() {
        return getMCC4BooleanV2(key);
    }

    public int getMCC4Int() {
        return ConfigUtilAdapter.getInt("sdasdaddsadsadasdasas", 15000);
    }


}
