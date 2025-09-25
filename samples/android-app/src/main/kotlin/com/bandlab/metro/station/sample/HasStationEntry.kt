package com.bandlab.metro.station.sample

//TODO: Consider moving to :runtime, we likely need to expose it
interface HasStationEntry {
    fun <T> nowArriving(): T
}