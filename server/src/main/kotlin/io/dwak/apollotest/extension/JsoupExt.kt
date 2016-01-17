package io.dwak.apollotest.extension

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rx.Observable

fun Connection.getObservable(): Observable<Document> {
    return Observable.fromCallable { get() }
}

