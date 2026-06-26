import { serve } from "https://deno.land/std@0.177.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"
import { JWT } from "npm:google-auth-library@9"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Handle CORS preflight request
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_ANON_KEY') ?? '',
      { global: { headers: { Authorization: req.headers.get('Authorization')! } } }
    )

    // Verify user is an admin
    const { data: { user }, error: userError } = await supabaseClient.auth.getUser()
    if (userError || !user) throw new Error('Unauthorized')

    const { data: profile } = await supabaseClient
      .from('profiles')
      .select('role')
      .eq('id', user.id)
      .single()

    if (profile?.role !== 'admin' && profile?.role !== 'doctor') {
      throw new Error('Only admins or doctors can create meet rooms')
    }

    const { appointment_id: appointmentId } = await req.json()
    if (!appointmentId) throw new Error('appointment_id is required')

    // Google Meet API setup
    // Ensure these secrets are set in Supabase via:
    // supabase secrets set GOOGLE_MEET_CLIENT_EMAIL=... GOOGLE_MEET_PRIVATE_KEY=...
    const clientEmail = Deno.env.get('GOOGLE_MEET_CLIENT_EMAIL')
    const privateKey = Deno.env.get('GOOGLE_MEET_PRIVATE_KEY')?.replace(/\\n/g, '\n')
    
    if (!clientEmail || !privateKey) {
       throw new Error('Google Meet API credentials are not configured in edge function secrets.')
    }

    const client = new JWT({
      email: clientEmail,
      key: privateKey,
      scopes: ['https://www.googleapis.com/auth/meetings.space.created'],
    })

    const accessToken = await client.getAccessToken()

    const meetResponse = await fetch('https://meet.googleapis.com/v2/spaces', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken.token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        // Request body for creating an anonymous/named space
        // See https://developers.google.com/meet/api/guides/spaces
      })
    })

    if (!meetResponse.ok) {
      const errorText = await meetResponse.text()
      console.error('Meet API Error:', errorText)
      throw new Error('Failed to create Google Meet space')
    }

    const meetData = await meetResponse.json()
    const meetUri = meetData.meetingUri // e.g. "https://meet.google.com/abc-defg-hij"

    // Update appointment with meet_link and status = calling
    const { error: updateError } = await supabaseClient
      .from('appointments')
      .update({ 
        meet_link: meetUri,
        status: 'calling'
      })
      .eq('id', appointmentId)

    if (updateError) throw updateError

    return new Response(
      JSON.stringify({ success: true, meetUri }),
      { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 200 }
    )

  } catch (error) {
    console.error(error)
    return new Response(
      JSON.stringify({ error: error.message }),
      { headers: { ...corsHeaders, 'Content-Type': 'application/json' }, status: 400 }
    )
  }
})
