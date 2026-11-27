package com.resik.client.ui.screens

import java.net.URLEncoder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Rute penjemputan optimal (Beranda Bank Sampah).
 *
 * Hanya memakai data dari API — tidak ada alamat atau koordinat yang
 * dikarang. Aturan pengurutan:
 * 1. Bila ada koordinat (latitude/longitude): nearest-neighbor dari titik
 *    pertama (titik dengan waktuJemput paling awal), memakai jarak
 *    haversine. Titik tanpa koordinat diurutkan sesudahnya.
 * 2. Bila tidak ada satu pun koordinat: urutkan menurut waktuJemput,
 *    lalu alamat (alfabetis) sebagai penentu.
 */
data class RouteStop(
    val id: Long,
    val label: String,
    val alamat: String,
    val waktu: String,
    val lat: Double?,
    val lng: Double?,
)

/** Jarak haversine dalam kilometer. */
fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2) * sin(dLng / 2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}

private fun compareByWaktuThenAlamat(a: RouteStop, b: RouteStop): Int {
    val byWaktu = a.waktu.compareTo(b.waktu)
    return if (byWaktu != 0) byWaktu else a.alamat.compareTo(b.alamat)
}

/**
 * Urutkan daftar jemputan menjadi rute. Mengembalikan daftar yang sama
 * panjangnya dengan masukan (tidak ada yang dibuang). Bila semua titik
 * punya koordinat, hasilnya murni nearest-neighbor; bila tidak ada yang
 * punya koordinat, hasilnya urutan waktu lalu alamat.
 */
fun orderRoute(stops: List<RouteStop>): List<RouteStop> {
    if (stops.size <= 1) return stops
    val withCoords = stops.filter { it.lat != null && it.lng != null }
        .sortedWith(::compareByWaktuThenAlamat)
    val withoutCoords = stops.filter { it.lat == null || it.lng == null }
        .sortedWith(::compareByWaktuThenAlamat)
    if (withCoords.isEmpty()) return withoutCoords
    if (withCoords.size == 1) return withCoords + withoutCoords

    val ordered = mutableListOf<RouteStop>()
    val remaining = withCoords.toMutableList()
    var current = remaining.removeAt(0) // titik paling awal sebagai start
    ordered.add(current)
    while (remaining.isNotEmpty()) {
        val next = remaining.minByOrNull { stop ->
            haversineKm(current.lat!!, current.lng!!, stop.lat!!, stop.lng!!)
        }!!
        remaining.remove(next)
        ordered.add(next)
        current = next
    }
    return ordered + withoutCoords
}

private fun stopQuery(stop: RouteStop): String =
    if (stop.lat != null && stop.lng != null) "${stop.lat},${stop.lng}" else stop.alamat

private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")

/**
 * URL Google Maps Directions (https://www.google.com/maps/dir/?api=1&...).
 * Mengembalikan null bila tidak ada satu pun alamat/koordinat (tombol
 * "Buka Rute di Maps" harus nonaktif dalam kondisi itu). Bila hanya ada
 * satu titik, memakai destination saja (tanpa origin/waypoints).
 */
fun buildMapsDirectionsUrl(orderedStops: List<RouteStop>): String? {
    val usable = orderedStops.filter { it.alamat.isNotBlank() || (it.lat != null && it.lng != null) }
    if (usable.isEmpty()) return null
    if (usable.size == 1) {
        return "https://www.google.com/maps/dir/?api=1&destination=${enc(stopQuery(usable[0]))}"
    }
    val origin = enc(stopQuery(usable.first()))
    val destination = enc(stopQuery(usable.last()))
    val waypoints = usable.drop(1).dropLast(1)
        .joinToString("|") { enc(stopQuery(it)) }
    return if (waypoints.isEmpty()) {
        "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination"
    } else {
        "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination&waypoints=$waypoints"
    }
}
