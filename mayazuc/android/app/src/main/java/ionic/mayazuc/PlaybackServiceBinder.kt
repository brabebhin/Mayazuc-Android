package ionic.mayazuc

import android.os.Binder
import ionic.mayazuc.PlaybackService

class PlaybackServiceBinder() : Binder() {
    private lateinit var service: PlaybackService
    val ServiceInstance: PlaybackService
        get() = service

    constructor(instance: PlaybackService) : this() {
        service = instance;
    }
}