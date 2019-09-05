# jNanoVNA
Plot s-parameters on Smith chart and log/mag plot from the USB serial interface of the NanoVNA device.
<BR>
<BR>
Only tested on Linux, but should run on Windows, Mac, etc.
<BR><BR>

<BR>Starting the app:
<BR>
<BR>1) plug the NanoVNA device into a USB interface for enumeration
<BR>2) git clone https://github.com/tvelliott/jNanoVNA.git
<BR>3) cd jNanoVNA
<BR>4) java -jar dist/jNanoVNA.jar 

<BR>
<BR>Building the app:
<BR>1) Open the project in Netbeans, start the app in Netbeans. This will update the project files.
<BR>2) After that, you can make edits and type 'ant jar' to rebuild the dist dir from the project dir.
<BR>   
<BR>
<img src="https://github.com/tvelliott/jNanoVNA/blob/master/images/jnanovna_bandpass_mag.png">
<BR>   
<img src="https://github.com/tvelliott/jNanoVNA/blob/master/images/jnanovna_bandpass_smith.png">

