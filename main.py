import typer
import os
from pathlib import Path
import json
import shutil #fairly certain this works on linux
import re
import subprocess

# initializing global variables
dangerRating = 0
app = typer.Typer()
state = {"verbose": False}
directory = 'out'
f = open("detectionPatterns.json")
detectionPatterns = json.load(f)
detectedPatterns = {}

#collection of all pattern keywords
allManifestKeywords =[]
allOptionalManifestKeywords = []
allJavaKeywords = []
allJavaOptionalKeywords = []
allManifestAlternateKeywords = [] 
allJavaAlternateKeyswords = []

#collection of all found pattern keywords
allManifestFoundKeywords = []
allJavaFoundKeywords = []
allManifestFoundOptionalKeywords = []
allJavaFoundOptionalKeywords = []
allManifestFoundAlternateKeywords = [] 
allJavaFoundAlternateKeywords = []

validManifestFoundOptionalKeywords = []
validJavaFoundOptionalKeywords = []

def apkFrenzyIntro():
    print("\n=================================================================")
    print("||   ___  ______ _   ______________ _____ _   _  ________   __ ||")
    print("||  / _ \ | ___ \ | / /|  ___| ___ \  ___| \ | ||___  /\ \ / / ||")
    print("|| / /_\ \| |_/ / |/ / | |_  | |_/ / |__ |  \| |   / /  \ V /  ||")
    print("|| |  _  ||  __/|    \ |  _| |    /|  __||     |  / /    \ /   ||")
    print("|| | | | || |   | |\  \| |   | |\ \| |___| |\  | / /___  | |   ||")
    print("|| \_| |_/\_|   \_| \_/\_|   \_| \_\____/\_| \_/\_____/  \_/   ||")
    print("||                                                             ||")
    print("=================================================================\n")
            

def checkApkInput(file):
    apkname = os.path.basename(file)
    if file is None:
        print("No APK file")
        raise typer.Abort()
    elif apkname=="null":
        print("no file has been specified, please specify a file")
        raise typer.Abort()
    elif file.is_dir():
        print("Config is a directory, please specify a file")
        raise typer.Abort()
    elif not file.exists():
        print("The apk doesn't exist")
        raise typer.Abort()
    elif not apkname.endswith(".apk"):
        print(f"File is not an apk")
        raise typer.Abort()
    apkFrenzyIntro()
    print(f"Filename: ({apkname})")
    print("-------------------------------------------------------------")

def decompileAPK(file):
    os.environ["PATH"] = f"{os.environ['PATH']};.\jadx\\bin\\"
    # Remove existing out directory from previous scan
    if(os.path.exists("out")):
        shutil.rmtree("out")
    os.mkdir("out")
    if (os.name == "nt"):       
        os.system(f'jadx -d out /"{file}"')
    elif (os.name == "posix"):
        print("linuxbaby")
        command = f'jadx/bin/jadx -d out /"{file}"'
        output = f'-d ./out'
        subprocess.run(command,shell=True)


def collectPatterns(detectionPatterns):
    global allManifestKeywords 
    global allOptionalManifestKeywords 
    global allJavaKeywords 
    global allJavaOptionalKeywords 
    for patternName in detectionPatterns:
        patternData = detectionPatterns[patternName]
        allManifestKeywords.extend(patternData["manifestKeywords"])
        allOptionalManifestKeywords.extend(patternData["optionalManifestKeywords"])
        allJavaKeywords.extend(patternData["javaKeywords"])
        allJavaOptionalKeywords.extend(patternData["javaOptionalKeywords"])
        allManifestAlternateKeywords.extend(patternData["alternateManifestKeywords"])
        allJavaAlternateKeyswords.extend(patternData["alternateJavaKeywords"])
    #remove duplicates
    allManifestKeywords= list(set(allManifestKeywords)) 
    allOptionalManifestKeywords = list(set(allOptionalManifestKeywords))
    allJavaKeywords= list(set(allJavaKeywords))
    allJavaOptionalKeywords=list(set(allJavaOptionalKeywords))

def patternDetection():
    # Searching through Manifest XML file
    out = Path(directory).rglob('*')
    for file in out:
        if file.name.endswith("Manifest.xml"):
            with open(file) as manifestFile:
                for line in manifestFile:
                    #looking through the line for manifest keywords
                    for manifestKeyword in allManifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            allManifestFoundKeywords.append(manifestKeyword)
                            allManifestKeywords.remove(manifestKeyword)
                    #looking through the line for alternate manifest keywords
                    for manifestKeywordList in allManifestAlternateKeywords: 
                        if type(manifestKeywordList) is not list: continue
                        for alternateKeyword in manifestKeywordList:
                            if line.find(alternateKeyword) != -1:
                                allManifestFoundAlternateKeywords.append(manifestKeywordList)
                                allManifestAlternateKeywords.remove(manifestKeywordList)
                    #looking through the line for optional manifest keywords
                    for manifestKeyword in allOptionalManifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            allManifestFoundOptionalKeywords.append(manifestKeyword)
                            allOptionalManifestKeywords.remove(manifestKeyword)
            manifestFile.close()
        elif file.name.endswith(".java"): 
            # Searching through Java files
                for javaKeyword in allJavaKeywords:
                    if file.read_text(encoding='utf-8').find(javaKeyword) != -1:
                        allJavaFoundKeywords.append(javaKeyword)
                        allJavaKeywords.remove(javaKeyword)
                #looking through the file for alternate java keywords
                for javaKeywordList in allJavaAlternateKeyswords:
                    if javaKeywordList is not list: continue
                    for javaKeyword in javaKeywordList:
                        if file.read_text(encoding='utf-8').find(javaKeyword) != -1:
                            allJavaFoundAlternateKeywords.append(javaKeywordList)
                            allJavaAlternateKeyswords.remove(javaKeywordList)
                #looking through the file for optional java keywords
                for javaKeyword in allJavaOptionalKeywords:
                    if file.read_text(encoding='utf-8').find(javaKeyword) != -1:
                        allJavaFoundOptionalKeywords.append(javaKeyword)
                        allJavaOptionalKeywords.remove(javaKeyword)
    
def checkDetected():
    global dangerRating
    for patternName in detectionPatterns:
        patternData = detectionPatterns[patternName]
        manifestKeywords = (patternData["manifestKeywords"])
        alternateManifestKeywords = (patternData["alternateManifestKeywords"])
        optionalManifestKeywords = (patternData["optionalManifestKeywords"])
        javaKeywords = (patternData["javaKeywords"])
        alternateJavaKeywords = (patternData["alternateJavaKeywords"])
        javaOptionalKeywords = (patternData["javaOptionalKeywords"])
        description = (patternData["description"])
        
        manifestKeywords = list(set(manifestKeywords)-set(allManifestFoundKeywords))
        javaKeywords = list(set(javaKeywords)-set(allJavaFoundKeywords))

        for manifestList in alternateManifestKeywords:
            if manifestList in allManifestFoundAlternateKeywords: alternateManifestKeywords.remove(manifestList)

        for javaList in alternateJavaKeywords:
            if javaList in allJavaFoundAlternateKeywords:alternateJavaKeywords.remove(javaList)

        if (manifestKeywords==[] and javaKeywords==[] and alternateManifestKeywords==[] and alternateJavaKeywords==[]):
            detectedPatterns[patternName] = description
            dangerRating = dangerRating + patternData["dangerRating"]
            validManifestFoundOptionalKeywords.extend(list(set(optionalManifestKeywords).intersection(allManifestFoundOptionalKeywords)))
            validJavaFoundOptionalKeywords.extend(list(set(javaOptionalKeywords).intersection(allJavaFoundOptionalKeywords)))

    list(set(validManifestFoundOptionalKeywords))
    list(set(validJavaFoundOptionalKeywords))

def scanReq():
    out = Path(directory).rglob('*')
    global httpList
    httpList = []
    for file in out:
        if file.name.endswith(".java"):
            #looking through the file for java keywords
            httpPattern = r'"https?://\S+"'
            temphttpList = re.findall(httpPattern, file.read_text(encoding='utf-8'))
            httpList.extend(temphttpList)
    list(set(httpList))
    httpList.sort()

def simpleScanResult():
    global dangerRating
    bar = '{:░<20}'.format('█'*(dangerRating//5))
    if(dangerRating > 99): dangerRating = 99
    print(f"\nMalicious Confidence Rating: {bar} {dangerRating}%")
    print("-------------------------------------------------------------")
    if (detectedPatterns == {}):
        print("No malicous paterns detected")
        return
    print(f"Paterns detected:")
    for i in detectedPatterns: print(f"-{i}")
    print("-------------------------------------------------------------")
   
def scanResult():
    global dangerRating
    bar = '{:░<20}'.format('█'*(dangerRating//5))
    if(dangerRating > 99): dangerRating = 99
    print(f"\nMalicious Confidence Rating: {bar} {dangerRating}%")
    print("-------------------------------------------------------------")
    if (detectedPatterns == {}):
        print("No malicous paterns detected")
        return
    print(f"Paterns detected:")
    for i in detectedPatterns: print(f"\n-{i}\n{detectedPatterns[i]}")
    if (validManifestFoundOptionalKeywords != []):
        print(f"\nInteresting manifest keywords found:")
        for i in validManifestFoundOptionalKeywords: print(f"-{i}")
    if (validJavaFoundOptionalKeywords != []):
        print(f"\nInteresting java keywords found:")
        for i in validJavaFoundOptionalKeywords: print(f"-{i}")
    print("-------------------------------------------------------------")
    
def reqResult():
    global httpList
    if (httpList == []):
        print("No http or https requests detected")
        return
    print("http/https requests detected:\n")
    for i in httpList: print(f"-{i}")

@app.command("s")
def scan(f: Path = typer.Option(default="null", resolve_path=True)):
    """
    Scan apk for malicious patterns
    """
    checkApkInput(f)
    decompileAPK(f)
    collectPatterns(detectionPatterns)
    patternDetection()
    checkDetected()
    scanResult()

@app.command("r")
def requests(f: Path = typer.Option(default="null", resolve_path=True)):
    """
    Scan apk for any http/https requests
    """
    checkApkInput(f)
    decompileAPK(f)
    scanReq()
    reqResult()

@app.command("sr")
def scanAndRequests(f: Path = typer.Option(default="null", resolve_path=True)):
    """
    Scan apk for both malicious patterns and http/https requests
    """
    checkApkInput(f)
    decompileAPK(f)
    collectPatterns(detectionPatterns)
    patternDetection()
    checkDetected()
    scanReq()
    scanResult()
    reqResult()

@app.callback(invoke_without_command=True,context_settings={"allow_extra_args": True, "ignore_unknown_options": True})
def main(ctx: typer.Context, f: Path = typer.Option(default="null",resolve_path=True)):
    """
    Scan apk for malicious patterns with a simplified output
    """
    if ctx.invoked_subcommand is None:
        checkApkInput(f)
        decompileAPK(f)
        collectPatterns(detectionPatterns)
        patternDetection()
        checkDetected()
        simpleScanResult()
        
if __name__ == "__main__":
    app()
