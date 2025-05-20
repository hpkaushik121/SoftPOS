package com.aicortex.softpos.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aicortex.softpos.R
import com.aicortex.softpos.utils.Constants
import com.aicortex.softpos.utils.DialogUtils
import com.aicortex.softpos.utils.Logger

open class BaseActivity : AppCompatActivity() {

    fun showProgressDialog(msg:String){
        DialogUtils.showDialog(this,msg)
    }
    fun hideProgressDialog(){
        DialogUtils.hideDialog()
    }
    fun logData(data:String) {
        Log.d(Constants.LOG_D_KEY,""+data)
    }
}