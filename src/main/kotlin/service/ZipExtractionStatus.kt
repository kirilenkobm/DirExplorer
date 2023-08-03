package service

/**
 * Enum describing all possible cases of ZipArchiveService status.
 */
enum class ZipExtractionStatus {
    NOT_YET_STARTED,
    IN_PROGRESS,
    DONE,
    UNDEFINED,
    FAILED
}
