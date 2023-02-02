# APK-Frenzy

APK-Frenzy is a tool that scans Android APKs for malicious activities using static analysis.

## Prerequisites
[Java](https://www.java.com/download/ie_manual.jsp)

[Python3](https://www.python.org/downloads/)

Python3-pip

## Installations needed:

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

There are 8 options to choose from:

```bash
# You can put the option in front or behind the malware path.
python apkfrenzy.py --f {malware path} v
# or
python apkfrenzy.py v --f {malware path}

# No option will run the malware scan and return a simplified output.
python apkfrenzy.py --f {malware path}

# "l" will run the malware scan, return the results and log the results to a file.
python apkfrenzy.py l --f {malware path}

# "r" will extract all the http/https requests and return the results.
python apkfrenzy.py r --f {malware path}

# "v" will run the malware scan and return a verbose output.
python apkfrenzy.py v --f {malware path}

# "rl" will extract all the http/https requests, return the results, and log the results to a file.
python apkfrenzy.py rl --f {malware path}

# "vl" will run the malware scan, return a verbose output, and log the results to a file.
python apkfrenzy.py vl --f {malware path}

# "vr" will run the malware scan, extract all the http/https requests and return a verbose output.
python apkfrenzy.py vr --f {malware path}

# "vrl" will run the malware scan, extract all the http/https requests, return a verbose output, and log the results to a file.
python apkfrenzy.py vrl --f {malware path}
```

## For Linux
Instead of python, you will need python3, and to run the script you should use python3 in place of python.

You will need to go to the "/jadx/bin" directory and give the file "jadx" execute permissions:
"chmod +x jadx".

If you receive the error saying "/bin/sh: 1: jadx/bin/jadx: not found" then you should either redownload the git repo or download jadx and replace the jadx folder.
