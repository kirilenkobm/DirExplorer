package model

/**
 * Service entity serves for cases where an entity-like object is needed,
 * but does not represent any actual file system entity.
 * For example, needed to show the zip unpacking spinner.
 */
class ServiceEntity(override val path: String) : FileSystemEntity {
    override val name = path
    override val isHidden: Boolean
        get() = false
    override val lastModified: Long = -1L
    override val size: Long = -1L
}
