package com.nownow.softpos.utils

import android.app.ProgressDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.nownow.softpos.R


class DialogUtils {
    fun myfun(context: Context) {
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        val color = Color.parseColor("#000000")
        progressBar.progressTintList = ColorStateList.valueOf(color)
    }

    //private lateinit var progressBar: ProgressBar

    companion object {
        private var progressBar: ProgressBar? = null
        private lateinit var progressDialog: ProgressDialog

        fun showDialog(context: Context, message: String?) {
            try {
                progressDialog = ProgressDialog(context)
                progressDialog.setMessage(message)
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                progressDialog.show()
                progressDialog.setCancelable(false)
            } catch (e: Exception) {
                e.printStackTrace();
            }
        }

        fun hideDialog() {
            try {
                if (progressDialog != null && progressDialog.isShowing) {
                    progressDialog.dismiss()
                    progressDialog.hide()
                }
            } catch (e: Exception) {
                e.printStackTrace();
            }

        }

        /*fun showHideProgressBar(context: Context, hideOrShow: String) {
            try {
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.custom_progressbar)

                progressBar = dialog.findViewById<ProgressBar>(R.id.progress_bar)
                val mainLayout = dialog.findViewById<RelativeLayout>(R.id.mainLayout)
                val color = Color.parseColor("#000000")
                progressBar?.indeterminateTintList = ColorStateList.valueOf(color)
                if (hideOrShow.equals("1")) {
                    dialog.setCancelable(false)
                    dialog.show()
                } else {
                    if (progressBar?.visibility == View.VISIBLE) {
                        *//*  if (progressBar != null) {
                              progressBar.post {
                                  progressBar.visibility = View.GONE
                              }
                          }*//*
                        mainLayout.visibility = GONE
                        //progressBar.visibility = View.GONE // dismisses the progressBar
                        //progressBar.visibility = View.INVISIBLE
                        //dialog.setCancelable(false)
                        dialog.dismiss()
                        dialog.hide()

                        Toast.makeText(context, " running detect", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Not running detect", Toast.LENGTH_LONG).show()
                    }

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }*/

        /* fun hideProgressBar(context: Context) {
             progressBar?.visibility = GONE
             try {
                 if (progressBar != null && progressBar!!.isShown) {
                     val dialog = Dialog(context)
                     dialog.setContentView(R.layout.custom_progressbar)

                     val progressBar = dialog.findViewById<ProgressBar>(R.id.progress_bar)
                     val color = Color.parseColor("#000000")
                     progressBar.indeterminateTintList = ColorStateList.valueOf(color)

                     dialog.setCancelable(true)
                     dialog.dismiss()
                 }
             } catch (e: Exception) {
                 e.printStackTrace();
             }
             *//*try {
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.custom_progressbar)

                val progressBar = dialog.findViewById<ProgressBar>(R.id.progress_bar)
                val color = Color.parseColor("#000000")
                progressBar.indeterminateTintList = ColorStateList.valueOf(color)

                dialog.setCancelable(true)
                dialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }*//*
        }

        fun hideBar(context: Context) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_progressbar)
            val progressBar = dialog.findViewById<ProgressBar>(R.id.progress_bar)
            val mainLayout = dialog.findViewById<RelativeLayout>(R.id.mainLayout)
            if (progressBar.visibility == View.VISIBLE) {
                mainLayout.visibility = GONE
                progressBar.visibility = View.GONE // dismisses the progressBar
                Toast.makeText(context, " running detect", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Not running detect", Toast.LENGTH_LONG).show()
            }
        }*/


        fun showCommonDialog(
            btnCount: Int,
            isCancelable:Boolean,
            context: Context, message: String?,
            onClickListener: View.OnClickListener?
        ) {
            val builder = AlertDialog.Builder(context, R.style.cardRemoveDialog)
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(
                R.layout.dialog_box_layout,
                null
            )
            val tvMessage = view.findViewById<TextView>(R.id.tv_message)
            tvMessage.text = message
            builder.setView(view)
            val dialog = builder.create()
            val cancel = view.findViewById<AppCompatButton>(R.id.btn_cancel)
            val ok = view.findViewById<AppCompatButton>(R.id.btn_ok)
            when (btnCount) {
                1 -> {
                    cancel.visibility = View.GONE
                }
                2 -> {
                    ok.visibility = View.VISIBLE
                    cancel.visibility = View.VISIBLE
                }
                else -> {
                    ok.visibility = View.GONE
                    cancel.visibility = View.GONE
                }
            }
            ok.setOnClickListener { view ->
                if (onClickListener != null) {
                    onClickListener.onClick(view)
                    dialog.dismiss()
                }
            }
            cancel.setOnClickListener { view1: View? -> dialog.dismiss() }
            if (isCancelable){ dialog.setCancelable(true)
            }else{dialog.setCancelable(false)}

            dialog.show()
        }

        fun showCommonDialog2(
            btnCount: Int,
            isCancelable:Boolean,
            context: Context, message: String?,
            onClickListenerOK: View.OnClickListener?,
            onClickListenerCancel: View.OnClickListener?
        ) {
            val builder = AlertDialog.Builder(context, R.style.cardRemoveDialog)
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(
                R.layout.dialog_box_layout,
                null
            )
            val tvMessage = view.findViewById<TextView>(R.id.tv_message)
            tvMessage.text = message
            builder.setView(view)
            val dialog = builder.create()
            val cancel = view.findViewById<AppCompatButton>(R.id.btn_cancel)
            val ok = view.findViewById<AppCompatButton>(R.id.btn_ok)
            when (btnCount) {
                1 -> {
                    cancel.visibility = View.GONE
                }
                2 -> {
                    ok.visibility = View.VISIBLE
                    cancel.visibility = View.VISIBLE
                }
                else -> {
                    ok.visibility = View.GONE
                    cancel.visibility = View.GONE
                }
            }
            ok.setOnClickListener { view ->
                if (onClickListenerOK != null) {
                    onClickListenerOK.onClick(view)
                    dialog.dismiss()
                }
            }
            cancel.setOnClickListener { view ->
                if (onClickListenerCancel != null) {
                    onClickListenerCancel.onClick(view)
                    dialog.dismiss()

                }
                else{
                    dialog.dismiss()
                }
            }
            if (isCancelable){ dialog.setCancelable(true)
            }else{dialog.setCancelable(false)}

            dialog.show()
        }

    }


}