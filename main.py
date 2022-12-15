import typer
import os
from pathlib import Path
import shutil #fairly certain this works on linux
# importing element tree
# under the alias of ET
import xml.etree.ElementTree as ET

app = typer.Typer()

directory = 'out'

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
            os.environ["PATH"] = f"{os.environ['PATH']};.\jadx\\bin\\"
            # Remove existing out directory from previous scan
            if(os.path.exists("out")):
                shutil.rmtree("out")
            os.system(f"jadx -d out /{f}")
            extractManifestPerms()
            extractJavaPerms()
            detectAccessibilityUI()
            detectCall()
            detectLogging()
        else:
            text = f.read_text()
            print(f"File is not an apk: {text}")
    # elif f.is_dir():
    #     print("Config is a directory, will use all its config files")
    elif not f.exists():
        print("The APK doesn't exist")

    
 
if __name__ == "__main__":
    app()
