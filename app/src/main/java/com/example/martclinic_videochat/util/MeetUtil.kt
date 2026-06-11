package com.example.martclinic_videochat.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object MeetUtil {
    fun openGoogleMeet(context: Context, meetUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meetUrl))
        // Try to open with Google Meet app specifically if desired, 
        // otherwise it will open in browser/system picker.
        intent.setPackage("com.google.android.apps.meetings")
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(meetUrl))
            context.startActivity(browserIntent)
        }
    }
}
