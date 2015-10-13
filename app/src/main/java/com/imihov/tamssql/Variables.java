package com.imihov.tamssql;

/**
 * Created by imihov on 8/26/15.
 */
public class Variables {

    // Database
    public static final String DATABASE_NAME = "tams.db";
    public static final int DATABASE_VERSION = 1;

    // Tables
    public static final String _ASSETS_TABLE = "assets";
    public static final String _LOCATIONS_TABLE = "locations";
    public static final String _MEDIA_TABLE = "media";
    public static final String _ATTRIBUTES_TABLE = "attributes";
    public static final String _ATTRIBUTES_INDEXES_TABLE = "attributes_indexes";
    public static final String _ATTRIBUTES_VALUES_TABLE = "attributes_values";
    public static final String _ASSET_TYPES_TABLE = "asset_types_table";

    // Table columns
    //Asset table columns
    public static final String _ASSETS_COLUMN_ASSET_ID = "asset_id";
    public static final String _ASSETS_COLUMN_CREATED_AT = "created_at";
    public static final String _ASSETS_COLUMN_UPDATED_AT = "updated_at";
    public static final String _ASSETS_COLUMN_ASSET_NAME = "name";
    public static final String _ASSETS_COLUMN_NEEDSSYNC = "needsSync"; //used in app only
    public static final String _ASSETS_COLUMN_DELETED = "deleted";
    public static final String _ASSETS_COLUMN_ISNEW = "isNew"; //used in app only - keeps track if the asset is brand new - useful for server function call
    //Locations table columns
    public static final String _LOCATIONS_COLUMN_LOCATION_ID = "location_id";
    public static final String _LOCATIONS_COLUMN_ASSET_ID = "asset_id";
    public static final String _LOCATIONS_COLUMN_LONGITUDE = "longitude";
    public static final String _LOCATIONS_COLUMN_LATITUDE = "latitude";
    //Media table columns
    public static final String _MEDIA_COLUMN_MEDIA_ID = "media_id";
    public static final String _MEDIA_COLUMN_ASSET_ID = "asset_id";
    public static final String _MEDIA_COLUMN_IMAGES = "images";
    public static final String _MEDIA_COLUMN_VOICE_MEMO = "voice_memo";
    //Asset types table columns
    public static final String _ASSET_TYPES_ASSET_TYPE_ID = "asset_type_id";
    public static final String _ASSET_TYPES_TYPE_VALUE = "type_value";
    //Attributes table columns
    public static final String _ATTRIBUTES_ATTRIBUTE_ID = "attribute_id";
    public static final String _ATTRIBUTES_ATTRIBUTE_LABEL = "attribute_label";
    //Attributes indexes columns
    public static final String _ATTRIBUTES_INDEXES_ATTRIBUTE_INDEX_ID = "attribute_index_id";
    public static final String _ATTRIBUTES_INDEXES_ASSET_ID = "asset_id";
    public static final String _ATTRIBUTES_INDEXES_ATTRIBUTE_ID = "attrubute_id";
    public static final String _ATTRIBUTES_INDEXES_ATTRIBUTE_VALUE_ID = "attribute_value_id";
    //Atributes values columns
    public static final String _ATTRIBUTES_VALUES_ATTRIBUTE_VALUE_ID = "attribute_value_id";
    public static final String _ATTRIBUTES_VALUES_ATTRIBUTE_VALUE = "attribute_value";
    public static final String _ATTRIBUTES_VALUES_ATTRIBUTE_ID = "attribute_id";

    // assets table create statement
    public static final String CREATE_TABLE_ASSETS = "CREATE TABLE " +
            Variables._ASSETS_TABLE + " ( " +
            Variables._ASSETS_COLUMN_ASSET_ID + " INTEGER PRIMARY KEY, " +
            Variables._ASSETS_COLUMN_ASSET_NAME + " TEXT, " +
            Variables._ASSETS_COLUMN_CREATED_AT + " INTEGER, " +
            Variables._ASSETS_COLUMN_UPDATED_AT + " INTEGER, " +
            Variables._ASSETS_COLUMN_NEEDSSYNC + " INTEGER, " +
            Variables._ASSETS_COLUMN_DELETED + " INTEGER DEFAULT '0', " +
            Variables._ASSETS_COLUMN_ISNEW + " INTEGER DEFAULT '0');";

    // location table create statement
    public static final String CREATE_TABLE_LOCATIONS = "CREATE TABLE " +
            Variables._LOCATIONS_TABLE + " ( " +
            Variables._LOCATIONS_COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY, " +
            Variables._LOCATIONS_COLUMN_ASSET_ID + " INTEGER, " +
            Variables._LOCATIONS_COLUMN_LATITUDE + " FLOAT, " +
            Variables._LOCATIONS_COLUMN_LONGITUDE + " FLOAT, " +
            "FOREIGN KEY ("+ _LOCATIONS_COLUMN_ASSET_ID +") REFERENCES "+_ASSETS_TABLE+"("+ _ASSETS_COLUMN_ASSET_ID +")" +
            ");";

    // media table create statement
    public static final String CREATE_TABLE_MEDIA = "CREATE TABLE " +
            Variables._MEDIA_TABLE + " ( " +
            Variables._MEDIA_COLUMN_MEDIA_ID + " INTEGER PRIMARY KEY, " +
            Variables._MEDIA_COLUMN_ASSET_ID + " INTEGER, " +
            Variables._MEDIA_COLUMN_IMAGES + " VARCHAR, " +
            Variables._MEDIA_COLUMN_VOICE_MEMO + " VARCHAR, " +
            "FOREIGN KEY ("+ _MEDIA_COLUMN_ASSET_ID +") REFERENCES "+_ASSETS_TABLE+"("+ _ASSETS_COLUMN_ASSET_ID +")" +
            ");";

    // asset types table create statement
    public static final String CREATE_TABLE_ASSET_TYPES = "CREATE TABLE " +
            Variables._ASSET_TYPES_TABLE + " ( " +
            Variables._ASSET_TYPES_ASSET_TYPE_ID + " INTEGER PRIMARY KEY, " +
            Variables._ASSET_TYPES_TYPE_VALUE + " VARCHAR" +
            ");";

    // attributes table create statement
    public static final String CREATE_TABLE_ATTRIBUTES = "CREATE TABLE " +
            Variables._ATTRIBUTES_TABLE + " ( " +
            Variables._ATTRIBUTES_ATTRIBUTE_ID + " INTEGER PRIMARY KEY, " +
            Variables._ATTRIBUTES_ATTRIBUTE_LABEL + " VARCHAR" +
            ");";

    // attributes indexes table create statement
    public static final String CREATE_TABLE_ATTRIBUTES_INDEXES = "CREATE TABLE " +
            Variables._ATTRIBUTES_INDEXES_TABLE + " ( " +
            Variables._ATTRIBUTES_INDEXES_ATTRIBUTE_INDEX_ID + " INTEGER PRIMARY KEY, " +
            Variables._ATTRIBUTES_INDEXES_ASSET_ID + " INTEGER, " +
            Variables._ATTRIBUTES_INDEXES_ATTRIBUTE_ID + " INTEGER, " +
            Variables._ATTRIBUTES_INDEXES_ATTRIBUTE_VALUE_ID + " INTEGER, " +
            "FOREIGN KEY ("+ _ATTRIBUTES_INDEXES_ASSET_ID +") REFERENCES "+_ASSETS_TABLE+"("+ _ASSETS_COLUMN_ASSET_ID +"), " +
            "FOREIGN KEY ("+ _ATTRIBUTES_INDEXES_ATTRIBUTE_ID +") REFERENCES "+_ATTRIBUTES_TABLE+"("+_ATTRIBUTES_ATTRIBUTE_ID+"), " +
            "FOREIGN KEY ("+ _ATTRIBUTES_INDEXES_ATTRIBUTE_VALUE_ID +") REFERENCES "+_ATTRIBUTES_VALUES_TABLE+"("+_ATTRIBUTES_VALUES_ATTRIBUTE_VALUE_ID+")" +
            ");";

    // attribute values table create statement
    public static final String CREATE_TABLE_ATTRIBUTES_VALUES = "CREATE TABLE " +
            Variables._ATTRIBUTES_VALUES_TABLE + " ( " +
            Variables._ATTRIBUTES_VALUES_ATTRIBUTE_VALUE_ID + " INTEGER PRIMARY KEY, " +
            Variables._ATTRIBUTES_VALUES_ATTRIBUTE_VALUE + " VARCHAR, " +
            Variables._ATTRIBUTES_VALUES_ATTRIBUTE_ID + " VARCHAR, " +
            "FOREIGN KEY ("+ _ATTRIBUTES_VALUES_ATTRIBUTE_ID +") REFERENCES "+_ATTRIBUTES_TABLE+"("+_ATTRIBUTES_ATTRIBUTE_ID+")" +
            ");";

    // Server settings - store address in prefferences
    public static final String _IPADDRESS = "http://tams.imihov.com";
    public static final String _PUSH_URL = "/api/push.php";
    public static final String _PULL_URL = "/api/pull.php";
    public static final String _API_AUTH_POST = "apiAuth";
    public static final String _ASSETS_JSON_POST = "assetsJSON";

    // API KEY - To be stored in preferences
    public static final String _API_PASSWORD = "1111";
    // created by - to be filled by the user
    public static final String _CREATED_BY = "IVAN";
}