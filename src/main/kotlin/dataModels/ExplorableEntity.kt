package dataModels

import state.SortOrder

interface ExplorableEntity: FileSystemEntity {
    var sortOrder: SortOrder
}