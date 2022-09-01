package com.diplom.petproject

import android.content.res.Resources
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

fun reformatStringDate(oldDate: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val formatter = SimpleDateFormat("dd.MM.yyyy")
    return formatter.format(parser.parse(oldDate))
}

fun reformatPercent(percent: Double): String {
    return String.format("%.2f", percent)
}

fun updateLayoutParamsWidth(view: View, dp: Int) {
    view.updateLayoutParams {
        view.layoutParams.width = (dp * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
    }
}

fun showLoading(view: View){
    view.visibility = View.VISIBLE
}

fun AppCompatActivity.hideLoading(view: View){
    view.visibility = View.INVISIBLE
}

fun AppCompatActivity.showLongToast(text: String){
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}
fun AppCompatActivity.showShortToast(text: String){
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}