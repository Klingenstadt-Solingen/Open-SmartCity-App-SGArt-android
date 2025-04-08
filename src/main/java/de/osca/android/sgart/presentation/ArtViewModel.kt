package de.osca.android.sgart.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.RemoteException
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.osca.android.essentials.domain.entity.Coordinates
import de.osca.android.essentials.presentation.base.BaseViewModel
import de.osca.android.essentials.utils.extensions.addIfNotContains
import de.osca.android.essentials.utils.extensions.displayContent
import de.osca.android.essentials.utils.extensions.getLastDeviceLocation
import de.osca.android.essentials.utils.extensions.loading
import de.osca.android.essentials.utils.extensions.resetWith
import de.osca.android.essentials.utils.extensions.toCoordinates
import de.osca.android.networkservice.utils.RequestHandler
import de.osca.android.sgart.data.ArtApiService
import de.osca.android.sgart.entity.KunstInWald
import de.osca.android.sgart.entity.MyBeacon
import de.osca.android.sgart.presentation.args.ArtDesignArgs
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class ArtViewModel @Inject constructor(
    private val artApiService:ArtApiService,
    private val requestHandler:RequestHandler
) : BaseViewModel() {
    var artworks = mutableStateListOf<KunstInWald>()
    var finalArtworks = mutableStateListOf<KunstInWald>()
    var minors = mutableStateListOf<Int>()
    lateinit var beaconManager: BeaconManager
    private val region: Region = Region("myRegion", null, null, null)

    private val myBeaconList = mutableListOf<MyBeacon>()

    @Inject
    lateinit var artDesignArgs: ArtDesignArgs

    fun initializeArtworks() {
        wrapperState.loading()
        this.viewModelScope.coroutineContext.cancelChildren()

        fetchArtworks()
    }

    override fun onCleared() {
        super.onCleared()
        stopSearchingBeacons()
    }

    fun initializeBeaconManager(context: Context) {
        wrapperState.loading()

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                fetchArtworks()

                beaconManager = BeaconManager.getInstanceForApplication(context.applicationContext)
                beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

                stopSearchingBeacons()

                // ADD MONITOR NOTIFIER
                beaconManager.addMonitorNotifier(object : MonitorNotifier {
                    override fun didEnterRegion(region: Region?) {
                        try {
                            finalArtworks.clear()
                            minors.clear()
                            myBeaconList.clear()

                            beaconManager.stopRangingBeacons(region!!)
                            beaconManager.removeAllRangeNotifiers()

                            // ADD RANGE NOTIFIER
                            beaconManager.addRangeNotifier { beacons, _ ->
                                if (beacons.isNotEmpty()) {
                                    for (b in beacons) {
                                        myBeaconList.add(
                                            MyBeacon(
                                                b.id1.toString(),
                                                b.id2.toInt(),
                                                b.id3.toInt(),
                                                b.distance.toString()
                                            )
                                        )
                                    }

                                    if (myBeaconList.isNotEmpty()) {
                                        for (beacon in myBeaconList) {
                                            val artworkObj = artworks.firstOrNull {
                                                it.Minor == beacon.minor && it.Major == beacon.major && it.UUID == beacon.uuid.uppercase()
                                            } // Major: 845, Minor: 1
                                            artworkObj?.let {
                                                it.distance = round(beacon.distance.toDouble()).toString()
                                                finalArtworks.addIfNotContains(artworkObj)
                                            }

                                        }
                                    }
                                }

                                for (artwork in artworks) {
                                    artwork.Minor?.let {
                                        minors.add(artwork.Minor)
                                    }
                                }
                            }

                            beaconManager.startRangingBeacons(region)
                        } catch (e: RemoteException) {
                        }
                    }

                    override fun didExitRegion(region: Region?) {
                        try {
                            beaconManager.stopRangingBeacons(region!!)
                            beaconManager.removeAllRangeNotifiers()

                            finalArtworks.clear()
                            minors.clear()
                            myBeaconList.clear()
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                    }

                    override fun didDetermineStateForRegion(state: Int, region: Region?) {
                    }
                })

                // START MONITORING
                beaconManager.startMonitoring(region)
            }
        }
    }

    fun stopSearchingBeacons() {
        if (::beaconManager.isInitialized) {
            beaconManager.stopRangingBeacons(region)
            beaconManager.removeAllRangeNotifiers()
            beaconManager.stopMonitoring(region)
            beaconManager.removeAllMonitorNotifiers()
        }

    }

    fun fetchArtworks(): Job = launchDataLoad {
        val result = requestHandler.makeRequest(artApiService::getKunstInWald) ?: emptyList()
        artworks.resetWith(result)

        wrapperState.displayContent()
    }

    fun getCenterGeoPoint(): Coordinates {
        var latSum = 0.0
        var lngSum = 0.0
        val geoSumDivider = artworks.size

        for (artwork in artworks) {
            latSum += artwork.geoPoint.latitude
            lngSum += artwork.geoPoint.longitude
        }

        val centerLat = latSum / geoSumDivider
        val centerLng = lngSum / geoSumDivider

        return Coordinates.getDefaultCoordinates(centerLat, centerLng)
    }

    fun getNearestGeoPoint(userLocation: Coordinates): Coordinates {
        var tempGeoPoint = artworks.firstOrNull()?.geoPoint ?: Coordinates()

        for (artwork in artworks) {
            if(userLocation.distanceTo(artwork.geoPoint) < userLocation.distanceTo(tempGeoPoint)) {
                tempGeoPoint = artwork.geoPoint
            }
        }

        return tempGeoPoint
        //return userLocation
    }

    fun isUserInRange(context: Context, range: Int = 500, inRange: () -> Unit, notInRange: () -> Unit) {
        wrapperState.displayContent()

        // LOOP start
        var isInRange = false
        var isNotInRange = false

        viewModelScope.launch {

            do {
                context.getLastDeviceLocation {
                    if (it != null) {
                        val distance = it.toCoordinates().distanceTo(getNearestGeoPoint(it.toCoordinates()))

                        if (distance <= range) {
                            // near enough to start searching for beacons
                            if(!isInRange) {
                                inRange()
                                isInRange = true
                                isNotInRange = false
                            }
                        } else {
                            // stop searching for beacons
                            if(!isNotInRange) {
                                notInRange()
                                isNotInRange = true
                                isInRange = false
                            }
                        }
                    } else {
                        // cannot scan for distance
                    }
                }

                delay(5000)
            } while(true)

        }
        // LOOP end
    }
}