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
f = open("detectionPatterns.json")
detectionPatterns = json.load(f)
detectedPatterns = []
manifestFoundOptionalKeywords = []
javaFoundOptionalKeywords = []

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

def patternDetection(patternName,patternData):
    out = Path(directory).rglob('*')
    global dangerRating
    manifestKeywords = patternData["manifestKeywords"]
    optionalManifestKeywords = patternData["optionalManifestKeywords"]
    javaKeywords = patternData["javaKeywords"]
    javaOptionalKeywords = patternData["javaOptionalKeywords"]
    tempManifestFoundOptionalKeywords = []
    tempJavaFoundOptionalKeywords = []
    #remove already found optional keywords
    optionalManifestKeywords = list(set(optionalManifestKeywords) - set(manifestFoundOptionalKeywords))
    javaOptionalKeywords = list(set(javaOptionalKeywords) - set(javaFoundOptionalKeywords))
    # Searching through Manifest XML file
    for file in out:
        if file.name.endswith("Manifest.xml"):
            with open(file) as manifestFile:
                for line in manifestFile:
                    for manifestKeyword in manifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            manifestKeywords.remove(manifestKeyword)
                    for manifestKeyword in optionalManifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            tempManifestFoundOptionalKeywords.append(manifestKeyword)
                            optionalManifestKeywords.remove(manifestKeyword)
            manifestFile.close()
        elif file.name.endswith(".java"): 

            # Searching through Java files

            keywordFound = False
            optionalKeywordFound = False

            with open(file, encoding='ansi') as readfile:
                for javaKeyword in javaKeywords:
                    if file.read_text(encoding='ansi').find(javaKeyword) != -1:
                        keywordFound = True
                        break

                for javaKeyword in javaOptionalKeywords:
                    if file.read_text(encoding='ansi').find(javaKeyword) != -1:
                        optionalKeywordFound = True
                        break
                    
                if keywordFound:
                        for line in readfile:
                            for javaKeyword in javaKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaKeywords.remove(javaKeyword)
                                    break

                if optionalKeywordFound:
                        for line in readfile:
                            for javaKeyword in javaOptionalKeywords:
                                if line.find(javaKeyword) != -1:
                                    tempJavaFoundOptionalKeywords.append(javaKeyword)
                                    javaOptionalKeywords.remove(javaKeyword)
                                    break
            readfile.close()
    if (manifestKeywords == [] and javaKeywords == []):
        detectedPatterns.append(patternName)
        dangerRating = dangerRating + patternData["dangerRating"]

        manifestFoundOptionalKeywords.extend(tempManifestFoundOptionalKeywords)
        javaFoundOptionalKeywords.extend(tempJavaFoundOptionalKeywords)

def scanResult():
    if (dangerRating <= 49):rating = "Low"
    elif (dangerRating <= 74):rating = "Medium"
    else:rating = "High"
    print("Danger rating scale:\n0-49 Low risk\n50-74: Medium risk\n75-100: High risk")
    print("------------------------------------------------------------------------------")
    if (detectedPatterns == []):
        print("No malicous paterns detected")
        print(f"App danger rating: {rating} risk({dangerRating})")
        return
    print(f"App danger rating: {rating} risk({dangerRating})")
    print(f"\nPaterns detected:")
    for i in detectedPatterns: print(f"-{i}")
    if (manifestFoundOptionalKeywords != []):
        print(f"\nOptional manifest keywords found:")
        for i in manifestFoundOptionalKeywords: print(f"-{i}")
    if (javaFoundOptionalKeywords != []):
        print(f"\nOptional java keywords found:")
        for i in javaFoundOptionalKeywords: print(f"-{i}")
        

@app.command()
def main(f: Path = typer.Option(default=True, resolve_path=True,)):
    checkApkInput(f)
    print("------------------------------------------------------------------------------")
    os.environ["PATH"] = f"{os.environ['PATH']};.\jadx\\bin\\"
    # Remove existing out directory from previous scan
    if(os.path.exists("out")):
        shutil.rmtree("out")            
    os.system(f'jadx -d out /"{f}"')
    for patternName in detectionPatterns:
        patternDetection(patternName,detectionPatterns[patternName])
    scanResult()
    
 
if __name__ == "__main__":
    app()