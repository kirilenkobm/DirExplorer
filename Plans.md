# Mandatory TODO List

* ~~Status bar with data like how many items in the directory, total size, etc~~

_I beleive these 3 are connected:_
* ~~Settings - apply after close immediately~~ -> added settings observer
* ~~Fix and order view updates~~
* ~~BUG: sometimes just does not update dir content view, updating State and AddressBar~~
-> probably no longer an issue

* ~~Fix extension filter -> now does not affect anything~~
* AppState: implement a better current path breakdown
* Fix bug with back and forward for archives
* Table view: sorting -> only after I fix view updates

* README and documentation
* BUG: resizing icon view
* BUG: filter crashes if I insert "?"
* Detach logic from UI entirely

* ~~Fix filesystem listener~~
* ~~Entity select on one click, open on two~~
  * ~~In tables~~
  * ~~In grid view~~
* ~~Disable editing in table view~~
* ~~Semaphore for text generation too~~
* ~~Fix bug where I cannot leave a directory where not all thumbnails are generated~~
* ~~Fix directory get content and system listener~~
* ~~Handle symlinks - depending on destination, pick appropriate handle function~~
* ~~Decomposition for mouse Click functions~~
* ~~ZIP files bug: only DEFLATED entries can have EXT descriptor~~

# Unlimited things

* Unit tests
* Manual test protocol
* Get rid of magic numbers and strings

# Prefer to do

* If open in external app - warn about it?
* Spinner when ZIP file is being unpacked
* Thumbnail LRU cache in zip files
* Localization
* Table - different color for even and odd rows
* ~~On system event in a directory -> update cache accordingly, instead of calling getContents() again~~ -> WON'T DO
* ~~Thumbnails - extract if exist~~ -> not sure whether it actually works
* ~~PDF previews - extract if exist~~ -> in fact, I generate them from page...
* ~~PNG preview -> transparency on icons~~

# Nice to have

* Lock buttons for not available actions, like go up if already at root
* Hotkeys ??
* Mouseover hints
* Localization: Cyrillic languages
* ~~Dark and light theme~~
