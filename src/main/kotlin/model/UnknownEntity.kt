package model

/**
 * Special class for file system entities that are not
 * files, symlinks, zip archives or directories.
 */
class UnknownEntity(override val path: String): FileSystemEntity
