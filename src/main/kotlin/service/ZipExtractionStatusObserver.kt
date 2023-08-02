package service

interface ZipExtractionStatusObserver {
    fun onExtractionStatusChanged(status: ZipExtractionStatus)
}
