# TODO List

* Settings - apply after close immediately
* ~~Fix bug where I cannot leave a directory where not all thumbnails are generated~~
* Semaphore for text generation too?
* Fix bug with back and forward for archives
* ~~Fix directory get content and system listener~~
* ~~Handle symlinks - depending on destination, pick appropriate handle function~~
* ~~Decomposition for mouse Click functions~~
* Entity select on one click, open on two
* Fix and order view updates
* Unit tests
* Stress tests ???
* Get rid of magic numbers and strings

# Prefer to do

* On system event in a directory -> update cache accordingly, instead of calling getContents() again
* Thumbnails - extract if exist
* PDF previews - extract if exist
* Status bar with data like how many items in the directory, total size, etc
* Better localization
* If open in external app -warn about it?
* Lock buttons for not available actions, like go up if already at root
* Spinner when ZIP file is being unpacked


# Nice to have

* Dark and light theme
* Hotkeys ??
* Mouseover hints
* Cyrillic languages
