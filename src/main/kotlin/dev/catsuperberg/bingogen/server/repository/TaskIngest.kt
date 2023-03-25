package dev.catsuperberg.bingogen.server.repository

import java.io.File

class TaskIngest(private val pathToScan: String, private val fileReader: IFileTaskReader) {
    fun load(): List<Task> {
        val files = scan()
        val textFiles = files.map { File(it).readText() }
        return textFiles.flatMap { fileReader.read(it) }
    }

    private fun scan(): List<String> = File(pathToScan).walk()
            .filter { it.isFile && it.extension == fileReader.fileExtension }
            .map { it.absolutePath }
            .toList()
}
