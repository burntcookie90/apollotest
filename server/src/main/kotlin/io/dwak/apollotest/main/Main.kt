package io.dwak.apollotest.main

import com.spotify.apollo.Environment
import com.spotify.apollo.RequestContext
import com.spotify.apollo.httpservice.HttpService
import com.spotify.apollo.route.AsyncHandler
import com.spotify.apollo.route.Route
import com.spotify.apollo.route.Route.sync
import com.spotify.apollo.route.Route.async
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.dwak.apollotest.Story
import io.dwak.apollotest.extension.getObservable
import org.jsoup.Jsoup
import rx.Observable
import rx.schedulers.Schedulers
import rx.schedulers.Schedulers.immediate
import rx.schedulers.Schedulers.io
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

fun main(args: Array<String>) {
    HttpService.boot(::init, "my-app", args)
}

fun init(env: Environment) {
    val moshi = Moshi.Builder().build()
    env.routingEngine().registerAutoRoute(sync("GET", "/") {
        "root"
    });

    env.routingEngine().registerAutoRoute(sync("GET", "/page") {
        "page"
    });

    env.routingEngine().registerAutoRoute(async("GET", "/news", getStories("news", moshi)))
    env.routingEngine().registerAutoRoute(async("GET", "/news?p=2", getStories("news?p=2", moshi)))
    env.routingEngine().registerAutoRoute(async("GET", "/newest", getStories("newest", moshi)))
    env.routingEngine().registerAutoRoute(async("GET", "/show", getStories("show", moshi)))

}

private fun getStories(page : String, moshi: Moshi): (RequestContext) -> CompletableFuture<String> {
    return {
        val stage = CompletableFuture<String>()
        Jsoup.connect("https://news.ycombinator.com/$page")
                .getObservable()
                .map {
                    it.body().getElementsByClass("itemlist")
                            .get(0)
                            .allElements
                            .flatMap { it.getElementsByClass("athing") }
                            .map { Story(it.getElementsByClass("title")[1].text()) }
                }
                .map {
                    moshi.adapter<List<Story>>(Types.newParameterizedType(List::class.java, Story::class.java))
                            .toJson(it)
                }
                .subscribeOn(io())
                .observeOn(immediate())
                .subscribe {
                    stage.complete(it)
                }

        stage
    }
}
