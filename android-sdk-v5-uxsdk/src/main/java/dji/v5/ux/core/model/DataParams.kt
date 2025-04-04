package dji.v5.ux.core.model

import com.google.gson.annotations.SerializedName

data class DataParams(
    @SerializedName("altitude")
    val altitude: Data,

    @SerializedName("distance")
    val distance: Data,
)

data class Data(
    @SerializedName("min")
    val min: Double,

    @SerializedName("max")
    val max: Double,
)