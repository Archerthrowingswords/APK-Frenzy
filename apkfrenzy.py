import typer
import os
from pathlib import Path
import json
import shutil
import re
import subprocess
from datetime import datetime

# initializing global variables
abPath = Path(__file__).parent.resolve()
dangerRating = 0
app = typer.Typer()
state = {"verbose": False}
outDirectory = f"{abPath}/out"
f = open(f"{abPath}/detectionPatterns.json")
detectionPatterns = json.load(f)
detectedPatterns = {}
callbackFilePath = None

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
    output = ""
    output +="\n=================================================================\n"
    output +="||   ___  ______ _   ______________ _____ _   _  ________   __ ||\n"
    output +="||  / _ \ | ___ \ | / /|  ___| ___ \  ___| \ | ||___  /\ \ / / ||\n"
    output +="|| / /_\ \| |_/ / |/ / | |_  | |_/ / |__ |  \| |   / /  \ V /  ||\n"
    output +="|| |  _  ||  __/|    \ |  _| |    /|  __||     |  / /    \ /   ||\n"
    output +="|| | | | || |   | |\  \| |   | |\ \| |___| |\  | / /___  | |   ||\n"
    output +="|| \_| |_/\_|   \_| \_/\_|   \_| \_\____/\_| \_/\_____/  \_/   ||\n"
    output +="||                                                             ||\n"
    output +="=================================================================\n"
    return output
            
def checkApkInput(file, apkname):
    if file is None:
        print("No APK file")
        raise typer.Abort()
    elif apkname=="null":
        print("No File has been specified, please specify a File")
        raise typer.Abort()
    elif file.is_dir():
        print("Config is a directory, please specify a File")
        raise typer.Abort()
    elif not file.exists():
        print("The APK doesn't Exist")
        raise typer.Abort()
    elif not apkname.endswith(".apk"):
        print(f"File is not an APK")
        raise typer.Abort()
    print(apkFrenzyIntro())
    print(f"Filename: ({apkname})")
    print("-------------------------------------------------------------")

def decompileAPK(file):
    os.environ["PATH"] = f"{os.environ['PATH']};{abPath}\jadx\\bin\\"
    # Remove existing output directory from previous scan
    if(os.path.exists(outDirectory)):
        shutil.rmtree(outDirectory)
    os.mkdir(outDirectory)
    if (os.name == "nt"):       
        subprocess.run(f'jadx -d "{outDirectory}" /"{file}"',shell=True)
    elif (os.name == "posix"):
        command = f'jadx/bin/jadx -d "{outDirectory}" /"{file}"'
        subprocess.run(command,shell=True)
    if(len(os.listdir(outDirectory))==0):
        print("\nThis specific APK File is not able to be decompiled")
        raise typer.Abort()

def checkIfDecompile(f):
    global callbackFilePath
    if (f == None) and (callbackFilePath != None):
        f = callbackFilePath
    if f == None: 
        if(os.path.exists(outDirectory) and len(os.listdir(outDirectory))!=0):
            print(apkFrenzyIntro())
            print(f"Scanning Extracted Files in {abPath}\out folder")
            print("-------------------------------------------------------------")
            return "out-folder"
        else: 
            print("Please use --f to specify an APK as no pre-decompiled APK code exists\nFor more information use --help")
            raise typer.Abort()
    else:
        apkname = os.path.basename(f)
        checkApkInput(f,apkname)
        decompileAPK(f)
        return apkname

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
    out = Path(outDirectory).rglob('*')
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
                                break
                    #looking through the line for optional manifest keywords
                    for manifestKeyword in allOptionalManifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            allManifestFoundOptionalKeywords.append(manifestKeyword)
                            allOptionalManifestKeywords.remove(manifestKeyword)
            manifestFile.close()
        elif file.name.endswith(".java"): 
            # Searching through Java files
            fileText = file.read_text(encoding='utf-8')
            for javaKeyword in allJavaKeywords:
                if fileText.find(javaKeyword) != -1:
                    allJavaFoundKeywords.append(javaKeyword)
                    allJavaKeywords.remove(javaKeyword)
            #looking through the file for alternate java keywords
            for javaKeywordList in allJavaAlternateKeyswords:
                if type(javaKeywordList) is not list: continue
                for alternateKeyword in javaKeywordList:
                    if fileText.find(alternateKeyword) != -1:
                        allJavaFoundAlternateKeywords.append(javaKeywordList)
                        allJavaAlternateKeyswords.remove(javaKeywordList)
                        break
            #looking through the file for optional java keywords
            for javaKeyword in allJavaOptionalKeywords:
                if fileText.find(javaKeyword) != -1:
                    allJavaFoundOptionalKeywords.append(javaKeyword)
                    allJavaOptionalKeywords.remove(javaKeyword)
    
def checkDetected(detectionPatterns):
    global dangerRating
    for patternName in detectionPatterns:
        patternData = detectionPatterns[patternName]
        manifestKeywords = (patternData["manifestKeywords"])
        alternateManifestKeywords = (patternData["alternateManifestKeywords"])
        optionalManifestKeywords = (patternData["optionalManifestKeywords"])
        javaKeywords = (patternData["javaKeywords"])
        alternateJavaKeywords = (patternData["alternateJavaKeywords"])
        javaOptionalKeywords = (patternData["javaOptionalKeywords"])
        
        manifestKeywords = list(set(manifestKeywords)-set(allManifestFoundKeywords))
        javaKeywords = list(set(javaKeywords)-set(allJavaFoundKeywords))

        for manifestList in alternateManifestKeywords:
            if manifestList in allManifestFoundAlternateKeywords: alternateManifestKeywords.remove(manifestList)

        for javaList in alternateJavaKeywords:
            if javaList in allJavaFoundAlternateKeywords:alternateJavaKeywords.remove(javaList)

        if (manifestKeywords==[] and javaKeywords==[] and alternateManifestKeywords==[] and alternateJavaKeywords==[]):
            detectedPatterns[patternName] = [patternData["description"],patternData["dangerRating"]]
            dangerRating = dangerRating + patternData["dangerRating"]
            validManifestFoundOptionalKeywords.extend(list(set(optionalManifestKeywords).intersection(allManifestFoundOptionalKeywords)))
            validJavaFoundOptionalKeywords.extend(list(set(javaOptionalKeywords).intersection(allJavaFoundOptionalKeywords)))

    list(set(validManifestFoundOptionalKeywords))
    list(set(validJavaFoundOptionalKeywords))

def scanReq():
    out = Path(outDirectory).rglob('*')
    global httpList
    httpList = []
    for file in out:
        if file.name.endswith(".java"):
            #looking through the file for http or https requests
            httpPattern = r'"https?://\S+"'
            temphttpList = re.findall(httpPattern, file.read_text(encoding='utf-8'))
            httpList.extend(temphttpList)
    list(set(httpList))
    httpList.sort()

def simpleScanResult():
    global dangerRating
    output = ""
    if(dangerRating > 99): 
        dangerRating = 99
        output += "We have capped the malicious confidence rating at 99% as we can never be completely sure that the APK is malicious\n"
    bar = '{:░<20}'.format('█'*(dangerRating//5))
    output += f"\nMalicious Confidence Rating: {bar} {dangerRating}% (Probability of APK being Malicious)\n"
    output +="-------------------------------------------------------------\n"
    if (detectedPatterns == {}):
        output +="No Malicous Patterns Detected\n"
        return output
    output +="Pattern(s) Detected:\n"
    for i in detectedPatterns: output += f"-{i}\n"
    output +="-------------------------------------------------------------"
    return output

   
def scanResult():
    global dangerRating
    output = ""
    if(dangerRating > 99): 
        dangerRating = 99
        output +="We have capped the malicious confidence rating at 99% as we can never be completely sure that the APK is malicious\n"
    bar = '{:░<20}'.format('█'*(dangerRating//5))
    output +=f"\nMalicious Confidence Rating: {bar} {dangerRating}% (Probability of APK being Malicious)\n"
    output +="-------------------------------------------------------------\n"
    if (detectedPatterns == {}):
        output +="No Malicous Patterns Detected\n"
        output +="-------------------------------------------------------------\n"
        return output
    output +="Pattern(s) Detected:\n"
    for i in detectedPatterns: output +=f"\n-{i} (+{detectedPatterns[i][1]}%)\n{detectedPatterns[i][0]}\n"
    if (validManifestFoundOptionalKeywords != []):
        output +="\nInteresting Manifest Keywords Found:\n"
        for i in validManifestFoundOptionalKeywords: output +=f"-{i}\n"
    if (validJavaFoundOptionalKeywords != []):
        output +="\nInteresting Java Keywords Found:\n"
        for i in validJavaFoundOptionalKeywords: output +=f"-{i}\n"
    output +="-------------------------------------------------------------\n"
    return output
    
def reqResult():
    global httpList
    output = ""
    if (httpList == []):
        output +="No HTTP/HTTPS Requests Detected\n"
        return output
    output +="HTTP/HTTPS Requests Detected:\n"
    for i in httpList: output +=f"-{i}\n"
    return output

@app.command("l")
def scanLog( f: Path = typer.Option(default=None, resolve_path=True)):
    """
    Scan APK for Malicious Patterns with a simple scan then log them in a file
    """
    log = apkFrenzyIntro()
    apkname = checkIfDecompile(f)
    collectPatterns(detectionPatterns)
    patternDetection()
    checkDetected(detectionPatterns)
    result = simpleScanResult()
    print(result)
    #saving results to a file
    log += result
    now = datetime.now()
    ts = f"{now.year}-{now.month}-{now.day}_{now.hour}-{now.minute}-{now.second}"
    f = open(f"{apkname}-scan-{ts}.txt", "w", encoding="utf-8")
    f.write(log)
    f.close()

@app.command("s")
def verboseScan( f: Path = typer.Option(default=None, resolve_path=True)):
    """
    Scan APK for Malicious Patterns and Provide More Information
    """
    checkIfDecompile(f)
    collectPatterns(detectionPatterns)
    patternDetection()
    checkDetected(detectionPatterns)
    print(scanResult())

@app.command("sl")
def verboseScanLog( f: Path = typer.Option(default=None, resolve_path=True)):
    """
    Scan APK for Malicious Patterns and Provide More Information then log them in a file
    """
    log = apkFrenzyIntro()
    apkname = checkIfDecompile(f)
    collectPatterns(detectionPatterns)
    patternDetection()
    checkDetected(detectionPatterns)
    result = scanResult()
    print(result)
    #saving results to a file
    log += result
    now = datetime.now()
    ts = f"{now.year}-{now.month}-{now.day}_{now.hour}-{now.minute}-{now.second}"
    f = open(f"{apkname}-scan-{ts}.txt", "w", encoding="utf-8")
    f.write(log)
    f.close()

@app.command("r")
def requests( f: Path = typer.Option(default=None, resolve_path=True)):
    """
    Scan APK for any HTTP/HTTPS Requests
    """
    checkIfDecompile(f)
    scanReq()
    print(reqResult())

@app.command("rl")
def requestsLog( f: Path = typer.Option(default=None, resolve_path=True)):
    """
    Scan APK for any HTTP/HTTPS Requests then log them in a file
    """
    log = apkFrenzyIntro()
    apkname = checkIfDecompile(f)
    scanReq()
    result = reqResult()
    print(result)
    #saving results to a file
    log += result
    now = datetime.now()
    ts = f"{now.year}-{now.month}-{now.day}_{now.hour}-{now.minute}-{now.second}"
    f = open(f"{apkname}-scan-{ts}.txt", "w", encoding="utf-8")
    f.write(log)
    f.close()

@app.command("sr")
def scanAndRequests( f: Path = typer.Option(default=None, resolve_path=True)):
    """
    Scan APK for both Malicious Patterns and HTTP/HTTPS Requests
    """
    result = ""
    checkIfDecompile(f)
    collectPatterns(detectionPatterns)
    patternDetection()
    checkDetected(detectionPatterns)
    scanReq()
    result += scanResult()
    result += reqResult()
    print(result)

@app.command("srl")
def scanAndRequestsLog( f: Path = typer.Option(default=None, resolve_path=True)):
    """
    Scan APK for both Malicious Patterns and HTTP/HTTPS Requests then log them in a file
    """
    result = ""
    log = apkFrenzyIntro()
    apkname = checkIfDecompile(f)
    collectPatterns(detectionPatterns)
    patternDetection()
    checkDetected(detectionPatterns)
    scanReq()
    result += scanResult()
    result += reqResult()

    print(result)
    #saving results to a file
    log += result
    now = datetime.now()
    ts = f"{now.year}-{now.month}-{now.day}_{now.hour}-{now.minute}-{now.second}"
    f = open(f"{apkname}-scan-{ts}.txt", "w", encoding="utf-8")
    f.write(log)
    f.close()
    
@app.callback(invoke_without_command=True,context_settings={"allow_extra_args": True, "ignore_unknown_options": True})
def main(ctx: typer.Context, f: Path = typer.Option(default=None,resolve_path=True)):
    """
    Scan APK for Malicious Patterns with a Simplified Output
    """
    global callbackFilePath
    # only run if there is no command
    if ctx.invoked_subcommand is None:
        checkIfDecompile(f)
        collectPatterns(detectionPatterns)
        patternDetection()
        checkDetected(detectionPatterns)
        result = simpleScanResult()
        print(result)
    else:
        # if user passed path into callback pass it into global var
        if f != None: 
            callbackFilePath = f
        
if __name__ == "__main__":
    app()
