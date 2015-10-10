package com.imihov.tamssql;

/**
 * Created by imihov on 8/26/15.
 */
public class Variables {

    public static final String DATABASE_NAME = "assets.db";
    public static final int DATABASE_VERSION = 1;

    public static final String _TABLE = "assets";

    public static final String _COLUMN_ASSETID = "assetId";
    public static final String _COLUMN_TIMESTAMP = "last_timestamp";
    public static final String _COLUMN_ASSETNAME = "name";
    public static final String _COLUMN_NEEDSSYNC = "needsSync"; //used in app only
    public static final String _COLUMN_DELETED = "deleted";
    public static final String _COLUMN_ISNEW = "isNew"; //used in app only - keeps track if the asset is brand new - useful for server function call

    public static final String _IPADDRESS = "http://tams.imihov.com";
    public static final String _PUSH_URL = "/api/push.php";
    public static final String _PULL_URL = "/api/pull.php";
    public static final String _GETCOUNT_URL = "/api/getdbrowcount.php";

    public static final String _API_PASSWORD = "1111";
}
