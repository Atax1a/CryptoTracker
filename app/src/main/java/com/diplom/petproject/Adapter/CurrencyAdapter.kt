package com.diplom.petproject.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diplom.petproject.Constants
import com.diplom.petproject.Model.CurrencyModel
import com.diplom.petproject.R
import com.diplom.petproject.databinding.CurrencyRvItemBinding
import com.diplom.petproject.reformatPercent
import com.diplom.petproject.reformatStringDate
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class CurrencyAdapter(private var currencyList: ArrayList<CurrencyModel>) :
    RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

    var code: Int = 1


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = CurrencyRvItemBinding.bind(view)
        private val decFormatPrice = DecimalFormat("#.##")
        fun bind(currencyModel: CurrencyModel, code: Int) {
            binding.apply {
                tvCurrencyName.text = currencyModel.currencyName
                tvCurrencyCode.text = currencyModel.currencyCode
                tvCurrencyPrice.text = "$ ${decFormatPrice.format(currencyModel.currencyPrice)}"


                if (currencyModel.imageUrl.isNotEmpty()) {
                    Picasso.get().load(currencyModel.imageUrl).into(binding.currencyLogo)
                    binding.logoProgressBar.visibility = View.GONE
                }

                tvLastUpdate.text = "Обновлено: ${reformatStringDate(currencyModel.lastUpdated)}"

                when (code) {
                    Constants.CONSTANT_PERCENT_CODE_HOUR -> {
                        tvCurrencyPercent.text =
                            "${reformatPercent(currencyModel.percentChangeHour)}%"
                        checkTrending(currencyModel.percentChangeHour, binding)
                    }
                    Constants.CONSTANT_PERCENT_CODE_DAY -> {
                        tvCurrencyPercent.text =
                            "${reformatPercent(currencyModel.percentChangeDay)}%"
                        checkTrending(currencyModel.percentChangeDay, binding)
                    }
                    Constants.CONSTANT_PERCENT_CODE_WEEK -> {
                        tvCurrencyPercent.text =
                            "${reformatPercent(currencyModel.percentChangeWeek)}%"
                        checkTrending(currencyModel.percentChangeWeek, binding)
                    }
                }
            }


        }


        private fun checkTrending(percent: Double, binding: CurrencyRvItemBinding) {
            if (percent < 0) {
                binding.tvCurrencyPercent.setTextColor(Color.parseColor("#ef233c"))
                binding.ivTrending.setImageDrawable(binding.root.context.getDrawable(R.drawable.ic_trending_down))
            } else {
                binding.tvCurrencyPercent.setTextColor(Color.parseColor("#19F462"))
                binding.ivTrending.setImageDrawable(binding.root.context.getDrawable(R.drawable.ic_trending_up))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.currency_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currencyModel = currencyList[position]
        holder.bind(currencyModel, code)
    }

    override fun getItemCount(): Int {
        return currencyList.size
    }

    fun filterList(filteredList: ArrayList<CurrencyModel>) {
        currencyList = filteredList
        notifyDataSetChanged()
    }

    fun changePercent(newCode: Int) {
        when (newCode) {
            Constants.CONSTANT_PERCENT_CODE_HOUR -> code = Constants.CONSTANT_PERCENT_CODE_HOUR
            Constants.CONSTANT_PERCENT_CODE_DAY -> code = Constants.CONSTANT_PERCENT_CODE_DAY
            Constants.CONSTANT_PERCENT_CODE_WEEK -> code = Constants.CONSTANT_PERCENT_CODE_WEEK
        }
        notifyDataSetChanged()
    }
}
