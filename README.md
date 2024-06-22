# Traditional-Antivirus
+ Work in progress

## About
+ A Java implementation of the traditional antivirus. We use ClamAV's[1] Database for signatures.

## Features
+ Automatically updates from ClamAV's DB, extracts the files and loads them into memory for on-demand scans.
+ Full-disk scanning / targeted file scanning. Does nothing but alert you of the detections (If any).

## Scanning Methods
+ Only file signature matching.
+ Regex / substring / logical are planned to be added.

## Links
1) https://github.com/Cisco-Talos/clamav

## Requires
+ Java 1.8

## Notes
+ This is not a wrapper on ClamAV, instead this is an independent implementation of the ClamAV Database. Our scanning methods are not nearly as advanced or complete. If you are looking for a stable AV I recommend you use that instead.