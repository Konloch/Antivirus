# Traditional-Antivirus
+ Work in progress

## About
+ A Java implementation of the traditional antivirus.

![Screenshot-1](.github/screen-1.png "Screenshot-1")

## Features
+ Automatically updates from ClamAV's DB[1], Malware Bazaar[2] & VirusShare[3].
+ Targeted file scanning. Does nothing but alert you of the detections.

## Scanning Methods
+ File signature matching

## Todo
+ Implement the false positive db
+ Scanning in archives
+ Regex / substring / logical scanning methods

## Links
1) https://github.com/Cisco-Talos/clamav
2) https://bazaar.abuse.ch/
3) https://virusshare.com/

## Requires
+ Java 1.8

## Notes
+ This is not a wrapper on ClamAV, instead this is an independent implementation using the ClamAV Database. Our scanning methods are not nearly as advanced or complete. If you are looking for a stable AV I recommend you use ClamAV instead.