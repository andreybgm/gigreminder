## Gig reminder ##

Gig reminder is an Android app to track the upcoming concerts of artists that you're interested in. It's written for educational purposes and provide only limited functionality. Despite that, it covers many Android development topics, such as the unit and instrumented testing, SQLite, RxJava, network requests, synchronization and others.

### Used libraries ###

* Retrofit
* OkHttp (with a mocking interceptor)
* RxJava
* SQLBrite
* Glide
* ButterKnife
* AssertJ
* Mockito

### Used API ###

The app uses public API [kudago.com](https://kudago.com) which is free and doesn't require any developer keys. The nice documentation is [available](https://docs.kudago.com/api/). Btw, Yandex also [uses](https://yandex.ru/support/afisha/partners.html) it.

### The main usage scenario ###

* Import a list of artists from the Google Play Music library or/and add them manually.
* Add one or more locations (Note: the API contains a predefined list of available cities).
* Get all the found upcoming concerts and keep getting them on automatic updates.

### State management ###

The app uses reactive state management approach that is perfectly explained in [Jake Wharton’s talk](https://youtu.be/0IKHxjkgop4?t=2885).

### The demo ###

This short video demonstrates the main usage scenario:

[![Gig reminder](https://raw.githubusercontent.com/andreybgm/andreybgm.github.io/master/images/gig_reminder.png)](https://youtu.be/tWzYCsIxbGE "Gig reminder")