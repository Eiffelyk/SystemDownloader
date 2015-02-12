/**
 * @FileName: SharedPreferencesManager.java
 * @Package com.application.base
 * @author 馋猫   Email： eiffelyk@gmail.com
 * @date 2014年9月6日 下午3:47:36  
 */
package com.eiffelyk.www.systemdownloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class SharedPreferencesManager {
    private static final int STRING_TYPE = 1;
    private static final int INT_TYPE = 2;
    private static final int BOOLEAN_TYPE = 3;
    private static final int FLOAT_TYPE = 4;
    private static final int LONG_TYPE = 5;
    private static String USER_TABLE_NAME = "shareprefrences.xml";
    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;


    private SharedPreferencesManager(Context context) {
        initSpManager(context);
    }

    public static SharedPreferencesManager getInstance(Context context) {
        synchronized (SharedPreferencesManager.class) {
            if (instance == null) {
                synchronized (SharedPreferencesManager.class) {
                    if (instance == null) {
                        instance = new SharedPreferencesManager(context);
                    }
                }
            }
        }
        return instance;
    }

    private void initSpManager(Context context) {
        sharedPreferences = context.getSharedPreferences(USER_TABLE_NAME, Context.MODE_PRIVATE);
    }

    public void putExtra(String key, Object content) {
        remove(key);//防止数据类型不一致，安全起见，先remove掉
        Editor editor = sharedPreferences.edit();
        if (content instanceof String) {
            insertConfigure(editor, key, STRING_TYPE, content);
        } else if (content instanceof Integer) {
            insertConfigure(editor, key, INT_TYPE, content);
        } else if (content instanceof Boolean) {
            insertConfigure(editor, key, BOOLEAN_TYPE, content);
        } else if (content instanceof Float) {
            insertConfigure(editor, key, FLOAT_TYPE, content);
        } else if (content instanceof Long) {
            insertConfigure(editor, key, LONG_TYPE, content);
        }
        editor.commit();
    }


    public void putExtras(HashMap<String, Object> map) {
        Editor editor = sharedPreferences.edit();
        for (Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object content = entry.getValue();
            putExtra(key, content);
        }
        editor.commit();
    }


    private void insertConfigure(Editor editor, String key, int type, Object content) {
        String value = String.valueOf(content);
        switch (type) {
            case STRING_TYPE:
                editor.putString(key, value);
                break;
            case BOOLEAN_TYPE:
                editor.putBoolean(key, Boolean.parseBoolean(value));
                break;
            case INT_TYPE:
                editor.putInt(key, Integer.parseInt(value));
                break;
            case FLOAT_TYPE:
                editor.putFloat(key, Float.parseFloat(value));
                break;
            case LONG_TYPE:
                editor.putLong(key, Long.parseLong(value));
                break;
        }
    }


    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }


    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }


    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public Float getFloat(String key) {
        return sharedPreferences.getFloat(key, 0);
    }

    public Long getLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }


    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }


    public String getString(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    public Float getFloat(String key, Float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    public Long getLong(String key, Long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }


    public Map<String, ?> getAllSharedPreferences() {
        return sharedPreferences.getAll();
    }


    public void clearAllSharedPreferences() {

        Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }


    public void remove(String key) {
        Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }
}
