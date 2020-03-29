package at.fhj.msd.slidshow.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import at.fhj.msd.slidshow.service.Slideshow
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.CoroutineContext

//data class Feed(val title: String, val imgURL: String? = null) => image kann immutable variable oder sonst null sein
data class Feed(val title:String, val imgURL:String = "http://default.img,Description,Location,Date") { // default img

    val wheatherAPI:String = "https://api.openweathermap.org/data/2.5/weather?q="    //Chiang%20Rai,%20Thailand
    val APIKey:String = "&units=metric&APPID=a6d3a3d09bcac10b5e6f7a7664093c40"

    val id:Int by lazy {
        assignID()
    }

    private fun assignID():Int {
        return Slideshow.getAmountOfSlides()
    }

    override fun toString(): String {
        return "Slide $title"
    }

    val base64ImageString:String by lazy {
        loadbase64Image()
    }

    private fun loadbase64Image():String {
        println("expensive operation")
        return "not implemented"
    }

    val soundLink:URL by lazy {
        loadSoundOverLink()
    }

    private fun loadSoundOverLink():URL {
        println("Put in URL")
        return URL("soundlink.com")
    }

    var imageBitmap : Bitmap? = null

    val lastSyncTimeStamp:String by lazy {
        syncImage()
    }

    public fun syncImage():String {
        var now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        // loading the image locally
        var parts = imgURL.split("|") // example input: URL,Description,Location,Date
        imageDescription = loadDescription(parts[1])
        LocationString = loadLocationString(parts[2])
        takenOnDate = loadTakenOnDate(parts[3])
        // load weather not when all images get synced
        return now
    }

    public suspend fun loadWeather(textView: TextView) {
        // loading the weather from the openWeather API
        if(LocationString != null) {

            try {
                var urlLocation = LocationString!!.replace(" ", "%20")
                var requestURL = wheatherAPI + urlLocation + APIKey;
                with(URL(requestURL).openConnection() as HttpURLConnection) {
                    requestMethod = "GET"

                    inputStream.bufferedReader().use {
                        var weatherJSONString = ""
                        var weatherString = ""
                        var coordJSONString = ""
                        it.lines().forEach{ line ->
                            var jsonArray = JSONArray("[" + line +"]")  // the API-Response has only one Object {...}, but we need an array [{...}]
                            weatherJSONString = jsonArray.getJSONObject(0).getString("weather")
                            weatherString = JSONArray(weatherJSONString).getJSONObject(0).getString("description")
                            if(GPSLocation == null) {
                                coordJSONString = jsonArray.getJSONObject(0).getString("coord")
                                var coordJSONArray = JSONArray("[" + coordJSONString + "]").getJSONObject(0)
                                var longitude = coordJSONArray.getString("lon").toDouble()
                                var latitidue = coordJSONArray.getString("lat").toDouble()
                                var latLng: LatLng = LatLng(latitidue, longitude)
                                GPSLocation = latLng
                            }
                            }
                        withContext(Main) {
                            textView.text = weatherString
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("Feed", "Could not load weather data for location: " + LocationString!!)
                withContext(Main) {
                    textView.text = "No weather data"
                }
            }
        }
    }


    var takenOnDate:LocalDate? = null

    private fun loadTakenOnDate(DateString:String?):LocalDate? {
        var formatter = DateTimeFormatter.ofPattern("d-MM-yyyy")
        return LocalDate.parse(DateString, formatter)
    }

    var imageDescription:String? = null

    private fun loadDescription(Desc:String?):String {
        if(Desc == null) {
            return "No Description"
        } else {
            return Desc
        }
    }

    var LocationString:String? = null

    private fun loadLocationString(Loc: String?):String? {
        // location will need additional conversion
        return Loc
    }

    var GPSLocation: LatLng? = null

}

