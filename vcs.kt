
import java.io.File
import java.lang.Exception
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

fun md5(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray(UTF_8))
fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

fun setConfig(command:String, argsCount:Int): Int {
    val config = File("vcs\\config.txt")

    if (command == "config" && argsCount == 1 && !config.exists()) {
        println("Please, tell me who you are.")
        return 0
    }

    if (command == "config" && argsCount == 1 && config.exists() ) {
        val name = config.readText()
        println("The username is ${name}.")
        return 0
    }
    return 0
}

fun setIndex(command:String, argsCount:Int): Int {
    val index = File("vcs\\index.txt")

    if (command == "add" && argsCount == 1 && !index.exists()) {
        println("Add a file to the index.")
        return 0
    }

    if (command == "add" && argsCount == 1 && index.exists() ) {
        val files = index.readLines()
        println("Tracked files:")
        for (file in files) {
            println(file)
        }
        return 0
    }
    return 0
}

fun getLog(command:String, argsCount:Int):Int {
    val commits = File("vcs\\commits")
    val log = File("vcs\\log.txt")

    if (command == "log" && argsCount == 1 && commits.list().size == 0) {
        println("No commits yet.")
        return 0
    }

    val commitsList = log.readText()
    println(commitsList)
    return 0
}

fun getCommit(command:String, commitText:String, argsCount:Int):Int {
    //println(md5("Hello, world!").toHex())
    val commitDir = File("vcs\\commits")
    val index = File("vcs\\index.txt")

    if (command == "commit" && argsCount == 1) {
        println("Message was not passed.")
        return 0
    }
    val files = index.readLines()
    var text = ""
    for (file in files) {
        text += File(file).readText()
    }
    val hash = md5(text).toHex()
    val hashDir = File("vcs\\commits\\$hash")
    if (hashDir.exists()) {
        println("Nothing to commit.")
        return 0
    }
    hashDir.mkdir()
    for (file in files) {
        val fileIn = File(file)
        val fileOut = File("vcs\\commits\\$hash\\$file")
        fileIn.copyTo(fileOut, overwrite = true)
    }

    val log = File("vcs\\log.txt")
    val name = File("vcs\\config.txt")
    val buf = log.readText()

    log.writeText("commit $hash\nAuthor: ${name.readText()}\n$commitText\n" + buf)
    println("Changes are committed.")
    return 0
}

fun restoreFiles(commitHash:String, argsCount:Int):Int {
    if (argsCount == 1) {
        println("Commit id was not passed.")
        return 0
    }
    val commitDir = File("vcs\\commits\\$commitHash")

    val files = File("vcs\\index.txt").readLines()
    if (commitDir.exists()) {
        for (file in files) {
            val fileIn = File("vcs\\commits\\$commitHash\\$file")
            val fileOut = File("$file")
            fileIn.copyTo(fileOut, overwrite = true)
        }
        println("Switched to commit $commitHash.")
        return 0
    }
    println("Commit does not exist.")
    return 0
}

fun updateConfig(name:String, argsCount:Int) {
    val config = File("vcs\\config.txt")
    config.writeText(name)
    println("The username is ${name}.")
}

fun updateIndex(filename:String, argsCount:Int):Int {
    val index = File("vcs\\index.txt")
    val file = File(filename)
    if (file.exists()) {
        index.appendText("${filename}\n")
        println("The file '${filename}' is tracked.")
    }
    else
        println("Can't find '${filename}'.")

    return 0
}

fun main(args: Array<String>) {
    if (args.size == 0 || args[0] == "--help") {
        println("""
These are SVCS commands:
config     Get and set a username.
add        Add a file to the index.
log        Show commit logs.
commit     Save changes.
checkout   Restore a file.""")
    }
    else if (args[0] == "config") { }
    else if (args[0] == "add"){ }
    else if (args[0] == "log") { }
    else if (args[0] == "commit") { }
    else if (args[0] == "checkout") { }
    else
        println("'${args[0]}' is not a SVCS command.")


    val vcsDir = File("vcs")
    if (!vcsDir.exists()) {
        vcsDir.mkdir()
    }

    val commitDir = File("vcs\\commits")
    if (!commitDir.exists()) {
        commitDir.mkdir()
    }

    val log = File("vcs\\log.txt")
    if (!log.exists()) {
        log.createNewFile()
    }

    if (args.size >= 1) {
        if (args[0] == "config" && args.size == 1) {
            setConfig(args[0], args.size)
        }
        if (args[0] == "add" && args.size == 1) {
            setIndex(args[0], args.size)
        }
        if (args[0] == "config" && args.size == 2) {
            updateConfig(args[1], args.size)
        }
        if (args[0] == "add" && args.size == 2) {
            updateIndex(args[1], args.size)
        }
        if (args[0] == "log" && args.size == 1) {
            getLog(args[0], args.size)
        }
        if (args[0] == "commit" && args.size == 1) {
            getCommit(args[0], "", args.size)
        }
        if (args[0] == "commit" && args.size == 2) {
            getCommit(args[0], args[1], args.size)
        }
        if (args[0] == "checkout" && args.size == 1) {
            restoreFiles("", args.size)
        }
        if (args[0] == "checkout" && args.size == 2) {
            restoreFiles(args[1], args.size)
        }
    }

}