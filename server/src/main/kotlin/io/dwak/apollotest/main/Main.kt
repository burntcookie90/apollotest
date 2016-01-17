package io.dwak.apollotest.main

import com.spotify.apollo.Environment
import com.spotify.apollo.httpservice.HttpService
import com.spotify.apollo.route.Route
import com.spotify.apollo.route.Route.sync
import java.util.concurrent.CompletionStage

fun main(args : Array<String>){
    HttpService.boot(::init, "my-app", args)
}

fun init(env : Environment){
    env.routingEngine().registerAutoRoute(sync("GET", "/") {
        "root"
    });

    env.routingEngine().registerAutoRoute(sync("GET", "/page") {
        "page"
    });

}
