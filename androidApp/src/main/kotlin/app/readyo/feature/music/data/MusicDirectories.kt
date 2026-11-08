package app.readyo.feature.music.data

import app.readyo.feature.music.domain.FolderCrumb
import app.readyo.feature.music.domain.MusicDirectory

/** Device and document-provider scans use the same folder identity for shared storage. */
internal fun storageDirectory(documentId: String): MusicDirectory {
    val volume = documentId.substringBefore(':')
    val relativePath = documentId.substringAfter(':', "").trim('/')
    val storageName =
        if (volume.equals("primary", ignoreCase = true)) "Internal storage" else "Storage $volume"
    return MusicDirectory(
        id = storageIdentity("$volume:$relativePath"),
        name = relativePath.substringAfterLast('/').ifBlank { storageName },
        path = listOf(storageName, relativePath).filter { it.isNotBlank() }.joinToString(" / "),
        ancestors =
            buildList {
                if (relativePath.isNotEmpty())
                    add(FolderCrumb(storageIdentity("$volume:"), storageName))
                var prefix = ""
                for (part in relativePath.split('/').dropLast(1)) {
                    prefix = if (prefix.isEmpty()) part else "$prefix/$part"
                    add(FolderCrumb(storageIdentity("$volume:$prefix"), part))
                }
            },
    )
}

internal val importedFilesDirectory =
    MusicDirectory(
        id = "readyo:imports",
        name = "Imported files",
        path = "Saved in ReadyO",
    )
