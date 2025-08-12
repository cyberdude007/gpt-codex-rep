package com.splitpaisa.core.backup

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipHelper {
    fun write(files: Map<String, ByteArray>, out: OutputStream) {
        ZipOutputStream(out).use { zip ->
            files.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
    }

    fun listEntries(input: ZipInputStream): List<String> {
        val names = mutableListOf<String>()
        var entry = input.nextEntry
        while (entry != null) {
            names += entry.name
            entry = input.nextEntry
        }
        return names
    }
}
