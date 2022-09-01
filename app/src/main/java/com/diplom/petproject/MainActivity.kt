package com.diplom.petproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.diplom.petproject.Adapter.CurrencyAdapter
import com.diplom.petproject.Model.CurrencyModel
import com.diplom.petproject.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    val currencyList = ArrayList<CurrencyModel>()
    lateinit var currencyAdapter: CurrencyAdapter
    private val currencyListFiltered = ArrayList<CurrencyModel>()
    private val coroutineIOScope = CoroutineScope(Dispatchers.IO + CoroutineName("NetworkIOScope"))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init(binding)

        coroutineIOScope.launch {
            getDataFromApi()
        }


        searchListener(binding.edSearch)

        showFilters(binding)

        filters(binding)

    }


    /** ------------------------------ Changes the code of filter in adapter------------------------------ */
    private fun filters(binding: ActivityMainBinding) {
        binding.apply {
            btnHourFilter.setOnClickListener {
                currencyAdapter.changePercent(Constants.CONSTANT_PERCENT_CODE_HOUR)
            }
            btnDayFilter.setOnClickListener {
                currencyAdapter.changePercent(Constants.CONSTANT_PERCENT_CODE_DAY)
            }
            btnWeekFilter.setOnClickListener {
                currencyAdapter.changePercent(Constants.CONSTANT_PERCENT_CODE_WEEK)
            }
        }
    }

    /** ------------------------------ Show the filter's View's------------------------------ */
    private fun showFilters(binding: ActivityMainBinding) {
        binding.apply {
            showFilters.setOnClickListener {
                if (filterContainer.visibility == View.INVISIBLE) {
                    updateLayoutParamsWidth(filterContainer, 200)
                    filterContainer.visibility = View.VISIBLE
                } else {
                    updateLayoutParamsWidth(filterContainer, 0)
                    filterContainer.visibility = View.INVISIBLE
                }
            }

        }


    }


    /** ------------------------------ Search Ed TextEdited Listener------------------------------ */
    private fun searchListener(edSearch: EditText) {
        edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                currencyListFiltered.clear()
                search(s.toString().lowercase())
            }

        })
    }

    /** ------------------------------ Search Method------------------------------ */
    private fun search(s: String) {
        if (s.isNotEmpty()) {
            for (item in currencyList) {
                if (item.currencyName.lowercase().contains(s)) currencyListFiltered.add(item)
            }
        } else currencyListFiltered.clear()

        if (currencyListFiltered.isNotEmpty()) currencyAdapter.filterList(currencyListFiltered)
        else currencyAdapter.filterList(currencyList)
    }


    /** ------------------------------ Getting the Data from API------------------------------ */
    private suspend fun getDataFromApi() {
        coroutineIOScope.launch(Dispatchers.Main) {
            showLoading(binding.loadingView)
        }
        var urlMetaData = Constants.CONSTANT_URL_CURRENCY_META_DATA

        val requestQueue = Volley.newRequestQueue(this)

        var currencyJsonObject: JSONObject
        var dataArray: JSONArray
        var usdJsonObject: JSONObject

        var currencyNameText: String
        var currencyCodeText: String
        var currencyPriceText: Double
        var percentChangeDay: Double
        var percentChangeHour: Double
        var percentChangeWeek: Double
        var lastUpdated: String
        var id: String


        val jsonRequest = @SuppressLint("NotifyDataSetChanged")
        object : JsonObjectRequest(Method.GET, Constants.CONSTANT_URL_CURRENCY, null, {
            try {
                dataArray = it.getJSONArray("data")
                for (i in 0 until dataArray.length()) {
                    // Currency Full Data Object
                    currencyJsonObject = dataArray.getJSONObject(i)
                    // Getting data from Full Data Object
                    id = currencyJsonObject.getInt("id").toString()
                    currencyNameText = currencyJsonObject.getString("name")
                    currencyCodeText = currencyJsonObject.getString("symbol")

                    // quote/USD Object
                    usdJsonObject = currencyJsonObject.getJSONObject("quote").getJSONObject("USD")
                    // Getting data from quote/USD
                    currencyPriceText = usdJsonObject.getDouble("price")
                    percentChangeDay = usdJsonObject.getDouble("percent_change_24h")
                    percentChangeHour = usdJsonObject.getDouble("percent_change_1h")
                    percentChangeWeek = usdJsonObject.getDouble("percent_change_7d")
                    lastUpdated = usdJsonObject.getString("last_updated")

                    // Adding the Data to the CurrencyList
                    currencyList.add(
                        CurrencyModel(
                            currencyNameText,
                            currencyCodeText,
                            currencyPriceText,
                            percentChangeDay,
                            percentChangeHour,
                            percentChangeWeek,
                            lastUpdated,
                            id
                        )
                    )

                    // Filling the METADATA URL
                    urlMetaData += if (i == dataArray.length() - 1) id
                    else "$id,"
                }
                currencyAdapter.notifyDataSetChanged()
                coroutineIOScope.launch { getMetaDataFromApi(requestQueue, urlMetaData) }


            } catch (e: JSONException) {
                with(e) { printStackTrace() }
                coroutineIOScope.launch(Dispatchers.Main) {
                    showLongToast("GET CURRENCY_DATA ERROR: ${e.message}")
                }
            }
        },// Volley Error Listener
            {
                coroutineIOScope.launch(Dispatchers.Main) {
                    showLongToast("VOLLEY_ERROR: ${it.message}")
                }
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers[Constants.CONSTANT_HEADER_KEY] = Constants.CONSTANT_HEADER_VALUE
                return headers
            }
        }
        requestQueue.add(jsonRequest)
    }

    private suspend fun getMetaDataFromApi(requestQueue: RequestQueue, url: String) {
        var allCurrencyObject: JSONObject
        var currencyObject: JSONObject

        val imageJsonRequest: JsonObjectRequest =
            @SuppressLint("NotifyDataSetChanged")
            object : JsonObjectRequest(Method.GET, url, null, {
                coroutineIOScope.launch(Dispatchers.Main) {
                    hideLoading(binding.loadingView)
                }
                try {
                    allCurrencyObject = it.getJSONObject("data")
                    for (i in currencyList) {
                        currencyObject = allCurrencyObject.getJSONObject(i.id)
                        i.imageUrl = currencyObject.getString("logo")
                    }
                } catch (e: JSONException) {
                    with(e) { printStackTrace() }
                    coroutineIOScope.launch(Dispatchers.Main) {
                        showLongToast("GET METADATA ERROR: ${e.message}")
                    }
                }
                currencyAdapter.notifyDataSetChanged()

            }, {
                coroutineIOScope.launch(Dispatchers.Main) {
                    hideLoading(binding.loadingView)
                    showLongToast("VOLLEY ERROR: ${it.printStackTrace()}")
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers[Constants.CONSTANT_HEADER_KEY] = Constants.CONSTANT_HEADER_VALUE
                    return headers
                }
            }
        requestQueue.add(imageJsonRequest)
    }


    /** ------------------------------ Init method------------------------------ */
    private fun init(binding: ActivityMainBinding) {
        binding.apply {
            currencyRv.setHasFixedSize(true)
            currencyRv.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            currencyAdapter = CurrencyAdapter(currencyList)
            currencyRv.adapter = currencyAdapter
        }

    }
}