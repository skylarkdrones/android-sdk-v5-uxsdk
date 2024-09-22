package dji.v5.ux.core.model

import com.google.gson.annotations.SerializedName

data class FirmwareDataConfig(
    @SerializedName("altitude")
    val altitude: FirmwareDataParam,

    @SerializedName("distance")
    val distance: FirmwareDataParam,
)

data class FirmwareDataParam(
    @SerializedName("min")
    val min: Double,

    @SerializedName("max")
    val max: Double,
)