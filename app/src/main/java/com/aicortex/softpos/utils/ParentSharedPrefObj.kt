package com.aicortex.softpos.utils



import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils

open class ParentSharedPrefObj {
    companion object {
        var prefs: SharedPreferences? = null
            private set
        private var sPrefsEditor: SharedPreferences.Editor? = null

        fun init(context: Context?) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            sPrefsEditor = prefs?.edit()
        }
        //for clear all pref
        fun clear() {
            sPrefsEditor!!.clear().commit()
        }
        //for clear one key
        fun clearKeyValue(key: String?) {
            sPrefsEditor?.remove(key)
            sPrefsEditor?.apply()
        }
        fun put(key: String?, value: Boolean?): SharedPreferences.Editor? {
            if (value == null) {
                sPrefsEditor!!.remove(key)
            } else {
                sPrefsEditor!!.putBoolean(key, value)
            }
            return sPrefsEditor
        }

        fun put(key: String?, value: Float?): SharedPreferences.Editor? {
            if (value == null) {
                sPrefsEditor!!.remove(key)
            } else {
                sPrefsEditor!!.putFloat(key, value)
            }
            return sPrefsEditor
        }

        fun put(key: String?, value: Int?): SharedPreferences.Editor? {
            if (value == null) {
                sPrefsEditor!!.remove(key)
            } else {
                sPrefsEditor!!.putInt(key, value)
            }
            return sPrefsEditor
        }

        fun put(key: String?, value: Long?): SharedPreferences.Editor? {
            if (value == null) {
                sPrefsEditor!!.remove(key)
            } else {
                sPrefsEditor!!.putLong(key, value)
            }
            return sPrefsEditor
        }

        fun put(key: String?, value: String?): SharedPreferences.Editor? {
            if (TextUtils.isEmpty(value)) {
                sPrefsEditor!!.remove(key)
            } else {
                sPrefsEditor!!.putString(key, value)
            }
            return sPrefsEditor
        }
    }
}