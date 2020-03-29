package at.fhj.msd.slidshow

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import at.fhj.msd.slidshow.model.Feed
import at.fhj.msd.slidshow.service.Slideshow
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dalvik.system.DelegateLastClassLoader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates

const val EXTRA_LOCATION_NAME = "at.fhj.msd.slidshow.LocationName"
const val EXTRA_LOCATION_LAT = "at.fhj.msd.slidshow.LocationLat"
const val EXTRA_LOCATION_LNG = "at.fhj.msd.slidshow.LocationLng"

class MainActivity : AppCompatActivity() {

    private val slideshow = Slideshow

    var currentFeedNumber: Int by Delegates.observable(0) { prop, old, new ->
        findViewById<TextView>(R.id.feedNumber).text = "Slide No. $new"
    }

    var currentFeedDescription: String by Delegates.observable("") { prop, old, new ->
        findViewById<TextView>(R.id.feedDescription).text = "$new"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("MainActivity", "The onCreate function is called")

        // add some demo slides
        if(slideshow.slides.size < 7) { // make sure that the demo slides won't be added more than once
            addSomeDemoSlides()
        }

        setFeed(slideshow.getFirstFeed())

        findViewById<Button>(R.id.next).setOnClickListener {
            setFeed(slideshow.getNextFeed())
        }

        findViewById<Button>(R.id.previous).setOnClickListener {
            setFeed(slideshow.getPreviousFeed())
        }

        findViewById<Button>(R.id.shuffel).setOnClickListener {
            findViewById<CheckBox>(R.id.sortByDate).isChecked = false
            findViewById<CheckBox>(R.id.sortByTitle).isChecked = false
            findViewById<CheckBox>(R.id.sortByID).isChecked = false
            setFeed(slideshow.shuffle())
        }

        findViewById<Switch>(R.id.filterDesc).setOnClickListener {
            findViewById<CheckBox>(R.id.sortByDate).isChecked = false
            findViewById<CheckBox>(R.id.sortByTitle).isChecked = false
            findViewById<CheckBox>(R.id.sortByID).isChecked = false
            if(!findViewById<Switch>(R.id.filterDesc).isChecked) { // uncheck
                slideshow.filterByExistingDescription = false
            } else { // check
                slideshow.checkExistingDescription()
                slideshow.filterByExistingDescription = true
            }
            setFeed(slideshow.getFirstFeed())
        }

        var pref = application.getSharedPreferences("at.fhj.msd.slideshow.START_PREFERENCES",Context.MODE_PRIVATE)
        var theme = pref.getString("THEME","light")
        findViewById<Switch>(R.id.colorTheme).isChecked = theme != "light"
        setTheme(theme)

        findViewById<Switch>(R.id.colorTheme).setOnClickListener {
            var editor = pref.edit()
            if(!findViewById<Switch>(R.id.colorTheme).isChecked) {
                editor.putString("THEME","light")
                setTheme("light")
            } else {
                editor.putString("THEME","dark")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setTheme("dark")
            }
            editor.putInt("FEED_ID",1)
            editor.apply()
        }

        findViewById<CheckBox>(R.id.sortByID).setOnClickListener {
            findViewById<CheckBox>(R.id.sortByDate).isChecked = false
            findViewById<CheckBox>(R.id.sortByTitle).isChecked = false
            slideshow.sortByID()
            setFeed(slideshow.getFirstFeed())
        }

        findViewById<CheckBox>(R.id.sortByDate).setOnClickListener {
            findViewById<CheckBox>(R.id.sortByID).isChecked = false
            findViewById<CheckBox>(R.id.sortByTitle).isChecked = false
            slideshow.unsort()
            slideshow.sortByDate()
            setFeed(slideshow.getFirstFeed())
        }

        findViewById<CheckBox>(R.id.sortByTitle).setOnClickListener {
            findViewById<CheckBox>(R.id.sortByDate).isChecked = false
            findViewById<CheckBox>(R.id.sortByID).isChecked = false
            slideshow.unsort()
            slideshow.sortByTitle()
            setFeed(slideshow.getFirstFeed())
        }

        showOnMap.setOnClickListener {
            if(slideshow.currentFeed != null && slideshow.currentFeed!!.GPSLocation != null) {
                val intent = Intent(this, MapActivity::class.java).apply {
                    putExtra(EXTRA_LOCATION_NAME, slideshow.currentFeed!!.LocationString)
                    putExtra(EXTRA_LOCATION_LAT, slideshow.currentFeed!!.GPSLocation!!.latitude.toString())
                    putExtra(EXTRA_LOCATION_LNG, slideshow.currentFeed!!.GPSLocation!!.longitude.toString())

                }
                startActivity(intent)
            }
        }

        takePicture.setOnClickListener {capture()}

    }

    private fun setFeed(feed: Feed) {

        // we only load the weather from an online resource, therefor we only need the `loadWeather`
        // function to be called asynchronously
        findViewById<TextView>(R.id.feedTitle).text = feed.title
        val imgView = findViewById<ImageView>(R.id.feedImg)
        if(feed.imageBitmap == null) {  // image from URL
            val resID = resources.getIdentifier(feed.imgURL.split("|")[0], "drawable", packageName)
            imgView.setImageResource(resID)
        } else { // image from Bitmap
            imgView.setImageBitmap(feed.imageBitmap)
        }
        currentFeedNumber = slideshow.getCurrentFeedPosition()
        currentFeedDescription = feed.imageDescription.orEmpty()

        CoroutineScope(IO).launch {
            feed.loadWeather(findViewById<TextView>(R.id.weather))
        }
    }

    private fun setTheme(theme: String? = "light") {
        if(theme == "light") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        Log.i("MainActivity", "The dark mode is switched to: $theme")
    }

    private fun addSomeDemoSlides() {
        slideshow.addFeed(Feed("Angkor, Cambodia","p1|Bild von Angkor in Cambodia|Siem Reap, Cambodia|30-12-2019"))
        slideshow.addFeed(Feed("Brihuega, Spain","p2|Lavendelfeld in Spanien|Spain|08-08-2018"))
        slideshow.addFeed(Feed("Andalusia, Spain","p4|Die Milchstraße über Andalusien in Spanien|Spain|06-07-2019"))
        slideshow.addFeed(Feed("Aupouri Peninsula, New Zealand","p5|Leuchturm in Aupouri Peninsula auf Neuseeland|New Zealand|10-09-2019"))
        //slideshow.addFeed(Feed("Bukhansan National Park, South Korea","p6|Der Bukhansan Nationalpark in Südkorea|South Korea|12-04-2019")) this Location does not get a result from the API
        slideshow.addFeed(Feed("Bukhansan National Park, South Korea","p6|Der Bukhansan Nationalpark in Südkorea|Seoul|12-04-2019"))
        slideshow.addFeed(Feed("Canton of Vaud, Switzerland","p7||Switzerland|01-10-2019"))
        slideshow.addFeed(Feed("Chiang Rai, Thailand","p8||Chiang Rai, Thailand|08-08-2018"))

        // syncs, the description, the location and the taken on date
        for(s in slideshow.slides) {
            s.syncImage()
        }
    }


    val REQUEST_IMAGE_CAPTURE = 101

    private fun capture() {
        if(this.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        } else {
            Log.i("CAMERA","Missing perms")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),201)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            if(data.extras != null) {
                Log.i("CAMERA","Photo was taken successfully")
                val imageBitmap = data.extras!!.get("data") as Bitmap
                addTakenImage(imageBitmap)
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                    val vib: Vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    var effectSucc: VibrationEffect = VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE)
                    vib.vibrate(effectSucc)
                    Log.i("CAMERA","Vibrated")
                } else {
                    Log.i("CAMERA","Vibration permission not granted")
                }
            }
        } else {
            Log.i("CAMERA","Problem in onActivityResult; resultCode = ${resultCode}")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun addTakenImage(bitmap: Bitmap) {
        var title = "Custom Photo"
        var description = "Custom Photo taken by the user"
        var GPSLocation : LatLng? = null
        // get current location
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("CAMERA","Missing perms")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),201)
        }
        if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) as Location
            if (location != null) {
                val latitude = location.getLatitude();
                val longitude = location.getLongitude();
                val currCity = LatLng(latitude,longitude)
                Log.i("LOCATION","Current Location: ${currCity.toString()}")
                GPSLocation = currCity
            }
        }else{
            Log.i("LOCATION","Missing perms")
            GPSLocation = LatLng(0.0,0.0)
        }
        // gut current date
        val currentDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("d-MM-yyyy")
        val date = currentDate.format(formatter)

        // example input: URL,Description,Location,Date
        var feed = Feed(title,"|$description|Austria|$date")
        slideshow.addFeed(feed)
        feed.imageBitmap = bitmap
        feed.GPSLocation = GPSLocation
        feed.syncImage()
        setFeed(slideshow.getLastFeed())
    }

    override fun onPause() {
        super.onPause()
        Log.i("MainActivity", "The onPause function is called")
        var pref = application.getSharedPreferences("at.fhj.msd.slideshow.ACTIVITY_STATE",Context.MODE_PRIVATE)
        var editor = pref.edit()
        editor.putInt("FEED_ID",slideshow.currentFeed!!.id)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainActivity", "The onResume function is called")
        var pref = application.getSharedPreferences("at.fhj.msd.slideshow.ACTIVITY_STATE",Context.MODE_PRIVATE)
        var id = pref.getInt("FEED_ID",1)
        var f = slideshow.findFeedByID(id)
        slideshow.setCurrentFeed(f,id-1)
        setFeed(f)
    }
}
