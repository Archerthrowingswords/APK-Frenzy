# APK-Frenzy

APK-Frenzy is a tool that scans Android APKs for malicious activities using static analysis.

## Prerequsites
[Java](https://www.java.com/download/ie_manual.jsp)

[Python3](https://www.python.org/downloads/)

## Instalations needed:

Use the package installer [pip](https://pip.pypa.io/en/stable/) to install [typer](https://typer.tiangolo.com/). Typer is a library for building CLI applications.

```bash
pip install "typer[all]"
```

## Usage

Before running the script, ensure that you are in the APK-Frenzy directory.

To run the script, all you need to run is:
```bash
python apkfrenzy.py --f {apk name}
```

If your filename has a space, then ensure that the filepath is wrapped using " ":
```bash
python apkfrenzy.py --f "mal ware.apk"
```

If you have already decompiled the APK file, then you can run the script without the --f option and the file path, which will scan the decompiled file in the ./out directory:
```bash
python apkfrenzy.py
```

## Options
```bash
# "s" will run the malware scan and provide more information in the output

python apkfrenzy.py s --f .\malware\BadNews.A.apk

# "r" will extract all the http/https requests and return the result

python apkfrenzy.py r --f .\malware\BadNews.A.apk

# "l" will run the malware scan and log the results to a file

python apkfrenzy.py l --f .\malware\BadNews.A.apk

# "srl" will run the malware scan, extract all the http/https requests, and log the results to a file

python apkfrenzy.py srl --f .\malware\BadNews.A.apk
```

## For Linux
Instead of python, you will need python3, and to run the script you should use python3 in place of python.

You will need to go to the "/jadx/bin" directory and give the file "jadx" execute permissions:
"chmod +x jadx".

If you receive the error saying "/bin/sh: 1: jadx/bin/jadx: not found" then you should either redownload the git repo or download jadx and replace the jadx folder.
