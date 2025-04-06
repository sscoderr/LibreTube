package com.bimilyoncu.sscoderr.libretubess.ui.models

import android.content.Context
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.MediaServiceRepository
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.extensions.TAG
import com.bimilyoncu.sscoderr.libretubess.helpers.LocaleHelper
import com.bimilyoncu.sscoderr.libretubess.util.deArrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class TrendsViewModel : ViewModel() {
    val trendingVideos = MutableLiveData<List<StreamItem>>()
    var recyclerViewState: Parcelable? = null

    fun fetchTrending(context: Context) {
        viewModelScope.launch {
            try {
                val region = LocaleHelper.getTrendingRegion(context)
                val response = withContext(Dispatchers.IO) {
                    MediaServiceRepository.instance.getTrending(region).deArrow()
                }
                
                // Always filter out shorts from trends section
                val filteredResponse = response.filter { !it.isShort }
                
                trendingVideos.postValue(filteredResponse)
            } catch (e: IOException) {
                println(e)
                Log.e(TAG(), "IOException, you might not have internet connection")
                Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
                return@launch
            } catch (e: HttpException) {
                Log.e(TAG(), "HttpException, unexpected response")
                Toast.makeText(context, R.string.server_error, Toast.LENGTH_SHORT).show()
                return@launch
            }
        }
    }
}
