package com.cannonballapps.lichess

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cannonballapps.lichess.ui.theme.LichessTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("fubar", "main activity")

        setContent {
            LichessTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val quotesApi = RetrofitHelper.getInstance().create(QuotesApi::class.java)
        val lichessApi = LichessRetrofitHelper.getInstance().create(LichessApi::class.java)
        // launching a new coroutine
        GlobalScope.launch {
            Log.d("fubar", "fuck")
//                val result = quotesApi.getQuotes()
            val result = lichessApi.getAccountEmail()
            if (result != null)
            // Checking the results
                Log.d("fubar", result.body().toString())
        }
    }
}

//object RetrofitHelper {
//
//    val baseUrl = "https://quotable.io/"
//
//    fun getInstance(): Retrofit {
//        return Retrofit.Builder().baseUrl(baseUrl)
//            .addConverterFactory(GsonConverterFactory.create())
//            // we need to add converter factory to
//            // convert JSON object to Java object
//            .build()
//    }
//}
//
//interface QuotesApi {
//    @GET("/quotes")
//    suspend fun getQuotes() : Response<QuoteList>
//}
//
//data class Result(
//    val _id: String,
//    val author: String,
//    val authorSlug: String,
//    val content: String,
//    val dateAdded: String,
//    val dateModified: String,
//    val length: Int,
//    val tags: List<String>
//)
//
//data class QuoteList(
//    val count: Int,
//    val lastItemIndex: Int,
//    val page: Int,
//    val results: List<Result>,
//    val totalCount: Int,
//    val totalPages: Int
//)


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LichessTheme {
        Greeting("Android")
    }
}