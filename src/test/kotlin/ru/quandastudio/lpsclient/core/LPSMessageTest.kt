package ru.quandastudio.lpsclient.core

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import ru.quandastudio.lpsclient.model.FriendInfo

class LPSMessageTest {
    @Test
    fun testFriendsListDeserialization() {
        val input = """
            {"data":[{"accepted":true,"login":"Test","userId":17873},
            {"accepted":false,"login":"Superheroes","userId":178},
            {"accepted":true,"login":"Unit","userId":9611}]}
        """

        val output = Gson().fromJson(input, LPSMessage.LPSFriendsList::class.java)

        assertEquals(output.data.size, 3)
        assertEquals(output.data[0], FriendInfo(17873, "Test", true, ""))
    }
}