import typer
import os
from pathlib import Path
import json
import shutil #fairly certain this works on linux
# importing element tree
# under the alias of ET
import xml.etree.ElementTree as ET

# initializing global variables
dangerRating = 0
app = typer.Typer()
directory = 'out'
f = open("detectionPaterns.json")
detectionPatterns = json.load(f)
detectedPatterns = []

#collection of all pattern keywords
allManifestKeywords =[]
allOptionalManifestKeywords = []
allJavaKeywords = []
allJavaOptionalKeywords = []

#collection of all found pattern keywords
allManifestFoundKeywords = []
allJavaFoundKeywords = []
allManifestFoundOptionalKeywords = []
allJavaFoundOptionalKeywords = []

validManifestFoundOptionalKeywords = []
validJavaFoundOptionalKeywords = []

def checkApkInput(file):
    apkname = os.path.basename(file)
    if file is None:
        print("No APK file")
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

def extractManifestPerms():
    #may be updated to be more efficient
    out = Path(directory).rglob('*')
    for file in out:
        if file.name.endswith("Manifest.xml"): 
            # Passing the path of the
            # xml document to enable the
            # parsing process
            tree = ET.parse(file)
            # getting the parent tag of
            # the xml document
            root = tree.getroot()
            permissions_list = []
            found_crit_perms = []
            # just an example of critical permissions
            critical_permissions_list = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_EXTERNAL_STORAGE"]

            for permissions in root.iter('uses-permission'):
                # print(permissions.attrib['{http://schemas.android.com/apk/res/android}name'])
                permissions_list.append(permissions.attrib['{http://schemas.android.com/apk/res/android}name'])
            # Remove duplicates from permissions_list
            permissions_list = list(dict.fromkeys(permissions_list))   
            for i in range(len(permissions_list)):
                if (permissions_list[i] in critical_permissions_list):
                    found_crit_perms.append(permissions_list[i])
            print(found_crit_perms, " <- crit perms")
            print("-----------------------------------------")
            return permissions_list

def extractJavaPerms(): 

    out = Path(directory).rglob('*')

    log = ""

    keywords = ["checkCallingPermission", "android.permission"]
    for file in out:
        if file.name.endswith(".java"): 
            log += file.__str__() + "\n"
            linecount = 0
            keywordFound = False

            # javafile = file.name
            with open(file) as readfile:
                for keyword in keywords:
                    if file.read_text().find(keyword) != -1:
                        keywordFound = True
                        break

                if keywordFound:
                        for line in readfile:
                            linecount += 1
                            for keyword in keywords:
                                if line.find(keyword) != -1:
                                    print(keyword + " Found")
                                    log += "Line " + linecount.__str__() + " - " + line.lstrip() + "\n"
                                    break
                else:
                    log += "No keywords found\n"
            
            log += "-----------------------------------------------------------\n\n"


    with open("javaperms.txt", "w") as javapermslog:
        javapermslog.write(log)

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
        
    #remove duplicates
    allManifestKeywords= list(set(allManifestKeywords)) 
    allOptionalManifestKeywords = list(set(allOptionalManifestKeywords))
    allJavaKeywords= list(set(allJavaKeywords))
    allJavaOptionalKeywords=list(set(allJavaOptionalKeywords))

def patternDetection():
    out = Path(directory).rglob('*')
    # Searching through Manifest XML file
    for file in out:
        if file.name.endswith("Manifest.xml"):
            with open(file) as manifestFile:
                for line in manifestFile:
                    for manifestKeyword in allManifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            allManifestFoundKeywords.append(manifestKeyword)
                            allManifestKeywords.remove(manifestKeyword)
                    for manifestKeyword in allOptionalManifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            allManifestFoundOptionalKeywords.append(manifestKeyword)
                            allOptionalManifestKeywords.remove(manifestKeyword)
            manifestFile.close()
        elif file.name.endswith(".java"): 

            # Searching through Java files

            keywordFound = False
            optionalKeywordFound = False

            with open(file, encoding='ansi') as readfile:
                for javaKeyword in allJavaKeywords:
                    if file.read_text(encoding='ansi').find(javaKeyword) != -1:
                        keywordFound = True
                        break

                for javaKeyword in allJavaOptionalKeywords:
                    if file.read_text(encoding='ansi').find(javaKeyword) != -1:
                        optionalKeywordFound = True
                        break
                    
                if keywordFound:
                        for line in readfile:
                            for javaKeyword in allJavaKeywords:
                                if line.find(javaKeyword) != -1:
                                    allJavaFoundKeywords.append(javaKeyword)
                                    allJavaKeywords.remove(javaKeyword)
                                    break

                if optionalKeywordFound:
                        for line in readfile:
                            for javaKeyword in allJavaOptionalKeywords:
                                if line.find(javaKeyword) != -1:
                                    allJavaFoundOptionalKeywords.append(javaKeyword)
                                    allJavaOptionalKeywords.remove(javaKeyword)
                                    break
            readfile.close()
    

def checkDetected():
    global dangerRating
    for patternName in detectionPatterns:
        patternData = detectionPatterns[patternName]
        manifestKeywords = (patternData["manifestKeywords"])
        optionalManifestKeywords = (patternData["optionalManifestKeywords"])
        javaKeywords = (patternData["javaKeywords"])
        javaOptionalKeywords = (patternData["javaOptionalKeywords"])

        manifestKeywords = list(set(manifestKeywords)-set(allManifestFoundKeywords))
        javaKeywords = list(set(javaKeywords)-set(allJavaFoundKeywords))

        if (manifestKeywords == [] and javaKeywords == []):
            detectedPatterns.append(patternName)
            dangerRating = dangerRating + patternData["dangerRating"]
            validManifestFoundOptionalKeywords.extend(list(set(optionalManifestKeywords).intersection(allManifestFoundOptionalKeywords)))
            validJavaFoundOptionalKeywords.extend(list(set(javaOptionalKeywords).intersection(allJavaFoundOptionalKeywords)))

    list(set(validManifestFoundOptionalKeywords))
    list(set(validJavaFoundOptionalKeywords))

        

def scanResult():
    if (dangerRating <= 49):rating = "Low"
    elif (dangerRating <= 74):rating = "Medium"
    else :rating = "High"
    print("Danger rating scale:\n0-49 Low risk\n50-74: Medium risk\n75-100: High risk")
    print("------------------------------------------------------------------------------")
    if (detectedPatterns == []):
        print("No malicous paterns detected")
        print(f"App danger rating: {rating} risk({dangerRating})")
        return
    print(f"App danger rating: {rating} risk({dangerRating})")
    print(f"\nPaterns detected:")
    for i in detectedPatterns: print(f"-{i}")
    if (validManifestFoundOptionalKeywords != []):
        print(f"\nOptional manifest keywords found:")
        for i in validManifestFoundOptionalKeywords: print(f"-{i}")
    if (validJavaFoundOptionalKeywords != []):
        print(f"\nOptional java keywords found:")
        for i in validJavaFoundOptionalKeywords: print(f"-{i}")
        

@app.command()
def main(f: Path = typer.Option(default=True, resolve_path=True,)):
    checkApkInput(f)
    print("------------------------------------------------------------------------------")
    os.environ["PATH"] = f"{os.environ['PATH']};.\jadx\\bin\\"
    # Remove existing out directory from previous scan
    if(os.path.exists("out")):
        shutil.rmtree("out")            
    os.system(f'jadx -d out /"{f}"')
    collectPatterns(detectionPatterns)
    patternDetection()
    checkDetected()
    scanResult()
    
 
if __name__ == "__main__":
    app()