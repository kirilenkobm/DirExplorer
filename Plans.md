# Mandatory TODO List

* ~~Status bar with data like how many items in the directory, total size, etc~~

_I beleive these 3 are connected:_
* ~~Settings - apply after close immediately~~ -> added settings observer
* ~~Fix and order view updates~~
* ~~BUG: sometimes just does not update dir content view, updating State and AddressBar~~

* Adaptive grid view
* Table view: sorting -> only after I fix view updates
* BUG: filter crashes if I insert "?"
* BUG: quit zip directory which is not yet unpacked
* ~~AppState: implement "in a zip dir" function~~
* ~~Fix bug with back and forward for archives~~
  * ~~Subtask: cleanup temp zips which are no longer in the history~~
* ~~Fix extension filter -> now does not affect anything~~
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

* Detach logic from UI entirely
* Unit tests
* Manual test protocol
* Get rid of magic numbers and strings
* README and documentation

# Prefer to do

* If open in external app - warn about it?
* Thumbnail LRU cache in zip files
* Localization
* Table - different color for even and odd rows
* ~~Spinner when ZIP file is being unpacked~~
* ~~On system event in a directory -> update cache accordingly, instead of calling getContents() again~~ -> WON'T DO
* ~~Thumbnails - extract if exist~~ -> not sure whether it actually works
* ~~PDF previews - extract if exist~~ -> in fact, I generate them from page...
* ~~PNG preview -> transparency on icons~~

# Nice to have

* Lock buttons for not available actions, like go up if already at root
* Hotkeys ??
* Mouseover hints
* Localization: Cyrillic languages
* ~~Status bar: shows whether hidden files are shown and whether inside a zip~~
* ~~Dark and light theme~~
