package dataModels

class ZipArchive(override val path: String) : ExplorerDirectory(path) {
    override fun getContents(): List<FileSystemEntity> {
        // TODO: Implement logic to get files and directories in ZIP file
        return emptyList()
    }

    override fun getSize(): Long {
        // Implement logic to get total size of ZIP file
        return 0
    }

    override fun sortByName() {
        // Implement logic to sort files and directories in ZIP file by name
    }

    override fun sortBySize() {
        // Implement logic to sort files and directories in ZIP file by size
    }
}