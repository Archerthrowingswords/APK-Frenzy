# APK-Frenzy

APK-Frenzy is a tool that can scan Android APKs for malicious activities.

## Prerequsites
java
python3

## Instalations needed:
```bash
pip install "typer[all]"
```

## Usage

Before running the script, ensure that you are in the APK-Frenzy directory.

To run the script, all you need to run is:
```bash
python main.py --f {apk name}
```

If your filename has a space, then ensure that the filepath is wrapped in a " ":
```bash
python main.py --f "mal ware.apk"
```

If you have already decompiled the APK file, then you can run the script without the --f option scan through the ./out directory:
```bash
python main.py
```

## Options
```bash
# "s" will run the malware scan and provide more information in the output

python main.py s --f .\malware\BadNews.A.apk
# or
python main.py s

# "r" will extract all the http/https requests and return the result

python main.py r --f .\malware\BadNews.A.apk
# or
python main.py r

# "sr" will both run the malware scan and extract all the http/https requests

python main.py sr --f .\malware\BadNews.A.apk
# or
python main.py sr
```

## For Linux
Instead of python, you will need python3, and to run the script you should use python3 in place of python.

You will need to go to the "/jadx/bin" directory and give the file "jadx" execute permissions:
"chmod +x jadx".

If you receive the error saying "/bin/sh: 1: jadx/bin/jadx: not found" then you should either redownload the git repo or download jadx and replace the jadx file.