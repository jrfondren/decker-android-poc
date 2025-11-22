# decker-android-poc
[Decker](https://github.com/JohnEarnest/Decker) on Android (Proof of Concept)

This is just JS-enabled WebView that loads Decker's own *unmodified* [tour.html](http://beyondloom.com/decker/tour.html) with some hacks:
1. a viewport is added to the html
2. a `saveBlob` function is added to the WebView, with a toast message
3. the 'target' anchor's click function is replaced with some JS that calls `saveBlob`, thus letting Decker save files
4. the WebView's file chooser lets it Decker open .html files (with a ton of Android security caveats: it doesn't work to save an .html in Brave and then load it in Decker)
5. window.open is hijacked to open 'new tabs' in Android's default browser instead
6. the app is fullscreened and locked to horizontal orientations

The code is inept and largely generated.

You can create decks, modify decks, save decks, and load your saved decks. If
you get HTML decks onto your phone in a way that lets this app load them (it
should work to attach the phone to a computer and transferring the files over),
you can load those decks as well. In Android you can kill the app and reload it
to get the tour again, if you load a deck that doesn't give you a menu.

Although you *can* edit decks in this, you're likely to lose work. No care
been's taken with the Android lifecycle to save data if the system decides to
kill the app off, for example.

# building
You need to load this whole thing in Android Studio, add a signing key, switch
to release mode, build a signed APK, and then transfer it to a phone. There are
dozens of additional steps implied here.

You can probably get an APK from a webpage with fewer manual steps with chrome
bubblewrap (or [PWABuilder](https://www.pwabuilder.com/)).

# wall of shame
redactions are mine:
```
ERROR Command failed: /home/jfondren/.bubblewrap/android_sdk/build-tools/34.0.0/apksigner sign --ks "/home/jfondren/android.jks" --ks-key-alias DeckerPOC --ks-pass pass:"redac`ted" --key-pass pass:"redacted" --out ./app-release-signed.apk ./app-release-unsigned-aligned.apk
/bin/sh: -c: line 1: unexpected EOF while looking for matching ``'
```
