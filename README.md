# decker-android-poc
[Decker](https://github.com/JohnEarnest/Decker) on Android (Proof of Concept)

This is just JS-enabled WebView that loads Decker's own [tour.html](http://beyondloom.com/decker/tour.html) with some hacks:
1. a viewport is added to the html
2. a `saveBlob` function is added to the WebView, with a toast message
3. the 'target' anchor's click function is replaced with some JS that calls `saveBlob`, thus letting Decker save files
4. the WebView's file chooser lets it Decker open .html files (with a ton of Android security caveats: it doesn't work to save an .html in Brave and then load it in Decker)
5. the app is fullscreened and locked to horizontal orientations

The code is inept and largely generated.

# build
You need to load this whole thing in Android Studio, add a signing key, switch to release mode, build a signed APK, and then transfer it to a phone. There are dozens of additional steps implied here.
