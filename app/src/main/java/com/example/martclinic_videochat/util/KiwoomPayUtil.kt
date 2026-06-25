package com.example.martclinic_videochat.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object KiwoomPayUtil {
    const val CPID = "CMC21258"

    suspend fun fetchKiwoomEnc(orderNo: String, amount: Int): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.kiwoompay.co.kr/pay/hash")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true

            val jsonPayload = JSONObject().apply {
                put("PAYMETHOD", "CARD")
                put("TYPE", "W")
                put("CPID", CPID)
                put("ORDERNO", orderNo)
                put("AMOUNT", amount.toString())
                put("PRODUCTTYPE", "2")
                put("PRODUCTNAME", "비대면 진료비")
                put("PRODUCTCODE", "MC001")
                put("USERID", "martclinic")
                put("TAXFREECD", "01")
            }.toString()

            conn.outputStream.use { os ->
                val input = jsonPayload.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseStr)
                if (responseJson.optString("RESULTCODE") == "0000") {
                    return@withContext responseJson.optString("KIWOOM_ENC")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    fun buildPaymentHtml(amount: Int, orderNo: String, hash: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body onload="document.payForm.submit();">
                <div style="display: flex; justify-content: center; align-items: center; height: 100vh;">
                    <p>결제창으로 이동 중입니다...</p>
                </div>
                <form name="payForm" accept-charset="EUC-KR" method="post" action="https://api.kiwoompay.co.kr/pay/linkEnc">
                    <input type="hidden" name="KIWOOM_ENC" value="$hash">
                    <input type="hidden" name="TYPE" value="W">
                    <input type="hidden" name="PAYMETHOD" value="CARD">
                    <input type="hidden" name="CERTTYPE" value="01">
                    <input type="hidden" name="CPID" value="$CPID">
                    <input type="hidden" name="ORDERNO" value="$orderNo">
                    <input type="hidden" name="PRODUCTTYPE" value="2">
                    <input type="hidden" name="AMOUNT" value="$amount">
                    <input type="hidden" name="PRODUCTNAME" value="비대면 진료비">
                    <input type="hidden" name="PRODUCTCODE" value="MC001">
                    <input type="hidden" name="USERID" value="martclinic">
                    <input type="hidden" name="TAXFREECD" value="01">
                    <input type="hidden" name="DIRECTRESULTFLAG" value="Y">
                    <input type="hidden" name="HOMEURL" value="https://martclinic.com/payment_success">
                    <input type="hidden" name="CLOSEURL" value="https://martclinic.com/payment_fail">
                    <input type="hidden" name="FAILURL" value="https://martclinic.com/payment_fail">
                </form>
            </body>
            </html>
        """.trimIndent()
    }
}
