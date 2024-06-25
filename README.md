# Yara-Antivirus
Built on top of [Traditional-Antivirus](https://github.com/Konloch/Traditional-Antivirus), YAV supports file signature scanning and Yara[1] scanning.

## Features
+ Scans using Yara & File signatures.
+ Automatically updates from ClamAV's DB[2], Malware Bazaar[3], VirusShare[4] & Yaraify[5].

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
+ This is simply a CLI wrapper on Yara
+ File Signature Scanning comes from [Traditional-Antivirus](https://github.com/Konloch/Traditional-Antivirus)