Option Explicit

Dim strHost, strMaster_Mask, strMaster_Gateway,changeip_Status

' Check that all arguments required have been passed.
If Wscript.Arguments.Count < 1 Then
    Wscript.Echo "Arguments <Host> required. For example:" & vbCrLf _
    & "cscript vbping.vbs savdaldc01"
    Wscript.Quit(0)
End If

strHost = Wscript.Arguments(0)
strMaster_Mask = Wscript.Arguments(1)
strMaster_Gateway = Wscript.Arguments(2)


if Ping(strHost) = True then
    'Wscript.Echo "Host " & strHost & " contacted"
   writeEvent("Host " & strHost & " contacted")
   '** write ping success to evenlog
   '** now copy the pbxnsip folder to this computer
   '** write copy success to eventlog
Else
    'Wscript.Echo "Host " & strHost & " could not be contacted"
   writeEvent("Host " & strHost & " LOST! Host Could not be contacted! Failover process will begin.")
    '** email admin, let them make sure original master server doesn't come back up
    '** change the IP address of this computer to what the original master was
	if ChangeIP(strHost,strMaster_Mask,strMaster_Gateway) = true then writeEvent("The IP Address of this machine was successfully changed to " & strHost )
    '** start pbxnsip service
end if

Function Ping(strHost)

    dim objPing, objRetStatus

    set objPing = GetObject("winmgmts:{impersonationLevel=impersonate}").ExecQuery _
      ("select * from Win32_PingStatus where address = '" & strHost & "'")

    for each objRetStatus in objPing
        if IsNull(objRetStatus.StatusCode) or objRetStatus.StatusCode<>0 then
    Ping = False
            'WScript.Echo "Status code is " & objRetStatus.StatusCode
        else
            Ping = True
            'Wscript.Echo "Bytes = " & vbTab & objRetStatus.BufferSize
            'Wscript.Echo "Time (ms) = " & vbTab & objRetStatus.ResponseTime
            'Wscript.Echo "TTL (s) = " & vbTab & objRetStatus.ResponseTimeToLive
        end if
    next
End Function 

Function ChangeIP(strHost,strMaster_Mask,strMaster_Gateway)

Dim objWMIService
Dim objNetAdapter
Dim strComputer
Dim arrIPAddress
Dim arrSubnetMask
Dim arrGateway
Dim colNetAdapters
Dim errEnableStatic
Dim errGateways

strComputer = "."
arrIPAddress = Array(strHost)
arrSubnetMask = Array(strMaster_Mask)
arrGateway = Array(strMaster_Gateway)

Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
Set colNetAdapters = objWMIService.ExecQuery("Select * from Win32_NetworkAdapterConfiguration where IPEnabled=TRUE")
For Each objNetAdapter in colNetAdapters
     errEnableStatic = objNetAdapter.EnableStatic(arrIPAddress, arrSubnetMask)
     errGateways = objNetAdapter.SetGateways(arrGateway)
Next

changeip = true

End Function


Function WriteEvent(event_description)

dim strmessage, objshell

' Constants for type of event log entry
const EVENTLOG_SUCCESS = 0
const EVENTLOG_ERROR = 1
const EVENTLOG_WARNING = 2
const EVENTLOG_INFORMATION = 4
const EVENTLOG_AUDIT_SUCCESS = 8
const EVENTLOG_AUDIT_FAILURE = 16

strMessage = event_description

Set objShell = Wscript.CreateObject("Wscript.Shell")
objShell.LogEvent EVENTLOG_INFORMATION, strmessage

'wscript.LogEvent EVENTLOG_INFORMATION, strMessage

end function

