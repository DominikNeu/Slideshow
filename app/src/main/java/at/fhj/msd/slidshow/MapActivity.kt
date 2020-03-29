package at.fhj.msd.slidshow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var ourGreatMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.i("MAIN-MAP","map has been activated")
        ourGreatMap = googleMap
        ourGreatMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        var locationName: String? = intent.getStringExtra(EXTRA_LOCATION_NAME)
        if (locationName.isNullOrBlank()) {
            locationName = "No Location specified"
        }
        var lat : Double = 0.0
        var lng : Double = 0.0
        var latitude : String? = intent.getStringExtra(EXTRA_LOCATION_LAT)
        var longitude : String? = intent.getStringExtra(EXTRA_LOCATION_LNG)
        if(!latitude.isNullOrBlank() || !longitude.isNullOrBlank()) {
            lat = latitude!!.toDouble()
            lng = longitude!!.toDouble()
        }

        val currCity = LatLng(lat, lng)
        ourGreatMap.addMarker(MarkerOptions().position(currCity).title(locationName))
        ourGreatMap.moveCamera(CameraUpdateFactory.newLatLng(currCity))

        // ring when position was set
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            Log.i("MAPS","Exception in Map Activity at notification ton")
        }
    }



}

