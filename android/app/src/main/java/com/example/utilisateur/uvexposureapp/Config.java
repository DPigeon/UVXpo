package com.example.coenelec390_uv_exposure.Database;

public class Config {
    public static final String DATABASE_NAME = "uv_exposure_db";               /** Database name */
    public static final int DATABASE_VERSION = 1;                            /** Database version */

    public static final String USER_TABLE_NAME = "user_info";                        /** User Info Table */
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_SKIN_TYPE = "skin_type";
    public static final String COLUMN_NOTIF = "notifications";

    public static final String UV_TABLE_NAME = "uv_data";                        /** UV Data Table */
    public static final String COLUMN_UV_ID = "uv_id";
    public static final String COLUMN_UV_USER_ID = "uv_user_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_UV_VALUE = "uv_value";
}
