package de.osca.android.sgart.entity

import com.google.gson.annotations.SerializedName
import de.osca.android.essentials.domain.entity.Coordinates

data class KunstInWald(
    @SerializedName("objectId")
    val objectId: String? = null,
    @SerializedName("LATERNE_NR")
    var LATERNE_NR: Int? = null,
    @SerializedName("KUENSTLER")
    val KUENSTLER: String? = null,
    @SerializedName("KW_IMAGEGALLERY")
    val KW_IMAGEGALLERY: MutableList<String>? = null,
    @SerializedName("KUENSTLER_KUNSTFORM")
    val KUENSTLER_KUNSTFORM: MutableList<String>? = null,
    @SerializedName("ORTSBESCHREIBUNG")
    val ORTSBESCHREIBUNG: String? = null,
    @SerializedName("KUENSTLER_IMAGES")
    val KUENSTLER_IMAGES: MutableList<String> = mutableListOf(),
    @SerializedName("KUENSTLER_LINK")
    val KUENSTLER_LINK: MutableList<String>? = null,
    @SerializedName("ID_KIW")
    val ID_KIW: Int? = null,
    @SerializedName("KUENSTLER_URL")
    val KUENSTLER_URL: MutableList<String> = mutableListOf(),
    @SerializedName("TITEL_HOTSPOT")
    val TITEL_HOTSPOT: String? = null,
    @SerializedName("KW_TITLEIMAGE")
    val KW_TITLEIMAGE: String? = null,
    @SerializedName("KURZTEXT")
    val KURZTEXT: String? = null,
    @SerializedName("ID_LATERNE")
    val ID_LATERNE: Int? = null,
    @SerializedName("artists")
    val artists: MutableList<String>? = null,
    @SerializedName("Minor")
    val Minor: Int? = null,
    @SerializedName("Major")
    val Major: Int? = null,
    @SerializedName("UUID")
    val UUID: String? = null,
    @SerializedName("geopoint")
    val geoPoint: Coordinates = Coordinates()
) {
    var distance: String = "-1m"
}