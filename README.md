# APK-Frenzy

requires:
java
python

instalations needed:
pip install "typer[all]"

To run the script all you need to run is "python main.py --f {apk name}"

If your filename has a space then ensuure that the path in placed in a " " for example: python main.py --f "mal ware.apk"

The options are:
"s" this will run the malware scan and provide more information in the output
"r" This will extract all the http/https requests and return the result
"sr" This will both run the malware scan and extract all the http/https requests

Example: python main.py sr --f .\malware\BadNews.A.apk


For Linux
instead of python you will need python3, and to run the script you should use python3 in place of python

you will need to go to the "/jadx/bin" directory and give the file "jadx" execute permissions:
"chmod +x jadx"

if you receive the error saying "/bin/sh: 1: jadx/bin/jadx: not found" then you should either redownload the git repo or download jadx and replace the jadx file