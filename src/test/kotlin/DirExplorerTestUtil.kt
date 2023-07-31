import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Test utils to create some random directory with content
 * to test different scenarios.
 *
 * tree
 * .
 * ├── archive.zip
 * ├── document1.pdf
 * ├── emptydir
 * ├── file.log
 * ├── file1.txt
 * ├── file2.txt
 * ├── image1.png
 * ├── image2.jpeg
 * ├── subdir1
 * │     └── subfile1.txt
 * ├── subdir2
 * │    └── subfile2.txt
 * └── symlink -> /var/folders/q6/wd325dmj47z8l7cdb51650z80000gn/T/testDir4750311573834825956/file1.txt
 *
 * 3 directories, 10 files
 *
 */
object DirExplorerTestUtil {
    // Create a temporary directory with known contents
    private val tempDir: Path = Files.createTempDirectory("testDir")

    fun setupTestDirectory(): Path {

        // Create some text files with random content
        Files.write(tempDir.resolve("file1.txt"), "Some random content".toByteArray())
        Files.write(tempDir.resolve("file2.txt"), "Other dummy content".toByteArray())
        Files.write(tempDir.resolve("file.log"), "Log file content".toByteArray())

        // Create some subdirectories
        val subdir1 = Files.createDirectory(tempDir.resolve("subdir1"))
        val subdir2 = Files.createDirectory(tempDir.resolve("subdir2"))
        val emptydir = Files.createDirectory(tempDir.resolve("emptydir"))

        // some files in the subdirectories
        Files.write(subdir1.resolve("subfile1.txt"), "Placeholder text".toByteArray())
        Files.write(subdir2.resolve("subfile2.txt"), "Another placeholder text".toByteArray())

        // some dummy image and PDF files
        // just empty files with the correct extensions
        Files.createFile(tempDir.resolve("image1.png"))
        Files.createFile(tempDir.resolve("image2.jpeg"))
        Files.createFile(tempDir.resolve("document1.pdf"))

        // Create a zip archive
        val zipFile = tempDir.resolve("archive.zip").toFile()
        ZipOutputStream(zipFile.outputStream()).use { zos ->
            val entry = ZipEntry("fileInZip.txt")
            zos.putNextEntry(entry)
            zos.write("Content of file in zip".toByteArray())
            zos.closeEntry()
        }

        // Create a symlink
        Files.createSymbolicLink(tempDir.resolve("root_to_subfile1"), subdir1.resolve("subfile1.txt"))

        return tempDir
    }

    fun teardownTestDirectory() {
        // Delete the temporary directory and its contents
        Files.walk(tempDir)
            .sorted(Comparator.reverseOrder())
            .forEach(Files::delete)
    }
}
