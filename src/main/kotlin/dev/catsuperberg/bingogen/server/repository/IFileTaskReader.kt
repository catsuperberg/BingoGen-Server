package dev.catsuperberg.bingogen.server.repository

interface IFileTaskReader {
    val fileExtension: String
    fun read(data: String): List<Task>
}
