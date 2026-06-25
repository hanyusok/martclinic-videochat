import java.net.URL
import java.net.HttpURLConnection

val url = URL("https://api.kiwoompay.co.kr/pay/hash")
val conn = url.openConnection() as HttpURLConnection
conn.requestMethod = "POST"
conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
conn.doOutput = true

val jsonInputString = """{"PAYMETHOD":"CARD","TYPE":"W","CPID":"CMC21258","ORDERNO":"TEST_123","AMOUNT":"1000"}"""
conn.outputStream.use { os ->
    val input = jsonInputString.toByteArray(Charsets.UTF_8)
    os.write(input, 0, input.size)
}

val responseCode = conn.responseCode
println("Response Code: $responseCode")
if (responseCode == HttpURLConnection.HTTP_OK) {
    println(conn.inputStream.bufferedReader().use { it.readText() })
} else {
    println(conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Error")
}
