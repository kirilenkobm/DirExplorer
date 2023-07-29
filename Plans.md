# Mandatory TODO List

* ~~BUG: quit zip directory which is not yet unpacked~~
* ~~Table view: sorting -> only after I fix view updates~~
* ~~BUG: fix heap issues~~
* ~~BUG: filter crashes if I insert "?"~~
* ~~BUG: dark theme in grid view is broken again~~
* ~~Make uniform icon size regardless on text length and image size~~
* ~~Adaptive grid view~~
* ~~Status bar with data like how many items in the directory, total size, etc~~
* ~~_I believe these 3 are connected:_~~
* ~~Settings - apply after close immediately~~ -> added settings observer
* ~~Fix and order view updates~~
* ~~BUG: sometimes just does not update dir content view, updating State and AddressBar~~
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
* Get rid of magic numbers and strings
* Code documentation
* ~~README~~

# Prefer to do

* Think about filter's behaviour better
* ~~Deselect action too~~
* ~~ILocalization~~I
* ~~ILocalization: Cyrillic languages~~I
* ~~Thumbnail generation: show as soon as are generated.~~
* ~~Thumbnail LRU cache in zip files~~
* ~~Table - different color for even and odd rows~~
* ~~Spinner when ZIP file is being unpacked~~
* ~~On system event in a directory -> update cache accordingly, instead of calling getContents() again~~ -> WON'T DO
* ~~Thumbnails - extract if exist~~ -> not sure whether it actually works
* ~~PDF previews - extract if exist~~ -> in fact, I generate them from page...
* ~~PNG preview -> transparency on icons~~

# Nice to have


* ~~Status bar: shows whether hidden files are shown and whether inside a zip~~
* ~~Dark and light theme~~

# Won't do at first iter

* Lock buttons for not available actions, like go up if already at root
* Hotkeys ??
* Mouseover hints
* If open in external app - warn about it?
