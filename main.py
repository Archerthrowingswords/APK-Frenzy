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
#will modify to remove the duplicate found optional keywords
manifestFoundOptionalKeywords = []
javaFoundOptionalKeywords = []

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

def detectAccessibilityUI():
    out = Path(directory).rglob('*')

    manifestKeywords = ["android.permission.BIND_ACCESSIBILITY_SERVICE", "android.accessibilityservice.AccessibilityService", "android.accessibilityservice", 'canRetrieveWindowContent="true"']
    optionalManifestKeyword = ["accessibilityEventTypes", "setServiceInfo()"]
    foundOptionalKeywords = []

    javaKeywords = ["onAccessibilityEvent"]
    javaOptionalKeywords = ['Intent("android.settings.ACCESSIBILITY_SETTINGS")']
    javaFoundOptionalKeywords = []

    # Searching through Manifest XML file
    for file in out:
        if file.name.endswith("Manifest.xml"):
            with open(file) as manifestFile:
                for line in manifestFile:
                    for manifestKeyword in manifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            manifestKeywords.remove(manifestKeyword)
                    for manifestKeyword in optionalManifestKeyword:
                        if line.find(manifestKeyword) != -1:
                            foundOptionalKeywords.append(manifestKeyword)
                            optionalManifestKeyword.remove(manifestKeyword)

        elif file.name.endswith(".java"): 

            # Searching through Java files

            keywordFound = False
            optionalKeywordFound = False

            with open(file) as readfile:
                for javaKeyword in javaKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        keywordFound = True
                        break

                for javaKeyword in javaOptionalKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        optionalKeywordFound = True
                        break
                    
                if keywordFound:
                        for line in readfile:
                            for javaKeyword in javaKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaKeywords.remove(javaKeyword)
                                    print(javaKeywords)
                                    break

                if optionalKeywordFound:
                        for line in readfile:
                            for javaKeyword in javaOptionalKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaFoundOptionalKeywords.append(javaKeyword)
                                    break

    if (manifestKeywords == [] and javaKeywords == [] and (foundOptionalKeywords != [] or javaOptionalKeywords != [])):
        print("Malicious - Accessibility, found optional keywords")
        if (foundOptionalKeywords != []):
            print(foundOptionalKeywords)
        if (javaOptionalKeywords != []):
            print(javaOptionalKeywords)
    elif (manifestKeywords == [] and javaKeywords == []):
        print("Malicious - Accessibility")

def detectCall():
    out = Path(directory).rglob('*')

    manifestKeywords = ["android.permission.CALL_PHONE"]
    optionalManifestKeyword = []
    foundOptionalKeywords = []

    javaKeywords = ["Intent.ACTION_DIAL"]
    javaOptionalKeywords = []
    javaFoundOptionalKeywords = []

    # Searching through Manifest XML file
    for file in out:
        if file.name.endswith("Manifest.xml"):
            with open(file) as manifestFile:
                for line in manifestFile:
                    for manifestKeyword in manifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            manifestKeywords.remove(manifestKeyword)
                    for manifestKeyword in optionalManifestKeyword:
                        if line.find(manifestKeyword) != -1:
                            foundOptionalKeywords.append(manifestKeyword)
                            optionalManifestKeyword.remove(manifestKeyword)

        elif file.name.endswith(".java"): 

            # Searching through Java files

            keywordFound = False
            optionalKeywordFound = False

            with open(file) as readfile:
                for javaKeyword in javaKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        keywordFound = True
                        break

                for javaKeyword in javaOptionalKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        optionalKeywordFound = True
                        break
                    
                if keywordFound:
                        for line in readfile:
                            for javaKeyword in javaKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaKeywords.remove(javaKeyword)
                                    print(javaKeywords)
                                    break

                if optionalKeywordFound:
                        for line in readfile:
                            for javaKeyword in javaOptionalKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaFoundOptionalKeywords.append(javaKeyword)
                                    break

    if (manifestKeywords == [] and javaKeywords == [] and (foundOptionalKeywords != [] or javaOptionalKeywords != [])):
        print("Malicious - Call, found optional keywords")
        if (foundOptionalKeywords != []):
            print(foundOptionalKeywords)
        if (javaOptionalKeywords != []):
            print(javaOptionalKeywords)
    elif (manifestKeywords == [] and javaKeywords == []):
        print("Malicious - Call")

#not finished
def detectLogging():
    out = Path(directory).rglob('*')

    manifestKeywords = []
    optionalManifestKeyword = []
    foundOptionalKeywords = []

    javaKeywords = []
    javaOptionalKeywords = ["Logger.", "Log."]
    javaFoundOptionalKeywords = []

    # Searching through Manifest XML file
    for file in out:
        if file.name.endswith("Manifest.xml"):
            with open(file) as manifestFile:
                for line in manifestFile:
                    for manifestKeyword in manifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            manifestKeywords.remove(manifestKeyword)
                    for manifestKeyword in optionalManifestKeyword:
                        if line.find(manifestKeyword) != -1:
                            foundOptionalKeywords.append(manifestKeyword)
                            optionalManifestKeyword.remove(manifestKeyword)

        elif file.name.endswith(".java"): 

            # Searching through Java files

            keywordFound = False
            optionalKeywordFound = False

            with open(file) as readfile:
                for javaKeyword in javaKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        keywordFound = True
                        break

                for javaKeyword in javaOptionalKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        optionalKeywordFound = True
                        break
                    
                if keywordFound:
                        for line in readfile:
                            for javaKeyword in javaKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaKeywords.remove(javaKeyword)
                                    print(javaKeywords)
                                    break

                if optionalKeywordFound:
                        for line in readfile:
                            for javaKeyword in javaOptionalKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaFoundOptionalKeywords.append(javaKeyword)
                                    break

    if (manifestKeywords == [] and javaKeywords == [] and (foundOptionalKeywords != [] or javaOptionalKeywords != [])):
        print("Malicious - Logging, found optional keywords")
        if (foundOptionalKeywords != []):
            print(foundOptionalKeywords)
        if (javaOptionalKeywords != []):
            print(javaOptionalKeywords)
    elif (manifestKeywords == [] and javaKeywords == []):
        print("Malicious - Logging")

def detectCallMonitoring():
    out = Path(directory).rglob('*')
    #and list, all of the following have to be true
    manifestKeywords = ["android.permission.READ_PHONE_STATE","android.permission.CALL_PHONE", "android.permission.READ_CONTACTS", "android.permission.INTERNET","android.permission.GET_TASKS" ]
    #or list, any of the following items may appear and will be reported
    optionalManifestKeyword = ["android.permission.WRITE_EXTERNAL_STORAGE","android.permission.PROCESS_OUTGOING_CALLS", "android.permission.RECEIVE_SMS"]
    foundOptionalKeywords = []

    javaKeywords = ["android.intent.action.PHONE_STATE"]
    javaOptionalKeywords = ["Logger.", "Log.","android.intent.action.NEW_OUTGOING_CALL"]
    javaFoundOptionalKeywords = []

    # Searching through Manifest XML file
    for file in out:
        if file.name.endswith("Manifest.xml"):
            with open(file) as manifestFile:
                for line in manifestFile:
                    for manifestKeyword in manifestKeywords:
                        if line.find(manifestKeyword) != -1:
                            manifestKeywords.remove(manifestKeyword)
                    for manifestKeyword in optionalManifestKeyword:
                        if line.find(manifestKeyword) != -1:
                            foundOptionalKeywords.append(manifestKeyword)
                            optionalManifestKeyword.remove(manifestKeyword)

        elif file.name.endswith(".java"): 

            # Searching through Java files

            keywordFound = False
            optionalKeywordFound = False

            with open(file) as readfile:
                for javaKeyword in javaKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        keywordFound = True
                        break

                for javaKeyword in javaOptionalKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        optionalKeywordFound = True
                        break
                    
                if keywordFound:
                        for line in readfile:
                            for javaKeyword in javaKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaKeywords.remove(javaKeyword)
                                    print(javaKeywords)
                                    break

                if optionalKeywordFound:
                        for line in readfile:
                            for javaKeyword in javaOptionalKeywords:
                                if line.find(javaKeyword) != -1:
                                    javaFoundOptionalKeywords.append(javaKeyword)
                                    break

    if (manifestKeywords == [] and javaKeywords == [] and (foundOptionalKeywords != [] or javaOptionalKeywords != [])):
        print("Malicious - Call monitoring, found optional keywords")
        if (foundOptionalKeywords != []):
            print(foundOptionalKeywords)
        if (javaOptionalKeywords != []):
            print(javaOptionalKeywords)
    elif (manifestKeywords == [] and javaKeywords == []):
        print("Malicious - Call monitoring")

def patternDetection(patternName,patternData):
    out = Path(directory).rglob('*')
    global dangerRating
    manifestKeywords = patternData["manifestKeywords"]
    optionalManifestKeywords = patternData["optionalManifestKeywords"]
    javaKeywords = patternData["javaKeywords"]
    javaOptionalKeywords = patternData["javaOptionalKeywords"]
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
                            manifestFoundOptionalKeywords.append(manifestKeyword)
                            optionalManifestKeywords.remove(manifestKeyword)
            manifestFile.close()
        elif file.name.endswith(".java"): 

            # Searching through Java files

            keywordFound = False
            optionalKeywordFound = False

            with open(file) as readfile:
                for javaKeyword in javaKeywords:
                    if file.read_text().find(javaKeyword) != -1:
                        keywordFound = True
                        break

                for javaKeyword in javaOptionalKeywords:
                    if file.read_text().find(javaKeyword) != -1:
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
                                    javaFoundOptionalKeywords.append(javaKeyword)
                                    javaOptionalKeywords.remove(javaKeyword)
                                    break
            readfile.close()
    if (manifestKeywords == [] and javaKeywords == []):
        detectedPatterns.append(patternName)
        dangerRating = dangerRating + patternData["dangerRating"]

def scanResult():
    print("Danger rating scale:\n0-49 low risk\n50-74: medium risk\n75-100: high risk")
    print("------------------------------------------------------------------------------")
    if (detectedPatterns != []):
        print(f"paterns detected:\n{detectedPatterns}")
        print(f"App danger rating:\n{dangerRating}")
        if (manifestFoundOptionalKeywords != []):
            print("------------------------------------------------------------------------------")
            print(f"Optional manifest keywords found:\n{manifestFoundOptionalKeywords}")
        if (javaFoundOptionalKeywords != []):
            print("------------------------------------------------------------------------------")
            print(f"Optional java keywords found:\n{javaFoundOptionalKeywords}")
    else:
        print("No malicous paterns detected")
        print(f"App danger rating:\n{dangerRating}")

@app.command()
def main(f: Path = typer.Option(default=True, resolve_path=True,)):
    apkname = os.path.basename(f)
    if f is None:
        print("No APK file")
        raise typer.Abort()
    if f.is_file():
        if apkname.endswith(".apk"):
            print("------------------------------------------------------------------------------")
            print(f"is APK: {apkname}")
            # os.environ["PATH"] = f"{os.environ['PATH']};.\jadx\\bin\\"
            # # Remove existing out directory from previous scan
            # if(os.path.exists("out")):
            #     shutil.rmtree("out")
            # os.system(f"jadx -d out /{f}")
            for patternName in detectionPatterns:
                # print(patternName)
                # print(detectionPatterns[patternName])
                patternDetection(patternName,detectionPatterns[patternName])
                # detectCallMonitoring()
            # print(detectedPatterns)
            scanResult()
        else:
            text = f.read_text()
            print(f"File is not an apk: {text}")
    elif f.is_dir():
        print("Config is a directory, please specify a file")
    elif not f.exists():
        print("The APK doesn't exist")

    
 
if __name__ == "__main__":
    app()