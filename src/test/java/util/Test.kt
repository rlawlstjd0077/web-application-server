package util

import org.junit.Test
import java.io.*

class Test {
    @Test
    fun test() {
        val getInputStream: InputStream = FileInputStream(File("./src/test/resources/" + "Http_GET.txt"))
        val postInputStream: InputStream = FileInputStream(File("./src/test/resources/" + "Http_POST.txt"))

        // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        val reader = BufferedReader(InputStreamReader(getInputStream, "UTF-8"))
        println("Method: " + reader.readLine().split(" ")[0])

        val builder = StringBuilder()


        var line: String?
        while (!reader.readLine().also { line = it }.isNullOrEmpty()) {
            println(line)
            if (line == null) {
                return
            }
            builder.append(line).append("\n")
        }
    }
}
