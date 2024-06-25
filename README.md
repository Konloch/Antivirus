# Antivirus
Built on top of existing AV Databases and tools we utilize experimental modules and large signature databases to preform throughout scans. The tool is still early in development.

![Screenshot-1](.github/screen-1.png "Screenshot-1")

## Features
+ Scans using Yara & File signatures.
+ SQLite for database storage.
+ Automatically updates from ClamAV's DB[2], Malware Bazaar[3], VirusShare[4], Yaraify[5] & Yara[1].

## How To Use
+ Clone repo & open folder in Intellij.
+ Run YaraAntiVirus with the argument of the file or folder you want to scan.

## Links
1) https://github.com/VirusTotal/yara
2) https://github.com/Cisco-Talos/clamav
3) https://bazaar.abuse.ch/
4) https://virusshare.com/
5) https://yaraify.abuse.ch/

## Requires
+ Java 1.8
+ Windows 10 (Earlier versions probably work)

## Notes
+ Yara integration is powered by Yara CLI
+ File Signature Scanning comes from [Traditional-Antivirus](https://github.com/Konloch/Traditional-Antivirus)