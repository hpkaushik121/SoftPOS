package com.nownow.softpos.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nownow.softpos.R
import com.nownow.softpos.R.string
import com.nownow.softpos.activities.DashboardActivity
import com.nownow.softpos.activities.SuccessActivity
import com.nownow.softpos.models.dashboard.TransactionInfo
import com.nownow.softpos.utils.Constants
import com.nownow.softpos.utils.DialogUtils
import com.nownow.softpos.utils.UtilHandler

class TransactionHistoryAdapter(val context: Context, var list: List<TransactionInfo>?) :
    RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {
    fun notifyAdapter(arrayList: List<TransactionInfo>?) {
        this.list = arrayList
        notifyDataSetChanged()
    }

    var str: String = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_history_child_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (list != null) {
            val senderName = list!![position].senderName
            if (!senderName.isNullOrEmpty()){
                if(senderName.length>15) {
                    holder.senderName.text = senderName.substring(0,14)+"..."
                    holder.textSeparator.visibility = View.VISIBLE
                }
                else
                {
                    holder.senderName.text = senderName
                    holder.textSeparator.visibility = View.VISIBLE
                }
            }else{
                holder.senderName.text =""
                holder.textSeparator.visibility=View.GONE
            }
            /*For date and date format*/
            val txDate = list!![position].transactionDate
            if (txDate.isNotEmpty()) {
                holder.textTransactionDate.text = UtilHandler.changeDateFormat(txDate,Constants.FROM_TX_HISTORY)

            } else {
                holder.textTransactionDate.text = UtilHandler.changeDateFormat("",Constants.FROM_TX_HISTORY)
            }
            val txAmount = list!![position].transactionAmount
            if (txAmount.isNotEmpty()){
                holder.amountText.text = context.getString(string.naira_icon) + txAmount
            }

            holder.textInfo.text = list!![position].transactionInfo

            when (list!![position].transactionStatus) {
                "SUCCESS" -> {
                    holder.textStatus.text = "Completed"
                    holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.green_2))
                    holder.textStatus.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.tx_history_inner_design_light_green
                    )
                    holder.textStatus.setPadding(8, 0, 8, 0)
                    /*For adjusting sender name and service name spaces we use tx status*/
                    alterServiceName(position)
                    if (list!![position].serviceName.length > 13 ) {
                        val truncatedText = list!![position].serviceName.substring(0, 13)
                        holder.textServiceName.text = truncatedText
                    } else {
                        holder.textServiceName.text = list!![position].serviceName
                    }
                }
                "FAILED" -> {
                    holder.textStatus.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.tx_history_inner_design_for_failed
                    )
                    holder.textStatus.text = "Failed"
                    holder.textStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.nfc_not_text_color
                        )
                    )
                    holder.textStatus.setPadding(8, 0, 8, 0)
                    /*For adjusting sender name and service name spaces we use tx status*/
                    alterServiceName(position)
                    //holder.textServiceName.text = list!![position].serviceName

                    if (list!![position].serviceName.length > 13 ) {
                        val truncatedText = list!![position].serviceName.substring(0, 13)
                        holder.textServiceName.text = truncatedText
                    } else {
                        holder.textServiceName.text = list!![position].serviceName
                    }

                }
                else -> {
                    holder.textStatus.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.tx_history_inner_design_for_pending
                    )
                    holder.textStatus.text = "Pending"
                    holder.textStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.pending_text_color
                        )
                    )
                    holder.textStatus.setPadding(8, 0, 8, 0)
                    /*For adjusting sender name and service name spaces we use tx status*/
                    alterServiceName(position)

                    if (list!![position].serviceName.length > 13 ) {
                        val truncatedText = list!![position].serviceName.substring(0, 13)
                        holder.textServiceName.text = truncatedText
                    } else {
                        holder.textServiceName.text = list!![position].serviceName
                    }
                }
            }
            /*when (position) {
                2 -> {
                    holder.textStatus.background = ContextCompat.getDrawable(context, R.drawable.tx_history_inner_design_for_pending)
                    holder.textStatus.text = list!![position].serviceName
                    holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.pending_text_color))
                    holder.textStatus.setPadding(8, 0, 8, 0)
                }
                3 -> {
                    holder.textStatus.background = ContextCompat.getDrawable(context, R.drawable.tx_history_inner_design_for_failed)
                    holder.textStatus.text = "Failed"
                    holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.nfc_not_text_color))
                    holder.textStatus.setPadding(8, 0, 8, 0)
                }
                else -> {
                    holder.textStatus.background = ContextCompat.getDrawable(context, R.drawable.tx_history_inner_design_light_green)
                    holder.textStatus.setPadding(8, 0, 8, 0)
                }
            }*/
            //holder.usernameText.text = list[0].toString()
            holder.mainLayoutTxHistort.setOnClickListener(View.OnClickListener {
                val clickedData = list!![position]

                val bundle = Bundle()
                bundle.putParcelable("data", clickedData)

                val intent = Intent(context, SuccessActivity::class.java)
                intent.putExtras(bundle)
                intent.putExtra(Constants.REQUEST_FROM,Constants.FROM_TX_HISTORY)
                intent.putExtra(Constants.TRANSACTIONID,clickedData.transactionId)
                intent.putExtra(Constants.SERVICE_NAME,clickedData.serviceName)
                context.startActivity(intent)
               //DialogUtils.showCommonDialog(0,true,context,"getString(R.string.biometric_setup_successful)",
               //    View.OnClickListener { })
            })
        }
    }

    private fun alterServiceName(position: Int) {
        if (list!![position].serviceName == Constants.SERVICE_NAME_REPLACE) {
            list!![position].serviceName = "PWT"
        }
    }

    override fun getItemCount(): Int {
        return list?.size!!
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val senderName: TextView = itemView.findViewById(R.id.text_tx_username)
        val textSeparator: TextView = itemView.findViewById(R.id.textSeparator)
        val amountText: TextView = itemView.findViewById(R.id.text_tx_amount)
        val textStatus: TextView = itemView.findViewById(R.id.text_tx_status)
        val textInfo: TextView = itemView.findViewById(R.id.textTransactionInfo)
        val textServiceName: TextView = itemView.findViewById(R.id.textServiceName)
        val textTransactionDate: TextView = itemView.findViewById(R.id.textTransactionDate)
        val mainLayoutTxHistort: ConstraintLayout = itemView.findViewById(R.id.mainLayoutTxHistort)
    }
}